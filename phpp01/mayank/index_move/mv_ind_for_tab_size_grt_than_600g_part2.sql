spool mv_ind_for_tab_size_grt_than_600g_part2
set echo on feedback on timing on

--disable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

exec dbms_streams.set_tag (hextoraw(3));

alter index PSPADM.PSP_PSTUB_PAY_ITEM_FK1 noparallel;
alter index PSPADM.PSP_PSTUB_PAY_ITEM_FK1 rebuild tablespace PSP_IDX02 online parallel 8;
alter index PSPADM.PSP_PSTUB_PAY_ITEM_FK1 noparallel;
alter index PSPADM.PSP_COMPANY_EVENT_DETAIL_FK2 rebuild tablespace PSP_IDX02 online parallel 8;
alter index PSPADM.PSP_COMPANY_EVENT_DETAIL_FK2 noparallel;
alter index PSPADM.PSP_COMPANY_EVENT_EMAIL_PA_FK1 rebuild tablespace PSP_IDX02 online parallel 8;
alter index PSPADM.PSP_COMPANY_EVENT_EMAIL_PA_FK1 noparallel;
alter index PSPADM.SYS_C0020540 rebuild tablespace PSP_IDX02 online parallel 8;
alter index PSPADM.SYS_C0020540 noparallel;

--enable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

spool off

