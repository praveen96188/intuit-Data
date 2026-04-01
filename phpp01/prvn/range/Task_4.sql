set lines 300 echo on timing on echo on feedback on trimspool on
spool get_task_4
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_PSTUB_PAY_ITEM;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_TAX;

spool off;
