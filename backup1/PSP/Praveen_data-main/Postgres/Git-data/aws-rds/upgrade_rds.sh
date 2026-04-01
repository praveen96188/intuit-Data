# Usage: ./upgrade_rds.sh <Profile> <Region> <DBName> <NewVersion>
#        where <Profile> is AWS account profile. 'no' means no profile provided
#              <Region> is AWS region
#              <DBName> is RDS name
#              <NewVersion> is the RDS version (like 19.0.0.0.ru-2020-01.rur-2020-01.r1) you want to upgrade to
#        for example
#        nohup ./upgrade_rds.sh no us-west-2 eqboc99 19.0.0.0.ru-2020-01.rur-2020-01.r1 > upgrade_rds_eqboc99.log &

if [ $# -eq 4 ]; then
  profile=$1
  if [ "$profile" == "no" ]; then
    profile_setting=""
  else
    profile_setting="--profile ${profile}"
  fi
  region=$2
  db_name=$3
  new_version=$4
else
  echo "$0 <profile> <region> <DBName> <NewVersion>"
  echo "where <Profile> is AWS account profile. 'no' means no profile provided"
  echo "      <Region> is AWS region"
  echo "      <DBName> is RDS name"
  echo "      <NewVersion> is the RDS version (like 19.0.0.0.ru-2020-01.rur-2020-01.r1) you want to upgrade to"
  exit 1
fi

export AWS_PAGER=""

echo "At `date`: Start upgrading RDS"

db_instance_status=`aws $profile_setting --region $region rds describe-db-instances --db-instance-identifier $db_name --query 'DBInstances[0].DBInstanceStatus' |cut -d'"' -f2`
echo "db_instance_status=$db_instance_status"

if [ "$db_instance_status" == "available" ]; then
  new_major_version=`echo $new_version|cut -d'.' -f1`

  typeset -i cnt
  cnt=0
  while true
  do
    group_names=`aws $profile_setting --region $region rds describe-db-instances --db-instance-identifier $db_name --query 'DBInstances[0].{DBParameterGroupName: DBParameterGroups[0].DBParameterGroupName, DBSubnetGroup: OptionGroupMemberships[0].OptionGroupName}' --output text`
    current_parameter_group_name=`echo $group_names |cut -d' ' -f1`
    current_option_group_name=`echo $group_names |cut -d' ' -f2`
    echo "current_parameter_group_name=$current_parameter_group_name"
    echo "current_option_group_name=$current_option_group_name"

    if [ "$current_parameter_group_name" == "" -o "$current_option_group_name" == "" ]; then
      echo "Did not get current DBParameterGroupName or OptionGroupName. Try again"
      break
    fi

    current_major_version1=`echo $current_parameter_group_name |cut -d'-' -f3`
    if [ "$current_major_version1" == "12" ]; then
      current_major_version=`echo $current_parameter_group_name |cut -d'-' -f3,4`
    else
      current_major_version=$current_major_version1
    fi

    if [ "$current_major_version" == "$new_major_version" ]; then
      echo "RDS is already on the new version $new_major_version. No upgrade is needed"
      break

    else
      new_parameter_group_name=`echo $current_parameter_group_name |sed "s/$current_major_version/$new_major_version/"`
      echo "new_parameter_group_name=$new_parameter_group_name"
      new_option_group_name=`echo $current_option_group_name |sed "s/$current_major_version/$new_major_version/"`
      echo "new_option_group_name=$new_option_group_name"

      aws $profile_setting --region $region rds modify-db-instance --db-instance-identifier $db_name --engine-version $new_version --db-parameter-group-name $new_parameter_group_name --option-group-name $new_option_group_name --allow-major-version-upgrade --apply-immediately 1> upgrade_rds_${db_name}.tmp 2>&1 
      if [ `grep "Rate exceeded" upgrade_rds_${db_name}.tmp |wc -l ` -eq 0 ]; then
         cat upgrade_rds_${db_name}.tmp
         echo "At `date`: Upgrading RDS command has been run. Please wait"
         sleep 60
         db_instance_status=`aws $profile_setting --region $region rds describe-db-instances --db-instance-identifier $db_name --query 'DBInstances[0].DBInstanceStatus' |cut -d'"' -f2`
         echo "db_instance_status=$db_instance_status"
         break
      fi
      cnt=${cnt}+1
      if [ $cnt -gt 10 ]; then
        echo "ALERT: Have tried to upgrade RDS with the AWS CLI command ${cnt} times with the error in the file upgrade_rds_${db_name}.tmp. Try again later."
      fi
      sleep 5
    fi
  done
fi

if [ "$db_instance_status" == "upgrading" -o "$db_instance_status" == "modifying" ]; then
  while true
  do
    echo "At `date`: Upgrading RDS is in progress. Please wait"
    sleep 30
    db_instance_status=`aws $profile_setting --region $region rds describe-db-instances --db-instance-identifier $db_name --query 'DBInstances[0].DBInstanceStatus' |cut -d'"' -f2`
    if [ "$db_instance_status" == "available" ]; then
      echo "At `date`: Upgrading RDS completed"
      break
    fi
  done
else 
  echo "At `date`: RDS has unknown status $db_instance_status. Please check and try again"
  exit
fi

parameter_apply_status=`aws $profile_setting --region $region rds describe-db-instances --db-instance-identifier $db_name --query 'DBInstances[0].DBParameterGroups[0].ParameterApplyStatus' |cut -d'"' -f2`
if [ "$parameter_apply_status" == "pending-reboot" ]; then
  aws $profile_setting --region $region rds reboot-db-instance --db-instance-identifier $db_name
  while true
  do
    echo "At `date`: Rebooting RDS is in progress. Please wait"
    sleep 30
    db_instance_status=`aws $profile_setting --region $region rds describe-db-instances --db-instance-identifier $db_name --query 'DBInstances[0].DBInstanceStatus' |cut -d'"' -f2`
    if [ "$db_instance_status" == "available" ]; then
      echo "At `date`: Rebooting RDS completed"
      break
    fi
  done
fi
