set lines 3000 feedback on
set pages 3000
col USERNAME for a20
col event for a30 trunc
col machine for a60 trunc
col module for a40
col CLIENT_IDENTIFIER for a30
select sid, 
       serial#, 
       username, 
       sql_id, 
       (sysdate-SQL_EXEC_START)*24*60*60 run_since_sec,  
       event, 
       client_identifier,
       module,
       machine, 
       to_char(LOGON_TIME,'DD-MON HH24:MI:SS') LOGONTIME, 
       blocking_session
  from gv$session 
  where status = 'ACTIVE'
  and sql_id is not null
order by run_since_sec desc; 
