SELECT datname,Usename, pid, state, age(clock_timestamp(), query_start) AS age ,query
FROM pg_stat_activity
WHERE state = 'active'  and query not like '%SELECT datname,Usename, pid, state, age(clock_timestamp(), query_start) AS age ,query
FROM pg_stat_activity%'
ORDER BY age DESC;
