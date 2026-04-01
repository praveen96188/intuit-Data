--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "START TIME" from dual;

Prompt TABLE PSP_TAX_PAYMENT_ON_HOLD_REASON;
--
-- PSP_TAX_PAYMENT_ON_HOLD_REASON  (Table) 
--
CREATE TABLE PSP_TAX_PAYMENT_ON_HOLD_REASON
(
  TAX_PAYMENT_ON_HOLD_REASON_SEQ  VARCHAR2(255 CHAR) NOT NULL,
  VERSION                         NUMBER(19)    NOT NULL,
  CREATOR_ID                      VARCHAR2(30 CHAR),
  CREATED_DATE                    TIMESTAMP(6)  NOT NULL,
  MODIFIER_ID                     VARCHAR2(30 CHAR),
  MODIFIED_DATE                   TIMESTAMP(6)  NOT NULL,
  REALM_ID                        NUMBER(19)    DEFAULT -1                    NOT NULL,
  EFFECTIVE_DATE                  TIMESTAMP(6),
  EXPIRATION_DATE                 TIMESTAMP(6),
  ON_HOLD_REASON_CD               VARCHAR2(255 CHAR),
  MONEY_MOVEMENT_TRANSACTION_FK   VARCHAR2(255 CHAR)
)
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

Prompt INDEX PSP_TAXPAYMENTONHOLDREASON_FK1;
--
-- PSP_TAXPAYMENTONHOLDREASON_FK1  (Index) 
--
CREATE INDEX PSP_TAXPAYMENTONHOLDREASON_FK1 ON PSP_TAX_PAYMENT_ON_HOLD_REASON
(MONEY_MOVEMENT_TRANSACTION_FK, REALM_ID)
LOGGING
NOPARALLEL;

ALTER TABLE PSP_TAX_PAYMENT_ON_HOLD_REASON
 ADD PRIMARY KEY
 (TAX_PAYMENT_ON_HOLD_REASON_SEQ, REALM_ID);

ALTER TABLE PSP_TAX_PAYMENT_ON_HOLD_REASON
 ADD CONSTRAINT PSP_TAXPAYMENTONHOLDREASON_FK1 
 FOREIGN KEY (MONEY_MOVEMENT_TRANSACTION_FK, REALM_ID) 
 REFERENCES PSP_MONEY_MOVEMENT_TRANSACTION (MONEY_MOVEMENT_TRANSACTION_SEQ,REALM_ID);

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "END TIME" from dual;


PROMPT finishedDBUpgrade_002.000.010.076.sql