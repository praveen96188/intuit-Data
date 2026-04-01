#!/bin/bash

# TODO with sudo Changing default Timezone to PTC
#ln -sf /usr/share/zoneinfo/US/Pacific /etc/localtime


download_dir="/tmp/psp-db-download"
install_dir="/tmp/psp-db-install"
file_name="psp-dbinstall"
apiPolicyId=""
applianceId=""
awsEnvName=""
version=""
nexusdepot=""
dbDeployment=""
dataAccessURL="dataAccess-url"

#Private Functions
__log() {
    echo "$(date +'%FT%T,%3N%z') $APP_NAME-LOGGER - $PSP_CONFIG_ENV - $1";
}

check_dbDeployment() {
    dbDeployment=`cat /app/dbDeployment.txt`
    __log "[stage=check_dbDeployment] DB Deployment variable is ${dbDeployment}";
}

__download_this() {
    local url="$1"
    local local_file="$2"

    __log "Downloading ${url}"
    curl -sS -L -o ${local_file} ${url}

    return
}

set_version(){
  version=`cat /app/version.txt`
  __log "[stage=set_version] version : ${version}"
}

set_nexusdepot(){
  if [[ ${version} == *"SNAPSHOT"* ]]
  then
    nexusdepot="Snapshots";
  else
    nexusdepot="Releases";
  fi
  __log "[stage=set_nexusdepot] nexusdepot : ${nexusdepot}"
}

setting_sql_parameters() {
    __log "[stage=create_sql_parameters] Setting parameters for  sqlplus ... Start";

    ORACLE_HOME="/opt/oracle/instantclient"
    DYLD_LIBRARY_PATH="/opt/oracle/instantclient"
    LD_LIBRARY_PATH="/opt/oracle/instantclient"
    NLS_LANG="AMERICAN_AMERICA.UTF8"
    TNS_ADMIN="/home/etc/"
    PATH="$PATH:/opt/oracle/instantclient"
    TNSNAMES="/home/etc/"
    ORACLE_SID="XE"

    __log "[stage=create_sql_parameters] Setting parameters for  sqlplus ... Complete";
}

install_sql() {
      __log "[stage=install_sql] Installing SQLPlus command... Start";

      sudo mkdir -p /opt/oracle/
      sudo cp /app/sqlplus/instantclient-* /opt/oracle/

      sudo mkdir -p /home/etc/
      sudo cp /app/sqlplus/tnsnames.ora /home/etc/
      sudo cp /app/sqlplus/sqlnet.ora /home/etc/

      sudo cp /app/sqlplus/oracleBase.sh /home/
      sudo /home/oracleBase.sh

      sudo mkdir -p ${ORACLE_HOME} ${DYLD_LIBRARY_PATH} ${LD_LIBRARY_PATH} ${NLS_LANG} ${TNS_ADMIN} ${PATH} ${TNSNAMES} ${ORACLE_SID}

      __log "[stage=install_sql] Installing SQLPlus command... Complete";
}

download_artifacts() {
    __log "[stage=download_artifacts] Downloading app artifacts... Start";

    rm -Rf $install_dir;
    [ -f $download_dir ] && rm -f $download_dir;

    if [ ! -d $download_dir ]; then mkdir -p $download_dir; chmod 777 $download_dir || exit 1; fi;
    if [ ! -d $install_dir ]; then mkdir -p $install_dir; chmod 777 $install_dir || exit 1; fi;

    cd $download_dir || exit 1;

    __log "Downloading psp-dbinstall-$version.zip as $file_name.zip";
    __download_this "https://artifact.intuit.com/nexus/service/local/artifact/maven/redirect?r=SBG.Payments.Intuit-$nexusdepot&g=com.intuit.sbg.psp&a=$file_name&v=$version&e=zip" "$file_name.zip"
    if [ $? -ne 0 ]; then
      __log "[ERROR] Downloading failed $file_name.zip";
      exit 1;
    fi;

    __log "Unzipping files..."
    unzip -o -j $download_dir/$file_name -d $install_dir || exit 1;

    __log "[stage=download_artifacts] Downloading app artifacts... Complete";
}

set_policyid() {
    __log "[stage=set_policyid] Setting Policy ID... Start";

    case "$PSP_CONFIG_ENV" in
       "ds2")
          __log "[stage=set_policyid] DS2 IDPS Policy for ${PSP_CONFIG_ENV}";
          apiPolicyId="p-r5ishwk5ne8l";
          awsEnvName="AWSDS2";
          applianceId="payrollsvcplatform-pre-production-n93c09.pd.idps.a.intuit.com";
          ;;
       "qal")
          __log "[stage=set_policyid] SYS IDPS Policy for ${PSP_CONFIG_ENV}";
          apiPolicyId="p-rfpj85uszps9";
          awsEnvName="AWSSYS";
          applianceId="payrollsvcplatform-pre-production-n93c09.pd.idps.a.intuit.com";
          ;;
       "e2e")
          __log "[stage=set_policyid] PDS IDPS Policy for ${PSP_CONFIG_ENV}";
          apiPolicyId="p-4vmqs9vi0k3u";
          awsEnvName="AWSPDS";
          applianceId="payrollsvcplatform-pre-production-n93c09.pd.idps.a.intuit.com";
          ;;
       "e2edr")
          __log "[stage=set_policyid] PDS IDPS Policy for ${PSP_CONFIG_ENV}";
          apiPolicyId="p-4vmqs9vi0k3u";
          awsEnvName="AWSPDS";
          dataAccessURL="dataAccess-url-dr"
          applianceId="payrollsvcplatform-pre-production-n93c09.pd.idps.a.intuit.com";
          ;;
       "prf")
          __log "[stage=set_policyid] PRF IDPS Policy for ${PSP_CONFIG_ENV}";
          apiPolicyId="p-49khk3shivzs";
          awsEnvName="AWSPRF";
          applianceId="payrollsvcplatform-pre-production-n93c09.pd.idps.a.intuit.com";
          ;;
       "stg")
          __log "[stage=set_policyid] STG IDPS Policy for ${PSP_CONFIG_ENV}";
          apiPolicyId="p-bfpmz50utyz9";
          awsEnvName="AWSSTG";
          applianceId="payrollsvcplatform-production-wuoxiq.pd.idps.a.intuit.com";
          ;;
       "prd")
          __log "[stage=set_policyid] PROD IDPS Policy for ${PSP_CONFIG_ENV}";
          apiPolicyId="p-vrtq178u32i3";
          awsEnvName="AWSPROD";
          applianceId="payrollsvcplatform-production-wuoxiq.pd.idps.a.intuit.com";
          ;;
       "prddr")
          __log "[stage=set_policyid] PROD IDPS Policy for ${PSP_CONFIG_ENV}";
          apiPolicyId="p-vrtq178u32i3";
          awsEnvName="AWSPROD";
          dataAccessURL="dataAccess-url-dr"
          applianceId="payrollsvcplatform-production-wuoxiq.pd.idps.a.intuit.com";
          ;;
    esac

    if [ -z "${apiPolicyId}" ] || [ -z "${awsEnvName}" ];
    then
      __log "IDPS api policy id or awsEnvName is not set! Exiting";
      exit 1;
    fi

    __log "awsEnvName :${awsEnvName}";
    __log "IDPS API Policy ID :${apiPolicyId}";
    __log "IDPS appliance ID :${applianceId}";

    __log "[stage=set_policyid] Setting Policy ID... Complete";
}

deploy_database() {
    __log "[stage=deploy_database] Deploying database changes... Start";

    cd $install_dir;

    export LD_LIBRARY_PATH=/usr/lib/oracle/11.2/client64/lib:$LD_LIBRARY_PATH
    export PATH=/usr/lib/oracle/11.2/client64/bin:$PATH
    export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH

    stash get --api-key-grant-policy-id ${apiPolicyId} --access-type kube --appliance ${applianceId} --secret-name PSPSecrets/secrets/${awsEnvName}/pspadm-db-password --output output --overwrite --utf-8
    if [ ! -e output ]; then
        __log "[ERROR] Database password not found";
        __log "[stage=deploy_database] Deploying database changes... Failed";
        exit 1;
    fi
    pwd=$(/bin/cat output)
    /bin/rm -f output

    stash get --api-key-grant-policy-id ${apiPolicyId} --access-type kube --appliance ${applianceId} --secret-name PSPSecrets/secrets/${awsEnvName}/${dataAccessURL} --output output --overwrite --utf-8
    if [ ! -e output ]; then
        __log "[ERROR] Database connection string not found";
        __log "[stage=deploy_database] Deploying database changes... Failed";
        exit 1;
    fi
    dbconnect=$(cut -d'@' -f2 output)
    /bin/rm -f output

    __log "Installing database changes...";
    sqlplus "PSPADM/${pwd}@${dbconnect}" @InstallDB.sql PSPAPP PSPAPP_ROLE CRUD PSPREAD PSPREAD_ROLE || exit 1;

    __log "Verify no PSP errors in InstallDB.log...";

    if grep 'Warning: Procedure created with compilation errors.' InstallDB.log; then
        __log "[stage=deploy_database] Deploying database changes... Failed";
        exit 1;
    fi

    if grep 'Warning: Trigger created with compilation errors.' InstallDB.log; then
        __log "[stage=deploy_database] Deploying database changes... Failed";
        exit 1;
    fi;

    if grep 'SYNONYMs count ERROR' InstallDB.log; then
        __log "[stage=deploy_database] Deploying database changes... Failed";
    #    exit 1;
    fi;

    __log "[stage=deploy_database] Deploying database changes... Complete";
}



check_dbDeployment

if [ "$dbDeployment" == "Yes" ]
then
    set_version
    set_nexusdepot
    setting_sql_parameters
    install_sql
    download_artifacts
    set_policyid
    deploy_database
fi

sleep 60