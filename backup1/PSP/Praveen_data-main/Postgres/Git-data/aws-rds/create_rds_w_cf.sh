# Usage: ./create_rds_w_cf.sh  <AppEnv> <DBType> <DBName> <VPC>
#        where <AppEnv> should be qa, e2e, e2es, stg, sbx, prf, prod, prods, prodm, prodx or prodn
#              <DBType> should be clusterdb or reportdb
#              e2es means small-size e2e 
#              prods means small-size prod
#              prodm means median-size prod
#              prodx means large-size prod
#              prodn means new prod (with less half of regular prod storage)
#              <VPC> should be vpc-1, vpc-2, etc


if [ $# -eq 4 ]; then
  app_env=$1
  db_type=$2
  db_name=$3
  vpc=$4 
else
  echo "Usage ./create_rds_w_cf.sh <AppEnv> <DBType> <DBName> <VPC>"
  echo "      where <AppEnv> should be qa, e2e, e2es, stg, sbx, prf, prod, prods, prodm, prodx or prodn"
  echo "            e2es means small-size e2e"
  echo "            prods means small-size prod"
  echo "            prodm means median-size prod"
  echo "            prodx means large-size prod"
  echo "            prodn means new prod (with less half of regular prod storage)"
  echo "            <DBType> should be clusterdb or reportdb"
  echo "            <VPC> should be vpc-1, vpc-2, etc"
  exit 1
fi

case "$app_env" in
  e2e|e2es|prf|qa)
      if [ $app_env == "qa" ]; then
        tag_env="qal"
      elif [ $app_env == "e2es" ]; then
        tag_env="e2e"
      else
        tag_env=$app_env
      fi
      monitoring_role=""
      hostedzoneid="ZPMOD8K08QNEL"
      dnsname="sbg-psp-ppd.a.intuit.com"
      vpc_env="ppd"
      profile="sbg-psp-ppd"
      app_env_b=`echo $app_env |cut -c1-3`
      ;;
  prod|prods|prodm|prodx|prodn|sbx|stg)
      if [ $app_env == "sbx" ]; then
        tag_env="sbx"
      elif [ $app_env == "stg" ]; then
        tag_env="stg"
      else
        tag_env="prd"
      fi
      monitoring_role=""
      hostedzoneid=" Z35GYKEF3QQE9L"
      dnsname="sbg-psp-prod.a.intuit.com"
      vpc_env="prod"
      profile="sbg-psp-prod"
      app_env_b=`echo $app_env |cut -c1-4`
      ;;
    *)
      echo "<AppEnv> not recognized"
      exit 1
      ;;
esac

# Get security group id
security_group_id=`aws --profile ${profile} ec2 describe-security-groups --filters "Name=group-name,Values=${app_env_b}-${db_type}-${vpc}" --query 'SecurityGroups[*].{GroupId: GroupId}' |grep "GroupId" |cut -d'"' -f4`

if [ "${security_group_id}" == "" ]; then
  echo "ERROR: Could not find security group id for AppEnv ${app_env}, DBType ${db_type}, VPC ${vpc}. It needs to be created first"
  exit 1
fi

# Get root id
root_id=`aws --profile ${profile} iam get-group --group-name "Admins" --query 'Group.Arn' |cut -d: -f5`

query="Aliases[?AliasName==\`alias/rds-${db_name}\`].TargetKeyId"
kmskeyid=`aws --profile ${profile} kms list-aliases --query "$query" |grep '"' |cut -d'"' -f2`

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
  kmskeyid=`aws --profile ${profile} kms create-key --description "Used for RDS ${db_name}" --policy file://kms_policy_${vpc_env}.json --query "KeyMetadata.KeyId" |cut -d'"' -f2`

  echo kmskeyid=$kmskeyid

  aws --profile ${profile} kms create-alias --alias-name alias/rds-${db_name} --target-key-id $kmskeyid
  aws --profile ${profile} kms enable-key-rotation --key-id $kmskeyid
fi

# Build CF rds parameter file cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo '[' > cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"DBName\", \"ParameterValue\": \"${db_name}\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"DnsName\", \"ParameterValue\": \"${db_name}.${dnsname}\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"HostedZoneId\", \"ParameterValue\": \"${hostedzoneid}\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"KmsKeyId\", \"ParameterValue\": \"${kmskeyid}\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"Port\", \"ParameterValue\": \"1521\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"PreferredBackupWindow\", \"ParameterValue\": \"05:00-05:30\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"PreferredMaintenanceWindow\", \"ParameterValue\": \"Mon:06:00-Mon:06:30\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"EngineVersion\", \"ParameterValue\": \"12.1.0.2.v14\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"SecurityGroup\", \"ParameterValue\": \"$security_group_id\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"AppEnv\", \"ParameterValue\": \"${app_env}\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"DBType\", \"ParameterValue\": \"${db_type}\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"DBParameterGroupName\", \"ParameterValue\": \"oracle-ee-12-1-psp-${app_env_b}-${db_type}-${vpc}\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"DBSubnetGroupName\", \"ParameterValue\": \"${app_env_b}-${db_type}-${vpc}\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"OptionGroupName\", \"ParameterValue\": \"oracle-ee-12-1-${app_env_b}-${db_type}-${vpc}\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
echo "  { \"ParameterKey\": \"MonitoringRoleArn\", \"ParameterValue\": \"${monitoring_role}\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
if [[ ! -z $cluster ]]; then
  echo "  { \"ParameterKey\": \"DBTagCluster\", \"ParameterValue\": \"${cluster}\" }," >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json
fi
echo "  { \"ParameterKey\": \"DBTagEnv\", \"ParameterValue\": \"${tag_env}\" }" >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json

echo ']' >> cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json

aws --profile ${profile} cloudformation create-stack --stack-name rds-${db_name}-`date +\%Y\%m\%dT\%H\%M\%S` \
    --template-body file://cf_rds_template.json \
    --parameters file://cf_rds_parameter_${app_env}_${db_type}-${db_name}-${vpc}.json \
    --capabilities CAPABILITY_IAM
