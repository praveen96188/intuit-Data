--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Table PSP_COMPANY_CONSENT;
CREATE TABLE PSP_COMPANY_CONSENT
(
  COMPANY_CONSENT_SEQ  VARCHAR2(255 CHAR)       NOT NULL,
  VERSION              NUMBER(19)               NOT NULL,
  CREATOR_ID           VARCHAR2(30 CHAR),
  CREATED_DATE         TIMESTAMP(6)             NOT NULL,
  MODIFIER_ID          VARCHAR2(30 CHAR),
  MODIFIED_DATE        TIMESTAMP(6)             NOT NULL,
  REALM_ID             NUMBER(19)               DEFAULT -1                    NOT NULL,
  FEIN                 VARCHAR2(4000 CHAR),
  SIGNUP_DATE          TIMESTAMP(6),
  SIGNED               NUMBER(1),
  APP_ID               VARCHAR2(4000 CHAR),
  APP_NAME             VARCHAR2(4000 CHAR)
)
NOPARALLEL;

ALTER TABLE PSP_COMPANY_CONSENT
 ADD PRIMARY KEY
  (COMPANY_CONSENT_SEQ, REALM_ID)
  USING INDEX;

Prompt Index PSP_COMPANY_CONSENT_I1;
CREATE INDEX PSP_COMPANY_CONSENT_I1 ON PSP_COMPANY_CONSENT
(FEIN)
NOPARALLEL;

PROMPT finished DBUpgrade_002.018.002.002.sql