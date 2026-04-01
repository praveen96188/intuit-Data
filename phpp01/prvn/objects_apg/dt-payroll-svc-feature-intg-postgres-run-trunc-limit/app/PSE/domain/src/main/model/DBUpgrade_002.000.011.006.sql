--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
Prompt COLUMN VERSION;
ALTER TABLE PSP_COMPANY
MODIFY(VERSION  DEFAULT 0);

Prompt COLUMN VERSION;
ALTER TABLE PSP_AUTH_USER
MODIFY(VERSION  DEFAULT 0);

Prompt TABLE PSP_SYSTEM_PAYMENT_REQUIREMENT;
--
-- PSP_SYSTEM_PAYMENT_REQUIREMENT  (Table) 
--
CREATE TABLE PSP_SYSTEM_PAYMENT_REQUIREMENT
(
  SYSTEM_PAYMENT_REQUIREMENT_SEQ  VARCHAR2(255 CHAR) NOT NULL,
  REALM_ID                        NUMBER(19)    DEFAULT -1                    NOT NULL,
  SYSTEM_REQUIREMENT_TYPE         VARCHAR2(255 CHAR)
)
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

ALTER TABLE PSP_SYSTEM_PAYMENT_REQUIREMENT
 ADD PRIMARY KEY
 (SYSTEM_PAYMENT_REQUIREMENT_SEQ, REALM_ID);

ALTER TABLE PSP_SYSTEM_PAYMENT_REQUIREMENT
 ADD CONSTRAINT PSP_SYSTEM_PAYMENT_REQUIRE_FK1 
 FOREIGN KEY (SYSTEM_PAYMENT_REQUIREMENT_SEQ, REALM_ID) 
 REFERENCES PSP_PAYMENT_REQUIREMENT (PAYMENT_REQUIREMENT_SEQ,REALM_ID);


PROMPT finishedDBUpgrade_002.000.011.006.sql