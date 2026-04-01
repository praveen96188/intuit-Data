#!/bin/ksh 
set -x
echo "running dbupgrade_PSRV001106c.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect pspadm/G0od!E4ts
@dbupgrade_PSRV001106c.sql
EOF

chmod 600 $LOG_DIR/dbupgrade_PSRV001106c.log
cat $LOG_DIR/dbupgrade_PSRV001106c.log |mailx -s "PSPADM.PSP_SOURCE_SYSTEM_TRANSMISSION purge" yichen_li@intuit.com allen_chaves@intuit.com raffi_norian@intuit.com david_fitzjarrell@intuit.com
