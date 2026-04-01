# Usage: ./create_gg_hub_ha.sh <AppName> <AppEnv> <DBName> <Region> <VPC> <HA> [<VolumeSize>]
#        where <AppName> should be qbo, payments, etc
#              <AppEnv> should be qa, e2e, stg, sbx, prf or prod
#              <Region> should be us-west-2, us-east-2, etc
#              <VPC> should be vpc-1, vpc-2, etc or Intuit-vpc-1, Intuit-vpc-2, etc
#	       <HA> should be yes or no
#              [<VolumeSize>] is optional. It is EBS volume /u01 size in GB and default is 1000 GB


if [ $# -eq 6 -o $# -eq 7 ]; then
  app_name=$1
  app_env=$2
  db_name=$3
  region=$4
  vpc=$5
  ha=$6
  vsize=$7
  if [ "$vsize" == "" ]; then
    vsize=1000
  fi
else
  echo "Usage: ./create_gg_hub_ha.sh <AppName> <AppEnv> <DBName> <Region> <VPC> <HA> [<VolumeSize>]"
  echo "       where <AppName> should be qbo, payments, etc"
  echo "             <AppEnv> should be qa, e2e, stg, sbx, prf or prod"
  echo "             <Region> should be us-west-2, us-east-2, etc"
  echo "             <HA> should be yes or no"
  echo "             <VPC> should be vpc-1, vpc-2, etc or Intuit-vpc-1, Intuit-vpc-2, etc"
  echo "             [<VolumeSize>] is optional. It is EBS volume /u01 size in GB and default is 1000 GB"
  exit 1
fi

if [ ! -f gg_hub_${app_name}.config ]; then
  echo "The configuration file gg_hub_${app_name}.config does not exit."
  exit 1
else
  config_file=gg_hub_${app_name}_${app_env}.config
  rm -f $config_file
  touch $config_file
  inline="false"
  cat gg_hub_${app_name}.config |while read line
  do
    if [`echo "$line" |grep "^\[" |wc -l` -gt 0 ]; then
      if [`echo "$line" |grep "\"${app_env}\"" |wc -l` -gt 0 ]; then
        inline="true"
      elif [ "$inline" == "true" ]; then
        break
      fi
    else
      if [ "$inline" == "true" -a "$line" != "" ]; then
        echo $line >> $config_file
      fi
    fi
  done
fi

. $config_file
echo "ami_us_west_2=$ami_us_west_2"
echo "ami_us_east_2=$ami_us_east_2"
echo "vpc_env=$vpc_env"
echo "profile=$profile"
echo "goldengate_iam=$goldengate_iam"
echo "notification_service_arn=$notification_service_arn"
echo "hosted_zone_id=$hosted_zone_id"
echo "domain_name=$domain_name"
echo "idps_endpoint=$idps_endpoint"
echo "idps_policy_id_us_west_2=$idps_policy_id_us_west_2"
echo "idps_policy_id_us_east_2=$idps_policy_id_us_east_2"

echo "region=$region"
region_u=`echo $region | tr -s '-' '_'`
echo region_u=$region_u
ami=`grep ami_${region_u} $config_file |cut -d'"' -f2`
if [ "$ami" == "" ]; then
  echo "ami for the region ${region} is not defined in the configure file gg_hub_${app_name}.config"
  exit 1
fi
echo "ami=$ami"

idps_policy_id=`grep idps_policy_id_${region_u} $config_file |cut -d'"' -f2`
echo "idps_policy_id=$idps_policy_id"

vpc_id=`aws --profile ${profile} --region ${region} ec2 describe-vpcs --filters "Name=tag:Name,Values=${vpc}" --query 'Vpcs[0].VpcId' |cut -d'"' -f2`

echo "vpc_id=$vpc_id"
if [ "$vpc_id" == "null" ]; then
  echo "VPC ${vpc} does not exit"
  exit 1
fi

# Create volumes
if [ "$ha" == "yes" ]; then
  volume_id1=`aws --profile ${profile} --region ${region} ec2 describe-volumes --filters "Name=tag:Name,Values=gg-${db_name}-a" --query 'Volumes[*].{VolumeId: VolumeId}' |grep "VolumeId" |cut -d'"' -f4`
  if [ "$volume_id1" == "" ]; then
    echo "Create the volume gg-${db_name}-a"
    aws --profile ${profile} --region ${region} ec2 create-volume --size $vsize --volume-type gp2 --availability-zone ${region}a --encrypted |tee volume_output_${app_env}_${db_name}_a.log
    volume_id1=`grep VolumeId volume_output_${app_env}_${db_name}_a.log |cut -d\" -f4`
    aws --profile ${profile} --region ${region} ec2 create-tags --resources $volume_id1 --tags Key=Name,Value=gg-${db_name}-a
  else
    echo "The volume gg-${db_name}-a was created before. Reuse it"
  fi

  volume_id2=`aws --profile ${profile} --region ${region} ec2 describe-volumes --filters "Name=tag:Name,Values=gg-${db_name}-b" --query 'Volumes[*].{VolumeId: VolumeId}' |grep "VolumeId" |cut -d'"' -f4`
  if [ "$volume_id2" == "" ]; then
    echo "Create the volume gg-${db_name}-b"
    aws --profile ${profile} --region ${region} ec2 create-volume --size $vsize --volume-type gp2 --availability-zone ${region}b --encrypted |tee volume_output_${app_env}_${db_name}_b.log
    volume_id2=`grep VolumeId volume_output_${app_env}_${db_name}_b.log |cut -d\" -f4`
    aws --profile ${profile} --region ${region} ec2 create-tags --resources $volume_id2 --tags Key=Name,Value=gg-${db_name}-b
  else
    echo "The volume gg-${db_name}-b was created before. Reuse it"
  fi

elif [ "$ha" == "no" ]; then
  volume_id1=`aws --profile $profile --region ${region} ec2 describe-volumes --filters "Name=tag:Name,Values=gg-${db_name}" --query 'Volumes[*].{VolumeId: VolumeId}' |grep "VolumeId" |cut -d'"' -f4`
  if [ "$volume_id1" == "" ]; then
    echo "Create the volume gg-${db_name}"
    aws --profile $profile --region ${region} ec2 create-volume --size $vsize --volume-type gp2 --availability-zone ${region}a --encrypted |tee volume_output_${app_env}_${db_name}.log
    volume_id1=`grep VolumeId volume_output_${app_env}_${db_name}.log |cut -d\" -f4`
    aws --profile $profile --region ${region} ec2 create-tags --resources $volume_id1 --tags Key=Name,Value=gg-${db_name}
  else
    echo "The volume gg-${db_name} was created before. Reuse it"
  fi

else
  echo "<HA> must be yes or no"
  exit 1
fi

# Get subnets, security group for goldengate EC2
subnet_az1=`aws --profile ${profile} --region ${region} ec2 describe-subnets --filters "Name=tag:Name,Values=ReplicationSubnetAz1" "Name=vpc-id,Values=$vpc_id" --query 'Subnets[*].{SubnetId: SubnetId}' |grep "SubnetId" |cut -d'"' -f4`
echo "subnet_az1=$subnet_az1"

subnet_az2=`aws --profile ${profile} --region ${region} ec2 describe-subnets --filters "Name=tag:Name,Values=ReplicationSubnetAz2" "Name=vpc-id,Values=$vpc_id" --query 'Subnets[*].{SubnetId: SubnetId}' |grep "SubnetId" |cut -d'"' -f4`
echo "subnet_az2=$subnet_az2"

subnet_az3=`aws --profile ${profile} --region ${region} ec2 describe-subnets --filters "Name=tag:Name,Values=ReplicationSubnetAz3" "Name=vpc-id,Values=$vpc_id" --query 'Subnets[*].{SubnetId: SubnetId}' |grep "SubnetId" |cut -d'"' -f4`
  echo "subnet_az3=$subnet_az3"

vpc_b=`echo $vpc |rev | cut -d"-" -f-2 |rev`
gg_security_group=`aws --profile ${profile} --region ${region} ec2 describe-security-groups --filters "Name=group-name,Values=${vpc_env}-goldengate-${vpc_b}" --query 'SecurityGroups[*].{GroupId: GroupId}' |grep "GroupId" |cut -d'"' -f4`
echo "gg_security_group=$gg_security_group"

# Create elastic network interface for two AZs
if [ "$ha" == "yes" ]; then
  for az in a b
  do
    eni_id=`aws --profile ${profile} --region ${region} ec2 describe-network-interfaces --filters "Name=tag:Name,Values=gg${db_name}-${az}" --query 'NetworkInterfaces[0].NetworkInterfaceId' |cut -d'"' -f2`
    if [ "$eni_id" == "null" ]; then
      echo "Create the elastic network interface gg${db_name}-${az}"
      if [ "${az}" == "a" ]; then
        aws --profile ${profile} --region ${region} ec2 create-network-interface --subnet-id ${subnet_az1} --groups ${gg_security_group} |tee eni_output_${app_env}_${db_name}_${az}.log
      else
        aws --profile ${profile} --region ${region} ec2 create-network-interface --subnet-id ${subnet_az2} --groups ${gg_security_group} |tee eni_output_${app_env}_${db_name}_${az}.log
      fi

      eni_id=`cat eni_output_${app_env}_${db_name}_${az}.log |grep NetworkInterfaceId |cut -d'"' -f4`
      eni_address=`cat eni_output_${app_env}_${db_name}_${az}.log |grep PrivateIpAddress |grep -v "\[" |head -1 |cut -d'"' -f4`
      echo "eni_id=$eni_id"
      echo "eni_address=$eni_address"
      aws --profile ${profile} --region ${region} ec2 create-tags --resources $eni_id --tags Key=Name,Value=gg${db_name}-${az}
    else
      echo "The elastic network interface gg${db_name}-${az} was created before. Reuse it"
    fi
  done
fi

# Build CF parameter file cf_gg_hub_parameter_${app_env}_${db_name}_${az}.json
if [ "$ha" == "yes" ]; then
  for az in a b
  do
    pfile=cf_gg_hub_parameter_${app_env}_${db_name}_${az}.json
    echo '[' > $pfile
    echo "  { \"ParameterKey\": \"AppName\", \"ParameterValue\": \"${app_name}\" }," >> $pfile
    echo "  { \"ParameterKey\": \"AppEnv\", \"ParameterValue\": \"${app_env}\" }," >> $pfile
    echo "  { \"ParameterKey\": \"VpcEnv\", \"ParameterValue\": \"${vpc_env}\" }," >> $pfile
    echo "  { \"ParameterKey\": \"AmiId\", \"ParameterValue\": \"${ami}\" }," >> $pfile
    echo "  { \"ParameterKey\": \"Locale\", \"ParameterValue\": \"USPacific\" }," >> $pfile
    echo "  { \"ParameterKey\": \"KeyName\", \"ParameterValue\": \"sbg-${app_name}-${vpc_env}-ops-${region}\" }," >> $pfile
    if [ "${az}" == "a" ]; then
      echo "  { \"ParameterKey\": \"GoldenGateVolumeId\", \"ParameterValue\": \"${volume_id1}\" }," >> $pfile
      echo "  { \"ParameterKey\": \"GoldenGateSubnetIds\", \"ParameterValue\": \"${subnet_az1}\" }," >> $pfile
      echo "  { \"ParameterKey\": \"Hostname\", \"ParameterValue\": \"gg${db_name}.${domain_name}\" }," >> $pfile
    else
      echo "  { \"ParameterKey\": \"GoldenGateVolumeId\", \"ParameterValue\": \"${volume_id2}\" }," >> $pfile
      echo "  { \"ParameterKey\": \"GoldenGateSubnetIds\", \"ParameterValue\": \"${subnet_az2}\" }," >> $pfile
      echo "  { \"ParameterKey\": \"Hostname\", \"ParameterValue\": \"gg${db_name}-ha.${domain_name}\" }," >> $pfile
    fi
    echo "  { \"ParameterKey\": \"GoldenGateSecGrpIds\", \"ParameterValue\": \"${gg_security_group}\" }," >> $pfile
    echo "  { \"ParameterKey\": \"HostedZoneId\", \"ParameterValue\": \"${hosted_zone_id}\" }," >> $pfile
    echo "  { \"ParameterKey\": \"ChefRole\", \"ParameterValue\": \"GoldenGate\" }," >> $pfile
    echo "  { \"ParameterKey\": \"GoldenGateIamInstanceProfile\", \"ParameterValue\": \"${goldengate_iam}\" }," >> $pfile
    echo "  { \"ParameterKey\": \"NotificationServiceArn\", \"ParameterValue\": \"${notification_service_arn}\" }," >> $pfile
    echo "  { \"ParameterKey\": \"HA\", \"ParameterValue\": \"${ha}\" }," >> $pfile
    echo "  { \"ParameterKey\": \"IdpsEndpoint\", \"ParameterValue\": \"${idps_endpoint}\" }," >> $pfile
    echo "  { \"ParameterKey\": \"IdpsPolicyId\", \"ParameterValue\": \"${idps_policy_id}\" }," >> $pfile
    echo "  { \"ParameterKey\": \"RdsInstanceName\", \"ParameterValue\": \"${db_name}\" }" >> $pfile
    echo ']' >> $pfile
  done
else
  az="a"
  pfile=cf_gg_hub_parameter_${app_env}_${db_name}_${az}.json
  echo '[' > $pfile
  echo "  { \"ParameterKey\": \"AppName\", \"ParameterValue\": \"${app_name}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"AppEnv\", \"ParameterValue\": \"${app_env}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"VpcEnv\", \"ParameterValue\": \"${vpc_env}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"AmiId\", \"ParameterValue\": \"${ami}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"Locale\", \"ParameterValue\": \"USPacific\" }," >> $pfile
  echo "  { \"ParameterKey\": \"KeyName\", \"ParameterValue\": \"sbg-${app_name}-${vpc_env}-ops-${region}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"GoldenGateVolumeId\", \"ParameterValue\": \"${volume_id1}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"GoldenGateSubnetIds\", \"ParameterValue\": \"${subnet_az1}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"Hostname\", \"ParameterValue\": \"gg${db_name}.${domain_name}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"GoldenGateSecGrpIds\", \"ParameterValue\": \"${gg_security_group}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"HostedZoneId\", \"ParameterValue\": \"${hosted_zone_id}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"ChefRole\", \"ParameterValue\": \"GoldenGate\" }," >> $pfile
  echo "  { \"ParameterKey\": \"GoldenGateIamInstanceProfile\", \"ParameterValue\": \"${goldengate_iam}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"NotificationServiceArn\", \"ParameterValue\": \"${notification_service_arn}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"HA\", \"ParameterValue\": \"${ha}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"IdpsEndpoint\", \"ParameterValue\": \"${idps_endpoint}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"IdpsPolicyId\", \"ParameterValue\": \"${idps_policy_id}\" }," >> $pfile
  echo "  { \"ParameterKey\": \"RdsInstanceName\", \"ParameterValue\": \"${db_name}\" }" >> $pfile
  echo ']' >> $pfile
fi

# Create GG Hub
if [ "$ha" == "yes" ]; then
  for az in a b
  do
    aws --profile ${profile} --region ${region} cloudformation create-stack --stack-name goldengate-${db_name}-${az}-`date +\%Y\%m\%d-\%H\%M\%S` --template-body file://goldengate-hub-ha.json --parameters file://cf_gg_hub_parameter_${app_env}_${db_name}_${az}.json --capabilities CAPABILITY_IAM --disable-rollback
  done
else
  az="a"
  aws --profile ${profile} --region ${region} cloudformation create-stack --stack-name goldengate-${db_name}-`date +\%Y\%m\%d-\%H\%M\%S` --template-body file://goldengate-hub-ha.json --parameters file://cf_gg_hub_parameter_${app_env}_${db_name}_${az}.json --capabilities CAPABILITY_IAM --disable-rollback
fi
