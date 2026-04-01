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

SPOOL dbupgrade_PSRV001099a1.log

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


PROMPT .
PROMPT Get initial table count for PSP_ENTRY_DETAIL_RECORD ...

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

-- 02.18.2009 EMR  to avoid resource busy ora error do prior to redef
-- ALTER TABLE PSP_ENTRY_DETAIL_RECORD       ENABLE ROW MOVEMENT;
ALTER TABLE INT_PSP_ENTRY_DETAIL_RECORD   ENABLE ROW MOVEMENT;


PROMPT . 
PROMPT Part a1 complete, please run part a2 if successful ...

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;

SPOOL OFF