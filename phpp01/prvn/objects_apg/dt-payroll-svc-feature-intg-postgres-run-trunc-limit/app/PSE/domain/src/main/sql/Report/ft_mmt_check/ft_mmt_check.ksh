#!/bin/ksh 
set -x
echo "running ft_mmt_check.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect / as sysdba
@ft_mmt_check.sql
@mmt_check.sql
EOF

chmod 600 $LOG_DIR/ft_mmt_check.log
chmod 600 $LOG_DIR/mmt_check.log
cat $LOG_DIR/ft_mmt_check.log |mailx -s "ft_mmt_check" psp_tax_payment_notify_prod@intuit.com
cat $LOG_DIR/mmt_check.log |mailx -s "mmt_check" psp_tax_payment_notify_prod@intuit.com
