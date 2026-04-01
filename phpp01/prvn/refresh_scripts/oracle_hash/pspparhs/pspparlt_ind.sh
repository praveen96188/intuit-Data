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
    connect intuadmin/$sys_password@'pspparlt.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/PSPPARHS'
    set echo on
    set feed on
    set time on
    set timi on

    select INSTANCE_NAME, HOST_NAME from v\$instance;

    $1 
    exit
EOF
}
export -f run_sql
nohup bash -c "run_sql @1_hash_ind_ffs.sql $1" &
nohup bash -c "run_sql @2_hash_ind_ffs.sql $1" &
nohup bash -c "run_sql @3_hash_ind_ffs.sql $1" &
nohup bash -c "run_sql @4_hash_ind_ffs.sql $1" &
nohup bash -c "run_sql @hash_part_tab_fs_3.sql $1" &
nohup bash -c "run_sql @Range_part_tab_fs.sql $1" &
