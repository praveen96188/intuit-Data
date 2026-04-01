#!/bin/bash

# Note to future inhabitants of Planet Snapshot-Tool:  all CFN stacks must be run in the SAME REGION as the source
# RDS cluster, in BOTH the source and destination accounts.

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

CONFIG_JSON=""
DEPLOY_JSON=""
TARGET_CONFIG=""
VERIFY=false

usage() {
  echo "Usage:"
  echo "  manage-snapshots.sh"
  echo "    -v : [Optional] verify mode, will check status of stacks/keys/snapshots (default: off)"
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

check_config() {
  echo "${GREEN}${BOLD}Checking config...${RESET}"

  # build flattened array of all snapshot names in config.json
  SNAPSHOT_NAMES=$(echo "${CONFIG_JSON}" | jq '[[getpath(paths | select(.[-1] == "snapshots"))][].name]')

  # verify snapshot name uniqueness
  if echo "${SNAPSHOT_NAMES}" | jq -r '.[]' | sort | uniq -c | awk {'print $1'} | grep -qvE "^1$"; then
    echo "${ERROR} snapshot names (config.json) must be unique"
    exit 1
  fi

  # verify snapshot name format
  if echo "${SNAPSHOT_NAMES}" | jq -r '.[]' | grep -qvE "^[A-Za-z0-9\-]{1,32}$"; then
    echo "${ERROR} snapshot names (config.json) must be alphanumeric (including hyphen) and <= 32 bytes"
    exit 1
  fi

  # cluster ids should be verified elsewhere, but for safety's sake check them here too
  CLUSTER_IDS=$(echo "${CONFIG_JSON}" | jq '[getpath(paths | select(.[-1] == "clusters"))[].id]')

  # verify cluster id uniqueness
  if echo "${CLUSTER_IDS}" | jq -r '.[]' | sort | uniq -c | awk {'print $1'} | grep -qvE "^1$"; then
    echo "${ERROR} cluster ids (config.json) must be unique"
    exit 1
  fi

  # verify cluster id format
  if echo "${CLUSTER_IDS}" | jq -r '.[]' | grep -qvE "^[A-Za-z0-9\-]{1,32}$"; then
    echo "${ERROR} cluster ids (config.json) must be alphanumeric (including hyphen) and <= 32 bytes"
    exit 1
  fi

  # pull target names from deploy.json
  TARGET_CONFIG=$(echo "${DEPLOY_JSON}" | jq '.targets')
  TARGET_NAMES=$(echo "${TARGET_CONFIG}" | jq '[.[].name]')

  # verify target name uniqueness
  if echo "${TARGET_NAMES}" | jq -r '.[]' | sort | uniq -c | awk {'print $1'} | grep -qvE "^1$"; then
    echo "${ERROR} target names (deploy.json) must be unique"
    exit 1
  fi

  # for each target in deploy.json, validate that all required fields are present
  for t in $(echo "${TARGET_NAMES}" | jq -r '.[]'); do
    target=$(echo "${TARGET_CONFIG}" | jq --arg i "$t" '.[] | select(.name == $i)')
    target_account=$(echo "${target}" | jq -r '.accountId')
    target_region=$(echo "${target}" | jq -r '.region')

    # check for required params
    if [[ -z "${target_account// }" ]] ||
       [[ -z "${target_region// }" ]];
    then
      echo "${ERROR} deploy.json is invalid, the following fields are all required for each target config:"
      echo "        name         : unique name for target"
      echo "        accountId    : target account id for snapshot copies (can be same account as source cluster)"
      echo "        region       : target region for snapshot copies"
      echo "        vpcId        : optional for snapshot config, required for actual cluster deployment"
      exit 1
    fi
  done
}

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

snapshot_usage_exit() {
  echo "${ERROR} snapshot spec in config.json is invalid, must use the following format:"
  echo
  echo '    "snapshots": {'
  echo '      "name": "snapshot-1",                       # required'
  echo '      "source_target": "target-1",                # source_target refers to a target in deploy.json'
  echo '      "backup_schedule": "0 1 * * ? *",           # optional'
  echo '      "copy_targets": [                           # required list, targets for copies of this snapshot'
  echo '        {'
  echo '          "dest_target": "region1-copy-target",   # dest_target refers to a target in deploy.json'
  echo '          "retention_days": 3                     # optional'
  echo '        }'
  echo '      ]'
  echo '    }'
  exit 1
}

create_source_stack() {
  SNAPSHOT_NAME=$1
  CLUSTER_PREFIX=$2
  DEST_ACCOUNT=$3
  RETAIN_DAYS=$4
  BACKUP_SCHEDULE=$5

  # ensure each combo of cluster_id and dest_account is deployed in its own source stack.
  # this is required in order to configure separate retention periods per snapshot.
  # it will result in some redundant resources, but should not generate extra snapshots.
  STACK_NAME="${SNAPSHOT_NAME}-SOURCE-${CLUSTER_PREFIX}-${DEST_ACCOUNT}"

  if [[ $VERIFY = true ]]; then
    if STATUS=$(aws cloudformation describe-stacks --stack-name ${STACK_NAME} --query Stacks[0].StackStatus 2>/dev/null); then
      echo "CFN stack ${STACK_NAME} exists, status is: ${YELLOW}${STATUS}${RESET}"
    else
      echo "${ERROR} CFN stack ${STACK_NAME} does not exist"
    fi
    return
  fi

  # verify whether stack exists
  if aws cloudformation describe-stacks --stack-name ${STACK_NAME} >/dev/null 2>&1; then
    echo "${WARN} stack exists already, performing an update"
    CMD="update-stack"
  else
    echo "${WARN} creating a new stack"
    CMD="create-stack"
  fi

  set -x
  aws cloudformation ${CMD} \
    --stack-name "${STACK_NAME}" \
    --template-url "https://ips-datastore-client.s3.us-west-2.amazonaws.com/snapshot/snapshots_tool_aurora_source.json" \
    --capabilities CAPABILITY_IAM \
    --tags Key=vpc_exception,Value=true \
    --parameters \
      "ParameterKey=CodeBucket,ParameterValue=ips-datastore-client" \
      "ParameterKey=ClusterNamePattern,ParameterValue=${CLUSTER_PREFIX}" \
      "ParameterKey=DestinationAccount,ParameterValue=${DEST_ACCOUNT}" \
      "ParameterKey=BackupSchedule,ParameterValue=${BACKUP_SCHEDULE}" \
      "ParameterKey=RetentionDays,ParameterValue=${RETAIN_DAYS}"
  set +x
}

create_dest_stack() {
  SNAPSHOT_NAME=$1
  CLUSTER_PREFIX=$2
  DEST_REGION=$3
  RETAIN_DAYS=$4
  KMS_SOURCE=$5
  KMS_DEST=$6
  CROSS_ACCOUNT=$7

  if [[ "${DEST_REGION}" != "us-west-2" ]] && [[ "${DEST_REGION}" != "us-east-2" ]]; then
    echo "${ERROR} target region not yet supported by IPS (snapshot-tool lambda code needs to be copied to ${DEST_REGION}, ping us and let us know)"
    exit 1
  fi

  # ensure each combo of snapshot_pattern and dest_region is deployed in its own destination stack.
  # this is required in order to configure separate retention periods per snapshot.
  # it will result in some redundant resources, but should not generate extra snapshots.
  STACK_NAME="${SNAPSHOT_NAME}-DEST-${CLUSTER_PREFIX}-${DEST_REGION}"

  if [[ $VERIFY = true ]]; then
    if STATUS=$(aws cloudformation describe-stacks --stack-name ${STACK_NAME} --query Stacks[0].StackStatus 2>/dev/null); then
      echo "CFN stack ${STACK_NAME} exists, status is: ${YELLOW}${STATUS}${RESET}"
      echo "Destination 'SnapshotCreateTime' for snapshots of ${CLUSTER_PREFIX} in ${DEST_REGION}:"
      aws rds describe-db-cluster-snapshots --region ${DEST_REGION} \
        --snapshot-type manual --db-cluster-identifier ${CLUSTER_PREFIX} --query DBClusterSnapshots[].SnapshotCreateTime
    else
      echo "${ERROR} CFN stack ${STACK_NAME} does not exist"
    fi
    return
  fi

  # verify whether stack exists
  if aws cloudformation describe-stacks --stack-name ${STACK_NAME} >/dev/null 2>&1; then
    echo "${WARN} stack exists already, performing an update"
    CMD="update-stack"
  else
    echo "${WARN} creating a new stack"
    CMD="create-stack"
  fi

  set -x
  aws cloudformation ${CMD} \
    --stack-name "${STACK_NAME}" \
    --template-url "https://ips-datastore-client.s3.us-west-2.amazonaws.com/snapshot/snapshots_tool_aurora_dest.json" \
    --capabilities CAPABILITY_IAM \
    --tags Key=vpc_exception,Value=true \
    --parameters \
      "ParameterKey=CodeBucket,ParameterValue=ips-datastore-client" \
      "ParameterKey=SnapshotPattern,ParameterValue=${CLUSTER_PREFIX}" \
      "ParameterKey=DestinationRegion,ParameterValue=${DEST_REGION}" \
      "ParameterKey=RetentionDays,ParameterValue=${RETAIN_DAYS}" \
      "ParameterKey=KmsKeySource,ParameterValue=${KMS_SOURCE}" \
      "ParameterKey=KmsKeyDestination,ParameterValue=${KMS_DEST}" \
      "ParameterKey=CrossAccountCopy,ParameterValue=${CROSS_ACCOUNT}"
  set +x
}

process_snapshots() {
  # full cluster identifier will be ${datastore_id}-${env_name}-${cluster_id}
  datastore_id=$(echo "${CONFIG_JSON}" | jq -r '.id')

  # for each env in config.json
  envct=$(echo "${CONFIG_JSON}" | jq '.environments | length')
  for (( i=0; i<$envct; i++ )); do
    ENV_CONFIG=$(echo "${CONFIG_JSON}" | jq --argjson i "$i" '.environments[$i]')
    env_name=$(echo "${ENV_CONFIG}" | jq -r '.name')

    # for each group in this env
    groupct=$(echo "${ENV_CONFIG}" | jq '.groups | length')
    for (( j=0; j<$groupct; j++ )); do
      GROUP_CONFIG=$(echo "${ENV_CONFIG}" | jq --argjson j "$j" '.groups[$j]')
      group_name=$(echo "${GROUP_CONFIG}" | jq -r '.name')

      # check for snapshot config at group level
      GROUP_SNAPSHOT=$(echo "${GROUP_CONFIG}" | jq '.snapshots')
      if [[ -z "${GROUP_SNAPSHOT// }" ]] || [[ "${GROUP_SNAPSHOT}" == "null" ]]; then
        GROUP_SNAPSHOT=""
      fi

      # for each cluster in this group
      clusterct=$(echo "${GROUP_CONFIG}" | jq '.clusters | length')
      for (( k=0; k<$clusterct; k++ )); do
        CLUSTER_CONFIG=$(echo "${GROUP_CONFIG}" | jq --argjson k "$k" '.clusters[$k]')

        # check for snapshot config at cluster level
        CLUSTER_SNAPSHOT=$(echo "${CLUSTER_CONFIG}" | jq '.snapshots')
        if [[ -z "${CLUSTER_SNAPSHOT// }" ]] || [[ "${CLUSTER_SNAPSHOT}" == "null" ]]; then
          CLUSTER_SNAPSHOT=""
        fi

        SNAPSHOT_CONFIG=""
        if [[ ! -z "${CLUSTER_SNAPSHOT}" ]]; then
          # prefer cluster-level config
          SNAPSHOT_CONFIG="${CLUSTER_SNAPSHOT}"
        elif [[ ! -z "${GROUP_SNAPSHOT}" ]]; then
          # otherwise, fall back to group-level config if present
          SNAPSHOT_CONFIG="${GROUP_SNAPSHOT}"
        else
          # no snapshots defined for this cluster, move along
          continue
        fi

        # start collecting info
        snapshot_name=$(echo "${SNAPSHOT_CONFIG}" | jq -r '.name')
        backup_schedule=$(echo "${SNAPSHOT_CONFIG}" | jq -r '.backup_schedule')
        cluster_id=$(echo "${CLUSTER_CONFIG}" | jq -r '.id')

        # full cluster identifier
        source_cluster_id="${datastore_id}-${env_name}-${cluster_id}"

        source_target=$(echo "${SNAPSHOT_CONFIG}" | jq -r '.source_target')
        if [[ -z "${source_target// }" ]]; then
          snapshot_usage_exit
        fi

        SRC_TARGET=$(echo "${TARGET_CONFIG}" | jq --arg t "${source_target}" '.[] | select(.name == $t)')
        if [[ -z "${SRC_TARGET// }" ]] || [[ "${SRC_TARGET}" == "null" ]]; then
          echo "${ERROR} source target '$source_target' for cluster id '$source_cluster_id' is not defined in deploy.json"
          exit 1
        fi

        source_account_id=$(echo "${SRC_TARGET}" | jq -r '.accountId' | sed 's/-//g')
        source_account_region=$(echo "${SRC_TARGET}" | jq -r '.region')
        source_account_aws_profile="default"

        # check for required source params
        if [[ -z "${snapshot_name// }" ]] ||
           [[ -z "${source_account_id// }" ]] ||
           [[ -z "${source_account_region// }" ]];
        then
          snapshot_usage_exit
        fi

        # set defaults for optional params
        if [[ -z "${backup_schedule// }" ]] || [[ "${backup_schedule}" == "null" ]]; then
          backup_schedule="0 1 * * ? *"
        fi

        echo "${GREEN}---------------------------------------------------${RESET}"
        [[ $VERIFY = true ]] && verb="Checking" || verb="Setting up"
        echo "${GREEN}${BOLD}${verb} snapshots for env=${env_name}, group=${group_name}, cluster=${source_cluster_id}${RESET}"
        echo "${GREEN}---------------------------------------------------${RESET}"

        # for each destination listed in snapshot config
        targetct=$(echo "${SNAPSHOT_CONFIG}" | jq '.copy_targets | length')
        for (( l=0; l<$targetct; l++ )); do
          COPY_CONFIG=$(echo "${SNAPSHOT_CONFIG}" | jq --argjson l "$l" '.copy_targets[$l]')
          retention_days=$(echo "${COPY_CONFIG}" | jq -r '.retention_days')

          dest_target=$(echo "${COPY_CONFIG}" | jq -r '.dest_target')
          if [[ -z "${dest_target// }" ]]; then
            snapshot_usage_exit
          fi

          DEST_TARGET=$(echo "${TARGET_CONFIG}" | jq --arg t "${dest_target}" '.[] | select(.name == $t)')
          if [[ -z "${DEST_TARGET// }" ]] || [[ "${DEST_TARGET}" == "null" ]]; then
            echo "${ERROR} dest target '$dest_target' for cluster id '$source_cluster_id' is not defined in deploy.json"
            exit 1
          fi

          dest_account_id=$(echo "${DEST_TARGET}" | jq -r '.accountId' | sed 's/-//g')
          dest_account_region=$(echo "${DEST_TARGET}" | jq -r '.region')
          dest_account_aws_profile="default"

          # check for required source params
          if [[ -z "${dest_account_id// }" ]] ||
             [[ -z "${dest_account_region// }" ]];
          then
            snapshot_usage_exit
          fi

          # set defaults for optional params
          if [[ -z "${retention_days// }" ]] || [[ "${retention_days}" == "null" ]]; then
            retention_days="7"
          fi

          eiamcli_fetch_keys "${source_account_id}" "${source_account_aws_profile}"
          check_clusters "${source_account_id}" "${source_cluster_id}"

          # check for cross-region copy (within same account)
          if [[ "${source_account_id}" == "${dest_account_id}" ]] &&
             [[ "${source_account_region}" != "${dest_account_region}" ]]; then

            echo "---------------------------------------------------"
            echo "copying cluster snapshots ($source_cluster_id) in account ($source_account_id)"
            echo "  FROM region ($source_account_region)"
            echo "  TO region ($dest_account_region)"
            echo "  with retention_days=$retention_days"
            echo "---------------------------------------------------"

            ########################
            # create 1 KMS key per target region, for cross-region copies
            ########################

            [[ $VERIFY = true ]] && verb="Checking" || verb="Creating"
            echo "${GREEN}${BOLD}${verb} KMS key for encryption of cross-region copies in ${dest_account_region}...${RESET}"

            KMS_ALIAS="alias/${source_cluster_id}-snap-${dest_account_region}"
            KMS_DEST_ARN=$(aws kms describe-key --region ${dest_account_region} \
                            --key-id "${KMS_ALIAS}" --query KeyMetadata.Arn --output text 2>/dev/null)

            if [[ $? -ne 0 ]]; then
              if [[ $VERIFY = true ]]; then
                echo "${ERROR} KMS key ${KMS_ALIAS} not found in destination region: ${dest_account_region}"
              else
                # alias not found, create key
                KMS_DEST_ARN=$(aws kms create-key --region ${dest_account_region} --query KeyMetadata.Arn --output text)
                aws kms create-alias --region ${dest_account_region} --alias-name ${KMS_ALIAS} --target-key-id ${KMS_DEST_ARN}
              fi
            elif [[ $VERIFY = true ]]; then
              echo "KMS key ${KMS_ALIAS} located in destination region ${dest_account_region}: ${YELLOW}${KMS_DEST_ARN}${RESET}"
            fi

            # get the original KMS key for source cluster as well, DEST stack requires both keys
            KMS_SRC_ARN=$(aws rds describe-db-clusters \
              --filters "Name=db-cluster-id,Values=${source_cluster_id}" \
              --query DBClusters[].KmsKeyId --output text)

            ########################
            # run SOURCE cfn stack in source account to create local snapshots
            ########################

            [[ $VERIFY = true ]] && verb="Checking" || verb="Creating"
            echo "${GREEN}${BOLD}${verb} SOURCE stacks for cross-region copies...${RESET}"

            create_source_stack "${snapshot_name}" "${source_cluster_id}" "000000000000" "${retention_days}" \
              "${backup_schedule}"

            ########################
            # run DEST cfn stack in source account to copy as requested (cross-region, same account)
            ########################

            [[ $VERIFY = true ]] && verb="Checking" || verb="Creating"
            echo "${GREEN}${BOLD}${verb} DEST stacks for cross-region copies...${RESET}"

            create_dest_stack "${snapshot_name}" "${source_cluster_id}" "${dest_account_region}" "${retention_days}" \
              "${KMS_SRC_ARN}" "${KMS_DEST_ARN}" "FALSE"

          # otherwise, for cross-account copies...
          elif [[ "${source_account_id}" != "${dest_account_id}" ]]; then

            echo "---------------------------------------------------"
            echo "copying cluster snapshots ($source_cluster_id)"
            echo "  FROM source ($source_account_id, $source_account_region)"
            echo "  TO dest ($dest_account_id) in regions ($dest_account_region)"
            echo "  with retention_days=$retention_days"
            echo "---------------------------------------------------"

            ########################
            # in source account: find KMS keys for RDS cluster, then add targeted dest account to the
            # key policy's principals, so destination can read encrypted snapshots
            ########################

            [[ $VERIFY = true ]] && verb="Checking" || verb="Updating"
            echo "${GREEN}${BOLD}${verb} SOURCE encryption keys for cross-account access...${RESET}"

            # get kms key for cluster
            kms_key_arn=$(aws rds describe-db-clusters \
              --filters "Name=db-cluster-id,Values=${source_cluster_id}" \
              --query DBClusters[].KmsKeyId --output text)

            if [[ $VERIFY = true ]]; then
              echo "Destination account configured for cluster ${source_cluster_id}: ${YELLOW}${dest_account_id}${RESET}"
              echo "Source account KMS key for cluster ${source_cluster_id}: ${YELLOW}${kms_key_arn}${RESET}"
              echo "Principals currently listed in key policy:"
              aws kms get-key-policy --key-id ${kms_key_arn} --policy-name default \
                --output text | jq '[.Statement[].Principal."AWS"]|flatten'
            else
              # get key policy, store in temp file
              FULL_POLICY="tmp_kms_key_policy.json"
              aws kms get-key-policy --key-id ${kms_key_arn} --policy-name default --output text > ${FULL_POLICY}

              # create modifiable copy of limited-scope kms key policy statement
              NEW_STATEMENT="tmp_src_key_policy_stmt.json"
              cp ${ROOT}/manage/snapshots/src_key_policy_stmt.json ${NEW_STATEMENT}

              # add dest_account_id to key policy statement (if not already present)
              ROOT_ARN="arn:aws:iam::${dest_account_id}:root"
              if ! grep -qc "${ROOT_ARN}" ${NEW_STATEMENT} ${FULL_POLICY}; then
                jq --arg p "${ROOT_ARN}" '.Principal.AWS += [$p]' ${NEW_STATEMENT} > ${NEW_STATEMENT}.2
                mv ${NEW_STATEMENT}.2 ${NEW_STATEMENT}
              fi

              # if a new ARN was added to the statement, assemble full policy and put back on kms key
              if grep -qc "arn:aws:iam" ${NEW_STATEMENT}; then
                jq --argjson ns "$(<${NEW_STATEMENT})" '.Statement += [$ns]' ${FULL_POLICY} > ${FULL_POLICY}.2
                mv ${FULL_POLICY}.2 ${FULL_POLICY}
                aws kms put-key-policy --key-id ${kms_key_arn} --policy-name default --policy file://${FULL_POLICY}
              fi
              rm -f ${NEW_STATEMENT} ${FULL_POLICY}
            fi

            ########################
            # in source account: create SOURCE stack to produce & share snapshot
            ########################

            [[ $VERIFY = true ]] && verb="Checking" || verb="Creating"
            echo "${GREEN}${BOLD}${verb} SOURCE stacks for cross-account copies...${RESET}"

            create_source_stack "${snapshot_name}" "${source_cluster_id}" "${dest_account_id}" "${retention_days}"

            ########################
            # in dest account: create 1 key in ${source_account_region} for the copy when it arrives, and
            # 1 key in ${dest_account_region} for the copy in its final destination
            # (pass into CFN stack as KmsKeySource & KmsKeyDestination)
            ########################

            eiamcli_fetch_keys "${dest_account_id}" "${dest_account_aws_profile}"

            [[ $VERIFY = true ]] && verb="Checking" || verb="Creating"
            echo "${GREEN}${BOLD}${verb} KMS keys for snapshot encryption in DEST account...${RESET}"

            KMS_ALIAS="alias/${source_cluster_id}-snap-src"
            KMS_SRC_ARN=$(aws kms describe-key --region ${source_account_region} \
                            --key-id "${KMS_ALIAS}" --query KeyMetadata.Arn --output text 2>/dev/null)

            if [[ $? -ne 0 ]]; then
              if [[ $VERIFY = true ]]; then
                echo "${ERROR} KMS key ${KMS_ALIAS} not found in destination account: ${dest_account_id}"
              else
                # alias not found, create key
                KMS_SRC_ARN=$(aws kms create-key --region ${source_account_region} --query KeyMetadata.Arn --output text)
                aws kms create-alias --region ${source_account_region} --alias-name ${KMS_ALIAS} --target-key-id ${KMS_SRC_ARN}
              fi
            elif [[ $VERIFY = true ]]; then
              echo "KMS key ${KMS_ALIAS} located in destination account ${dest_account_id}: ${YELLOW}${KMS_SRC_ARN}${RESET}"
            fi

            KMS_ALIAS="alias/${source_cluster_id}-snap-${dest_account_region}"
            KMS_DEST_ARN=$(aws kms describe-key --region ${dest_account_region} \
                            --key-id "${KMS_ALIAS}" --query KeyMetadata.Arn --output text 2>/dev/null)

            if [[ $? -ne 0 ]]; then
              if [[ $VERIFY = true ]]; then
                echo "${ERROR} KMS key ${KMS_ALIAS} not found in destination account: ${dest_account_id}"
              else
                # alias not found, create key
                KMS_DEST_ARN=$(aws kms create-key --region ${dest_account_region} --query KeyMetadata.Arn --output text)
                aws kms create-alias --region ${dest_account_region} --alias-name ${KMS_ALIAS} --target-key-id ${KMS_DEST_ARN}
              fi
            elif [[ $VERIFY = true ]]; then
              echo "KMS key ${KMS_ALIAS} located in destination account ${dest_account_id}: ${YELLOW}${KMS_DEST_ARN}${RESET}"
            fi

            ########################
            # run DEST cfn stack in destination account, one time per cluster-id and region provided,
            # to create all snapshot copies that were requested (cross-account)
            ########################

            [[ $VERIFY = true ]] && verb="Checking" || verb="Creating"
            echo "${GREEN}${BOLD}${verb} DEST stacks for cross-account copies...${RESET}"

            create_dest_stack "${snapshot_name}" "${source_cluster_id}" "${dest_account_region}" "${retention_days}" \
              "${KMS_SRC_ARN}" "${KMS_DEST_ARN}" "TRUE"
          fi
        done
      done
    done
  done
}

###################
# main
###################

while getopts ":f:d:v" opt; do
  case "${opt}" in
    v)
      VERIFY=true
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

CONFIG_JSON=$(cat ${CONFIG_JSON_PATH})
DEPLOY_JSON=$(cat ${DEPLOY_JSON_PATH})

check_tools
check_config
process_snapshots
