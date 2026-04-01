select 								sql_id,
										plan_hash_value ,
										sum(executions),
										round(sum(rows_processed)/sum(executions)) avg_rows_returned,
										round(sum(elapsed_time)/1000/sum(executions)/1000,4) elapsed_time_per_exec ,
										round(sum(disk_reads)/sum(executions)) avg_pio,
										round (sum(buffer_gets) / sum(executions)) avg_lio,
										round (sum(cpu_time)/ sum(executions) / 1000/1000,4) avg_cpu_time_per_exec,
										round(sum(concurrency_wait_time)/sum(executions)/1000/1000,4) concurrency_wait_time,
										round(sum(user_io_wait_time)/sum(executions)/1000/1000,4) user_io_wait_time,
										command_type,
										max(last_active_time) last_active_time
 from gv$sql where executions > 100
 group by sql_id,
										plan_hash_value ,
										sql_profile,
										sql_plan_baseline,command_type
order by 3;
