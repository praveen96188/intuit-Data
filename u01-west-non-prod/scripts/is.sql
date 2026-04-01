set lines 300
set pages 3000
col event for a30 trunc
col machine for a30 trunc
col module for a50
col CLIENT_IDENTIFIER for a20
select inst_id, sid, serial#, username, sql_id, (sysdate-SQL_EXEC_START)*24*60*60 run_since_sec, event, CLIENT_IDENTIFIER ,module, machine, to_char(LOGON_TIME,'DD-MON HH24:MI:SS')
from gv$session 
where status='INACTIVE' and SQL_ID is not null
order by CLIENT_IDENTIFIER;
