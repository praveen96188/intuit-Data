. /l/orcl
if [ `uname -a |grep Linux |wc -l` -gt 0 ]; then
  profile_setting="--region us-west-2"
else
  profile_setting="--profile sbg-psp-ppd --region us-west-2"
fi

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:us-west-2:152430470825:db-sbg-psp-ppd-t"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

dms_task_name=$1
ReplicationTaskArn=`aws $profile_setting dms describe-replication-tasks --query "ReplicationTasks[*].ReplicationTaskArn" --filters "Name=replication-task-id,Values=${dms_task_name}" |grep dms |cut -d'"' -f2`

aws $profile_setting dms describe-replication-tasks --filters "Name=replication-task-id,Values=${dms_task_name}" > ${dms_task_name}.json
if [ -f ${dms_task_name}_status_new.txt ]; then
  mv ${dms_task_name}_status_new.txt ${dms_task_name}_status_old.txt
else
  touch ${dms_task_name}_status_old.txt
fi

ReplicationTaskArn=`grep ReplicationTaskArn ${dms_task_name}.json |cut -d'"' -f4`

rm -f ${dms_task_name}_status_new.txt
Status=`grep \"Status\" ${dms_task_name}.json |cut -d'"' -f4`
echo "Status = $Status" >> ${dms_task_name}_status_new.txt

FullLoadProgressPercent=`grep FullLoadProgressPercent ${dms_task_name}.json |cut -d':' -f2 | cut -d',' -f1`
echo "FullLoadProgressPercent =$FullLoadProgressPercent" >> ${dms_task_name}_status_new.txt

TablesErrored=`grep TablesErrored ${dms_task_name}.json |cut -d':' -f2 | cut -d',' -f1`
echo "TablesErrored =$TablesErrored" >> ${dms_task_name}_status_new.txt

diff ${dms_task_name}_status_new.txt ${dms_task_name}_status_old.txt > ${dms_task_name}_status_diff.txt
if [ `cat ${dms_task_name}_status_diff.txt |grep -v FullLoadProgressPercent |wc -l` -gt 2 -o `cat ${dms_task_name}_status_diff.txt |grep "FullLoadProgressPercent = 100" |wc -l` -gt 0 ]; then
  echo "FROM:" > ${dms_task_name}_status.txt
  cat ${dms_task_name}_status_old.txt >> ${dms_task_name}_status.txt
  echo "" >> ${dms_task_name}_status.txt
  echo "TO:" >> ${dms_task_name}_status.txt
  cat ${dms_task_name}_status_new.txt >> ${dms_task_name}_status.txt
  echo "INFO: DMS ${dms_task_name} status has changed"
  /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "INFO: DMS ${dms_task_name} status has changed" --message "`cat ${dms_task_name}_status.txt`"
fi

