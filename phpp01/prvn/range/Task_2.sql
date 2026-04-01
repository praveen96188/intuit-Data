set lines 300 echo on timing on echo on feedback on trimspool on
spool get_task_2

SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_COMPANY_EVENT;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_COMPANY_EVENT_DETAIL;
SELECT /*+ parallel(4) */count(*) FROM PSPADM.PSP_COMPANY_EVENT_EMAIL_PARAM;

spool off;
