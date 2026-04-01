# Usage: ./update_gg_hub_assetid.sh <Profile> <Region> <DBName> [<AssetID>]
#        where <Profile> should be AWS profile like sbg-qbo-prod. 'no' means no profile provided
#	       <Region> should be us-west-2 or us-east-2
#	       <DBName> should be RDS name
#              <AssetID> should be asset id

if [ $# -eq 3 -o $# -eq 4 ]; then
  profile=$1
  if [ "$profile" == "no" ]; then
    profile_setting=""
  else
    profile_setting="--profile ${profile}"
  fi
  region=$2
  db_name=$3
  assetid=$4
else
  echo "Usage: ./update_gg_hub_assetid.sh <Profile> <Region> <DBName> [<AssetID>]"
  echo "       where <Profile> should be AWS profile like sbg-qbo-prod. 'no' means no profile provided"
  echo "             <Region> should be us-west-2 or us-east-2"
  echo "             <DBName> should be RDS name"
  echo "             <AssetID> should be asset id"
  exit 1
fi

echo "profile=${profile}"
echo "region=${region}"
echo "db_name=${db_name}"
echo "Find the cloudformation stacks. Please wait"

stack_ids=`aws $profile_setting --region ${region} cloudformation list-stacks --stack-status-filter CREATE_COMPLETE UPDATE_COMPLETE UPDATE_ROLLBACK_COMPLETE |grep "goldengate-${db_name}" |grep StackId |cut -d\" -f4`

  echo "stack_ids=$stack_ids"
for stack_id in $stack_ids
do
  aws $profile_setting --region ${region} cloudformation get-template --stack-name $stack_id --query 'TemplateBody' > template_${db_name}.json
  aws $profile_setting --region ${region} cloudformation describe-stacks --stack-name $stack_id --query 'Stacks[0].Parameters' > parameter_${db_name}.json
  aws $profile_setting --region ${region} cloudformation update-stack --stack-name $stack_id --template-body file://template_${db_name}.json --parameters file://parameter_${db_name}.json --tags Key=intuit:asset_id,Value=$assetid
done

