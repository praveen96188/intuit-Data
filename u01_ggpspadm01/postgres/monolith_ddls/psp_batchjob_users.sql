-- us-west-2 users
-- switch to psppp001 database
\c psppp001 pspadm_owner

--# create read-write(OLAP) user/role with permission to login and grant needful permissions #--
create user pspbatch_rw_user with password 'xxxxxx';
--grant read only role
grant pspadm_readwrite_role to pspbatch_rw_user;
--set search path
alter user pspbatch_rw_user set search_path to pspadm;
--set resource usage
ALTER role pspbatch_rw_user SET work_mem TO '64MB';
ALTER role pspbatch_rw_user SET max_parallel_workers_per_gather TO 4;

--# create read-only(OLAP) user/role with permission to login and grant needful permissions #--
create user pspbatch_ro_user with password 'xxxxxx';
--grant read only role
grant pspadm_readonly_role to pspbatch_ro_user;
--set search path
alter user pspbatch_ro_user set search_path to pspadm;
--set resource usage
ALTER role pspbatch_ro_user SET work_mem TO '64MB';
ALTER role pspbatch_ro_user SET max_parallel_workers_per_gather TO 4;

--# create read-only(OLAP) user for RJF with permission to login and grant needful permissions #--
create user psprjf with password 'xxxxx';
--grant read only role
grant pspadm_readonly_role to psprjf;
--set search path
alter user psprjf set search_path to pspadm;
--set resource usage
ALTER role psprjf SET work_mem TO '64MB';
ALTER role psprjf SET max_parallel_workers_per_gather TO 4;

--# create read-only(OLAP) user for PayrollDM with permission to login and grant needful permissions #--
create user psp_payroll_dm with password 'xxxxx';
--grant read only role
grant pspadm_readonly_role to psp_payroll_dm;
--set search path
alter user psp_payroll_dm set search_path to pspadm;
--set resource usage
ALTER role psp_payroll_dm SET work_mem TO '64MB';
ALTER role psp_payroll_dm SET max_parallel_workers_per_gather TO 4;

--# create read-only(OLAP) user for Finance downstreams jobs with permission to login and grant needful permissions #--
create user pspqsfinadmin with password 'xxxxxx';
grant pspadm_readonly_role to pspqsfinadmin;
alter user pspqsfinadmin set search_path to pspadm;
--set resource usage
ALTER role pspqsfinadmin SET work_mem TO '64MB';
ALTER role pspqsfinadmin SET max_parallel_workers_per_gather TO 4;




-- us-east-2 users
-- switch to pspapg02 database
\c pspapg02 pspadm_owner

--# create read-write(OLAP) user/role with permission to login and grant needful permissions #--
create user pspbatch_rw_user_ue2 with password 'xxxxxx';
--grant read only role
grant pspadm_readwrite_role to pspbatch_rw_user_ue2;
--set search path
alter user pspbatch_rw_user_ue2 set search_path to pspadm;
--set resource usage
ALTER role pspbatch_rw_user_ue2 SET work_mem TO '64MB';
ALTER role pspbatch_rw_user_ue2 SET max_parallel_workers_per_gather TO 4;

--# create read-only(OLAP) user/role with permission to login and grant needful permissions #--
create user pspbatch_ro_user_ue2 with password 'xxxxxx';
--grant read only role
grant pspadm_readonly_role to pspbatch_ro_user_ue2;
--set search path
alter user pspbatch_ro_user_ue2 set search_path to pspadm;
--set resource usage
ALTER role pspbatch_ro_user_ue2 SET work_mem TO '64MB';
ALTER role pspbatch_ro_user_ue2 SET max_parallel_workers_per_gather TO 4;

--# create read-only(OLAP) user for RJF with permission to login and grant needful permissions #--
create user psprjf_ue2 with password 'xxxxx';
--grant read only role
grant pspadm_readonly_role to psprjf_ue2;
--set search path
alter user psprjf_ue2 set search_path to pspadm;
--set resource usage
ALTER role psprjf_ue2 SET work_mem TO '64MB';
ALTER role psprjf_ue2 SET max_parallel_workers_per_gather TO 4;

--# create read-only(OLAP) user for PayrollDM with permission to login and grant needful permissions #--
create user psp_payroll_dm_ue2 with password 'xxxxxx';
--grant read only role
grant pspadm_readonly_role to psp_payroll_dm_ue2;
--set search path
alter user psp_payroll_dm_ue2 set search_path to pspadm;
--set resource usage
ALTER role psp_payroll_dm_ue2 SET work_mem TO '64MB';
ALTER role psp_payroll_dm_ue2 SET max_parallel_workers_per_gather TO 4;
