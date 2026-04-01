spool mv_ind_for_tab_size_less_than_150g_part1
set echo on feedback on timing on

--disable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

exec dbms_streams.set_tag (hextoraw(3));

alter index PSPADM.PSP_DEDUCTION_FK2 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_DEDUCTION_FK4 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_DEDUCTION_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_PAYSTUB_FK3 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_PAYSTUB_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020721 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_PAYSTUB_FK2 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020724 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITE_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_PSTUB_EMPLOYEE_INFO_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020718 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_PSTUB_EMPLOYEE_INFO_FK2 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020612 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EMPLOYEE_LAW_QTR_TOTAL_FK6 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EMPLOYEE_LAW_QTR_TOTAL_FK2 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020694 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EMPLOYEE_LAW_QTR_TOTAL_FK3 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_COMPANY_EVENT_EMAIL_FK1 rebuild tablespace PSP_IDX02 online;

--enable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

spool off

