. /l/orcl

cluster_postgres=$1
db_name=$2

logdir=./log
mkdir -p $logdir

# number of days to keep log files
DAYS_KEPT_LOG=14

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:152430470825:DB-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:893547637742:apg-db-sbg-psp-critical-${VPC_ENV}-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi
for instance_name in `aws --region us-west-2 rds describe-db-clusters --db-cluster-identifier ${cluster_postgres} --query 'DBClusters[0].DBClusterMembers' | grep DBInstanceIdentifier | awk '{print $2}'|sed 's/"//'|sed 's/"//' | rev | cut -c2- | rev`
do
./run_postgres.sh ${instance_name} $db_name default_partition_rows_check.sql "-v dbname=${db_name}" > ${logdir}/${db_name}_default_partition.log
the_subject="Default_partition_row_count"
row_count=`tail -3 ${logdir}/${db_name}_default_partition.log |head -1 |tr -s ' ' ' '` > ${logdir}/${db_name}_partition_table_log.txt
if [ "$row_count" -gt 0 ] ; then
/usr/bin/aws sns publish --topic-arn arn:aws:sns:us-west-2:152430470825:PSP_Partition_table_alert_test --subject "${the_subject}" --message "`cat ${logdir}/${db_name}_partition_table_log.txt`"
else
/usr/bin/aws sns publish --topic-arn arn:aws:sns:us-west-2:152430470825:PSP_Partition_table_alert_test --subject "${the_subject}" --message  "No rows in default_partition"
fi
done

