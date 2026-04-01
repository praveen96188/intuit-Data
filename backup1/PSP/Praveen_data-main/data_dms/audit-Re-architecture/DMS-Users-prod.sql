--staging 
--monolith
DROP table "AWSDMS_CONTROL_TABLE"."awsdms_apply_exceptions";      
DROP table "AWSDMS_CONTROL_TABLE"."awsdms_history";               
DROP table "AWSDMS_CONTROL_TABLE"."awsdms_status";                
DROP table "AWSDMS_CONTROL_TABLE"."awsdms_suspended_tables";      
DROP table "AWSDMS_CONTROL_TABLE"."awsdms_validation_failures_v1";

DROP schema "AWSDMS_CONTROL_TABLE";
drop schema awsdms_control_schema;

drop function awsdms_ddl_audit.awsdms_intercept_ddl() cascade;
drop table awsdms_ddl_audit.awsdms_ddl_audit;
drop schema awsdms_ddl_audit;

--public schema
drop table public.awsdms_ddl_audit;

--audit

DROP table awsdms_control_table.awsdms_apply_exceptions ;
DROP table awsdms_control_table.awsdms_history  ;        
DROP table awsdms_control_table.awsdms_status    ;       
DROP table awsdms_control_table.awsdms_suspended_tables; 
DROP table awsdms_control_table.awsdms_full_load_exceptions;

drop schema awsdms_control_table;

drop function public.awsdms_intercept_ddl() cascade;
drop function awsdms_ddl_audit.awsdms_intercept_ddl() ;
drop table awsdms_ddl_audit.awsdms_ddl_audit;

--public schema
drop function public.awsdms_intercept_ddl() cascade;
drop table public.awsdms_ddl_audit;

--Create User source Postgres

CREATE USER  dms_apg_src WITH PASSWORD 'e#xc91kggPGH';
GRANT CONNECT ON DATABASE prodapgib to dms_apg_src;
GRANT CREATE ON DATABASE prodapgib  TO dms_apg_src;
GRANT USAGE ON SCHEMA ibobadm TO dms_apg_src;
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



--DMS Target Postgres



CREATE USER  dms_apg_tgt WITH PASSWORD 'e#xc91kggPGH';
GRANT CONNECT ON DATABASE prodapgib to dms_apg_tgt;
GRANT CREATE ON DATABASE prodapgib TO dms_apg_tgt;
GRANT CREATE ON SCHEMA ibobadm TO dms_apg_tgt;
GRANT USAGE ON SCHEMA ibobadm TO dms_apg_tgt;
alter default privileges in schema ibobadm grant select, insert, update, delete on  tables to dms_apg_tgt;
GRANT UPDATE, INSERT, SELECT, DELETE ON ALL TABLES IN SCHEMA ibobadm TO dms_apg_tgt;





--monolith 

--Create User source Postgres

CREATE USER  dms_apg_src WITH PASSWORD 'e#xc91kggPGH';
GRANT CONNECT ON DATABASE pspapg02 to dms_apg_src;
GRANT CREATE ON DATABASE pspapg02  TO dms_apg_src;
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



--DMS Target Postgres



CREATE USER  dms_apg_user WITH PASSWORD 'e#xc91kggPGH';
GRANT CONNECT ON DATABASE pspapg02 to dms_apg_user;
GRANT CREATE ON DATABASE pspapg02 TO dms_apg_user;
GRANT CREATE ON SCHEMA pspadm TO dms_apg_user;
GRANT USAGE ON SCHEMA pspadm TO dms_apg_user;
alter default privileges in schema pspadm grant select, insert, update, delete on  tables to dms_apg_user;
GRANT UPDATE, INSERT, SELECT, DELETE ON ALL TABLES IN SCHEMA pspadm TO dms_apg_user;

