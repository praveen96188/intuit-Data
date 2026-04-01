#!/bin/sh

LOCAL_SETUP_START_TIME=$SECONDS

# load shell config file

if [[ `echo $SHELL | grep -o "zsh"| wc -l` -ne "0" ]]
then
  export RC_FILE="$HOME/.zprofile"
  echo $RC_FILE
elif [[ `echo $SHELL | grep -o "bash"| wc -l` -ne "0" ]]
then
  export RC_FILE="$HOME/.bash_profile"
  echo $RC_FILE
fi
if [ ! -e $RC_FILE ]
then
  touch $RC_FILE
fi
source $RC_FILE
sudo mkdir -p ~/Downloads/binaries
sudo chmod -R 777 ~/Downloads/binaries
sudo chmod +x ./dev/setup_local/install_java.sh
sudo chmod +x ./dev/setup_local/install_maven.sh
sudo chmod +x ./dev/setup_local/install_sqlplus.sh
sudo chmod +x ./dev/setup_local/install_tomcat.sh
sudo chmod +x ./dev/deploy_local/add_secondary_user.sh
sudo chmod +x ./dev/deploy_local/start_oracle.sh
sudo chmod +x ./dev/deploy_local/start_colima.sh

./dev/setup_local/install_java.sh
./dev/setup_local/install_maven.sh

source $RC_FILE

HOSTNAME=`hostname`
if [ `grep -i "127.0.0.1\s*localhost\s*$HOSTNAME" /etc/hosts |wc -l` = "0" ]
then
  echo "127.0.0.1 localhost $HOSTNAME" | sudo tee -a /etc/hosts
fi

if [ -z "$CATALINA_HOME" ]
then
  ./dev/setup_local/install_tomcat.sh
  source $RC_FILE
fi
sudo chmod -R 777 $CATALINA_HOME

skipDB=0
skipMvn=0
skipSQL=0

# Disables macOS gatekeeper. Gatekeeper is re-enabled at the end of this script.
# This is required to run sqlplus since gatekeeper prevents applications downloaded outside of the App Store from running.
sudo spctl --master-disable


while [ -n "$1" ]
do
  case "$1" in
    "-s"|"--skip")
      param="$2"
      if [ -z "$2" ]
      then
        echo "No parameter specified for $1"
        break
      fi
      IFS=','
      read -ra skipArray <<< "$param"
      for i in "${skipArray[@]}"
      do
        if [ "$i" = "db" ]
        then
          skipDB=1
          echo "Database setup will be skipped."
        elif [ "$i" = "mvn" ]
        then
          skipMvn=1
          echo "mvn build will be skipped."
        elif [ "$i" = "sql" ]
        then
          echo "SQLPLUS installation will be skipped."
          skipSQL=1
        fi
      done
      shift
      ;;
    "-h"|"--help")
      echo ""
      echo "Perform local tomcat config changes, bring up the oracle database and install sqlplus."
      echo ""
      echo "Options:"
      echo "  -s, --skip string   Used to pass a string of comma separated values that specifies the steps to be skipped."
      echo "                      db:  To skip database setup"
      echo "                      mvn: To skip mvn install"
      echo "                      sql: To skip SQLPLUS installation"
      echo ""
      echo "  -h, --help          Help for start_local.sh"
      echo ""
      echo "Sample usage:-"
      echo "./start_local.sh --skip db,mvn,sql #skips database setup, SQLPLUS installation and mvn install."
      echo "./start_local.sh --skip db         #skips database setup."
      echo "./start_local.sh -s mvn            #skips mvn install."
      echo ""
      exit
      ;;
    *)
      echo "Unknown option: $1"
      exit
  esac
  shift
done


if [ -z "$CATALINA_HOME" ]; then
    echo "[ERROR] Please set Tomcat Home Directory path as CATALINA_HOME environment variable!!"
    exit 0
fi
if [ $skipSQL = 0 ]
then
  echo "SQLPLUS installation in progress..."
  SQLPLUS_START_TIME=$SECONDS
  ./dev/setup_local/install_sqlplus.sh
  source $RC_FILE
  SQLPLUS_ELAPSED_TIME=$(($SECONDS - $SQLPLUS_START_TIME))
  echo "SQLPLUS installation completed in $SQLPLUS_ELAPSED_TIME seconds."
else
  echo "Skipped SQLPLUS installation."
fi

# Bring up the oracle-db
if [ $skipDB = 0 ]
then
  echo "Setting up oracle database..."
  ORACLE_START_TIME=$SECONDS
  if [[ $(arch) == 'arm64' ]]; then
    echo "Running oracle setup for M1 Mac..."
    ./dev/deploy_local/start_colima.sh
  else
    echo "Running oracle setup for Intel Mac..."
    ./dev/deploy_local/start_oracle.sh
  fi
  source $RC_FILE
  ORACLE_ELAPSED_TIME=$(($SECONDS - $ORACLE_START_TIME))
  echo "Database setup completed in $ORACLE_ELAPSED_TIME seconds."
else
  echo "Skipped database setup."
fi

LOCALCONFIG_START_TIME=$SECONDS
# Create local variables
WEBAPPS="${CATALINA_HOME}/webapps"
TOMCAT_BIN="${CATALINA_HOME}/bin/"
WORKING_DIR="$PWD"
LOCAL_TEST_CONF=$WORKING_DIR/app/PSE/configuration/src/main/resources/local/test-conf
BATCH_JOB=$WORKING_DIR/app/PSE/batch-jobs/
PSP_LOCAL_KEYS_CONF=${LOCAL_TEST_CONF}/psp-keys-conf.xml
PSP_LOCAL_SPCF_META_CONF=${LOCAL_TEST_CONF}/spcf-meta-conf.xml
PSP_LOCAL_COMMON_CONF=${LOCAL_TEST_CONF}/psp-common-conf.xml
tomcat_psp_keys_conf_file=${TOMCAT_BIN}psp-keys-conf.xml
tomcat_spcf_meta_conf_file=${TOMCAT_BIN}spcf-meta-conf.xml
batchjob_spcf_meta_conf_file=${BATCH_JOB}spcf-meta-conf.xml

# PSP keys
LOCAL_TEMPLATES_FOLDER=$WORKING_DIR/app/PSE/configuration/src/main/resources/templates
PSP_KEY=${LOCAL_TEMPLATES_FOLDER}/key_v2-b95a8179f0779.pem
PSP_KEY2=${LOCAL_TEMPLATES_FOLDER}/key_v2-e3b373bd2284e.pem
PSP_JKS_FILE=${LOCAL_TEMPLATES_FOLDER}/Intuit.cto.gateway.preProd.jks

# Artifact to be deployed. For eg. CdmAdapter
artifacts_directory=$WORKING_DIR/app/artifacts/wars/
# TODO change this to deploy a different artifact.
artifact_name="CdmAdapter"

# skip DB install
touch $WORKING_DIR/app/PSE/domain/skip-install-db.txt
touch $WORKING_DIR/app/PSE/domain-secondary/skip-install-db.txt

# update test-tools-ui pom path
testtools_ui_pom_file=$WORKING_DIR/app/UI/test-tools-ui/pom.xml
sed -i '' 's/\${main\.dir}\/Adapters/\${main\.dir}\/app\/Adapters/g' $testtools_ui_pom_file

if [ $skipMvn = 0 ]
then
  mvn clean install -DskipTests
else
  echo "Skipped mvn clean install."
fi

echo LOCAL BUILD COMPLETED!

if [ ! -f "$PSP_LOCAL_KEYS_CONF" ]; then
    echo "Please ensure local maven build completed successfully!"
    exit 0
fi

#copy local/test-conf/psp-keys-conf.xml to TOMCAT_HOME/bin/
cp $PSP_LOCAL_KEYS_CONF $TOMCAT_BIN

# change 'working.dir' to [project-folder]/app for psp_idps_api_secret_key - give complete path
sed -i '' 's|working\.dir|'"${WORKING_DIR}/app"'|g' $tomcat_psp_keys_conf_file

#copy local/test-conf/spcf-meta-conf to TOMCAT_HOME/bin/
cp $PSP_LOCAL_SPCF_META_CONF $TOMCAT_BIN

# In TOMCAT_HOME/bin/spcf-meta-conf, provide path for 'PSP-Keys' module as TOMCAT_HOME/bin/psp-keys-conf.xml
sed -i '' 's|working.dir/PSE/configuration/src/main/resources/local/test-conf/psp-keys-conf.xml|'"${tomcat_psp_keys_conf_file}"'|g' $tomcat_spcf_meta_conf_file
sed -i '' 's|working\.dir|'"${WORKING_DIR}/app"'|g' $tomcat_spcf_meta_conf_file

#copy key..779.pem to to TOMCAT_HOME/bin/
cp $PSP_KEY $TOMCAT_BIN

#copy key..284.pem to /tmp/
cp $PSP_KEY2 /tmp

# in app/PSE/configuration/src/main/resources/local/test-conf/psp-common-conf.xml file, replace value for lma.keystore.path with actual path of Intuit.cto.gateway.preProd.jks file
sed -i '' 's|/usr/local/tomcat/lib/psp/conf/Intuit.cto.gateway.preProd.jks|'"${PSP_JKS_FILE}"'|g' $PSP_LOCAL_COMMON_CONF

# copy war file from app/artifacts/wars/batch-jobs.war to TOMCAT_HOME/webapps/batch-jobs.war
cp ${artifacts_directory}${artifact_name}*.war ${WEBAPPS}/${artifact_name}.war

# setup for local batch-job
cp $tomcat_spcf_meta_conf_file $batchjob_spcf_meta_conf_file
cp $tomcat_spcf_meta_conf_file $WORKING_DIR/app/
cp $PSP_KEY $BATCH_JOB
cp $PSP_KEY2 $BATCH_JOB

echo ====================================================================
echo Now Let us setup Local Tomcat
echo "1. Click Add Configuration in IntelijIdea, and setup Tomcat (locate Tomcat Home)"
echo "2. Add VM option for tomcat -Dspring.profiles.active=dev"
echo "3. Pass env variable in Tomcat intellij for both RUN & DEBUG: Click '+' & set CATALINA_BASE=/path/to/local/apache-tomcat"
echo ====================================================================
LOCALCONFIG_ELAPSED_TIME=$(($SECONDS-$LOCALCONFIG_START_TIME))
echo "Local configuration completed in $LOCALCONFIG_ELAPSED_TIME seconds."
sudo spctl --master-enable
LOCAL_SETUP_ELAPSED_TIME=$(($SECONDS-$LOCAL_SETUP_START_TIME))
echo "Total time taken for local setup is $LOCAL_SETUP_ELAPSED_TIME seconds."
