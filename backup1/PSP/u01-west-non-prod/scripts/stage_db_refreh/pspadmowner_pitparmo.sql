grant create on database psppdarc1 to pspadm_owner;
grant usage, create on schema pspadm to pspadm_owner;
grant connect on database psppdarc1 to pspadm_readonly_role;
grant usage on schema pspadm to pspadm_readonly_role;
grant select on all tables in schema pspadm to pspadm_readonly_role;
grant select on pg_stat_statements  to pspadm_readonly_role;
alter default privileges in schema pspadm grant select on tables to pspadm_readonly_role;
grant connect on database pitparmo to pspadm_readwrite_role;
grant usage on schema pspadm to pspadm_readwrite_role;
grant select on pg_stat_statements  to pspadm_readwrite_role;
grant select on all tables in schema pspadm to pspadm_readonly_role;
alter default privileges in schema pspadm grant select on  tables to pspadm_readonly_role;
-- grant select, DML permission to all the existing tables and views if any to the role
grant select, insert, update, delete on all tables in schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission on all tables and views at later date to the role
alter default privileges in schema pspadm grant select, insert, update, delete on  tables to pspadm_readwrite_role;
-- grant select, DML permission to all the existing sequences if any to the role
grant select, usage on all sequences in schema pspadm to pspadm_readwrite_role;
-- grant select, DML permission to all the sequences created at later date to the role
alter default privileges in schema pspadm grant select, usage on sequences to pspadm_readwrite_role;
-- grant permission on functions, procedures
grant execute on all functions in schema pspadm to pspadm_readwrite_role;
grant execute on all procedures in schema pspadm to pspadm_readwrite_role;
-- grant permission on functions, procedures
alter default privileges in schema pspadm grant execute on functions to pspadm_readwrite_role;
