if [ $# -ne 4 ]; then
  echo "Usage: awr_report_m.sh <RDS_On_EC2_hub> <DB_name_for_AWR> <start_time> <end_time>" 
  echo "      where <RDS_On_EC2_hub> is value of RDS_SID set on GG hub"
  echo "            <DB_name_for_AWR> is value of RDS SID against which AWR need to generate"  
  echo "            <start_time> start time in YYMMDDHHMI format"
  echo "            <end_time> end time in YYMMDDHHMI format"
  echo "Example: awr_report_m.sh pspuwp01 2009181000 2007081100" 
  exit 2
fi

. /l/orcl
script_dir="/u01/mayank"
sqlplus="$ORACLE_HOME/bin/sqlplus"
#maillist="mayank_choubey@intuit.com"
#maillist="ketan_popat@intuit.com,aditya_bhardwaj@intuit.com,mayank_choubey@intuit.com"

cd ${script_dir}
RDS_SID=$1
AWR_SID=$2
start_time=$3
end_time=$4

encryption_password=`head -1 /dev/shm/goldengate/${RDS_SID}/password_file`
encryption_key=`tail -1 /dev/shm/goldengate/${RDS_SID}/password_file`
password=`echo $encryption_password| openssl aes-256-cbc -a -d -salt -k $encryption_key`
#login_string=ops_user/${password}@${RDS_SID}
login_string="ops_user/"${password}"@'"${AWR_SID}.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/${AWR_SID}"'"




#login="ops_user/'Salsa!23'@$qdc_db"
logdir="/u01/mayank/logs"
awr_sql="$ORACLE_HOME/rdbms/admin/awrrpt.sql"

cd $logdir

#rm -f awrrpt_*.html
#rm -f /tmp/awrrpt_*.html

dbid=`$ORACLE_HOME/bin/sqlplus -s <<EOF
$login_string
set pages 0
select dbid from v\\$database;
EOF`

start_snap=`echo $start_time | ${script_dir}/timeout.sh -t 15 $ORACLE_HOME/bin/sqlplus -s $login_string @$script_dir/awr_snap_m.sql | tr -dc '0-9'` 
end_snap=`echo $end_time | ${script_dir}/timeout.sh -t 15 $ORACLE_HOME/bin/sqlplus -s $login_string @$script_dir/awr_snap_m.sql | tr -dc '0-9'`

awrrpt="awrrpt_${AWR_SID}_1_${start_time}_${end_time}.html"

sqlplus -s $login_string @$awr_sql <<EOF
html
30
$start_snap
$end_snap
$awrrpt

EOF

#echo " " | mailx -s "AWR report for SID ${qdc_db}" -a "$awrrpt" "$maillist"
cp ${awrrpt} /tmp/
chmod 644 /tmp/${awrrpt}

