
--Create User source Postgres
CREATE USER  dms_apg_src WITH PASSWORD 'e#xc91kggPGH';
GRANT CONNECT ON DATABASE pspapg02 to dms_apg_src;
GRANT CREATE ON DATABASE pspapg02 TO dms_apg_src;
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

grant select on "AWSDMS_CONTROL_TABLE"."awsdms_apply_exceptions" to DMS_USER_TGT;

--Disable Fk constraints

set serverout on
begin
for i in (select constraint_name, table_name from dba_constraints where owner='PSPADM') LOOP
dbms_output.put_line ('alter table '||i.table_name||' disable constraint '||i.constraint_name||'');
end loop;
end;
/

select * from dba_constraints where owner='PSPADM' and constraint_type='R' and status='DISABLED';

--Disable Triggers

set serverout on
begin
for i in (select trigger_name from dba_triggers where owner='PSPADM') LOOP
dbms_output.put_line ('alter trigger pspadm.'||i.trigger_name||' disable ');
end loop;
end;
/

select * from DBA_TRIGGERS where owner='PSPADM'  and status='DISABLED';


--Task run

./dms_cdc.sh pspapg02 psphpp07 task1a  arn:aws:dms:us-west-2:893547637742:rep:EHPBGKYQV5SD24PUN6J2XLT6Z53L57FBOPCF2JQ
./dms_cdc.sh pspapg02 psphpp07 task1b  arn:aws:dms:us-west-2:893547637742:rep:EHPBGKYQV5SD24PUN6J2XLT6Z53L57FBOPCF2JQ
./dms_cdc.sh pspapg02 psphpp07 task1c  arn:aws:dms:us-west-2:893547637742:rep:EHPBGKYQV5SD24PUN6J2XLT6Z53L57FBOPCF2JQ
./dms_cdc.sh pspapg02 psphpp07 task1d  arn:aws:dms:us-west-2:893547637742:rep:EHPBGKYQV5SD24PUN6J2XLT6Z53L57FBOPCF2JQ
./dms_cdc.sh pspapg02 psphpp07 task2 arn:aws:dms:us-west-2:893547637742:rep:3QZSP3Z7DYHJ3IEK6QB3KRELWSIFZ65SZGXPXCY
./dms_cdc.sh pspapg02 psphpp07 task3 arn:aws:dms:us-west-2:893547637742:rep:63N23JHUF4X3WPS22ZCAEJTH525YQIH6AYKBB3Y
./dms_cdc.sh pspapg02 psphpp07 task4 arn:aws:dms:us-west-2:893547637742:rep:3QZSP3Z7DYHJ3IEK6QB3KRELWSIFZ65SZGXPXCY
./dms_cdc.sh pspapg02 psphpp07 task5 arn:aws:dms:us-west-2:893547637742:rep:63N23JHUF4X3WPS22ZCAEJTH525YQIH6AYKBB3Y
./dms_cdc.sh pspapg02 psphpp07 task6 arn:aws:dms:us-west-2:893547637742:rep:3QZSP3Z7DYHJ3IEK6QB3KRELWSIFZ65SZGXPXCY
./dms_cdc.sh pspapg02 psphpp07 task7a  arn:aws:dms:us-west-2:893547637742:rep:3QZSP3Z7DYHJ3IEK6QB3KRELWSIFZ65SZGXPXCY
./dms_cdc.sh pspapg02 psphpp07 task7b  arn:aws:dms:us-west-2:893547637742:rep:NAJTH2BBXBANRBWAISBSRU2VKMQC6JCDORUCUEI
./dms_cdc.sh pspapg02 psphpp07 task7c  arn:aws:dms:us-west-2:893547637742:rep:NAJTH2BBXBANRBWAISBSRU2VKMQC6JCDORUCUEI
./dms_cdc.sh pspapg02 psphpp07 task7d  arn:aws:dms:us-west-2:893547637742:rep:EHPBGKYQV5SD24PUN6J2XLT6Z53L57FBOPCF2JQ
./dms_cdc.sh pspapg02 psphpp07 task7e  arn:aws:dms:us-west-2:893547637742:rep:EHPBGKYQV5SD24PUN6J2XLT6Z53L57FBOPCF2JQ
./dms_cdc.sh pspapg02 psphpp07 task7f  arn:aws:dms:us-west-2:893547637742:rep:EHPBGKYQV5SD24PUN6J2XLT6Z53L57FBOPCF2JQ
./dms_cdc.sh pspapg02 psphpp07 task8 arn:aws:dms:us-west-2:893547637742:rep:63N23JHUF4X3WPS22ZCAEJTH525YQIH6AYKBB3Y



arn:aws:dms:us-west-2:893547637742:rep:63N23JHUF4X3WPS22ZCAEJTH525YQIH6AYKBB3Y
arn:aws:dms:us-west-2:893547637742:rep:NAJTH2BBXBANRBWAISBSRU2VKMQC6JCDORUCUEI
arn:aws:dms:us-west-2:893547637742:rep:3QZSP3Z7DYHJ3IEK6QB3KRELWSIFZ65SZGXPXCY
arn:aws:dms:us-west-2:893547637742:rep:EHPBGKYQV5SD24PUN6J2XLT6Z53L57FBOPCF2JQ



##DMS Tasks Status monitoring B-C3
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-1 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-2 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-2-2tab 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-3 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-4 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-5 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-6 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-7 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-8 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-9 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psphdg02-psp-prod-mon-pspadm-task-pem 1>/dev/null 2>&1

##DMS Tasks Status monitoring C3-D3
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task1a 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task1b 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task1c 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task2 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task3 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task4 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task5 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task6 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task7a 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task7b 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task7c 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task7d 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh psppp01-psphpp06-pspadm-task8 1>/dev/null 2>&1

##DMS Tasks Status monitoring C2-D2Prime
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task1a 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task1b 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task1c 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task1d 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task2 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task3 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task4 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task5 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task6 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task7a 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task7b 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task7c 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task7d 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task7e 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task7f 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./check_dms_status.sh pspapg02-psphpp07-pspadm-task8 1>/dev/null 2>&1

##DMS Apply Exceptions monitoring C3 and D3
*/30 * * * * cd /u01/postgres/scripts; ./chk_dms_apply_exceptions.sh psphpp06 psphpp06 1>/dev/null 2>&1
*/30 * * * * cd /u01/postgres/scripts; ./chk_dms_apply_exceptions.sh psppp01 psp-prod-mon1 1>log/apply_exceptions_report_o2p_psppp01.log 2>&1

