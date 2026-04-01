-- CREATED  DATE: 1.14.2009
-- MODIFIED DATE: 2.19.2009  11:10
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

SPOOL dbupgrade_PSRV001099c1.log

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


PROMPT .
PROMPT Get initial table count for PSP_FINANCIAL_TRANSACTION ...

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

-- 02.18.2009 EMR  to avoid resource busy ora error do prior to redef 
-- ALTER TABLE PSP_FINANCIAL_TRANSACTION      ENABLE ROW MOVEMENT;
ALTER TABLE INT_PSP_FINANCIAL_TRANSACTION  ENABLE ROW MOVEMENT;


PROMPT . 
PROMPT Part c1 complete, please run part c2 if successful ...

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


SPOOL OFF