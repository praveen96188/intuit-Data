--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column IN_HOUSE_W2;
ALTER TABLE PSP_TAX_COMPANY_SERVICE_INFO
 ADD (IN_HOUSE_W2  NUMBER(1));

Prompt Column INCLUDE_ON_S_S_A_FILE;
ALTER TABLE PSP_TAX_COMPANY_SERVICE_INFO
 ADD (INCLUDE_ON_S_S_A_FILE  NUMBER(1));

PROMPT finishedDBUpgrade_002.012.012.000.sql