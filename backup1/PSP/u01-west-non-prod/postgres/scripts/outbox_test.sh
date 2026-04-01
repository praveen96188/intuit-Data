.  /l/orcl

cluster_postgres=$1
db_name=$2

logdir=./log
mkdir -p $logdir

# number of days to keep log files
DAYS_KEPT_LOG=14

#export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:152430470825:DB-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:893547637742:apg-db-sbg-psp-warning-${VPC_ENV}-a-intuit-com"

else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

./run_postgres.sh ${cluster_postgres} $db_name PSP_ENTITY_DEFAULT_PATITION_ROWS_check.sql "-v dbname=${db_name}" > ${logdir}/${cluster_postgres}_${db_name}__psp_entitity_publish_default_partition_rows.log
./run_postgres.sh ${cluster_postgres} $db_name PSP_OUTBOX_ROWS_check.sql "-v dbname=${db_name}" > ${logdir}/${cluster_postgres}_${db_name}__psp_outbox_default_partition_rows.log
./run_postgres.sh ${cluster_postgres} $db_name rep_slot_check.sql "-v dbname=${db_name}" > ${logdir}/${cluster_postgres}_${db_name}__rep_slot_check.log

row_count=`tail -3 ${logdir}/${cluster_postgres}_${db_name}__psp_entitity_publish_default_partition_rows.log |head -1 |tr -s ' ' ' '`
the_subject="ALERT: $cluster_postgres -PSP_ENTITY_PUBLISH table partition issue"
the_subject2="ALERT: $cluster_postgres -PSP_OUTBOX table partition issue"
row_count2=`tail -3 ${logdir}/${cluster_postgres}_${db_name}__psp_outbox_default_partition_rows.log |head -1 |tr -s ' ' ' '`
the_subject3="ALERT: $cluster_postgres -Replication slot  issue"
row_count3=`tail -2 ${logdir}/${cluster_postgres}_${db_name}__rep_slot_check.log |head -1 |cut -c 2`

if [ "$row_count" -ne 0 ] ; then
/usr/bin/aws sns publish --topic-arn ${topic_arn} --region us-west-2  --subject "${the_subject}" --message "psp_entity_publish_Default partition table contain ${row_count} rows.  
ACTION:cleanup psp_entity_publish_Default partition table , Reference wiki https://wiki.intuit.com/pages/viewpage.action?spaceKey=EMSPD&title=PSP+-+Outbox+On-boarding"
if [ "$row_count2" -ne 0 ] ; then
/usr/bin/aws sns publish --topic-arn ${topic_arn} --region us-west-2  --subject "${the_subject2}" --message "psp_outbox_Default partition table contain ${row_count2} rows.   
ACTION:cleanup psp_outbox_Default partition table , Reference wiki https://wiki.intuit.com/pages/viewpage.action?spaceKey=EMSPD&title=PSP+-+Outbox+On-boarding"
fi
fi
if [ "$row_count3" -ne 0 ] ; then
  echo "Non-active slot:$row_count3"
/usr/bin/aws sns publish --topic-arn ${topic_arn} --region us-west-2  --subject "${the_subject3}" --message "`cat ${logdir}/${cluster_postgres}_${db_name}__rep_slot_check.log`"
fi


