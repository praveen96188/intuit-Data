--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database psppp01 from public;

-- switch to pspapg02 database
\c psppp01

create role pspadm_readonly_role;
grant connect on database psppp01 to pspadm_readonly_role;
grant usage on schema pspadm to pspadm_readonly_role;
grant select on all tables in schema pspadm to pspadm_readonly_role;
alter default privileges in schema pspadm grant select on tables to pspadm_readonly_role;