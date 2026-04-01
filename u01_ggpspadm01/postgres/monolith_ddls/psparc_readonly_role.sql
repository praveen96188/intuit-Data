--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database pspprdparc from public;

-- switch to pspprdparc database
\c pspprdparc

create role psparc_readonly_role;
grant connect on database pspprdparc to psparc_readonly_role;
grant usage on schema psparc to psparc_readonly_role;
grant select on all tables in schema psparc to psparc_readonly_role;
alter default privileges in schema psparc grant select on tables to psparc_readonly_role;

