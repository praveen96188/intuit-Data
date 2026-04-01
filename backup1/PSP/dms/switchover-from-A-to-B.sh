. /l/orcl
# Usage: ./switchover-from-A-to-B.sh.sh <Source_DBName> <Target_DBName>
#        where <Source_DBName> should be source db name
#        where <Target_DBName> should be target db name


if [ $# -eq 2 ]; then
    source=$1
    target=$2
else
  echo "# Usage: ./switchover-from-A-to-B.sh.sh <Source_DBName> <Target_DBName>"
  echo "#        where <Source_DBName> should be source db name"
  echo "#        where <Target_DBName> should be target db name"
  exit 1
fi

# stop A=>B all tasks 
aws dms describe-replication-tasks --region us-west-2 --filter=Name=migration-type,Values=full-load-and-cdc,cdc |grep ReplicationTaskIdentifier|awk {'print $2'}|sed 's/\"//g; s/,//'|grep ^"$source-$target" > a_to_b_repl_tasks_to_stop.txt
cat a_to_b_repl_tasks_to_stop.txt
echo ""
echo "Do you want to stop above replication tasks (y/n)?"
read answer

if [ "$answer" == "y" ]; then
    while read line; do
        echo -e "\nstopping replication task: $line"
        rep_task_arn=$(aws dms describe-replication-tasks --region us-west-2 --filter=Name=replication-task-id,Values=$line --query="ReplicationTasks[0].ReplicationTaskArn" --output text); echo $rep_task_arn        
        aws dms stop-replication-task --region us-west-2 --replication-task-arn $rep_task_arn 1> /dev/null
        rep_tast_status=$(aws dms describe-replication-tasks --region us-west-2 --filter=Name=replication-task-arn,Values=$rep_task_arn --query="ReplicationTasks[0].Status" --output text); echo $rep_tast_status
        if [ $rep_tast_status = "stopping" ]; then                
            while true
            do
                echo -e "Stopping $line replication task....."
                sleep 5
                rep_tast_status=$(aws dms describe-replication-tasks --region us-west-2 --filter=Name=replication-task-arn,Values=$rep_task_arn --query="ReplicationTasks[0].Status" --output text)
                if [ $rep_tast_status = "stopped" ]; then
                    echo  "$line replication task has been stopped successfully"
                    break
                fi
            done
        fi    
    done <a_to_b_repl_tasks_to_stop.txt 

    echo ""
    #Delete A=>B all stopped fullload+cdc and cdc tasks
    aws dms describe-replication-tasks --region us-west-2 --filter=Name=migration-type,Values=full-load-and-cdc,cdc |grep ReplicationTaskIdentifier|awk {'print $2'}|sed 's/\"//g; s/,//'|grep ^$source > a_to_b_repl_tasks_to_delete.txt
    cat a_to_b_repl_tasks_to_delete.txt

    echo ""
    echo "Do you want to delete above replication tasks (y/n)?"
    read answer

    if [ "$answer" == "y" ]; then
        while read line; do
            rep_task_arn=$(aws dms describe-replication-tasks --region us-west-2 --filter=Name=replication-task-id,Values=$line --query="ReplicationTasks[0].ReplicationTaskArn" --output text); echo $rep_task_arn        
            rep_tast_status=$(aws dms describe-replication-tasks --region us-west-2 --filter=Name=replication-task-arn,Values=$rep_task_arn --query="ReplicationTasks[0].Status" --output text)
            if [ $rep_tast_status = "stopped" ]; then
                echo -e "\ndeleting replication task: $line"
                echo ""
                rep_task_arn=$(aws dms describe-replication-tasks --region us-west-2 --filter=Name=replication-task-id,Values=$line --query="ReplicationTasks[0].ReplicationTaskArn" --output text); echo $rep_task_arn        
                aws dms delete-replication-task --region us-west-2 --replication-task-arn $rep_task_arn 1> /dev/null
                rep_task_del_status=$(aws dms describe-replication-tasks --region us-west-2 --filter=Name=migration-type,Values=full-load-and-cdc,cdc |grep $line|awk {'print $2'}|sed 's/\"//g; s/,//'|grep ^$source|wc -l)
                if [ $rep_task_del_status -ne 0 ]; then                
                    while true
                    do
                        echo "Deleting $line replication task....."
                        sleep 5
                        rep_task_del_status=$(aws dms describe-replication-tasks --region us-west-2 --filter=Name=migration-type,Values=full-load-and-cdc,cdc |grep $line|awk {'print $2'}|sed 's/\"//g; s/,//'|grep ^$source|wc -l)
                        if [ $rep_task_del_status -eq 0 ]; then
                            echo  "$line replication task has been deleted successfully"
                            break
                        fi
                    done
                fi    
            else
                echo  "$line replication task is not in stopped state. Please check."
                break
            fi
        done <a_to_b_repl_tasks_to_delete.txt 
    fi

#Switchback (B=>A)
    source_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=spsp-sys-db1-source" --query="Endpoints[0].EndpointArn" --output text); echo $source_endpoint_arn
    target_endpoint_arn=$(aws dms describe-endpoints --region us-west-2 --filter="Name=endpoint-id,Values=pspsysib-target" --query="Endpoints[0].EndpointArn" --output text); echo $target_endpoint_arn
    rep_instance_arn="arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI"

    aws dms create-replication-task \
    --region us-west-2 --replication-task-identifier spspsysdb-pspsysib-ibobadm-smc-cdc \
    --source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn $rep_instance_arn \
    --migration-type cdc --table-mappings file://spspsysdb-pspsysib-ibobadm-smc-cdc-table-mappings.json \
    --replication-task-settings file://spspsysdb-pspsysib-ibobadm-smc-cdc-rep-settings.json 1> /dev/null
    sleep 5
    aws dms create-replication-task \
    --region us-west-2 --replication-task-identifier spspsysdb-pspsysib-ibobadm-qri-hcm-cdc \
    --source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn $rep_instance_arn \
    --migration-type cdc --table-mappings file://spspsysdb-pspsysib-ibobadm-qri-hcm-cdc-table-mappings.json \
    --replication-task-settings file://spspsysdb-pspsysib-ibobadm-qri-hcm-cdc-rep-settings.json 1> /dev/null
    sleep 5
    aws dms create-replication-task \
    --region us-west-2 --replication-task-identifier spspsysdb-pspsysib-ibobadm-tab-subpart-level-Aug22-Dec22-Overflow-cdc \
    --source-endpoint-arn $source_endpoint_arn --target-endpoint-arn $target_endpoint_arn --replication-instance-arn $rep_instance_arn \
    --migration-type cdc --table-mappings file://spspsysdb-pspsysib-ibobadm-tab-subpart-level-Aug22-Dec22-Overflow-cdc-table-mappings.json \
    --replication-task-settings file://spspsysdb-pspsysib-ibobadm-tab-subpart-level-Aug22-Dec22-Overflow-cdc-rep-settings.json 1> /dev/null
    sleep 5
    aws dms describe-replication-tasks --region us-west-2 --filter=Name=migration-type,Values=cdc |grep ReplicationTaskIdentifier|awk {'print $2'}|sed 's/\"//g; s/,//'|grep ^$target > b_to_a_new_cdc_repl_tasks.txt
    tot_new_cdc_tasks_created=$(cat b_to_a_new_cdc_repl_tasks.txt|wc -l)
    echo ""
    echo -e "\n Created $tot_new_cdc_tasks_created new cdc replication tasks as below. Please verify"
    cat b_to_a_new_cdc_repl_tasks.txt

fi

