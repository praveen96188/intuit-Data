set echo off
-- FILE:        chk_sqlarea_history_nocase.sql
-- FUNCTION:    Check all SQL statements which contain the given string.
-- INPUT:       Search string in SQL 

spool chk_sqlarea_history_nocase
set pagesize 1000
set feed on
set verify off
column username format a10
column sid format 999
column serial format 99999
column osuser format a10
column executions format 9,999,999,999
column disk_reads format 999,999,999
column buffer_gets format 999,999,999
column rows_processed format 9,999,999,999
column cpu_time format 999,999,999,999   
column elapsed_time format 99,999,999,999,999

SELECT a.first_load_time,
       a.parsing_user_id,
       a.executions,
       a.disk_reads,
       a.buffer_gets,
       a.rows_processed,
       a.cpu_time,
       a.elapsed_time,
       a.sql_id,
       rawtohex(a.address) address,
       a.hash_value,               
       a.sql_text
  FROM v$sqlarea a
  WHERE upper(a.sql_text) like upper('%&String_in_SQL%')
  ORDER BY 1
/

spool off

