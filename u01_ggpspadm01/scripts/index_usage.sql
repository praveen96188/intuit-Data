col SQL_PROFILE for a30
set lines 240
select SQL_PROFILE, SQL_PLAN_BASELINE, sql_id,child_number, PLAN_HASH_VALUE , EXECUTIONS, ELAPSED_TIME/1000/EXECUTIONS/1000 EL_PER_EXEC , to_char(LAST_ACTIVE_TIME,'DD-MON HH24:MI'),round ((buffer_gets)/ (executions)) lio, round((disk_reads)/(executions)) PIO, ROUND ((cpu_time)/(executions) / 1000/1000,4) avg_cpu_time_per_exec, ROUND((USER_IO_WAIT_TIME)/(executions) / 1000/1000,4) IO_TIME, round((rows_processed)/(executions)) avg_rows  from gv$sql where sql_id in (select distinct sql_id from v$sql_plan where OBJECT_NAME=upper('&object_name')) and EXECUTIONS > &ex order by sql_id 
/
