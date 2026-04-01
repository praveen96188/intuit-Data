-- FILE NAME    : dbupgrade_PSRV001197_PC_a4.sql
-- CREATED  DATE: 3.20.2009
-- MODIFIED DATE: 3.25.2009  02:00 PM
--                - 
-- AUTHOR       : EMR
--
-- PURPOSE: 
--   This script will change the PSP SOURCE SYSTEM TRANSMISSION table from no partitions  
--   to monthly, or twelve times a year.  The partition key will be on CREATED_DATE.
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

SPOOL dbupgrade_PSRV001197_SST_a4.log

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
     AND ui.TABLE_NAME   = 'PSP_SOURCE_SYSTEM_TRANSMISSION'
     AND ui.UNIQUENESS   = 'UNIQUE'
     AND uic.COLUMN_NAME = 'SOURCE_SYSTEM_TRANSMISSION_SEQ';

  -- primary key index
	DBMS_REDEFINITION.REGISTER_DEPENDENT_OBJECT (
    uname            => 'PSPADM',        
    orig_table       => 'PSP_SOURCE_SYSTEM_TRANSMISSION',
    int_table        => 'INT_PSP_SRC_SYS_TRANSMISSION',
    dep_type         => DBMS_REDEFINITION.CONS_INDEX,
    dep_owner        => 'PSPADM',
    dep_orig_name    => v_src_pk_idx_id, 
    dep_int_name     => 'INT_XPKSRC_SYS_TRANSMISSION'
  );	  

  SELECT DISTINCT uc.CONSTRAINT_NAME
    INTO v_src_pk_con_id
    FROM USER_CONSTRAINTS uc,
         USER_CONS_COLUMNS ucc
   WHERE uc.CONSTRAINT_NAME = ucc.CONSTRAINT_NAME
     AND uc.TABLE_NAME      = 'PSP_SOURCE_SYSTEM_TRANSMISSION'
     AND uc.CONSTRAINT_TYPE = 'P'
     AND ucc.COLUMN_NAME    = 'SOURCE_SYSTEM_TRANSMISSION_SEQ';

  -- primary key check
	DBMS_REDEFINITION.REGISTER_DEPENDENT_OBJECT (
    uname            => 'PSPADM',        
    orig_table       => 'PSP_SOURCE_SYSTEM_TRANSMISSION',
    int_table        => 'INT_PSP_SRC_SYS_TRANSMISSION',
    dep_type         => DBMS_REDEFINITION.CONS_CONSTRAINT,
    dep_owner        => 'PSPADM',
    dep_orig_name    => v_src_pk_con_id,
    dep_int_name     => 'INT_XPKSRC_SYS_TRANSMISSION'
  );	  

  DBMS_REDEFINITION.COPY_TABLE_DEPENDENTS (
    uname            => 'PSPADM',        
    orig_table       => 'PSP_SOURCE_SYSTEM_TRANSMISSION',
    int_table        => 'INT_PSP_SRC_SYS_TRANSMISSION',
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
PROMPT Part a4 complete, please run part a5 if successful ...

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


SPOOL OFF