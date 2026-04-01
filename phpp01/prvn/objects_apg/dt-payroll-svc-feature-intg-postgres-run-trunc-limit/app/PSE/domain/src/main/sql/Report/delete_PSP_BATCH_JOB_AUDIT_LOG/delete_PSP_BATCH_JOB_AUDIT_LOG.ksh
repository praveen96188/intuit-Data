#!/bin/ksh 
. /l/pspprod1
set -x
echo "running dbupgrade_PSRV001106b.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
##export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
##export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect pspadm/G0od!E4ts
@dbupgrade_PSRV001106b.sql
@delete_payroll_fraud_batch.sql
EOF

chmod 600 $LOG_DIR/dbupgrade_PSRV001106b.log
chmod 600 $LOG_DIR/delete_payroll_fraud_batch.log
cat $LOG_DIR/dbupgrade_PSRV001106b.log |mailx -s "PSPADM.PSP_BATCH_JOB_AUDIT_LOG purge" yichen_li@intuit.com allen_chaves@intuit.com raffi_norian@intuit.com david_fitzjarrell@intuit.com
cat $LOG_DIR/delete_payroll_fraud_batch.log |mailx -s "PSPADM.PSP_PAYROLL_FRAUD_BATCH pruge" yichen_li@intuit.com allen_chaves@intuit.com raffi_norian@intuit.com david_fitzjarrell@intuit.com
