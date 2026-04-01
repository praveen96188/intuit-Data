-- CREATED  DATE: 1.15.2009
-- MODIFIED DATE: 2.19.2009  09:10
--                - commented out drop int table
--                - uncommented drop int table due to constraint conflict issues
--                - split part 2 from this file to allow for recovery.
--                - changed one block to individual blocks for DBMS_REPARTITION
--                - moved alter row movement to prior work to avoid ora error
--                - split files event further.  structure is now the following:
--                    file-a1     = create int table
--                    file-a2     = run can_redef_table
--                    file-a3     = run start_redef_table
--                    file-a4     = run copy_dependents
--                    file-a5     = run finish_redef_table
--                    file-aPart2 = drop int table, and add additional objects
--
-- AUTHOR       : EMR
--
-- PURPOSE: 
--   This script will change the PSE entry detail record table from no partitions
--   to monthly, or twelve per year.  The partition key will be on INITIATION_DATE.
--   Please note this is a new field in this table.  Applicable indexes will also be 
--   modified.
--
-- LOGON AS : PSPADM


-- question: is this needed GRANT ROLE EXECUTE_CATALOG_ROLE
-- grant using SYS

-- GRANT execute ON DBMS_REDEFINITION TO PSPADM;
-- GRANT create materialized view     TO PSPADM;
-- GRANT alter  any table             TO PSPADM;
-- GRANT create any table             TO PSPADM;
-- GRANT drop   any table             TO PSPADM;
-- GRANT lock   any table             TO PSPADM;
-- GRANT select any table             TO PSPADM;
-- GRANT create any trigger           TO PSPADM; -- used for COPY_TABLE_DEPENDENTS
-- GRANT create any index             TO PSPADM; -- used for COPY_TABLE_DEPENDENTS


SET SERVEROUTPUT ON
SET LINESIZE     1000
SET PAGESIZE     0
SET DEFINE       OFF

SPOOL dbupgrade_PSRV001099a2.log

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


PROMPT .
PROMPT Start the redefinition process ...

-- design note:
--   if an error occurs must follow process by using ABORT_REDEF_TABLE.
--   best practice is to manually register the primary key index and constraint
--   because the create table already created that.  Then the COPY_TABLE_DEPENDENTS copies
--   and registers the remaining objects.

-- questions:
--   how will the index be partitioned, should it.  SEE LAST STEP BELOW.
--   when can this be run - while redefinition is running. NO, AFTER.
--   will the not null check constraint come over.  YES.


-- --------> CUT HERE <--------

SELECT 'START CAN_REDEF_TABLE : ' || TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI:SS') FROM DUAL;
 
BEGIN

	DBMS_OUTPUT.PUT_LINE ('Check to ensure redefinition is possible ...');

  DBMS_REDEFINITION.CAN_REDEF_TABLE(
    'PSPADM', 
    'PSP_ENTRY_DETAIL_RECORD'
  );
  
	DBMS_OUTPUT.PUT_LINE ('CAN_REDEF_TABLE successful.');
  
EXCEPTION
  WHEN OTHERS THEN
	  DBMS_OUTPUT.PUT_LINE ('CAN_REDEF_TABLE not successful due to following error ...');
    DBMS_OUTPUT.PUT_LINE (SQLERRM);    
END;
/
SHOW ERRORS

SELECT 'END CAN_REDEF_TABLE : ' || TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI:SS') FROM DUAL;


PROMPT . 
PROMPT Part a2 complete, please run part a3 if successful ...

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


SPOOL OFF