#login Database 
#psql -h ppsp-stg-pitparmo-new-cluster.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -p 5432 -U postgres
--PIgRgK7d#(2XZ

alter user pspapp with NOLOGIN;
select pg_drop_replication_slot(slot_name) from pg_replication_slots;
alter database pspapg02 rename to pitparmo;
--connect to staging database as below
\c pitparmo
alter user pspadm_owner login password 'ppp3zu#JA7M5aa';
grant create on database pitparmo to pspadm_owner;
grant usage, create on schema pspadm to pspadm_owner;
grant connect on database pitparmo to pspadm_readonly_role;
grant usage on schema pspadm to pspadm_readonly_role;
grant select on all tables in schema pspadm to pspadm_readonly_role;
grant select on pg_stat_statements  to pspadm_readonly_role;
alter default privileges in schema pspadm grant select on tables to pspadm_readonly_role;
grant connect on database pitparmo to pspadm_readwrite_role;
grant usage on schema pspadm to pspadm_readwrite_role;
grant select on pg_stat_statements  to pspadm_readwrite_role;
exit

#login to pspadm_owner

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
grant pspadm_readwrite_role to psp_prl_app;
alter user psp_prl_app set search_path to pspadm;
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

create user psprjf with password 'JsiHKoB7s#(5hW';
Alter user psprjf with password 'JsiHKoB7s#(5hW';
--grant read only role
grant pspadm_readonly_role to psprjf;
--set search path
alter user psprjf set search_path to pspadm;
--set resource usage
ALTER role psprjf SET work_mem TO '64MB';
ALTER role psprjf SET max_parallel_workers_per_gather TO 4;

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

