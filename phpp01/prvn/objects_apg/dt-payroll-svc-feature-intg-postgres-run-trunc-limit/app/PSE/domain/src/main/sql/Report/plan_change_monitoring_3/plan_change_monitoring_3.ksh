#!/bin/ksh 
set -x
echo "running plan_change_monitoring_3.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod3"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect / as sysdba
set pages 200 lines 120
spool plan_change_monitoring_3.log
@plan_change_monitoring_3.sql
spool off
EOF

chmod 600 $LOG_DIR/plan_change_monitoring_3.log
cat $LOG_DIR/plan_change_monitoring_3.log |mailx -s "Execution plan change" allen_chaves@intuit.com raffi_norian@intuit.com david_fitzjarrell@intuit.com
