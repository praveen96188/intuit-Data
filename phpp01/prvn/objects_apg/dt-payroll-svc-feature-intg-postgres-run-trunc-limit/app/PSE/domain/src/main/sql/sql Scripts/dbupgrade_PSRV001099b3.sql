-- CREATED  DATE: 1.13.2009
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
--   This script will change the PSE financial transaction state table from no partitions
--   to monthly, or twelve per year.  The partition key will be on TRANSACTION_STATE_EFF_DATE.
--   Applicable indexes will also be modified.
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

SPOOL dbupgrade_PSRV001099b3.log

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

SELECT 'START START_REDEF_TABLE : ' || TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI:SS') FROM DUAL;

BEGIN

	DBMS_OUTPUT.PUT_LINE ('Start the redefinition process ...');
	
  DBMS_REDEFINITION.START_REDEF_TABLE (
    uname        => 'PSPADM',        
    orig_table   => 'PSP_FINANCIAL_TRANS_STATE',
    int_table    => 'INT_FINANCIAL_TXN_STATE',
    options_flag => DBMS_REDEFINITION.CONS_USE_PK
  );

	DBMS_OUTPUT.PUT_LINE ('START_REDEF_TABLE successful.');
  
EXCEPTION
  WHEN OTHERS THEN
	  DBMS_OUTPUT.PUT_LINE ('START_REDEF_TABLE not successful due to following error ...');
    DBMS_OUTPUT.PUT_LINE (SQLERRM);    
END;
/
SHOW ERRORS

SELECT 'END START_REDEF_TABLE : ' || TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI:SS') FROM DUAL;


PROMPT . 
PROMPT Part b3 complete, please run part b4 if successful ...

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;

SPOOL OFF