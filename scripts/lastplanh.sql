select ss.snap_id, 
       begin_interval_time, 
       sql_id, 
       plan_hash_value, 
       sql_profile, nvl(executions_delta,0) execs,
	   round((elapsed_time_delta/decode(nvl(executions_delta,0),0,1,executions_delta))/1000000,3) elapsed_per_exec,
	   round((cpu_time_delta/decode(nvl(executions_delta,0),0,1,executions_delta))/1000000,3) cpu_per_exec,
	   round((buffer_gets_delta/decode(nvl(buffer_gets_delta,0),0,1,executions_delta))) gets_per_exec,
	   round(disk_reads_delta/decode(executions_delta, 0, 1, executions_delta)) reads_per_exec,
	   round(rows_processed_delta/decode(executions_delta, 0, 1, executions_delta)) rows_per_exec
from DBA_HIST_SQLSTAT S, DBA_HIST_SNAPSHOT SS 
where sql_id IN ('&sql_id') 
and ss.snap_id = S.snap_id and ss.instance_number = S.instance_number and executions_delta > 0
 order by 1, 3, 7 desc
/
