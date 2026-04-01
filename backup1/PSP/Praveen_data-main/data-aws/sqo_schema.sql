--create schema objects owner
create role soadm_owner with login password 'Bl8p#uOPT1s';

--create schema for objects
create schema soadm;

-- grant permission to connect the database 
grant connect on database ppspsodb to soadm_owner;

-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage, create on schema soadm to soadm_owner;

-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema soadm to soadm_owner;

-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema soadm grant select, insert, update, delete on tables to soadm_owner;

-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema soadm to soadm_owner;

-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema soadm grant usage on sequences to soadm_owner;

--set search path for ibobadm_owner
alter user soadm_owner set search_path to soadm;

--create application user
create role soapp with login password 'Ez#Ga3h8uFi';

-- grant permission to connect the database 
grant connect on database ppspsodb to soapp;

-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema soadm to soapp;

-- connect to owner user and grant access to soapp
\c ppspsodb soadm_owner

-- grant select, DML permission to all the existing tables and views if any to the role 
grant select, insert, update, delete on all tables in schema soadm to soapp;

-- grant select, DML permission on all tables and views at later date to the role 
alter default privileges in schema soadm grant select, insert, update, delete on tables to soapp;

-- grant select, DML permission to all the existing sequences if any to the role 
grant usage on all sequences in schema soadm to soapp;

-- grant select, DML permission to all the sequences created at later date to the role 
alter default privileges in schema soadm grant usage on sequences to soapp;

--set search path for soapp
alter user soapp set search_path to soadm;

--create readonly user
create role soadm_readonly with login password 'FGz#Ga3Pdg';

-- grant permission to connect the database 
grant connect on database ppspsodb to soadm_readonly;

-- grant permission to the role to perform activity on the schema, mandatory for any permission including object level permission to work 
grant usage on schema soadm to soadm_readonly;

-- grant select permission to all the existing tables and views if any to the role 
grant select on all tables in schema soadm to soadm_readonly; 

-- grant select permission on all tables and views created at later date to the role 
alter default privileges in schema soadm grant select on tables to soadm_readonly;

--set search path for readonly 
alter user soadm_readonly set search_path to soadm;

-- create tables
CREATE TABLE asset_details (
    asset_id VARCHAR(255) NOT NULL,
    data_source_name VARCHAR(255) NOT NULL,
    environment VARCHAR(255) NOT NULL,
    isStatCollectionScheduled BOOLEAN NOT NULL DEFAULT TRUE,
    schedule VARCHAR(255),
    version INTEGER DEFAULT 1,
    created_date TIMESTAMP DEFAULT NOW(),
    modified_date TIMESTAMP DEFAULT NOW(),
    creator_id VARCHAR(255) DEFAULT 'tool',
    modifier_id VARCHAR(255) DEFAULT 'tool',
    PRIMARY KEY (asset_id, data_source_name, environment)
);

CREATE TABLE index_statistics (
    index_statistics_id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_fk VARCHAR(255) NOT NULL,
    data_source_fk VARCHAR(255) NOT NULL,
    environment_fk VARCHAR(255) NOT NULL,
    indexed_table_name VARCHAR(255) NOT NULL,
    index_name VARCHAR(255) NOT NULL,
    column_names TEXT NOT NULL,
    version INTEGER DEFAULT 1,
    created_date TIMESTAMP DEFAULT NOW(),
    modified_date TIMESTAMP DEFAULT NOW(),
    creator_id VARCHAR(255) DEFAULT 'tool',
    modifier_id VARCHAR(255) DEFAULT 'tool',
    FOREIGN KEY (asset_fk, data_source_fk, environment_fk)
        REFERENCES asset_details(asset_id, data_source_name, environment)
);

CREATE TABLE partition_statistics (
    partition_statistics_id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_fk VARCHAR(255) NOT NULL,
    data_source_fk VARCHAR(255) NOT NULL,
    environment_fk VARCHAR(255) NOT NULL,
    part_table_name VARCHAR(255) NOT NULL,
    part_key VARCHAR(255) NOT NULL,
    part_strategy VARCHAR(255) NOT NULL,
    version INTEGER DEFAULT 1,
    created_date TIMESTAMP DEFAULT NOW(),
    modified_date TIMESTAMP DEFAULT NOW(),
    creator_id VARCHAR(255) DEFAULT 'tool',
    modifier_id VARCHAR(255) DEFAULT 'tool',
    FOREIGN KEY (asset_fk, data_source_fk, environment_fk)
        REFERENCES asset_details(asset_id, data_source_name, environment)
);

CREATE TABLE schema_details (
    schema_details_id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_fk VARCHAR(255) NOT NULL,
    data_source_fk VARCHAR(255) NOT NULL,
    environment_fk VARCHAR(255) NOT NULL,
    table_name VARCHAR(255) NOT NULL,
    schema_details TEXT NOT NULL,
    version INTEGER DEFAULT 1,
    created_date TIMESTAMP DEFAULT NOW(),
    modified_date TIMESTAMP DEFAULT NOW(),
    creator_id VARCHAR(255) DEFAULT 'tool',
    modifier_id VARCHAR(255) DEFAULT 'tool',
    FOREIGN KEY (asset_fk, data_source_fk, environment_fk)
        REFERENCES asset_details(asset_id, data_source_name, environment)
);

CREATE TABLE query_plan (
    query_plan_id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_fk VARCHAR(255) NOT NULL,
    data_source_fk VARCHAR(255) NOT NULL,
    environment_fk VARCHAR(255) NOT NULL,
    query TEXT NOT NULL,
    explain_plan TEXT NOT NULL,
    optimization_result TEXT,
    version INTEGER DEFAULT 1,
    created_date TIMESTAMP DEFAULT NOW(),
    modified_date TIMESTAMP DEFAULT NOW(),
    creator_id VARCHAR(255) DEFAULT 'tool',
    modifier_id VARCHAR(255) DEFAULT 'tool',
    status  varchar(30) NOT NULL,
    FOREIGN KEY (asset_fk, data_source_fk, environment_fk)
    REFERENCES asset_details(asset_id, data_source_name, environment)
);