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

##check connection on existing database 
echo "Connections on $STG_INSTANCE_ID_W "
psql -h ${STG_INSTANCE_ID_W}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$sql_query"
echo "Connections on $STG_INSTANCE_ID_R "
psql -h ${STG_INSTANCE_ID_R}.${instance_endpoint} -U postgres  -d $db_name -p 5432 -c "$sql_query"
