set echo on feedback on timing on
spool create_indexes_1
--  disable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

Prompt Index PSP_COMPANY_EVENT_EMAIL_FK2;
CREATE INDEX PSPADM.PSP_COMPANY_EVENT_EMAIL_FK2 ON PSPADM.PSP_COMPANY_EVENT_EMAIL (COMPANY_FK, REALM_ID) online parallel 8;
alter index PSPADM. PSP_COMPANY_EVENT_EMAIL_FK2 noparallel;

Prompt Index PSP_PSTUB_PAID_TIMEOFF_ITE_FK2;
CREATE INDEX PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITE_FK2 ON PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITEM (COMPANY_FK, REALM_ID) online parallel 8;
alter index PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITE_FK2 noparallel;

Prompt Index PSP_PSTUB_MSG_FK2;
CREATE INDEX PSPADM.PSP_PSTUB_MSG_FK2 ON PSPADM.PSP_PSTUB_MSG (COMPANY_FK, REALM_ID) online parallel 8;
alter index PSPADM.PSP_PSTUB_MSG_FK2 noparallel;

Prompt Index PSP_PSTUB_DDITEM_FK2;
CREATE INDEX PSPADM.PSP_PSTUB_DDITEM_FK2 ON PSPADM.PSP_PSTUB_DDITEM (COMPANY_FK, REALM_ID) online parallel 8;
alter index PSPADM.PSP_PSTUB_DDITEM_FK2 noparallel;

--  enable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

spool off

