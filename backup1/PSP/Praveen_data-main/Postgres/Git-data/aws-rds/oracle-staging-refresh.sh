# Usage: ./oracle-staging-refresh.sh <AppEnv> <DBType> <Source DBName> <Target DBName> <old_staging_db>
#        where <AppEnv> should be qa, e2e, stg, sbx, prf, prod
#              <DBType> should be clusterdb or reportdb
#              <Source DBName> is name of Source RDS to be restored
#              <Target DBName> is name of the Restored RDS
#              <RestoreTime_in_UTC_Format> is Point-in-Restore-Time in UTC format, for e.g 2017-10-23T16:45:00Z (Oct 23 2017 4:45PM PDT)
# Example: nohup ./oracle-staging-refresh.sh prod clusterdb qbopp094 qbopr001 2017-10-23T16:45:00Z > oracle-staging-refresh_qbopp094.log 2>&1 & 
export AWS_REGION=us-west-2
export AWS_DEFAULT_REGION=us-west-2


if [ $# -eq 5 ]; then
  app_env=$1
  db_type=$2
  source_db=$3
  target_db=$4
#  restore_time=$5
  old_staging_db=$5
else
  echo "Usage: ./oracle-staging-refresh.sh <AppEnv> <DBType> <Source DBName> <Target DBName> <old_staging_db>"
  echo "       where <AppEnv> should be qa, e2e, stg, sbx, prf, prod"
  echo "             <DBType> should be clusterdb or reportdb"
  echo "             <Source DBName> is name of RDS to be copied"
  echo "             <Target DBName> is name of the Restored RDS"
#  echo "             <RestoreTime_in_UTC_Format> is Point-in-Restore-Time in UTC format, for e.g 2017-10-23T16:45:00Z"
  echo "             <old_staging_db> is old staging database. for eg. pspparmo, pspparhs"
  exit 1
fi

case "$app_env" in
    e2e|prf|qa|stg)
      vpc_env="ppd"
      profile=sbg-psp-ppd
      monitoring_role="arn:aws:iam::152430470825:role/rds-monitoring-role"
      ;;
    prod)
      vpc_env="prod"
      profile=sbg-psp-prod
      monitoring_role="arn:aws:iam::893547637742:role/rds-monitoring-role"
      ;;
    *)
      echo "<AppEnv> not recognized"
      exit 1
      ;;
esac

if [ `uname -a |grep Linux|wc -l` -gt 0 ]; then
  profile_setting=''
else
  profile_setting="--profile ${profile}"
fi

app_env_b=`echo ${app_env} |cut -c1-4`

if [ "${source_db}" == "${target_db}" ] ; then
   echo "Source and Target RDS instance names are same, exiting..."
   exit 1
fi

if [ `aws ${profile_setting} rds describe-db-instances --db-instance-identifier ${source_db} |grep DBInstanceStatus |egrep "available|backing-up" |wc -l` -eq 0 ] ; then
   echo "Source RDS instance does not exists, exiting..."
   exit 1
fi

if [ `aws ${profile_setting} rds describe-db-instances --db-instance-identifier ${target_db} |grep DBInstanceStatus |egrep "available|backing-up|creating|deleting|failed|maintenance|modifying|rebooting|renaming|restore-error|starting|stopping|stopped|upgrading" |wc -l` -eq 1 ] ; then
   echo "Target RDS instance already exists, exiting..."
   exit 1
fi

# Get Subnet/Security/Parameter Group Names for the RDS instance

subnet_group_name=`aws ${profile_setting} rds describe-db-instances --filters "Name=db-instance-id, Values=${source_db}" --query 'DBInstances[*].{DBSubnetGroup: DBSubnetGroup}'|grep "DBSubnetGroupName" |cut -d':' -f2|cut -d',' -f1 |cut -d'"' -f2`
echo "subnet_group_name=${subnet_group_name}"

security_group_id=`aws ${profile_setting} rds describe-db-instances --filters "Name=db-instance-id, Values=${old_staging_db}" --query 'DBInstances[*].{VpcSecurityGroups: VpcSecurityGroups}'|grep "VpcSecurityGroupId" |cut -d':' -f2|cut -d',' -f1|cut -d'"' -f2`
echo "security_group_id=${old_staging_db}"

parameter_group_name=`aws ${profile_setting} rds describe-db-instances --db-instance-identifier ${source_db} --query 'DBInstances[*].{DBParameterGroups: DBParameterGroups}' |grep DBParameterGroupName | cut -d':' -f2 |cut -d',' -f1|cut -d'"' -f2`
echo "parameter_group_name=${parameter_group_name}"

# Restore RDS instance from Source RDS 

echo "PIT (${restore_time}) Restoring RDS ${source_db} to ${target_db} starting at `date`"

aws ${profile_setting} rds restore-db-instance-to-point-in-time --source-db-instance-identifier ${source_db} --target-db-instance-identifier ${target_db} --use-latest-restorable-time --db-subnet-group-name ${subnet_group_name} --db-name ${target_db} --no-multi-az --db-instance-class db.r5.2xlarge
if [ $? != 0 ] ; then
   echo "Restore Command Failed, please check the log."
   exit 1
fi

while true
do
#  progress=`aws ${profile_setting} rds describe-db-instances --db-instance-identifier ${target_db} |grep "PercentProgress"`
  status=`aws ${profile_setting} rds describe-db-instances --db-instance-identifier ${target_db} |grep DBInstanceStatus |grep available |wc -l`
  if [ $status = 1 ]; then
    echo "Restoring RDS ${target_db} is completed"
    break
  fi
  echo "Restoring RDS ${target_db} is in progress"
  sleep 300
done

# Change Security Group Id and Parameter Group Name
echo "Modify RDS ${target_db}'s security group id and parameter group name starting at `date`"

aws ${profile_setting} rds modify-db-instance --db-instance-identifier ${target_db} --vpc-security-group-ids ${security_group_id} --db-parameter-group-name ${parameter_group_name} --db-port-number 2632

echo "Completed at `date`"

# Reboot RDS to take parameter group change effect
echo "Reboot target RDS starting at `date`"

aws ${profile_setting} rds reboot-db-instance --db-instance-identifier ${target_db}

while true
do
  status=`aws ${profile_setting} rds describe-db-instances --db-instance-identifier ${target_db} |grep DBInstanceStatus |grep available |wc -l`
  if [ $status = 1 ]; then
    echo "Rebooting RDS ${target_db} is completed"
    break
  fi
  echo "Rebooting RDS ${target_db} is in progress"
  sleep 10
done

# Get RDS instance End Point
rds_db_endpoint=`aws ${profile_setting} rds describe-db-instances --filters "Name=db-instance-id, Values=${target_db}" --query 'DBInstances[*].{Endpoint: Endpoint}'|grep "Address" |cut -d':' -f2 |cut -d',' -f1`

echo "PIT (${restore_time}) Restoring RDS ${source_db} to ${target_db} is completed at `date`"
echo "Restored RDS instance EndPoint is: ${rds_db_endpoint}"