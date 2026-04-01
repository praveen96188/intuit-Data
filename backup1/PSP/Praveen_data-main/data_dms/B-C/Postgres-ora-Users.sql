
--Create User source Postgres
CREATE USER  dms_apg_src WITH PASSWORD 'e#xc91kggPGH';
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
CREATE USER DMS_USER_TGT IDENTIFIED by "e#xc91kggPGH"
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



