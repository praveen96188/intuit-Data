 SELECT now() - query_start as "runtime", usename, datname, state, query 
   FROM pg_stat_activity 
  WHERE state='active' 
    and now() - query_start > '2 seconds'::interval 
  ORDER BY runtime DESC;
