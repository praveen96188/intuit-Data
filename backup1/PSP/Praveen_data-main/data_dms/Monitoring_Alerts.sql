#DMS Tasks Status monitoring B-C3###
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-1 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-2 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-2-2tab 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-3 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-4 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-5 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-6 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-7 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-8 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-9 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-pem 1>/dev/null 2>&1

#DMS Tasks Status monitoring C3-C3##
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task1a 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task1b 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task1c 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task2 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task3 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task4 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task5 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task6 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task7a 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task7b 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task7c 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task7d 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task8 1>/dev/null 2>&1

#DMS Apply Exceptions monitoring
*/30 * * * * cd /u01/postgres/scripts; ./chk_dms_apply_exceptions.sh psphpp06 psphpp06 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./chk_dms_apply_exceptions.sh psppp01 psp-prod-mon1 1>log/apply_exceptions_report_o2p_psppp01.log 2>&1






-----check_dms_status.sh
. /l/orcl
if [ `uname -a |grep Linux |wc -l` -gt 0 ]; then
  profile_setting="--region us-west-2"
else
  profile_setting="--profile sbg-psp-ppd --region us-west-2"
fi

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:us-west-2:152430470825:db-sbg-psp-ppd-t"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

dms_task_name=$1
ReplicationTaskArn=`aws $profile_setting dms describe-replication-tasks --query "ReplicationTasks[*].ReplicationTaskArn" --filters "Name=replication-task-id,Values=${dms_task_name}" |grep dms |cut -d'"' -f2`

aws $profile_setting dms describe-replication-tasks --filters "Name=replication-task-id,Values=${dms_task_name}" > ${dms_task_name}.json
if [ -f ${dms_task_name}_status_new.txt ]; then
  mv ${dms_task_name}_status_new.txt ${dms_task_name}_status_old.txt
else
  touch ${dms_task_name}_status_old.txt
fi

ReplicationTaskArn=`grep ReplicationTaskArn ${dms_task_name}.json |cut -d'"' -f4`

rm -f ${dms_task_name}_status_new.txt
Status=`grep \"Status\" ${dms_task_name}.json |cut -d'"' -f4`
echo "Status = $Status" >> ${dms_task_name}_status_new.txt

FullLoadProgressPercent=`grep FullLoadProgressPercent ${dms_task_name}.json |cut -d':' -f2 | cut -d',' -f1`
echo "FullLoadProgressPercent =$FullLoadProgressPercent" >> ${dms_task_name}_status_new.txt

TablesErrored=`grep TablesErrored ${dms_task_name}.json |cut -d':' -f2 | cut -d',' -f1`
echo "TablesErrored =$TablesErrored" >> ${dms_task_name}_status_new.txt

diff ${dms_task_name}_status_new.txt ${dms_task_name}_status_old.txt > ${dms_task_name}_status_diff.txt
if [ `cat ${dms_task_name}_status_diff.txt |grep -v FullLoadProgressPercent |wc -l` -gt 2 -o `cat ${dms_task_name}_status_diff.txt |grep "FullLoadProgressPercent = 100" |wc -l` -gt 0 ]; then
  echo "FROM:" > ${dms_task_name}_status.txt
  cat ${dms_task_name}_status_old.txt >> ${dms_task_name}_status.txt
  echo "" >> ${dms_task_name}_status.txt
  echo "TO:" >> ${dms_task_name}_status.txt
  cat ${dms_task_name}_status_new.txt >> ${dms_task_name}_status.txt
  echo "INFO: DMS ${dms_task_name} status has changed"
  /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "INFO: DMS ${dms_task_name} status has changed" --message "`cat ${dms_task_name}_status.txt`"
fi


*/30 * * * * cd /u01/postgres/scripts; ./chk_dms_apply_exceptions.sh psphpp06 psphpp06 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./chk_dms_apply_exceptions.sh psppp01 psp-prod-mon1 1>log/apply_exceptions_report_o2p_psppp01.log 2>&1

dms-sbg-psp-oracle-exit-critical-prod-a-intuit-com


---chk_dms_apply_exceptions.sh
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
  topic_arn="arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-critical-prod-a-intuit-com"
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

if [ -f ${db_name}_dms_apply_exceptions_new.txt ]; then
    mv ${db_name}_dms_apply_exceptions_new.txt ${db_name}_dms_apply_exceptions_old.txt
else
    touch ${db_name}_dms_apply_exceptions_old.txt
fi

rm -f ${db_name}_dms_apply_exceptions_new.txt

if [ ${rds_type} == "oracle-ee" ]; then    
    sqlplus -s << EOF
    $login_string
    spool ${db_name}_dms_apply_exceptions_new.txt
    select count(*)
    from AWSDMS_CONTROL_TABLE."awsdms_apply_exceptions"
    where error_time between systimestamp-(1/24) and systimestamp;

    select count(*)
    from AWSDMS_CONTROL_TABLE."awsdms_apply_exceptions";
    spool off
    exit
EOF
elif [ ${rds_type} == "aurora-postgresql" ]; then    
  ./run_postgres.sh ${rds_instance} $db_name  apply_exceptions_report_o2p_count.sql "-v dbname=${db_name}" > ${db_name}_dms_apply_exceptions_new.txt
else
  echo -e "\n Invalid Engine type or this engine type is not supported by this script"
fi

diff ${db_name}_dms_apply_exceptions_new.txt ${db_name}_dms_apply_exceptions_old.txt > ${db_name}_dms_apply_exceptions_diff.txt
if [ `cat ${db_name}_dms_apply_exceptions_diff.txt |grep -v FullLoadProgressPercent |wc -l` -gt 2 -o `cat ${db_name}_dms_apply_exceptions_diff.txt |grep "FullLoadProgressPercent = 100" |wc -l` -gt 0 ]; then
    echo "FROM:" > dms_apply_exceptions.txt
    cat ${db_name}_dms_apply_exceptions_old.txt >> dms_apply_exceptions.txt
    echo "" >> dms_apply_exceptions.txt
    echo "TO:" >> dms_apply_exceptions.txt
    cat ${db_name}_dms_apply_exceptions_new.txt >> dms_apply_exceptions.txt
    echo "ALERT: DMS Apply Exception Report on ${db_name}"
    /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "ALERT: DMS Apply Exception Report on ${db_name}" --message "`cat dms_apply_exceptions.txt`"
fi

#dms_apply_exceptions=`tail -2 ${logdir}/dms_apply_exceptions.lst |head -1 |tr -s ' ' ' '`
#echo "dms_apply_exceptions=$dms_apply_exceptions"

#aws cloudwatch put-metric-data --metric-name ${db_name} --namespace "dms_apply_exceptions" --value $dms_apply_exceptions --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`




--scripts for chk_dms_apply_exceptions.sh
--apply_exceptions_report_o2p_count.sql
select count(*)
from awsdms_control_table.awsdms_apply_exceptions
where "ERROR_TIME" between current_timestamp-interval '1 hour' and current_timestamp;

select count(*)
from awsdms_control_table.awsdms_apply_exceptions;



--run_postgres.sh
export PGPASSWORD=`cat .op`
psql_cmd="psql --username=ops_user -h ${1}.cjls0bohfgpq.us-west-2.rds.amazonaws.com -p 5432 ${2} --echo-all -P pager=off -f $3 $4"
eval "$psql_cmd"


User:ops_user
password:LQx8Jsiv#)6P
vi .op
vi .pp








$ aws cloudwatch put-metric-alarm --alarm-name ${team_tag_value}-${task_name}-cdcthroughputrowssource --alarm-description "Outgoing task changes for the target is more than 1000 rows per second" --metric-name CDCThroughputRowsSource --namespace "AWS/DMS" --statistic Average --period 60 --threshold 1000 --comparison-operator GreaterThanOrEqualToThreshold --evaluation-periods 1 --dimensions "Name=ReplicationInstanceIdentifier,Value=$replication_instance_identifier" "Name=ReplicationTaskIdentifier,Value=$replication_task_identifier" --region=$region

Non-Prod Postgres SYS Database is ready, below are details:
Host : ppsp-sys-mon.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com
Port: 5432
DB Name: psyspg01 

Username:Postgres
Password:changeme

Username:pspadm_owner
Password: p3zu#JAaPR

Username: pspapp
password: K0tvv1Y.j5kZBg


Why DMS Migrated Every character separated with space?
--PSP_SQL_EXECUTION_LOG_ENTRY



--Oracle source

UPDATE pspadm.psp_money_movement_transaction set modified_date = SYS_EXTRACT_UTC(systimestamp), modifier_id = 'PSP-27270',
    status='Canceled' where money_movement_transaction_seq in ('e0660b79-db50-4f18-8e76-386155834cc6');


--Postgres Target

U P D A T E   p s p a d m . p s p _ m o n e y _ m o v e m e n t _ t r a n s a c t i o n   s e t   m o d i f i e d _ d a t e   =   S Y S _ E X T R A C T _ U T C ( s y s t i m e s t a m p ) ,   m o d i f i e r _ i d   =   ' P S P - 2 7 2 7 0 ' , 
         s t a t u s = ' C a n c e l e d '   w h e r e   m o n e y _ m o v e m e n t _ t r a n s a c t i o n _ s e q   i n   ( ' e 0 6 6 0 b 7 9 - d b 5 0 - 4 f 1 8 - 8 e 7 6 - 3 8 6 1 5 5 8 3 4 c c 6 ' ) ;


