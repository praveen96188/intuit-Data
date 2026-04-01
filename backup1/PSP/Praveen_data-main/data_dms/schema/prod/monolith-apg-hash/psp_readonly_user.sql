-- switch to psppp001 database
\c psppp001 pspadm_owner

--# create read-only user/role with permission to login and grant needful permissions #--
create user pspapp_readonly with password 'jW7ijCbfhv*89hdS';
--grant read only role to users
grant pspadm_readonly_role to pspapp_readonly;
--set search path for pspapp
alter user pspapp_readonly set search_path to pspadm;


create user psprjf with password 'ijCbfhv*89h';
--grant read only role
grant pspadm_readonly_role to psprjf;
--set search path
alter user psprjf set search_path to pspadm;