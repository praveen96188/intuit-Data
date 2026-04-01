
ggpspuwp01.sbg-psp-prod.a.intuit.com

cd /u01/parallel-setup/

intuadmin/"HxPmTv#aw8r5fi"@'pspparmo-new.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparmo'

alter user pspadm identified by "ToBU#2l0)Yra";

@cr_psp_prl_app_usr.sql ViqB#N4uLG)a
--password: ViqB#N4uLG)a
 
@cr_psp_prl_read_usr.sql FJx9)Vt#n7iO
--password: FJx9)Vt#n7iO


pspadm/"ToBU#2l0)Yra"@'pspparmo-new.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparmo' 

@installdb.sql PSP_PRL_APP PSPAPP_ROLE CRUD PSP_PRL_READ PSPREAD_ROLE

intuadmin/"HxPmTv#aw8r5fi"@'pspparmo-new.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparmo'
alter user pspadm account lock;
alter user PSPAPP account lock;

grant select on V$TRANSACTION to PSP_PRL_APP;
grant select on V$SESSION to PSP_PRL_APP;
select username,account_status from dba_users where username in ('PSPADM','PSPAPP');


CREATE USER Perf_test IDENTIFIED BY "Perf#123"
      DEFAULT TABLESPACE USERS
      TEMPORARY TABLESPACE TEMP
      PROFILE INDIVIDUAL_PROFILE;
GRANT PSPREAD_ROLE TO Perf_test;
GRANT CREATE SESSION TO Perf_test;
GRANT SELECT_CATALOG_ROLE TO PERF_TEST;
ALTER USER Perf_test DEFAULT ROLE ALL;
grant execute on rdsadmin.rdsadmin_util to perf_test;
GRANT "PSPAPP_ROLE" TO perf_test;
grant create any procedure to PERF_TEST;
grant execute on dbms_workload_repository to perf_test;
grant select any dictionary to perf_test;
grant execute on dbms_workload_repository to perf_test;	
GRANT EXECUTE ON "PSPADM"."PK_GEMS_ACCOUNTS_RECEIVABLE" TO perf_test;
GRANT EXECUTE ON "PSPADM"."FN_GET_EDR_AMOUNT" TO perf_test;
GRANT EXECUTE ON "PSPADM"."FN_GET_LEDGER_BALANCE" TO perf_test;
GRANT DELETE ON "PSPADM"."PSP_PERF_SST" TO perf_test;
GRANT DELETE ON "PSPADM"."PSP_ADE_LAW_MAP" TO perf_test;
GRANT DELETE ON "PSPADM"."PSP_EMPLOYER_PREFERENCE" TO perf_test;
GRANT DELETE ON "PSPADM"."PSP_TAX_CREDITS_APPLICATION" TO perf_test;
GRANT DELETE ON "PSPADM"."PSP_FRAUD_CONTACT" TO perf_test;
GRANT DELETE ON "PSPADM"."PSP_FRAUD_COMPANY" TO perf_test;
GRANT DELETE ON "PSPADM"."PSP_FRAUD_ADDRESS" TO perf_test;
GRANT DELETE ON "PSPADM"."PSP_SQL_EXECUTION_LOG_ENTRY" TO perf_test;
GRANT DELETE ON "PSPADM"."PSP_PAY_ITEM" TO perf_test;
GRANT DELETE ON "PSPADM"."PSP_ENTITY_UPDATE" TO perf_test;
GRANT INSERT ON "PSPADM"."PSP_PERF_SST" TO perf_test;
GRANT INSERT ON "PSPADM"."PSP_ADE_LAW_MAP" TO perf_test;
GRANT INSERT ON "PSPADM"."PSP_EMPLOYER_PREFERENCE" TO perf_test;
GRANT INSERT ON "PSPADM"."PSP_TAX_CREDITS_APPLICATION" TO perf_test;
GRANT INSERT ON "PSPADM"."PSP_FRAUD_CONTACT" TO perf_test;
GRANT INSERT ON "PSPADM"."PSP_FRAUD_COMPANY" TO perf_test;
GRANT INSERT ON "PSPADM"."PSP_FRAUD_ADDRESS" TO perf_test;
GRANT INSERT ON "PSPADM"."PSP_SQL_EXECUTION_LOG_ENTRY" TO perf_test;
GRANT INSERT ON "PSPADM"."PSP_PAY_ITEM" TO perf_test;
GRANT INSERT ON "PSPADM"."PSP_ENTITY_UPDATE" TO perf_test;
GRANT SELECT ON "PSPADM"."SEQ_ATF_BATCH_ID_NBR" TO perf_test;
GRANT SELECT ON "PSPADM"."PSP_PERF_SST" TO perf_test;
GRANT SELECT ON "PSPADM"."PSP_ADE_LAW_MAP" TO perf_test;
GRANT SELECT ON "PSPADM"."PSP_EMPLOYER_PREFERENCE" TO perf_test;
GRANT SELECT ON "PSPADM"."PSP_TAX_CREDITS_APPLICATION" TO perf_test;
GRANT SELECT ON "PSPADM"."PSP_FRAUD_CONTACT" TO perf_test;
GRANT SELECT ON "PSPADM"."PSP_FRAUD_COMPANY" TO perf_test;
GRANT SELECT ON "PSPADM"."PSP_FRAUD_ADDRESS" TO perf_test;
GRANT SELECT ON "PSPADM"."PSP_SQL_EXECUTION_LOG_ENTRY" TO perf_test;
GRANT SELECT ON "PSPADM"."PSP_PAY_ITEM" TO perf_test;
GRANT SELECT ON "PSPADM"."PSP_ENTITY_UPDATE" TO perf_test;
GRANT UPDATE ON "PSPADM"."PSP_PERF_SST" TO perf_test;
GRANT UPDATE ON "PSPADM"."PSP_ADE_LAW_MAP" TO perf_test;
GRANT UPDATE ON "PSPADM"."PSP_EMPLOYER_PREFERENCE" TO perf_test;
GRANT UPDATE ON "PSPADM"."PSP_TAX_CREDITS_APPLICATION" TO perf_test;
GRANT UPDATE ON "PSPADM"."PSP_FRAUD_CONTACT" TO perf_test;
GRANT UPDATE ON "PSPADM"."PSP_FRAUD_COMPANY" TO perf_test;
GRANT UPDATE ON "PSPADM"."PSP_FRAUD_ADDRESS" TO perf_test;
GRANT UPDATE ON "PSPADM"."PSP_SQL_EXECUTION_LOG_ENTRY" TO perf_test;
GRANT UPDATE ON "PSPADM"."PSP_PAY_ITEM" TO perf_test;
GRANT UPDATE ON "PSPADM"."PSP_ENTITY_UPDATE" TO perf_test;
GRANT EXECUTE ON "PSPADM"."PK_GEMS_ACCOUNTS_RECEIVABLE" TO perf_test;
GRANT EXECUTE ON "PSPADM"."FN_GET_EDR_AMOUNT" TO perf_test;
GRANT EXECUTE ON "PSPADM"."FN_GET_LEDGER_BALANCE" TO perf_test;



CREATE TABLESPACE logminer_tbs DATAFILE SIZE 10G AUTOEXTEND ON NEXT 1G MAXSIZE UNLIMITED;

CREATE USER dbzuser IDENTIFIED BY "dbzusr#123" DEFAULT TABLESPACE LOGMINER_TBS QUOTA UNLIMITED ON LOGMINER_TBS;

GRANT CREATE SESSION TO dbzuser;
GRANT SET CONTAINER TO dbzuser;
GRANT SELECT ON V$DATABASE TO dbzuser;
GRANT FLASHBACK ANY TABLE TO dbzuser;
GRANT SELECT ANY TABLE TO dbzuser;
GRANT SELECT_CATALOG_ROLE TO dbzuser;
GRANT EXECUTE_CATALOG_ROLE TO dbzuser;
GRANT SELECT ANY TRANSACTION TO dbzuser;
GRANT SELECT ANY DICTIONARY TO dbzuser;
GRANT LOGMINING TO dbzuser;
GRANT CREATE TABLE TO dbzuser;
GRANT LOCK ANY TABLE TO dbzuser;
GRANT CREATE SEQUENCE TO dbzuser;
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOG','DBZUSER','SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOG_HISTORY','DBZUSER','SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOGMNR_LOGS','DBZUSER','SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOGMNR_CONTENTS','DBZUSER','SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOGMNR_PARAMETERS','DBZUSER','SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$LOGFILE','DBZUSER','SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$ARCHIVED_LOG','DBZUSER','SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$ARCHIVE_DEST_STATUS','DBZUSER','SELECT');
exec rdsadmin.rdsadmin_util.grant_sys_object('V_$TRANSACTION','DBZUSER','SELECT');

grant select any dictionary to ksur; 
grant create session to ksur; 
grant execute on dbms_workload_repository to ksur;

--enable suplemental logging
SELECT supplemental_log_data_min FROM v$database;
exec rdsadmin.rdsadmin_util.alter_supplemental_logging('ADD');
exec rdsadmin.rdsadmin_util.alter_supplemental_logging('ADD','PRIMARY KEY');

@/u01/prvn/refresh_scripts/enable_sup_login_tab.sql

BEGIN
  DBMS_WORKLOAD_REPOSITORY.modify_snapshot_settings(
    retention => 129600,        -- Minutes (= 90 Days). 
    interval  => 15);          -- Minutes. 
END;
/



set lines 300
col username for a30
col machine form a70;
select username,machine,count(*) from gv$session where username is not NULL and username not in ('RDSADMIN','INTUADMIN','SYS','SYSTEM') and type = 'USER'  group by service_name,username,machine order by username,machine;

intuadmin/"HxPmTv#aw8r5fi"@'pspparmo.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparmo'

intuadmin/"HxPmTv#aw8r5fi"@'pspparmo-old.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparmo'


psp_prl_app/"ViqB#N4uLG)a"@'pspparmo-old.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparmo'


psp_prl_app/"ViqB#N4uLG)a"@'pspparmo.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparmo'

select username,account_status from dba_users where username in ('PSP_PRL_APP','PSP_PRL_READ');

intuadmin/"HxPmTv#aw8r5fi"@'psppardg.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparmo'

