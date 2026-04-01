set lines 3000
set pages 3000
col username for a15 trunc
col event for a30 trunc
col machine for a50 trunc
col module for a50
col CLIENT_IDENTIFIER for a20
select sid, 
       serial#, 
       username, 
       sql_id, 
       (sysdate-SQL_EXEC_START)*24*60*60 run_since_sec,  
       event, 
       client_identifier,
       machine, 
       to_char(LOGON_TIME,'DD-MON HH24:MI:SS') LOGONTIME, 
       blocking_session
  from gv$session 
  where status = 'ACTIVE'
  and sql_id is not null
order by run_since_sec desc; 
