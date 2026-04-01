
Restored DB:

Host:ppsphp05.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com
port:1521
DB:ppsphp05
Intuadmin: changeme


Postgres DB:

Host:ppsp-pds-uw02.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com
Port:5432
DB:ppdspg02
Postgre: changeme


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


rds.logical_replication=1
wal_sender_timeout=0,
max_slot_wal_keep_size should be default

--Create User source Postgres

CREATE USER  dms_apg_src WITH PASSWORD '';
GRANT USAGE ON SCHEMA pspadm TO dms_apg_src;
GRANT CONNECT ON DATABASE psppp01 to dms_apg_src;
GRANT CREATE ON DATABASE psppp01 TO dms_apg_src;
Grant role rds_superuser to dms_apg_src;
Grant role rds_replication to dms_apg_src;



CREATE TABLE pspadm.awsdms_ddl_audit
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
                                               

CREATE OR REPLACE FUNCTION pspadm.awsdms_intercept_ddl()
  RETURNS event_trigger
LANGUAGE plpgsql
SECURITY DEFINER
  AS $$
  declare _qry text;
BEGIN
  if (tg_tag='CREATE TABLE' or tg_tag='ALTER TABLE' or tg_tag='DROP TABLE') then
         SELECT current_query() into _qry;
         insert into pspadm..awsdms_ddl_audit
         values
         (
         default,current_timestamp,current_user,cast(TXID_CURRENT()as varchar(16)),tg_tag,0,'',current_schema,_qry
         );
         delete from pspadm..awsdms_ddl_audit;
end if;
END;
$$;
                        

--connect user and run below 
CREATE EVENT TRIGGER awsdms_intercept_ddl ON ddl_command_end 
EXECUTE PROCEDURE pspadm..awsdms_intercept_ddl();
                        
