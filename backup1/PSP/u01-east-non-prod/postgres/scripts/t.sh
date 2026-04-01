. /l/orcl

cluster_postgres=$1
db_name=$2

logdir=./log
mkdir -p $logdir

# number of days to keep log files
DAYS_KEPT_LOG=14

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:152430470825:db-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:893547637742:apg-db-sbg-psp-critical-${VPC_ENV}-a-intuit-com"

else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

for instance_name in `aws --region us-west-2 rds describe-db-clusters --db-cluster-identifier ${cluster_postgres} --query 'DBClusters[0].DBClusterMembers' | grep DBInstanceIdentifier | awk '{print $2}'|sed 's/"//'|sed 's/"//'`
do
    ./run_postgres_instance.sh ${instance_name} $db_name chk_active_session_pg.sql "-v dbname=${db_name}" > ${logdir}/${instance_name}_${db_name}__chk_active_connection.txt
    chk_active_connection=`tail -3 ${logdir}/${instance_name}_${db_name}__chk_active_connection.txt |head -1 |tr -s ' ' ' '`
    echo "chk_active_connection_${instance_name}=$chk_active_connection"
    aws cloudwatch put-metric-data --metric-name ${instance_name} --namespace "DatabaseActiveConnection" --value $chk_active_connection --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`
done

