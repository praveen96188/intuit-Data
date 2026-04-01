. ~/.bash_profile
. /l/orcl
logdir=./log
mkdir -p $logdir

RDS_SID=$1

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:us-west-2:152430470825:db-sbg-psp-ppd-t"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-critical-prod-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

#encryption_password=`head -1 /dev/shm/goldengate/${RDS_SID}/password_file`
#encryption_key=`tail -1 /dev/shm/goldengate/${RDS_SID}/password_file`
#password=`echo $encryption_password| openssl aes-256-cbc -a -d -salt -k $encryption_key`
password=`cat .p`
#login_string=ops_user/"'"${password}"'@'"${RDS_SID}.sbg-psp-ppd.a.intuit.com:1521/${RDS_SID}"'"
login_string=ops_user/${password}@${RDS_SID}

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ -f ${RDS_SID}_dms_apply_exceptions_new.txt ]; then
  mv ${RDS_SID}_dms_apply_exceptions_new.txt {$RDS_SID}_dms_apply_exceptions_old.txt
else
  touch {$RDS_SID}_dms_apply_exceptions_old.txt
fi

rm -f ${RDS_SID}_dms_apply_exceptions_new.txt

sqlplus -s << EOF
$login_string
spool ${RDS_SID}_dms_apply_exceptions_new.txt
select count(*)
from AWSDMS_CONTROL_TABLE."awsdms_apply_exceptions"
where error_time between systimestamp-(1/24) and systimestamp;

select count(*)
from AWSDMS_CONTROL_TABLE."awsdms_apply_exceptions";
spool off
exit
EOF

diff ${RDS_SID}_dms_apply_exceptions_new.txt {$RDS_SID}_dms_apply_exceptions_old.txt > dms_apply_exceptions_diff.txt
if [ `cat dms_apply_exceptions_diff.txt |grep -v FullLoadProgressPercent |wc -l` -gt 2 -o `cat dms_apply_exceptions_diff.txt |grep "FullLoadProgressPercent = 100" |wc -l` -gt 0 ]; then
  echo "FROM:" > dms_apply_exceptions.txt
  cat {$RDS_SID}_dms_apply_exceptions_old.txt >> dms_apply_exceptions.txt
  echo "" >> dms_apply_exceptions.txt
  echo "TO:" >> dms_apply_exceptions.txt
  cat ${RDS_SID}_dms_apply_exceptions_new.txt >> dms_apply_exceptions.txt
  echo "ALERT: DMS Apply Exception Report B=>C"
  /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "ALERT: DMS Apply Exception Report B=>C" --message "`cat dms_apply_exceptions.txt`"
fi

#dms_apply_exceptions=`tail -2 ${logdir}/dms_apply_exceptions.lst |head -1 |tr -s ' ' ' '`
#echo "dms_apply_exceptions=$dms_apply_exceptions"

#aws cloudwatch put-metric-data --metric-name ${RDS_SID} --namespace "dms_apply_exceptions" --value $dms_apply_exceptions --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`


