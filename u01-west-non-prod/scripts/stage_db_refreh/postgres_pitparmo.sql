create user psp_prl_app with login;
--\password psp_prl_app
grant pspadm_readwrite_role to psp_prl_app;
alter user psp_prl_app set search_path to pspadm;
ALTER role psp_prl_app SET work_mem TO '16MB';
ALTER role psp_prl_app SET max_parallel_workers_per_gather TO 2;
 
--no need to create perf_test user until specifically asked
create user perf_test  with password 'XXXX';
alter user perf_test with password 'XXXX';
--grant read only role to users
grant pspadm_readwrite_role to perf_test;
grant usage, create on schema pspadm to perf_test;
--set search path for pspapp
alter user perf_test set search_path to pspadm;
grant select on pg_stat_statements to perf_test;
 
create user psp_prl_read with login;
--\password psp_prl_read
--grant read only role
grant pspadm_readonly_role to psp_prl_read;
--set search path
alter user psp_prl_read set search_path to pspadm;
--set resource usage
ALTER role psp_prl_read SET work_mem TO '64MB';
ALTER role psp_prl_read SET max_parallel_workers_per_gather TO 4;
 
--create below user only if required
create user psprjf with login;;
--grant read only role
grant pspadm_readonly_role to psprjf;
--set search path
alter user psprjf set search_path to pspadm;
--set resource usage
ALTER role psprjf SET work_mem TO '64MB';
ALTER role psprjf SET max_parallel_workers_per_gather TO 4;
 
--# create read-write(OLAP) user/role with permission to login and grant needful permissions #--
create user pspbatch_rw_user with login;
--\password pspbatch_rw_user
--grant read only role
grant pspadm_readwrite_role to pspbatch_rw_user;
--set search path
alter user pspbatch_rw_user set search_path to pspadm;
--set resource usage
ALTER role pspbatch_rw_user SET work_mem TO '64MB';
ALTER role pspbatch_rw_user SET max_parallel_workers_per_gather TO 4;
 
--# create read-only(OLAP) user/role with permission to login and grant needful permissions #--
create user pspbatch_ro_user with login;
--\password pspbatch_ro_user
--grant read only role
grant pspadm_readonly_role to pspbatch_ro_user;
--set search path
alter user pspbatch_ro_user set search_path to pspadm;
--set resource usage
ALTER role pspbatch_ro_user SET work_mem TO '64MB';
ALTER role pspbatch_ro_user SET max_parallel_workers_per_gather TO 4;
