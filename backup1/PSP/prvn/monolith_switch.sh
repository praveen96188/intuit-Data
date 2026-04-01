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
OLD_INSTANCE_ID_W="${cluster_postgres}-old-1"
OLD_INSTANCE_ID_R="${cluster_postgres}-old-2"
STG_INSTANCE_ID_W="${cluster_postgres}1"
STG_INSTANCE_ID_R="${cluster_postgres}2"
NEW_INSTANCE_ID_W="${cluster_postgres}-new-1"
NEW_INSTANCE_ID_R="${cluster_postgres}-new-2"
##fetch logs with this date
log_dat=$(date "+%Y%m%d%H%M%S")
echo "OLD_INSTANCE_ID_W: $OLD_INSTANCE_ID_W"
echo "OLD_INSTANCE_ID_R: $OLD_INSTANCE_ID_R"
echo "STG_INSTANCE_ID_W: $STG_INSTANCE_ID_W"
echo "STG_INSTANCE_ID_R: $STG_INSTANCE_ID_R"
echo "NEW_INSTANCE_ID_W: $NEW_INSTANCE_ID_W"
echo "NEW_INSTANCE_ID_R: $NEW_INSTANCE_ID_R"
echo "logdate: $log_dat"
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
echo "Connections on $STG_INSTANCE_ID_W "
psql -h ${STG_INSTANCE_ID_W}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$sql_query"
echo "Connections on $STG_INSTANCE_ID_R "
psql -h ${STG_INSTANCE_ID_R}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$sql_query"


####creating new reader instance 
echo "Creating new Reader Instance $NEW_INSTANCE_ID_R on $NEW_CLUSTER_ID is started ...!!!"
dbinstancePargrp=$(aws rds --region us-west-2 describe-db-instances \
    --db-instance-identifier $STG_INSTANCE_ID_W \
    --query 'DBInstances[*].DBParameterGroups[*].DBParameterGroupName' --output text)

dbinstanceSize=$(aws rds --region us-west-2 describe-db-instances --db-instance-identifier $STG_INSTANCE_ID_W \
    --query 'DBInstances[*].DBInstanceClass' --output text)
echo "Parameter Group: $dbinstancePargrp"
echo "DBInstance-Size: $dbinstanceSize"
##adding new reader
aws rds --region us-west-2 create-db-instance --db-instance-identifier $NEW_INSTANCE_ID_R --db-cluster-identifier  $NEW_CLUSTER_ID \
      --engine aurora-postgresql --db-instance-class $dbinstanceSize --availability-zone us-west-2a \
      --db-parameter-group-name $dbinstancePargrp --output json > Create-reader-${NEW_INSTANCE_ID_R}-${log_dat}.json

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
check_instance_status  $NEW_INSTANCE_ID_R
echo " Created new Reader Instance $NEW_INSTANCE_ID_R on $NEW_CLUSTER_ID...!!! "

#### Re-creating custom endpoints on the new cluster
check_instance_status  $NEW_INSTANCE_ID_R
echo "Creating custom endpoints on the $NEW_CLUSTER_ID cluster..."
for endpoint in $OLD_ENDPOINTS; do
    aws rds --region us-west-2  create-db-cluster-endpoint --db-cluster-identifier $NEW_CLUSTER_ID \
    --db-cluster-endpoint-identifier $endpoint --endpoint-type ANY --static-members $NEW_INSTANCE_ID_R --output json > Created-${endpoint}-${log_dat}.json 
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
check_instance_status  $STG_INSTANCE_ID_W
check_instance_status  $STG_INSTANCE_ID_R
echo "Modifying Instance $STG_INSTANCE_ID_W to $OLD_INSTANCE_ID_W is  Started .....!!!"
aws rds --region us-west-2 modify-db-instance --db-instance-identifier $STG_INSTANCE_ID_W \
  --new-db-instance-identifier $OLD_INSTANCE_ID_W --apply-immediately --output json > ${STG_INSTANCE_ID_W}-${OLD_INSTANCE_ID_W}-${log_dat}.json
#modify Reader instance existing to old
echo "Modifying Instance $STG_INSTANCE_ID_R to $OLD_INSTANCE_ID_R is  Started .....!!!"
aws rds --region us-west-2 modify-db-instance --db-instance-identifier $STG_INSTANCE_ID_R \
  --new-db-instance-identifier $OLD_INSTANCE_ID_R --apply-immediately --output json > ${STG_INSTANCE_ID_R}-${OLD_INSTANCE_ID_R}-${log_dat}.json
sleep 120
check_instance_status  $OLD_INSTANCE_ID_W
echo "Modifying Instance $STG_INSTANCE_ID_W to $OLD_INSTANCE_ID_W  is Completed.....!!!"
check_instance_status  $OLD_INSTANCE_ID_R
echo "Modifying Instance $STG_INSTANCE_ID_R to $OLD_INSTANCE_ID_R  is Completed.....!!!"


### modify cluster new to existing staging
echo "Switching existing staging cluster and Instances  SuccessFully renamed to old " 
### modify cluster new to staging
echo "Switching new refreshed  cluster and Instances rename to staging is started ...!!! " 
check_cluster_status $NEW_CLUSTER_ID
echo "Modifying cluster $NEW_CLUSTER_ID to  $STG_CLUSTER_ID is Started ....!!!"
aws rds --region us-west-2  modify-db-cluster --db-cluster-identifier $NEW_CLUSTER_ID \
    --new-db-cluster-identifier $STG_CLUSTER_ID --apply-immediately --output json > ${NEW_CLUSTER_ID}-${STG_CLUSTER_ID}-${log_dat}.json
sleep 60
check_cluster_status $STG_CLUSTER_ID
echo "Modifying cluster $NEW_CLUSTER_ID to  $STG_CLUSTER_ID is Completed ....!!!"
#modify writer instance new to staging
check_instance_status $NEW_INSTANCE_ID_W
check_instance_status $NEW_INSTANCE_ID_R
echo "Modifying cluster $NEW_INSTANCE_ID_W to  $STG_INSTANCE_ID_W is Started ....!!!"
aws rds --region us-west-2 modify-db-instance --db-instance-identifier $NEW_INSTANCE_ID_W \
   --new-db-instance-identifier $STG_INSTANCE_ID_W --apply-immediately --output json > ${NEW_INSTANCE_ID_W}-${STG_INSTANCE_ID_W}-${log_dat}.json
#modify Reader instance new to staging
echo "Modifying cluster $NEW_INSTANCE_ID_R to  $STG_INSTANCE_ID_R is Started ....!!!"
aws rds --region us-west-2 modify-db-instance --db-instance-identifier $NEW_INSTANCE_ID_R \
   --new-db-instance-identifier $STG_INSTANCE_ID_R --apply-immediately --output json > ${NEW_INSTANCE_ID_R}-${STG_INSTANCE_ID_R}-${log_dat}.json
sleep 120
check_instance_status $STG_INSTANCE_ID_W
echo "Modifying cluster $NEW_INSTANCE_ID_W to  $STG_INSTANCE_ID_W is Completed ....!!!"
check_instance_status $STG_INSTANCE_ID_R
echo "Modifying cluster $NEW_INSTANCE_ID_R to  $STG_INSTANCE_ID_W is Completed ....!!!"
check_cluster_status $STG_CLUSTER_ID
check_instance_status $STG_INSTANCE_ID_W
check_instance_status $STG_INSTANCE_ID_R
echo "Switching new refreshed  cluster and Instances SuccessFully renamed to staging...!!! " 


### check connections on staging Database
echo "Connections on $STG_INSTANCE_ID_W "
psql -h ${STG_INSTANCE_ID_W}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$sql_query"
echo "Connections on $STG_INSTANCE_ID_R "
psql -h ${STG_INSTANCE_ID_W}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$sql_query"

echo "Connections on $OLD_INSTANCE_ID_W "
psql -h ${OLD_INSTANCE_ID_W}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$sql_query"
echo "Connections on $OLD_INSTANCE_ID_R "
psql -h ${OLD_INSTANCE_ID_R}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$sql_query"


##deleting reader on old instance
echo "Deleting Reader instance  $OLD_INSTANCE_ID_R "
aws rds  --region us-west-2  delete-db-instance --db-instance-identifier $OLD_INSTANCE_ID_R \
  --skip-final-snapshot  --output json > Deleted-Reader-${OLD_INSTANCE_ID_R}-${log_dat}.json

echo " Monolith  Database Refresh and switching is completed"



