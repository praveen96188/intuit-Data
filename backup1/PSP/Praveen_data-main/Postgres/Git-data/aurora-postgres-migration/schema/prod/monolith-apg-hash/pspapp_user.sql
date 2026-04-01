-- switch to psppp001 database
\c psppp001

-- create read-write(OLTP) application user/role with permission to login and grant needful permissions #--
create user pspapp with password '*******';
-- grant read write role to user
grant pspadm_readwrite_role to pspapp;
--set search path for pspapp
alter user pspapp set search_path to pspadm;
--set resource usage
ALTER role pspapp SET work_mem TO '16MB';
ALTER role pspapp SET max_parallel_workers_per_gather TO 2;

--# create read-only(OLTP) user/role with permission to login and grant needful permissions #--
create user pspread with password 'xxxxxxxx';
--grant read only role to users
grant pspadm_readonly_role to pspread;
--set search path for pspapp
alter user pspread set search_path to pspadm;
--set resource usage
ALTER role pspread SET work_mem TO '16MB';
ALTER role pspread SET max_parallel_workers_per_gather TO 2;