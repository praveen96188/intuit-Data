set lines 3000
col SQL_PROFILE for a15
col  SQL_PLAN_BASELINE for a20
select
SQL_PROFILE, SQL_PLAN_BASELINE, sql_id, PLAN_HASH_VALUE , EXECUTIONS, ELAPSED_TIME/1000/EXECUTIONS/1000 EL_PER_EXEC , to_char(LAST_ACTIVE_TIME,'DD-MON HH24:MI'),round ((buffer_gets)/ (executions)) lio, round((disk_reads)/(executions)) PIO, ROUND ((cpu_time)/(executions) / 1000/1000,4) avg_cpu_time_per_exec, ROUND((USER_IO_WAIT_TIME)/(executions) / 1000/1000,4) IO_TIME, round((rows_processed)/(executions)) avg_rows 
from gv$sql 
where EXECUTIONS > 0
and (sql_text) like '%&sql_handel%'
and sql_fulltext not like '%v$sql%'
-- order by to_char(LAST_ACTIVE_TIME,'DD-MON HH24:MI')
order by EL_PER_EXEC
/
