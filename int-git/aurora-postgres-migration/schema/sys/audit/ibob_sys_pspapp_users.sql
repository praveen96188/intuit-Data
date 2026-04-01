--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database sysibobdb from public;

-- switch to sysibobdb database
\c sysibobdb
--# create schema into the database #--
create schema ibobadm;

--# create owner user/role with permission to login and grant needful permissions. This will be used for any DB Deployment #--
create role ibobadm_owner with login password xxxxxxxxxx
-- grant permission to connect the database 
grant connect on database sysibobdb to ibobadm_owner;
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
create role ibob_sys_pspapp with login password xxxxxxxxxx
-- grant permission to connect the database 
grant connect on database ibobadmdb to ibob_sys_pspapp;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema ibobadm to ibob_sys_pspapp;

--# create read-only user/role with permission to login and grant needful permissions #--
create role ibobadm_sys_readonly with login password xxxxxxxxxx
-- grant permission to connect the database 
grant connect on database ibobadmdb to ibobadm_sys_readonly;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema ibobadm to ibobadm_sys_readonly; 

-- connect to owner user and grant access to ibob_sys_pspapp
\c sysibobdb ibobadm_owner
-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema ibobadm to ibob_sys_pspapp;
-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema ibobadm grant select, insert, update, delete on tables to ibob_sys_pspapp;
-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema ibobadm to ibob_sys_pspapp;
-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema ibobadm grant usage on sequences to ibob_sys_pspapp;

-- grant access to ibobadm_sys_readonly
-- grant select permission to all the existing tables and views if any to the role 
grant select on all tables in schema ibobadm to ibobadm_sys_readonly; 
-- grant select permission on all tables and views created at later date to the role 
alter default privileges in schema ibobadm grant select on tables to ibobadm_sys_readonly;