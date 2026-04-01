-- TEAMTRACK NUM: tbd
-- CREATED  DATE: 1.20.2009
-- MODIFIED DATE: 1.20.2009  15:00
-- AUTHOR       : EMR
--
-- PURPOSE: 
--   This script will purge 
--
--     PSE_EVENT_LOG                  => created more than 30 days ago         (part 2b)
--
-- LOGON AS : PSPLOG


SET SERVEROUTPUT ON
SET HEADING      ON
SET LINESIZE     100
SET DEFINE       OFF


SPOOL dbupgrade_PSRV001106e.log

SELECT USER AS LOGIN_ID FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS START_TIME FROM DUAL;

PROMPT .
PROMPT Gather the row count prior to the purge ...

SELECT COUNT(*) FROM PSE_EVENT_LOG;

  
PROMPT .
PROMPT Purge the data except for the past 30 days ...

DECLARE
  v_count          PLS_INTEGER := 0;
  v_iteration_cnt  PLS_INTEGER := 0;
  
BEGIN

        -- question: use timestamp to avoid type conversion

        SELECT COUNT(*)
          INTO v_count
          FROM PSE_EVENT_LOG
         WHERE Z_INS_DTTM < SYSTIMESTAMP-30;

        DBMS_OUTPUT.PUT_LINE ('Total rows to update : ' || v_count);
         
        -- update field 20k at a time.

        v_count := 0;

        LOOP   

    DELETE FROM PSE_EVENT_LOG
     WHERE Z_INS_DTTM < SYSTIMESTAMP-30
                   AND ROWNUM < 20001;

    v_count := v_count + SQL%ROWCOUNT;             

                EXIT WHEN SQL%ROWCOUNT = 0;

                COMMIT;

    v_iteration_cnt := v_iteration_cnt + 1;           

        END LOOP;

        COMMIT;

        DBMS_OUTPUT.PUT_LINE ('Total rows deleted : '      || v_count);
  DBMS_OUTPUT.PUT_LINE ('Total update iterations : ' || v_iteration_cnt);

EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE ('Error : ' || SQLERRM);
END;
/
SHOW ERRORS


PROMPT .
PROMPT Now that the tables are purged squeeze them down ...

-- question: should we use cascade for indexes

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS SHRINK_START_TIME FROM DUAL;

ALTER TABLE PSE_EVENT_LOG SHRINK SPACE COMPACT;

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS SHRINK1_TIME FROM DUAL;

ALTER TABLE PSE_EVENT_LOG SHRINK SPACE;

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS SHRINK_END_TIME FROM DUAL;


PROMPT .
PROMPT Rebuilt the indexes on the table

-- question: does this need to be modified to rebuild partitioned indexes

BEGIN
        FOR rec IN (
          SELECT distinct 'ALTER INDEX ' ||  a.index_name || ' COALESCE' sql_stmt
                  FROM USER_INDEXES a
           WHERE table_name = Upper('PSE_EVENT_LOG')
        ) 
        LOOP
            DBMS_OUTPUT.PUT_LINE(rec.sql_stmt);
            EXECUTE IMMEDIATE rec.sql_stmt;
  END LOOP;
END;
/
SHOW ERRORS


PROMPT .
PROMPT Gather the row count after the purge ...

SELECT COUNT(*) FROM PSE_EVENT_LOG;


PROMPT .
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS END_TIME FROM DUAL;
PROMPT Done.

SPOOL OFF