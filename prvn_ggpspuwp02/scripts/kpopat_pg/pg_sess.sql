select usename, client_hostname, count(1)
  from pg_stat_activity  
group by usename, client_hostname;
