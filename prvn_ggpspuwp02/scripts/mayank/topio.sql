select substr(sql_text, instr(sql_text,'/*',1, 1), instr(sql_text,'*/',1, 1)+1) sql_h,
       sql_id,
       plan_hash_value,
       sum(executions) EXEC,
       round (sum(buffer_gets)/sum(executions)) avg_LIO_per_exec,
       round(sum(disk_reads)/sum(executions)) avg_PIO_per_exec,
       round (sum(cpu_time)/sum(executions)/1000000,4) avg_cpu_tme_per_exec,
       round(sum(ELAPSED_TIME)/1000000/sum(EXECUTIONS),2) EL_PER_EXEC
  from v$sql
 where EXECUTIONS > 100 and parsing_schema_name in ('QBO','QBO_DATA')
--having round(sum(ELAPSED_TIME)/1000000/sum(EXECUTIONS),2) > 0.7
having round (sum(buffer_gets)/sum(executions)) > 100000
 group by substr(sql_text, instr(sql_text,'/*',1, 1), instr(sql_text,'*/',1, 1)+1), sql_id,
           plan_hash_value
 order by AVG_LIO_PER_EXEC
/
