--# secure public schema from being junkyard #--
revoke create on schema public from public;

--# secure data for unauthorised access #--
revoke all on database psppp01 from public;

-- switch to psppp01 database
\c psppp01

create role pspadm_readwrite_role;
-- grant permission to connect the database
grant connect on database psppp01 to pspadm_readwrite_role;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema pspadm to pspadm_readwrite_role;
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












create role ops_user with login password 'opsuser#123';

grant connect on database ppdspg01 to ops_user;

grant usage on schema pspadm to ops_user;

grant select on all tables in schema pspadm to ops_user; 

alter default privileges in schema pspadm grant select on tables to ops_user;

 alter user ops_user set search_path to pspadm;



grant  pg_read_all_data to ops_user;

grant  pg_read_all_settings to ops_user;

grant  pg_read_all_stats to ops_user;

grant  pg_stat_scan_tables to ops_user;


Hash Read Replica ppsphpdg (B)
Host:ppsphpdg.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com
port:1521
DB:PPSPHP01

APG Hash ppsp-pds-uw01 (C3)
Host: ppsp-pds-uw01.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com
port: 5432
DB:ppdspg01
username:ops_user
password:opsuser#123

Oracle Hash ppsphp06 (D3)
Host: ppsphp06.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com
port:1521
DB: ppsphp06


