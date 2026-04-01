. /l/orcl

DB_SID=$1
export TNS_ADMIN=.

stty -echo
echo "Please enter the database master user intuadmin password:"
read sys_password
stty echo

echo $sys_password > .p

#login_string="intuadmin/"${sys_password}"@'"${DB_SID}.sbg-psp-prod.a.intuit.com:1521/${DB_SID}"'"
#login_string=intuadmin/"${sys_password}"@"'${DB_SID}.sbg-psp-prod.a.intuit.com:2632/${DB_SID}'"

function run_sql
{
  DB_SID=$1
  sys_password=`cat .p`
  rm -f .p
  login_string="intuadmin/"${sys_password}"@'"${DB_SID}.sbg-psp-prod.a.intuit.com:1521/${DB_SID}"'"
echo ${login_string}
  $ORACLE_HOME/bin/sqlplus <<EOF
  ${login_string}
    set echo on
    set feed on
    set time on
    set timi on

    select INSTANCE_NAME, HOST_NAME from v\$instance;
    select name,open_mode,database_role from v\$database;

    @$2 $3
    exit
EOF
}
export -f run_sql
nohup bash -c "run_sql $1 $2 $3" &

