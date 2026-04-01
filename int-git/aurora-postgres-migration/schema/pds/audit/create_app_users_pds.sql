--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database pdsibobdb from public;

-- switch to pdsibobdb database
\c pdsibobdb
--# create schema into the database #--
create schema ibobadm_pds;

--# create owner user/role with permission to login and grant needful permissions. This will be used for any DB Deployment #--
create role ibobadm_pds_owner with login password xxxxxxxxxx;
-- grant permission to connect the database 
grant connect on database pdsibobdb to ibobadm_pds_owner;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage, create on schema ibobadm_pds to ibobadm_pds_owner;
-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema ibobadm_pds to ibobadm_pds_owner;
-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema ibobadm_pds grant select, insert, update, delete on tables to ibobadm_pds_owner;
-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema ibobadm_pds to ibobadm_pds_owner;
-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema ibobadm_pds grant usage on sequences to ibobadm_pds_owner;

--# create read-write application user/role with permission to login and grant needful permissions #--
create role ibob_pds_pspapp with login password xxxxxxxxxx;
-- grant permission to connect the database 
grant connect on database pdsibobdb to ibob_pds_pspapp;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema ibobadm_pds to ibob_pds_pspapp;

--# create read-only user/role with permission to login and grant needful permissions #--
create role ibobadm_pds_readonly with login password xxxxxxxxxx;
-- grant permission to connect the database 
grant connect on database pdsibobdb to ibobadm_pds_readonly;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema ibobadm_pds to ibobadm_pds_readonly; 

-- connect to owner user and grant access to ibob_pds_pspapp
\c pdsibobdb ibobadm_pds_owner
-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema ibobadm_pds to ibob_pds_pspapp;
-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema ibobadm_pds grant select, insert, update, delete on tables to ibob_pds_pspapp;
-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema ibobadm_pds to ibob_pds_pspapp;
-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema ibobadm_pds grant usage on sequences to ibob_pds_pspapp;

-- grant access to ibobadm_pds_readonly
-- grant select permission to all the existing tables and views if any to the role 
grant select on all tables in schema ibobadm_pds to ibobadm_pds_readonly; 
-- grant select permission on all tables and views created at later date to the role 
alter default privileges in schema ibobadm_pds grant select on tables to ibobadm_pds_readonly;