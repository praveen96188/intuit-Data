-- FILE NAME    : dbupgrade_PSRV001197_PCS_a1.sql
-- CREATED  DATE: 3.20.2009
-- MODIFIED DATE: 3.26.2009  02:00 PM
--                - 
-- AUTHOR       : EMR
--
-- PURPOSE: 
--   This script will change the PSP PAYCHECK SPLIT table from no partitions  
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

SPOOL dbupgrade_PSRV001197_PCS_a1.log

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;


PROMPT .
PROMPT Get initial table count for PSP_PAYCHECK_SPLIT ...

SELECT COUNT(*) FROM PSP_PAYCHECK_SPLIT;


PROMPT .
PROMPT Create an interim table ...

-- design note: the partition format is BA<1-2>YYYY, where 
--                BA   = biannual timeperiod, JFMAMJ and JASOND.
--                YYYY = year

-- this is simply to compare to at the end
CREATE TABLE Z_REDEF_PAYCHECK_SPLIT AS
SELECT * FROM PSP_PAYCHECK_SPLIT WHERE 1=0;

-- this is the real deal for DBMS_REDEFINITION
CREATE TABLE INT_PSP_PAYCHECK_SPLIT
(
  PAYCHECK_SPLIT_SEQ        VARCHAR2(255 CHAR),
  VERSION                   NUMBER(19),
  CREATOR_ID                VARCHAR2(30 CHAR),
  CREATED_DATE              TIMESTAMP(6),
  MODIFIER_ID               VARCHAR2(30 CHAR),
  MODIFIED_DATE             TIMESTAMP(6),
  REALM_ID                  NUMBER(19) DEFAULT -1,
  PAYCHECK_SPLIT_AMOUNT     NUMBER(19,4),
  SOURCE_DD_TXN_ID          VARCHAR2(50 CHAR),
  EMPLOYEE_BANK_ACCOUNT_FK  VARCHAR2(255 CHAR),
  PAYCHECK_FK               VARCHAR2(255 CHAR)
)
PARTITION BY RANGE (CREATED_DATE)
(
 PARTITION PAYCHKSPLIT_2008    VALUES LESS THAN (TO_DATE('01/01/2009', 'MM/DD/YYYY')),
 PARTITION PAYCHKSPLIT_BA12009 VALUES LESS THAN (TO_DATE('07/01/2009', 'MM/DD/YYYY')),
 PARTITION PAYCHKSPLIT_BA22009 VALUES LESS THAN (TO_DATE('01/01/2010', 'MM/DD/YYYY')),
 PARTITION PAYCHKSPLIT_BA12010 VALUES LESS THAN (TO_DATE('07/01/2010', 'MM/DD/YYYY')),
 PARTITION PAYCHKSPLIT_BA22010 VALUES LESS THAN (TO_DATE('01/01/2011', 'MM/DD/YYYY')),
 PARTITION PAYCHKSPLIT_BA12011 VALUES LESS THAN (TO_DATE('07/01/2011', 'MM/DD/YYYY')),
 PARTITION PAYCHKSPLIT_BA22011 VALUES LESS THAN (TO_DATE('01/01/2012', 'MM/DD/YYYY')),
 PARTITION PAYCHKSPLIT_9999    VALUES LESS THAN (MAXVALUE)
);

CREATE UNIQUE INDEX INT_XPKPAYCHECK_SPLIT
  ON INT_PSP_PAYCHECK_SPLIT (PAYCHECK_SPLIT_SEQ, REALM_ID);

ALTER TABLE INT_PSP_PAYCHECK_SPLIT ADD (
  CONSTRAINT INT_XPKPAYCHECK_SPLIT
  PRIMARY KEY (PAYCHECK_SPLIT_SEQ, REALM_ID));

-- 03.20.2009 EMR  to avoid resource busy ora error do prior to redef
-- ALTER TABLE INT_PSP_PAYCHECK_SPLIT       ENABLE ROW MOVEMENT;
ALTER TABLE INT_PSP_PAYCHECK_SPLIT   ENABLE ROW MOVEMENT;


PROMPT . 
PROMPT Part a1 complete, please run part a2 if successful ...

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS TIME_MARK FROM DUAL;

SPOOL OFF