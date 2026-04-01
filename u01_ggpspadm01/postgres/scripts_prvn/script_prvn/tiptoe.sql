begin
for i in (select * from kpopat.c34_sql_handel) loop
insert into kpopat.c34_sql_perf_stats select i.sql_handel, 
                                        sql_id, 
                                        plan_hash_value ,
                                        sql_profile, 
                                        sql_plan_baseline, 
                                        sum(executions) sum_executions,
                                        sum(rows_processed) sum_rows_returned, 
                                        sum(elapsed_time)/1000000 sum_elapsed_time_sec ,        
                                        sum(disk_reads) sum_pio, 
                                        sum(buffer_gets)  sum_lio,
                                        sum(cpu_time) sum_cpu_time_per_exec,
                                        sum(concurrency_wait_time) sum_conc_wait_time,
                                        sum(user_io_wait_time) sum_user_io_wait_time,
                                        command_type,
                                        max(last_active_time) last_active_time
                                 from v$sql
                                where sql_text like '%'||i.sql_handel||'%'
                                  and executions > 0 
                                  and PARSING_SCHEMA_NAME='QBO_DATA'
                                 group by sql_id, 
                                          plan_hash_value ,
                                          sql_profile, 
                                          sql_plan_baseline,
                                          command_type;

commit;                                                                                                                            
end loop;
end;
/
