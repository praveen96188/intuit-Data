--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Table PSP_REPORT_JOB_SETUP;
CREATE TABLE PSP_REPORT_JOB_SETUP
(
  REPORT_NAME                 VARCHAR2(255 CHAR) NOT NULL,
  VERSION                     NUMBER(19)        NOT NULL,
  REALM_ID                    NUMBER(19)        DEFAULT -1                    NOT NULL,
  REPORT_SCHEDULE             VARCHAR2(4000 CHAR),
  REPORT_MAILING_LIST         VARCHAR2(4000 CHAR),
  QUERY_FILENAME              VARCHAR2(4000 CHAR),
  IS_AUTOMATICALLY_SCHEDULED  NUMBER(1),
  REPORT_NAMESPACE            VARCHAR2(4000 CHAR),
  ENCRYPTED_FIELDS            VARCHAR2(4000 CHAR)
)
NOPARALLEL;

ALTER TABLE PSP_REPORT_JOB_SETUP
 ADD PRIMARY KEY
  (REPORT_NAME, REALM_ID)
  USING INDEX;

PROMPT finished DBUpgrade_002.018.004.004.sql