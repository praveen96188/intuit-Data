. /l/orcl
script_dir="/u01/mayank"
sqlplus="$ORACLE_HOME/bin/sqlplus"
#maillist="mayank_choubey@intuit.com"
#maillist="ketan_popat@intuit.com,aditya_bhardwaj@intuit.com,mayank_choubey@intuit.com"

cd ${script_dir}
RDS_SID=$1
start_time=$2
end_time=$3

encryption_password=`head -1 /dev/shm/goldengate/db/password_file`
encryption_key=`tail -1 /dev/shm/goldengate/db/password_file`
password=`echo $encryption_password| openssl aes-256-cbc -a -d -salt -k $encryption_key`
login_string=ops_user/${password}@${RDS_SID}





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

awrrpt="awrrpt_${RDS_SID}_1_${start_snap}_${end_snap}.html"

sqlplus -s $login_string @$awr_sql <<EOF
html
15
$start_snap
$end_snap
$awrrpt

EOF

#echo " " | mailx -s "AWR report for SID ${qdc_db}" -a "$awrrpt" "$maillist"
cp ${awrrpt} /tmp/
chmod 644 /tmp/${awrrpt}

