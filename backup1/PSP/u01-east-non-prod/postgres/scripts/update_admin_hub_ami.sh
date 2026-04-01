# Usage: ./update_gg_hub_ami.sh <Profile> <Region> <DBName> [<NewAMI>]
#        where <Profile> should be AWS profile like sbg-psp-prod
#              <Region> should be us-west-2 or us-east-2
#              <DBName> should be RDS name
#              <NewAMI> should be new ami. It is optional with the default as the latest ami

if [ $# -eq 3 -o $# -eq 4 ]; then
  profile=$1
  region=$2
  db_name=$3
  new_ami=$4
#  if [ "$new_ami" == "" ]; then
#    new_ami=`curl --silent "https://amiquery.intuit.com/amis?region=$region&tag=osVersion:rhel7.4&tag=status:available" | jq -r 'max_by(.tags.creationDate) | .id'`
#  fi
else
  echo "Usage: ./update_gg_hub_ami.sh <Profile> <Region> <DBName> [<NewAMI>]"
  echo "       where <Profile> should be AWS profile like sbg-psp-prod"
  echo "             <Region> should be us-west-2 or us-east-2"
  echo "             <DBName> should be RDS name"
  echo "             <NewAMI> should be new ami. It is optional with the default as the latest ami"
  exit 1
fi

#echo "profile="
echo "region=${region}"
echo "db_name=${db_name}"
#echo "new_ami=${new_ami}"
echo "Find the cloudformation stacks. Please wait"

stack_ids=`aws  --region ${region} cloudformation list-stacks --stack-status-filter CREATE_COMPLETE UPDATE_COMPLETE UPDATE_ROLLBACK_COMPLETE |grep "psp-${db_name}" |grep StackId |cut -d\" -f4`

for stack_id in $stack_ids
do
  echo "stack_id=$stack_id"

  # Get old AMI:
  old_ami=`aws  --region ${region} cloudformation describe-stacks --stack-name $stack_id --query 'Stacks[].Parameters[?ParameterKey==\`AmiId\`'].ParameterValue |grep ami |cut -d\" -f2`
  echo "old_ami=${old_ami}"

  if [ "$new_ami" == "" ]; then
#    os=`aws  --region ${region} ec2 describe-images --owner 508971454034 --image-id $old_ami --query 'Images[].Name' | grep bl-amzn2-2.0. |cut -d'-' -f2-3 |cut -d'.' -f1`
     os="bl-amzn2-2.0."
    new_ami=`aws  --region ${region} ec2 describe-images --owner 508971454034 --filters "Name=architecture, Values=x86_64" "Name=name, Values=*${os}*"  --query 'sort_by(Images, &CreationDate)[-1].[ImageId]' |grep ami |cut -d\" -f2`
    if [ -z "$new_ami" ]; then
      echo "Can not retrieve the new AMI!"
      exit 1
    fi
  else
    if [ `aws  --region ${region} ec2 describe-images --owner 508971454034 --image-id $new_ami --query 'Images[].Name' | grep rhel |wc -l` -eq 0 ]; then
      echo "New AMI $new_ami does not exist!"
      exit 1
    fi
  fi
  echo "new_ami=${new_ami}"

  if [ "$old_ami" == "$new_ami" ]; then
    echo "New AMI is the same as current AMI. No update is needed"
  else
    # Update AMI
    aws  --region ${region} cloudformation get-template --stack-name $stack_id --query 'TemplateBody' --output text > template_${db_name}.json
    aws  --region ${region} cloudformation describe-stacks --stack-name $stack_id --query 'Stacks[0].Parameters' > parameter_${db_name}.json

    sed -i -e "s/${old_ami}/${new_ami}/g" parameter_${db_name}.json

    aws  --region ${region} cloudformation update-stack --stack-name $stack_id --template-body  file://template_${db_name}.json --parameters  file://parameter_${db_name}.json   --capabilities  CAPABILITY_NAMED_IAM

    instance_name=`echo $stack_id |cut -d'/' -f2`
    instance_id=`aws  --region ${region} ec2 describe-instances --filters "Name=tag:Name,Values=${instance_name}" --query "Reservations[*].Instances[*].InstanceId" |grep "\"" |cut -d'"' -f2`
    echo "Terminate existing instance (instance_id=$instance_id)"
    aws  --region ${region} ec2 terminate-instances --instance-ids $instance_id
  fi
done

