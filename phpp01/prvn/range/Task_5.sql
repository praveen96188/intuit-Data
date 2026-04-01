set lines 300 echo on timing on echo on feedback on trimspool on
spool get_task_5
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_ENTITLEMENT_MESSAGE;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_PAYCHECK_USAGE;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_DISBURSE_ADVICE_TAX_LIAB;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_QBDT_TRANSACTION_INFO;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_PAYSTUB;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_DEDUCTION;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITEM;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_PSTUB_EMPLOYEE_INFO;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_ENTITY_UPDATE;
spool off;
