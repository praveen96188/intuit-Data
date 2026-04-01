#!/bin/bash

###################
BOLD=`tput bold`
RED=`tput setaf 1`
GREEN=`tput setaf 2`
YELLOW=`tput setaf 3`
RESET=`tput sgr0`
WARN="${YELLOW}${BOLD}WARN:${RESET}"
ERROR="${RED}${BOLD}ERROR:${RESET}"
###################

ROOT=$(git rev-parse --show-toplevel)
CONFIG_JSON_PATH="${ROOT}/config.json"
DEPLOY_JSON_PATH="${ROOT}/deploy.json"

# TODO: for now, this script assumes you have PowerUser credentials fetched already
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

CONFIG_JSON=""
DEPLOY_JSON=""
ENV=""
GROUP=""
CLUSTER=""

usage() {
  echo "Usage:"
  echo "  manage-password.sh"
  echo "    -e : environment to change passwords in"
  echo "    -g : [Optional] group to change passwords for (default: all groups in specified ENV)"
  echo "    -c : [Optional] cluster id to change password for (default: all clusters in specified ENV/GROUP)"
  echo "    -f : [Optional] path to config.json (default: repo root directory)"
  echo "    -d : [Optional] path to deploy.json (default: repo root directory)"
}

check_tools() {
  echo "${GREEN}${BOLD}Checking tools...${RESET}"
  eiamcli --version
  if [[ $? -ne 0 ]]; then echo "${ERROR} script requires eiamcli: https://github.intuit.com/EIAM/eiamCLI"; exit 1; fi
  aws --version
  if [[ $? -ne 0 ]]; then echo "${ERROR} script requires awscli: https://aws.amazon.com/cli/"; exit 1; fi
  jq --version
  if [[ $? -ne 0 ]]; then echo "${ERROR} script requires jq: https://stedolan.github.io/jq/"; exit 1; fi
}

# TODO: once deploy.json pairs clusters with targets, we can dynamically switch to proper creds for each cluster
eiamcli_fetch_keys() {
  ACCOUNT_ID=$1
  AWS_PROFILE=$2
  echo "${GREEN}${BOLD}Fetching PowerUser access keys (account: ${ACCOUNT_ID}, profile: ${AWS_PROFILE})${RESET}"
  echo "eiamcli getAWSTempCredentials -a ${ACCOUNT_ID} -r PowerUser -p ${AWS_PROFILE}"
  eiamcli getAWSTempCredentials -a ${ACCOUNT_ID} -r PowerUser -p ${AWS_PROFILE} > /dev/null
  if [[ $? -ne 0 ]]; then echo "${ERROR} eiamcli returned an error"; exit 1; fi
}

check_clusters() {
  ACCOUNT_ID=$1
  CLUSTER_ID=$2
  found=$(aws rds describe-db-clusters --query DBClusters[].DBClusterIdentifier | grep -c "${CLUSTER_ID}")
  if [[ $found == 0 ]]; then
    echo "${ERROR} for cluster id ${YELLOW}${CLUSTER_ID}${RESET}, found zero clusters in account ${ACCOUNT_ID}."
    exit 1
  fi
}

update_password() {
  echo "${GREEN}${BOLD}Checking config...${RESET}"

  # verify cluster id uniqueness
  CLUSTER_IDS=$(echo ${CONFIG_JSON} | jq '[getpath(paths | select(.[-1] == "clusters"))[].id]')
  if echo ${CLUSTER_IDS} | jq -r '.[]' | sort | uniq -c | awk {'print $1'} | grep -qvE "^1$"; then
    echo "${ERROR} cluster ids (config.json) must be unique"
    exit 1
  fi

  # verify target name uniqueness
  TARGET_CONFIG=$(echo ${DEPLOY_JSON} | jq '.targets')
  TARGET_NAMES=$(echo ${TARGET_CONFIG} | jq '[.[].name]')
  if echo ${TARGET_NAMES} | jq -r '.[]' | sort | uniq -c | awk {'print $1'} | grep -qvE "^1$"; then
    echo "${ERROR} target names (deploy.json) must be unique"
    exit 1
  fi

  # full cluster identifier will be ${datastore_id}-${env_name}-${cluster_id}
  datastore_id=$(echo ${CONFIG_JSON} | jq -r '.id')

  # verify env
  ENV_CONFIG=$(echo ${CONFIG_JSON} | jq --arg e "${ENV}" '.environments[] | select(.name == $e)')
  if [[ -z ${ENV_CONFIG} ]] || [[ "${ENV_CONFIG}" == "null" ]]; then
    echo "${ERROR} environment '$ENV' does not exist in config.json"
    exit 1
  fi

  # collect new password for the selected clusters
  echo "Setting new master password for env=${ENV}, group=${GROUP:-ALL}, cluster=${CLUSTER:-ALL}"
  echo "${GREEN}${BOLD}Enter a password:${RESET}"
  read -s new_password

  echo "${GREEN}${BOLD}Re-enter the password:${RESET}"
  read -s new_password_confirm

  if [[ "${new_password}" != "${new_password_confirm}" ]]; then
    echo "${ERROR} passwords do not match, exiting"
    exit 1
  fi

  # iterate configs, find target clusters
  groupct=$(echo ${ENV_CONFIG} | jq '.groups | length')
  for (( i=0; i<$groupct; i++ )); do
    GROUP_CONFIG=$(echo ${ENV_CONFIG} | jq --argjson i "$i" '.groups[$i]')
    group_name=$(echo ${GROUP_CONFIG} | jq -r '.name')

    # if the -g option was provided, ignore non matching groups
    if [[ ! -z ${GROUP} ]] && [[ "${group_name}" != "${GROUP}" ]]; then
      continue
    fi

    clusterct=$(echo ${GROUP_CONFIG} | jq '.clusters | length')
    for (( j=0; j<$clusterct; j++ )); do
      CLUSTER_CONFIG=$(echo ${GROUP_CONFIG} | jq --argjson j "$j" '.clusters[$j]')
      cluster_id=$(echo ${CLUSTER_CONFIG} | jq -r '.id')

      # if the -c option was provided, ignore non matching clusters
      if [[ ! -z ${CLUSTER} ]] && [[ "${cluster_id}" != "${CLUSTER}" ]]; then
        continue
      fi

      # assemble full cluster id
      full_cluster_id="${datastore_id}-${ENV}-${cluster_id}"
      check_clusters "${ACCOUNT_ID}" "${full_cluster_id}"

      echo "${GREEN}${BOLD}Updating password for cluster: ${full_cluster_id}${RESET}"

      # send updated password to cluster (and try not to echo the password to the console in the process)
      set +x
      aws rds modify-db-cluster --db-cluster-identifier ${full_cluster_id} \
        --apply-immediately --master-user-password ${new_password} \
        --query DBCluster.PendingModifiedValues

      if [[ $? -ne 0 ]]; then
        echo "${ERROR} modify-db-cluster command exited with an error"
        exit 1
      fi

      echo "${GREEN}${BOLD}Note that password update takes a few minutes! Check status with:${RESET}"
      echo "aws rds describe-db-clusters --db-cluster-identifier ${full_cluster_id} --query DBClusters[].Status"

      echo "${GREEN}${BOLD}Updating password (in AWS Secrets Manager) for cluster: ${full_cluster_id}${RESET}"

      # get physical resource id (ARN) of secret for this cluster
      secret_arn=$(aws cloudformation describe-stack-resources --stack-name ${full_cluster_id} | \
        jq -r '.StackResources[] | select(.ResourceType == "AWS::SecretsManager::Secret") | .PhysicalResourceId')

      # pull secret value from aws secrets manager
      secret_value=$(aws secretsmanager get-secret-value --secret-id ${secret_arn} --query SecretString --output text)

      # update password, reinsert modified secret
      updated_secret=$(echo $secret_value | jq -c --arg pw "${new_password}" '.password = $pw')
      aws secretsmanager update-secret --secret-id ${secret_arn} --secret-string ${updated_secret}
    done
  done
}

###################
# main
###################

while getopts ":e:g:c:f:d:" opt; do
  case "${opt}" in
    e)
      ENV="${OPTARG}"
      ;;
    g)
      GROUP="${OPTARG}"
      ;;
    c)
      CLUSTER="${OPTARG}"
      ;;
    f)
      CONFIG_JSON_PATH="${OPTARG}"
      ;;
    d)
      DEPLOY_JSON_PATH="${OPTARG}"
      ;;
    *)
      usage; exit 1;;
  esac
done
shift "$((OPTIND-1))"

check_tools

if [[ -z "${ENV}" ]]; then
  echo "${ERROR} the -e option must be specified; you may only change passwords in one environment at a time."
  usage; exit 1
fi

CONFIG_JSON=$(cat ${CONFIG_JSON_PATH})
DEPLOY_JSON=$(cat ${DEPLOY_JSON_PATH})

update_password