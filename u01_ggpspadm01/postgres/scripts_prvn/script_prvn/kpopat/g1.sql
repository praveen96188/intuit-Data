set head on
set pages 10000
select PARSING_SCHEMA_NAME, SQL_PROFILE, SQL_PLAN_BASELINE, sql_id, PLAN_HASH_VALUE , ELAPSED_TIME/1000, EXECUTIONS, round(ELAPSED_TIME/1000/EXECUTIONS/1000,2) EL_PER_EXEC , to_char(LAST_ACTIVE_TIME,'DD-MON HH24:MI') last_active_time,round ((buffer_gets)/ (executions)) lio, round((disk_reads)/(executions)) PIO, round((rows_processed)/(executions)) avg_rows	from gv$sql where PARSING_SCHEMA_NAME='PSPADM' and EXECUTIONS > 10 and ELAPSED_TIME/1000/EXECUTIONS/1000 > 1 
order by LAST_ACTIVE_TIME 
--order by sql_id
/
