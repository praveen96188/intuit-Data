--create database
create database pspval;
--connect to newly created database
\c pspval
--create schema
create schema pspval;
-- create read-write application user/role with permission to login and grant needful permissions #--
create role pspval with login password 'xxxx';
-- grant permission to connect the database 
grant connect on database pspval to pspval;
-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema pspval to pspval;
grant usage, create on schema pspval to pspval;

alter user pspval set search_path to pspval;