#!/bin/ksh 
set -x
echo "running follow_fed_frequency_issue_search.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect / as sysdba
set pages 200 lines 120
spool follow_fed_frequency_issue_search.log
@follow_fed_frequency_issue_search.sql
spool off
EOF

chmod 600 $LOG_DIR/follow_fed_frequency_issue_search.log
cat $LOG_DIR/follow_fed_frequency_issue_search.log |mailx -s "follow_fed_frequency_issue_search" psp_tax_payment_notify_prod@intuit.com
