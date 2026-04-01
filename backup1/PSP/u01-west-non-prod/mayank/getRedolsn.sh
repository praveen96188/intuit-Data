#!/bin/bash


while getopts "e:i:d:h:S:" opt ; do
   case ${opt} in
     e) env=$OPTARG ;;
     i) instance=$OPTARG ;;
   esac
done 

checkInstanceIsWriter(){
    cluster=$(aws rds describe-db-instances --db-instance-identifier $instance --query 'DBInstances[].DBClusterIdentifier' --output text)
    ## echo "$cluster"
    writerInstance=$(aws rds describe-db-clusters --db-cluster-identifier $cluster | jq -r '.DBClusters[].DBClusterMembers[] | select (.IsClusterWriter == true) | .DBInstanceIdentifier')
    ## echo "$writerInstance"
    cloneHost=$(aws rds describe-db-clusters --db-cluster-identifier $cluster --query 'DBClusters[0].Endpoint' --output text )
    if [ "$instance" == "$writerInstance" ] ; then 
       return 0
    fi
    return 1
}

getRedoLSN() {
    
    mkdir -p /tmp/pglogs
    tempDir=$(mktemp -d /tmp/pglogs/${instance}.XXXXX)

    recentLogs=$(aws rds describe-db-log-files --db-instance-identifier ${instance} | jq -r  '.DescribeDBLogFiles[] | [ .LogFileName , .LastWritten ] | @tsv' | sort -k1 -nr | head -4 | cut -f1)

    for log in ${recentLogs} ; do 
        logFile="${tempDir}/$(basename $log)"
        aws rds download-db-log-file-portion --db-instance-identifier ${instance} --log-file-name ${log} --starting-token 0 --output text > ${logFile}
    done

    ## grep 'redo done at' ${tempDir}/* | sed -re 's/.*redo done at //g'
    awk '/redo done at/{split($1,date,"-"); split($2,time,":"); dt=date[1]" "date[2]" "date[3]" "time[1]" "time[2]" "time[3]; print mktime(dt),$NF  }' ${tempDir}/*  | sort -k1 -n | head -1 | cut -f2 -d" "
}


if [ "$env" != "" ] ; then 
    export AWS_PROFILE="$env"
fi

if [ "$instance" == "" ] ; then 
    exit 2
fi  

if ! checkInstanceIsWriter ; then 
   echo "Instance is not Writer"
   exit 2
fi

if [ "$writerInstance" == "" ] ; then 
    echo "Writer Instance not found"
    exit 2
fi 

applyFromPos=$(getRedoLSN)

if [ "${applyFromPos}" == "" ] ; then
    echo "LSN position not found"
    exit 3
fi

echo "LSN: ${applyFromPos}" 
echo "LSN: ${applyFromPos}" > ${instance}.RedoLsn

