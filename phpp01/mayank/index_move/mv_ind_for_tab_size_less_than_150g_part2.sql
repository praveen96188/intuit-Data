spool mv_ind_for_tab_size_less_than_150g_part2
set echo on feedback on timing on

--disable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

exec dbms_streams.set_tag (hextoraw(3));

alter index PSPADM.SYS_C0020539 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020579 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EMPLOYERCONTRIBUTION_FK2 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EMPLOYER_CONTRIBUTION_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EMPLOYERCONTRIBUTION_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_PSTUB_DDITEM_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020722 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020720 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EMPLOYEEUSAGE_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020689 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EMPLOYEE_USAGE_U1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020615 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_TAX_TABLE_MISC_DATA_FK2 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_PSTUB_MSG_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020723 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020613 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_COMPANY_DAILY_LIABILIT_FK2 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020634 rebuild tablespace PSP_IDX02 online;

--enable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

spool off

