spool mv_ind_for_tab_size_150g-600g_part2
set echo on feedback on timing on

--disable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

exec dbms_streams.set_tag (hextoraw(3));

alter index PSPADM.SYS_C008737 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.SYS_C008737 noparallel;
alter index PSPADM.PSP_QBDT_PAYCHECK_INFO_FK1 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_QBDT_PAYCHECK_INFO_FK1 noparallel;
alter index PSPADM.PSP_QBDT_PAYCHECK_INFO_FK2 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_QBDT_PAYCHECK_INFO_FK2 noparallel;
alter index PSPADM.SYS_C0020602 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.SYS_C0020602 noparallel;
alter index PSPADM.SYS_C0020587 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.SYS_C0020587 noparallel;
alter index PSPADM.PSP_PAYCHECK_SPLIT_FK3 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_PAYCHECK_SPLIT_FK3 noparallel;
alter index PSPADM.SYS_C008745 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.SYS_C008745 noparallel;
alter index PSPADM.SYS_C0070166 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.SYS_C0070166 noparallel;
alter index PSPADM.PSP_ENTITY_UPDATE_U1 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_ENTITY_UPDATE_U1 noparallel;
alter index PSPADM.PSP_COMPENSATION_FK4 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_COMPENSATION_FK4 noparallel;

--enable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

spool off

