-- switch to psppp001 database
\c psppp001 pspadm_owner

--# create read-only user/role with permission to login and grant needful permissions #--
create user pspapp_batchjobs with password 'xxxxx';
--grant read only role to users
grant pspadm_readonly_role to pspapp_batchjobs;
--set search path for pspapp
alter user pspapp_batchjobs set search_path to pspadm;

ALTER USER pspapp_batchjobs SET work_mem = '50MB';
ALTER USER pspapp_batchjobs SET maintenance_work_mem = '50MB';


