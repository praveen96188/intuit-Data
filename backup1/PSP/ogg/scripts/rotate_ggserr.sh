#!/bin/bash
# Usage: rotate_ggserr_aws.sh aws or ihp> [db_name]
# Sample cron job entry:
#       */5 * * * * cd /u01/ogg/scripts; ./rotate_ggserr.sh aws ppsppp04 1>/dev/null 2>&1

the_dist="SBGOPSIDCDBA@intuit.com"

. ~/.bash_profile

site=$1
dbname=$2

if [ "$site" == "ihp" ]; then
  . /l/${dbname}
  export https_proxy=http://qy1prdproxy01.pprod.ie.intuit.net:80
  profile=" --profile sbg-psp-ppd"
elif [ "$site" == "aws" ]; then
  . /l/orcl
  if [ "${dbname}" == "" ]; then
    dbname=${RDS_SID}
  fi
  profile=""
else
  echo "Wrong site"
  exit 1
fi

shdir=`pwd`
logdir=${shdir}/log

mkdir -p $logdir


export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:us-west-2:152430470825:gg-sbg-psp-prf-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:us-west-2:893547637742:db-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
else
  echo "Wrong VPC_ENV"
  exit 1
fi

today=`date +%Y-%m-%d`

cd $GG_HOME

cp ggserr.log ggserr.${today}

cp /dev/null ggserr.log
if [ -s ${logdir}/error_save ]
then
        rm ${logdir}/error_save_${dbname}
fi

nfile=$(ls -ltr ggserr.* | wc -l)
if [ ${nfile} -lt 10 ]
then
       exit
fi

nfile=`expr $nfile - 10`

ls -ltr ggserr.* | awk '{print "rm",$9}' | head -${nfile} > ${logdir}/purge_ggserr_${dbname}.tmp
chmod u+x ${logdir}/purge_ggserr_${dbname}.tmp
${logdir}/purge_ggserr_${dbname}.tmp

