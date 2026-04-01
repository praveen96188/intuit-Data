. /l/orcl

cluster_postgres=$1
db_name=$2

logdir=./log
mkdir -p $logdir

# number of days to keep log files
DAYS_KEPT_LOG=14

if [ "${VPC_ENV}" == "ppd" ]; then
  DB_ENDPOINT="cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com"
  topic_arn="arn:aws:sns:us-west-2:152430470825:DB-sbg-psp-critical-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  DB_ENDPOINT="cjls0bohfgpq.us-west-2.rds.amazonaws.com"

fi

# Configuration variables
DB_PORT="5432"
DB_USER="postgres"
the_subject="Aurora PostgreSQL Connection Failure Alert"
# Temporary file to store the result
RESULT_FILE=${db_name}_connection_chk.log

# Test the database connection
export PGPASSWORD=`cat .pp|grep ${1::-1}|awk '{print $2}'`
psql -h $cluster_postgres.$DB_ENDPOINT -p "$DB_PORT" -U "$DB_USER" -d "$db_name" -c '\q' > "$RESULT_FILE" 2>&1
CONNECTION_RESULT=$?

# Check the connection result
if [ $CONNECTION_RESULT -ne 0 ]; then
    ERROR_MESSAGE=$(<"$RESULT_FILE")

    echo "Connection failed. Sending notification..."
    echo "Error: $ERROR_MESSAGE"

    # Publish to SNS topic
    /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject " $cluster_postgres-${the_subject}" --message "$ERROR_MESSAGE"
else
    echo "Connection successful."
fi

# Clean up
rm -f "$RESULT_FILE"

