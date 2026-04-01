#!/bin/ksh
set -x
echo "running sox_user_extract_queries.sql"

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/home/oracle/ycl/SECURITY"
export ORACLE_BASE="/u01/app/pspprod/oracle"
export ORACLE_HOME="/u01/app/pspprod/oracle/product/10.2.0.3/db"
export ORACLE_SID="pspprod1"
export SQLPLUS="${ORACLE_HOME}/bin/sqlplus"

##echo "enter date in yyyy_mm_dd"
DATE=`date +%Y_%m_%d`
LOGIN=`awk '{print $2}' ${ORACLE_BASE}/local/monitor/dba_${ORACLE_SID}.sql`

NEW_DIR=`date +%Y_%b`
if [ ! -d ${LOG_DIR}/${NEW_DIR} ]
then
mkdir ${LOG_DIR}/${NEW_DIR}
fi
cd ${LOG_DIR}/${NEW_DIR}

$SQLPLUS $LOGIN @${SCRIPT_DIR}/sox_user_extract_queries.sql << EOF
${DATE}
EOF

ls -ltr *${DATE}*

sed 's/\s*|/\t/g' pspprd_users_${DATE}.log > pspprd_users_${DATE}.txt
sed 's/\s*|/\t/g' pspprd_upd_user_${DATE}.log > pspprd_upd_user_${DATE}.txt
sed 's/\s*|/\t/g' pspprd_upd_app_${DATE}.log > pspprd_upd_app_${DATE}.txt
sed 's/\s*|/\t/g' pspprd_intuitusers_${DATE}.log > pspprd_intuitusers_${DATE}.txt
sed 's/\s*|/\t/g' pspprd_deleteusers_${DATE}.log > pspprd_deleteusers_${DATE}.txt
sed 's/\s*|/\t/g' pspprd_roles_${DATE}.log >  pspprd_roles_${DATE}.txt
sed 's/\s*|/\t/g' pspprd_dblink_${DATE}.log > pspprd_dblink_${DATE}.txt

ls -ltr *_${DATE}*
ls -ltr *_${DATE}.txt

/usr/bin/unix2dos pspprd_users_${DATE}.txt
/usr/bin/unix2dos pspprd_upd_user_${DATE}.txt
/usr/bin/unix2dos pspprd_upd_app_${DATE}.txt
/usr/bin/unix2dos pspprd_intuitusers_${DATE}.txt
/usr/bin/unix2dos pspprd_deleteusers_${DATE}.txt
/usr/bin/unix2dos pspprd_roles_${DATE}.txt
/usr/bin/unix2dos pspprd_dblink_${DATE}.txt

/usr/bin/uuencode pspprd_users_${DATE}.txt pspprd_users_${DATE}.txt > ${LOG_DIR}/${NEW_DIR}/out.mail
/usr/bin/uuencode pspprd_upd_user_${DATE}.txt pspprd_upd_user_${DATE}.txt >> ${LOG_DIR}/${NEW_DIR}/out.mail
/usr/bin/uuencode pspprd_upd_app_${DATE}.txt pspprd_upd_app_${DATE}.txt >> ${LOG_DIR}/${NEW_DIR}/out.mail
/usr/bin/uuencode pspprd_intuitusers_${DATE}.txt pspprd_intuitusers_${DATE}.txt >> ${LOG_DIR}/${NEW_DIR}/out.mail
/usr/bin/uuencode pspprd_deleteusers_${DATE}.txt pspprd_deleteusers_${DATE}.txt >> ${LOG_DIR}/${NEW_DIR}/out.mail
/usr/bin/uuencode pspprd_roles_${DATE}.txt pspprd_roles_${DATE}.txt >> ${LOG_DIR}/${NEW_DIR}/out.mail
/usr/bin/uuencode pspprd_dblink_${DATE}.txt pspprd_dblink_${DATE}.txt >> ${LOG_DIR}/${NEW_DIR}/out.mail

mailx -s "PSP user extract" James_Duke@intuit.com Samuel_Calder@intuit.com db-pspprod-email < ${LOG_DIR}/${NEW_DIR}/out.mail
