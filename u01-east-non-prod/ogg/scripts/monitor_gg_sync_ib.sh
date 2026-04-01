# Monitor database replication lag
# Usage: monitor_gg_sync_aws.sh <lag_warning_threshold_in_minutes> <aws or ihp> [db_name]
# Sample cron job entry:
#       */5 * * * * cd /u01/ogg/scripts; ./monitor_gg_sync.sh 10 aws pqbopp04 1>/dev/null 2>&1
set -x
. ~/.bash_profile

lag_warning=$1
site=$2
dbname=$3

. /l/orcl

the_dist="SBSEGDBTeam-PSP@intuit.com"

logdir=./log

mkdir -p $logdir

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

typeset -i lag_warning

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:us-west-2:152430470825:gg-sbg-psp-prf-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:us-east-2:893547637742:DB-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
else
  echo "Wrong VPC_ENV"
  exit 1
fi

if [ -f /tmp/monitor_gg_sync_${dbname}.flag ]; then
  echo "Monitoring is blocked temporarily now" > monitor_gg_sync_${dbname}.txt
  exit 0
fi

#login_string=`cat login`@${dbname}
encryption_password=`head -1 /dev/shm/goldengate/db/password_file`
encryption_key=`tail -1 /dev/shm/goldengate/db/password_file`
password=`echo $encryption_password| openssl aes-256-cbc -a -d -salt -k $encryption_key`
login_string=ops_user/${password}@${dbname}

$ORACLE_HOME/bin/sqlplus -s <<EOF > ${logdir}/monitor_gg_sync_${dbname}.lst
$login_string
whenever oserror exit sql.oscode
  whenever sqlerror exit sql.sqlcode

  set head off
  select 'Replication lag on db '|| db_unique_name || ' is ' ||
         round((sysdate - last_update) * 24 * 60, 1) || ' minutes at ' ||
         to_char(sysdate, 'dd-mon-yy hh24:mi:ss')
    from ibobadm.gg_heartbeat, v\$database
    where source = '$site';
--    and database_role = 'PRIMARY';
exit
EOF

if [ `grep "ORA-" ${logdir}/monitor_gg_sync_${dbname}.lst|wc -l` -gt 0 -o \
     `grep "Usage 1: sqlplus -H" ${logdir}/monitor_gg_sync_${dbname}.lst|wc -l` -gt 0 -o \
     `grep "SP2-" ${logdir}/monitor_gg_sync_${dbname}.lst|wc -l` -gt 0 ]; then
  cat ${logdir}/monitor_gg_sync_${dbname}.lst > ${logdir}/monitor_gg_sync_${dbname}.txt
  echo " " >> ${logdir}/monitor_gg_sync_${dbname}.txt
  echo "To disable this monitoring temporarily, please run the following command on `hostname`" >> ${logdir}/monitor_gg_sync_${dbname}.txt
  echo " " >> ${logdir}/monitor_gg_sync_${dbname}.txt
  echo "touch /tmp/monitor_gg_sync_${dbname}.flag" >> ${logdir}/monitor_gg_sync_${dbname}.txt
  echo " " >> ${logdir}/monitor_gg_sync_${dbname}.txt
  echo "To enable this monitoring again, please run the following command on `hostname`" >> ${logdir}/monitor_gg_sync_${dbname}.txt
  echo " " >> ${logdir}/monitor_gg_sync_${dbname}.txt
  echo "rm /tmp/monitor_gg_sync_${dbname}.flag" >> ${logdir}/monitor_gg_sync_${dbname}.txt
#  if [ "$site" == "ihp" ]; then
#    cat ${logdir}/monitor_gg_sync_${dbname}.txt |mail -s "ALERT: There are some errors with running $0 for ${dbname}" ${the_dist}
#  else
    /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "ALERT: There are some errors with running $0 for ${dbname}" --message "`cat ${logdir}/monitor_gg_sync_${dbname}.txt`"
#  fi
  exit;
fi

message=`grep "Replication lag on db" ${logdir}/monitor_gg_sync_${dbname}.lst`
echo $message > ${logdir}/monitor_gg_sync_${dbname}.txt

cat ${logdir}/monitor_gg_sync_${dbname}.txt >> ${logdir}/monitor_gg_sync_${dbname}.hist
echo "message=$message"
if [ "$message" != "" ]; then
  lag=`echo $message |cut -d' ' -f7`
  aws $profile cloudwatch put-metric-data --metric-name ${dbname}-HEARTBEAT --namespace "GoldengateLag" --value $lag --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`

  if [ $(bc <<< "$lag > $lag_warning") -eq 1 ]; then
    echo " " >> ${logdir}/monitor_gg_sync_${dbname}.txt
    echo "To disable this monitoring temporarily, please run the following command on `hostname`" >> ${logdir}/monitor_gg_sync_${dbname}.txt
    echo " " >> ${logdir}/monitor_gg_sync_${dbname}.txt
    echo "touch /tmp/${logdir}/monitor_gg_sync_${dbname}.flag" >> ${logdir}/monitor_gg_sync_${dbname}.txt
    echo " " >> ${logdir}/monitor_gg_sync_${dbname}.txt
    echo "To enable this monitoring again, please run the following command on `hostname`" >> ${logdir}/monitor_gg_sync_${dbname}.txt
    echo " " >> ${logdir}/monitor_gg_sync_${dbname}.txt
    echo "rm /tmp/${logdir}/monitor_gg_sync_${dbname}.flag" >> ${logdir}/monitor_gg_sync_${dbname}.txt
#    if [ "$site" == "ihp" ]; then
#      cat ${logdir}/monitor_gg_sync_${dbname}.txt |mail -s "ALERT: Lag on ${dbname} is $lag minutes" ${the_dist}
#    else
      /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "ALERT: Lag on ${dbname} is $lag minutes" --message "`cat ${logdir}/monitor_gg_sync_${dbname}.txt`"
#    fi
  fi
fi

