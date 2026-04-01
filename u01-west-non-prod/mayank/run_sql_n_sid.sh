. /l/orcl

DB_SID=$1

stty -echo
echo "Please enter the database master user intuadmin password:"
read sys_password
stty echo

echo $sys_password > .p

login_string="intuadmin/"${sys_password}"@'"${DB_SID}.sbg-psp-ppd.a.intuit.com:1521/${DB_SID}"'"

function run_sql
{
  DB_SID=$1
  sys_password=`cat .p`
  rm -f .p
  login_string="intuadmin/"${sys_password}"@'"${DB_SID}.sbg-psp-ppd.a.intuit.com:1521/${DB_SID}"'"
  /u01/softwares/oracle_client/19.3_client/bin/sqlplus <<EOF
$login_string
--    connect intuadmin/$sys_password@${RDS_SID}
    set echo on
    set feed on
    set time on
    set timi on

    select INSTANCE_NAME, HOST_NAME from v\$instance;

    @$2 $3
    exit
EOF
}
export -f run_sql
nohup bash -c "run_sql $1 $2 $3" &

