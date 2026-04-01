-- TEAMTRACK NUM: tbd
-- CREATED  DATE: 1.20.2009
-- MODIFIED DATE: 1.20.2009  15:00
-- AUTHOR       : EMR
--
-- PURPOSE: 
--   This script will purge three tables.  It is part 1 of 2.
--   However since the table is in two schemas it is broken into
--   two parts.
--
--     PSP_SOURCE_SYSTEM_TRANSMISSION => 'CRIS', created more than 10 days ago (part 1a)
--     PSP_BATCH_JOB_AUDIT_LOG,       => created more than 5 days ago          (part 1a)
--     PSE_EVENT_LOG                  => created more than 30 days ago         (part 1b)
--
-- LOGON AS : PSPLOG


SET SERVEROUTPUT ON
SET HEADING      ON
SET LINESIZE     1000
SET PAGESIZE     0
SET DEFINE       OFF


SPOOL dbupgrade_PSRV001106d.log

SELECT USER AS LOGIN_ID FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS START_TIME FROM DUAL;

PROMPT .
PROMPT Gather the customer info prior to the purge ...

SELECT COUNT(*) FROM PSE_EVENT_LOG;

  
PROMPT .
PROMPT Add the new object so that the purge will run fast ...

CREATE INDEX PSE_EVENT_LOG_PURGE
  ON PSE_EVENT_LOG (Z_INS_DTTM)
  NOLOGGING COMPUTE STATISTICS
  TABLESPACE PSP_IDX01;

ALTER TABLE PSE_EVENT_LOG                  ENABLE ROW MOVEMENT;


PROMPT .
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS END_TIME FROM DUAL;
PROMPT Done.

SPOOL OFF