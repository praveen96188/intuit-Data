-- TEAMTRACK NUM: tbd
-- CREATED  DATE: 1.20.2009
-- MODIFIED DATE: 4.09.2009  10:00 AM
--                4.9.09 added AS400 to purge list
--                4.9.09 modified coalesce for partitioned index
-- AUTHOR       : EMR
--
-- PURPOSE: 
--   This script will purge PSP_SOURCE_SYSTEM_TRANSMISSION => 'CRIS', created more than 10 days ago
--
-- LOGON AS : PSPADM


SET SERVEROUTPUT ON
SET HEADING      ON
SET LINESIZE     100
SET DEFINE       OFF


SPOOL dbupgrade_PSRV001106c.log

SELECT USER AS LOGIN_ID FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS START_TIME FROM DUAL;


PROMPT .
PROMPT Gather the row count info prior to the purge ...

SELECT COUNT(*) FROM PSP_SOURCE_SYSTEM_TRANSMISSION;


PROMPT .
PROMPT Purge the PSP_SOURCE_SYSTEM_TRANSMISSION Cris data except for the past 10 days ...

DECLARE
  v_count          PLS_INTEGER := 0;
  v_iteration_cnt  PLS_INTEGER := 0;
  
BEGIN

  -- question: use timestamp to avoid type conversion

  SELECT COUNT(*)
    INTO v_count
    FROM PSP_SOURCE_SYSTEM_TRANSMISSION
   WHERE From_Source_System IN ('CRIS', 'AS400')
     AND Created_Date        < SYSTIMESTAMP-10;

  DBMS_OUTPUT.PUT_LINE ('Total rows to delete : ' || v_count);
         
  -- update field 20k at a time.

  v_count := 0;

  LOOP   

    DELETE FROM PSP_SOURCE_SYSTEM_TRANSMISSION
     WHERE From_Source_System IN ('CRIS', 'AS400')
       AND Created_Date        < SYSTIMESTAMP-10
       AND ROWNUM              < 20001;

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
PROMPT delete SYNC request for the test company
BEGIN

LOOP

    delete from psp_source_system_transmission where company_fk = '1adf4866-8548-42f2-97e5-f22ca2e1c6a0' and type = 'Sync' and rownum < 10000;
             
       IF SQL%ROWCOUNT = 0 THEN
                       EXIT;
       END IF;
       COMMIT;
END LOOP;
COMMIT;

END;
/
PROMPT .
PROMPT delete SYNC request for the test company completed

PROMPT .
PROMPT Now that the tables are purged squeeze them down ...

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS SHRINK_START_TIME FROM DUAL;

ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION       SHRINK SPACE COMPACT;

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS SHRINK1_TIME FROM DUAL;

ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION       SHRINK SPACE;

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS SHRINK_END_TIME FROM DUAL;

PROMPT .
PROMPT Rebuilt the indexes on the table

BEGIN
  FOR rec IN (
    SELECT distinct 'ALTER INDEX ' ||  a.index_name || ' COALESCE' sql_stmt
      FROM USER_INDEXES a
     WHERE table_name  = 'PSP_SOURCE_SYSTEM_TRANSMISSION'
       AND index_type  = 'NORMAL'
       AND partitioned = 'NO'
  ) 
  LOOP
      DBMS_OUTPUT.PUT_LINE(rec.sql_stmt);
      EXECUTE IMMEDIATE rec.sql_stmt;
  END LOOP;
END;
/
SHOW ERRORS


PROMPT .
PROMPT Gather the row count info after the purge ...

SELECT COUNT(*) FROM PSP_SOURCE_SYSTEM_TRANSMISSION;


PROMPT .

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS END_TIME FROM DUAL;

PROMPT Done.

SPOOL OFF

