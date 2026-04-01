set echo on timing on feedback on
spool create_indexes_CHG2103248_13Mar
--  disable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

CREATE INDEX PSPADM.PSP_PSTUB_EMPLOYEE_INFO_FK3 ON PSPADM.PSP_PSTUB_EMPLOYEE_INFO (COMPANY_FK, REALM_ID) online parallel 8;
alter index PSPADM.PSP_PSTUB_EMPLOYEE_INFO_FK3 noparallel;

CREATE INDEX PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITE_FK2 ON PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITEM (COMPANY_FK, REALM_ID) online parallel 8;
alter index PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITE_FK2 noparallel;

CREATE INDEX PSPADM.PSP_PSTUB_DDITEM_FK2 ON PSPADM.PSP_PSTUB_DDITEM (COMPANY_FK, REALM_ID) online parallel 8;
alter index PSPADM.PSP_PSTUB_DDITEM_FK2 noparallel;

CREATE INDEX PSPADM.PSP_PSTUB_MSG_FK2 ON PSPADM.PSP_PSTUB_MSG (COMPANY_FK, REALM_ID) online parallel 8;
alter index PSPADM.PSP_PSTUB_MSG_FK2 noparallel;

select degree from dba_indexes where index_name=upper('PSP_PSTUB_PAID_TIMEOFF_ITE_FK2');
select degree from dba_indexes where index_name=upper('PSP_PSTUB_DDITEM_FK2');
select degree from dba_indexes where index_name=upper('PSP_PSTUB_MSG_FK2');
select degree from dba_indexes where index_name=upper('PSP_PSTUB_EMPLOYEE_INFO_FK3');

--  enable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

exit
spool off
