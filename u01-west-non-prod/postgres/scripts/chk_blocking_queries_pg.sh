. /l/orcl

cluster_postgres=$1
db_name=$2

logdir=./log
mkdir -p $logdir

# number of days to keep log files
DAYS_KEPT_LOG=14

#export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:152430470825:DB-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
  topic_arn1="arn:aws:sns:${AWS_DEFAULT_REGION}:152430470825:DB-sbg-psp-critical-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:893547637742:apg-db-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
  topic_arn1="arn:aws:sns:${AWS_DEFAULT_REGION}:893547637742:apg-db-sbg-psp-critical-${VPC_ENV}-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

./run_postgres.sh ${cluster_postgres} $db_name chk_blocking_queries_pg.sql "-v dbname=${db_name}" > ${logdir}/${db_name}_chk_blocking_queries.log
./run_postgres.sh ${cluster_postgres} $db_name chk_blocking_queries_pg_10minWait.sql "-v dbname=${db_name}" > ${logdir}/${db_name}_chk_blocking_queries_pg_10minWait.log

the_subject=" $cluster_postgres Blocking Queries > 5min "
the_subject1=" $cluster_postgres Blocking Queries > 10min "
if [ `cat ${logdir}/${db_name}_chk_blocking_queries.log |grep -i "0 rows" |wc -l` -ne 1 ]; then
/usr/bin/aws sns publish --topic-arn ${topic_arn} --subject " $cluster_postgres - ${the_subject}" --message "`cat ${logdir}/${db_name}_chk_blocking_queries.log`"

if [ `cat ${logdir}/${db_name}_chk_blocking_queries_pg_10minWait.log |grep -i "0 rows" |wc -l` -ne 1 ]; then
/usr/bin/aws sns publish --topic-arn ${topic_arn1} --subject " $cluster_postgres - ${the_subject1}" --message "`cat ${logdir}/${db_name}_chk_blocking_queries_pg_10minWait.log`"
fi
fi
