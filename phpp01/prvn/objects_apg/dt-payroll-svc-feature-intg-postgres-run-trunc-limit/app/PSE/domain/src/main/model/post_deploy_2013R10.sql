Prompt updating psp_ledger_operation_job

UPDATE PSP_LEDGER_OPERATION_JOB
SET JOB_TYPE = 'BulkDebit'
WHERE JOB_TYPE = 'CRReduct'; 
commit;	

/