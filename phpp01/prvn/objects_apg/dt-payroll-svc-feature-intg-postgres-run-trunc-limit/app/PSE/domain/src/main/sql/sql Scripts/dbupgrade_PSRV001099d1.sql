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

SPOOL dbupgrade_PSRV001099d1.log

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


PROMPT .
PROMPT Get initial table count for PSP_MONEY_MOVEMENT_TRANSACTION ...

SELECT COUNT(*) FROM PSP_MONEY_MOVEMENT_TRANSACTION;


PROMPT .
PROMPT Create an interim table ...

-- design notes : the partition format is MG<1-6>YYYY, where 
--                  MG   = month group, 1 <J/F>, 2 <M/A>, 3 <M/J>, 4 <J/A>, 5 <S/O>, 6 <N/D>
--                  YYYY = year
-- questions: 
--   how many partitions to initially create - eg stop at 2010 or more. can add later? 2 YEARS.
--   less than, or less than equal to

-- this is simply to compare to at the end
CREATE TABLE Z_REDEF_MONEY_MOVEMENT_TXN AS
SELECT * FROM PSP_MONEY_MOVEMENT_TRANSACTION WHERE 1=0;

-- this is the real deal for DBMS_REDEFINITION
CREATE TABLE INT_MONEY_MOVEMENT_TXN
(
  MONEY_MOVEMENT_TRANSACTION_SEQ  VARCHAR2(255),  -- no primary key so can use COPY_TABLE_DEPENDENTS
  VERSION                         NUMBER(19),
  CREATOR_ID                      VARCHAR2(30),
  CREATED_DATE                    TIMESTAMP(6),
  MODIFIER_ID                     VARCHAR2(30),
  MODIFIED_DATE                   TIMESTAMP(6),
  REALM_ID                        NUMBER(19) DEFAULT -1,
  DUE_DATE                        TIMESTAMP(6),
  INITIATION_DATE                 TIMESTAMP(6),
  MM_TRANSACTION_AMOUNT           NUMBER(19,4),
  STATUS                          VARCHAR2(255),
  MONEY_MOVEMENT_PAYMENT_METHOD   VARCHAR2(255),
  ORIGINAL_INITIATION_DATE        TIMESTAMP(6),
  DEPOSIT_FREQUENCY_FK            VARCHAR2(255),
  COMPANY_FK                      VARCHAR2(255),
  OFFLOAD_BATCH_FK                VARCHAR2(255)
)
PARTITION BY RANGE (INITIATION_DATE)
(
 PARTITION MONEY_MOVEMENT_TXN_MG62008 VALUES LESS THAN (TO_DATE('01/01/2009', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_MG12009 VALUES LESS THAN (TO_DATE('03/01/2009', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_MG22009 VALUES LESS THAN (TO_DATE('05/01/2009', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_MG32009 VALUES LESS THAN (TO_DATE('07/01/2009', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_MG42009 VALUES LESS THAN (TO_DATE('09/01/2009', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_MG52009 VALUES LESS THAN (TO_DATE('11/01/2009', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_MG62009 VALUES LESS THAN (TO_DATE('01/01/2010', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_MG12010 VALUES LESS THAN (TO_DATE('03/01/2010', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_MG22010 VALUES LESS THAN (TO_DATE('05/01/2010', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_MG32010 VALUES LESS THAN (TO_DATE('07/01/2010', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_MG42010 VALUES LESS THAN (TO_DATE('09/01/2010', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_MG52010 VALUES LESS THAN (TO_DATE('11/01/2010', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_MG62010 VALUES LESS THAN (TO_DATE('01/01/2011', 'MM/DD/YYYY')),
 PARTITION MONEY_MOVEMENT_TXN_9999    VALUES LESS THAN (MAXVALUE)
);

CREATE UNIQUE INDEX INT_XPKMONEY_MOVEMENT_TXN
  ON INT_MONEY_MOVEMENT_TXN (MONEY_MOVEMENT_TRANSACTION_SEQ, REALM_ID);

ALTER TABLE INT_MONEY_MOVEMENT_TXN ADD (
  CONSTRAINT INT_XPKMONEY_MOVEMENT_TXN
  PRIMARY KEY (MONEY_MOVEMENT_TRANSACTION_SEQ, REALM_ID));

-- 02.18.2009 EMR  to avoid resource busy ora error do prior to redef 
-- ALTER TABLE PSP_MONEY_MOVEMENT_TRANSACTION ENABLE ROW MOVEMENT;
ALTER TABLE INT_MONEY_MOVEMENT_TXN         ENABLE ROW MOVEMENT;


PROMPT . 
PROMPT Part d1 complete, please run part d2 if successful ...

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;

SPOOL OFF