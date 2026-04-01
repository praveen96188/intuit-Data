#!/bin/sh
source /home/oracle/.bashrc

echo `date`

export WORKING_DIR=/home/oracle/install

cd $WORKING_DIR

echo "Currently in $WORKING_DIR"

unzip psp-dbinstall-${ARTIFACT_VERSION}.zip -d psp-dbinstall

# Create PSP_LOCAL Schema
echo "Setup PSP_LOCAL schema"
sqlplus / as sysdba @User.sql PSP_LOCAL NON_DBA
echo "PSP_LOCAL succesfully setup"

cd $WORKING_DIR/psp-dbinstall

# Create all schema objects and populate data
echo "Creating PSP_LOCAL Primary schema"
sqlplus PSP_LOCAL/PSP_LOCAL @InstallDB.sql NONE NONE NONE NONE NONE
echo "PSP_LOCAL Primary schema created successfully"

# Populate SAP user data
exit | sqlplus PSP_LOCAL/PSP_LOCAL @SAP_InsertTestUsers.sql
echo "SAP users created successfully"

cd $WORKING_DIR
unzip psp-dbinstall-secondary-${ARTIFACT_VERSION}.zip -d psp-dbinstall-secondary

# Create PSP_LOCAL1 Schema
echo "Setup PSP_LOCAL1 schema"
sqlplus / as sysdba @User.sql PSP_LOCAL1 NON_DBA
echo "PSP_LOCAL1 succesfully setup"

cd $WORKING_DIR/psp-dbinstall-secondary

echo "Creating PSP_LOCAL1 Secondary schema"
sqlplus PSP_LOCAL1/PSP_LOCAL1 @InstallDB.sql NONE NONE NONE NONE NONE
echo "PSP_LOCAL1 Secondary schema created successfully"
