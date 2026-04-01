. /l/orcl

cluster_postgres=$1
db_name=$2
instance_postgres_1=$3
instance_postgres_2=$4



logdir=./log
mkdir -p $logdir

# number of days to keep log files
DAYS_KEPT_LOG=14

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:475968958217:db-sbg-qbo-warning-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:094918931292:db-and-app-sbg-qbo-critical-${VPC_ENV}-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi


./run_postgres_monolith_pds_wr.sh

export PGPASSWORD=`cat .cp1`
psql_cmd="psql --username=postgres -h ${1}.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -p 5432 ${2} --echo-all -P pager=off -f $3 $4"
eval "$psql_cmd"

aws cloudwatch put-metric-data --metric-name ${instance_postgres_1} --namespace "DatabaseActiveConnection" --value $chk_active_connection --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`

./run_postgres_monolith_pds_rr.sh

export PGPASSWORD=`cat .cp2`
psql_cmd="psql --username=postgres -h ${1}2.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -p 5432 ${2} --echo-all -P pager=off -f $3 $4"
eval "$psql_cmd"

aws cloudwatch put-metric-data --metric-name ${instance_postgres_2} --namespace "DatabaseActiveConnection" --value $chk_active_connection --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`

