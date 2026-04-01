--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database prodapgib from public;

-- switch to prodapgib database
\c prodapgib
--# create schema into the database #--
create schema ibobadm;

--# create owner user/role with permission to login and grant needful permissions. This will be used for any DB Deployment #--
create role ibobadm_owner with login password *******;
-- grant permission to connect the database 
grant connect on database ibobadm to ibobadm_owner;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage, create on schema ibobadm to ibobadm_owner;
-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema ibobadm to ibobadm_owner;
-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema ibobadm grant select, insert, update, delete on tables to ibobadm_owner;
-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema ibobadm to ibobadm_owner;
-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema ibobadm grant usage on sequences to ibobadm_owner;

--# create read-write application user/role with permission to login and grant needful permissions #--
create role ibob_prod_pspapp with login password *******;
-- grant permission to connect the database 
grant connect on database ibobadmdb to ibob_prod_pspapp;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema ibobadm to ibob_prod_pspapp;

--# create read-only user/role with permission to login and grant needful permissions #--
create role ibobadm_readonly with login password *******;
-- grant permission to connect the database 
grant connect on database ibobadmdb to ibobadm_readonly;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema ibobadm to ibobadm_readonly; 

-- connect to owner user and grant access to ibob_prod_pspapp
\c prodapgib ibobadm_owner
-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema ibobadm to ibob_prod_pspapp;
-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema ibobadm grant select, insert, update, delete on tables to ibob_prod_pspapp;
-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema ibobadm to ibob_prod_pspapp;
-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema ibobadm grant usage on sequences to ibob_prod_pspapp;

-- grant access to ibobadm_readonly
-- grant select permission to all the existing tables and views if any to the role 
grant select on all tables in schema ibobadm to ibobadm_readonly; 
-- grant select permission on all tables and views created at later date to the role 
alter default privileges in schema ibobadm grant select on tables to ibobadm_readonly;

--set search path for ibob_prod_pspapp
alter user ibob_prod_pspapp set search_path to ibobadm;
alter user ibobadm_readonly set search_path to ibobadm;
alter user ibobadm_owner set search_path to ibobadm;
