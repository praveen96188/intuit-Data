--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database psppp001 from public;

-- switch to psppp001 database
\c psppp001

create role pspadm_readwrite_role;
-- grant permission to connect the database
grant connect on database psppp001 to pspadm_readwrite_role;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema pspadm grant select, insert, update, delete on all tables to pspadm_readwrite_role;

-- grant select, DML permission to all the existing sequences if any to the role 
grant select, usage on all sequences in schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema pspadm grant select, usage on sequences to pspadm_readwrite_role;
-- grant permission on functions, procedures
grant execute on all functions in schema pspadm to pspadm_readwrite_role;
grant execute on all procedures in schema pspadm to pspadm_readwrite_role;
-- grant permission on functions, procedures
alter default privileges in schema pspadm grant execute on functions to pspadm_readwrite_role;


