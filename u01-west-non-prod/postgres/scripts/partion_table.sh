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
    for instance_name1 in `echo ${instance_name} | awk '{print $1}'| rev | cut -c1- | rev`
    do
./run_postgres.sh ${instance_name1} $db_name chk_instance_db_mode.sql "-v dbname=${db_name}" > ${logdir}/${instance_name}_${db_name}_chk_instance_db_mode.log
 if [ "`grep "WRITER" ${logdir}/${instance_name1}_${db_name}_chk_instance_db_mode.log | wc -l`" -eq "1" ] ; then

    ./run_postgres.sh ${instance_name} $db_name partition_table.sql "-v dbname=${db_name}" > ${logdir}/${instance_name}_${db_name}__partition_table.txt
    record_count=`tail -3 ${logdir}/${instance_name}_${db_name}__partition_table.txt |head -1 |tr -s ' ' ' '`
    if [ "$record_count" -gt 0 ]; then
    # Send email if record count is greater than 0
    #echo -e "Subject:$SUBJECT\nFrom:$FROM_EMAIL\n\n$BODY" | sendmail -v $TO_EMAIiL
    echo -e "number of records in $instance_name is $record_count"
else
    echo -e "number of records is 0"
    # Alternatively, you can use mailx or mail command depending on your system
    # echo -e "$BODY" | mailx -s "$SUBJECT" $TO_EMAIL
fi
 fi
done
done
