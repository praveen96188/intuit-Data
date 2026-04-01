  select
   sql_id, sum(EXECUTIONS) S_Exec, SUM(ELAPSED_TIME/1000/1000) SUM_EL_PER_EXEC , SUM(disk_reads) SUM_PIO
  from gv$sql
  where EXECUTIONS > 1000
  and sql_text like '%&sql_handel%'
group by sql_id
order by sum_pio
/
