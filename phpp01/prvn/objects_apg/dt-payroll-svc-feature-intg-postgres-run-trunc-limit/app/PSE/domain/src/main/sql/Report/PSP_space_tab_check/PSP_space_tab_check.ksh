#!/bin/ksh 
set -x
echo "running PSP_space_tab_check.ksh"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus -s /nolog"

cd ${SCRIPT_DIR}
$SQLPLUS << EOF
connect / as sysdba
@tbspace.sql
@pspadm_tab_row_cnt_size_build_nopartition.sql
@pspadm_tab_row_cnt_size_build_partition.sql
EOF

chmod 600 $LOG_DIR/tbspace.log
cat tbspace.log |mailx -s "PSP tablespace usage" yichen_li@intuit.com allen_chaves@intuit.com raffi_norian@intuit.com david_fitzjarrell@intuit.com rob_histing@intuit.com

export NONPART_LOG=`ls $LOG_DIR/*tab_row_cnt_size_*.log|tail -1`
chmod 600 ${NONPART_LOG}
cat ${NONPART_LOG} |mailx -s "PSP big non-partitioned table check" yichen_li@intuit.com allen_chaves@intuit.com raffi_norian@intuit.com david_fitzjarrell@intuit.com

export PART_LOG=`ls $LOG_DIR/*tab_part_row_cnt_size_*.log|tail -1`
chmod 600 ${PART_LOG}
cat ${PART_LOG} |mailx -s "PSP partitioned table check" yichen_li@intuit.com allen_chaves@intuit.com raffi_norian@intuit.com david_fitzjarrell@intuit.com
