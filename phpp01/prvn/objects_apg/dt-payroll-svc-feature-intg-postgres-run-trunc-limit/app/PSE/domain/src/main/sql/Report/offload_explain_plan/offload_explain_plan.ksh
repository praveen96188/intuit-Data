#!/bin/ksh 
set -x
echo "running offload_explain_plan.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect / as sysdba
@offload_explain_plan.sql
EOF

chmod 600 $LOG_DIR/offload_explain_plan_*.log
cat $LOG_DIR/offload_explain_plan_mmt.log |mailx -s "offload_explain_plan_mmt" allen_chaves@intuit.com raffi_norian@intuit.com david_fitzjarrell@intuit.com
cat $LOG_DIR/offload_explain_plan_edr.log |mailx -s "offload_explain_plan_edr" allen_chaves@intuit.com raffi_norian@intuit.com david_fitzjarrell@intuit.com
cat $LOG_DIR/offload_explain_plan_ft.log |mailx -s "offload_explain_plan_ft" allen_chaves@intuit.com raffi_norian@intuit.com david_fitzjarrell@intuit.com
