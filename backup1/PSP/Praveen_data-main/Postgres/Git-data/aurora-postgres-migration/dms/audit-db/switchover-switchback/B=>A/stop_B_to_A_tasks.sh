. /l/orcl
# Usage: ./stop_B_to_A_tasks.sh <Source_DBName> <Target_DBName>
#        where <Source_DBName> should be source db name
#        where <Target_DBName> should be target db name


if [ $# -eq 2 ]; then
    source=$1
    target=$2
else
  echo "# Usage: ./stop_B_to_A_tasks.sh <Source_DBName> <Target_DBName>"
  echo "#        where <Source_DBName> should be source db name"
  echo "#        where <Target_DBName> should be target db name"
  exit 1
fi

# stop A=>B all tasks 
aws dms describe-replication-tasks --region us-west-2 --filter=Name=migration-type,Values=full-load-and-cdc,cdc |grep ReplicationTaskIdentifier|awk {'print $2'}|sed 's/\"//g; s/,//'|grep ^"$source-$target" > b_to_a_repl_tasks_to_stop.txt
cat b_to_a_repl_tasks_to_stop.txt
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
    done <b_to_a_repl_tasks_to_stop.txt 
fi