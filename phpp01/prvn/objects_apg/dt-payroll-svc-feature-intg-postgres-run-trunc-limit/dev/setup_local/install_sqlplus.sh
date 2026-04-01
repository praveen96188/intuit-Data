#!/bin/sh

echo "RC FILE IS $RC_FILE"

INSTANT_CLIENT_FOLDER=/opt/oracle/instantclient_12_1

if [[ -d $INSTANT_CLIENT_FOLDER ]]
then
  read -p "Instantclient found in  $INSTANT_CLIENT_FOLDER . Do you wish to continue with sqlplus installation (y/n)? Default is n." replace_instant_client
  if [ "$replace_instant_client" != "y" ]
  then
    echo "Skipping sqlplus installation..."
    exit 0
  fi
fi

DT_PAYROLL_SVC_DIR=$(pwd)

INSTANT_CLIENT_ZIP_LOCATION="$DT_PAYROLL_SVC_DIR/dev/setup_local/tmp"
mkdir -p $INSTANT_CLIENT_ZIP_LOCATION
echo "Downloading instantclient-basic-macos.x64-12.1.0.2.0.zip"
sudo curl -o "$INSTANT_CLIENT_ZIP_LOCATION/instantclient-basic-macos.x64-12.1.0.2.0.zip" https://artifact.intuit.com/artifactory/generic-local/payroll-dtpayroll/dt-payroll-svc/localsetup/instantclient-basic-macos.x64-12.1.0.2.0.zip
echo "Downloading instantclient-sdk-macos.x64-12.1.0.2.0.zip"
sudo curl -o "$INSTANT_CLIENT_ZIP_LOCATION/instantclient-sdk-macos.x64-12.1.0.2.0.zip" https://artifact.intuit.com/artifactory/generic-local/payroll-dtpayroll/dt-payroll-svc/localsetup/instantclient-sdk-macos.x64-12.1.0.2.0.zip
echo "Downloading instantclient-sqlplus-macos.x64-12.1.0.2.0.zip"
sudo curl -o "$INSTANT_CLIENT_ZIP_LOCATION/instantclient-sqlplus-macos.x64-12.1.0.2.0.zip" https://artifact.intuit.com/artifactory/generic-local/payroll-dtpayroll/dt-payroll-svc/localsetup/instantclient-sqlplus-macos.x64-12.1.0.2.0.zip

sudo rm -rf /opt/oracle/
sudo mkdir -p /opt/oracle
sudo unzip $INSTANT_CLIENT_ZIP_LOCATION/instantclient-\*.zip -d /opt/oracle/

cd /opt/oracle
if [[ -d $INSTANT_CLIENT_FOLDER ]]
then
  echo "Instantclient unzipped to  $INSTANT_CLIENT_FOLDER"
  sudo rm -rf $INSTANT_CLIENT_ZIP_LOCATION
else
  echo "Error in unzipping file."
  exit 1
fi

# oracle
mkdir -p $HOME/etc
source $RC_FILE

if [ "$ORACLE_HOME" != "/opt/oracle/instantclient_12_1" ]
then
  echo "export ORACLE_HOME=/opt/oracle/instantclient_12_1" >>$RC_FILE
fi
if [ "$DYLD_LIBRARY_PATH" != "$ORACLE_HOME" ]
then
  echo "export DYLD_LIBRARY_PATH=\$ORACLE_HOME">>$RC_FILE
fi
if [ "$LD_LIBRARY_PATH" != "$ORACLE_HOME" ]
then
  echo "export LD_LIBRARY_PATH=\$ORACLE_HOME" >>$RC_FILE
fi


case ":$PATH:" in
  *:$ORACLE_HOME:*) echo "PATH already has $ORACLE_HOME";;
  *) echo "export PATH=\$PATH:\$ORACLE_HOME" >>$RC_FILE ;;
esac

if [ "$TNSNAMES" != "$HOME/etc" ]
then
  echo "export TNSNAMES=\$HOME/etc" >>$RC_FILE
fi

if [ "$TNS_ADMIN" != "$HOME/etc" ]
then
  echo "export TNS_ADMIN=\$HOME/etc" >>$RC_FILE
fi

source $RC_FILE

echo "SQLPLUS installation completed."

cd $DT_PAYROLL_SVC_DIR

echo "Creating tnsnames.ora ..."
touch "$TNSNAMES/tnsnames.ora"
cp "$DT_PAYROLL_SVC_DIR/dev/setup_local/localsetup.ora" "$TNSNAMES/tnsnames.ora"
echo "Done."