# Usage:./create_rds_dependency_w_cli.sh <AppEnv> <DBType> <VPC>
#       where <AppEnv> should be qa, e2e, prf, stg, sbx or prod
#             <DBType> should be clusterdb or reportdb
#             <VPC> should be vpc-1, vpc-2, etc

if [ $# -eq 3 ]; then 
  app_env=$1
  db_type=$2
  vpc=$3
else
  echo "Usage ./create_rds_dependency_w_cli.sh <AppEnv> <DBType> <VPC>"
  echo "      where <AppEnv> should be qa, e2e, prf, stg, sbx or prod"
  echo "            <DBType> should be clusterdb or reportdb"
  echo "            <VPC> should be vpc-1, vpc-2, etc"
  exit 1
fi

case "$app_env" in
  e2e|prf|qa)
      if [ $app_env == "qa" ]; then
        tag_env="qal"
      else
        tag_env=$app_env
      fi
      vpc_env="ppd"
      ;;
  prod|sbx|stg)
      if [ $app_env == "sbx" ]; then
        tag_env="sbx"
      elif [ $app_env == "stg" ]; then
        tag_env="stg"
      else
        tag_env="prd"
      fi
      vpc_env="prod"
      ;;
    *)
      echo "<AppEnv> not recognized"
      exit 1
      ;;
esac

vpcid=`aws --profile sbg-psp-${vpc_env} ec2 describe-vpcs --filters "Name=tag:Name,Values=${vpc}" --query 'Vpcs[0].VpcId' |cut -d'"' -f2`

subnet1=`aws --profile sbg-psp-${vpc_env} ec2 describe-subnets --filter "Name=vpc-id,Values=$vpcid" "Name=tag:Name,Values=DataSubnetAz1" --query 'Subnets[0].SubnetId' |cut -d'"' -f2`
subnet2=`aws --profile sbg-psp-${vpc_env} ec2 describe-subnets --filter "Name=vpc-id,Values=$vpcid" "Name=tag:Name,Values=DataSubnetAz2" --query 'Subnets[0].SubnetId' |cut -d'"' -f2`
subnet3=`aws --profile sbg-psp-${vpc_env} ec2 describe-subnets --filter "Name=vpc-id,Values=$vpcid" "Name=tag:Name,Values=DataSubnetAz3" --query 'Subnets[0].SubnetId' |cut -d'"' -f2`

# Crearte db parameter group
aws --profile sbg-psp-${vpc_env} rds copy-db-parameter-group \
    --source-db-parameter-group-identifier oracle-ee-12-1-psp \
    --target-db-parameter-group-identifier oracle-ee-12-1-psp-${app_env}-${db_type}-${vpc} \
    --target-db-parameter-group-description oracle-ee-12-1-psp-${app_env}-${db_type}-${vpc} \

aws --profile sbg-psp-${vpc_env} rds modify-db-parameter-group --db-parameter-group-name oracle-ee-12-1-psp-${app_env}-${db_type}-${vpc} --parameters "ParameterName=nls_length_semantics, ParameterValue=BYTE, ApplyMethod=immediate" 

aws --profile sbg-psp-${vpc_env} rds modify-db-parameter-group --db-parameter-group-name oracle-ee-12-1-psp-${app_env}-${db_type}-${vpc} --parameters "ParameterName=open_cursors, ParameterValue=10000, ApplyMethod=immediate" 

aws --profile sbg-psp-${vpc_env} rds modify-db-parameter-group --db-parameter-group-name oracle-ee-12-1-psp-${app_env}-${db_type}-${vpc} --parameters "ParameterName=enable_goldengate_replication, ParameterValue=true, ApplyMethod=immediate" 

aws --profile sbg-psp-${vpc_env} rds modify-db-parameter-group --db-parameter-group-name oracle-ee-12-1-psp-${app_env}-${db_type}-${vpc} --parameters "ParameterName=optimizer_adaptive_features, ParameterValue=true, ApplyMethod=pending-reboot" 

if [ "$app_env" == "prod" -o "$app_env" == "prf" ]; then
  # enable hugepages
  aws --profile sbg-psp-${vpc_env} rds modify-db-parameter-group --db-parameter-group-name oracle-ee-12-1-psp-${app_env}-${db_type}-${vpc} --parameters "ParameterName=memory_target, ParameterValue=0, ApplyMethod=pending-reboot" 
  aws --profile sbg-psp-${vpc_env} rds modify-db-parameter-group --db-parameter-group-name oracle-ee-12-1-psp-${app_env}-${db_type}-${vpc} --parameters "ParameterName=memory_max_target, ParameterValue=0, ApplyMethod=pending-reboot" 
  aws --profile sbg-psp-${vpc_env} rds modify-db-parameter-group --db-parameter-group-name oracle-ee-12-1-psp-${app_env}-${db_type}-${vpc} --parameters "ParameterName=sga_target, ParameterValue='{DBInstanceClassMemory*3/4}', ApplyMethod=pending-reboot" 
  aws --profile sbg-psp-${vpc_env} rds modify-db-parameter-group --db-parameter-group-name oracle-ee-12-1-psp-${app_env}-${db_type}-${vpc} --parameters "ParameterName=sga_max_size, ParameterValue='{DBInstanceClassMemory*3/4}', ApplyMethod=pending-reboot" 
  aws --profile sbg-psp-${vpc_env} rds modify-db-parameter-group --db-parameter-group-name oracle-ee-12-1-psp-${app_env}-${db_type}-${vpc} --parameters "ParameterName=pga_aggregate_target, ParameterValue='{DBInstanceClassMemory*1/10}', ApplyMethod=pending-reboot" 
  aws --profile sbg-psp-${vpc_env} rds modify-db-parameter-group --db-parameter-group-name oracle-ee-12-1-psp-${app_env}-${db_type}-${vpc} --parameters "ParameterName=use_large_pages, ParameterValue=ONLY, ApplyMethod=pending-reboot" 

fi

# Create db option group
aws --profile sbg-psp-${vpc_env} rds create-option-group \
    --option-group-name oracle-ee-12-1-${app_env}-${db_type}-${vpc} \
    --engine-name oracle-ee \
    --major-engine-version 12.1 \
    --option-group-description "oracle-ee-12-1-${app_env}-${db_type}-${vpc}"

aws --profile sbg-psp-${vpc_env} rds add-option-to-option-group \
    --option-group-name oracle-ee-12-1-${app_env}-${db_type}-${vpc} \
    --options "OptionName=Timezone, OptionSettings=[{Name=TIME_ZONE, Value=US/Pacific}]"

aws --profile sbg-psp-${vpc_env} rds add-option-to-option-group \
    --option-group-name oracle-ee-12-1-${app_env}-${db_type}-${vpc} \
    --options "OptionName=NATIVE_NETWORK_ENCRYPTION, OptionSettings=[{Name=SQLNET.CRYPTO_CHECKSUM_SERVER, Value=REQUESTED}]"

aws --profile sbg-psp-${vpc_env} rds add-option-to-option-group \
    --option-group-name oracle-ee-12-1-${app_env}-${db_type}-${vpc} \
    --options "OptionName=NATIVE_NETWORK_ENCRYPTION, OptionSettings=[{Name=SQLNET.ENCRYPTION_SERVER, Value=REQUIRED}]"


# Create subnet group
aws --profile sbg-psp-${vpc_env} rds create-db-subnet-group --db-subnet-group-name ${app_env}-${db_type}-${vpc} --db-subnet-group-description "${app_env}-${db_type}-${vpc} subnet group" --subnet-ids ${subnet1} ${subnet2} ${subnet3}

# Create security group
aws --profile sbg-psp-${vpc_env} ec2 create-security-group --group-name ${app_env}-${db_type}-${vpc} --description "${app_env}-${db_type}-${vpc} security group" --vpc-id ${vpcid} 
group_id=`aws --profile sbg-psp-${vpc_env} ec2 describe-security-groups --filters "Name=group-name,Values=${app_env}-${db_type}-${vpc}" --query 'SecurityGroups[*].{GroupId: GroupId}' |grep "GroupId" |cut -d'"' -f4`
echo "Security Group ID: $group_id"
aws --profile sbg-psp-${vpc_env} ec2 create-tags --resources $group_id --tags Key=Name,Value=${app_env}-${db_type}-${vpc}

subnet_name_prefixes="PrivateSubnetAz replicationSubnetAz DataSubnetAz"

for subnet_name_prefix in `echo $subnet_name_prefixes`; do
  for az in 1 2 3; do
    client_subnet=`aws --profile sbg-psp-${vpc_env} ec2 describe-subnets --filter "Name=vpc-id,Values=${vpcid}" "Name=tag:Name,Values=${subnet_name_prefix}${az}" --query 'Subnets[0].CidrBlock' |cut -d\" -f2`
    echo "client_subnet=${client_subnet}"
    aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $group_id --protocol tcp --port 1521 --cidr ${client_subnet}
  done
done
aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $group_id --protocol tcp --port 1521 --cidr 10.143.0.0/19
aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $group_id --protocol tcp --port 1521 --cidr 10.161.0.0/19
