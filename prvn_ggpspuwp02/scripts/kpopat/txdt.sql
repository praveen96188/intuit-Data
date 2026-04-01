break on sql_id
set pages 1000
set lines 300
col elapsed_per_exec for 99.999
select trunc(begin_interval_time), 
       sql_id,
       plan_hash_value old_phv,
       (select count(1) from v$sql_plan sp where OBJECT_NAME='TXDETAILS_TX_DATE' and sp.sql_id = s.sql_id and sp.plan_hash_value=s.plan_hash_value)  plan_cnt,
       sum(nvl(executions_delta,0)) execs,
       round((sum(elapsed_time_delta)/decode(nvl(sum(executions_delta),0),0,1,sum(executions_delta)))/1000000,3) elapsed_per_exec,
       round((sum(buffer_gets_delta)/decode(nvl(sum(buffer_gets_delta),0),0,1,sum(executions_delta)))) gets_per_exec,
       round(sum(disk_reads_delta)/decode(sum(executions_delta), 0, 1, sum(executions_delta))) reads_per_exec,
       round(sum(rows_processed_delta)/decode(sum(executions_delta), 0, 1, sum(executions_delta))) rows_per_exec,
       round((sum(cpu_time_delta)/decode(nvl(sum(executions_delta),0),0,1,sum(executions_delta)))/1000000,3) cpu_per_exec,
       round((sum(iowait_delta)/decode(nvl(sum(executions_delta),0),0,1,sum(executions_delta)))/1000000,3) io_per_exec,
      'H' 
  from dba_hist_sqlstat S, dba_hist_snapshot SS
 where trunc(begin_interval_time) in ('21-SEP-18','24-SEP-18','25-SEP-18')
--   and sql_id in (select sql_id from v$sql_plan where OBJECT_NAME='TXDETAILS_TX_DATE')
   and sql_id in (select x.sql_id from v$sql_plan x, v$sqlarea y where x.sql_id=y.sql_id and OBJECT_NAME='TXDETAILS_TX_DATE' group by x.sql_id having sum(executions) > 1000)
   and ss.snap_id = S.snap_id and ss.instance_number = S.instance_number and executions_delta > 100
 group by trunc(begin_interval_time), sql_id, plan_hash_value
union all
select trunc(LAST_ACTIVE_TIME) "day", sql_id, plan_hash_value, (select count(1) from v$sql_plan sp where OBJECT_NAME='TXDETAILS_TX_DATE' and sp.sql_id = s.sql_id and sp.plan_hash_value=s.plan_hash_value) , EXECUTIONS, ELAPSED_TIME/1000/EXECUTIONS/1000 EL_PER_EXEC ,round ((buffer_gets)/ (executions)) lio, round((disk_reads)/(executions)) PIO, round((rows_processed)/(executions)) avg_rows, ROUND ((cpu_time)/(executions) / 1000/1000,4) avg_cpu_time_per_exec, ROUND((USER_IO_WAIT_TIME)/(executions) / 1000/1000,4) IO_TIME, 'C'
  from gv$sql s 
where sql_id in (select x.sql_id from v$sql_plan x, v$sqlarea y where x.sql_id=y.sql_id and OBJECT_NAME='TXDETAILS_TX_DATE' group by x.sql_id having sum(executions) > 1000)
--  where sql_id in (select sql_id from v$sql_plan where OBJECT_NAME='TXDETAILS_TX_DATE')
  and EXECUTIONS > 100
 order by 2,1 asc
/   

