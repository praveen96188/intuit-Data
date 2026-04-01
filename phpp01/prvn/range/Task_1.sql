set lines 300 echo on timing on echo on feedback on trimspool on
spool get_task_1

SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_LEDGER_BALANCE;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_ENTRY_DETAIL_RECORD;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_FINANCIAL_TRANS_STATE;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_FINANCIAL_TRANSACTION;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_PAYCHECK;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_PAYCHECK_SPLIT;

spool off;
