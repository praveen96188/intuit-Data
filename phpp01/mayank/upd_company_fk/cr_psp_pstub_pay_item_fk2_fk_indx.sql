set echo on feedback on timing on
set serveroutput on
spool cr_psp_pstub_pay_item_fk2_fk_indx

--  disable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
-- get table size
select sum(bytes)/1024/1024/1024 from dba_segments where segment_name=upper('psp_pstub_pay_item') and owner='PSPADM;

create index pspadm.psp_pstub_pay_item_fk2 on pspadm.psp_pstub_pay_item (company_fk, realm_id) online parallel 8;
alter index pspadm.psp_pstub_pay_item_fk2 noparallel;
--  get index size
select sum(bytes)/1024/1024/1024 from dba_segments where segment_name=upper('psp_pstub_pay_item_fk2') and owner='PSPADM;
select degree from dba_indexes where index_name=upper('psp_pstub_pay_item_fk2') and owner='PSPADM;
--  enable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

spool off
