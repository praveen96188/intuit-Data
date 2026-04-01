-- FILE NAME    : dbupgrade_PSRV001197_PCS_b1.sql
-- CREATED  DATE: 3.20.2009
-- MODIFIED DATE: 3.20.2009  02:00 PM
--                - 
-- AUTHOR       : EMR
--
-- PURPOSE: 
--   This script will change the PSP PAYCHECK SPLIT table from no partitions  
--   to biannually, or twice a year.  The partition key will be on CREATED_DATE.
--   Applicable indexes will also be modified.
--                    file-a1 = create int table
--                    file-a2 = run can_redef_table
--                    file-a3 = run start_redef_table
--                    file-a4 = run copy_dependents
--                    file-a5 = run finish_redef_table
--                    file-b1 = drop int table, and add additional objects
--
-- LOGON AS : PSPADM


SET SERVEROUTPUT ON
SET LINESIZE     1000
SET PAGESIZE     0
SET DEFINE       OFF

SPOOL dbupgrade_PSRV001197_PCS_b1.log

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

SELECT COUNT(*) FROM PSP_PAYCHECK_SPLIT;


PROMPT . 
PROMPT Remove interim table ...

DROP TABLE INT_PSP_PAYCHECK_SPLIT CASCADE CONSTRAINTS;


PROMPT . 
PROMPT Rename all the constraints and indexes to match the original names ...

PROMPT not necessary because we used COPY_TABLE_DEPENDENTS

CREATE INDEX PSP_PAYCHECK_SPLIT_I1 ON PSP_PAYCHECK_SPLIT
  (CREATED_DATE) 
  LOCAL NOLOGGING COMPUTE STATISTICS
  TABLESPACE PSP_IDX01 ONLINE;


PROMPT .
PROMPT Enable all constraints ...

BEGIN
  FOR rec IN (
    SELECT DISTINCT 
           'ALTER TABLE ' || table_name || ' ENABLE CONSTRAINT ' || constraint_name sql_stmt
      FROM USER_CONSTRAINTS
     WHERE TABLE_NAME = 'PSP_PAYCHECK_SPLIT'
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