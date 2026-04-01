\set ECHO none
\x
select * from pg_replication_slots where active='f';
