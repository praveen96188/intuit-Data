#!/bin/bash
# Author: Adityanath Dewoolkar
# Date: 16 JAN 2023
# Description: DDL for all non default users in database
# Version:  v1 - initial
# syntax: sh user_ddl.sh

##set -x

########################
## ENV SETTINGS
########################
. /l/orcl
DT11=`date +%Y%m%d`
DT21=`date +%Y%m%d-%H%M`
DIR=/u01/mayank/range-hash-replication/USER_DDL
LOGFILE=$DIR/USER_LIST_$DT11.txt
LOGFILE1=$DIR/USER_DDL_$DT11.sql

export ORACLE_SID=TEST1

#######################
## DIRECTORY
#######################

mkdir -p $DIR
cd $DIR

######################
## USER LIST
######################
sqlplus intuadmin/"changeme"@'pspe2euw.sbg-psp-ppd.a.intuit.com:1521/pspe2euw' << EOF
PROMPT
spool ${LOGFILE}
set pages 0
set echo off heading off feedback off
select username from dba_users where oracle_maintained='N' order by 1;
spool off
EOF

cd $DIR
> $LOGFILE1

###############################
## DYNAMIC USER SCRIPT CREATION
###############################

for dbuser in `cat ${LOGFILE}`
do
echo "-------------"
echo "--" $dbuser
echo "-------------"
sqlplus intuadmin/"changeme"@'pspe2euw.sbg-psp-ppd.a.intuit.com:1521/pspe2euw' << EOF
--spool ${LOGFILE1} APPEND
set termout off
set linesize 19000
set pages 50000
set feedback off
set trim on
set echo off
set serveroutput on
set long 99999999
set longchunksize 20000 pagesize 0
column Extracted_DDL for a1000
EXEC DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'PRETTY',TRUE);
EXEC DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'SQLTERMINATOR',TRUE);

select (case
when ((select count(*)
from dba_users
where username = '$dbuser' and profile <> 'DEFAULT') > 0)
then chr(10)||' -- Note: Profile'||(select dbms_metadata.get_ddl('PROFILE', u.profile) AS ddl from dba_users u where u.username = '$dbuser')
else to_clob (chr(10)||' -- Note: Default profile, no need to create!')
end ) from dual
UNION ALL
select (case
when ((select count(*)
from dba_users
where username = '$dbuser') > 0)
then ' -- Note: Create user statement'||dbms_metadata.get_ddl ('USER', '$dbuser')
else to_clob (chr(10)||' -- Note: User not found!')
end ) Extracted_DDL from dual
UNION ALL
select (case
when ((select count(*)
from dba_ts_quotas
where username = '$dbuser') > 0)
then ' -- Note: TBS quota'||dbms_metadata.get_granted_ddl( 'TABLESPACE_QUOTA', '$dbuser')
else to_clob (chr(10)||' -- Note: No TS Quotas found!')
end ) from dual
UNION ALL
select (case
when ((select count(*)
from dba_role_privs
where grantee = '$dbuser') > 0)
then ' -- Note: Roles'||dbms_metadata.get_granted_ddl ('ROLE_GRANT', '$dbuser')
else to_clob (chr(10)||' -- Note: No granted Roles found!')
end ) from dual
UNION ALL
select (case
when ((select count(*)
from V\$PWFILE_USERS
where username = '$dbuser' and SYSDBA='TRUE') > 0)
then ' -- Note: sysdba'||chr(10)||to_clob (' GRANT SYSDBA TO '||'"'||'$dbuser'||'"'||';')
else to_clob (chr(10)||' -- Note: No sysdba administrative Privilege found!')
end ) from dual
UNION ALL
select (case
when ((select count(*)
from dba_sys_privs
where grantee = '$dbuser') > 0)
then ' -- Note: System Privileges'||dbms_metadata.get_granted_ddl ('SYSTEM_GRANT', '$dbuser')
else to_clob (chr(10)||' -- Note: No System Privileges found!')
end ) from dual
UNION ALL
select (case
when ((select count(*)
from dba_tab_privs
where grantee = '$dbuser') > 0)
then ' -- Note: Object Privileges'||dbms_metadata.get_granted_ddl ('OBJECT_GRANT', '$dbuser')
else to_clob (chr(10)||' -- Note: No Object Privileges found!')
end ) from dual;

EOF
done >> $LOGFILE1

echo "Kindly check USER_DDL_$DT11.sql for USER DDL in $DIR"

