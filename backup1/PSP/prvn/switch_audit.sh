#!/bin/bash
cluster=$1
db_name=$2
# cluster Variables
OLD_CLUSTER_ID="${cluster}-old"
STG_CLUSTER_ID="${cluster}"
NEW_CLUSTER_ID="${cluster}-new"
echo "old-cluster-id: $OLD_CLUSTER_ID"
echo "staging-cluster-id: $STG_CLUSTER_ID"
echo "new-cluster-id: $NEW_CLUSTER_ID"

#instance variables
OLD_INSTANCE_ID_W="${cluster}-old-1"
STG_INSTANCE_ID_W="${cluster}1"
NEW_INSTANCE_ID_W="${cluster}-new-1"
log_dat=$(date "+%Y%m%d%H%M%S")
echo "OLD-INSTANCE-ID_Writer: $OLD_INSTANCE_ID_W"
echo "STG_INSTANCE_ID_Writer: $STG_INSTANCE_ID_W"
echo "nNEW_INSTANCE_ID_Writer: $NEW_INSTANCE_ID_W"
echo "LOGDATE: $log_dat"
###password import
export PGPASSWORD=`cat .pp|grep ${1::-1}|awk '{print $2}'`
#connection sql script
sql_query=" SELECT datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost') as client_hostname,application_name,count(*)
from pg_stat_activity
where pid != pg_backend_pid() and usename not in ('postgres','rdsadmin','ggs','postgresi')
group by datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost'),application_name
order by usename; "

# Instance Status Check function
function check_instance_status() {
   local INSTANCE_IDENTIFIER=$1
   
   local STATUS=$(aws rds --region us-west-2 describe-db-instances --db-instance-identifier $INSTANCE_IDENTIFIER --query "DBInstances[0].DBInstanceStatus" --output text)

   while [ "$STATUS" != "available" ]
   do
      echo "Instance status is $STATUS. Waiting..."
      sleep 30
      STATUS=$(aws rds --region us-west-2 describe-db-instances --db-instance-identifier $INSTANCE_IDENTIFIER --query "DBInstances[0].DBInstanceStatus" --output text)
   done

   echo "Instance $INSTANCE_IDENTIFIER is now available."
}

##cluster status check

function check_cluster_status() {
   local CLUSTER_IDENTIFIER=$1
   
   local STATUS=$(aws rds --region us-west-2 describe-db-clusters --db-cluster-identifier $CLUSTER_IDENTIFIER --query "DBClusters[0].Status" --output text)

   while [ "$STATUS" != "available" ]
   do
      echo "Cluster status is $STATUS. Waiting..."
      sleep 30
      STATUS=$(aws rds --region us-west-2 describe-db-clusters --db-cluster-identifier $CLUSTER_IDENTIFIER --query "DBClusters[0].Status" --output text)
   done

   echo "Cluster $CLUSTER_IDENTIFIER is now available."

}


##################### main code starts here###################################
check_cluster_status $STG_CLUSTER_ID
check_instance_status  $STG_INSTANCE_ID_W
##check connection on existing database 
echo "Connections on $STG_INSTANCE_ID_W "
psql -h ${STG_INSTANCE_ID_W}.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -U postgres  -d $db_name -p 5432 -c "$sql_query"


### modify cluster existing to old
check_cluster_status $STG_CLUSTER_ID
echo "Modifying cluster  $STG_CLUSTER_ID to $OLD_CLUSTER_ID is Started .....!!!"
aws rds --region us-west-2  modify-db-cluster --db-cluster-identifier $STG_CLUSTER_ID \
--new-db-cluster-identifier $OLD_CLUSTER_ID --apply-immediately --output json > ${STG_CLUSTER_ID}-${OLD_CLUSTER_ID}-${log_dat}.json
sleep 60
check_cluster_status $OLD_CLUSTER_ID
echo "Modifying cluster  $STG_CLUSTER_ID to $OLD_CLUSTER_ID  is Completed .....!!!"
#modify instance existing to old
check_instance_status  $STG_INSTANCE_ID_W
echo "Modifying Instance $STG_INSTANCE_ID_W to $OLD_INSTANCE_ID_W is  Started .....!!!"
aws rds --region us-west-2 modify-db-instance --db-instance-identifier $STG_INSTANCE_ID_W \
  --new-db-instance-identifier $OLD_INSTANCE_ID_W --apply-immediately --output json > ${STG_INSTANCE_ID_W}-${OLD_INSTANCE_ID_W}-${log_dat}.json
sleep 120
check_instance_status  $OLD_INSTANCE_ID_W
echo "Modifying Instance $STG_INSTANCE_ID_W to $OLD_INSTANCE_ID_W  is Completed.....!!!"


### modify cluster new to staging
check_cluster_status $NEW_CLUSTER_ID
echo "Modifying cluster $NEW_CLUSTER_ID to  $STG_CLUSTER_ID is Started ....!!!"
aws rds --region us-west-2  modify-db-cluster --db-cluster-identifier $NEW_CLUSTER_ID \
    --new-db-cluster-identifier $STG_CLUSTER_ID --apply-immediately --output json > ${NEW_CLUSTER_ID}-${STG_CLUSTER_ID}-${log_dat}.json
sleep 60
check_cluster_status $STG_CLUSTER_ID
echo "Modifying cluster $NEW_CLUSTER_ID to  $STG_CLUSTER_ID is Completed ....!!!"
#modify instance new to staging
check_instance_status $NEW_INSTANCE_ID_W
echo "Modifying cluster $NEW_INSTANCE_ID_W to  $STG_INSTANCE_ID_W is Started ....!!!"
aws rds --region us-west-2 modify-db-instance --db-instance-identifier $NEW_INSTANCE_ID_W \
   --new-db-instance-identifier $STG_INSTANCE_ID_W --apply-immediately --output json > ${NEW_INSTANCE_ID_W}-${STG_INSTANCE_ID_W}-${log_dat}.json
sleep 120
check_instance_status $STG_INSTANCE_ID_W
echo "Modifying cluster $NEW_INSTANCE_ID_W to  $STG_INSTANCE_ID_W is Completed ....!!!"
check_cluster_status $STG_CLUSTER_ID
check_instance_status $STG_INSTANCE_ID_W

### check connections on old Database
echo "Connections on $OLD_INSTANCE_ID_W "
psql -h ${OLD_INSTANCE_ID_W}.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -U postgres  -d $db_name -p 5432 -c "$sql_query"
### check connections on new refreshed  Database
echo "Connections on $STG_INSTANCE_ID_W "
psql -h ${STG_INSTANCE_ID_W}.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -U postgres  -d $db_name -p 5432 -c "$sql_query"

echo " Audit Database Refresh and switching is completed"



