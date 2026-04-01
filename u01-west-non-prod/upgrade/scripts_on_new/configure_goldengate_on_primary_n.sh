#-----------------------------------------------------------------------
# Filename: configure_goldengate_on_primary.sh
# Usage: ./configure_goldengate_on_primary.sh
#-----------------------------------------------------------------------
. /l/orcl

stty -echo
echo "Please enter the database master user intuadmin password:"
read sys_password
echo "Please enter the database GGS/GGT password to create these users:"
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
  CLUSTER_NAME="`echo $RDS_SID`"
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
  grant create session to GGS;
  grant DBA to GGS;
  exec rdsadmin.rdsadmin_dbms_goldengate_auth.grant_admin_privilege(grantee=>'GGS',grant_select_privileges=>true, do_grants=>TRUE);
  grant exempt access policy to GGS;
  grant select any dictionary to GGS;

  grant create session to GGT;
  grant DBA to GGT;
  exec rdsadmin.rdsadmin_dbms_goldengate_auth.grant_admin_privilege(grantee=>'GGT',grant_select_privileges=>true, do_grants=>TRUE);
  grant exempt access policy to GGT;
  host sed '8,25d' $GG_HOME/sequence.sql > $GG_HOME/rds_sequence.sql
  @$GG_HOME/rds_sequence.sql ggs
  GRANT EXECUTE ON GGS.updateSequence to GGT;
  GRANT EXECUTE ON GGS.replicateSequence to GGT;
EOF
}

function config_gg
{
  char7_8=${RDS_SID:6:2}
#  cluster_num=`echo $RDS_SID |rev |cut -c1-2 |rev`

  if [ "$char7_8" == "01" ]; then
    # production primary
#    primary="y"
#    cluster_num="m2"
    extract="epspsym2"
    pump="ppspsym1"
  elif [ "$char7_8" == "ib" ]; then
    # production standby
    #cluster_num="i2"
    extract="epspsyi2"
    pump="ppspsyi1"
  else
    echo "Unsupported env"
    exit 1
  fi

  extract_obey="create_${extract}.oby"
  pump_obey="create_${pump}.oby"
  replicat_obey="create_${replicat}.oby"

  echo "Create credentialstore, schematrandata, extracts and pumps"
#  if [ "$primary" == "y" ]; then
    cd $GG_HOME
    ./ggsci << EOF
      start mgr
      delete CREDENTIALSTORE
      add CREDENTIALSTORE
      ALTER CREDENTIALSTORE ADD USER ggs@$RDS_SID password $gg_password alias ggsource
      ALTER CREDENTIALSTORE ADD USER ggt@$RDS_SID password $gg_password alias ggtarget
      info CREDENTIALSTORE
      obey ./diroby/dblogin_s.oby
      obey ./diroby/dblogin_t.oby
      add checkpointtable
      obey ./diroby/dblogin_s.oby
      add schematrandata PSPADM
      obey ./diroby/$extract_obey
      obey ./diroby/$pump_obey
      unregister extract $extract database
      register extract $extract database
      info all
      start e*
      start p*
      info all
      exit
EOF
#  fi
}

function upload_wallet
{
  echo ""
  echo "Upload wallet files to IDPS"
  mv $GG_HOME/dircrd/cwallet.sso /dev/shm/goldengate/${CLUSTER_NAME}/dircrd/cwallet.sso
  ln -s /dev/shm/goldengate/${CLUSTER_NAME}/dircrd/cwallet.sso $GG_HOME/dircrd/cwallet.sso

  stash put-secret --api-key-grant-policy-id ${IDPS_POLICY_ID} --api-endpoint ${IDPS_ENDPOINT} --secret-name secrets/goldengate/${APP_ENV}/${AWS_REGION}/${CLUSTER_NAME}/dircrd/cwallet.sso.tmp --secret-value /dev/shm/goldengate/${CLUSTER_NAME}/dircrd/cwallet.sso -overwrite
  stash put-secret --api-key-grant-policy-id ${IDPS_POLICY_ID} --api-endpoint ${IDPS_ENDPOINT} --secret-name secrets/goldengate/${APP_ENV}/${AWS_REGION}/${CLUSTER_NAME}/dirwlt/cwallet.sso.tmp --secret-value /dev/shm/goldengate/${CLUSTER_NAME}/dirwlt/cwallet.sso -overwrite
}

get_env_variables
configure_db
config_gg
#upload_wallet

