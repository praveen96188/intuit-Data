--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL

-- Remove primary keys
ALTER TABLE PSP_STATERPT_PMNTTEMPFRQ_ASSOC drop PRIMARY KEY;

ALTER TABLE PSP_STATERPT_PMNTTEMPFRQ_ASSOC
 ADD  (
  STATE_REPORT_ASSOC_SEQ         VARCHAR2(255 CHAR),
  VERSION                        NUMBER(19),
  CREATOR_ID                     VARCHAR2(30 CHAR),
  CREATED_DATE                   TIMESTAMP(6),
  MODIFIER_ID                    VARCHAR2(30 CHAR),
  MODIFIED_DATE                  TIMESTAMP(6)
);

-- Add row data SEQ GUIDS
UPDATE PSP_STATERPT_PMNTTEMPFRQ_ASSOC SET STATE_REPORT_ASSOC_SEQ = FN_FORMAT_SYSGUID(SYS_GUID()), VERSION = 1, CREATED_DATE = SYS_EXTRACT_UTC(SYSTIMESTAMP), MODIFIED_DATE = SYS_EXTRACT_UTC(SYSTIMESTAMP);


 -- Go back to not null
 ALTER TABLE PSP_STATERPT_PMNTTEMPFRQ_ASSOC
 MODIFY (
  STATE_REPORT_ASSOC_SEQ         VARCHAR2(255 CHAR) NOT NULL,
  VERSION                        NUMBER(19)     NOT NULL,
  CREATED_DATE                   TIMESTAMP(6)   NOT NULL,
  MODIFIED_DATE                  TIMESTAMP(6)   NOT NULL
);

ALTER TABLE PSP_STATERPT_PMNTTEMPFRQ_ASSOC
 ADD PRIMARY KEY
 (STATE_REPORT_ASSOC_SEQ, REALM_ID);
 
 ALTER TABLE PSP_STATERPT_PMNTTEMPFRQ_ASSOC
 RENAME TO PSP_STATE_REPORT_ASSOC;




PROMPT finishedDBUpgrade_002.000.011.008.sql
