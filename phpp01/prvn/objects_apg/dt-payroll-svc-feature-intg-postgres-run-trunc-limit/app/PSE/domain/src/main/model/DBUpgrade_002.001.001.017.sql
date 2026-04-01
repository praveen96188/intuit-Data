--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column COMPANY_FK;
ALTER TABLE PSP_EMPLOYEE_W2_TOTALS
 ADD (COMPANY_FK VARCHAR2(255 CHAR));        

Prompt Index PSP_EMPLOYEE_W2_TOTALS_FK4;
CREATE INDEX PSP_EMPLOYEE_W2_TOTALS_FK4 ON PSP_EMPLOYEE_W2_TOTALS
(COMPANY_FK, REALM_ID)
NOPARALLEL;

PROMPT finishedDBUpgrade_002.001.001.017.sql