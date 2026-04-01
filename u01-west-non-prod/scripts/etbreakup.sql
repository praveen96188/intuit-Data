set lines 240
select sql_id,
	count(1) executions,
	round(round(sum(elapsed_time)/1000000)/count(1),2) elapsed_time,
	round(round(sum(cpu_time)/1000000)/count(1),2) cpu_time,
	round(round(sum(user_io_wait_time)/1000000)/count(1),2) user_io_wait_time,
	round(round(sum(plsql_exec_time)/1000000)/count(1),2) plsql_exec_time,
	round((sum(cpu_time)/sum(elapsed_time))*100) pct_cpu_time,
	round((sum(user_io_wait_time)/sum(elapsed_time))*100) pct_user_io_wait_time,
	round((sum(plsql_exec_time)/sum(elapsed_time))*100) pct_plsql_exec_time
from v$sql_monitor
having count(1) > 100
group by sql_id
order by 2 desc
/
