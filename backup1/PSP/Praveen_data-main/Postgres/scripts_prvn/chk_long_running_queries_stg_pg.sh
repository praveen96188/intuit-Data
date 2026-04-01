. /l/orcl

cluster_postgres=$1
db_name=$2
instance_postgres_1=$3
instance_postgres_2=$4
instance_postgres_3=$5
instance_postgres_4=$6
instance_postgres_5=$7

logdir=./log
mkdir -p $logdir

# number of days to keep log files
DAYS_KEPT_LOG=14

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`
slack_topic_arn="arn:aws:sns:us-west-2:893547637742:psp-staging-slack"

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:152430470825:db-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:893547637742:apg-db-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

for i in {1..2}
do
instance_postgres=${cluster_postgres}${i}
./run_postgres_instance.sh ${instance_postgres} $db_name chk_long_running_queries_pg.sql "-v dbname=${db_name}" > ${logdir}/${instance_postgres}_chk_long_running_queries.log

if [ `cat ${logdir}/${instance_postgres}_chk_long_running_queries.log |grep -i "0 rows" |wc -l` -ne 1 ]; then
 echo "good"
 if [[ `cat ${logdir}/${instance_postgres}_chk_long_running_queries.log |grep state|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'` == "idle in transaction" && `cat ${logdir}/${instance_postgres}_chk_long_running_queries.log |grep query_duration_secs|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'| xargs printf "%.*f\n" "0"` -ge 900 ]]; then
  the_subject="Query in idle in transaction state (>15 mins)"
  /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_postgres}_chk_long_running_queries.log`"
  /usr/bin/aws sns publish --topic-arn ${slack_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_postgres}_chk_long_running_queries.log`"
 elif [[ `cat ${logdir}/${instance_postgres}_chk_long_running_queries.log |grep usename|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'` == "pspapp" && `cat ${logdir}/${instance_postgres}_chk_long_running_queries.log |grep query_duration_secs|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'| xargs printf "%.*f\n" "0"` -ge 300 ]]; then
  the_subject="Long running query (> 5 Mins) - OLTP"
  /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_postgres}_chk_long_running_queries.log`"
  /usr/bin/aws sns publish --topic-arn ${slack_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_postgres}_chk_long_running_queries.log`"
 elif [[ `cat ${logdir}/${instance_postgres}_chk_long_running_queries.log |grep usename|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'|awk -F, '!seen[$1]++'` == "pspbatch_rw_user" && `cat ${logdir}/${instance_postgres}_chk_long_running_queries.log |grep query_duration_secs|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'|awk -F"." '{print $1}'|awk -F, '!seen[$1]++'| xargs printf "%.*f\n" "0"` -ge 3600 ]]; then
  the_subject="Long running query (> 60 Mins) - OLAP"
  /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_postgres}_chk_long_running_queries.log`"
  /usr/bin/aws sns publish --topic-arn ${slack_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_postgres}_chk_long_running_queries.log`"
 fi
fi

done

#the_subject="Long running Queries"

#if [ `cat ${logdir}/${instance_postgres}_chk_long_running_queries.log |grep -i "0 rows" |wc -l` -ne 1 ]; then
#/usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_postgres}_chk_long_running_queries.log`"
#fi

