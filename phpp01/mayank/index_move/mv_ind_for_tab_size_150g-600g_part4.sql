spool mv_ind_for_tab_size_150g-600g_part4
set echo on feedback on timing on

--disable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

exec dbms_streams.set_tag (hextoraw(3));

alter index PSPADM.SYS_C008432 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.SYS_C008432 noparallel;
alter index PSPADM.PSP_QBDTTRANSACTIONINFO_FK1 noparallel;
alter index PSPADM.PSP_QBDTTRANSACTIONINFO_FK1 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_QBDTTRANSACTIONINFO_FK1 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK3 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK3 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK3 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK5 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK5 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK5 noparallel;
alter index PSPADM.SYS_C0020599 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.SYS_C0020599 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK2 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK2 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK2 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK6 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK6 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK6 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK4 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK4 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK4 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK1 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK1 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK1 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK7 noparallel;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK7 rebuild tablespace PSP_IDX02 online parallel 4;
alter index PSPADM.PSP_QBDT_TRANSACTION_INFO_FK7 noparallel;

--enable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

spool off

