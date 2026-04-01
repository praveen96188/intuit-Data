-- create read-write(OLTP) application user/role with permission to login and grant needful permissions #--
create user pspapp with password 'NLjPxCf9v#(5eG';
-- grant read write role to user
grant pspadm_readwrite_role to pspapp;
--set search path for pspapp
alter user pspapp set search_path to pspadm;
--set resource usage
ALTER role pspapp SET work_mem TO '16MB';
ALTER role pspapp SET max_parallel_workers_per_gather TO 2;

--# create read-only(OLTP) user/role with permission to login and grant needful permissions #--
create user pspread with password 'TsLqAcK4i#(1cV';
--grant read only role to users
grant pspadm_readonly_role to pspread;
--set search path for pspapp
alter user pspread set search_path to pspadm;
--set resource usage
ALTER role pspread SET work_mem TO '16MB';
ALTER role pspread SET max_parallel_workers_per_gather TO 2;

--batchjob
--# create read-write(OLAP) user/role with permission to login and grant needful permissions #--
create user pspbatch_rw_user with password 'JvLzGuQ7s#(1fH';
--grant read only role
grant pspadm_readwrite_role to pspbatch_rw_user;
--set search path
alter user pspbatch_rw_user set search_path to pspadm;
--set resource usage
ALTER role pspbatch_rw_user SET work_mem TO '64MB';
ALTER role pspbatch_rw_user SET max_parallel_workers_per_gather TO 4;

--# create read-only(OLAP) user/role with permission to login and grant needful permissions #--
create user pspbatch_ro_user with password 'UoEsDtR3s#(9cS';
--grant read only role
grant pspadm_readonly_role to pspbatch_ro_user;
--set search path
alter user pspbatch_ro_user set search_path to pspadm;
--set resource usage
ALTER role pspbatch_ro_user SET work_mem TO '64MB';
ALTER role pspbatch_ro_user SET max_parallel_workers_per_gather TO 4;