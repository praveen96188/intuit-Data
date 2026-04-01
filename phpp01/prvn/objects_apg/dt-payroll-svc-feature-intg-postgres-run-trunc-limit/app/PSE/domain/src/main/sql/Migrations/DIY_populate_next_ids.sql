-- CREATED: 12.01.2008 EMR
--
-- this script addresses an issue where a company went from Assisted to DIY.
-- the issue is with the next ids (employee and pay item), currently they are
-- not migrated.  this script will copy those values over for the companies
-- affected by this situation.
--
-- This must be run as DIYMIGADM user.
-- Also ensure that UPDATE is granted to DIYMIGADM on PSP_COMPANY.

SET serveroutput on
SET define       off
SET linesize     500

SPOOL DIY_populate_next_ids.log


SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') FROM DUAL;


PROMPT Create a temp table of companies with next ids greater than 0 on the AS400
       -- this table will only have companies that were just migrated that match
       -- the criteria.  only those will be updated.
CREATE TABLE Z_TEMP_LASTID AS (
  SELECT a.CLI_USERID,
         a.CLI_LAST_PITEM,
         a.CLI_LAST_EMP,
         b.TARGET_DB_COMPANY_ID
    FROM DIY_IQCLIENT      a,
         COMPANY_MIGRATION b 
   WHERE a.CLI_USERID         = TO_NUMBER(b.SOURCE_DB_COMPANY_ID)
     AND (a.CLI_LAST_PITEM > 0 OR a.CLI_LAST_EMP > 0)
     AND a.CLI_TAXSERVICE     = 'N'
     AND a.CLI_STATUS         = 'A'
     AND b.MIGRATION_STATE_CD = 'CP'
     AND TRUNC(b.MIGRATION_ACTUAL_DATE) = TRUNC(SYSDATE)
);


PROMPT --
PROMPT The total number of migrated companies that will be updated.
SELECT COUNT(*) AS "TOTAL TO UPDATE"
  FROM Z_TEMP_LASTID;

PROMPT --
PROMPT Actual rows that will be updated.
SELECT SUBSTR(a.Company_Seq, 1, 50),
       a.Source_Company_ID,
       a.Next_Payline_Transaction_ID,
       a.Next_Employee_ID,
       b.CLI_Last_Pitem,
       b.CLI_Last_Emp
  FROM PSP_COMPANY   a,
       Z_TEMP_LASTID b
 WHERE a.Company_Seq       = b.Target_DB_Company_ID
   AND a.Source_Company_ID = TO_CHAR(b.CLI_USERID);


PROMPT Now updating the selected companies.
DECLARE
  v_rec_count    NUMBER;
  
BEGIN
	
  v_rec_count := 0;
	 
	FOR rec IN (
	  SELECT *
	    FROM Z_TEMP_LASTID
	)
	LOOP
	 
	 	UPDATE PSP_COMPANY
	 	   SET Next_Payline_Transaction_ID = rec.CLI_LAST_PITEM + 1,
	 	       Next_Employee_ID            = rec.CLI_LAST_EMP + 1
	 	 WHERE Company_SEQ       = rec.TARGET_DB_COMPANY_ID
	 	   AND Source_Company_ID = TO_CHAR(rec.CLI_USERID);

	  v_rec_count := v_rec_count + 1;

  END LOOP;

  DBMS_OUTPUT.PUT_LINE ('Total records updated = ' || v_rec_count);
	 
END;
/

SHOW ERRORS


PROMPT --
PROMPT Actual rows that were updated.
SELECT SUBSTR(a.Company_Seq, 1, 50),
       a.Source_Company_ID,
       a.Next_Payline_Transaction_ID,
       a.Next_Employee_ID,
       b.CLI_Last_Pitem,
       b.CLI_Last_Emp
  FROM PSP_COMPANY   a,
       Z_TEMP_LASTID b
 WHERE a.Company_Seq       = b.Target_DB_Company_ID
   AND a.Source_Company_ID = TO_CHAR(b.CLI_USERID);


PROMPT Now deleting the temp tables
DROP TABLE Z_TEMP_LASTID;


PROMPT The DIY DD next ids have been updated.

PROMPT Please remember to manually COMMIT or ROLLBACK.

SPOOL OFF
