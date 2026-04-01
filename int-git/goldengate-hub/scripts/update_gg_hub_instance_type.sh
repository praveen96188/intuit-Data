# Usage: ./update_gg_hub_ami.sh <Profile> <Region> <AppEnv> <DBName> <NewInstType>
#        where <Profile> should be AWS profile like sbg-qbo-prod 
#	       <Region> should be us-west-2 or us-east-2
#	       <AppEnv> should be qa, e2e, stg, sbx, prf or prod
#	       <DBName> should be RDS name
#              <NewInstType> should be new instance type like m4.xlarge or m4.2xlarge

if [ $# -eq 5 ]; then
  profile=$1
  region=$2
  app_env=$3
  db_name=$4
  new_instance_type=$5
else
  echo "Usage: ./update_gg_hub_ami.sh <Profile> <Region> <AppEnv> <DBName> <NewInstType>"
  echo "       where <Profile> should be AWS profile like sbg-qbo-prod"
  echo "             <Region> should be us-west-2 or us-east-2"
  echo "             <DBName> should be RDS name"
  echo "             <AppEnv> should be qa, e2e, stg, sbx, prf or prod"
  echo "             <NewInstType> should be new_instance_type like m4.xlarge or m4.2xlarge"
  exit 1
fi

echo "profile=${profile}"
echo "region=${region}"
echo "app_env=${app_env}"
echo "db_name=${db_name}"
echo "new_instance_type=${new_instance_type}"
echo "Find the cloudformation stacks. Please wait"

stack_ids=`aws --profile ${profile} --region ${region} cloudformation list-stacks --stack-status-filter CREATE_COMPLETE UPDATE_COMPLETE UPDATE_ROLLBACK_COMPLETE |grep "goldengate-${db_name}" |grep StackId |cut -d\" -f4`

for stack_id in $stack_ids
do 
  echo "stack_id=$stack_id"

  # Get old instance type
  old_instance_type=`aws --profile ${profile} --region ${region} cloudformation get-template --stack-name $stack_id --query "TemplateBody.Mappings.InstanceTypes.GoldenGate.${app_env}" |cut -d\" -f2`
echo "old_instance_type=$old_instance_type"

  if [ "$old_instance_type" == "$new_instance_type" ]; then
    echo "New instance type is the same as current instance type. No update is needed"
  else
    # Update instance type
    aws --profile ${profile} --region ${region} cloudformation get-template --stack-name $stack_id --query 'TemplateBody' > template_${db_name}.json
    aws --profile ${profile} --region ${region} cloudformation describe-stacks --stack-name $stack_id --query 'Stacks[0].Parameters' > parameter_${db_name}.json

    cp template_${db_name}.json template_${db_name}.json.old
    sed -i -e "s/\"${app_env}\": \"${old_instance_type}/\"${app_env}\": \"${new_instance_type}/g" template_${db_name}.json

    aws --profile ${profile} --region ${region} cloudformation update-stack --stack-name $stack_id --template-body file://template_${db_name}.json --parameters file://parameter_${db_name}.json

    instance_name=`echo $stack_id |cut -d'/' -f2`
    instance_id=`aws --profile ${profile} --region ${region} ec2 describe-instances --filters "Name=tag:Name,Values=${instance_name}" --query "Reservations[*].Instances[*].InstanceId" |grep "\"" |cut -d'"' -f2`
    echo "Terminate existing instance (instance_id=$instance_id)"
    aws --profile ${profile} --region ${region} ec2 terminate-instances --instance-ids $instance_id 
  fi
done

