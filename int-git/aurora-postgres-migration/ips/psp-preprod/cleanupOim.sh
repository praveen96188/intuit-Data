#!/bin/bash

##################
# This script is a script that cleans up orphaned oim stacks.
# OIM creates stacks with deletion protection so they aren't deleted during parent stack deletion
# Use this script when cleaning up and/or resolving issues with cloudwatch metrics not being forwarded to wavefront
#
# Usage notes:
#   * Users should have poweruser
#
##################

region="$1"
if [[ "${region}" == "" ]]; then
  region="us-west-2"
fi

active_status="CREATE_IN_PROGRESS CREATE_COMPLETE ROLLBACK_IN_PROGRESS ROLLBACK_FAILED ROLLBACK_COMPLETE \
DELETE_IN_PROGRESS DELETE_FAILED UPDATE_IN_PROGRESS UPDATE_COMPLETE_CLEANUP_IN_PROGRESS UPDATE_COMPLETE \
UPDATE_ROLLBACK_IN_PROGRESS UPDATE_ROLLBACK_FAILED UPDATE_ROLLBACK_COMPLETE_CLEANUP_IN_PROGRESS UPDATE_ROLLBACK_COMPLETE"

output=$(aws cloudformation list-stacks --stack-status-filter ${active_status} --region ${region} | jq '.[][].StackName')
objects=($output)

for i in ${objects[@]}; do
    if [[ "$i" =~ "chp-"[0-9] ]]; then
        i=$(echo "$i" | tr -d "\"")
        desc=$(aws cloudformation describe-stacks --stack-name $i --region ${region} | jq '.[][].Description')
        if [[ "$desc" =~ "CHAPI - OIM onboarding" && "$i" != "chp-oim" ]]; then
            echo "found oim stack: $i"
            aws cloudformation update-termination-protection --stack-name $i --no-enable-termination-protection --region ${region}
            while true; do
              read -p "Do you wish to delete stack $i? " yn
              case $yn in
                [Yy]* ) aws cloudformation delete-stack --stack-name $i --region ${region}; break;;
                [Nn]* ) break;;
                * ) echo "Please answer yes(y) or no(n).";;
              esac
            done
        fi
    fi
done

##################
# create and execute a change set on the REGION STACK to remove the (deleted) OIM chaplette from its list of resources
##################

stack_name="IpsRds-${region}"
compact_region="$(echo ${region} | sed 's/-//g')"
changeset_name="RemoveOimChaplette${RANDOM}"

echo "Removing old OIM chaplette from region stack: ${stack_name}"

read -p "Do you wish to proceed? " yn
case $yn in
  [Yy]* ) break;;
  [Nn]* ) exit;;
  * ) echo "Please answer yes(y) or no(n).";;
esac

aws cloudformation get-template --stack-name ${stack_name} --query TemplateBody --region ${region} | jq '.' > regstack.template.json

cat regstack.template.json | jq --arg cr "IpsRds${compact_region}OIMChaplette${compact_region}" '.|del(.Resources[$cr])' > regstack.changeset.json

aws cloudformation create-change-set --no-use-previous-template --region ${region} \
    --stack-name ${stack_name} \
    --change-set-name ${changeset_name} \
    --template-body file://regstack.changeset.json \
    --capabilities CAPABILITY_IAM CAPABILITY_AUTO_EXPAND CAPABILITY_NAMED_IAM \
    --query "{Id:Id}"

status="CREATE_IN_PROGRESS"
while [ "${status}" == "CREATE_IN_PROGRESS" ]; do
  sleep 2
  status=$(aws cloudformation describe-change-set --output text --region ${region} \
    --change-set-name ${changeset_name} --stack-name ${stack_name} --query Status)
  echo ${status}
done
if [ "${status}" != "CREATE_COMPLETE" ]; then
  echo "${RED}>>> Unexpected status, exiting${RESET}"
  exit 1
fi

aws cloudformation execute-change-set --region ${region} \
    --stack-name ${stack_name} \
    --change-set-name ${changeset_name}

status="UPDATE_IN_PROGRESS"
while [ "${status}" == "UPDATE_IN_PROGRESS" ] || [ "${status}" == "UPDATE_COMPLETE_CLEANUP_IN_PROGRESS" ]; do
  sleep 5
  status=$(aws cloudformation describe-stacks --output text --region ${region} \
    --stack-name ${stack_name} --query Stacks[0].StackStatus)
  echo ${status}
done
if [ "${status}" != "UPDATE_COMPLETE" ]; then
  echo "${RED}>>> Unexpected status, exiting${RESET}"
  exit 1
fi

rm -f regstack.*
