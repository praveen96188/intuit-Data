. /l/orcl
# Usage: ./create_A_to_B_tasks.sh <Source_DBName> <Target_DBName>
#        where <Source_DBName> should be source db name
#        where <Target_DBName> should be target db name


if [ $# -eq 2 ]; then
    source=$1
    target=$2
else
  echo "# Usage: ./create_A_to_B_tasks.sh <Source_DBName> <Target_DBName>"
  echo "#        where <Source_DBName> should be source db name"
  echo "#        where <Target_DBName> should be target db name"
  exit 1
fi

# create new  cdc A=>B replication tasks
source_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=pspuwp02-source" --query="Endpoints[0].EndpointArn" --output text); echo $source_endpoint_arn
target_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=prodapgib-target" --query="Endpoints[0].EndpointArn" --output text); echo $target_endpoint_arn
rep_instance_arn="arn:aws:dms:us-west-2:893547637742:rep:V4Z4BNFZV2XPGIERGHVYAVFXA3HZ2EICRMALBSQ"

aws dms create-replication-task \
--region us-west-2 --replication-task-identifier pspuwp02-prodapgib-ibobadm-qri-hcm-cdc \
--source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn $rep_instance_arn \
--migration-type cdc --table-mappings file://pspuwp02-prodapgib-ibobadm-qri-hcm-cdc-table-mappings.json \
--replication-task-settings file://pspuwp02-prodapgib-ibobadm-qri-hcm-cdc-rep-settings.json 1> /dev/null
sleep 5
aws dms create-replication-task \
--region us-west-2 --replication-task-identifier pspuwp02-prodapgib-ibobadm-smc-cdc \
--source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn $rep_instance_arn \
--migration-type cdc --table-mappings file://pspuwp02-prodapgib-ibobadm-smc-cdc-table-mappings.json \
--replication-task-settings file://pspuwp02-prodapgib-ibobadm-smc-cdc-rep-settings.json 1> /dev/null
sleep 5
aws dms create-replication-task \
--region us-west-2 --replication-task-identifier pspuwp02-prodapgib-ibobadm-sst-cdc \
--source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn $rep_instance_arn \
--migration-type cdc --table-mappings file://pspuwp02-prodapgib-ibobadm-sst-cdc-table-mappings.json \
--replication-task-settings file://pspuwp02-prodapgib-ibobadm-sst-cdc-rep-settings.json 1> /dev/null
sleep 5

aws dms describe-replication-tasks --region us-west-2 --filter=Name=migration-type,Values=cdc |grep ReplicationTaskIdentifier|awk {'print $2'}|sed 's/\"//g; s/,//'|grep ^$source > a_to_b_new_cdc_repl_tasks.txt
tot_new_cdc_tasks_created=$(cat a_to_b_new_cdc_repl_tasks.txt|wc -l)
echo ""
echo -e "\n Created $tot_new_cdc_tasks_created new cdc replication tasks as below. Please verify"
cat a_to_b_new_cdc_repl_tasks.txt