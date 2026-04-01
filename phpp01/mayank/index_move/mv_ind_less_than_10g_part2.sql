spool mv_ind_less_than_10g_part2
set echo on feedback on timing on

--disable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.disable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

exec dbms_streams.set_tag (hextoraw(3));

alter index PSPADM.PSP_ATFPAYROLLS_TO_PROCESS_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EMPLOYEE_PAYROLL_ITEM__FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EMPLOYEE_PAYROLL_ITEM__FK2 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_COMPANY_PAYROLL_ITEM_FK1 rebuild tablespace PSP_IDX02 online;  
alter index PSPADM.SYS_C0020558 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.SYS_C0020573 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_EFTPS_PAYMENT_DETAIL_FK4 rebuild tablespace PSP_IDX02 online;  
alter index PSPADM.SYS_C0020710 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.SYS_C0020623 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_COMPANY_ADJUSTMENT_SUB_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020635 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_LIABILITY_ADJUSTMENT_FK2 rebuild tablespace PSP_IDX02 online;  
alter index PSPADM.PSP_LIABILITY_ADJUSTMENT_FK3 rebuild tablespace PSP_IDX02 online;  
alter index PSPADM.PSP_EFTPS_PAYMENT_DETAIL_FK1 rebuild tablespace PSP_IDX02 online;  
alter index PSPADM.PSP_QBDT_PAYROLL_ITEM_INFO_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EFTPS_PAYMENT_DETAIL_FK3 rebuild tablespace PSP_IDX02 online;  
alter index PSPADM.PSP_WC_PCHK_PCHK_FK rebuild tablespace PSP_IDX02 online;           
alter index PSPADM.SYS_C0020704 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_COMPANY_OFFERING_FK2 rebuild tablespace PSP_IDX02 online;      
alter index PSPADM.SYS_C0020595 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.SYS_C0020614 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_COMPANY_PAYROLL_ITEM_FK2 rebuild tablespace PSP_IDX02 online;  
alter index PSPADM.SYS_C0020562 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_COMPANY_LAW_FK1 rebuild tablespace PSP_IDX02 online;           
alter index PSPADM.PSP_COMPANY_OFFERING_FK1 rebuild tablespace PSP_IDX02 online;      
alter index PSPADM.PSP_LEDGER_OPERATION_FK1 rebuild tablespace PSP_IDX02 online;      
alter index PSPADM.PSP_COMPANY_PAYROLL_ITEM_FK4 rebuild tablespace PSP_IDX02 online;  
alter index PSPADM.SYS_C0020653 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_COMPANYPAYMENTTEMPLATE_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_COMPANY_ADJUSTMENT_SUB_FK3 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_COMPANY_ADJUSTMENT_SUB_FK4 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_EFTPS_PAYMENT_DETAIL_FK2 rebuild tablespace PSP_IDX02 online;  
alter index PSPADM.PSP_EMPLOYEE_WAGE_PLAN_FK1 rebuild tablespace PSP_IDX02 online;    
alter index PSPADM.SYS_C0020557 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_EFFECTIVE_DEPOSIT_FREQ_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.SYS_C0020563 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_COMPANY_AGENCY_PAYMENT_FK1 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_COMPANY_LAW_FK2 rebuild tablespace PSP_IDX02 online;           
alter index PSPADM.PSP_COMPANY_LAW_FK4 rebuild tablespace PSP_IDX02 online;           
alter index PSPADM.PSP_EFFECTIVE_DEPOSIT_FREQ_FK2 rebuild tablespace PSP_IDX02 online;
alter index PSPADM.PSP_PAYEE_BANK_ACCOUNT_FK1 rebuild tablespace PSP_IDX02 online;    
alter index PSPADM.SYS_C0020568 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_PAYEE_BANK_ACCOUNT_FK2 rebuild tablespace PSP_IDX02 online;    
alter index PSPADM.PSP_COMPANY_SERVICE_FK3 rebuild tablespace PSP_IDX02 online;       
alter index PSPADM.SYS_C0020566 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_PAYEE_FK1 rebuild tablespace PSP_IDX02 online;                 
alter index PSPADM.SYS_C0020551 rebuild tablespace PSP_IDX02 online;                  
alter index PSPADM.PSP_COMPANY_AGENCY_FK2 rebuild tablespace PSP_IDX02 online;        
alter index PSPADM.PSP_COMPANY_I6 rebuild tablespace PSP_IDX02 online;                
alter index PSPADM.STATS_11DEC rebuild tablespace PSP_IDX02 online;                   

--enable online index rebuild cleanup job
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';
exec rdsadmin.rdsadmin_dbms_scheduler.enable('SYS.CLEANUP_ONLINE_IND_BUILD');
select owner, job_name, enabled, state, next_run_date from DBA_SCHEDULER_JOBS where job_name ='CLEANUP_ONLINE_IND_BUILD';

spool off;

