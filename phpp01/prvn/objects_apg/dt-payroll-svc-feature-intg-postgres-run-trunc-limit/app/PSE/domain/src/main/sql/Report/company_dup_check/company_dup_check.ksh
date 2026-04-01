#!/bin/ksh 
set -x
echo "running company_dup_check.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect / as sysdba
@company_dup_check.sql
EOF

chmod 600 $LOG_DIR/company_dup_check.log

if
  test -s company_dup_check.log
then
  cat $LOG_DIR/company_dup_check.log | mailx -s "Company Dup Check" David_Weinberg@intuit.com Linda_Ferguson@Intuit.com
else
  cat $LOG_DIR/company_dup_check.log | mailx -s "Company Dup Check - no duplicates" yichen_li@intuit.com
fi

