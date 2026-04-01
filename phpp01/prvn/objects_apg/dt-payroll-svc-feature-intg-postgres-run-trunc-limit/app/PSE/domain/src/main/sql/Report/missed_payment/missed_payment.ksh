#!/bin/ksh 
set -x
echo "running missed_payments_fed_state.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect / as sysdba
@missed_payments_fed_state.sql
EOF

chmod 600 $LOG_DIR/missed_payment.log
cat $LOG_DIR/missed_payment.log |mailx -s "missed_payments" psp_tax_payment_notify_prod@intuit.com
