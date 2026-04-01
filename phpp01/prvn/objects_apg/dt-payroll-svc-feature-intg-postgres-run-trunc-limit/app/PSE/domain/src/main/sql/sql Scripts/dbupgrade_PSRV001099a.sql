-- CREATED  DATE: 1.15.2009
-- MODIFIED DATE: 1.21.2009  14:00
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

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;

SPOOL partitioning_entry_detail_record.log

PROMPT .
PROMPT Get initial table count ...

SELECT COUNT(*) FROM PSP_ENTRY_DETAIL_RECORD;


PROMPT .
PROMPT Create an interim table ...

-- design note: the partition format is M<01-12>YYYY, where 
--                M    = month, Jan to Dec
--                YYYY = year
--            : it is assumed that INITIATION_DATE will have been
--              added and populated prior to partitioning.
--                ALTER TABLE PSP_ENTRY_DETAIL_RECORD
--                  ADD (INITIATION_DATE TIMESTAMP(6));
-- questions: 
--   how many partitions to initially create - eg stop at 2010 or more. can add later?
--   less than, or less than equal to

-- this is simply to compare to at the end
CREATE TABLE Z_REDEF_ENTRY_DETAIL_RECORD AS
SELECT * FROM PSP_ENTRY_DETAIL_RECORD WHERE 1=0;

-- this is the real deal for DBMS_REDEFINITION
CREATE TABLE INT_PSP_ENTRY_DETAIL_RECORD
(
  ENTRY_DETAIL_RECORD_SEQ        VARCHAR2(255),
  VERSION                        NUMBER(19),
  CREATOR_ID                     VARCHAR2(30),
  CREATED_DATE                   TIMESTAMP(6),
  MODIFIER_ID                    VARCHAR2(30),
  MODIFIED_DATE                  TIMESTAMP(6),
  REALM_ID                       NUMBER(19) DEFAULT -1,
  AMOUNT                         NUMBER(19,4),
  TRACE_NUMBER                   VARCHAR2(20),
  CREDIT_DEBIT_INDICATOR         VARCHAR2(255),
  RECORD_DATA                    VARCHAR2(250),
  INTUIT_BANK_ACCOUNT_FK         VARCHAR2(255),
  N_A_C_H_A_FILE_FK              VARCHAR2(255),
  MONEY_MOVEMENT_TRANSACTION_FK  VARCHAR2(255),
  COMPANY_FK                     VARCHAR2(255),
  INITIATION_DATE                TIMESTAMP(6)
)
PARTITION BY RANGE (INITIATION_DATE)
(
 PARTITION ENTRY_DETAIL_RCD_2008    VALUES LESS THAN (TO_DATE('01/01/2009', 'MM/DD/YYYY')),
 PARTITION ENTRY_DETAIL_RCD_M012009 VALUES LESS THAN (TO_DATE('02/01/2009', 'MM/DD/YYYY')),
 PARTITION ENTRY_DETAIL_RCD_M022009 VALUES LESS THAN (TO_DATE('03/01/2009', 'MM/DD/YYYY')),
 PARTITION ENTRY_DETAIL_RCD_M032009 VALUES LESS THAN (TO_DATE('04/01/2009', 'MM/DD/YYYY')),
 PARTITION ENTRY_DETAIL_RCD_M042009 VALUES LESS THAN (TO_DATE('05/01/2009', 'MM/DD/YYYY')),
 PARTITION ENTRY_DETAIL_RCD_M052009 VALUES LESS THAN (TO_DATE('06/01/2009', 'MM/DD/YYYY')),   
 PARTITION ENTRY_DETAIL_RCD_M062009 VALUES LESS THAN (TO_DATE('07/01/2009', 'MM/DD/YYYY')), 
 PARTITION ENTRY_DETAIL_RCD_M072009 VALUES LESS THAN (TO_DATE('08/01/2009', 'MM/DD/YYYY')), 
 PARTITION ENTRY_DETAIL_RCD_M082009 VALUES LESS THAN (TO_DATE('09/01/2009', 'MM/DD/YYYY')), 
 PARTITION ENTRY_DETAIL_RCD_M092009 VALUES LESS THAN (TO_DATE('10/01/2009', 'MM/DD/YYYY')),  
 PARTITION ENTRY_DETAIL_RCD_M102009 VALUES LESS THAN (TO_DATE('11/01/2009', 'MM/DD/YYYY')), 
 PARTITION ENTRY_DETAIL_RCD_M112009 VALUES LESS THAN (TO_DATE('12/01/2009', 'MM/DD/YYYY')), 
 PARTITION ENTRY_DETAIL_RCD_M122009 VALUES LESS THAN (TO_DATE('01/01/2010', 'MM/DD/YYYY')),  
 PARTITION ENTRY_DETAIL_RCD_M012010 VALUES LESS THAN (TO_DATE('02/01/2010', 'MM/DD/YYYY')),
 PARTITION ENTRY_DETAIL_RCD_M022010 VALUES LESS THAN (TO_DATE('03/01/2010', 'MM/DD/YYYY')),
 PARTITION ENTRY_DETAIL_RCD_M032010 VALUES LESS THAN (TO_DATE('04/01/2010', 'MM/DD/YYYY')),
 PARTITION ENTRY_DETAIL_RCD_M042010 VALUES LESS THAN (TO_DATE('05/01/2010', 'MM/DD/YYYY')),
 PARTITION ENTRY_DETAIL_RCD_M052010 VALUES LESS THAN (TO_DATE('06/01/2010', 'MM/DD/YYYY')),   
 PARTITION ENTRY_DETAIL_RCD_M062010 VALUES LESS THAN (TO_DATE('07/01/2010', 'MM/DD/YYYY')), 
 PARTITION ENTRY_DETAIL_RCD_M072010 VALUES LESS THAN (TO_DATE('08/01/2010', 'MM/DD/YYYY')), 
 PARTITION ENTRY_DETAIL_RCD_M082010 VALUES LESS THAN (TO_DATE('09/01/2010', 'MM/DD/YYYY')), 
 PARTITION ENTRY_DETAIL_RCD_M092010 VALUES LESS THAN (TO_DATE('10/01/2010', 'MM/DD/YYYY')),  
 PARTITION ENTRY_DETAIL_RCD_M102010 VALUES LESS THAN (TO_DATE('11/01/2010', 'MM/DD/YYYY')), 
 PARTITION ENTRY_DETAIL_RCD_M112010 VALUES LESS THAN (TO_DATE('12/01/2010', 'MM/DD/YYYY')), 
 PARTITION ENTRY_DETAIL_RCD_M122010 VALUES LESS THAN (TO_DATE('01/01/2011', 'MM/DD/YYYY')),  
 PARTITION ENTRY_DETAIL_RCD_9999    VALUES LESS THAN (MAXVALUE)
);

CREATE UNIQUE INDEX INT_XPKENTRY_DETAIL_RECORD
  ON INT_PSP_ENTRY_DETAIL_RECORD (ENTRY_DETAIL_RECORD_SEQ, REALM_ID);

ALTER TABLE INT_PSP_ENTRY_DETAIL_RECORD ADD (
  CONSTRAINT INT_XPKENTRY_DETAIL_RECORD
  PRIMARY KEY (ENTRY_DETAIL_RECORD_SEQ, REALM_ID));
 
ALTER TABLE PSP_ENTRY_DETAIL_RECORD       ENABLE ROW MOVEMENT;
ALTER TABLE INT_PSP_ENTRY_DETAIL_RECORD   ENABLE ROW MOVEMENT;


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

SELECT 'START REDEF : ' || TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI:SS') FROM DUAL;

DECLARE
  v_num_errors      PLS_INTEGER := 0;
  v_src_pk_idx_id   VARCHAR2(100);
  v_src_pk_con_id   VARCHAR2(100);  
BEGIN

	DBMS_OUTPUT.PUT_LINE ('Check to ensure redefinition is possible ...');

  DBMS_REDEFINITION.CAN_REDEF_TABLE(
    'PSPADM', 
    'PSP_ENTRY_DETAIL_RECORD'
  );

	DBMS_OUTPUT.PUT_LINE ('Start the redefinition process ...');
	
  DBMS_REDEFINITION.START_REDEF_TABLE (
    uname        => 'PSPADM',        
    orig_table   => 'PSP_ENTRY_DETAIL_RECORD',
    int_table    => 'INT_PSP_ENTRY_DETAIL_RECORD',
    options_flag => DBMS_REDEFINITION.CONS_USE_PK
  );
  
	DBMS_OUTPUT.PUT_LINE ('Add new constraints, indexes, and triggers to interim table ...');
	
  SELECT DISTINCT ui.INDEX_NAME 
    INTO v_src_pk_idx_id
    FROM USER_INDEXES     ui,
         USER_IND_COLUMNS uic
   WHERE ui.INDEX_NAME   = uic.INDEX_NAME
     AND ui.TABLE_NAME   = 'PSP_ENTRY_DETAIL_RECORD'
     AND ui.UNIQUENESS   = 'UNIQUE'
     AND uic.COLUMN_NAME = 'ENTRY_DETAIL_RECORD_SEQ';

	DBMS_REDEFINITION.REGISTER_DEPENDENT_OBJECT (
    uname            => 'PSPADM',        
    orig_table       => 'PSP_ENTRY_DETAIL_RECORD',
    int_table        => 'INT_PSP_ENTRY_DETAIL_RECORD',
    dep_type         => DBMS_REDEFINITION.CONS_INDEX,
    dep_owner        => 'PSPADM',
    dep_orig_name    => v_src_pk_idx_id,           -- primary key index
    dep_int_name     => 'INT_XPKENTRY_DETAIL_RECORD'
  );	  

  SELECT DISTINCT uc.CONSTRAINT_NAME
    INTO v_src_pk_con_id
    FROM USER_CONSTRAINTS uc,
         USER_CONS_COLUMNS ucc
   WHERE uc.CONSTRAINT_NAME = ucc.CONSTRAINT_NAME
     AND uc.TABLE_NAME      = 'PSP_ENTRY_DETAIL_RECORD'
     AND uc.CONSTRAINT_TYPE = 'P'
     AND ucc.COLUMN_NAME    = 'ENTRY_DETAIL_RECORD_SEQ';

	DBMS_REDEFINITION.REGISTER_DEPENDENT_OBJECT (
    uname            => 'PSPADM',        
    orig_table       => 'PSP_ENTRY_DETAIL_RECORD',
    int_table        => 'INT_PSP_ENTRY_DETAIL_RECORD',
    dep_type         => DBMS_REDEFINITION.CONS_CONSTRAINT,
    dep_owner        => 'PSPADM',
    dep_orig_name    => v_src_pk_con_id,           -- primary key check
    dep_int_name     => 'INT_XPKENTRY_DETAIL_RECORD'
  );	  

  DBMS_REDEFINITION.COPY_TABLE_DEPENDENTS (
    uname            => 'PSPADM',        
    orig_table       => 'PSP_ENTRY_DETAIL_RECORD',
    int_table        => 'INT_PSP_ENTRY_DETAIL_RECORD',
    copy_indexes     => DBMS_REDEFINITION.CONS_ORIG_PARAMS,
    copy_triggers    => TRUE,
    copy_constraints => TRUE,
    copy_privileges  => TRUE,
    ignore_errors    => FALSE,
    num_errors       => v_num_errors,
    copy_statistics  => FALSE
  );	
  
  DBMS_OUTPUT.PUT_LINE ('Copy Dependents error count = ' || v_num_errors);

	DBMS_OUTPUT.PUT_LINE ('Finish the process if all objects copied successfully ...');
	  
  IF (v_num_errors = 0) THEN
  
    DBMS_REDEFINITION.SYNC_INTERIM_TABLE(
      uname      => 'PSPADM',        
      orig_table => 'PSP_ENTRY_DETAIL_RECORD',
      int_table  => 'INT_PSP_ENTRY_DETAIL_RECORD'
    );

    DBMS_REDEFINITION.FINISH_REDEF_TABLE (
      uname      => 'PSPADM',        
      orig_table => 'PSP_ENTRY_DETAIL_RECORD',
      int_table  => 'INT_PSP_ENTRY_DETAIL_RECORD'
    );
  
  ELSE
    RAISE_APPLICATION_ERROR (
      -20051,
      'Error with COPY_TABLE_DEPENDENTS.',
      FALSE
    );
  END IF;

EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE (SQLERRM);    
END;
/
SHOW ERRORS
	
SELECT 'END REDEF : ' || TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI:SS') FROM DUAL;

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

SPOOL OFF