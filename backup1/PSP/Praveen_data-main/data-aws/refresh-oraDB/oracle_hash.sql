
ggpspuwp01.sbg-psp-prod.a.intuit.com

cd /u01/parallel-setup/

intuadmin/"VrPZqe#xc86bni"@'pspparhs-new.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparhs'
intuadmin/"VrPZqe#xc86bni"@'pspparhdg.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparhs'

alter user pspadm identified by "ToBU#2l0)Yra";

@cr_psp_prl_app_usr.sql ViqB#N4uLG)a

@cr_psp_prl_read_usr.sql FJx9)Vt#n7iO

exit;

sqlplus pspadm/"ToBU#2l0)Yra"@'pspparhs-new.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparhs'

@installdb.sql PSP_PRL_APP PSPAPP_ROLE CRUD PSP_PRL_READ PSPREAD_ROLE

sqlplus intuadmin/"VrPZqe#xc86bni"@'pspparhs-new.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparhs'
alter user pspadm account lock;
alter user PSPAPP account lock;
select username,account_status from dba_users where username in ('PSPADM','PSPAPP');

select username,account_status from dba_users where username in ('PSP_PRL_READ','PSP_PRL_APP');
alter user PSP_PRL_READ  account unlock;
alter user PSP_PRL_APP  account unlock;




grant select on V$TRANSACTION to PSP_PRL_APP;
grant select on V$SESSION to PSP_PRL_APP;

set echo on feedback on
spool DROP_GLOBAL_INDEXES_CREATED_TO_SUPPORT_VALIDATION
drop index PSPADM.IDX_PSP_FINANCIAL_TRANSACTION_GLOBAL;
drop index PSPADM.IDX_PSP_COMPANY_EVENT_GLOBAL;
drop index PSPADM.IDX_PSP_COMPANY_EVENT_DETAIL_GLOBAL;
drop index PSPADM.IDX_PSP_COMPANY_EVENT_EMAIL_PARAM_GLOBAL;
drop index PSPADM.IDX_PSP_COMPENSATION_GLOBAL;
drop index PSPADM.IDX_PSP_ENTRY_DETAIL_RECORD_GLOBAL;
drop index PSPADM.IDX_PSP_FINANCIAL_TRANS_STATE_GLOBAL;
drop index PSPADM.IDX_PSP_LEDGER_BALANCE_GLOBAL;
drop index PSPADM.IDX_PSP_MONEY_MOVEMENT_TRANSACTION_GLOBAL;
drop index PSPADM.IDX_PSP_PAYCHECK_GLOBAL;
drop index PSPADM.IDX_PSP_PAYCHECK_SPLIT_GLOBAL;
drop index PSPADM.IDX_PSP_PROPERTY_AUDIT_GLOBAL;
drop index PSPADM.IDX_PSP_PSTUB_PAY_ITEM_GLOBAL;
drop index PSPADM.IDX_PSP_QBDT_PAYCHECK_INFO_GLOBAL;
drop index PSPADM.IDX_PSP_QBDT_PAYLINE_INFO_GLOBAL;
drop index PSPADM.IDX_PSP_TAX_GLOBAL;
drop index PSPADM.IDX_PSP_DISBURSE_ADVICE_TAX_LIAB_GLOBAL;
drop index PSPADM.IDX_PSP_QBDT_TRANSACTION_INFO_GLOBAL;
drop index PSPADM.IDX_PSP_PAYSTUB_GLOBAL;
drop index PSPADM.IDX_PSP_DEDUCTION_GLOBAL;
drop index PSPADM.IDX_PSP_PSTUB_PAID_TIMEOFF_ITEM_GLOBAL;
drop index PSPADM.IDX_PSP_PSTUB_EMPLOYEE_INFO_GLOBAL;

spool off

Perf#123
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
grant select any dictionary to jbansal; 
grant create session to jbansal; 
grant execute on dbms_workload_repository to jbansal;
grant  EXECUTE ANY PROCEDURE to jbansal;

--enable suplemental logging
SELECT supplemental_log_data_min FROM v$database;
exec rdsadmin.rdsadmin_util.alter_supplemental_logging('ADD');
exec rdsadmin.rdsadmin_util.alter_supplemental_logging('ADD','PRIMARY KEY');

@/u01/prvn/refresh_scripts/enable_sup_login_tab.sql

@/u01/scripts/cr_user_RO.sql
1)EBIETL_HASH_STG
Password:VXgFq7d#(2eC

BEGIN
  DBMS_WORKLOAD_REPOSITORY.modify_snapshot_settings(
    retention => 129600,        -- Minutes (= 90 Days). 
    interval  => 15);          -- Minutes. 
END;
/

UPDATE PSPADM.PSP_SYSTEM_PARAMETER
SET SYSTEM_PARAMETER_VALUE='true'
WHERE SYSTEM_PARAMETER_CD = 'JPMC_ENABLE_ENCRYPTION';

UPDATE PSPADM.PSP_SYSTEM_PARAMETER
SET SYSTEM_PARAMETER_VALUE=NULL
WHERE SYSTEM_PARAMETER_CD='BANK_SFTP_ACH_HOST';

UPDATE PSPADM.PSP_SYSTEM_PARAMETER
SET SYSTEM_PARAMETER_VALUE='1234567890'
WHERE SYSTEM_PARAMETER_CD = 'JPMC_IMMEDIATE_ORIGIN';

UPDATE PSPADM.PSP_SYSTEM_PARAMETER
SET SYSTEM_PARAMETER_VALUE='1234567890'
WHERE SYSTEM_PARAMETER_CD = 'JPMC_IMMEDIATE_DESTINATION';

UPDATE PSPADM.PSP_SYSTEM_PARAMETER
SET SYSTEM_PARAMETER_VALUE='60000'
WHERE SYSTEM_PARAMETER_CD = 'JPMC_NACHA_FILE_UPLOAD_DELAY';



psp_prl_app/"ViqB#N4uLG)a"@'pspparhs-new.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparhs'

--after rename cluster
psp_prl_app/"ViqB#N4uLG)a"@'pspparhs.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparhs'


set lines 300
col username for a30
col machine form a70;
select username,machine,count(*) from gv$session where username is not NULL and username not in ('RDSADMIN','INTUADMIN','SYS','SYSTEM') and type = 'USER'  group by service_name,username,machine order by username,machine;

psp_prl_app/"ViqB#N4uLG)a"@'pspparhs.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparhs'


intuadmin/"VrPZqe#xc86bni"@'pspparhs.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparhs'
Perf_test/"Perf#123"@'pspparhs.cjls0bohfgpq.us-west-2.rds.amazonaws.com:2632/pspparhs'

SELECT * FROM session_privs
ORDER BY privilege;

set lines 300
col username for a30
col machine form a70;
select username,machine,count(*) from gv$session where username is not NULL and username not in ('RDSADMIN','INTUADMIN','SYS','SYSTEM') and type = 'USER'  group by service_name,username,machine order by username,machine;


select s.schema,s.table_name,s.partition_key from 
(select c.relnamespace::regnamespace::text as schema,
       c.relname as table_name, 
       pg_get_partkeydef(c.oid) as partition_key
from   pg_class c
where  c.relkind = 'p') as s  where s.schema='pspadm' and s.table_name='psp_paycheck_usage'; 


Oracle
CREATE TABLE pspadm.psp_entry_detail_record_staging(
    company_fk VARCHAR2(255) NOT NULL,
    entry_detail_record_seq VARCHAR2(255) NOT NULL,
    n_a_c_h_a_file_fk VARCHAR2(255) NOT NULL,
    trace_number VARCHAR2(20)
);
GRANT SELECT, INSERT, DELETE, UPDATE ON PSPADM.PSP_ENTRY_DETAIL_RECORD_STAGING TO psp_prl_app;
create or replace synonym psp_prl_app.psp_entry_detail_record_staging for pspadm.psp_entry_detail_record_staging;
Postgres
CREATE TABLE pspadm.psp_entry_detail_record_staging(
    company_fk CHARACTER VARYING(255) NOT NULL,
    entry_detail_record_seq CHARACTER VARYING(255) NOT NULL,
    n_a_c_h_a_file_fk CHARACTER VARYING(255) NOT NULL,
    trace_number CHARACTER VARYING(20)
);
