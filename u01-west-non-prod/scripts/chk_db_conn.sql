SELECT datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost') as client_hostname,application_name,count(*)
from pg_stat_activity
where pid != pg_backend_pid() and usename not in ('postgres','rdsadmin','ggs')
group by datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost'),application_name
order by usename;
