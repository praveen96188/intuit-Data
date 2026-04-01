#!/bin/bash
# Purpose: To generate DMS apply exception report 
# Usage:   ./appy_exceptions_report_o2p.sh <oracle db> <postgres db>

. /l/orcl

if [ $# -eq 3 ]; then
  dbname_oracle=$1
  dbname_postgres=$2
  cluster_postgres=$3
else
  echo "$0 <oracle db> <postgres db>"
  exit 1
fi

mkdir -p log

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:us-west-2:152430470825:db-sbg-psp-ppd-t"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:us-west-2:893547637742:db-sbg-psp-t"
#  topic_arn="arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-critical-prod-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

./run_postgres.sh $cluster_postgres $dbname_postgres apply_exceptions_report_o2p.sql "-v dbname=${dbname_postgres}" > /dev/null

report=log/apply_exceptions_report_${dbname_postgres}.txt

if [ `cat $report |wc -l` -gt 4 ]; then
  if [ `du $report |cut -f1` -lt 100 ]; then
    /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "ALERT: DMS Apply Exception Report for ${dbname_oracle}->${dbname_postgres}" --message "`cat $report`"
     cat $report
  else
    report_tmp=${report}.tmp
    pwd=`pwd`
    hostname=`hostname`
    echo "The detail report file $pwd/$report is too big to send. Please check it on $hostname" > $report_tmp
    /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "ALERT: DMS Apply Exception Report for ${dbname_oracle}->${dbname_postgres}" --message "`cat $report_tmp`"
    cat $report
  fi
else
  echo "No Error" >> $report
  /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "DMS Apply Exception Report for ${dbname_oracle}->${dbname_postgres}" --message "`cat $report`"
  cat $report
fi

