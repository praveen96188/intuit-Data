. /l/orcl
# Usage: ./create_B_to_A_tasks.sh <Source_DBName> <Target_DBName>
#        where <Source_DBName> should be source db name
#        where <Target_DBName> should be target db name


if [ $# -eq 2 ]; then
    source=$1
    target=$2
else
  echo "# Usage: ./create_B_to_A_tasks.sh <Source_DBName> <Target_DBName>"
  echo "#        where <Source_DBName> should be source db name"
  echo "#        where <Target_DBName> should be target db name"
  exit 1
fi

#create B=>A CDC Only tasks
    source_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=prodapgib-source" --query="Endpoints[0].EndpointArn" --output text); echo $source_endpoint_arn
    target_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=pspuwp02-target" --query="Endpoints[0].EndpointArn" --output text); echo $target_endpoint_arn
    rep_instance_arn="arn:aws:dms:us-west-2:893547637742:rep:V4Z4BNFZV2XPGIERGHVYAVFXA3HZ2EICRMALBSQ"

    aws dms create-replication-task \
    --region us-west-2 --replication-task-identifier prodapgib-pspuwp02-ibobadm-smc-cdc \
    --source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn $rep_instance_arn \
    --migration-type cdc --table-mappings file://prodapgib-pspuwp02-ibobadm-smc-cdc-table-mappings.json \
    --replication-task-settings file://prodapgib-pspuwp02-ibobadm-smc-cdc-rep-settings.json 1> /dev/null
    sleep 5
    aws dms create-replication-task \
    --region us-west-2 --replication-task-identifier prodapgib-pspuwp02-ibobadm-qri-hcm-cdc \
    --source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn $rep_instance_arn \
    --migration-type cdc --table-mappings file://prodapgib-pspuwp02-ibobadm-qri-hcm-cdc-table-mappings.json \
    --replication-task-settings file://prodapgib-pspuwp02-ibobadm-qri-hcm-cdc-rep-settings.json 1> /dev/null
    sleep 5
    aws dms create-replication-task \
    --region us-west-2 --replication-task-identifier prodapgib-pspuwp02-ibobadm-tab-subpart-level-Aug22-Dec22-Overflow-cdc \
    --source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn $rep_instance_arn \
    --migration-type cdc --table-mappings file://prodapgib-pspuwp02-ibobadm-tab-subpart-level-Aug22-Dec22-Overflow-cdc-table-mappings.json \
    --replication-task-settings file://prodapgib-pspuwp02-ibobadm-tab-subpart-level-Aug22-Dec22-Overflow-cdc-rep-settings.json 1> /dev/null
    sleep 5
    aws dms describe-replication-tasks --region us-west-2 --filter=Name=migration-type,Values=cdc |grep ReplicationTaskIdentifier|awk {'print $2'}|sed 's/\"//g; s/,//'|grep ^$source > b_to_a_new_cdc_repl_tasks.txt
    tot_new_cdc_tasks_created=$(cat b_to_a_new_cdc_repl_tasks.txt|wc -l)
    echo ""
    echo -e "\n Created $tot_new_cdc_tasks_created new cdc replication tasks as below. Please verify"
    cat b_to_a_new_cdc_repl_tasks.txt