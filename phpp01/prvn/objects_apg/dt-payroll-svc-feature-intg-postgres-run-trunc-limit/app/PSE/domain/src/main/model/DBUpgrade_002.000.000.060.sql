--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 

Prompt Table PSP_FRAUD_BANK_ACCOUNT;
--
-- PSP_FRAUD_BANK_ACCOUNT  (Table) 
--
CREATE TABLE PSP_FRAUD_BANK_ACCOUNT
(
  FRAUD_BANK_ACCOUNT_SEQ     VARCHAR2(255 CHAR) NOT NULL,
  VERSION                    NUMBER(19)         NOT NULL,
  CREATOR_ID                 VARCHAR2(30 CHAR),
  CREATED_DATE               TIMESTAMP(6)       NOT NULL,
  MODIFIER_ID                VARCHAR2(30 CHAR),
  MODIFIED_DATE              TIMESTAMP(6)       NOT NULL,
  REALM_ID                   NUMBER(19)         DEFAULT -1                    NOT NULL,
  ROUTING_NUMBER             VARCHAR2(20 CHAR),
  ACCOUNT_NUMBER             VARCHAR2(80 CHAR),
  ACCOUNT_TYPE_CD            VARCHAR2(255 CHAR),
  BANK_NAME                  VARCHAR2(256 CHAR),
  BANK_ACCOUNT_OWNER_NAME    VARCHAR2(256 CHAR),
  FRAUD_BANK_ACCOUNT_REASON  VARCHAR2(255 CHAR),
  COMPANY_FK                 VARCHAR2(255 CHAR)
)
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

Prompt Index PSP_FRAUDBANKACCOUNT_FK1;
--
-- PSP_FRAUDBANKACCOUNT_FK1  (Index) 
--
CREATE INDEX PSP_FRAUDBANKACCOUNT_FK1 ON PSP_FRAUD_BANK_ACCOUNT
(COMPANY_FK, REALM_ID)
LOGGING
NOPARALLEL;

ALTER TABLE PSP_FRAUD_BANK_ACCOUNT
 ADD PRIMARY KEY
 (FRAUD_BANK_ACCOUNT_SEQ, REALM_ID);

ALTER TABLE PSP_FRAUD_BANK_ACCOUNT
 ADD CONSTRAINT PSP_FRAUDBANKACCOUNT_FK1 
 FOREIGN KEY (COMPANY_FK, REALM_ID) 
 REFERENCES PSP_COMPANY (COMPANY_SEQ,REALM_ID);


PROMPT finishedDBUpgrade_002.000.000.060.sql