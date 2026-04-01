--create schema objects owner
create role psparc_owner with login password 'xxxxx';

-- grant permission to connect the database 
grant connect on database pspprdparc to psparc_owner;

-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage, create on schema psparc to psparc_owner;

-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema psparc to psparc_owner;

-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema psparc grant select, insert, update, delete on tables to psparc_owner;

-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema psparc to psparc_owner;

-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema psparc grant usage on sequences to psparc_owner;

--set search path for ibobadm_owner
alter user psparc_owner set search_path to psparc;

--create application user
create role psp_arc_app with login password 'xxxxxxxxx';

-- grant permission to connect the database 
grant connect on database pspprdparc to psp_arc_app;

-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema psparc to psp_arc_app;

-- connect to owner user and grant access to psp_arc_app
\c pspprdparc psparc_owner

-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema psparc to psp_arc_app;

-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema psparc grant select, insert, update, delete on tables to psp_arc_app;

-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema psparc to psp_arc_app;

-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema psparc grant usage on sequences to psp_arc_app;

--set search path for psp_arc_app
alter user psp_arc_app set search_path to psparc;

--create readonly user
create role psparc_readonly with login password 'xxxxxxxx';

-- grant permission to connect the database 
grant connect on database pspprdparc to psparc_readonly;

-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema psparc to psparc_readonly;

-- grant select permission to all the existing tables and views if any to the role 
grant select on all tables in schema psparc to psparc_readonly; 

-- grant select permission on all tables and views created at later date to the role 
alter default privileges in schema psparc grant select on tables to psparc_readonly;

--set search path for readonly 
alter user psparc_readonly set search_path to psparc;
