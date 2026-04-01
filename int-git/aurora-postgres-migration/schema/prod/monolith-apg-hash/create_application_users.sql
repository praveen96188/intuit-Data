--create schema objects owner
create role pspadm_owner with login password 'xxxxx';

-- grant permission to connect the database 
grant connect on database pitparmo to pspadm_owner;

-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage, create on schema pspadm to pspadm_owner;

-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema pspadm to pspadm_owner;

-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema pspadm grant select, insert, update, delete on tables to pspadm_owner;

-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema pspadm to pspadm_owner;

-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema pspadm grant usage on sequences to pspadm_owner;

--set search path for ibobadm_owner
alter user pspadm_owner set search_path to pspadm;

--create application user
create role psp_prl_pspapp with login password 'xxxxxxxxx';

-- grant permission to connect the database 
grant connect on database pitparmo to psp_prl_pspapp;

-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema pspadm to psp_prl_pspapp;

-- connect to owner user and grant access to psp_prl_pspapp
\c pitparmo pspadm_owner

-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema pspadm to psp_prl_pspapp;

-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema pspadm grant select, insert, update, delete on tables to psp_prl_pspapp;

-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema pspadm to psp_prl_pspapp;

-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema pspadm grant usage on sequences to psp_prl_pspapp;

--set search path for psp_prl_pspapp
alter user psp_prl_pspapp set search_path to pspadm;

--create readonly user
create role pspadm_readonly with login password 'xxxxxxxx';

-- grant permission to connect the database 
grant connect on database pitparmo to pspadm_readonly;

-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema pspadm to pspadm_readonly;

-- grant select permission to all the existing tables and views if any to the role 
grant select on all tables in schema pspadm to pspadm_readonly; 

-- grant select permission on all tables and views created at later date to the role 
alter default privileges in schema pspadm grant select on tables to pspadm_readonly;

--set search path for readonly 
alter user pspadm_readonly set search_path to pspadm;