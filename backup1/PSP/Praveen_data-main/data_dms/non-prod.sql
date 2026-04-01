
--Create User source Postgres
CREATE USER  dms_apg_src WITH PASSWORD 'dms#123';
GRANT CONNECT ON DATABASE ppdspg01 to dms_apg_src;
GRANT CREATE ON DATABASE ppdspg01 TO dms_apg_src;
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




INTUADMIN/"changeme"@'ppsphp01.sbg-psp-ppd.a.intuit.com:1521/ppsphp01'
psql -h ppsp-pds-uw01.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -p 5432 -U postgres -d ppdspg01

hostname: ppsp-pds-uw01.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com
port: 5432
Dbname: ppdspg01
object_owner:pspadm_owner
password: Pp3zu#JA7M5
appuser: pspapp
password: IgRgK7d#PrG

select rolname,rolcanlogin
from pg_roles
where rolname in ('pspapp','pspadm_owner');

--on B
--Disable Triggers
--Disable FK


\c ppdspg01 pspadm_owner
Pp3zu#JA7M5


\i CreateProcedures.sql
\i CreateFunctions.sql
\i CreateSequencess.sql
\i CreateTrigger.sql
\i Createview.sql

--Sequence reset
SELECT 'ALTER SEQUENCE '||sequence_owner||'.'||sequence_name||' RESTART WITH '||(last_number + 1)||';'
FROM all_sequences
WHERE sequence_owner = 'PSPADM';


SELECT  nspname, proname 
FROM    pg_catalog.pg_namespace  
JOIN    pg_catalog.pg_proc  
ON      pronamespace = pg_namespace.oid 
WHERE   nspname = 'pspadm'
ORDER BY Proname;

select distinct trigger_name from information_schema.triggers where trigger_schema='pspadm';

select schemaname ,sequencename from pg_sequences where schemaname='pspadm';

./dms_cdc.sh ppdspg01 ppsphp01 task1 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppdspg01 ppsphp01 task2 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppdspg01 ppsphp01 task3 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppdspg01 ppsphp01 task4 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppdspg01 ppsphp01 task5 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppdspg01 ppsphp01 task6 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppdspg01 ppsphp01 task7 arn:aws:dms:us-west-2:152430470825:rep:55TRK6FXIG7T7AT4XOPWAL3LWME55PE4OMGHO3I
./dms_cdc.sh ppdspg01 ppsphp01 task8 arn:aws:dms:us-west-2:152430470825:rep:55TRK6FXIG7T7AT4XOPWAL3LWME55PE4OMGHO3I




--rollback

./dms_cdc.sh ppsphpdg ppdspg01 task1 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppsphpdg ppdspg01 task2 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppsphpdg ppdspg01 task3 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppsphpdg ppdspg01 task4 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppsphpdg ppdspg01 task5 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppsphpdg ppdspg01 task6 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppsphpdg ppdspg01 task7 arn:aws:dms:us-west-2:152430470825:rep:55TRK6FXIG7T7AT4XOPWAL3LWME55PE4OMGHO3I
./dms_cdc.sh ppsphpdg ppdspg01 task8 arn:aws:dms:us-west-2:152430470825:rep:55TRK6FXIG7T7AT4XOPWAL3LWME55PE4OMGHO3I



alter user pspapp with NOLOGIN;


cd app/PSE/domain/src/main/sql/postgres/monolith/

cp -r Trigger Sequence Procedure Function  /u01/switchover_to_c3/


ppdspg01=> ALTER SEQUENCE PSPADM.SEQ_TAX_ACC_AUD RESTART WITH 28622;
ERROR:  relation "pspadm.seq_tax_acc_aud" does not exist
ppdspg01=> ALTER SEQUENCE PSPADM.SEQ_TEMP_COMPANY RESTART WITH 3352567;
ERROR:  relation "pspadm.seq_temp_company" does not exist
ppdspg01=> ALTER SEQUENCE PSPADM.SEQ_TEST RESTART WITH 23;
ERROR:  relation "pspadm.seq_test" does not exist
ppdspg01=> ALTER SEQUENCE PSPADM.TEST RESTART WITH 778;
ERROR:  relation "pspadm.test" does not exist
ppdspg01=> ALTER SEQUENCE PSPADM.TEST1 RESTART WITH 89;
ERROR:  relation "pspadm.test1" does not exist
ppdspg01=> ALTER SEQUENCE PSPADM.SEQ_PSID RESTART WITH 99900510;
ERROR:  relation "pspadm.seq_psid" does not exist
ppdspg01=> ALTER SEQUENCE PSPADM.SEQ_ATF_BATCH_ID_NBR RESTART WITH 9922;
ERROR:  RESTART value (9922) cannot be less than MINVALUE (10000)
ppdspg01=> ALTER SEQUENCE PSPADM.ENTITLEMENT_SEQ RESTART WITH 463004;
ERROR:  relation "pspadm.entitlement_seq" does not exist



ALTER SEQUENCE PSPADM.SEQ_TAX_ACC_AUD RESTART WITH 31178;
psql:alter_seq_hash_post.sql:3: ERROR:  relation "pspadm.seq_tax_acc_aud" does not exist

ALTER SEQUENCE PSPADM.SEQ_PSID RESTART WITH 99900510;
psql:alter_seq_hash_post.sql:9: ERROR:  relation "pspadm.seq_psid" does not exist

ALTER SEQUENCE PSPADM.TEST1 RESTART WITH 89;
psql:alter_seq_hash_post.sql:18: ERROR:  relation "pspadm.test1" does not exist

ALTER SEQUENCE PSPADM.ENTITLEMENT_SEQ RESTART WITH 463004;
psql:alter_seq_hash_post.sql:39: ERROR:  relation "pspadm.entitlement_seq" does not exist


ALTER SEQUENCE PSPADM.SEQ_TEMP_COMPANY RESTART WITH 3352567;
psql:alter_seq_hash_post.sql:67: ERROR:  relation "pspadm.seq_temp_company" does not exist

ALTER SEQUENCE PSPADM.TEST RESTART WITH 819;
psql:alter_seq_hash_post.sql:75: ERROR:  relation "pspadm.test" does not exist


ALTER SEQUENCE PSPADM.SEQ_ATF_BATCH_ID_NBR RESTART WITH 10000;
psql:alter_seq_hash_post.sql:47: ERROR:  RESTART value (9924) cannot be less than MINVALUE (10000)



./dms_cdc.sh ppsphpdg ppdspg01 task1 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppsphpdg ppdspg01 task2 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppsphpdg ppdspg01 task3 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppsphpdg ppdspg01 task4 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppsphpdg ppdspg01 task5 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppsphpdg ppdspg01 task6 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppsphpdg ppdspg01 task7 arn:aws:dms:us-west-2:152430470825:rep:55TRK6FXIG7T7AT4XOPWAL3LWME55PE4OMGHO3I
./dms_cdc.sh ppsphpdg ppdspg01 task8 arn:aws:dms:us-west-2:152430470825:rep:55TRK6FXIG7T7AT4XOPWAL3LWME55PE4OMGHO3I


./dms_cdc.sh ppsphpdg ppdspg01 task1 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppsphpdg ppdspg01 task2 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppsphpdg ppdspg01 task3 arn:aws:dms:us-west-2:152430470825:rep:4WCMKFU53EMW5CMGGQ552QEA5LNJXZYLZY23KVQ
./dms_cdc.sh ppsphpdg ppdspg01 task4 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppsphpdg ppdspg01 task5 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppsphpdg ppdspg01 task6 arn:aws:dms:us-west-2:152430470825:rep:ZDXY4F3MPFQCZOQFI32GU7VJAG57KBZZ3XQAFGI
./dms_cdc.sh ppsphpdg ppdspg01 task7 arn:aws:dms:us-west-2:152430470825:rep:55TRK6FXIG7T7AT4XOPWAL3LWME55PE4OMGHO3I
./dms_cdc.sh ppsphpdg ppdspg01 task8 arn:aws:dms:us-west-2:152430470825:rep:55TRK6FXIG7T7AT4XOPWAL3LWME55PE4OMGHO3I

