#!/bin/bash
. /l/orcl

cluster_postgres=$1
db_name=$2

# Set the AWS region (replace with your region if necessary)
REGION="us-east"

# Get the list of all DB instances
db_instances=$(aws rds describe-db-instances --region "$REGION" --query "DBInstances[*].DBInstanceIdentifier" --output text)

# Initialize variables to store writer and reader instance names
writer_instance=""
reader_instances=()

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:152430470825:db-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:893547637742:apg-db-sbg-psp-critical-${VPC_ENV}-a-intuit-com"

else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi


# Loop through each DB instance to check if it's a writer or reader
for db_instance in $db_instances; do
  # Check if the DB instance is a writer or reader
  is_writer=$(aws rds describe-db-instances --region "$REGION" --db-instance-identifier "$db_instance" --query "DBInstances[0].ReadReplicaSourceDBInstanceIdentifier" --output text)
  
  if [ "$is_writer" == "None" ]; then
    # This is a writer instance (primary)
    writer_instance=$db_instance
  else
    # This is a reader instance (replica)
    reader_instances+=("$db_instance")
  fi
done

# Output the results
if [ -n "$writer_instance" ]; then
  echo "Writer (Primary) Instance: $writer_instance"
else
  echo "No writer instance found."
fi

if [ ${#reader_instances[@]} -gt 0 ]; then
  echo "Reader (Replica) Instances:"
  for reader in "${reader_instances[@]}"; do
    echo "  - $reader"
  done
else
  echo "No reader instances found."
fi
