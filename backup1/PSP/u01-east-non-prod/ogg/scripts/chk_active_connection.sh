. ~/.bash_profile
. /l/orcl
RDS_SID=$1
logdir=./log
mkdir -p $logdir

# number of days to keep log files
DAYS_KEPT_LOG=14

encryption_password=`head -1 /dev/shm/goldengate/db/password_file`
encryption_key=`tail -1 /dev/shm/goldengate/db/password_file`
#password=`echo $encryption_password| openssl aes-256-cbc -a -d -salt -k $encryption_key`
password=`echo TLx#3Jp)zv`
login_string=ops_user/${password}@${RDS_SID}

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:475968958217:db-sbg-qbo-warning-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:094918931292:db-and-app-sbg-qbo-critical-${VPC_ENV}-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

sqlplus -s << EOF
$login_string
spool ${logdir}/chk_active_connection
select count(*) from v\$session where type != 'BACKGROUND' and status = 'ACTIVE';
spool off
exit
EOF

chk_active_connection=`tail -2 ${logdir}/chk_active_connection.lst |head -1 |tr -s ' ' ' '`
echo "chk_active_connection=$chk_active_connection"

aws cloudwatch put-metric-data --metric-name ${RDS_SID} --namespace "DatabaseActiveConnection" --value $chk_active_connection --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`

# Delete the old log files.
if [[ -L ${log_dir} ]]; then
  find ${log_dir} -follow -name "chk_active_connection_*.log" -mtime +${DAYS_KEPT_LOG} -exec rm {} \;
else
  find ${log_dir} -name "chk_active_connection_*.log" -mtime +${DAYS_KEPT_LOG} -exec rm {} \;
fi

