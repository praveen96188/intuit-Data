
--unlock/lock user
alter user pspadm_owner with LOGIN;

--check user status
select rolname,rolcanlogin from pg_roles where rolname in ('pspadm_owner','pspapp','psprjf','pspbatch_rw_user','pspbatch_ro_user','pspread');

----connections
SELECT datname as db,usename as
    username,client_addr as machine, count(*) FROM pg_stat_activity
where usename not in ('rdsadmin','postgres','dms_apg_src','ggt','ggs')
group by db,usename,machine;


--active connections
SELECT current_timestamp,
       datname, pid,leader_pid, usesysid, usename, application_name, backend_type,
       coalesce(client_hostname, client_addr::text, 'localhost') as client_hostname,
       wait_event_type, wait_event, query, query_start,
       1000 * EXTRACT(EPOCH FROM (clock_timestamp()-query_start)) as duration
from pg_stat_activity
where state in ('active','idle in transaction') and pid != pg_backend_pid()
and usename not in ('ggs','ggt','dms_apg_src','postgres')
order by query_start desc;

--Triggers
select distinct trigger_name from information_schema.triggers where trigger_schema='pspadm';

--sequences
SELECT sequence_schema, sequence_name 
FROM information_schema.sequences 
 ORDER BY sequence_name ;

--object view

select
    routine_catalog AS DatabaseName
     ,routine_schema AS SchemaName
     ,routine_name AS FunctionName
     ,routine_type AS ObjectType
from information_schema.routines
where routine_schema = 'pspadm' order by ObjectType;

--age 
SELECT c.oid::regclass as table_name,
       greatest(age(c.relfrozenxid),age(t.relfrozenxid)) as age
FROM pg_class c
         LEFT JOIN pg_class t ON c.reltoastrelid = t.oid
WHERE c.relkind IN ('r', 'm')  order by 2 desc; 


--db age
SELECT datname, age(datfrozenxid) FROM pg_database ORDER BY age(datfrozenxid) desc limit 20;


--tables
select * from Pg_stat_all_tables;

--invalid objects

SELECT pg_class.relname 
FROM pg_class, pg_index 
WHERE pg_index.indisvalid = false 
AND pg_index.indexrelid = pg_class.oid;



--blocking

select pid, 
       usename, 
       pg_blocking_pids(pid) as blocked_by, 
       query as blocked_query
from pg_stat_activity
where cardinality(pg_blocking_pids(pid)) > 0;


--gswait

select usename, application_name, pid, wait_event_type, wait_event, state, substr(query,1,100), backend_type, now() - query_start, pid
  from pg_stat_activity 
 where state IN ('active','idle in transaction') ;

--kill session

SELECT pg_terminate_backend();

--blocking  query

select pid,usename,datname,query from pg_stat_activity  where pid=59216;

--replication slots status 

select * from pg_replication_slots;

--check replication slot size 

select slot_name, pg_size_pretty(pg_wal_lsn_diff(pg_current_wal_lsn(),restart_lsn)) as replicationSlotLag, active,restart_lsn,confirmed_flush_lsn  from pg_replication_slots ;
