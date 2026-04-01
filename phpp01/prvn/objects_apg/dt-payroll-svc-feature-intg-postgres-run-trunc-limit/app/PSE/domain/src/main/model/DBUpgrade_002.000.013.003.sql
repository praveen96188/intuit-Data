--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Table PSP_DELETED_RECORD;
CREATE TABLE PSP_DELETED_RECORD
(
  DELETED_RECORD_SEQ  VARCHAR2(255 CHAR),
  VERSION             NUMBER(19),
  CREATOR_ID          VARCHAR2(30 CHAR),
  CREATED_DATE        TIMESTAMP(6),
  MODIFIER_ID         VARCHAR2(30 CHAR),
  MODIFIED_DATE       TIMESTAMP(6),
  REALM_ID            NUMBER(19)                DEFAULT -1,
  RECORD_IDENTIFIER   VARCHAR2(4000 CHAR),
  TABLE_NAME          VARCHAR2(4000 CHAR)
)
NOPARALLEL;

ALTER TABLE PSP_DELETED_RECORD
 ADD PRIMARY KEY
  (DELETED_RECORD_SEQ, REALM_ID)
  USING INDEX;


Prompt Column TXP_RECORD_DATA;
ALTER TABLE PSP_ENTRY_DETAIL_RECORD
MODIFY(TXP_RECORD_DATA VARCHAR2(90 CHAR));


Prompt Column NOTE;
ALTER TABLE PSP_TAX_PAYMENT_ON_HOLD_REASON
 ADD (NOTE  VARCHAR2(500 CHAR));

PROMPT finishedDBUpgrade_002.000.013.003.sql