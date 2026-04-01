-- FILE NAME    : dbupgrade_PSRV001197_PC_a5.sql
-- CREATED  DATE: 3.20.2009
-- MODIFIED DATE: 3.20.2009  02:00 PM
--                - 
-- AUTHOR       : EMR
--
-- PURPOSE: 
--   This script will change the PSP PAYCHECK table from no partitions  
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

SPOOL dbupgrade_PSRV001197_PC_a5.log

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


PROMPT .
PROMPT Start the redefinition process ...

-- design note:
--   if an error occurs must follow process by using ABORT_REDEF_TABLE.
--   best practice is to manually register the primary key index and constraint
--   because the create table already created that.  Then the COPY_TABLE_DEPENDENTS copies
--   and registers the remaining objects.

-- --------> CUT HERE <--------

SELECT 'START FINISH_REDEF_TABLE : ' || TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI:SS') FROM DUAL;

BEGIN 

	DBMS_OUTPUT.PUT_LINE ('Finish the process if all objects copied successfully ...');
  
  DBMS_REDEFINITION.SYNC_INTERIM_TABLE(
    uname      => 'PSPADM',        
    orig_table => 'PSP_PAYCHECK',
    int_table  => 'INT_PSP_PAYCHECK'
  );
  
	DBMS_OUTPUT.PUT_LINE ('SYNC_INTERIM_TABLE successful.');

  DBMS_REDEFINITION.FINISH_REDEF_TABLE (
    uname      => 'PSPADM',        
    orig_table => 'PSP_PAYCHECK',
    int_table  => 'INT_PSP_PAYCHECK'
  );

	DBMS_OUTPUT.PUT_LINE ('FINISH_REDEF_TABLE successful.');
  
EXCEPTION
  WHEN OTHERS THEN
	  DBMS_OUTPUT.PUT_LINE ('FINISH_REDEF_TABLE not successful due to following error ...');
    DBMS_OUTPUT.PUT_LINE (SQLERRM);    
END;
/
SHOW ERRORS
	
SELECT 'END FINISH_REDEF_TABLE : ' || TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI:SS') FROM DUAL;


PROMPT . 
PROMPT Part 1 complete, please run part 2 if successful ...

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


SPOOL OFF