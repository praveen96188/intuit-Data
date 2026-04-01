set lines 300 echo on timing on echo on feedback on trimspool on
spool get_task_3

SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_COMPENSATION;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_PROPERTY_AUDIT;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_QBDT_PAYCHECK_INFO;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_QBDT_PAYLINE_INFO;

spool off;
