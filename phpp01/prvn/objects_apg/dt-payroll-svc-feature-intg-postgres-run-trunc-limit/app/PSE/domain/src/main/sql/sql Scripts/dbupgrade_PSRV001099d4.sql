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
--   This script will change the PSE money movement transaction table from no partitions
--   to bi-monthly, or six per year.  The partition key will be on INITIATION_DATE.  Applicable
--   indexes will also be modified.
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

SPOOL dbupgrade_PSRV001099d4.log

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

SELECT 'START COPY_TABLE_DEPENDENTS : ' || TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI:SS') FROM DUAL;

DECLARE
  v_num_errors      PLS_INTEGER := 0;
  v_src_pk_idx_id   VARCHAR2(100);
  v_src_pk_con_id   VARCHAR2(100);  

BEGIN
  
	DBMS_OUTPUT.PUT_LINE ('Add new constraints, indexes, and triggers to interim table ...');
	
  SELECT DISTINCT ui.INDEX_NAME 
    INTO v_src_pk_idx_id
    FROM USER_INDEXES     ui,
         USER_IND_COLUMNS uic
   WHERE ui.INDEX_NAME   = uic.INDEX_NAME
     AND ui.TABLE_NAME   = 'PSP_MONEY_MOVEMENT_TRANSACTION'
     AND ui.UNIQUENESS   = 'UNIQUE'
     AND uic.COLUMN_NAME = 'MONEY_MOVEMENT_TRANSACTION_SEQ';

	DBMS_REDEFINITION.REGISTER_DEPENDENT_OBJECT (
    uname            => 'PSPADM',        
    orig_table       => 'PSP_MONEY_MOVEMENT_TRANSACTION',
    int_table        => 'INT_MONEY_MOVEMENT_TXN',
    dep_type         => DBMS_REDEFINITION.CONS_INDEX,
    dep_owner        => 'PSPADM',
    dep_orig_name    => v_src_pk_idx_id,           -- primary key index
    dep_int_name     => 'INT_XPKMONEY_MOVEMENT_TXN'
  );	  

  SELECT DISTINCT uc.CONSTRAINT_NAME
    INTO v_src_pk_con_id
    FROM USER_CONSTRAINTS uc,
         USER_CONS_COLUMNS ucc
   WHERE uc.CONSTRAINT_NAME = ucc.CONSTRAINT_NAME
     AND uc.TABLE_NAME      = 'PSP_MONEY_MOVEMENT_TRANSACTION'
     AND uc.CONSTRAINT_TYPE = 'P'
     AND ucc.COLUMN_NAME    = 'MONEY_MOVEMENT_TRANSACTION_SEQ';

	DBMS_REDEFINITION.REGISTER_DEPENDENT_OBJECT (
    uname            => 'PSPADM',        
    orig_table       => 'PSP_MONEY_MOVEMENT_TRANSACTION',
    int_table        => 'INT_MONEY_MOVEMENT_TXN',
    dep_type         => DBMS_REDEFINITION.CONS_CONSTRAINT,
    dep_owner        => 'PSPADM',
    dep_orig_name    => v_src_pk_con_id,           -- primary key check
    dep_int_name     => 'INT_XPKMONEY_MOVEMENT_TXN'
  );	  

  DBMS_REDEFINITION.COPY_TABLE_DEPENDENTS (
    uname            => 'PSPADM',        
    orig_table       => 'PSP_MONEY_MOVEMENT_TRANSACTION',
    int_table        => 'INT_MONEY_MOVEMENT_TXN',
    copy_indexes     => DBMS_REDEFINITION.CONS_ORIG_PARAMS,
    copy_triggers    => TRUE,
    copy_constraints => TRUE,
    copy_privileges  => TRUE,
    ignore_errors    => FALSE,
    num_errors       => v_num_errors,
    copy_statistics  => FALSE
  );	
  
  DBMS_OUTPUT.PUT_LINE ('Copy Dependents error count = ' || v_num_errors);
  DBMS_OUTPUT.PUT_LINE ('COPY_TABLE_DEPENDENTS successful.');
  
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE ('Copy Dependents error count = ' || v_num_errors);
	  DBMS_OUTPUT.PUT_LINE ('COPY_TABLE_DEPENDENTS not successful due to following error ...');
    DBMS_OUTPUT.PUT_LINE (SQLERRM);    
END;
/
SHOW ERRORS

SELECT 'END COPY_TABLE_DEPENDENTS : ' || TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI:SS') FROM DUAL;


PROMPT . 
PROMPT Part d4 complete, please run part d5 if successful ...

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;

SPOOL OFF