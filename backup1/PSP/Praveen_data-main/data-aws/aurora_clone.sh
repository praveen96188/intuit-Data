#!/bin/bash


while getopts "e:c:" opt ; do
   case ${opt} in
     e) env=$OPTARG ;;
     c) sourceId=$OPTARG ;;
  esac
done


if [ "$env" != "" ] ; then
    export AWS_PROFILE=${env}
fi

clusterJson=$(mktemp /tmp/${sourceId}.XXXXXXX)

if ! aws rds describe-db-clusters --db-cluster-identifier ${sourceId} > ${clusterJson} ; then
   echo "ClusterId ${sourceId} does not exist"
   exit 1
else
   cloneId="${sourceId}-clone"
fi

clusterPG=$(jq -r '.DBClusters[].DBClusterParameterGroup' $clusterJson)
clusterSG=$(jq -r '.DBClusters[].DBSubnetGroup'  $clusterJson)
clusterSecGrp=( $(jq -r '.DBClusters[0].VpcSecurityGroups[].VpcSecurityGroupId'  $clusterJson) )

echo "Creating cluster ${cloneId}"
aws rds restore-db-cluster-to-point-in-time \
     --source-db-cluster-identifier "${sourceId}" \
     --db-cluster-identifier "${cloneId}" \
     --db-subnet-group-name "${clusterSG}" \
     --restore-type copy-on-write \
     --use-latest-restorable-time \
     --enable-cloudwatch-logs-exports "postgresql" \
     --deletion-protection \
     --db-cluster-parameter-group-name "${clusterPG}" \
     --vpc-security-group-ids ${clusterSecGrp[*]} >/dev/null

instances=( $(jq -r '.DBClusters[].DBClusterMembers[].DBInstanceIdentifier' $clusterJson) )

echo "Creating Instances"
for instance in ${instances[*]} ; do
   instanceJson=$(mktemp /tmp/${instance}.XXXXXX)
   if ! aws rds describe-db-instances --db-instance-identifier $instance > ${instanceJson} ; then
      exit 1
   fi
   instanceClass=$(jq -r '.DBInstances[0].DBInstanceClass' ${instanceJson} )
   instanceParam=$(jq -r '.DBInstances[0].DBParameterGroups[0].DBParameterGroupName' ${instanceJson} )
   aws rds create-db-instance \
        --db-instance-identifier "${instance}-clone" \
        --db-cluster-identifier "${cloneId}" \
        --db-instance-class  "${instanceClass}" \
        --db-parameter-group-name "${instanceParam}" \
        --engine aurora-postgresql > /dev/null
   rm -f ${instanceJson}
done
sleep 60
writerIns=$(aws rds describe-db-clusters --db-cluster-identifier ${cloneId} | jq -r '.DBClusters[].DBClusterMembers[] | select (.IsClusterWriter == true) | .DBInstanceIdentifier')

echo -ne "Waiting for the writer instance ${writerIns}."
while true ; do
    sts=$(aws rds describe-db-instances --db-instance-identifier ${writerIns} --query 'DBInstances[].DBInstanceStatus' --output text)
    if [ "$sts" == "available" ] ; then
       break
    fi
    echo -ne " ."
    sleep 60
done
echo -ne "Done\n"

IFS=$'\n'
for role in `jq -r '.DBClusters[0].AssociatedRoles[] | [ .RoleArn, .FeatureName ] | @tsv' $clusterJson | awk '{ print $1"@"$2}'` ; do
    roleArn=${role%@*}
    featureName=${role#*@}
    aws rds add-role-to-db-cluster \
        --db-cluster-identifier ${cloneId} \
        --role-arn ${roleArn} \
        --feature-name ${featureName}
done
unset IFS

rm -f $clusterJson
