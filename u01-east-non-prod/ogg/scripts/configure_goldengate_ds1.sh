#-----------------------------------------------------------------------
# Filename: configure_goldengate.sh
# Usage: ./configure_goldengate.sh
#-----------------------------------------------------------------------

if [ $# -eq 1 ]; then
  CLUSTER_NAME=$1
  RDS_SID=$1
else
  echo "$0 <DBName>"
  echo "where <DBName> is RDS name"
  exit 1
fi

stty -echo
echo "Please enter the database master user intuadmin password:"
read sys_password
echo "Please enter the database GGS/GGT password:"
read gg_password

stty echo
function get_env_variables
{
  . /l/orcl
  APP_ENV=`cat /etc/intu_metadata/app.ini |grep APP_ENV |cut -d'=' -f2`
  ACCOUNT_ID=`cat /etc/intu_metadata/app.ini |grep ACCOUNT_ID |cut -d'=' -f2`
  IDPS_ENDPOINT=`cat /etc/intu_metadata/app.ini |grep IDPS_ENDPOINT |cut -d'=' -f2`
  IDPS_POLICY_ID=`cat /etc/intu_metadata/app.ini |grep IDPS_POLICY_ID |cut -d'=' -f2`
  AWS_REGION=`cat /etc/intu_metadata/app.ini |grep AWS_REGION |cut -d'=' -f2`
}

function configure_db
{
  sqlplus -s /nolog <<EOF
  connect intuadmin/$sys_password@$RDS_SID
  select name from v\$database;
  SELECT SUPPLEMENTAL_LOG_DATA_MIN, FORCE_LOGGING FROM V\$DATABASE;
  exec rdsadmin.rdsadmin_util.alter_supplemental_logging('ADD');
  exec rdsadmin.rdsadmin_util.force_logging();
  exec rdsadmin.rdsadmin_util.switch_logfile;
  exec rdsadmin.rdsadmin_util.set_configuration('archivelog retention hours',72);
  commit;
  grant create session to GGT;
  grant DBA to GGT;
  exec rdsadmin.rdsadmin_dbms_goldengate_auth.grant_admin_privilege(grantee=>'GGT',grant_select_privileges=>true, do_grants=>TRUE);
  grant exempt access policy to GGT;
  host sed '8,25d' $GG_HOME/sequence.sql > $GG_HOME/rds_sequence.sql
  @$GG_HOME/rds_sequence.sql ggs
  GRANT EXECUTE ON GGS.updateSequence to GGT;
  GRANT EXECUTE ON GGS.replicateSequence to GGT;
  drop table GGT.CHECKPOINT;
EOF
}

function config_gg
{
  replicat_obey=`ls $GG_HOME/diroby/create_r*ds1*oby`
  replicat=`grep 'add replicat' $replicat_obey |cut -d' ' -f3|sed 's/.$//'`
  echo "Will use $replicat_obey to create replicat"
  echo "replicat=$replicat"
  cd $GG_HOME
  ./ggsci << EOF
    delete CREDENTIALSTORE
    add CREDENTIALSTORE
    ALTER CREDENTIALSTORE ADD USER ggs@$RDS_SID password $gg_password alias ggsource
    ALTER CREDENTIALSTORE ADD USER ggt@$RDS_SID password $gg_password alias ggtarget
    info CREDENTIALSTORE
    obey ./diroby/dblogin_s.oby
    obey ./diroby/dblogin_t.oby
    add checkpointtable
    obey ./diroby/dblogin_s.oby
    add schematrandata QBO
    add schematrandata QBO_DATA
    obey $replicat_obey
    register replicat $replicat database
    info all
    exit
EOF
}

function start_replicat
{
  sqlplus -s /nolog <<EOF
  spool scn_for_${RDS_SID}.txt
  connect intuadmin/$sys_password@$RDS_SID
  set numwidth 30
  select name from v\$database;
  select resetlogs_change# from v\$database_incarnation where incarnation# = (select max(incarnation#) from v\$database_incarnation);
  spool off
EOF

  scn=`cat scn_for_${RDS_SID}.txt |grep -A2 "RESETLOGS_CHANGE" |tail -1 |sed "s/ //g"`
  if [ "$scn" == "" -o `grep "ORA-" scn_for_${RDS_SID}.txt |wc -l` -gt 0 ]; then
    echo "Can not get the scn. check the file scn_for_${RDS_SID}.txt for the reason"
  else
    echo "scn for starting replicat: $scn"
  fi

  echo ""
  echo "Do you want to start the replicat at scn $scn now (y/n)?"
  read answer

  if [ "$answer" == "y" ]; then
    replicat=`grep 'add replicat' $replicat_obey |cut -d' ' -f3`
    echo "replicat=$replicat"
    echo "scn=$scn"
    cd $GG_HOME
    ./ggsci << EOF
      start $replicat atcsn $scn
      info $replicat
      sh sleep 5
      info all
      exit
EOF
  fi
}

function upload_wallet
{
  echo ""
  echo "Upload wallet files to IDPS"

  mv $GG_HOME/dircrd/cwallet.sso /dev/shm/goldengate/${CLUSTER_NAME}/dircrd/cwallet.sso
  ln -s /dev/shm/goldengate/${CLUSTER_NAME}/dircrd/cwallet.sso $GG_HOME/dircrd/cwallet.sso
  stash put-secret --api-key-grant-policy-id ${IDPS_POLICY_ID} --api-endpoint ${IDPS_ENDPOINT} --secret-name secrets/goldengate/${APP_ENV}/${AWS_REGION}/${CLUSTER_NAME}/dircrd/cwallet.sso --secret-value /dev/shm/goldengate/${CLUSTER_NAME}/dircrd/cwallet.sso -overwrite
  stash put-secret --api-key-grant-policy-id ${IDPS_POLICY_ID} --api-endpoint ${IDPS_ENDPOINT} --secret-name secrets/goldengate/${APP_ENV}/${AWS_REGION}/${CLUSTER_NAME}/dirwlt/cwallet.sso --secret-value /dev/shm/goldengate/${CLUSTER_NAME}/dirwlt/cwallet.sso -overwrite
  stash put-secret --api-key-grant-policy-id ${IDPS_POLICY_ID} --api-endpoint ${IDPS_ENDPOINT} --secret-name secrets/goldengate/${APP_ENV}/${AWS_REGION}/db/password_file --secret-value /dev/shm/goldengate/db/password_file -overwrite
}

get_env_variables
configure_db
config_gg
#upload_wallet
start_replicat

