--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
Prompt Column COMPANY_FK;
ALTER TABLE PSP_CHECK_PRINT_PAYCHECK
 ADD (COMPANY_FK  VARCHAR2(255 CHAR)                NOT NULL);

Prompt Index PSP_CHECKPRINTPAYCHECK_FK2;
--
-- PSP_CHECKPRINTPAYCHECK_FK2  (Index) 
--
CREATE INDEX PSP_CHECKPRINTPAYCHECK_FK2 ON PSP_CHECK_PRINT_PAYCHECK
(COMPANY_FK, REALM_ID)
LOGGING
NOPARALLEL;

ALTER TABLE PSP_CHECK_PRINT_PAYCHECK
 ADD CONSTRAINT PSP_CHECKPRINTPAYCHECK_FK2 
 FOREIGN KEY (COMPANY_FK, REALM_ID) 
 REFERENCES PSP_COMPANY (COMPANY_SEQ,REALM_ID);


PROMPT finishedDBUpgrade_002.000.000.046.sql