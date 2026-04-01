spool cr_index_c15.log
set lines 240
set echo on timing on
exec dbms_application_info.set_module('Creating Index txheaders_lastmodify_txtype', null)
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
select sum(bytes)/1024/1024/1024 from dba_segments where segment_name=upper('TxHeaders_1');
CREATE INDEX qbo_data.txheaders_lastmodify_txtype ON qbo_data.TxHeaders_1 (company_id, last_modify_date desc, tx_type_id) local online parallel 8;
Alter INDEX qbo_data.txheaders_lastmodify_txtype noparallel;
select sum(bytes)/1024/1024/1024 from dba_segments where segment_name=upper('txheaders_lastmodify_txtype');
select DEGREE from dba_indexes where index_name=upper('txheaders_lastmodify_txtype');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD'); 
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
spool off
exit
