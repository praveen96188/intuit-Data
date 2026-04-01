. /l/orcl

stty -echo
echo "Please enter the database master user intuadmin password:"
read sys_password
stty echo

echo $sys_password > .p
function run_sql
{
  sys_password=`cat .p`
  rm -f .p
  sqlplus /nolog <<EOF
    connect intuadmin/$sys_password@'psphpp02.cjls0bohfgpq.us-west-2.rds.amazonaws.com:1521/PSPHPP02'
    set echo on
    set feed on
    set time on
    set timi on

    select INSTANCE_NAME, HOST_NAME from v\$instance;

    @$1 $2
    exit
EOF
}
export -f run_sql
nohup bash -c "run_sql $1 $2" &
