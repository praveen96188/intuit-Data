-- TEAMTRACK NUM: PSRV001089
-- CREATED  DATE: 1.16.2009
-- MODIFIED DATE: 1.16.2009  09:00
-- AUTHOR       : EMR
--
-- PURPOSE: 
--   This script populate the new INITIATION_DATE field in the PSE entry detail record 
--   table from.  This will allow us to partition this table using this new field.
--   Also it is estimated that ten million rows will be updated so performance is
--   critical.  The method of update is to take chunks of rows (eg. 100k, update them,
--   and then commit; repeat until all rows updated).  Finally the field will be 
--   populated using the INITIATION_DATE from money movement transaction table.
--
--   It is assumed that the new field was already added to the table using the database
--   deploy.
--
-- LOGON AS : PSPADM


SET SERVEROUTPUT ON
SET HEADING      ON
SET LINESIZE     1000
SET PAGESIZE     0
SET DEFINE       OFF


SPOOL dbupgrade_PSRV001089.log

SELECT 'User = ' || USER FROM DUAL;
SELECT 'Start Time = ' || TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') FROM DUAL;

PROMPT .
PROMPT Total number of records for interested tables ...

SELECT 'Number of rows in MMT = ' || COUNT(*)
  FROM PSP_MONEY_MOVEMENT_TRANSACTION;
  
SELECT 'Number of rows in EDR = ' || COUNT(*)
  FROM PSP_ENTRY_DETAIL_RECORD;
  
SELECT 'Number of rows to update = ' || COUNT(*)
  FROM PSP_MONEY_MOVEMENT_TRANSACTION mmt,
       PSP_ENTRY_DETAIL_RECORD        edr
 WHERE mmt.MONEY_MOVEMENT_TRANSACTION_SEQ = edr.MONEY_MOVEMENT_TRANSACTION_FK
   AND mmt.REALM_ID                       = edr.REALM_ID
   AND edr.INITIATION_DATE IS NULL;   
  
PROMPT .
PROMPT Update PSP_Entry_Detail_Record.Initiation_Date 20k at a time ...

DECLARE
  v_count          PLS_INTEGER := 0;
  v_iteration_cnt  PLS_INTEGER := 0;
  
BEGIN
	
	-- ensure the field is there and it is all null.
	SELECT COUNT(*)
	  INTO v_count
	  FROM PSP_ENTRY_DETAIL_RECORD
	 WHERE INITIATION_DATE IS NULL;
	
	DBMS_OUTPUT.PUT_LINE ('Total rows to update : ' || v_count);
	 
	-- update field 20k at a time.
	
	v_count := 0;
	
	LOOP   
	
    UPDATE PSP_ENTRY_DETAIL_RECORD edr
		  SET edr.INITIATION_DATE = (
		    SELECT mmt.INITIATION_DATE
		      FROM PSP_MONEY_MOVEMENT_TRANSACTION mmt
		     WHERE mmt.MONEY_MOVEMENT_TRANSACTION_SEQ = edr.MONEY_MOVEMENT_TRANSACTION_FK
           AND mmt.REALM_ID                       = edr.REALM_ID
      )
		 WHERE edr.INITIATION_DATE IS NULL
		   AND ROWNUM < 20001;

    v_count := v_count + SQL%ROWCOUNT;		   
		
		EXIT WHEN SQL%ROWCOUNT = 0;
		
		COMMIT;
		
    v_iteration_cnt := v_iteration_cnt + 1;           
		
	END LOOP;
	
	COMMIT;
	
	DBMS_OUTPUT.PUT_LINE ('Total rows updated : '      || v_count);
  DBMS_OUTPUT.PUT_LINE ('Total update iterations : ' || v_iteration_cnt);
	
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE (SQLERRM);
END;
/
SHOW ERRORS


PROMPT .
PROMPT Confirm the total number of records for interested tables ...

SELECT 'Number of rows in MMT = ' || COUNT(*)
  FROM PSP_MONEY_MOVEMENT_TRANSACTION;
  
SELECT 'Number of rows in EDR = ' || COUNT(*)
  FROM PSP_ENTRY_DETAIL_RECORD;
  
SELECT 'Number of rows to update = ' || COUNT(*)
  FROM PSP_MONEY_MOVEMENT_TRANSACTION mmt,
       PSP_ENTRY_DETAIL_RECORD        edr
 WHERE mmt.MONEY_MOVEMENT_TRANSACTION_SEQ = edr.MONEY_MOVEMENT_TRANSACTION_FK
   AND mmt.REALM_ID                       = edr.REALM_ID
   AND edr.INITIATION_DATE IS NULL;   


SELECT 'End Time = ' || TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') FROM DUAL;


PROMPT .
PROMPT Done.

SPOOL OFF