select usename, application_name, pid, wait_event_type, wait_event, state, substr(query,1,100), backend_type, now() - query_start
  from pg_stat_activity 
 where state IN ('active','idle in transaction') ;
