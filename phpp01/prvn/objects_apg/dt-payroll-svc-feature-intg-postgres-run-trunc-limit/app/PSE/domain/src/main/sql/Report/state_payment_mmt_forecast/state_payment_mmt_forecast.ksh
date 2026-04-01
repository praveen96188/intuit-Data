#!/bin/ksh 
set -x
echo "running state_payment_mmt_forecast.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect / as sysdba
set pages 200 lines 120
spool state_payment_mmt_forecast.log
@state_payment_mmt_forecast.sql
spool off
EOF

chmod 600 $LOG_DIR/state_payment_mmt_forecast.log
cat $LOG_DIR/state_payment_mmt_forecast.log |mailx -s "state_payment_mmt_forecast" psp_tax_payment_notify_prod@intuit.com
