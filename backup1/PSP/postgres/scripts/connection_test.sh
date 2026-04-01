. /l/orcl

#cluster_postgres=$1
#db_name=$2

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
cronTest_logfile="$(date +%Y_%m_%d_%H_%M_%S).log"
./cron_test.sh  > ${logdir}/${cronTest_logfile}

the_subject=" $VPC_ENV $AWS_DEFAULT_REGION Connection Issue "
if grep -qi "failed" ${logdir}/${cronTest_logfile}; then
/usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`${logdir}/${cronTest_logfile}`"
fi
