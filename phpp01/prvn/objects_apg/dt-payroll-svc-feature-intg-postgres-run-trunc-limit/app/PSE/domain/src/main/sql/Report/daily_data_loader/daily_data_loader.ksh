#!/bin/ksh 
set -x
echo "running daily_data_loader.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect / as sysdba
@daily_data_loader.sql
EOF

chmod 600 $LOG_DIR/daily_data_loader.log
cat $LOG_DIR/daily_data_loader.log |mailx -s "daily_data_loader" yichen_li@intuit.com
