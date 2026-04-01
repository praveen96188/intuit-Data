set head on
select * from (
select
trunc(begin_interval_time),
sql_id, plan_hash_value,
sql_profile, sum(nvl(executions_delta,0)) execs,
round((sum(elapsed_time_delta)/decode(nvl(sum(executions_delta),0),0,1,sum(executions_delta)))/1000000,3) elapsed_per_exec,
round((sum(buffer_gets_delta)/decode(nvl(sum(buffer_gets_delta),0),0,1,sum(executions_delta)))) gets_per_exec,
round(sum(disk_reads_delta)/decode(sum(executions_delta), 0, 1, sum(executions_delta))) reads_per_exec,
round(sum(rows_processed_delta)/decode(sum(executions_delta), 0, 1, sum(executions_delta))) rows_per_exec,
round((sum(cpu_time_delta)/decode(nvl(sum(executions_delta),0),0,1,sum(executions_delta)))/1000000,3) cpu_per_exec,
round((sum(iowait_delta)/decode(nvl(sum(executions_delta),0),0,1,sum(executions_delta)))/1000000,3) io_per_exec
from DBA_HIST_SQLSTAT S, DBA_HIST_SNAPSHOT SS
where
 sql_id IN ('&sql_id') and
ss.snap_id = S.snap_id and ss.instance_number = S.instance_number and executions_delta > 0
group by trunc(begin_interval_time),
sql_id, plan_hash_value,
sql_profile
order by 1 desc, 3, 7 desc
)
where rownum < 10
/
