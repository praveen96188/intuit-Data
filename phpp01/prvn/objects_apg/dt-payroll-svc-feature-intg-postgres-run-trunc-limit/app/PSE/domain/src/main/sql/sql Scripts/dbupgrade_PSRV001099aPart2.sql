-- CREATED  DATE: 1.15.2009
-- MODIFIED DATE: 2.17.2009  12:10
--                - commented out drop int table
--                - uncommented drop int table due to constraint conflict issues
-- AUTHOR       : EMR
--
-- PURPOSE: 
--   This is part 2 of the entry detail record partitioning.  It is to be run
--   if all goes well with the initial repartitioning.  Otherwise an Abort must
--   be run.
--
-- LOGON AS : PSPADM


SET SERVEROUTPUT ON
SET LINESIZE     1000
SET PAGESIZE     0
SET DEFINE       OFF

SPOOL dbupgrade_PSRV001099aPart2.log

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


-- as SYSDBA to ensure there are no errors.
-- SELECT *
--   FROM DBA_REDEFINITION_ERRORS;

-- as SYSDBA to ensure all dependents are mapped.
-- SELECT *
--   FROM DBA_REDEFINITION_OBJECTS;


PROMPT . 
PROMPT Check if everything was okay ...

SELECT COUNT(*) FROM PSP_ENTRY_DETAIL_RECORD;


PROMPT . 
PROMPT Remove interim table ...

-- 02.13.2009 EMR  commented out
-- 02.17.2009 EMR  removed comment out
DROP TABLE INT_PSP_ENTRY_DETAIL_RECORD CASCADE CONSTRAINTS;


PROMPT . 
PROMPT Rename all the constraints and indexes to match the original names ...

PROMPT not necessary because we used COPY_TABLE_DEPENDENTS

-- question: do we need to drop and recreate the index to be partitioned. YES.

CREATE INDEX PSP_ENTRY_DETAIL_RECORD_I2 ON PSP_ENTRY_DETAIL_RECORD
  (INITIATION_DATE) 
  LOCAL NOLOGGING COMPUTE STATISTICS
  TABLESPACE PSP_IDX01 ONLINE;


PROMPT .
PROMPT Enable all constraints ...

BEGIN
  FOR rec IN (
    SELECT DISTINCT 
           'ALTER TABLE ' || table_name || ' ENABLE CONSTRAINT ' || constraint_name sql_stmt
      FROM USER_CONSTRAINTS
     WHERE TABLE_NAME = 'PSP_ENTRY_DETAIL_RECORD'
       AND VALIDATED  = 'NOT VALIDATED'    
  ) 
  LOOP
    DBMS_OUTPUT.PUT_LINE(rec.sql_stmt);
    EXECUTE IMMEDIATE rec.sql_stmt;
  END LOOP;
END;
/
SHOW ERRORS


PROMPT . 
PROMPT Reminder: check to ensure tables and indexes are analyzed, constraints enabled, and plsql compiled ...

EXEC DBMS_UTILITY.COMPILE_SCHEMA ('PSPADM', FALSE);

SELECT OBJECT_NAME,
       OBJECT_TYPE,
       STATUS
  FROM USER_OBJECTS
 WHERE STATUS = 'INVALID';


PROMPT . 
PROMPT Done Dude ...

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;

SPOOL OFF