-- CREATED  DATE: 1.14.2009
-- MODIFIED DATE: 1.21.2009  14:00
-- AUTHOR       : EMR
--
-- PURPOSE: 
--   This script will change the PSE financial transaction table from no partitions
--   to bi-monthly, or 6 per year.  The partition key will be on SETTLEMENT_DATE.
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

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


PROMPT .
PROMPT Get initial table count ...

SELECT COUNT(*) FROM PSP_FINANCIAL_TRANSACTION;


PROMPT .
PROMPT Create an interim table ...

-- design notes : the partition format is MG<1-6>YYYY, where 
--                  MG   = month group, 1 <J/F>, 2 <M/A>, 3 <M/J>, 4 <J/A>, 5 <S/O>, 6 <N/D>
--                  YYYY = year
-- questions: 
--   how many partitions to initially create - eg stop at 2010 or more. can add later?  2 YEARS.
--   less than, or less than equal to

-- this is simply to compare to at the end
CREATE TABLE Z_REDEF_FINANCIAL_TRANSACTION AS
SELECT * FROM PSP_FINANCIAL_TRANSACTION WHERE 1=0;

-- this is the real deal for DBMS_REDEFINITION
CREATE TABLE INT_PSP_FINANCIAL_TRANSACTION
(
  FINANCIAL_TRANSACTION_SEQ      VARCHAR2(255),
  VERSION                        NUMBER(19),
  CREATOR_ID                     VARCHAR2(30),
  CREATED_DATE                   TIMESTAMP(6),
  MODIFIER_ID                    VARCHAR2(30),
  MODIFIED_DATE                  TIMESTAMP(6),
  REALM_ID                       NUMBER(19) DEFAULT -1,
  FINANCIAL_TRANSACTION_AMOUNT   NUMBER(19,4),
  SETTLEMENT_DATE                TIMESTAMP(6),
  SETTLEMENT_TYPE_CD             VARCHAR2(255),
  CREDIT_BANK_ACCOUNT_TYPE       VARCHAR2(255),
  DEBIT_BANK_ACCOUNT_TYPE        VARCHAR2(255),
  ON_HOLD                        NUMBER(1),
  SKU                            VARCHAR2(40),
  SKU_QUANTITY                   NUMBER(10),
  BILLING_DETAIL_FK              VARCHAR2(255),
  CREDIT_BANK_ACCOUNT_FK         VARCHAR2(255),
  DEBIT_BANK_ACCOUNT_FK          VARCHAR2(255),
  COMPANY_FK                     VARCHAR2(255),
  PAYROLL_RUN_FK                 VARCHAR2(255),
  PAYCHECK_SPLIT_FK              VARCHAR2(255),
  TRANSACTION_TYPE_FK            VARCHAR2(255),
  LAW_FK                         VARCHAR2(255),
  CURRENT_TRANSACTION_STATE_FK   VARCHAR2(255),
  ORIGINAL_TRANSACTION_FK        VARCHAR2(255),
  MONEY_MOVEMENT_TRANSACTION_FK  VARCHAR2(255)
)
PARTITION BY RANGE (SETTLEMENT_DATE)
(
 PARTITION FINANCIAL_TXN_MG62008 VALUES LESS THAN (TO_DATE('01/01/2009', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_MG12009 VALUES LESS THAN (TO_DATE('03/01/2009', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_MG22009 VALUES LESS THAN (TO_DATE('05/01/2009', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_MG32009 VALUES LESS THAN (TO_DATE('07/01/2009', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_MG42009 VALUES LESS THAN (TO_DATE('09/01/2009', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_MG52009 VALUES LESS THAN (TO_DATE('11/01/2009', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_MG62009 VALUES LESS THAN (TO_DATE('01/01/2010', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_MG12010 VALUES LESS THAN (TO_DATE('03/01/2010', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_MG22010 VALUES LESS THAN (TO_DATE('05/01/2010', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_MG32010 VALUES LESS THAN (TO_DATE('07/01/2010', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_MG42010 VALUES LESS THAN (TO_DATE('09/01/2010', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_MG52010 VALUES LESS THAN (TO_DATE('11/01/2010', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_MG62010 VALUES LESS THAN (TO_DATE('01/01/2011', 'MM/DD/YYYY')),
 PARTITION FINANCIAL_TXN_9999    VALUES LESS THAN (MAXVALUE)
);

CREATE UNIQUE INDEX INT_XPKFINANCIAL_TXN
  ON INT_PSP_FINANCIAL_TRANSACTION (FINANCIAL_TRANSACTION_SEQ, REALM_ID);

ALTER TABLE INT_PSP_FINANCIAL_TRANSACTION ADD (
  CONSTRAINT INT_XPKFINANCIAL_TXN
  PRIMARY KEY (FINANCIAL_TRANSACTION_SEQ, REALM_ID));
 
ALTER TABLE PSP_FINANCIAL_TRANSACTION      ENABLE ROW MOVEMENT;
ALTER TABLE INT_PSP_FINANCIAL_TRANSACTION  ENABLE ROW MOVEMENT;


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
    'PSP_FINANCIAL_TRANSACTION'
  );

	DBMS_OUTPUT.PUT_LINE ('Start the redefinition process ...');
	
  DBMS_REDEFINITION.START_REDEF_TABLE (
    uname        => 'PSPADM',        
    orig_table   => 'PSP_FINANCIAL_TRANSACTION',
    int_table    => 'INT_PSP_FINANCIAL_TRANSACTION',
    options_flag => DBMS_REDEFINITION.CONS_USE_PK
  );
  
	DBMS_OUTPUT.PUT_LINE ('Add new constraints, indexes, and triggers to interim table ...');
	
  SELECT DISTINCT ui.INDEX_NAME 
    INTO v_src_pk_idx_id
    FROM USER_INDEXES     ui,
         USER_IND_COLUMNS uic
   WHERE ui.INDEX_NAME   = uic.INDEX_NAME
     AND ui.TABLE_NAME   = 'PSP_FINANCIAL_TRANSACTION'
     AND ui.UNIQUENESS   = 'UNIQUE'
     AND uic.COLUMN_NAME = 'FINANCIAL_TRANSACTION_SEQ';

	DBMS_REDEFINITION.REGISTER_DEPENDENT_OBJECT (
    uname            => 'PSPADM',        
    orig_table       => 'PSP_FINANCIAL_TRANSACTION',
    int_table        => 'INT_PSP_FINANCIAL_TRANSACTION',
    dep_type         => DBMS_REDEFINITION.CONS_INDEX,
    dep_owner        => 'PSPADM',
    dep_orig_name    => v_src_pk_idx_id,           -- primary key index
    dep_int_name     => 'INT_XPKFINANCIAL_TXN'
  );	  

  SELECT DISTINCT uc.CONSTRAINT_NAME
    INTO v_src_pk_con_id
    FROM USER_CONSTRAINTS uc,
         USER_CONS_COLUMNS ucc
   WHERE uc.CONSTRAINT_NAME = ucc.CONSTRAINT_NAME
     AND uc.TABLE_NAME      = 'PSP_FINANCIAL_TRANSACTION'
     AND uc.CONSTRAINT_TYPE = 'P'
     AND ucc.COLUMN_NAME    = 'FINANCIAL_TRANSACTION_SEQ';

	DBMS_REDEFINITION.REGISTER_DEPENDENT_OBJECT (
    uname            => 'PSPADM',        
    orig_table       => 'PSP_FINANCIAL_TRANSACTION',
    int_table        => 'INT_PSP_FINANCIAL_TRANSACTION',
    dep_type         => DBMS_REDEFINITION.CONS_CONSTRAINT,
    dep_owner        => 'PSPADM',
    dep_orig_name    => v_src_pk_con_id,           -- primary key check
    dep_int_name     => 'INT_XPKFINANCIAL_TXN'
  );	  

  DBMS_REDEFINITION.COPY_TABLE_DEPENDENTS (
    uname            => 'PSPADM',        
    orig_table       => 'PSP_FINANCIAL_TRANSACTION',
    int_table        => 'INT_PSP_FINANCIAL_TRANSACTION',
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
      orig_table => 'PSP_FINANCIAL_TRANSACTION',
      int_table  => 'INT_PSP_FINANCIAL_TRANSACTION'
    );

    DBMS_REDEFINITION.FINISH_REDEF_TABLE (
      uname      => 'PSPADM',        
      orig_table => 'PSP_FINANCIAL_TRANSACTION',
      int_table  => 'INT_PSP_FINANCIAL_TRANSACTION'
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

SELECT COUNT(*) FROM PSP_FINANCIAL_TRANSACTION;


PROMPT Remove interim table ...

DROP TABLE INT_PSP_FINANCIAL_TRANSACTION CASCADE CONSTRAINTS;


PROMPT .
PROMPT Rename all the constraints and indexes to match the original names ...

PROMPT not necessary because we used COPY_TABLE_DEPENDENTS

-- question: should we create this new index, and if so what are the keys.

CREATE INDEX PSP_FINANCIAL_TRANSACTION_I2 ON PSP_FINANCIAL_TRANSACTION
(  SETTLEMENT_DATE)
  LOCAL NOLOGGING COMPUTE STATISTICS
  TABLESPACE PSP_IDX01;


PROMPT .
PROMPT Enable all constraints ...

BEGIN
  FOR rec IN (
    SELECT DISTINCT 
           'ALTER TABLE ' || table_name || ' ENABLE CONSTRAINT ' || constraint_name sql_stmt
      FROM USER_CONSTRAINTS
     WHERE TABLE_NAME = 'PSP_FINANCIAL_TRANSACTION'
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