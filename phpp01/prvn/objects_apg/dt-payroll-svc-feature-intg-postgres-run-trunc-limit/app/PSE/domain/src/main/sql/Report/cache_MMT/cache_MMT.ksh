#!/bin/ksh 
set -x
echo "running cache_MMT.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect / as sysdba
@cache_MMT.sql
EOF

chmod 600 $LOG_DIR/cache_MMT.log
cat $LOG_DIR/cache_MMT.log |mailx -s "cache MMT" allen_chaves@intuit.com raffi_norian@intuit.com david_fitzjarrell@intuit.com
