set lines 300
set pages 3000
col event for a30 trunc
col username for a20
col machine for a30 trunc
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
