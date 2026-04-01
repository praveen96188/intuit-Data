set break on sql_id
select SQL_PROFILE, SQL_PLAN_BASELINE, sql_id, PLAN_HASH_VALUE , ELAPSED_TIME/1000, EXECUTIONS, ELAPSED_TIME/1000/EXECUTIONS/1000 EL_PER_EXEC , to_char(LAST_ACTIVE_TIME,'DD-MON HH24:MI'),round ((buffer_gets)/ (executions)) lio, round((rows_processed)/(executions)) avg_rows  from gv$sql where sql_text like '%&sql_h%' and PARSING_SCHEMA_NAME='QBO_DATA' and EXECUTIONS > 0 order by sql_id, to_char(LAST_ACTIVE_TIME,'DD-MON HH24:MI')
/
