. /l/orcl

RDS_SID=$1
start_time=$2
end_time=$3

encryption_password=`head -1 /dev/shm/goldengate/${RDS_SID}/password_file`
encryption_key=`tail -1 /dev/shm/goldengate/${RDS_SID}/password_file`
password=`echo $encryption_password| openssl aes-256-cbc -a -d -salt -k $encryption_key`
login_string=ops_user/"${password}"@${RDS_SID}

logdir=/u01/mayank/logs"

cd $logdir

dbid=`$ORACLE_HOME/bin/sqlplus ${login_string} @/u01/mayank/t.sql`

echo $dbid
