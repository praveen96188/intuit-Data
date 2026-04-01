--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column ADDITIONAL_COMPANY_LAW_FK;
ALTER TABLE PSP_COMPANY_LAW
 ADD (ADDITIONAL_COMPANY_LAW_FK  VARCHAR2(255 CHAR));

Prompt Index PSP_COMPANY_LAW_FK4;
CREATE INDEX PSP_COMPANY_LAW_FK4 ON PSP_COMPANY_LAW
(ADDITIONAL_COMPANY_LAW_FK, REALM_ID)
NOPARALLEL;

ALTER TABLE PSP_COMPANY_LAW
 ADD CONSTRAINT PSP_COMPANY_LAW_FK4 
  FOREIGN KEY (ADDITIONAL_COMPANY_LAW_FK, REALM_ID) 
  REFERENCES PSP_COMPANY_LAW (COMPANY_LAW_SEQ,REALM_ID);

PROMPT finishedDBUpgrade_002.012.012.003.sql