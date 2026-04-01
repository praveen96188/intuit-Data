spool mv_ind_less_than_10g_part1
set echo on feedback on timing on

--disable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

exec dbms_streams.set_tag (hextoraw(3));

alter index PSPADM.SYS_C0020607 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_BILLING_DETAIL_FK2 rebuild tablespace PSP_IDX02 online;        
alter index PSPADM.PSP_LIABILITY_CHECK_BILLIN_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_LIABILITY_CHECK_BILLIN_FK2 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020683 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_ATFPAYMENTS_TO_PROCESS_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EMPLOYEE_W2_TOTALS_FK2 rebuild tablespace PSP_IDX02 online;    
alter index PSPADM.PSP_LIABILITY_CHECK_FK2 rebuild tablespace PSP_IDX02 online;       
alter index PSPADM.PSP_ATFPAYMENTS_TO_PROCESS_FK3 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020673 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.SYS_C0020696 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_EMPLOYEE_W2_TOTALS_FK4 rebuild tablespace PSP_IDX02 online;    
alter index PSPADM.PSP_EMPLOYEE_LAW_QTR_TOTAL_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020608 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_LIABILITY_ADJUSTMENT_FK4 rebuild tablespace PSP_IDX02 online;  
alter index PSPADM.PSP_LIABILITY_ADJUSTMENT_FK1 rebuild tablespace PSP_IDX02 online;  
alter index PSPADM.SYS_C0020560 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_LIABILITY_ADJUSTMENT_FK7 rebuild tablespace PSP_IDX02 online;  
alter index PSPADM.PSP_COMPANY_DAILY_LIABILIT_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020707 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_QBDT_EMPLOYEE_INFO_FK2 rebuild tablespace PSP_IDX02 online;    
alter index PSPADM.PSP_QBDT_EMPLOYEE_INFO_FK1 rebuild tablespace PSP_IDX02 online;    
alter index PSPADM.SYS_C0020603 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_BILL_PAYMENT_FK2 rebuild tablespace PSP_IDX02 online;          
alter index PSPADM.PSP_BILL_PAYMENT_SPLIT_FK2 rebuild tablespace PSP_IDX02 online;    
alter index PSPADM.PSP_BILL_PAYMENT_FK1 rebuild tablespace PSP_IDX02 online;          
alter index PSPADM.PSP_BILL_PAYMENT_SPLIT_FK1 rebuild tablespace PSP_IDX02 online;    
alter index PSPADM.SYS_C0020570 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.SYS_C0020569 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_WC_PCHK_STATE_PCHK_FK rebuild tablespace PSP_IDX02 online;     
alter index PSPADM.SYS_C0020609 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_DISBURSE_ADVICE_FK1 rebuild tablespace PSP_IDX02 online;       
alter index PSPADM.SYS_C0020610 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.SYS_C0020705 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.SYS_C0020695 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.SYS_C0020611 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_EMPLOYEE_W2_TOTALS_FK1 rebuild tablespace PSP_IDX02 online;    
alter index PSPADM.SYS_C0020600 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_USAGE_PERIOD_U1 rebuild tablespace PSP_IDX02 online;           
alter index PSPADM.PSP_QBDT_PAYROLL_ITEM_INFO_FK3 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EMPLOYEE_W2_TOTALS_FK3 rebuild tablespace PSP_IDX02 online;    
alter index PSPADM.PSP_ATFPAYMENTS_TO_PROCESS_FK2 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_BILL_U1 rebuild tablespace PSP_IDX02 online;                   
alter index PSPADM.PSP_USAGEPERIOD_FK1 rebuild tablespace PSP_IDX02 online;           
alter index PSPADM.SYS_C0020672 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_BILL_FK1 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.SYS_C0020687 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.SYS_C0020688 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_QBDT_PAYROLL_ITEM_INFO_FK2 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_LEDGER_OPERATION_FK2 rebuild tablespace PSP_IDX02 online;      


--enable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

spool off;

