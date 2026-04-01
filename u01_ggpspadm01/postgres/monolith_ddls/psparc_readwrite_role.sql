--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database pspprdparc from public;

-- switch to pspprdparc database
\c pspprdparc

create role psparc_readwrite_role;
-- grant permission to connect the database
grant connect on database pspprdparc to psparc_readwrite_role;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema psparc to psparc_readwrite_role;
-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema psparc to psparc_readwrite_role;
-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema psparc grant select, insert, update, delete on tables to psparc_readwrite_role;

-- grant select, DML permission to all the existing sequences if any to the role 
grant select, usage on all sequences in schema psparc to psparc_readwrite_role;
-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema psparc grant select, usage on sequences to psparc_readwrite_role;
-- grant permission on functions, procedures
grant execute on all functions in schema psparc to psparc_readwrite_role;
grant execute on all procedures in schema psparc to psparc_readwrite_role;
-- grant permission on functions, procedures
alter default privileges in schema psparc grant execute on functions to psparc_readwrite_role;


