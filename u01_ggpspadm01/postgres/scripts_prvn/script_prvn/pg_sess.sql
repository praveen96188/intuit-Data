select usename, client_hostname,client_addr , count(1)
  from pg_stat_activity  
group by usename, client_hostname, client_addr order by 1;

