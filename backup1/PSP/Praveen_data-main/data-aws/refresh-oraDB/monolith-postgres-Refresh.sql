---check active connections

grant create on database psparcdb to psparc_readonly_role;
grant usage on schema psparc to psparc_readonly_role;
grant connect on database psparcdb to psparc_readonly_role;


SELECT datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost') as client_hostname,application_name,count(*)
from pg_stat_activity
where pid != pg_backend_pid()
group by datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost'),application_name
order by usename;



psql -h ppsp-stg-pitparmo-new-cluster.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -p 5432 -U postgres
--PIgRgK7d#(2XZ

psql -h ppsp-stg-pitparmo.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -p 5432 -U postgres
--PIgRgK7d#(2XZ


alter user pspapp with NOLOGIN;
alter user pspadm_owner with LOGIN;

export PGPASSWORD="PIgRgK7d#(2XZ"
psql -h  ppsp-stg-pitparmo-new-cluster.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -U postgres -p 6543 -d pitparmo -f $1 -a -b -e

select * from pg_replication_slots;
select pg_drop_replication_slot(slot_name) from pg_replication_slots;

alter database pspapg02 rename to pitparmo;

--connect to staging database as below
\c pitparmo
 
create or replace view dual as select 1; 
alter user pspadm_owner login password 'ppp3zu#JA7M5aa';
grant create on database pitparmo to pspadm_owner;
grant usage, create on schema pspadm to pspadm_owner;
GRANT SET ON PARAMETER session_replication_role to pspadm_owner;
grant rds_replication to pspadm_owner;

--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database pitparmo from public;

-- switch to pspapg02 database
\c pitparmo

create role pspadm_readonly_role;
grant connect on database pitparmo to pspadm_readonly_role;
grant usage on schema pspadm to pspadm_readonly_role;
grant select on all tables in schema pspadm to pspadm_readonly_role;
alter default privileges in schema pspadm grant select on tables to pspadm_readonly_role;


####Read write
--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database pitparmo from public;

-- switch to pspapg02 database
\c pitparmo

create role pspadm_readwrite_role;
-- grant permission to connect the database
grant connect on database pitparmo to pspadm_readwrite_role;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema pspadm to pspadm_readwrite_role;

\c pitparmo pspadm_owner

-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema pspadm grant select, insert, update, delete on  tables to pspadm_readwrite_role;
-- grant select, DML permission to all the existing sequences if any to the role 
grant select, usage on all sequences in schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema pspadm grant select, usage on sequences to pspadm_readwrite_role;
-- grant permission on functions, procedures
grant execute on all functions in schema pspadm to pspadm_readwrite_role;
grant execute on all procedures in schema pspadm to pspadm_readwrite_role;
-- grant permission on functions, procedures
alter default privileges in schema pspadm grant execute on functions to pspadm_readwrite_role;


\c pitparmo postgres

create user psp_prl_app password 'ViqB#N4uLG)a';
alter user psp_prl_app login password 'ViqB#N4uLG)a';
alter user psp_prl_app set search_path to pspadm;
ALTER role psp_prl_app SET work_mem TO '16MB';
ALTER role psp_prl_app SET max_parallel_workers_per_gather TO 2;

create user psp_prl_app with password 'ViqB#N4uLG)a';
-- grant read write role to user
grant pspadm_readwrite_role to psp_prl_app;
--set search path for pspapp
alter user psp_prl_app set search_path to pspadm;
--set resource usage
ALTER role psp_prl_app SET work_mem TO '16MB';
ALTER role psp_prl_app SET max_parallel_workers_per_gather TO 2;



create user perf_test  with password 'Perf#123';
alter user perf_test with password 'Perf#123';
--grant read only role to users
grant pspadm_readwrite_role to perf_test;
grant usage, create on schema pspadm to perf_test;
--set search path for pspapp
alter user perf_test set search_path to pspadm;

grant select on pg_stat_statements to perf_test;


create user psp_prl_read with password 'VrXdZcH5s#(1lS';
alter user psp_prl_read with password 'VrXdZcH5s#(1lS';
--grant read only role
grant pspadm_readonly_role to psp_prl_read;
--set search path
alter user psp_prl_read set search_path to pspadm;
--set resource usage
ALTER role psp_prl_read SET work_mem TO '64MB';
ALTER role psp_prl_read SET max_parallel_workers_per_gather TO 4;


Alter user psprjf with password 'JsiHKoB7s#(5hW';
--grant read only role
grant pspadm_readonly_role to psprjf;
--set search path
alter user psprjf set search_path to pspadm;
--set resource usage
ALTER role psprjf SET work_mem TO '64MB';
ALTER role psprjf SET max_parallel_workers_per_gather TO 4;

SELECT datname as db,usename as
    username,client_addr as machine, count(*) FROM pg_stat_activity 
where usename not in ('rdsadmin','postgres') 
group by db,usename,machine;


aws rds --profile sbg-psp-prod --region us-west-2 describe-db-clusters --db-cluster-identifier ppsp-stg-pitparmo > /tmp/ppsp-stg-pitparmo.json
jq -rj '.DBClusters[].TagList' /tmp/ppsp-stg-pitparmo.json > /tmp/tags
aws rds --profile sbg-psp-prod --region us-west-2 add-tags-to-resource --resource-name arn:aws:rds:us-west-2:893547637742:db:ppsp-stg-pitparmo1 --tags file:///tmp/tags
aws rds --profile sbg-psp-prod --region us-west-2 add-tags-to-resource --resource-name arn:aws:rds:us-west-2:893547637742:db:ppsp-stg-pitparmo2 --tags file:///tmp/tags


cdk diff --all -c env=stg -c group=primary-olap -c config=ppsp-stg-olap.json 
cdk diff --all -c env=stg -c group=primary-stg -c config=ppsp-stg-pitparmo.json 

select pc.relname, ps.* from pg_statistic ps, pg_class pc where ps.starelid = pc.oid and pc.relkind='i' order by relname;




SELECT 'ALTER SEQUENCE '||sequence_owner||'.'||sequence_name||' RESTART WITH '||(last_number + 1)||';'
FROM all_sequences
WHERE sequence_owner = 'PSPADM';





grant all on all tables in schema pspadm  to psp_prl_app;
grant all on all sequences in schema pspadm to psp_prl_app;
grant all on all functions in schema pspadm to psp_prl_app;
grant all on all procedures in schema pspadm to psp_prl_app;
alter default privileges in schema pspadm grant all on tables to psp_prl_app;
alter default privileges in schema pspadm grant all on sequences to psp_prl_app;
alter default privileges in schema pspadm grant all on functions to psp_prl_app;
alter user psp_prl_app set search_path to pspadm;






--PDS
set search_path to pspadm;
CREATE EXTENSION dblink schema pspadm;
GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO psp_pds2_pspapp;
GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO psp_pds2_pspapp;
GRANT EXECUTE ON FUNCTION dblink_connect_u(text, text) TO psp_pds2_pspapp;
GRANT EXECUTE ON FUNCTION dblink_exec(text,text) TO psp_pds2_pspapp;
GRANT EXECUTE ON FUNCTION dblink_disconnect(text) TO psp_pds2_pspapp;
CREATE SERVER loopback_dblink FOREIGN DATA WRAPPER dblink_fdw OPTIONS (hostaddr 'ppsp-pds-ue01.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com',port '5432', dbname 'ppdspg01');
CREATE USER MAPPING FOR public SERVER loopback_dblink OPTIONS (user 'psp_pds2_pspapp', password 'Pzu#JA7M55$%');


--staging
set search_path to pspadm;
CREATE EXTENSION dblink schema pspadm;
GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO psp_prl_app;
GRANT EXECUTE ON FUNCTION dblink_connect_u(text, text) TO psp_prl_app;
GRANT EXECUTE ON FUNCTION dblink_exec(text,text) TO psp_prl_app;
GRANT EXECUTE ON FUNCTION dblink_disconnect(text) TO psp_prl_app;
CREATE SERVER loopback_dblink FOREIGN DATA WRAPPER dblink_fdw OPTIONS (hostaddr 'ppsp-stg-pitparmo.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com',port '5432', dbname 'pitparmo');
CREATE USER MAPPING FOR public SERVER loopback_dblink OPTIONS (user 'psp_prl_app', password 'ViqB#N4uLG)a');

GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO pspadm_owner;
GRANT EXECUTE ON FUNCTION dblink_connect_u(text) TO pspadm_owner;
GRANT EXECUTE ON FUNCTION dblink_connect_u(text, text) TO pspadm_owner;
GRANT EXECUTE ON FUNCTION dblink_exec(text,text) TO pspadm_owner;
GRANT EXECUTE ON FUNCTION dblink_disconnect(text) TO pspadm_owner;
CREATE USER MAPPING FOR pspadm_owner SERVER loopback_dblink OPTIONS (user 'pspadm_owner', password 'ppp3zu#JA7M5aa');
GRANT USAGE ON FOREIGN SERVER loopback_dblink TO pspadm_owner;
CREATE SERVER loopback_dblink_localMap FOREIGN DATA WRAPPER dblink_fdw OPTIONS (hostaddr '127.0.0.1',port '10560', dbname 'pitparmo');
CREATE USER MAPPING FOR pspadm_owner SERVER loopback_dblink_localMap OPTIONS (user 'pspadm_owner', password 'ppp3zu#JA7M5aa');
CREATE USER MAPPING FOR public SERVER loopback_dblink_localMap OPTIONS (user 'pspadm_owner', password 'ppp3zu#JA7M5aa');


\c ppdspg01 pspadm_owner
grant select on dual to  pspadm_readwrite_role;
grant all on all tables in schema pspadm  to pspadm_readwrite_role;
grant all on all sequences in schema pspadm to pspadm_readwrite_role;
grant all on all functions in schema pspadm to pspadm_readwrite_role;
grant all on all procedures in schema pspadm to pspadm_readwrite_role;
alter default privileges in schema pspadm grant all on tables to pspadm_readwrite_role;
alter default privileges in schema pspadm grant all on sequences to pspadm_readwrite_role;
alter default privileges in schema pspadm grant all on functions to pspadm_readwrite_role;













--create database
create database pspval;
--connect to newly created database
\c pspval
--create schema
create schema pspval;
-- create read-write application user/role with permission to login and grant needful permissions
create role pspval with login password 'pspval#123';
-- grant permission to connect the database
grant connect on database pspval to pspval;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work
grant usage on schema pspval to pspval;
grant usage, create on schema pspval to pspval;
 
alter user pspval set search_path to pspval;

\c pspval pspval 
--apply below scripts

--extentions



-- grant permission to connect the database 
grant connect on database pstgolap to pspadm_owner;

-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage, create on schema pspadm to pspadm_owner;

-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema pspadm to pspadm_owner;

-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema pspadm grant select, insert, update, delete on tables to pspadm_owner;

-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema pspadm to pspadm_owner;

-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema pspadm grant usage on sequences to pspadm_owner;

--set search path for ibobadm_owner
alter user pspadm_owner set search_path to pspadm;


pspval=> truncate table pspval.compare_info_cdc_event;
TRUNCATE TABLE
pspval=> truncate table pspval.oracle_hash_cdc_event;
TRUNCATE TABLE
pspval=> truncate table pspval.oracle_range_cdc_event;
TRUNCATE TABLE
pspval=> truncate table pspval.postgres_hash_cdc_event;;
TRUNCATE TABLE

truncate table pspval.compare_info_cdc_event;
truncate table pspval.oracle_hash_cdc_event;
truncate table pspval.oracle_range_cdc_event;
truncate table pspval.postgres_hash_cdc_event;
psql -h ppsp-stg-olap-new-cluster.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -U postgres -p 5432 

--pstgolap
alter database pspapg02 rename to pstgolap;
alter user pspadm_owner login password 'ppp3zu#JA7M5aa';
grant create on database pstgolap to pspadm_owner;
grant usage, create on schema pspadm to pspadm_owner;
GRANT SET ON PARAMETER session_replication_role to pspadm_owner;
grant rds_replication to pspadm_owner;
create user psp_prl_app password 'ViqB#N4uLG)a';
grant connect on database pstgolap to psp_prl_app;
grant usage on schema pspadm to psp_prl_app;

--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database pstgolap from public;

-- switch to pspapg02 database
\c pstgolap

create role pspadm_readonly_role;
grant connect on database pstgolap to pspadm_readonly_role;
grant usage on schema pspadm to pspadm_readonly_role;
grant select on all tables in schema pspadm to pspadm_readonly_role;
alter default privileges in schema pspadm grant select on tables to pspadm_readonly_role;


####Read write
--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database pstgolap from public;

-- switch to pspapg02 database
\c pstgolap

create role pspadm_readwrite_role;
-- grant permission to connect the database
grant connect on database pstgolap to pspadm_readwrite_role;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema pspadm grant select, insert, update, delete on  tables to pspadm_readwrite_role;

-- grant select, DML permission to all the existing sequences if any to the role 
grant select, usage on all sequences in schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema pspadm grant select, usage on sequences to pspadm_readwrite_role;
-- grant permission on functions, procedures
grant execute on all functions in schema pspadm to pspadm_readwrite_role;
grant execute on all procedures in schema pspadm to pspadm_readwrite_role;
-- grant permission on functions, procedures
alter default privileges in schema pspadm grant execute on functions to pspadm_readwrite_role;


grant select on pg_stat_statements to pspadm_readwrite_role;



--# create read-write(OLAP) user/role with permission to login and grant needful permissions #--
create user pspbatch_rw_user with password 'YqXwHmW2s#(9iJ';
alter user pspbatch_rw_user login password 'YqXwHmW2s#(9iJ';
--grant read only role
grant pspadm_readwrite_role to pspbatch_rw_user;
--set search path
alter user pspbatch_rw_user set search_path to pspadm;
--set resource usage
ALTER role pspbatch_rw_user SET work_mem TO '64MB';
ALTER role pspbatch_rw_user SET max_parallel_workers_per_gather TO 4;

--# create read-only(OLAP) user/role with permission to login and grant needful permissions #--
create user pspbatch_ro_user with password 'DZysNtJ4s#(1lQ';
alter user pspbatch_ro_user login password 'DZysNtJ4s#(1lQ';

--grant read only role
grant pspadm_readonly_role to pspbatch_ro_user;
--set search path
alter user pspbatch_ro_user set search_path to pspadm;
--set resource usage
ALTER role pspbatch_ro_user SET work_mem TO '64MB';
ALTER role pspbatch_ro_user SET max_parallel_workers_per_gather TO 4;

--# create read-only(OLAP) user for RJF with permission to login and grant needful permissions #--
Alter user psprjf with password 'JsiHKoB7s#(5hW';
alter user psprjf login password 'JsiHKoB7s#(5hW';
--grant read only role
grant pspadm_readonly_role to psprjf;
--set search path
alter user psprjf set search_path to pspadm;
--set resource usage
ALTER role psprjf SET work_mem TO '64MB';
ALTER role psprjf SET max_parallel_workers_per_gather TO 4;



grant select on pg_stat_statements to pspadm_readwrite_role;



--# create read-write(OLAP) user/role with permission to login and grant needful permissions #--
create user pspbatch_rw_user with password 'YqXwHmW2s#(9iJ';
alter user pspbatch_rw_user login password 'YqXwHmW2s#(9iJ';
--grant read only role
grant pspadm_readwrite_role to pspbatch_rw_user;
--set search path
alter user pspbatch_rw_user set search_path to pspadm;
--set resource usage
ALTER role pspbatch_rw_user SET work_mem TO '64MB';
ALTER role pspbatch_rw_user SET max_parallel_workers_per_gather TO 4;

--# create read-only(OLAP) user/role with permission to login and grant needful permissions #--
create user pspbatch_ro_user with password 'DZysNtJ4s#(1lQ';
alter user pspbatch_ro_user login password 'DZysNtJ4s#(1lQ';

--grant read only role
grant pspadm_readonly_role to pspbatch_ro_user;
--set search path
alter user pspbatch_ro_user set search_path to pspadm;
--set resource usage
ALTER role pspbatch_ro_user SET work_mem TO '64MB';
ALTER role pspbatch_ro_user SET max_parallel_workers_per_gather TO 4;

--# create read-only(OLAP) user for RJF with permission to login and grant needful permissions #--
Alter user psprjf with password 'JsiHKoB7s#(5hW';
alter user psprjf login password 'JsiHKoB7s#(5hW';
--grant read only role
grant pspadm_readonly_role to psprjf;
--set search path
alter user psprjf set search_path to pspadm;
--set resource usage
ALTER role psprjf SET work_mem TO '64MB';
ALTER role psprjf SET max_parallel_workers_per_gather TO 4;


OLAP APG Database is ready, below are details:
Hostname: ppsp-stg-olap.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com
Port: 5432
DBname: pstgolap
--Read/write

Username: pspbatch_rw_user
password: YqXwHmW2s#(9iJ

--Read only:
Username: pspbatch_ro_user
Password; DZysNtJ4s#(1lQ

--psprjf
username: psprjf
password: JsiHKoB7s#(5hW


