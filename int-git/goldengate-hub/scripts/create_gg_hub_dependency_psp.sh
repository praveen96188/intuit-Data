# Usage:./create_gg_hub_dependency.sh <VPCEnv> <VPC>
#       where <VPCEnv> should be ppd or prod
#             <VPC> should be vpc-1, vpc-2, etc

if [ $# -eq 2 ]; then 
  vpc_env=$1
  vpc=$2
else
  echo "Usage ./create_gg_hub_dependency.sh <VPCEnv> <VPC>"
  echo "      where <VPCEnv> should be ppd or prod"
  echo "            <VPC> should be vpc-1, vpc-2, etc"
  exit 1
fi

vpcid=`aws --profile sbg-psp-${vpc_env} ec2 describe-vpcs --filters "Name=tag:Name,Values=${vpc}" --query 'Vpcs[0].VpcId' |cut -d'"' -f2`

subnet1=`aws --profile sbg-psp-${vpc_env} ec2 describe-subnets --filter "Name=vpc-id,Values=$vpcid" "Name=tag:Name,Values=DataSubnetAz1" --query 'Subnets[0].SubnetId' |cut -d'"' -f2`
subnet2=`aws --profile sbg-psp-${vpc_env} ec2 describe-subnets --filter "Name=vpc-id,Values=$vpcid" "Name=tag:Name,Values=DataSubnetAz2" --query 'Subnets[0].SubnetId' |cut -d'"' -f2`
subnet3=`aws --profile sbg-psp-${vpc_env} ec2 describe-subnets --filter "Name=vpc-id,Values=$vpcid" "Name=tag:Name,Values=DataSubnetAz3" --query 'Subnets[0].SubnetId' |cut -d'"' -f2`


# Create security group for EFS
aws --profile sbg-psp-${vpc_env} ec2 create-security-group --group-name ${vpc_env}-goldengate-efs-${vpc} --description "${vpc_env}-goldengate-efs-${vpc} security group" --vpc-id ${vpcid} 
efs_group_id=`aws --profile sbg-psp-${vpc_env} ec2 describe-security-groups --filters "Name=group-name,Values=${vpc_env}-goldengate-efs-${vpc}" --query 'SecurityGroups[*].{GroupId: GroupId}' |grep "GroupId" |cut -d'"' -f4`
echo "Security Group ID: $efs_group_id"
aws --profile sbg-psp-${vpc_env} ec2 create-tags --resources $efs_group_id --tags Key=Name,Value=${vpc_env}-goldengate-efs-${vpc}

efs_subnet_name_prefixes="replicationSubnetAz"
for efs_subnet_name_prefix in `echo $efs_subnet_name_prefixes`; do
  for az in 1 2 3; do
    client_subnet=`aws --profile sbg-psp-${vpc_env} ec2 describe-subnets --filter "Name=vpc-id,Values=${vpcid}" "Name=tag:Name,Values=${efs_subnet_name_prefix}${az}" --query 'Subnets[0].CidrBlock' |cut -d\" -f2`
    echo "client_subnet=${client_subnet}"
    aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $efs_group_id --protocol tcp --port 2049 --cidr ${client_subnet}
  done
done

# Create security group for Goldengate Hub EC2 
aws --profile sbg-psp-${vpc_env} ec2 create-security-group --group-name ${vpc_env}-goldengate-${vpc} --description "${vpc_env}-goldengate-${vpc} security group" --vpc-id ${vpcid} 
gg_group_id=`aws --profile sbg-psp-${vpc_env} ec2 describe-security-groups --filters "Name=group-name,Values=${vpc_env}-goldengate-${vpc}" --query 'SecurityGroups[*].{GroupId: GroupId}' |grep "GroupId" |cut -d'"' -f4`
echo "Security Group ID: $gg_group_id"
aws --profile sbg-psp-${vpc_env} ec2 create-tags --resources $gg_group_id --tags Key=Name,Value=${vpc_env}-goldengate-${vpc}

gg_subnet_name_prefixes="replicationSubnetAz DataSubnetAz"
for gg_subnet_name_prefix in `echo $gg_subnet_name_prefixes`; do
  for az in 1 2 3; do
    client_subnet=`aws --profile sbg-psp-${vpc_env} ec2 describe-subnets --filter "Name=vpc-id,Values=${vpcid}" "Name=tag:Name,Values=${gg_subnet_name_prefix}${az}" --query 'Subnets[0].CidrBlock' |cut -d\" -f2`
    echo "client_subnet=${client_subnet}"
    aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $gg_group_id --protocol tcp --port 1521 --cidr ${client_subnet}
  done
done

gg_subnet_name_prefixes2="IngressSubnetAz"
for gg_subnet_name_prefix in `echo $gg_subnet_name_prefixes2`; do
  for az in 1 2 3; do
    client_subnet=`aws --profile sbg-psp-${vpc_env} ec2 describe-subnets --filter "Name=vpc-id,Values=${vpcid}" "Name=tag:Name,Values=${gg_subnet_name_prefix}${az}" --query 'Subnets[0].CidrBlock' |cut -d\" -f2`
    echo "client_subnet=${client_subnet}"
    aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $gg_group_id --protocol tcp --port 22 --cidr ${client_subnet}
  done
done

aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $gg_group_id --protocol tcp --port 1521 --cidr 10.143.0.0/20
aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $gg_group_id --protocol tcp --port 15000-15030 --cidr 10.143.0.0/20
aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $gg_group_id --protocol tcp --port 16000-16030 --cidr 10.143.0.0/20
aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $gg_group_id --protocol tcp --port 17000-17030 --cidr 10.143.0.0/20
aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $gg_group_id --protocol tcp --port 18000-18030 --cidr 10.143.0.0/20
aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $gg_group_id --protocol tcp --port 1521 --cidr 10.161.0.0/20

aws --profile sbg-psp-${vpc_env} ec2 authorize-security-group-ingress --group-id $gg_group_id --protocol tcp --port 2049 --source-group $efs_group_id
