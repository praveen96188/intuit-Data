--validation-CDC

. /l/orcl
# Usage: ./create_C3_to_D3_tasks.sh <Source_DBName> <Target_DBName> <task_identifier> <replication_instance_arn>
#        where <Source_DBName> should be source db name
#              <Target_DBName> should be target db name
#               <task_identifier> is task identifier
#  example   ./create_C3_to_D3_tasks.sh psppp01 psphpp06 task1 arn:aws:dms:us-west-2:893547637742:rep:KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y full-load/cdc


if [ $# -eq 5 ]; then
    source=$1
    target=$2
    task_identifier=$3
    replication_instance_arn=$4
    validation_type=$5

else
  echo "# Usage: ./create_A_to_B_tasks.sh <Source_DBName> <Target_DBName>"
  echo "#        where <Source_DBName> should be source db name"
  echo "#        where <Target_DBName> should be target db name"
  exit 1
fi

# create new  cdc Validation C3=>D3 replication tasks
source_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=${source}-source" --query="Endpoints[0].EndpointArn" --output text); echo $source_endpoint_arn
target_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=${target}-target" --query="Endpoints[0].EndpointArn" --output text); echo $target_endpoint_arn
rep_instance_arn="${replication_instance_arn}"

aws dms create-replication-task \
--region us-west-2 --replication-task-identifier ${source}-${target}-${task_identifier} \
--replication-task-settings '{"FullLoadSettings":{"TargetTablePrepMode":"DO_NOTHING"},"ValidationSettings":{"EnableValidation":true,"ValidationOnly":true}}' \
--source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn "${rep_instance_arn}" \
--migration-type $validation_type --table-mappings file://${source}-${target}-${task_identifier}-table-mappings.json 1> /dev/null

sleep 5

aws dms describe-replication-tasks --region us-west-2 --filter=Name=migration-type,Values=cdc |grep ReplicationTaskIdentifier|awk {'print $2'}|sed 's/\"//g; s/,//'|grep ^$source > ${source}-${target}-${task_identifier}_validation_cdc_repl_tasks.txt
tot_new_tasks_created=$(cat ${source}-${target}-${task_identifier}_validation_cdc_repl_tasks.txt|wc -l)
echo ""
echo -e "\n Created $tot_new_tasks_created new  data validation replication tasks as below. Please verify"
cat ${source}-${target}-${task_identifier}_validation_cdc_repl_tasks.txt


--Validation full-load


. /l/orcl
# Usage: ./create_C3_to_D3_tasks.sh <Source_DBName> <Target_DBName> <task_identifier> <replication_instance_arn>
#        where <Source_DBName> should be source db name
#              <Target_DBName> should be target db name
#               <task_identifier> is task identifier


#  example   ./create_C3_to_D3_tasks.sh psppp01 psphpp06 task1 arn:aws:dms:us-west-2:893547637742:rep:KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y


if [ $# -eq 4 ]; then
    source=$1
    target=$2
    task_identifier=$3
    replication_instance_arn=$4

else
  echo "# Usage: ./create_A_to_B_tasks.sh <Source_DBName> <Target_DBName>"
  echo "#        where <Source_DBName> should be source db name"
  echo "#        where <Target_DBName> should be target db name"
  exit 1
fi

# create new  cdc Validation C3=>D3 replication tasks
source_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=${source}-source" --query="Endpoints[0].EndpointArn" --output text); echo $source_endpoint_arn
target_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=${target}-target" --query="Endpoints[0].EndpointArn" --output text); echo $target_endpoint_arn
rep_instance_arn="${replication_instance_arn}"

aws dms create-replication-task \
--region us-west-2 --replication-task-identifier ${source}-${target}-${task_identifier} \
--replication-task-settings '{"FullLoadSettings":{"TargetTablePrepMode":"DO_NOTHING"},"ValidationSettings":{"EnableValidation":true,"ValidationOnly":true}}' \
--source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn "${rep_instance_arn}" \
--migration-type full-load --table-mappings file://${source}-${target}-${task_identifier}-table-mappings.json 1> /dev/null

sleep 5

aws dms describe-replication-tasks --region us-west-2 --filter=Name=migration-type,Values=cdc |grep ReplicationTaskIdentifier|awk {'print $2'}|sed 's/\"//g; s/,//'|grep ^$source > ${source}-${target}-${task_identifier}_repl_tasks.txt
tot_tasks_created=$(cat ${source}-${target}-${task_identifier}_repl_tasks.txt|wc -l)
echo ""
echo -e "\n Created $tot_tasks_created new data validation replication tasks as below. Please verify"
cat ${source}-${target}-${task_identifier}_repl_tasks.txt



--DMS validation

aws dms create-replication-task \
--region us-west-2 --replication-task-identifier pdsibobdbeast-ppdspg01east-aud-mon-validation \
--replication-task-settings '{"FullLoadSettings":{"TargetTablePrepMode":"DO_NOTHING"},"ValidationSettings":{"EnableValidation":true,"ValidationOnly":true}}' --replication-instance-arn  arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ \
--source-endpoint-arn arn:aws:dms:us-west-2:152430470825:endpoint:3UO2KLVEQYPGAN4HCIVWU4SSU5AX76INGZ4G4II \
--target-endpoint-arn arn:aws:dms:us-west-2:152430470825:endpoint:4PD3B53RSGRFW3AXLPIG2RCCP6KHBLFM7A2S34I \
--migration-type full-load --table-mappings file://pdsibobdbeast-ppdspg01east-aud-mon-table-mappings.json


. /l/orcl
# Usage: ./create_C3_to_D3_tasks.sh <Source_DBName> <Target_DBName> <task_identifier> <replication_instance_arn>
#        where <Source_DBName> should be source db name
#              <Target_DBName> should be target db name
#               <task_identifier> is task identifier


#  example   ./create_C3_to_D3_tasks.sh psppp01 psphpp06 task1 arn:aws:dms:us-west-2:893547637742:rep:KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y


if [ $# -eq 4 ]; then
    source=$1
    target=$2
    task_identifier=$3
    replication_instance_arn=$4
    start_time=$5
else
  echo "# Usage: ./create_A_to_B_tasks.sh <Source_DBName> <Target_DBName>"
  echo "#        where <Source_DBName> should be source db name"
  echo "#        where <Target_DBName> should be target db name"
  exit 1
fi

# create new  cdc Validation C3=>D3 replication tasks
source_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=${source}-source" --query="Endpoints[0].EndpointArn" --output text); echo $source_endpoint_arn
target_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=${target}-target" --query="Endpoints[0].EndpointArn" --output text); echo $target_endpoint_arn
rep_instance_arn="${replication_instance_arn}"

aws dms create-replication-task \
--replication-task-identifier validation-only-task \
--replication-task-settings '{"FullLoadSettings":{"TargetTablePrepMode":"DO_NOTHING"},"ValidationSettings":{"EnableValidation":true,"ValidationOnly":true}}' \
--source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn "${rep_instance_arn}" \
--migration-type cdc --cdc-start-time "${start_time}" --table-mappings file://${source}-${target}-${task_identifier}-table-mappings.json 1> /dev/null

sleep 5

aws dms describe-replication-tasks --region us-west-2 --filter=Name=migration-type,Values=cdc |grep ReplicationTaskIdentifier|awk {'print $2'}|sed 's/\"//g; s/,//'|grep ^$source > ${source}-${target}-${task_identifier}_repl_tasks.txt
tot_new_cdc_tasks_created=$(cat ${source}-${target}-${task_identifier}_repl_tasks.txt|wc -l)
echo ""
echo -e "\n Created $tot_new_cdc_tasks_validation_created new cdc replication tasks as below. Please verify"
cat ${source}-${target}-${task_identifier}_repl_tasks.txt



