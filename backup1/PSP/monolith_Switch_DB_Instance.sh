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
OLD_INSTANCE_ID2="${cluster_postgres}-old-2"
STG_INSTANCE_ID1="${cluster_postgres}1"
STG_INSTANCE_ID2="${cluster_postgres}2"
NEW_INSTANCE_ID1="${cluster_postgres}-new-1"
NEW_INSTANCE_ID2="${cluster_postgres}-new-2"
##fetch logs with this date
log_dat=$(date "+%Y%m%d%H%M%S")
echo "OLD_INSTANCE_ID1: $OLD_INSTANCE_ID1"
echo "OLD_INSTANCE_ID2: $OLD_INSTANCE_ID2"
echo "STG_INSTANCE_ID1: $STG_INSTANCE_ID1"
echo "STG_INSTANCE_ID2: $STG_INSTANCE_ID2"
echo "NEW_INSTANCE_ID1: $NEW_INSTANCE_ID1"
echo "NEW_INSTANCE_ID2: $NEW_INSTANCE_ID2"
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

instance_check=" SELECT case when pg_is_in_recovery = 'f' then 'WRITER' when pg_is_in_recovery = 't' then 'READER' else 'wrong' end t FROM (SELECT pg_is_in_recovery()) x; "
##wait for end point available
function check_endpoint_status() {
    local endpoint=$1
    
    while true; do
        # Fetch the status from the describe-db-cluster-endpoints AWS CLI command
        local STATUS=$(aws rds describe-db-cluster-endpoints \
                        --region us-west-2 \
                        --db-cluster-endpoint-identifier "$endpoint" \
                        --query "DBClusterEndpoints[?DBClusterEndpointIdentifier=='$endpoint'].Status" \
                        --output text)
        
        # Check if the status is null/empty, indicating deletion or non-existence
        if [ -z "$STATUS" ]; then
            echo "Custom endpoint $endpoint is deleted or does not exist."
            break
        
        # Check if the status is 'available'
        elif [[ "$STATUS" == "available" ]]; then
            echo "Custom endpoint $endpoint is available."
            break
        
        # Any other status
        else
            echo "Current status of $endpoint is $STATUS."
            sleep 30  # Wait for 30 seconds before checking again
        fi
    done
}

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
echo "Connections on $STG_INSTANCE_ID2 "
psql -h ${STG_INSTANCE_ID2}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$conn_chk"


####creating new reader instance 
echo "Creating new Reader Instance $NEW_INSTANCE_ID2 on $NEW_CLUSTER_ID is started ...!!!"
dbinstancePargrp=$(aws rds --region us-west-2 describe-db-instances \
    --db-instance-identifier $STG_INSTANCE_ID2 \
    --query 'DBInstances[*].DBParameterGroups[*].DBParameterGroupName' --output text)

dbinstanceSize=$(aws rds --region us-west-2 describe-db-instances --db-instance-identifier $STG_INSTANCE_ID2 \
    --query 'DBInstances[*].DBInstanceClass' --output text)
echo "Parameter Group: $dbinstancePargrp"
echo "DBInstance-Size: $dbinstanceSize"
##adding new reader
aws rds --region us-west-2 create-db-instance --db-instance-identifier $NEW_INSTANCE_ID2 --db-cluster-identifier  $NEW_CLUSTER_ID \
      --engine aurora-postgresql --db-instance-class $dbinstanceSize \
      --db-parameter-group-name $dbinstancePargrp --output json > Create-reader-${NEW_INSTANCE_ID2}-${log_dat}.json

####Deleting endpoints
OLD_ENDPOINTS=$(aws rds --region us-west-2 describe-db-cluster-endpoints --db-cluster-identifier $STG_CLUSTER_ID \
    --query "DBClusterEndpoints[?EndpointType=='CUSTOM'].DBClusterEndpointIdentifier" --output text)
#### Deleting custom endpoints from the old cluster
echo "Deleting custom endpoints from the old cluster..."
for endpoint in $OLD_ENDPOINTS; do
    aws rds  --region us-west-2  delete-db-cluster-endpoint --db-cluster-endpoint-identifier $endpoint --output json > Deleted-${endpoint}-${log_dat}.json
    check_endpoint_status $endpoint
done
check_cluster_status $STG_CLUSTER_ID
echo "custom end point deletion completed on $STG_CLUSTER_ID "
check_instance_status  $NEW_INSTANCE_ID2
echo " Created new Reader Instance $NEW_INSTANCE_ID2 on $NEW_CLUSTER_ID...!!! "

#### Re-creating custom endpoints on the new cluster
check_instance_status  $NEW_INSTANCE_ID2
echo "Creating custom endpoints on the $NEW_CLUSTER_ID cluster..."
for endpoint in $OLD_ENDPOINTS; do
    aws rds --region us-west-2  create-db-cluster-endpoint --db-cluster-identifier $NEW_CLUSTER_ID \
    --db-cluster-endpoint-identifier $endpoint --endpoint-type ANY --static-members $NEW_INSTANCE_ID2 --output json > Created-${endpoint}-${log_dat}.json 
    check_endpoint_status $endpoint
done
check_cluster_status $NEW_CLUSTER_ID
echo "custom end point creation completed on $NEW_CLUSTER_ID "


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
check_instance_status  $STG_INSTANCE_ID2
echo "Modifying Instance $STG_INSTANCE_ID1 to $OLD_INSTANCE_ID1 is  Started .....!!!"
aws rds --region us-west-2 modify-db-instance --db-instance-identifier $STG_INSTANCE_ID1 \
  --new-db-instance-identifier $OLD_INSTANCE_ID1 --apply-immediately --output json > ${STG_INSTANCE_ID1}-${OLD_INSTANCE_ID1}-${log_dat}.json
#modify Reader instance existing to old
echo "Modifying Instance $STG_INSTANCE_ID2 to $OLD_INSTANCE_ID2 is  Started .....!!!"
aws rds --region us-west-2 modify-db-instance --db-instance-identifier $STG_INSTANCE_ID2 \
  --new-db-instance-identifier $OLD_INSTANCE_ID2 --apply-immediately --output json > ${STG_INSTANCE_ID2}-${OLD_INSTANCE_ID2}-${log_dat}.json
sleep 150
check_instance_status  $OLD_INSTANCE_ID1
echo "Modifying Instance $STG_INSTANCE_ID1 to $OLD_INSTANCE_ID1  is Completed.....!!!"
check_instance_status  $OLD_INSTANCE_ID2
echo "Modifying Instance $STG_INSTANCE_ID2 to $OLD_INSTANCE_ID2  is Completed.....!!!"
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
#modify Reader instance new to staging
echo "Modifying cluster $NEW_INSTANCE_ID2 to  $STG_INSTANCE_ID2 is Started ....!!!"
aws rds --region us-west-2 modify-db-instance --db-instance-identifier $NEW_INSTANCE_ID2 \
   --new-db-instance-identifier $STG_INSTANCE_ID2 --apply-immediately --output json > ${NEW_INSTANCE_ID2}-${STG_INSTANCE_ID2}-${log_dat}.json
sleep 150
check_instance_status $STG_INSTANCE_ID1
echo "Modifying cluster $NEW_INSTANCE_ID1 to  $STG_INSTANCE_ID1 is Completed ....!!!"
check_instance_status $STG_INSTANCE_ID2
echo "Modifying cluster $NEW_INSTANCE_ID2 to  $STG_INSTANCE_ID1 is Completed ....!!!"
check_cluster_status $STG_CLUSTER_ID
check_instance_status $STG_INSTANCE_ID1
check_instance_status $STG_INSTANCE_ID2
echo "Switching new refreshed  cluster and Instances SuccessFully renamed to staging...!!! " 


### check connections on staging Database
echo "Connections on $STG_INSTANCE_ID1 "
psql -h ${STG_INSTANCE_ID1}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$conn_chk"
echo "Connections on $STG_INSTANCE_ID2 "
psql -h ${STG_INSTANCE_ID1}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$conn_chk"

echo "Connections on $OLD_INSTANCE_ID1 "
psql -h ${OLD_INSTANCE_ID1}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$conn_chk"
echo "Connections on $OLD_INSTANCE_ID2 "
psql -h ${OLD_INSTANCE_ID2}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$conn_chk"


##deleting reader on old instance
# Execute the SQL query and store the output
Instace_mode=$(psql -h ${OLD_INSTANCE_ID2}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -t -c "$instance_check")
# Trim whitespace
Instace_mode=$(echo $Instace_mode | xargs)
# Conditional logic based on query output
if [ "$Instace_mode" = "READER" ]; then
  echo "Instance identified as READER, hence deleting the instance."
 echo "Deleting Reader instance  $OLD_INSTANCE_ID2 "
aws rds  --region us-west-2  delete-db-instance --db-instance-identifier $OLD_INSTANCE_ID2 \
  --skip-final-snapshot  --output json > Deleted-Reader-${OLD_INSTANCE_ID2}-${log_dat}.json
else
  echo "Instance is not a READER (status: $output). Check Instace and delete from console"
fi


echo " Monolith  Database Refresh and switching is completed"

