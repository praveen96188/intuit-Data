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

# create new  cdc C3=>D3 replication tasks
source_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=${source}-source" --query="Endpoints[0].EndpointArn" --output text); echo $source_endpoint_arn
target_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=${target}-target" --query="Endpoints[0].EndpointArn" --output text); echo $target_endpoint_arn
rep_instance_arn="${replication_instance_arn}"

aws dms create-replication-task \
--region us-west-2 --replication-task-identifier ${source}-${target}-${task_identifier}-fullload-cdc \
--source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn "${rep_instance_arn}" \
--migration-type full-load-and-cdc --table-mappings file://${source}-${target}-${task_identifier}-table-mappings.json \
--replication-task-settings file://${source}-${target}-rep-settings.json 1> /dev/null
sleep 5

aws dms describe-replication-tasks --region us-west-2 --filter=Name=migration-type,Values=full-load-and-cdc |grep ReplicationTaskIdentifier|awk {'print $2'}|sed 's/\"//g; s/,//'|grep ^$source > ${source}-${target}-${task_identifier}-fullload-cdc_repl_tasks.txt
tot_new_cdc_tasks_created=$(cat ${source}-${target}-${task_identifier}-fullload-cdc_repl_tasks.txt|wc -l)
echo ""
echo -e "\n Created $tot_new_cdc_tasks_created new cdc replication tasks as below. Please verify"
cat ${source}-${target}-${task_identifier}-fullload-cdc_repl_tasks.txt
