create user perf_test with password 'xxxx';
--grant read only role to users
grant pspadm_readwrite_role to perf_test;
grant usage, create on schema pspadm to perf_test;
--set search path for pspapp
alter user perf_test set search_path to pspadm;

grant select on pg_stat_activity,pg_stat_statements to perf_test;

