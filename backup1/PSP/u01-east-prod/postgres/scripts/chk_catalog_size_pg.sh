. /l/orcl

cluster_postgres=$1
db_name=$2

logdir=./log
mkdir -p $logdir

# number of days to keep log files
DAYS_KEPT_LOG=14

#export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:475968958217:db-sbg-qbo-warning-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:094918931292:db-and-app-sbg-qbo-critical-${VPC_ENV}-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

./run_postgres.sh ${cluster_postgres} $db_name chk_catalog_size_pg.sql "-v dbname=${db_name}" > ${logdir}/${db_name}_chk_catalog_siz_pg.txt

chk_pgcatalog_size=`cat log/${db_name}_chk_catalog_siz_pg.txt|grep ^' pg_catalog'| awk '{print $3}'`
chk_info_schema_size=`cat log/${db_name}_chk_catalog_siz_pg.txt|grep ^' information_schema'| awk '{print $3}'`
echo "chk_pgcatalog_size=$chk_pgcatalog_size"
echo "chk_info_schema_size=$chk_info_schema_size"

aws cloudwatch put-metric-data --region us-east-2  --metric-name ${db_name}_pgcatalog_size --namespace "PostgresCatalogSize" --value $chk_pgcatalog_size --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`
aws cloudwatch put-metric-data --region us-east-2 --metric-name ${db_name}_info_schema_size --namespace "PostgresCatalogSize" --value $chk_info_schema_size --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`

