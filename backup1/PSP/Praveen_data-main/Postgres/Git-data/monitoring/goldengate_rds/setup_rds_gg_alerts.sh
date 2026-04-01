# Usage: ./setup_aws_alerts.sh  <AppEnv> <Region> <DBName> [<DBOnly>]
#        where <AppEnv> should be qa, e2e, stg, sbx, prf and prod
#        <Region> is us-west-2, us-east-2, etc
#        [<DBOnly>] is optional. n (default) means DB and Goldengate;  y means DB only

if [ $# -eq 3 -o $# -eq 4 ]; then
  app_env=$1
  region=$2
  db_name=$3
  dbonly=$4
else
  echo "Usage ./setup_aws_alerts.sh <AppEnv> <Region> <DBName> [<DBOnly>]"
  echo "      where <AppEnv> should be qa, e2e, stg, sbx, prf and prod"
  echo "      <Region> is us-west-2, us-east-2, etc"
  echo "      [<DBOnly>] is optional. n (default) means DB and Goldengate;  y means DB only"
  exit 1
fi

case "$app_env" in
  e2e|prf|qa|sys|pds)
      dnsname="sbg-psp-ppd.a.intuit.com"
      vpc_env="ppd"
      profile="sbg-psp-ppd"
      critical_topic="arn:aws:sns:${region}:152430470825:DB-sbg-psp-critical-ppd-a-intuit-com"
      warning_topic="arn:aws:sns:${region}:152430470825:DB-sbg-psp-warning-ppd-a-intuit-com"
      goldengate_lag_critical=360
      goldengate_lag_warning=60
      ;;
  prod|stg)
      dnsname="sbg-psp-prod.a.intuit.com"
      vpc_env="prod"
      profile="sbg-psp-prod"
      critical_topic="arn:aws:sns:${region}:893547637742:DB-sbg-psp-critical-prod-a-intuit-com"
      warning_topic="arn:aws:sns:${region}:893547637742:DB-sbg-psp-warning-prod-a-intuit-com"
      goldengate_lag_critical=360
      goldengate_lag_warning=60
      ;;
    *)
      echo "<AppEnv> not recognized"
      exit 1
      ;;
esac


db_instance_class=`aws --profile $profile --region $region rds describe-db-instances --db-instance-identifier $db_name --query 'DBInstances[0].DBInstanceClass' |cut -d\" -f2`

case "$db_instance_class" in
  db.r6.16xlarge|db.r4.16xlarge|db.r5.16xlarge)
    free_memory_critical=8000000000
    free_memory_warning=16000000000
    ;;
  db.r6.12xlarge|db.r4.12xlarge|db.r5.12xlarge)
    free_memory_critical=6000000000
    free_memory_warning=12000000000
    ;;
  db.r6.8xlarge|db.r4.8xlarge|db.r5.8xlarge)
    free_memory_critical=4000000000
    free_memory_warning=8000000000
    ;;
  db.r6.4xlarge|db.r4.4xlarge|db.r5.4xlarge)
    free_memory_critical=2000000000
    free_memory_warning=4000000000
    ;;
  db.r6.2xlarge|db.r4.2xlarge|db.r5.2xlarge)
    free_memory_critical=1000000000
    free_memory_warning=2000000000
    ;;
  db.r6.xlarge|db.r4.xlarge|db.r5.xlarge)
    free_memory_critical=500000000
    free_memory_warning=1000000000
    ;;
  *)
    echo "db_instance_class: $db_instance_class not recognized"
    ;;
esac
echo "free_memory_critical=$free_memory_critical"
echo "free_memory_warning=$free_memory_warning"

typeset -i allocated_storage
typeset -i free_storage_space_critical
typeset -i free_storage_space_warning
allocated_storage=`aws --profile $profile --region $region rds describe-db-instances --db-instance-identifier $db_name --query 'DBInstances[0].AllocatedStorage'`
echo "allocated_storage=$allocated_storage"

if [ ${allocated_storage} -lt 10240 ]; then
  free_storage_space_critical=${allocated_storage}/5
  free_storage_space_critical=${free_storage_space_critical}*1073741824
  free_storage_space_warning=${allocated_storage}/100
  free_storage_space_warning=${free_storage_space_warning}*25
  free_storage_space_warning=${free_storage_space_warning}*1073741824
elif [ ${allocated_storage} -lt 20480 ]; then
  free_storage_space_critical=${allocated_storage}*15/100
  free_storage_space_critical=${free_storage_space_critical}*1073741824
  free_storage_space_warning=${allocated_storage}/100
  free_storage_space_warning=${free_storage_space_warning}*20
  free_storage_space_warning=${free_storage_space_warning}*1073741824
else
  free_storage_space_critical=${allocated_storage}/10
  free_storage_space_critical=${free_storage_space_critical}*1073741824
  free_storage_space_warning=${allocated_storage}/100
  free_storage_space_warning=${free_storage_space_warning}*15
  free_storage_space_warning=${free_storage_space_warning}*1073741824
fi
echo "free_storage_space_critical=$free_storage_space_critical"
echo "free_storage_space_warning=$free_storage_space_warning"

typeset -i iops
typeset -i read_iops_critical
typeset -i read_iops_warning
typeset -i write_iops_critical
typeset -i write_iops_warning
iops=`aws --profile $profile --region $region rds describe-db-instances --db-instance-identifier $db_name --query 'DBInstances[0].Iops'`
if [ $iops != 0 ]; then
  read_iops_critical=${iops}/100
  read_iops_critical=${read_iops_critical}*95
  read_iops_warning=${iops}/10
  read_iops_warning=${read_iops_warning}*9
  echo "read_iops_critical=$read_iops_critical"
  echo "read_iops_warning=$read_iops_warning"
  write_iops_critical=${iops}/10
  write_iops_critical=${write_iops_critical}*8
  write_iops_warning=${iops}/10
  write_iops_warning=${write_iops_warning}*7
  echo "write_iops_critical=$write_iops_critical"
  echo "write_iops_warning=$write_iops_warning"
fi

# Setup alerts on DatabaseConnections
echo "Setup alerts on DatabaseConnections"
aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-DatabaseConnections-Critical --alarm-description "Alarm/page when # of db connectoins exceed this threshold" --metric-name DatabaseConnections --namespace AWS/RDS --statistic Average --period 300 --threshold 3000 --comparison-operator GreaterThanOrEqualToThreshold --dimensions "Name=DBInstanceIdentifier,Value=${db_name}" --evaluation-periods 1 --alarm-actions $critical_topic --ok-actions $warning_topic
aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-DatabaseConnections-Critical

aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-DatabaseConnections-Warning --alarm-description "Alarm/email when # of db connectoins exceed this threshold" --metric-name DatabaseConnections --namespace AWS/RDS --statistic Average --period 300 --threshold 2500 --comparison-operator GreaterThanOrEqualToThreshold --dimensions "Name=DBInstanceIdentifier,Value=${db_name}" --evaluation-periods 1 --alarm-actions $warning_topic --ok-actions $warning_topic
aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-DatabaseConnections-Warning

# Setup alerts on DatabaseActiveConnection
echo "Setup alerts on DatabaseActiveConnection" 
aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-DatabaseActiveConnection-Critical --alarm-description "Alarm/page when # of db active connectoins exceed this threshold" --metric-name ${db_name} --namespace DatabaseActiveConnection --statistic Average --period 300 --threshold 300 --comparison-operator GreaterThanOrEqualToThreshold --evaluation-periods 1 --alarm-actions $critical_topic --ok-actions $warning_topic
aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-DatabaseActiveConnection-Critical

aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-DatabaseActiveConnection-Warning --alarm-description "Alarm/email when # of db active connectoins exceed this threshold" --metric-name ${db_name} --namespace DatabaseActiveConnection --statistic Average --period 300 --threshold 200 --comparison-operator GreaterThanOrEqualToThreshold --evaluation-periods 1 --alarm-actions $warning_topic --ok-actions $warning_topic
aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-DatabaseActiveConnection-Warning

# Setup alerts on CPUUtilization
echo "Setup alerts on CPUUtilization"
aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-CPUUtilization-Critical --alarm-description "Alarm/page when # of db CPU utilization exceed this threshold" --metric-name CPUUtilization --namespace AWS/RDS --statistic Average --period 300 --threshold 80 --comparison-operator GreaterThanOrEqualToThreshold --dimensions "Name=DBInstanceIdentifier,Value=${db_name}" --evaluation-periods 1 --alarm-actions $critical_topic --ok-actions $warning_topic
aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-CPUUtilization-Critical

aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-CPUUtilization-Warning --alarm-description "Alarm/email when # of db CPU utilization exceed this threshold" --metric-name CPUUtilization --namespace AWS/RDS --statistic Average --period 300 --threshold 70 --comparison-operator GreaterThanOrEqualToThreshold --dimensions "Name=DBInstanceIdentifier,Value=${db_name}" --evaluation-periods 1 --alarm-actions $warning_topic --ok-actions $warning_topic
aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-CPUUtilization-Warning

echo "Setup alerts on FreeableMemory"
aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-FreeableMemory-Critical --alarm-description "Alarm/page when # of db Freeable Memory exceed this threshold" --metric-name FreeableMemory --namespace AWS/RDS --statistic Average --period 300 --threshold $free_memory_critical --comparison-operator LessThanOrEqualToThreshold --dimensions "Name=DBInstanceIdentifier,Value=${db_name}" --evaluation-periods 1 --alarm-actions $critical_topic --ok-actions $warning_topic
aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-FreeableMemory-Critical

aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-FreeableMemory-Warning --alarm-description "Alarm/email when # of db Freeable Memory exceed this threshold" --metric-name FreeableMemory --namespace AWS/RDS --statistic Average --period 300 --threshold $free_memory_warning --comparison-operator LessThanOrEqualToThreshold --dimensions "Name=DBInstanceIdentifier,Value=${db_name}" --evaluation-periods 1 --alarm-actions $warning_topic --ok-actions $warning_topic
aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-FreeableMemory-Warning

echo "Setup alerts on FreeStorageSpace"
aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-FreeStorageSpace-Critical --alarm-description "Alarm/page when # of db Free Storage Space exceed this threshold (20%)" --metric-name FreeStorageSpace --namespace AWS/RDS --statistic Average --period 300 --threshold $free_storage_space_critical --comparison-operator LessThanOrEqualToThreshold --dimensions "Name=DBInstanceIdentifier,Value=${db_name}" --evaluation-periods 1 --alarm-actions $critical_topic --ok-actions $warning_topic
aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-FreeStorageSpace-Critical

aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-FreeStorageSpace-Warning --alarm-description "Alarm/email when # of db Free Storage Space exceed this threshold (25%)" --metric-name FreeStorageSpace --namespace AWS/RDS --statistic Average --period 300 --threshold $free_storage_space_warning --comparison-operator LessThanOrEqualToThreshold --dimensions "Name=DBInstanceIdentifier,Value=${db_name}" --evaluation-periods 1 --alarm-actions $warning_topic --ok-actions $warning_topic
aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-FreeStorageSpace-Warning

if [ $iops != 0 ]; then
  echo "Setup alerts on Read Iops"
  aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-Read-Iops-Critical --alarm-description "Alarm/page when # of db Read Iops exceed 95% of total IOPS" --metric-name ReadIOPS --namespace AWS/RDS --statistic Average --period 300 --threshold $read_iops_critical --comparison-operator GreaterThanOrEqualToThreshold --dimensions "Name=DBInstanceIdentifier,Value=${db_name}" --evaluation-periods 2 --alarm-actions $critical_topic --ok-actions $warning_topic
  aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-Read-Iops-Critical

  aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-Read-Iops-Warning --alarm-description "Alarm/email when # of db Read Iops exceed 90% of total IOPS" --metric-name ReadIOPS --namespace AWS/RDS --statistic Average --period 300 --threshold $read_iops_warning --comparison-operator GreaterThanOrEqualToThreshold --dimensions "Name=DBInstanceIdentifier,Value=${db_name}" --evaluation-periods 2 --alarm-actions $warning_topic --ok-actions $warning_topic
  aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-Read-Iops-Warning

  echo "Setup alerts on Write Iops"
  aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-Write-Iops-Critical --alarm-description "Alarm/page when # of db Write Iops exceed 80% of total IOPS" --metric-name WriteIOPS --namespace AWS/RDS --statistic Average --period 300 --threshold $write_iops_critical --comparison-operator GreaterThanOrEqualToThreshold --dimensions "Name=DBInstanceIdentifier,Value=${db_name}" --evaluation-periods 2 --alarm-actions $critical_topic --ok-actions $warning_topic
  aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-Write-Iops-Critical
  aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-Write-Iops-Warning --alarm-description "Alarm/email when # of db Write Iops exceed 70% of total IOPS" --metric-name WriteIOPS --namespace AWS/RDS --statistic Average --period 300 --threshold $write_iops_warning --comparison-operator GreaterThanOrEqualToThreshold --dimensions "Name=DBInstanceIdentifier,Value=${db_name}" --evaluation-periods 2 --alarm-actions $warning_topic --ok-actions $warning_topic
  aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-Write-Iops-Warning
fi

if [ "$dbonly" == "" -o "$dbonly" == "n" ]; then
  # Setup alerts on GoldengateLag
  echo "Setup alerts on GoldengateLag"
  aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-GoldengateLag-Critical --alarm-description "Alarm/page when the replication lag exceed this threshold" --metric-name ${db_name}-HEARTBEAT --namespace GoldengateLag --statistic Average --period 300 --threshold $goldengate_lag_critical --comparison-operator GreaterThanOrEqualToThreshold --evaluation-periods 1 --alarm-actions $critical_topic --ok-actions $warning_topic
  aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-GoldengateLag-Critical

  aws --profile $profile --region $region cloudwatch put-metric-alarm --alarm-name ${db_name}-GoldengateLag-Warning --alarm-description "Alarm/email when the replication lag exceed this threshold" --metric-name ${db_name}-HEARTBEAT --namespace GoldengateLag --statistic Average --period 300 --threshold $goldengate_lag_warning --comparison-operator GreaterThanOrEqualToThreshold --evaluation-periods 1 --alarm-actions $warning_topic --ok-actions $warning_topic
  aws --profile $profile --region $region cloudwatch describe-alarms --alarm-names ${db_name}-GoldengateLag-Warning
fi