select 
  sql_name||','||
  sql_id||','||
  shards||','||
  num_shards||','||
  exec||','||
  lio_per_exec||','||
  pio_per_exec||','||
  cpu_time_per_exec||','||
  el_time_per_exec||','||
  tot_el_time||','||
  tot_lio||','||
  tot_el_time_rnk||','||
  tot_lio_rnk||','||
  tot_el_time_pct||','||
  tot_lio_pct||','||
  cum_pct_elapsed||','||
  cum_pct_lio||','||
  min_elapsed||','||
  max_elapsed||','||
  num_plans
  from (
select * from (
select 
  sql_name,
  sql_id,
  shards,
  num_shards,
  exec,
  lio_per_exec,
  pio_per_exec,
  cpu_time_per_exec,
  el_time_per_exec,
  tot_el_time,
  tot_lio,
  tot_el_time_rnk,
  tot_lio_rnk,
  tot_el_time_pct,
  tot_lio_pct,
  sum(tot_el_time_pct) over (order by tot_el_time_pct desc) cum_pct_elapsed,
  sum(tot_lio_pct) over (order by tot_el_time_pct desc) cum_pct_lio,
  min_elapsed,
  max_elapsed,
  num_plans
from (
  select sql_name,
  sql_id,
  shards,
  num_shards,
  exec,
  lio_per_exec,
  pio_per_exec,
  cpu_time_per_exec,
  el_time_per_exec,
  round(exec*el_time_per_exec) tot_el_time,
  round(exec*lio_per_exec) tot_lio,
  dense_rank() over (order by round(exec*el_time_per_exec) desc) tot_el_time_rnk,
  dense_rank() over (order by round(exec*lio_per_exec) desc) tot_lio_rnk,
  round((ratio_to_report(round(exec*el_time_per_exec)) over ())*100,2) tot_el_time_pct,
  round((ratio_to_report(round(exec*lio_per_exec)) over ())*100,2) tot_lio_pct,
  min_elapsed, max_elapsed, num_plans
  from (
    select 
      sql_handle sql_name,
      sql_id,
      listagg(cluster_id, '#') within group (order by sql_id) shards,
      count(distinct cluster_id) num_shards,
      sum(exec) exec,
      round(sum(avg_lio_per_exec*exec)/sum(exec)) lio_per_exec,
      round(sum(avg_pio_per_exec*exec)/sum(exec)) pio_per_exec,
      round(sum(avg_cpu_tme_per_exec*exec)/sum(exec),2) cpu_time_per_exec,
      round(sum(el_per_exec*exec)/sum(exec),2) el_time_per_exec,
      count(distinct plan_hash_value) num_plans,
      min(el_per_exec) as min_elapsed, max(el_per_exec) as max_elapsed
      from qbo_cluster_top_sql
      where trunc(sample_date) = '23-APR-18'
      group by sql_handle, sql_id
      having min(el_per_exec) > 0
    )
)
  order by tot_el_time desc
)
where (cum_pct_elapsed <=80  OR tot_lio_rnk <=30)
);
