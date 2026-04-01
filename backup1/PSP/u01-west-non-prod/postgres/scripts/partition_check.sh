.  /l/orcl

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
    ./run_postgres.sh ${instance_name} $db_name PSP_ENTITY_DEFAULT_PATITION_ROWS_check.sql "-v dbname=${db_name}" > ${logdir}/${instance_name}_${db_name}__psp_entitity_publish_default_partition_rows.log
    ./run_postgres.sh ${instance_name} $db_name PSP_OUTBOX_ROWS_check.sql "-v dbname=${db_name}" > ${logdir}/${instance_name}_${db_name}__psp_outbox_default_partition_rows.log
done
row_count=`tail -3 ${logdir}/${instance_name}_${db_name}__psp_entitity_publish_default_partition_rows.log |head -1 |tr -s ' ' ' '`
the_subject="Default_partition_row_count_for_table_PSP_ENTITY_PUBLISH"
the_subject2="Default_partition_row_count_for_table_PSP_OUTBOX"
row_count2=`tail -3 ${logdir}/${instance_name}_${db_name}__psp_outbox_default_partition_rows.log |head -1 |tr -s ' ' ' '`
if [ "$row_count" -gt 0 ] ; then
/usr/bin/aws sns publish --topic-arn arn:aws:sns:us-west-2:152430470825:PSP_Partition_table_alert_test --region us-west-2  --subject "${the_subject}" --message "psp_entitity_publish_Default_partition contain ${row_count} rows- Please take action to move the data from default to specific partition
Referce wiki link https://wiki.intuit.com/pages/viewpage.action?spaceKey=EMSPD&title=PSP+-+Outbox+On-boarding"
if [ "$row_count2" -gt 0 ] ; then
/usr/bin/aws sns publish --topic-arn arn:aws:sns:us-west-2:152430470825:PSP_Partition_table_alert_test --region us-west-2  --subject "${the_subject2}" --message "psp_outbox__Default_partition contain ${row_count2} rows- Please take action to move the data from default to specific partition
Referce wiki link https://wiki.intuit.com/pages/viewpage.action?spaceKey=EMSPD&title=PSP+-+Outbox+On-boarding"
fi
fi

