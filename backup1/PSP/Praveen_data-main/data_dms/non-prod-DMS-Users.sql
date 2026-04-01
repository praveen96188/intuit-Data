CREATE USER  dms_apg_tgt WITH PASSWORD 'dms#123';
GRANT SET ON PARAMETER session_replication_role TO dms_apg_tgt;
GRANT CONNECT ON DATABASE ppdspg01 to dms_apg_tgt;
GRANT CREATE ON DATABASE ppdspg01 TO dms_apg_tgt;
GRANT CREATE ON SCHEMA pspadm TO dms_apg_tgt;
GRANT USAGE ON SCHEMA pspadm TO dms_apg_tgt;
alter default privileges in schema pspadm grant select, insert, update, delete on tables to dms_apg_tgt;
GRANT UPDATE, INSERT, SELECT, DELETE, TRUNCATE ON ALL TABLES IN SCHEMA pspadm TO dms_apg_tgt;




--create schema objects owner
create role  with login password 'Pp3zu#JA7M5';

-- grant permission to connect the database 
grant connect on database ppdspg01 to pspadm_owner;

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




--Oracle as Source
CREATE USER DMS_USER IDENTIFIED by "Dms#1234"
      DEFAULT TABLESPACE "PSP_DATA01"
      TEMPORARY TABLESPACE "TEMP"
      PROFILE "APPLICATION_HIGH_RISK_PROFILE";

GRANT CREATE SESSION to DMS_USER;
GRANT SELECT ANY TRANSACTION to DMS_USER;
GRANT SELECT on DBA_TABLESPACES to DMS_USER;
GRANT SELECT ANY TABLE TO DMS_USER;
GRANT EXECUTE on rdsadmin.rdsadmin_util to DMS_USER;
GRANT LOGMINING to DMS_USER; 
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_VIEWS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_TAB_PARTITIONS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_INDEXES', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_OBJECTS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_TABLES', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_USERS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_CATALOG', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_CONSTRAINTS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_CONS_COLUMNS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_TAB_COLS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_IND_COLUMNS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_LOG_GROUPS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$ARCHIVED_LOG', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOG', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOGFILE', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$DATABASE', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$THREAD', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$PARAMETER', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$NLS_PARAMETERS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$TIMEZONE_NAMES', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$TRANSACTION', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$CONTAINERS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBA_REGISTRY', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('OBJ$', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_ENCRYPTED_COLUMNS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOGMNR_LOGS', 'DMS_USER', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOGMNR_CONTENTS','DMS_USER','SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBMS_LOGMNR', 'DMS_USER', 'EXECUTE');

-- (as of Oracle versions 12.1 and higher)
exec rdsadmin.rdsadmin_util.grant_sys_object('REGISTRY$SQLPATCH', 'DMS_USER', 'SELECT');

-- (for Amazon RDS Active Dataguard Standby (ADG))
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$STANDBY_LOG', 'DMS_USER', 'SELECT'); 

-- (for transparent data encryption (TDE))

exec rdsadmin.rdsadmin_util.grant_sys_object('ENC$', 'DMS_USER', 'SELECT'); 
               
-- (for validation with LOB columns)
exec rdsadmin.rdsadmin_util.grant_sys_object('DBMS_CRYPTO', 'DMS_USER', 'EXECUTE');
                    
-- (for binary reader)
exec rdsadmin.rdsadmin_util.grant_sys_object('DBA_DIRECTORIES','DMS_USER','SELECT'); 
                    
-- Required when the source database is Oracle Data guard, and Oracle Standby is used in the latest release of DMS version 3.4.6, version 3.4.7, and higher.

exec rdsadmin.rdsadmin_util.grant_sys_object('V_$DATAGUARD_STATS', 'DMS_USER', 'SELECT');


GRANT READ ON DIRECTORY ARCHIVELOG_DIR_A TO DMS_USER;
GRANT READ ON DIRECTORY ARCHIVELOG_DIR_B TO DMS_USER;
GRANT READ ON DIRECTORY ONLINELOG_DIR_A TO DMS_USER;
GRANT READ ON DIRECTORY ONLINELOG_DIR_B TO DMS_USER;




--Create User source Postgres
CREATE USER  dms_apg_src WITH PASSWORD 'dms#123';
GRANT CONNECT ON DATABASE psppp01 to dms_apg_src;
GRANT CREATE ON DATABASE psppp01 TO dms_apg_src;
GRANT USAGE ON SCHEMA pspadm TO dms_apg_src;
Grant  rds_superuser to dms_apg_src;
Grant  rds_replication to dms_apg_src;
--connect dms_apg_user and create schema


Create schema awsdms_ddl_audit;
GRANT USAGE ON SCHEMA awsdms_ddl_audit TO dms_apg_src;
CREATE TABLE awsdms_ddl_audit.awsdms_ddl_audit
(
  c_key    bigserial primary key,
  c_time   timestamp,    -- Informational
  c_user   varchar(64),  -- Informational: current_user
  c_txn    varchar(16),  -- Informational: current transaction
  c_tag    varchar(24),  -- Either 'CREATE TABLE' or 'ALTER TABLE' or 'DROP TABLE'
  c_oid    integer,      -- For future use - TG_OBJECTID
  c_name   varchar(64),  -- For future use - TG_OBJECTNAME
  c_schema varchar(64),  -- For future use - TG_SCHEMANAME. For now - holds current_schema
  c_ddlqry  text         -- The DDL query associated with the current DDL event
);
                                               
CREATE OR REPLACE FUNCTION awsdms_ddl_audit.awsdms_intercept_ddl()
  RETURNS event_trigger
LANGUAGE plpgsql
SECURITY DEFINER
  AS $$
  declare _qry text;
BEGIN
  if (tg_tag='CREATE TABLE' or tg_tag='ALTER TABLE' or tg_tag='DROP TABLE') then
         SELECT current_query() into _qry;
         insert into awsdms_ddl_audit.awsdms_ddl_audit
         values
         (
         default,current_timestamp,current_user,cast(TXID_CURRENT()as varchar(16)),tg_tag,0,'',current_schema,_qry
         );
         delete from awsdms_ddl_audit.awsdms_ddl_audit;
end if;
END;
$$;
                        
CREATE EVENT TRIGGER awsdms_intercept_ddl ON ddl_command_end 
EXECUTE PROCEDURE awsdms_ddl_audit.awsdms_intercept_ddl();

grant all on awsdms_ddl_audit.awsdms_ddl_audit to public;
grant all on awsdms_ddl_audit.awsdms_ddl_audit_c_key_seq to public;




--Target Oracle 
CREATE USER DMS_USER_TGT IDENTIFIED by "Dms#12345"
      DEFAULT TABLESPACE "PSP_DATA01"
      TEMPORARY TABLESPACE "TEMP"
      PROFILE "APPLICATION_HIGH_RISK_PROFILE";

GRANT CREATE SESSION to DMS_USER_TGT;
GRANT LOCK ANY TABLE to DMS_USER_TGT;
GRANT CREATE ANY INDEX to DMS_USER_TGT;
GRANT DROP ANY TABLE TO DMS_USER_TGT;
GRANT SELECT ANY TABLE TO DMS_USER_TGT;
GRANT INSERT ANY TABLE TO DMS_USER_TGT;
GRANT UPDATE ANY TABLE TO DMS_USER_TGT;
GRANT CREATE ANY VIEW TO DMS_USER_TGT;
GRANT DROP ANY VIEW TO DMS_USER_TGT;
GRANT CREATE ANY PROCEDURE TO DMS_USER_TGT;
GRANT ALTER ANY PROCEDURE TO DMS_USER_TGT;
GRANT DROP ANY PROCEDURE TO DMS_USER_TGT;
GRANT CREATE ANY SEQUENCE TO DMS_USER_TGT;
GRANT ALTER ANY SEQUENCE TO DMS_USER_TGT;
GRANT DROP ANY SEQUENCE TO DMS_USER_TGT;
GRANT DELETE ANY TABLE TO DMS_USER_TGT;
GRANT CREATE ANY TABLE TO DMS_USER_TGT;

exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_VIEWS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_TAB_PARTITIONS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_INDEXES', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_OBJECTS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_TABLES', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_USERS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_CATALOG', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_CONSTRAINTS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_CONS_COLUMNS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_TAB_COLS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_IND_COLUMNS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_LOG_GROUPS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$ARCHIVED_LOG', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOG', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOGFILE', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$DATABASE', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$THREAD', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$PARAMETER', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$NLS_PARAMETERS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$TIMEZONE_NAMES', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$TRANSACTION', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$CONTAINERS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBA_REGISTRY', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('OBJ$', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('ALL_ENCRYPTED_COLUMNS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOGMNR_LOGS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOGMNR_CONTENTS','DMS_USER_TGT','SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBMS_LOGMNR', 'DMS_USER_TGT', 'EXECUTE');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBA_USERS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBA_TAB_PRIVS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBA_OBJECTS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBA_SYNONYMS', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBA_SEQUENCES', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBA_TYPES', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBA_INDEXES','DMS_USER_TGT','SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBA_TABLES', 'DMS_USER_TGT', 'SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('DBA_TRIGGERS', 'DMS_USER_TGT', 'SELECT');

CREATE USER "AWSDMS_CONTROL_TABLE" IDENTIFIED BY "AwsCon#123"
      DEFAULT TABLESPACE "PSP_DATA01"
      TEMPORARY TABLESPACE "TEMP"
      PROFILE "APPLICATION_HIGH_RISK_PROFILE";


   GRANT "CONNECT" TO "AWSDMS_CONTROL_TABLE";
   GRANT "RESOURCE" TO "AWSDMS_CONTROL_TABLE";
   GRANT "SCHEDULER_ADMIN" TO "AWSDMS_CONTROL_TABLE";


  GRANT ALTER ANY TABLE TO "AWSDMS_CONTROL_TABLE";
  GRANT CREATE ANY TABLE TO "AWSDMS_CONTROL_TABLE";
  GRANT UNLIMITED TABLESPACE TO "AWSDMS_CONTROL_TABLE";


   ALTER USER "AWSDMS_CONTROL_TABLE" DEFAULT ROLE ALL;


*/30 * * * * cd /u01/postgres/scripts; ./chk_dms_apply_exceptions.sh psphpp07 psphpp07 1>/dev/null 2>&1




./dms_cdc.sh ppdspg02dr ppsphp07 task1 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppdspg02dr ppsphp07 task2 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppdspg02dr ppsphp07 task3 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppdspg02dr ppsphp07 task4 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppdspg02dr ppsphp07 task5 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppdspg02dr ppsphp07 task6 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppdspg02dr ppsphp07 task7 arn:aws:dms:us-west-2:152430470825:rep:55TRK6FXIG7T7AT4XOPWAL3LWME55PE4OMGHO3I
./dms_cdc.sh ppdspg02dr ppsphp07 task8 arn:aws:dms:us-west-2:152430470825:rep:55TRK6FXIG7T7AT4XOPWAL3LWME55PE4OMGHO3I
