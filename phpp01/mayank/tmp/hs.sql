SET PAGESIZE 0
SET HEAD on ECHO OFF
SET LONG 1000000
SET LONGCHUNKSIZE 1000000
SET TRIM ON
SET TRIMSPOOL ON
SET FEEDBACK OFF
SET MARKUP HTML ON
set heading on
--SET NUM 30

spool prod_hs.xls
SELECT
    h.sample_time,
    u.username,
    h.MACHINE,
    h.ACTION,
    h.module,
    s.SQL_ID,
    s.sql_text
FROM
    DBA_HIST_ACTIVE_SESS_HISTORY h,
    DBA_USERS u,
    DBA_HIST_SQLTEXT s
WHERE
--      sample_time >= SYSDATE - 1
   sample_time between to_date('20/09/2021 00:00:00','DD/MM/YYYY HH24:MI:SS')
and to_date('24/09/2021 23:59:00','DD/MM/YYYY HH24:MI:SS')
  AND h.user_id=u.user_id
  AND h.sql_id = s.sql_iD
 and u.USERNAME in ('PSPAPP','PSPADM')
ORDER BY h.sample_time;

spool off
