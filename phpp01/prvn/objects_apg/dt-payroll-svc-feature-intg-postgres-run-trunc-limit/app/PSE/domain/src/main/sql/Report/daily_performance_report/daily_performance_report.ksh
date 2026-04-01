#!/bin/ksh 
set -x
echo "running daily_performance_report.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect / as sysdba
@daily_performance_report.sql
EOF

chmod 600 $LOG_DIR/daily_performance_report.log
cat $LOG_DIR/daily_performance_report.log |mailx -s "daily_performance_report" Raffi_Norian@intuit.com Allen_Chaves@intuit.com Mark_Dunn@intuit.com
