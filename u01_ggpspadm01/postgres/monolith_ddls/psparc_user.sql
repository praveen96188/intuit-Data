-- switch to pspprdparc database
\c pspprdparc

-- create read-write(OLTP) application user/role with permission to login and grant needful permissions #--
create user psparc with password '*******';
-- grant read write role to user
grant psparc_readwrite_role to psparc;
--set search path for psparc
alter user psparc set search_path to psparc;
--set resource usage
ALTER role psparc SET work_mem TO '16MB';
ALTER role psparc SET max_parallel_workers_per_gather TO 2;

--# create read-only(OLTP) user/role with permission to login and grant needful permissions #--
create user psparc_read with password 'xxxxxxxx';
--grant read only role to users
grant psparc_readonly_role to psparc_read;
--set search path for psparc
alter user psparc_read set search_path to psparc;
--set resource usage
ALTER role psparc_read SET work_mem TO '16MB';
ALTER role psparc_read SET max_parallel_workers_per_gather TO 2;
