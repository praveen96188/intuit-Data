SELECT current_database() shard_id,
       queryid sql_id,
       substring(query, 1, 100) AS sql_text,
       calls num_execs,
       round((total_exec_time/calls)::numeric, 0) AS avg_time,
       round((blk_read_time/calls)::numeric, 0) AS avg_pio_time,
       round((total_exec_time/calls)::numeric, 0)-round((blk_read_time/calls)::numeric, 0) avg_cpu_n_wait_time,
       round((shared_blks_hit/calls)::numeric, 0) AS avg_lio,
       round((shared_blks_read/calls)::numeric, 0) AS avg_pio,
       round((temp_blks_written/calls)::numeric, 0) AS avg_disksort_wrt,
       round((temp_blks_read/calls)::numeric, 0) AS avg_disksort_reads,
       round(rows/calls,0) avg_rows,
       round((max_exec_time/1000)::numeric, 0) AS max_exec_time,
       round((100 * total_exec_time /sum(total_exec_time::numeric) OVER ())::numeric, 2) AS pct_tot_time,
       round((100 * blk_read_time /sum(blk_read_time::numeric) OVER ())::numeric, 2) AS pct_pio_time,
       round((100 * (round((total_exec_time/calls)::numeric, 0)-round((blk_read_time/calls)::numeric, 0))/sum((round((total_exec_time/calls)::numeric, 0)-round((blk_read_time/calls)::numeric, 0))) OVER ())::numeric, 0) AS pct_cpu_n_wait_time
FROM    pg_stat_statements
WHERE shared_blks_hit > 0 and calls > 0 and blk_read_time > 0
  AND userid in (select oid from pg_roles where rolname in ('pspapp','pspbatch_ro_user','pspbatch_rw_user','pspqsfinadmin','pspread','psprjf','pspqsfinadmin'))
ORDER BY total_exec_time DESC
LIMIT 50;
