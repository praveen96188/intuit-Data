# Usage: ./copy_rds_cross_region.sh <AppEnv> <DBType> <Source DBName> <Target DBName> <Source Region> <Target Region> <Target VPC> [<DBInstanceClass>]
#        where <AppEnv> should be qa, e2e, stg, sbx, prf, prod
#              <DBType> should be clusterdb or reportdb
#              <Source DBName> and <Target DBName should be us-west-2 or us-east-2
#              <Target VPC> should be vpc-1, vpc-2, etc
#	       <DBInstanceClass> should db instance class like db.r4.8xlarge. It is optional with the default db.r4.xlarge
# Example: ./copy_rds_cross_region.sh e2e clusterdb eqbopc50 eqbosc50 us-west-2 us-east-2 vpc-2

if [ $# -eq 7 -o $# -eq 8 ]; then
  app_env=$1
  db_type=$2
  source_db=$3
  target_db=$4
  source_region=$5
  target_region=$6
  target_vpc=$7
  db_instance_class=$8
  if [ "$db_instance_class" == "" ]; then
    db_instance_class="db.r5.large"
  fi
else
  echo "Usage: ./copy_rds_cross_region.sh <AppEnv> <DBType> <Source DBName> <Target DBName> <Source Region> <Target Region> <Target VPC> [<DBInstanceClass>]"
  echo "      where <AppEnv> should be qa, e2e, stg, sbx, prf, prod"
  echo "            prodn means new prod (with less half of regular prod storage)"
  echo "            <DBType> should be clusterdb or reportdb"
  echo "            <Source DBName> and <Target DBName should be us-west-2 or us-east-2"
  echo "            <Target VPC> should be vpc-1, vpc-2, etc"
  echo "	    <DBInstanceClass> should db instance class like db.r4.8xlarge. It is optional with the default db.r4.xlarge"
  exit 1
fi

export AWS_PAGER=""

echo "At `date`: Start"

case "$app_env" in
    e2e|prf|qa|ds1|ds2|ds3|ds4|ds5|sys|pds)
      if [ $app_env == "ds1" ] || [ $app_env == "ds2" ] || [ $app_env == "ds3" ] || [ $app_env == "ds4" ] || [ $app_env == "ds5" ] || [ $app_env == "e2e" ] || [ $app_env == "sys" ] || [ $app_env == "pds" ]; then
        tag_env="qa"
      elif [ $app_env == "pds" ] || [ $app_env == "prf" ]
      then
        tag_env="prf"
      else
        tag_env=$app_env
      fi
      monitoring_role="arn:aws:iam::152430470825:role/rds-monitoring-role"
      hostedzoneid="ZPMOD8K08QNEL"
      dnsname="sbg-psp-ppd.a.intuit.com"
      vpc_env="ppd"
      profile=sbg-psp-ppd
      ;;
    prod)
      tag_env="prd"
      monitoring_role="arn:aws:iam::893547637742:role/rds-monitoring-role"
      hostedzoneid="Z35GYKEF3QQE9L"
      dnsname="sbg-psp-prod.a.intuit.com"
      vpc_env="prod"
      profile=sbg-psp-prod
      ;;
    *)
      echo "<AppEnv> not recognized"
      exit 1
      ;;
esac

if [ `uname -a |grep Linux|wc -l` -gt 0 ]; then
  profile_setting=''
else
  profile_setting="--profile ${profile}"
fi
  
# Get root id
root_id=`aws ${profile_setting} --region ${target_region} iam get-group --group-name "Admins" --query 'Group.Arn' |cut -d: -f5`

query="Aliases[?AliasName==\`alias/rds-${target_db}\`].TargetKeyId"
kmskeyid=`aws ${profile_setting} --region ${target_region} kms list-aliases --query "$query" |grep '"' |cut -d'"' -f2`

if [ "$kmskeyid" != "" ]; then
  echo "CMK exists. Reuse it"
  echo kmskeyid=$kmskeyid
else
  # Generate kms policy file
  echo '{' > kms_policy_${vpc_env}.json
  echo '  "Id": "kms-policy-for-RDS",' >> kms_policy_${vpc_env}.json
  echo '  "Version": "2012-10-17",' >> kms_policy_${vpc_env}.json
  echo '  "Statement": [' >> kms_policy_${vpc_env}.json
  echo '    {' >> kms_policy_${vpc_env}.json
  echo '      "Sid": "Enable IAM User Permissions",' >> kms_policy_${vpc_env}.json
  echo '      "Effect": "Allow",' >> kms_policy_${vpc_env}.json
  echo '      "Principal": {' >> kms_policy_${vpc_env}.json
  echo '        "AWS": [' >> kms_policy_${vpc_env}.json
  echo "          \"arn:aws:iam::${root_id}:root\"" >> kms_policy_${vpc_env}.json
  echo '        ]' >> kms_policy_${vpc_env}.json
  echo '      },' >> kms_policy_${vpc_env}.json
  echo '      "Action": "kms:*",' >> kms_policy_${vpc_env}.json
  echo '      "Resource": "*"' >> kms_policy_${vpc_env}.json
  echo '    },' >> kms_policy_${vpc_env}.json
  echo '    {' >> kms_policy_${vpc_env}.json
  echo '      "Sid": "Allow access for Key Administrators",' >> kms_policy_${vpc_env}.json
  echo '      "Effect": "Deny",' >> kms_policy_${vpc_env}.json
  echo '      "Principal": {' >> kms_policy_${vpc_env}.json
  echo '        "AWS": [' >> kms_policy_${vpc_env}.json
  echo '          "*" ' >> kms_policy_${vpc_env}.json
  echo '        ]' >> kms_policy_${vpc_env}.json
  echo '      },' >> kms_policy_${vpc_env}.json
  echo '      "Action": [' >> kms_policy_${vpc_env}.json
  echo '        "kms:Disable*",' >> kms_policy_${vpc_env}.json
  echo '        "kms:Delete*",' >> kms_policy_${vpc_env}.json
  echo '        "kms:ScheduleKeyDeletion"' >> kms_policy_${vpc_env}.json
  echo '      ],' >> kms_policy_${vpc_env}.json
  echo '      "Resource": "*"' >> kms_policy_${vpc_env}.json
  echo '    },' >> kms_policy_${vpc_env}.json
  echo '    {' >> kms_policy_${vpc_env}.json
  echo '      "Sid": "Allow use of the key",' >> kms_policy_${vpc_env}.json
  echo '      "Effect": "Allow",' >> kms_policy_${vpc_env}.json
  echo '      "Principal": {' >> kms_policy_${vpc_env}.json
  echo '        "AWS": [' >> kms_policy_${vpc_env}.json
  echo '          "*" ' >> kms_policy_${vpc_env}.json
  echo '          ' >> kms_policy_${vpc_env}.json
  echo '        ]' >> kms_policy_${vpc_env}.json
  echo '      },' >> kms_policy_${vpc_env}.json
  echo '      "Action": [' >> kms_policy_${vpc_env}.json
  echo '        "kms:Encrypt",' >> kms_policy_${vpc_env}.json
  echo '        "kms:Decrypt",' >> kms_policy_${vpc_env}.json
  echo '        "kms:ReEncrypt*",' >> kms_policy_${vpc_env}.json
  echo '        "kms:GenerateDataKey*",' >> kms_policy_${vpc_env}.json
  echo '        "kms:DescribeKey"' >> kms_policy_${vpc_env}.json
  echo '      ],' >> kms_policy_${vpc_env}.json
  echo '      "Resource": "*"' >> kms_policy_${vpc_env}.json
  echo '    },' >> kms_policy_${vpc_env}.json
  echo '    {' >> kms_policy_${vpc_env}.json
  echo '      "Sid": "Allow attachment of persistent resources",' >> kms_policy_${vpc_env}.json
  echo '      "Effect": "Allow",' >> kms_policy_${vpc_env}.json
  echo '      "Principal": {' >> kms_policy_${vpc_env}.json
  echo '        "AWS": [' >> kms_policy_${vpc_env}.json
  echo '          "*" ' >> kms_policy_${vpc_env}.json
  echo '          ' >> kms_policy_${vpc_env}.json
  echo '        ]' >> kms_policy_${vpc_env}.json
  echo '      },' >> kms_policy_${vpc_env}.json
  echo '      "Action": [' >> kms_policy_${vpc_env}.json
  echo '        "kms:CreateGrant",' >> kms_policy_${vpc_env}.json
  echo '        "kms:ListGrants",' >> kms_policy_${vpc_env}.json
  echo '        "kms:RevokeGrant"' >> kms_policy_${vpc_env}.json
  echo '      ],' >> kms_policy_${vpc_env}.json
  echo '      "Resource": "*",' >> kms_policy_${vpc_env}.json
  echo '      "Condition": {' >> kms_policy_${vpc_env}.json
  echo '        "Bool": {' >> kms_policy_${vpc_env}.json
  echo '          "kms:GrantIsForAWSResource": true' >> kms_policy_${vpc_env}.json
  echo '        }' >> kms_policy_${vpc_env}.json
  echo '      }' >> kms_policy_${vpc_env}.json
  echo '    }' >> kms_policy_${vpc_env}.json
  echo '  ]' >> kms_policy_${vpc_env}.json
  echo '}' >> kms_policy_${vpc_env}.json

  # Create custom CMK
  echo "Create custom CMK..."
  kmskeyid=`aws ${profile_setting} --region ${target_region} kms create-key --description "Used for RDS ${target_db}" --policy file://kms_policy_${vpc_env}.json --query "KeyMetadata.KeyId" |cut -d'"' -f2`

  echo kmskeyid=$kmskeyid

  aws ${profile_setting} --region ${target_region} kms create-alias --alias-name alias/rds-${target_db} --target-key-id $kmskeyid
  aws ${profile_setting} --region ${target_region} kms enable-key-rotation --key-id $kmskeyid
fi

# Create temp RDS by performing a point-in-time recovery
temp_db="${source_db/psp/pit}"
source_subnet_group_name=`aws ${profile_setting} --region ${source_region} rds describe-db-instances --filters "Name=db-instance-id, Values=${source_db}" --query 'DBInstances[*].{DBSubnetGroup: DBSubnetGroup}'|grep "DBSubnetGroupName" |cut -d':' -f2|cut -d',' -f1 |cut -d'"' -f2`

echo "Create temp RDS ${temp_db} starting at `date`"
aws ${profile_setting} --region ${source_region} rds restore-db-instance-to-point-in-time --source-db-instance-identifier ${source_db} --target-db-instance-identifier ${temp_db}  --db-subnet-group-name ${source_subnet_group_name} --use-latest-restorable-time --db-name ${temp_db} --no-multi-az --db-instance-class ${db_instance_class}

while true
do
  status=`aws ${profile_setting} --region ${source_region} rds describe-db-instances --db-instance-identifier ${temp_db} |grep Status |grep available |wc -l`
  if [ $status = 1 ]; then
    echo "At `date`: Creating temp RDS ${temp_db} is completed"
    break 
  fi 
  echo "At `date`: Creating temp RDS ${temp_db} is in progress"
  sleep 60
done

# Create db snapshot from temp db
echo "Create db snapshot ${temp_db}-src starting at `date`"
aws ${profile_setting} --region ${source_region} rds create-db-snapshot --db-instance-identifier $temp_db --db-snapshot-identifier ${temp_db}-src

while true
do
  progress=`aws ${profile_setting} --region ${source_region} rds describe-db-snapshots --db-instance-identifier $temp_db --db-snapshot-identifier ${temp_db}-src |grep "PercentProgress"`
  status=`aws ${profile_setting} --region ${source_region} rds describe-db-snapshots --db-instance-identifier $temp_db --db-snapshot-identifier ${temp_db}-src |grep Status |grep available |wc -l`
  if [ $status = 1 ]; then
    echo "At `date`: Creating snapshot ${temp_db}-src is completed"
    break 
  fi 
  echo "At `date`: Creating snapshot ${temp_db}-src $progress"
  sleep 60
done

# Change temp RDS's security group id
security_group_id=`aws ${profile_setting} --region ${source_region} rds describe-db-instances --filters "Name=db-instance-id, Values=${source_db}" --query 'DBInstances[*].{VpcSecurityGroups: VpcSecurityGroups}'|grep "VpcSecurityGroupId" |cut -d':' -f2|cut -d',' -f1|cut -d'"' -f2`
echo "security_group_id=${security_group_id}"

aws ${profile_setting} --region ${source_region} rds modify-db-instance --db-instance-identifier ${temp_db} --vpc-security-group-ids ${security_group_id}

# Get RDS major version
#major_version=`aws ${profile_setting} --region ${source_region} rds describe-db-instances --db-instance-identifier ${source_db} --query 'DBInstances[0].EngineVersion' |cut -d'"' -f2 |cut -d'.' -f1-2`
major_version=`aws ${profile_setting} --region ${source_region} rds describe-db-instances --db-instance-identifier ${source_db} --query 'DBInstances[0].EngineVersion' |cut -d'"' -f2 |cut -d'.' -f1`
if [ "$major_version" == "19.0" ]; then
  major_version="19"
#  major_version=`echo $major_version |cut -d'.' -f1`
#  major_version=`echo "${major_version//./-}"`
fi

# Copy db snapshot to target region
source_account=`aws ${profile_setting} --region ${source_region} rds describe-db-snapshots --db-snapshot-identifier ${temp_db}-src --query "DBSnapshots[0].KmsKeyId" |cut -d":" -f4-5`
source_snapshot_arn="arn:aws:rds:${source_account}:snapshot:${temp_db}-src"
echo "source_snapshot_arn=$source_snapshot_arn"

echo "Copy db snapshot ${temp_db}-src to ${temp_db}-tgt starting at `date`"
aws ${profile_setting} --region ${target_region} rds copy-db-snapshot --source-db-snapshot-identifier $source_snapshot_arn --target-db-snapshot-identifier ${temp_db}-tgt --source-region ${source_region} --kms-key-id $kmskeyid --option-group-name oracle-ee-${major_version}-${app_env}-${db_type}-${target_vpc}

while true
do
  progress=`aws ${profile_setting} --region ${target_region} rds describe-db-snapshots --db-instance-identifier $temp_db --db-snapshot-identifier ${temp_db}-tgt |grep "PercentProgress"`
  status=`aws ${profile_setting} --region ${target_region} rds describe-db-snapshots --db-instance-identifier $temp_db --db-snapshot-identifier ${temp_db}-tgt |grep Status |grep available |wc -l`
  if [ $status = 1 ]; then
    echo "At `date`: Copying db snapshot ${temp_db}-src to ${temp_db}-tgt is completed"
    break 
  fi 
  echo "At `date`: Copying db snapshot ${temp_db}-src to ${temp_db}-tgt: $progress"
  sleep 60
done

# Create RDS from snapshot
echo "Create RDS $target_db from snapshot ${temp_db}-tgt starting at `date`"
aws ${profile_setting} --region ${target_region} rds restore-db-instance-from-db-snapshot --db-instance-identifier $target_db --db-snapshot-identifier ${temp_db}-tgt --db-name $target_db --db-subnet-group-name ${app_env}-${db_type}-${target_vpc} --option-group-name oracle-ee-${major_version}-${app_env}-${db_type}-${target_vpc} --db-instance-class ${db_instance_class} 

while true
do
  status=`aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db |grep DBInstanceStatus |egrep "available|incompatible-parameters" |wc -l`
  if [ $status = 1 ]; then
    echo "At `date`: Creating RDS ${target_db} is completed"
    break
  fi
  echo "At `date`: Creating RDS ${target_db} is in progress"
  sleep 60
done

# Change parameter group name and reboot first
echo "Modify RDS ${target_db}'s parameter group name starting at `date`"
app_env_b=`echo $app_env |cut -c1-4`

aws ${profile_setting} --region ${target_region} rds modify-db-instance --db-instance-identifier $target_db --db-parameter-group-name oracle-ee-${major_version}-psp-${app_env_b}-${db_type}-${target_vpc}

echo "Reboot target RDS starting at `date`"
echo "RDS status before rebooting"
aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db

aws ${profile_setting} --region ${target_region} rds reboot-db-instance --db-instance-identifier $target_db

while true
do
  status=`aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db |grep DBInstanceStatus |egrep "available|incompatible-parameters" |wc -l`
  if [ $status = 1 ]; then
    echo "At `date`: Rebooting RDS ${target_db} is completed"
    break
  fi
  echo "At `date`: Rebooting RDS ${target_db} is in progress"
  sleep 60
done

echo "RDS status after rebooting"
aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db

echo "Reboot target RDS again starting at `date`"
echo "RDS status before rebooting"
aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db

aws ${profile_setting} --region ${target_region} rds reboot-db-instance --db-instance-identifier $target_db

while true
do
  status=`aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db |grep DBInstanceStatus |egrep "available|incompatible-parameters" |wc -l`
  if [ $status = 1 ]; then
    echo "At `date`: Rebooting RDS ${target_db} is completed"
    break
  fi
  echo "At `date`: Rebooting RDS ${target_db} is in progress"
  sleep 60
done

echo "RDS status after rebooting"
aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db

echo "Reboot target RDS again starting at `date`"
echo "RDS status before rebooting"
aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db

aws ${profile_setting} --region ${target_region} rds reboot-db-instance --db-instance-identifier $target_db

while true
do
  status=`aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db |grep DBInstanceStatus |egrep "available|incompatible-parameters" |wc -l`
  if [ $status = 1 ]; then
    echo "At `date`: Rebooting RDS ${target_db} is completed"
    break
  fi
  echo "At `date`: Rebooting RDS ${target_db} is in progress"
  sleep 60
done

echo "RDS status after rebooting"
aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db

echo "Reboot target RDS again starting at `date`"
echo "RDS status before rebooting"
aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db

aws ${profile_setting} --region ${target_region} rds reboot-db-instance --db-instance-identifier $target_db

while true
do
  status=`aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db |grep DBInstanceStatus |egrep "available|incompatible-parameters" |wc -l`
  if [ $status = 1 ]; then
    echo "At `date`: Rebooting RDS ${target_db} is completed"
    break
  fi
  echo "At `date`: Rebooting RDS ${target_db} is in progress"
  sleep 60
done

echo "RDS status after rebooting"
aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db

echo "Reboot target RDS again starting at `date`"
echo "RDS status before rebooting"
aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db

aws ${profile_setting} --region ${target_region} rds reboot-db-instance --db-instance-identifier $target_db

while true
do
  status=`aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db |grep DBInstanceStatus |egrep "available|incompatible-parameters" |wc -l`
  if [ $status = 1 ]; then
    echo "At `date`: Rebooting RDS ${target_db} is completed"
    break
  fi
  echo "At `date`: Rebooting RDS ${target_db} is in progress"
  sleep 60
done

echo "RDS status after rebooting"
aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db

# Change security group id
echo "Modify RDS ${target_db}'s security group id and parameter group name starting at `date`"
app_env_b=`echo $app_env |cut -c1-4`
security_group_id=`aws ${profile_setting} --region ${target_region} ec2 describe-security-groups --filters "Name=group-name,Values=${app_env_b}-${db_type}-${target_vpc}" --query 'SecurityGroups[*].{GroupId: GroupId}' |grep "GroupId" |cut -d'"' -f4`
echo "security_group_id=$security_group_id"

aws ${profile_setting} --region ${target_region} rds modify-db-instance --db-instance-identifier $target_db --vpc-security-group-ids $security_group_id

# Change backup retention to 7 days
aws ${profile_setting} --region ${target_region} rds modify-db-instance --db-instance-identifier $target_db --backup-retention-period 7

# Create DNS for target db
echo "Create DNS for target db starting at `date`" 

private_host=`aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db --query "DBInstances[0].Endpoint" |grep Address |cut -d'"' -f4`

echo '{' > change-resource-record-sets.json
echo '  "Comment": "Setup CNAME for RDS",' >> change-resource-record-sets.json
echo '  "Changes": [' >> change-resource-record-sets.json
echo '    {' >> change-resource-record-sets.json
echo '      "Action": "UPSERT",' >> change-resource-record-sets.json
echo '      "ResourceRecordSet": {' >> change-resource-record-sets.json
echo "        \"Name\": \"${target_db}.${dnsname}\"," >> change-resource-record-sets.json
echo '        "Type": "CNAME",' >> change-resource-record-sets.json
echo '        "TTL": 300,' >> change-resource-record-sets.json
echo '        "ResourceRecords": [' >> change-resource-record-sets.json
echo '          {' >> change-resource-record-sets.json
echo "            \"Value\": \"${private_host}\"" >> change-resource-record-sets.json
echo '          }' >> change-resource-record-sets.json
echo '        ]' >> change-resource-record-sets.json
echo '      }' >> change-resource-record-sets.json
echo '    }' >> change-resource-record-sets.json
echo '  ]' >> change-resource-record-sets.json
echo '}' >> change-resource-record-sets.json

aws ${profile_setting} --region ${target_region} route53 change-resource-record-sets --hosted-zone-id $hostedzoneid --change-batch file://change-resource-record-sets.json
echo "Completed at `date`"

# Reboot RDS to take the change effect

echo "Reboot target RDS starting at `date`"
echo "RDS status before rebooting"
aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db

aws ${profile_setting} --region ${target_region} rds reboot-db-instance --db-instance-identifier $target_db

while true
do
  status=`aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db |grep DBInstanceStatus |grep available |wc -l`
  if [ $status = 1 ]; then
    echo "At `date`: Rebooting RDS ${target_db} is completed"
    break
  fi
  echo "At `date`: Rebooting RDS ${target_db} is in progress"
  sleep 60
done

echo "RDS status after rebooting"
aws ${profile_setting} --region ${target_region} rds describe-db-instances --db-instance-identifier $target_db

echo "At `date`: Completed"