#!/bin/bash
. /l/orcl

cluster_postgres=$1
db_name=$2

if [ "${VPC_ENV}" == "ppd" ]; then
  instance_endpoint="ccqjgvvo0rwy.us-west-2.rds.amazonaws.com"
elif [ "${VPC_ENV}" == "prod" ]; then
  instance_endpoint="cjls0bohfgpq.us-west-2.rds.amazonaws.com"
fi

##postgres_cluster is which clsuer you want refresh
##database name for cluster
# cluster Variables
OLD_CLUSTER_ID="${cluster_postgres}-old"
STG_CLUSTER_ID="${cluster_postgres}"
NEW_CLUSTER_ID="${cluster_postgres}-new"
echo "OLD_CLUSTER_ID: $OLD_CLUSTER_ID"
echo "STG_CLUSTER_ID: $STG_CLUSTER_ID"
echo "NEW_CLUSTER_ID: $NEW_CLUSTER_ID"

#instance variables
OLD_INSTANCE_ID1="${cluster_postgres}-old-1"
STG_INSTANCE_ID1="${cluster_postgres}1"
NEW_INSTANCE_ID1="${cluster_postgres}-new-1"

##fetch logs with this date
log_dat=$(date "+%Y%m%d%H%M%S")
echo "OLD_INSTANCE_ID1: $OLD_INSTANCE_ID1"
echo "STG_INSTANCE_ID1: $STG_INSTANCE_ID1"
echo "NEW_INSTANCE_ID1: $NEW_INSTANCE_ID1"
echo "logdate: $log_dat"
###password import
export PGPASSWORD=$(grep "^$1" .pp | awk '{print $2}' )
#connection sql script
conn_chk=" SELECT datname,usename,
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

   echo "Instance $INSTANCE_IDENTIFIER is available."
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

   echo "Cluster $CLUSTER_IDENTIFIER is  available."

}


##################### main code starts here###################################
##check connection on existing database 
echo "Connections on $STG_INSTANCE_ID1 "
psql -h ${STG_INSTANCE_ID1}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$conn_chk"


### modify cluster existing to old
echo "Switching existing staging cluster and Instances rename to old is started ...!!! " 
check_cluster_status $STG_CLUSTER_ID
echo "Modifying cluster  $STG_CLUSTER_ID to $OLD_CLUSTER_ID is Started .....!!!"
aws rds --region us-west-2  modify-db-cluster --db-cluster-identifier $STG_CLUSTER_ID \
--new-db-cluster-identifier $OLD_CLUSTER_ID --apply-immediately --output json > ${STG_CLUSTER_ID}-${OLD_CLUSTER_ID}-${log_dat}.json
sleep 60
check_cluster_status $OLD_CLUSTER_ID
echo "Modifying cluster  $STG_CLUSTER_ID to $OLD_CLUSTER_ID  is Completed .....!!!"
#modify writer instance existing to old
check_instance_status  $STG_INSTANCE_ID1
echo "Modifying Instance $STG_INSTANCE_ID1 to $OLD_INSTANCE_ID1 is  Started .....!!!"
aws rds --region us-west-2 modify-db-instance --db-instance-identifier $STG_INSTANCE_ID1 \
  --new-db-instance-identifier $OLD_INSTANCE_ID1 --apply-immediately --output json > ${STG_INSTANCE_ID1}-${OLD_INSTANCE_ID1}-${log_dat}.json
sleep 150
check_instance_status  $OLD_INSTANCE_ID1
echo "Modifying Instance $STG_INSTANCE_ID1 to $OLD_INSTANCE_ID1  is Completed.....!!!"
echo "Switching existing staging cluster and Instances  SuccessFully renamed to old " 


## modify cluster new to staging
echo "Switching new refreshed  cluster and Instances rename to staging is started ...!!! " 
check_cluster_status $NEW_CLUSTER_ID
echo "Modifying cluster $NEW_CLUSTER_ID to  $STG_CLUSTER_ID is Started ....!!!"
aws rds --region us-west-2  modify-db-cluster --db-cluster-identifier $NEW_CLUSTER_ID \
    --new-db-cluster-identifier $STG_CLUSTER_ID --apply-immediately --output json > ${NEW_CLUSTER_ID}-${STG_CLUSTER_ID}-${log_dat}.json
sleep 60
check_cluster_status $STG_CLUSTER_ID
echo "Modifying cluster $NEW_CLUSTER_ID to  $STG_CLUSTER_ID is Completed ....!!!"
#modify writer instance new to staging
check_instance_status $NEW_INSTANCE_ID1
check_instance_status $NEW_INSTANCE_ID2
echo "Modifying cluster $NEW_INSTANCE_ID1 to  $STG_INSTANCE_ID1 is Started ....!!!"
aws rds --region us-west-2 modify-db-instance --db-instance-identifier $NEW_INSTANCE_ID1 \
   --new-db-instance-identifier $STG_INSTANCE_ID1 --apply-immediately --output json > ${NEW_INSTANCE_ID1}-${STG_INSTANCE_ID1}-${log_dat}.json
sleep 150
check_instance_status $STG_INSTANCE_ID1
echo "Modifying cluster $NEW_INSTANCE_ID1 to  $STG_INSTANCE_ID1 is Completed ....!!!"
check_cluster_status $STG_CLUSTER_ID
check_instance_status $STG_INSTANCE_ID1
echo "Switching new refreshed  cluster and Instances SuccessFully renamed to staging...!!! " 


### check connections on staging Database
echo "Connections on $STG_INSTANCE_ID1 "
psql -h ${STG_INSTANCE_ID1}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$conn_chk"

echo "Connections on $OLD_INSTANCE_ID1 "
psql -h ${OLD_INSTANCE_ID1}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$conn_chk"

echo " Database Refresh and switching is completed"

