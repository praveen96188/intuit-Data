#!/bin/sh

#artifact_directory="/Users/djain5/Desktop/artifacts/wars"
#CATALINA_HOME="/Users/djain5/Downloads/apache-tomcat-7.0.42"
#webapps="$CATALINA_HOME/webapps"
#libdir="$CATALINA_HOME/lib"
#configenv=awssys
#APP_NAME="jss-normal"

artifact_directory="/app/artifacts"
keys_directory="/keys"
CATALINA_HOME="/usr/local/tomcat"
webapps="$CATALINA_HOME/webapps"
libdir="$CATALINA_HOME/lib"
configenv=$PSP_CONFIG_ENV
machine_type="APP"
version="";
nexusdepot="";

__log() {
    echo "$(date +'%FT%T,%3N%z') $APP_NAME-LOGGER - $PSP_CONFIG_ENV - $1";
}

set_machine_type() {
  if [[ $APP_NAME == *"jss"* ]]
  then
    __log "[stage=set_machine_type] Setting machine_type to JSS...";
    machine_type="JSS";
  fi
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
}

setup_printer_config() {
   __log "[stage=start_xfs] Starting xfs... Start";
  sudo /usr/bin/xfs -droppriv -daemon
  __log "[stage=start_xfs] Starting xfs... End";

    __log "[stage=start_cups] Starting cups... Start";
  sudo /usr/sbin/cupsd -f &
  __log "[stage=start_cups] Starting cups... End";

   __log "[stage=start_fonts_install] Running fontsetup... Start";
  if ! (chkfontpath -l | grep -q '/usr/local/fonts/ttf')
  then
    sudo /etc/intuit/psp/printsetup/fontsetup.sh
  else
    __log "[stage=start_fonts_install] Failed to run fontsetup.sh";
  fi

}

__download_this() {
    local url="$1"
    local local_file="$2"

    __log "Downloading ${url}"
    curl -sS -L -o ${local_file} ${url}

    return
}


__get_artifact_list() {
    local app_artifacts="";
    case $APP_NAME in
        "qbdt")
          app_artifacts="payroll qbdtws Keynote-Adapter"
          ;;
        "adapter")
          app_artifacts="EWSAdapter cep DISAdapter ptcadapter BRMAdapter"
          ;;
        "sap")
          if [[ $APP_ENV == "prd" ]]
          then
            app_artifacts="SAP"
          else
            app_artifacts="SAP test-ws testtools"
          fi
          ;;
        "vmp")
          app_artifacts="CdmAdapter"
          ;;
        "jss-high"|"jss-normal"|"jss-monitor")
          app_artifacts="batch-jobs"
          ;;
    esac

    #Returning value from this function
    echo $app_artifacts;
}

copy_psp_artifacts(){
   __log "[stage=copy_psp_artifacts] Copy app artifacts from to webapps folder... START";
  fileset=$(__get_artifact_list)
   for i in $fileset; do
    cp $artifact_directory/$i.war $webapps/$i.war
    __log "copying war file to webapps: $i.war"
  done
  __log "[stage=copy_psp_artifacts] Copy artifacts to webapps folder... COMPLETED";
}

copy_psp_app_configuration() {
   __log "[stage=copy_psp_configuration] Copy PSP configuration... START";

    __log "Copying keys to ${keys_directory} to /usr/local/tomcat/key"
    cp ${keys_directory}/key/* /usr/local/tomcat/key

    __log "Copying the psp-config-private.key if it exists";
    if [ -e $CATALINA_HOME/key/psp-config-private.key ]; then cp $CATALINA_HOME/key/psp-config-private.key $libdir/psp/conf; chmod 400 $libdir/psp/conf/psp-config-private.key; fi;

    __log "Copying the psp-config-public.key if it exists";
    if [ -e $CATALINA_HOME/key/psp-config-public.key ]; then cp $CATALINA_HOME/key/psp-config-public.key $libdir/psp/conf; chmod 400 $libdir/psp/conf/psp-config-public.key; fi;

    __log "Copying the psp-prod-app-secret.key if it exists";
    if [ -e $CATALINA_HOME/key/psp-prod-app-secret.key ]; then cp $CATALINA_HOME/key/psp-prod-app-secret.key $libdir/psp/conf; chmod 400 $libdir/psp/conf/psp-prod-app-secret.key; fi;

    __log "Copying the psp-prod-qbapp-aes.key if it exists";
    if [ -e $CATALINA_HOME/key/psp-prod-qbapp-aes.key ]; then cp $CATALINA_HOME/key/psp-prod-qbapp-aes.key $libdir/psp/conf; chmod 400 $libdir/psp/conf/psp-prod-qbapp-aes.key; fi;

    __log "Unzipping ${artifact_directory}/psp-configuration.zip/psp-configuration/psp-configuration-${version}-$configenv-jboss-conf.zip to $libdir/psp/conf/";
    unzip -o -j ${artifact_directory}/psp-configuration.zip psp-configuration/psp-configuration-${version}-$configenv-jboss-conf.zip -d $libdir/psp/conf/
    unzip -o $libdir/psp/conf/psp-configuration-${version}-$configenv-jboss-conf.zip -d $libdir/psp/conf/
    rm $libdir/psp/conf/psp-configuration-${version}-$configenv-jboss-conf.zip

    __log "Copy spcf-meta-conf to tomcat bin folder... Start";
    cp $libdir/psp/conf/spcf-meta-conf.xml $libdir/../bin/spcf-meta-conf.xml
    chmod 775  $libdir/../bin/spcf-meta-conf.xml;
    __log "Copy spcf-meta-conf to tomcat bin folder...  Complete";

    __log "Creating /apps/batch/flux/lib/resources";
    mkdir -p /apps/batch/flux/lib/resources;
    unzip -j -o ${artifact_directory}/psp-batchjobs-batchdeploy.zip lib/resources/IntuitEntitlementABM.xsd -d /apps/batch/flux/lib/resources || exit 1;
    unzip -j -o ${artifact_directory}/psp-batchjobs-batchdeploy.zip lib/resources/IntuitEntitlementABO.xsd -d /apps/batch/flux/lib/resources || exit 1;
    unzip -j -o ${artifact_directory}/psp-batchjobs-batchdeploy.zip lib/resources/IntuitEntitlementReqABCSImpl.wsdl -d /apps/batch/flux/lib/resources || exit 1;
    unzip -j -o ${artifact_directory}/psp-batchjobs-batchdeploy.zip lib/resources/IntuitCustomerAsset.wsdl -d /apps/batch/flux/lib/resources || exit 1;
    unzip -j -o ${artifact_directory}/psp-batchjobs-batchdeploy.zip lib/resources/intuit.xsd -d /apps/batch/flux/lib/resources || exit 1;

    mkdir -p /tmp/batchdeploy;

    if [[ $APP_NAME == "sap" ]] || [[ $APP_NAME == "adapter" ]]
    then
      __log "Copying config files specific for sap and adapter";
      unzip -j -o ${artifact_directory}/SAP.war WEB-INF/lib/aia-gateway-${version}.jar WEB-INF/lib/amo-gateway-${version}.jar -d /tmp;

      cd /tmp && /usr/java/default/bin/jar -xf /tmp/aia-gateway-${version}.jar;
      cp /tmp/resources/CommonBillingProfileABM.xsd  /apps/batch/flux/lib/resources;
      cp /tmp/resources/CommonBillingProfileABO.xsd  /apps/batch/flux/lib/resources;
      cp /tmp/resources/Fault.xsd  /apps/batch/flux/lib/resources;
      cp /tmp/resources/QueryBillingTransactionsIntuitEBS.wsdl  /apps/batch/flux/lib/resources;
      cp /tmp/resources/QueryBillingTransactionsIntuitReqABCS.wsdl  /apps/batch/flux/lib/resources;

      cd /tmp && /usr/java/default/bin/jar -xf /tmp/amo-gateway-${version}.jar;
      cp /tmp/resources/IntuitCustomerAssetABO.xsd  /apps/batch/flux/lib/resources;
      cp /tmp/resources/IntuitCustomerAssetABM.xsd  /apps/batch/flux/lib/resources;
      cp /tmp/resources/IntuitCustomerAsset.wsdl  /apps/batch/flux/lib/resources;
      cp /tmp/resources/CommonComponents.xsd  /apps/batch/flux/lib/resources;
      cp /tmp/resources/CustomerAccountBase.xsd  /apps/batch/flux/lib/resources;
      cp /tmp/resources/EntitlementServiceBase.xsd  /apps/batch/flux/lib/resources;
      cp /tmp/resources/IntuitBillingProfileABO.xsd  /apps/batch/flux/lib/resources;
      cp /tmp/resources/IntuitCustomerAsset_V1.wsdl  /apps/batch/flux/lib/resources;
    fi

    __log "Copying keystores on app servers";
    if  [ -e $CATALINA_HOME/key/psp.test.keystore.jks ]; then cp -f $CATALINA_HOME/key/psp.test.keystore.jks $libdir/psp/conf; chmod 400 $libdir/psp/conf/psp.test.keystore.jks; fi;
    if  [ -e $CATALINA_HOME/key/psp.test.truststore.jks ]; then cp -f $CATALINA_HOME/key/psp.test.truststore.jks $libdir/psp/conf; chmod 400 $libdir/psp/conf/psp.test.truststore.jks; fi;

    __log "Copying ach bank key files if they exist on this server";
    if [ -e $CATALINA_HOME/key/bank-test-prv-20141206.asc ]; then cp -f $CATALINA_HOME/key/bank-test-prv-20141206.asc $libdir/psp/conf; chmod 400 $libdir/psp/conf/bank-test-prv-20141206.asc; fi;
    if [ -e $CATALINA_HOME/key/bank-test-pub-20141206.asc ]; then cp -f $CATALINA_HOME/key/bank-test-pub-20141206.asc $libdir/psp/conf; chmod 400 $libdir/psp/conf/bank-test-pub-20141206.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-prod-prv-2017.asc ]; then cp -f $CATALINA_HOME/key/intu-prod-prv-2017.asc $libdir/psp/conf; chmod 400 $libdir/psp/conf/intu-prod-prv-2017.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-prod-pub-2017.asc ]; then cp -f $CATALINA_HOME/key/intu-prod-pub-2017.asc $libdir/psp/conf; chmod 400 $libdir/psp/conf/intu-prod-pub-2017.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-prod-prv-2019.asc ]; then cp -f $CATALINA_HOME/key/intu-prod-prv-2019.asc $libdir/psp/conf; chmod 400 $libdir/psp/conf/intu-prod-prv-2019.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-prod-pub-2019.asc ]; then cp -f $CATALINA_HOME/key/intu-prod-pub-2019.asc $libdir/psp/conf; chmod 400 $libdir/psp/conf/intu-prod-pub-2019.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-prod-prv-2015.asc ]; then cp -f $CATALINA_HOME/key/intu-prod-prv-2015.asc $libdir/psp/conf; chmod 400 $libdir/psp/conf/intu-prod-prv-2015.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-prod-pub-2015.asc ]; then cp -f $CATALINA_HOME/key/intu-prod-pub-2015.asc $libdir/psp/conf; chmod 400 $libdir/psp/conf/intu-prod-pub-2015.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-test-prv-20141206.asc ]; then cp -f $CATALINA_HOME/key/intu-test-prv-20141206.asc $libdir/psp/conf; chmod 400 $libdir/psp/conf/intu-test-prv-20141206.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-test-pub-20141206.asc ]; then cp -f $CATALINA_HOME/key/intu-test-pub-20141206.asc $libdir/psp/conf; chmod 400 $libdir/psp/conf/intu-test-pub-20141206.asc; fi;

    __log "Copying ach bank key files if they exist to /apps/batch/flux/etc to enable encryption for qa environments that run flux code on app servers";
    if [ -e $CATALINA_HOME/key/bank-test-prv-20141206.asc ]; then cp -f $CATALINA_HOME/key/bank-test-prv-20141206.asc /apps/batch/flux/etc; chmod 400 /apps/batch/flux/etc/bank-test-prv-20141206.asc; fi;
    if [ -e $CATALINA_HOME/key/bank-test-pub-20141206.asc ]; then cp -f $CATALINA_HOME/key/bank-test-pub-20141206.asc /apps/batch/flux/etc; chmod 400 /apps/batch/flux/etc/bank-test-pub-20141206.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-prod-prv-2017.asc ]; then cp -f $CATALINA_HOME/key/intu-prod-prv-2017.asc /apps/batch/flux/etc; chmod 400 /apps/batch/flux/etc/intu-prod-prv-2017.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-prod-pub-2017.asc ]; then cp -f $CATALINA_HOME/key/intu-prod-pub-2017.asc /apps/batch/flux/etc; chmod 400 /apps/batch/flux/etc/intu-prod-pub-2017.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-prod-prv-2019.asc ]; then cp -f $CATALINA_HOME/key/intu-prod-prv-2019.asc /apps/batch/flux/etc; chmod 400 /apps/batch/flux/etc/intu-prod-prv-2019.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-prod-pub-2019.asc ]; then cp -f $CATALINA_HOME/key/intu-prod-pub-2019.asc /apps/batch/flux/etc; chmod 400 /apps/batch/flux/etc/intu-prod-pub-2019.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-prod-prv-2015.asc ]; then cp -f $CATALINA_HOME/key/intu-prod-prv-2015.asc /apps/batch/flux/etc; chmod 400 /apps/batch/flux/etc/intu-prod-prv-2015.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-prod-pub-2015.asc ]; then cp -f $CATALINA_HOME/key/intu-prod-pub-2015.asc /apps/batch/flux/etc; chmod 400 /apps/batch/flux/etc/intu-prod-pub-2015.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-test-prv-20141206.asc ]; then cp -f $CATALINA_HOME/key/intu-test-prv-20141206.asc /apps/batch/flux/etc; chmod 400 /apps/batch/flux/etc/intu-test-prv-20141206.asc; fi;
    if [ -e $CATALINA_HOME/key/intu-test-pub-20141206.asc ]; then cp -f $CATALINA_HOME/key/intu-test-pub-20141206.asc /apps/batch/flux/etc; chmod 400 /apps/batch/flux/etc/intu-test-pub-20141206.asc; fi;

    __log "Unzipping app resources...";
    unzip -o ${artifact_directory}/psp-batchjobs-batchdeploy.zip -d /tmp/batchdeploy/ || exit 1;

    if [ -e /tmp/batchdeploy/lib/psp-iop-gateway-${version}.jar ]; then
      unzip -j -o /tmp/batchdeploy/lib/psp-iop-gateway-${version}.jar resources/EMSPManagerV1.wsdl -d /apps/batch/flux/etc || exit 1;
    else
      __log "Cannot extract EMSPManagerV1.wsdl";
      exit 1;
    fi;

    if [ -e /tmp/batchdeploy/lib/psp-iop-gateway-${version}.jar ]; then
      unzip -j -o /tmp/batchdeploy/lib/psp-iop-gateway-${version}.jar resources/EMSPManagerV1.xsd -d /apps/batch/flux/etc || exit 1;
    else
      __log "Cannot extract EMSPManagerV1.xsd";
      exit 1;
    fi;

    __log "Removing files from /tmp/batchdeploy/...";
    rm -Rf /tmp/batchdeploy/;

    __log "Adding jks file for LMA";
    if [[ $APP_ENV == "prd" ]]
    then
      cp ${keys_directory}/prod/Intuit.cto.gateway.prod.jks $libdir/psp/conf/Intuit.cto.gateway.prod.jks
      chmod 664 $libdir/psp/conf/Intuit.cto.gateway.prod.jks
    else
      cp ${keys_directory}/pre-prod/Intuit.cto.gateway.preProd.jks $libdir/psp/conf/Intuit.cto.gateway.preProd.jks
      chmod 664 $libdir/psp/conf/Intuit.cto.gateway.preProd.jks
    fi

    __log "Configuration file updates";
    sed -i 's_>url=_>_g' /usr/local/tomcat/lib/psp/conf/spcf-dac-conf.xml;
    sed -i 's_url=url=_url=_g' /usr/local/tomcat/lib/psp/conf/start-unsecured-flux-engine.fec;
    sed -i 's_>url=_>_g' /usr/local/tomcat/lib/psp/conf/psp-batchjobs-conf.xml;

    chmod 775 /usr/local/tomcat/lib/psp/ -R

    __log "Files in $webapps: $(ls -la $webapps)";
    __log "Files in $libdir: $(ls -la $libdir)";
    __log "Files in $libdir/psp: $(ls -la $libdir/psp)";

    __log "Files in $webapps: $(ls -la $webapps)";
    __log "Files in $libdir: $(ls -la $libdir)";
    __log "Files in $libdir/psp: $(ls -la $libdir/psp)";

    __log "[stage=copy_psp_configuration] Copy PSP configuration... COMPLETED";
}

create_jss_efs_directory_structure() {
  if [[ $APP_ENV == "ds2" ]] || [[ $APP_ENV == "stg" ]]
  then
    __log "[stage=create_jss_efs_directory_structure] Create JSS EFS Directory Structure for STG Env... START";
    sh /app/create-jss-efs-directory-structure.sh
    __log "[stage=create_jss_efs_directory_structure] Create JSS EFS Directory Structure for STG Env... End";
  fi
}

copy_psp_jss_configuration() {
  __log "[stage=copy_psp_configuration] Copy PSP configuration... START";

    __log "Unzipping ${artifact_directory}/psp-configuration.zip/psp-configuration/psp-configuration-${version}-$configenv-batch-conf.zip to /apps/batch/flux/etc";
    unzip -o -j ${artifact_directory}/psp-configuration.zip psp-configuration/psp-configuration-${version}-$configenv-batch-conf.zip -d /apps/batch/flux/etc/
    unzip -o /apps/batch/flux/etc/psp-configuration-${version}-$configenv-batch-conf.zip -d /apps/batch/flux/etc/
    rm /apps/batch/flux/etc/psp-configuration-${version}-$configenv-batch-conf.zip

    __log "Unzipping ${artifact_directory}/psp-batchjobs-batchdeploy-jss.zip to /apps/batch/jss/"
    unzip -o ${artifact_directory}/psp-batchjobs-batchdeploy-jss.zip -d /apps/batch/jss/

    __log "Copy spcf-meta-conf into /apps/batch/flux/etc/, /apps/batch/jss/shell/ and /usr/local/tomcat/bin/";
    mv /apps/batch/flux/etc/spcf-meta-conf.xml /apps/batch/flux/shell/
    ln -s /apps/batch/flux/shell/spcf-meta-conf.xml /apps/batch/jss/shell/spcf-meta-conf.xml
    cp /apps/batch/flux/shell/spcf-meta-conf.xml $libdir/../bin/spcf-meta-conf.xml;
    chmod 775  $libdir/../bin/spcf-meta-conf.xml;

    __log "Files in $webapps: $(ls -la $webapps)";
    __log "Files in $libdir: $(ls -la $libdir)";
    __log "Files in $libdir/psp: $(ls -la $libdir/psp)";

    __log "Unzipping ${artifact_directory}/batch-jobs-spcfdeploy.zip to /apps/spcf/lib";
    unzip -o ${artifact_directory}/batch-jobs-spcfdeploy.zip -d /apps/spcf/lib

    __log "Copying object-service-component-factory-conf.xml to /apps/batch/flux/shell/";
    mv /apps/batch/flux/etc/object-service-component-factory-conf.xml /apps/batch/flux/shell/

    __log "Creating required symlinks... START";
    rm -rf /apps/batch/flux/lib
    ln -s /apps/batch/jss/lib /apps/batch/flux/lib

    rm -rdf /apps/batch/jss/etc
    ln -s /apps/batch/flux/etc /apps/batch/jss/etc || exit 1;
	  ln -s /apps/batch/flux/shell/object-service-component-factory-conf.xml /apps/batch/jss/shell/object-service-component-factory-conf.xml || exit 1;

    rm -f $libdir/../bin/object-service-component-factory-conf.xml;
    ln -s /apps/batch/flux/shell/object-service-component-factory-conf.xml $libdir/../bin/object-service-component-factory-conf.xml;

    ln -s /apps/spcf/lib /apps/batch/jss/lib/spcf || exit 1;
    __log "Creating required symlinks... COMPLETED"

    __log "Giving executable permissions to shell scripts in /apps/batch/jss/shell/";
    chmod 754 /apps/batch/jss/shell/*.sh || exit 1;

    __log "Unzipping wsdl into /apps/batch/jss/etc";
    cd /tmp;

    unzip -j -o ${artifact_directory}/batch-jobs.war WEB-INF/lib/email-gateway-${version}.jar -d /tmp;
    unzip -j -o ${artifact_directory}/batch-jobs.war WEB-INF/lib/iop-gateway-${version}.jar -d /tmp;

    unzip -j -o /tmp/email-gateway-${version}.jar notification.wsdl -d /apps/batch/jss/etc || exit 1;
    /usr/java/default/bin/jar -xf iop-gateway-${version}.jar;
    cp /tmp/EMSPManagerV1.wsdl /apps/batch/jss/etc/EMSPManagerV1.wsdl || exit 1;
    cp /tmp/EMSPManagerV1.xsd /apps/batch/jss/etc/EMSPManagerV1.xsd || exit 1;

    __log "Adding jks file for LMA";
    if [[ $APP_ENV == "prd" ]]
    then
      cp ${keys_directory}/prod/Intuit.cto.gateway.prod.jks $libdir/psp/conf/Intuit.cto.gateway.prod.jks
      chmod 664 $libdir/psp/conf/Intuit.cto.gateway.prod.jks

      cp ${keys_directory}/prod/Intuit.cto.gateway.prod.jks /apps/batch/flux/etc/Intuit.cto.gateway.prod.jks
      chmod 664 /apps/batch/flux/etc/Intuit.cto.gateway.prod.jks
    else
      cp ${keys_directory}/pre-prod/Intuit.cto.gateway.preProd.jks $libdir/psp/conf/Intuit.cto.gateway.preProd.jks
      chmod 664 $libdir/psp/conf/Intuit.cto.gateway.preProd.jks

      cp ${keys_directory}/pre-prod/Intuit.cto.gateway.preProd.jks /apps/batch/flux/etc/Intuit.cto.gateway.preProd.jks
      chmod 664 /apps/batch/flux/etc/Intuit.cto.gateway.preProd.jks
    fi

    __log "Configuration file updates";
    sed -i 's_>url=_>_g' /apps/batch/jss/etc/spcf-dac-conf.xml;
	  sed -i 's_>url=_>_g' /apps/batch/jss/etc/psp-batchjobs-conf.xml;

    __log "Adding spring profile to /apps/batch/jss/shell/setenv.sh";
    int_spring_activeprofile "/apps/batch/jss/shell/setenv.sh"

    __log "Adding database configuration to /apps/batch/jss/shell/setenv.sh";
    init_database_configuration "/apps/batch/jss/shell/setenv.sh"

    __log "Copying ach bank key file";
    if [[ $APP_ENV == "prd" ]]
    then
      cp ${keys_directory}/prod/Intuit.ems.psp.prod.jks $libdir/psp/conf/Intuit.ems.psp.prod.jks
      chmod 664 $libdir/psp/conf/Intuit.ems.psp.prod.jks

      cp ${keys_directory}/prod/Intuit.ems.psp.prod.jks /apps/batch/flux/etc/Intuit.ems.psp.prod.jks
      chmod 0444 /apps/batch/flux/etc/Intuit.ems.psp.prod.jks
    else
      cp ${keys_directory}/pre-prod/Intuit.ems.psp.jks $libdir/psp/conf/Intuit.ems.psp.jks
      chmod 664 $libdir/psp/conf/Intuit.ems.psp.jks

      cp ${keys_directory}/pre-prod/Intuit.ems.psp.jks /apps/batch/flux/etc/Intuit.ems.psp.jks
      chmod 0444 /apps/batch/flux/etc/Intuit.ems.psp.jks
    fi

    __log "Files in $webapps: $(ls -la $webapps)";
    __log "Files in $libdir: $(ls -la $libdir)";
    __log "Files in $libdir/psp: $(ls -la $libdir/psp)";

  __log "[stage=copy_psp_configuration] Copy PSP configuration... COMPLETED";
}

download_psp_domain_jar() {
    __log "[stage=download_psp_domain_jar] Download PSP domain jar ... START";

    __log "Downloading psp-domain-$version.jar as psp-domain-0.1.0.jar in ${artifact_directory}";
    __download_this "https://artifact.intuit.com/nexus/service/local/artifact/maven/redirect?r=SBG.Payments.Intuit-$nexusdepot&g=com.intuit.sbg.psp&a=domain&v=$version&e=jar" ${artifact_directory}/psp-domain-0.1.0.jar

    __log "Copying psp-domain-0.1.0.jar to $libdir/psp";
    cp ${artifact_directory}/psp-domain-0.1.0.jar $libdir/psp

    __log "[stage=download_psp_domain_jar] Download PSP domain jar... COMPLETED";
}

configure_aspectJ() {
  __log "[stage=configure_aspectJ] aspectJ configuration....START";
#  if  [ "${APP_ENV}" == "qal" ] || [ "${APP_ENV}" == "e2e" ] ; then
    __log "[stage=configure_aspectJ] adding aspectjweaver.jar to -javaagent ... Start";

    aspectjweaver_jar="/app/contrast/javaagent/aspectjweaver.jar"
    JAVA_OPTS="${JAVA_OPTS} -javaagent:${aspectjweaver_jar}"

   __log "[stage=configure_aspectJ] adding aspectjweaver.jar to -javaagent ... End";
#  fi

  __log "[stage=configure_aspectJ] aspectJ configuration....COMPLETED";
}

configure_contrastassess_and_appd() {
  __log "[stage=configure_contrastassess_and_appd] appdynamics and contrastassess configuration....START";
  contrastassess_enabled=yes
  contrastassess_env=qal
  contrastassess_jar="/app/contrast/javaagent/contrast.jar"
  if [ "${contrastassess_enabled}" = "yes" ] && [ "${APP_ENV}" = "${contrastassess_env}" ]; then
    __log "[stage=configure_contrast_assess] Enabling contrast assess ... Start";

    JAVA_OPTS="${JAVA_OPTS} -javaagent:${contrastassess_jar}"
    JAVA_OPTS="${JAVA_OPTS} -Dcontrast.dir=/app/contrast/javaagent"
    JAVA_OPTS="${JAVA_OPTS} -Dcontrast.application.code=6847740825712235356"
    JAVA_OPTS="${JAVA_OPTS} -Dcontrast.application.name=payroll-dtpayroll-dt-payroll-svc"
    JAVA_OPTS="${JAVA_OPTS} -Dcontrast.inspect.allclasses=false -Dcontrast.process.codesources=false"
    JAVA_OPTS="${JAVA_OPTS} -Dcontrast.inventory.libraries=false"

   __log "[stage=configure_contrast_assess] Enabling contrast assess ... End";
  fi

  appdynamics_enabled=yes
  appdynamics_jar="/app/appdynamics/javaagent.jar"
  # Disable app-dynamics in qal to save license cost
  if ! [ "$( echo $APP_ENV | grep -E '(prf|qal)')" = "$APP_ENV" ] && [ "${appdynamics_enabled}" = "yes" ] && [ -r ${appdynamics_jar} ] && [ -f /etc/secrets/appd-account-access-key ]; then
      __log "[stage=configure_appd]  appdynamics configuration....START";
    export APPDYNAMICS_CONTROLLER_PORT=443
    export APPDYNAMICS_CONTROLLER_SSL_ENABLED=true

    export APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY=$(cat /etc/secrets/appd-account-access-key)

    JAVA_OPTS="$JAVA_OPTS -javaagent:${appdynamics_jar}"
    JAVA_OPTS="$JAVA_OPTS -Dappdynamics.agent.applicationName=${L1}-${L2}-sbg-psp-iks-${APP_ENV}"
    JAVA_OPTS="$JAVA_OPTS -Dappdynamics.agent.tierName=${APPDYNAMICS_AGENT_TIER_NAME}"
    JAVA_OPTS="$JAVA_OPTS -Dappdynamics.agent.nodeName=${APPDYNAMICS_AGENT_TIER_NAME}_${HOSTNAME}"

    __log "[stage=configure_appd] setting of java options for appdynamics....COMPLETED"
  fi

    export JAVA_OPTS=${JAVA_OPTS}
    echo -e '\nJAVA_OPTS="$JAVA_OPTS '$JAVA_OPTS'"' >> $CATALINA_HOME/bin/setenv.sh
    echo -e 'export JAVA_OPTS\n' >> $CATALINA_HOME/bin/setenv.sh

  __log "[stage=configure_contrastassess_and_appd] appdynamics and contrastassess configuration....COMPLETED";
}

int_spring_activeprofile() {
  __log "[stage=int_spring_activeprofile] Adding spring activeprofile system property to JAVA OPTS in the file '$1'... START";
  echo '' >> $1
  echo JAVA_OPTS=\"\$JAVA_OPTS -Dspring.profiles.active=$configenv\" >> $1
  echo 'export JAVA_OPTS' >> $1
  cat $CATALINA_HOME/bin/setenv.sh

  __log "[stage=int_spring_activeprofile] Adding spring activeprofile system property to JAVA OPTS ... COMPLETED";
}

init_database_configuration() {
  __log "[stage=init_database_configuration] Adding database configuration to JAVA OPTS in the file '$1', Monolith_DB: '$MONOLITH_DB', Audit_DB: '$AUDIT_DB' ... START";

  cat $CATALINA_HOME/bin/setenv.sh

  echo JAVA_OPTS=\"\$JAVA_OPTS -Ddatabase.monolith=$MONOLITH_DB\" >> $1
  echo JAVA_OPTS=\"\$JAVA_OPTS -Ddatabase.audit=$AUDIT_DB\" >> $1

  echo 'export JAVA_OPTS' >> $1
  cat $CATALINA_HOME/bin/setenv.sh

  __log "[stage=init_database_configuration] Adding database configuration to JAVA OPTS ... COMPLETED";
}

start_tomcat() {
    __log "[stage=start_tomcat] Starting tomcat...";
    int_spring_activeprofile "$CATALINA_HOME/bin/setenv.sh"
    init_database_configuration "$CATALINA_HOME/bin/setenv.sh"
    cd $CATALINA_HOME/bin;
    #replaces the bash with the command to be executed
    exec ./catalina.sh run
    __log "[stage=start_tomcat] Starting tomcat... Complete";
}


configuring_jvm_options() {
  __log "Configuring JAVA OPTIONS"
  # Configure log4j.configurationFile to load spcf-logger-conf.xml as default log config.
  # Reason: Some spring framework classes load logger instance before Application.startApplication
  if [ $machine_type == "JSS" ]; then
    JAVA_OPTS="$JAVA_OPTS -Dlog4j.configurationFile=file:///apps/batch/flux/etc/spcf-logger-conf.xml"
  else
    JAVA_OPTS="$JAVA_OPTS -Dlog4j.configurationFile=file:///usr/local/tomcat/lib/psp/conf/spcf-logger-conf.xml"
  fi

  #using slf4j for c3p0 logging
  JAVA_OPTS="$JAVA_OPTS -Dcom.mchange.v2.log.MLog=com.mchange.v2.log.slf4j.Slf4jMLog"

  #invalidating jvm dns cache
  JAVA_OPTS="$JAVA_OPTS -Dsun.net.inetaddr.ttl=10"

  # enabling log4j2 async logging
  #JAVA_OPTS="$JAVA_OPTS -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"
  #JAVA_OPTS="$JAVA_OPTS -Dlog4j2.asyncLoggerThreadNameStrategy=CACHED"
  #JAVA_OPTS="$JAVA_OPTS -Dlog4j2.asyncLoggerExceptionHandler=com.intuit.spc.foundations.primarySpecific.logging.SpcfLoggingExceptionHandler"

  # enable logger internal debug logs
  # JAVA_OPTS="$JAVA_OPTS -Dlog4j2.debug"

  export JAVA_OPTS=${JAVA_OPTS}
  echo -e '\nJAVA_OPTS="$JAVA_OPTS '$JAVA_OPTS'"' >> $CATALINA_HOME/bin/setenv.sh
  echo -e 'export JAVA_OPTS\n' >> $CATALINA_HOME/bin/setenv.sh

  __log "Configuring JAVA OPTIONS done."
}

verify_tomcat_startup() {
    __log "[stage=verify_tomcat_startup] verify tomcat startup... Started";
    STARTED_TEXT="org.apache.catalina.startup.Catalina.start Server startup in";
    LOGFILE="/usr/local/tomcat/logs/catalina.*.log";

    # look for the startup success logs
    timestamp=`grep "$STARTED_TEXT" $LOGFILE | tail -1`;
    success=0;
    for x in {0..360}; do
        __log "Waiting for \"$STARTED_TEXT\" to be written to $LOGFILE";
        line=`grep "$STARTED_TEXT" $LOGFILE | tail -1`;
        if [ "$line" != "" ] && [ "$line" != "$timestamp" ]; then
            success=1;
            __log $line;
            break;
        fi;
        sleep 5;
    done;

    if [ $success = 0 ]; then
        __log "[stage=start_tomcat] Starting tomcat... Failed";
        exit 1;
    fi;
    __log "[stage=verify_tomcat_startup] verify tomcat startup... Completed";
}

verify_app() {
    verify_tomcat_startup

    __log "[stage=verify_app] Verifying application deployment... Start";

    cd $webapps;

    sh /app/pspHealthCheck.sh

    # WHY THISS???
    success=0;
    for x in {0..24}; do
        sleep 5;

        if [ -f SAP.war ]; then
          __log "Verifying SAP...";
          wget -nv http://`uname -n`:8080/SAP/SAP.html -O /dev/null || continue;
        fi;

        if [ -f EWSAdapter.war ]; then
            __log "Verifying EWSwsdl";
            verify_full_health "EWS Adapter" "POST" "Content-type:text/xml" "http://`uname -n`:8080/EWSAdapter/services/EWSAdapter/v1_10" "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservices.v1_10.ews.adapters.psp.payroll.sbd.intuit.com/\"> <soapenv:Header/> <soapenv:Body> <web:Query_Account> <!--Optional:--> <QueryAccountRequest> <IpAddress>103.15.250.10</IpAddress> <DateTimeStamp>2019-03-13T23:37:45.180-07:00</DateTimeStamp> <Company> <EIN>201903134</EIN> </Company> <SubscriptionNumber>26519885</SubscriptionNumber> </QueryAccountRequest> </web:Query_Account> </soapenv:Body> </soapenv:Envelope>" "<Message>EIN Does Not Exist</Message>"

            wget -nv http://`uname -n`:8080/EWSAdapter/services/EWSAdapter/v1_9?wsdl -O /tmp/EWSwsdl.tmp || continue;
            grep 'xmlns:tns="http://webservices.v1_9.ews.adapters.psp.payroll.sbd.intuit.com/"' /tmp/EWSwsdl.tmp > /dev/null || continue;

            __log "Verifying EWSxsd...";
            wget -nv http://`uname -n`:8080/EWSAdapter/services/EWSAdapter/v1_9?xsd=1 -O /tmp/EWSxsd.tmp || continue;
            #Changed grep statement to remove xmlns:tns by Sakshi for resolving EWSAdapter issue
            #grep 'xmlns:tns="http://webservices.v1_9.ews.adapters.psp.payroll.sbd.intuit.com/"' /tmp/EWSxsd.tmp > /dev/null || continue;
            grep 'http://webservices.v1_9.ews.adapters.psp.payroll.sbd.intuit.com/' /tmp/EWSxsd.tmp > /dev/null || continue;
        fi;

        if [ -f payroll.war ]; then
            verify_full_health "QBDT Adapter" "POST" "Content-Type:application/xml" "http://`uname -n`:8080/payroll/payrollwebexchange.dll" "<OFX>
            <SIGNONMSGSRQV1>
            <SONRQ>
            <DTCLIENT>20080621231237
            <USERID>632004976
            <USERPASS>Test1234
            <LANGUAGE>ENG
            <APPVER>20.00.R.8/21011#accountant
            <APPID>QBWPRO
            <I.QBFILENAME>C:\QuickBooks Co Files\Joe Chick Test Co 123.QBW
            <I.QBFILEID>8c6dba598fc54a3ba650964013e43377
            <I.IPADDRESS>FileInfo:QB_data_engine_18:172.17.214.91#10180
            <I.QBUSERNAME>Admin
            </SONRQ>
            </SIGNONMSGSRQV1>
            <I.PAYROLLMSGSRQV1>
            <I.PAYROLLUPDATERQ>
            <TOKEN>^@~*
            <REJECTIFMISSING>N
            </I.PAYROLLUPDATERQ>
            </I.PAYROLLMSGSRQV1>
            </OFX>" "Payroll services are valid only when you are on a supported version of QuickBooks."

            __log "Verifying qbdt...";

            wget -nv http://`uname -n`:8080/payroll/payrollwebexchange.dll -O /tmp/QBDT.tmp || continue;
            grep "<OFX>" /tmp/QBDT.tmp > /dev/null || continue;
        fi;

        if [ -f qbdtws.war ]; then
            verify_full_health "QBDTWS Adapter" "POST" "Content-Type:text/xml" "http://`uname -n`:8080/qbdtws/services/BillPaymentWebServices" "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservices.qbdtws.adapters.psp.payroll.sbd.intuit.com/\"> <soapenv:Header/> <soapenv:Body> <web:QueryPaymentStatus> <!--Optional:--> <QueryBillPaymentStatusRequest> <PIN>632004976</PIN> <PSID>Test1234</PSID> <!--Optional:--> <BillPaymentIds> <!--Zero or more repetitions:--> <BillPaymentId>NA</BillPaymentId> </BillPaymentIds> </QueryBillPaymentStatusRequest> </web:QueryPaymentStatus> </soapenv:Body> </soapenv:Envelope>" "Company QBDT:Test1234 does not exist."

            __log "Verifying BillPaymentWebServices wsdl";
            wget -nv http://`uname -n`:8080/qbdtws/services/BillPaymentWebServices?wsdl -O /tmp/BillPaymentWebServicesWSDL.tmp || continue;
            grep 'targetNamespace="http://webservices.qbdtws.adapters.psp.payroll.sbd.intuit.com/"' /tmp/BillPaymentWebServicesWSDL.tmp > /dev/null || continue;

            __log "Verifying BillPaymentWebServices xsd";
            wget -nv http://`uname -n`:8080/qbdtws/services/BillPaymentWebServices?xsd=1 -O /tmp/BillPaymentWebServicesXSD.tmp || continue;
            grep 'targetNamespace="http://webservices.qbdtws.adapters.psp.payroll.sbd.intuit.com/"' /tmp/BillPaymentWebServicesXSD.tmp > /dev/null || continue;

            __log "Verifying QBPayrollWebServices wsdl";
            wget -nv http://`uname -n`:8080/qbdtws/services/QBPayrollWebServices?wsdl -O /tmp/QBPayrollWebServicesWSDL.tmp || continue;
            grep 'targetNamespace="http://webservices.qbdtws.adapters.psp.payroll.sbd.intuit.com/"' /tmp/QBPayrollWebServicesWSDL.tmp > /dev/null || continue;

            __log "Verifying QBPayrollWebServices xsd";
            wget -nv http://`uname -n`:8080/qbdtws/services/QBPayrollWebServices?xsd=1 -O /tmp/QBPayrollWebServicesXSD.tmp || continue;
            grep 'targetNamespace="http://webservices.qbdtws.adapters.psp.payroll.sbd.intuit.com/"' /tmp/QBPayrollWebServicesXSD.tmp > /dev/null || continue;
        fi;

        if [ -f BRMAdapter.war ]; then
            __log "Verifying BRMWebServices xsd...";

            verify_full_health "BRM Adapter" "POST" "Content-type:text/xml" "http://`uname -n`:8080/BRMAdapter/services/BRMWebServices?wsdl" "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservices.brm.adapters.psp.payroll.sbd.intuit.com/\"><soapenv:Header/><soapenv:Body><web:getUsageBillingEmployeeDetails><GetUsageBillingDetailRequest><BillDate>01/04/2018</BillDate><CompanyID>103058084</CompanyID><EIN>431898294</EIN><ViewAll>false</ViewAll></GetUsageBillingDetailRequest></web:getUsageBillingEmployeeDetails></soapenv:Body></soapenv:Envelope>" "<NumCompaniesBilled>0</NumCompaniesBilled>"

            wget -nv http://`uname -n`:8080/BRMAdapter/services/BRMWebServices?xsd=1 -O /tmp/BRMWebServicesXSD.tmp || continue;
            grep 'targetNamespace="http://webservices.brm.adapters.psp.payroll.sbd.intuit.com/"' /tmp/BRMWebServicesXSD.tmp > /dev/null || continue;
        fi;

        if [ -f Keynote-Adapter.war ]; then
            __log "Validating keynote adapter...";
            verify_full_health "KeyNote Adapter" "POST" "Content-type:text/xml" "http://`uname -n`:8080/Keynote-Adapter/services/KeynoteWS" "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservices.keynote.adapters.psp.payroll.sbd.com/\"> <soapenv:Header/> <soapenv:Body> <web:KeynoteValidation> <!--Optional:--> <KeynoteRequest> <PIN>Test1234</PIN> <PSID>632004976</PSID> </KeynoteRequest> </web:KeynoteValidation> </soapenv:Body> </soapenv:Envelope>" "<Message>PIN not recognized. Account is now locked. Please try again in 15 minutes.</Message>"

            wget -nv http://`uname -n`:8080/Keynote-Adapter/services/KeynoteWS?wsdl -O /tmp/Keynote-AdapterWSDL.tmp > /dev/null || continue;
        fi;

        if [ -f ddrepui-ws.war ]; then
            __log "Verifying ddrepui-ws...";
            wget -nv http://`uname -n`:8080/ddrepui-ws/services -O /dev/null || continue;
        fi;

        if [ -f ptcadapter.war ]; then
            verify_full_health "PTC Adapter" "POST" "Content-type:text/xml" "http://`uname -n`:8080/ptcadapter/services/PTCWebServices" "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservices.ptc.adapters.psp.payroll.sbd.intuit.com/\"> <soapenv:Header/> <soapenv:Body> <web:getPSIDForEIN> <!--Optional:--> <sourceSystemCode>QBDT</sourceSystemCode> <!--Optional:--> <EIN>593020964</EIN> </web:getPSIDForEIN> </soapenv:Body> </soapenv:Envelope>" "<PSID>312014199</PSID>"
        fi;

        if [ -f CdmAdapter.war ]; then
            verify_full_health "CDM Adapter" "GET" "Content-Type:application/json" "http://`uname -n`:8080/CdmAdapter/resource/v1/users/123146532348059/payrollemployees" "" "Aleksandr Y Arutyunov"
        fi;

        if [ -f DISAdapter.war ]; then
            verify_full_health "DIS Adapter" "POST" "Content-type:text/xml" "http://`uname -n`:8080/DISAdapter/services/DISAdapterService/v1_8" "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://v1_8.dis.adapters.psp.payroll.sbd.intuit.com/\"> <soapenv:Header/> <soapenv:Body> <v1:GetAgencyRules> <!--Optional:--> <GetAgencyRulesRequest/> </v1:GetAgencyRules> </soapenv:Body> </soapenv:Envelope>" "<PaymentTemplateId>IRS-940-PAYMENT</PaymentTemplateId>"
        fi;

#            if [ -f DataAdapter.war ]; then
#                verify_full_health "Data Adapter - Ratable" "POST" "Content-Type:application/json" "http://`uname -n`:8080/DataAdapter/rest/ratable/v1/licenseInfo" "{ \"QBLicense\": \"6206-4827-4850-254\", \"SKU\": \"Pro\", \"Source\": \"install_full\", \"MajorVersion\": \"25\", \"MinorVersion\": \"07\" }" "<QBLicense>6206-4827-4850-254</QBLicense>"
#            fi;

        if [ -f cep.war ]; then
            verify_full_health "ADE Adapter" "GET" "Accept:application/json" "http://`uname -n`:8080/cep/rest/v1/companies/606022619/taxsetup/taxitems/US_AZ_SC_ER_JTT" "" "AZ Job Training Tax"
        fi;

        success=1;
        break;
    done;

    __log "Cleanup tmp files...";
    rm -f /tmp/EWSwsdl.tmp /tmp/EWSxsd.tmp /tmp/QBDT.tmp /tmp/BillPaymentWebServicesWSDL.tmp /tmp/BillPaymentWebServicesXSD.tmp /tmp/QBPayrollWebServicesWSDL.tmp /tmp/QBPayrollWebServicesXSD.tmp;

    if [ $success = 0 ]; then
        __log "Error verifying app servers";
#            exit 1;
    fi;

    __log "[stage=verify_app] Verifying application deployment... Complete";
}

verify_full_health() {
    __log "Verifying full health...";

    app_name=$1
    type=$2
    headers=$3
    url=$4
    request=$5
    expected_response=$6
    unset response

    __log "Verifying $app_name endpoint - $url"
    __log "Expected - $expected_response"

    response=$(curl -sS -w "\n%{http_code}" -X $type -H $headers $url -d "$request" )
    response=(${response[@]}) # convert to array
    response_code=${response[${#response[@]}-1]} # last element
    response_body=${response[@]::${#response[@]}-1} # get all elements except last

    __log "Actual response code - $response_code"
    __log "Actual response body - $response_body"

    if [[ $response_code = "200" ]]; then
        __log "appName=$app_name - response_status_code_validation=successful"
    else
        __log "appName=$app_name - response_status_code_validation=failed"
    fi

    if [[ $response_body =~ $expected_response ]]; then
        __log "appName=$app_name - response_body_validation=successful"
    else
        __log "appName=$app_name - response_body_validation=failed"
    fi

    __log "Verifying full health...Done!";
}



__log "START deployment script"
set_machine_type
set_version
set_nexusdepot
if [ $machine_type == "APP" ]; then
  copy_psp_artifacts
  copy_psp_app_configuration
  download_psp_domain_jar
  configure_contrastassess_and_appd
  configure_aspectJ
  configuring_jvm_options
  verify_app &
  start_tomcat
else
  create_jss_efs_directory_structure
  copy_psp_artifacts
  copy_psp_jss_configuration
  setup_printer_config
  configure_contrastassess_and_appd
  configure_aspectJ
  configuring_jvm_options
  start_tomcat
fi
__log "END deployment script"

