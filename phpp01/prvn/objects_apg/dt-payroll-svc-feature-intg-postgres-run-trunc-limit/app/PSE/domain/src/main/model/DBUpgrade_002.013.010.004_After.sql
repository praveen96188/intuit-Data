--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.010.004.sql
--
-- Developers can hand code logic here for data migration purposes
--
prompt updating the LedgerOperationJob with 'BulkDebit' for 'CRReduct'

begin
	UPDATE PSP_LEDGER_OPERATION_JOB
    SET JOB_TYPE = 'BulkDebit', modifier_id='PSP-3157' ,modified_date=sys_extract_utc(systimestamp)
    WHERE JOB_TYPE = 'CRReduct';
    commit;     
END;
/