. /l/orcl
logdir=./log
mkdir -p $logdir

if [ $# -eq 2 ]; then
  db_name=$1
  rds_instance=$2
else
  echo "$0 <Oracle/Postgres db name> <Oracle/Postgres instance name>"
  exit 1
fi

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:us-west-2:152430470825:db-sbg-psp-ppd-t"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

opassword=`cat .op`
ppassword=`cat .pp`
#login_string=ops_user/"'"${opassword}"'@'"${db_name}.sbg-psp-ppd.a.intuit.com:1521/${db_name}"'"
login_string=ops_user/${opassword}@${db_name}

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

rds_type=`aws rds --region us-west-2 describe-db-instances --db-instance-identifier ${rds_instance} --query 'DBInstances[*].[Engine]' --output text`

if [ -f ${db_name}_dms_validation_p2p_new.txt ]; then
    mv ${db_name}_dms_validation_p2p_new.txt ${db_name}_dms_validation_p2p_old.txt
else
    touch ${db_name}_dms_validation_p2p_old.txt
fi

rm -f ${db_name}_dms_validation_p2p_new.txt

if [ ${rds_type} == "oracle-ee" ]; then    
    sqlplus -s << EOF
    $login_string
    spool ${db_name}_dms_validation_p2p_new.txt
    select count(*)
    from AWSDMS_CONTROL_TABLE."awsdms_validation_p2p"
    where error_time between systimestamp-(1/24) and systimestamp;

    select count(*)
    from AWSDMS_CONTROL_TABLE."awsdms_validation_p2p";
    spool off
    exit
EOF
elif [ ${rds_type} == "aurora-postgresql" ]; then    
  ./run_audit_postgres.sh ${rds_instance} $db_name  dms_validation_p2p.sql "-v dbname=${db_name}" > ${db_name}_dms_validation_p2p_new.txt
else
  echo -e "\n Invalid Engine type or this engine type is not supported by this script"
fi

diff ${db_name}_dms_validation_p2p_new.txt ${db_name}_dms_validation_p2p_old.txt > ${db_name}_dms_validation_p2p_diff.txt
if [ `cat ${db_name}_dms_validation_p2p_diff.txt |grep -v FullLoadProgressPercent |wc -l` -gt 2 -o `cat ${db_name}_dms_validation_p2p_diff.txt |grep "FullLoadProgressPercent = 100" |wc -l` -gt 0 ]; then
    echo "FROM:" > dms_validation_p2p.txt
    cat ${db_name}_dms_validation_p2p_old.txt >> dms_validation_p2p.txt
    echo "" >> dms_validation_p2p.txt
    echo "TO:" >> dms_validation_p2p.txt
    cat ${db_name}_dms_validation_p2p_new.txt >> dms_validation_p2p.txt
    echo "ALERT: DMS HCM Validation mismatch Report on ${db_name}"
    /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "ALERT: DMS HCM Validation mismatch Report on ${db_name}" --message "`cat dms_validation_p2p.txt`"
fi

#dms_validation_p2p=`tail -2 ${logdir}/dms_validation_p2p.lst |head -1 |tr -s ' ' ' '`
#echo "dms_validation_p2p=$dms_validation_p2p"

#aws cloudwatch put-metric-data --metric-name ${db_name} --namespace "dms_validation_p2p" --value $dms_validation_p2p --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`

