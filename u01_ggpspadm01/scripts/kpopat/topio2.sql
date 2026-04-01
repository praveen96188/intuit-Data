col sql_h for a100 trunc
select substr(sql_text, instr(sql_text,'/*',1, 1), instr(sql_text,'*/',1, 1)+1) sql_h, 
       x.sql_id,
       plan_hash_value,
       sum(executions_delta) EXEC, 
       round(sum(buffer_gets_delta)/sum(executions_delta)) avg_LIO_per_exec, 
       round(sum(disk_reads_delta)/sum(executions_delta)) avg_PIO_per_exec, 
       round(sum(cpu_time_delta)/sum(executions_delta)/1000000,4) avg_cpu_tme_per_exec, 
       round(sum(elapsed_time_delta)/1000000/sum(executions_delta),2) el_per_exec, 
       sysdate,
  from dba_hist_sqlstatx, 
       (select sql_id, to_char(substr(sql_text,1,2048)) sql_text from gv$sql) y,
       dba_hist_snapshot z
 where x.sql_id = y.sql_id
   and x.snap_id= z.snap_id
   and x.executions_delta > 0  
   and x.parsing_schema_name in ('QBO','QBO_DATA') 
   and (begin_interval_time) between sysdate-1/24 and sysdate
    having round (sum(buffer_gets_delta)/sum(executions_delta)) > 100000 -- IO
   -- having round(sum(elapsed_time_delta)/1000000/sum(executions_delta),2) > 0.5 /* Elapsed time*/ and sum(executions_delta) > 100
 group by substr(sql_text, instr(sql_text,'/*',1, 1), instr(sql_text,'*/',1, 1)+1), x.sql_id,
          plan_hash_value
/
