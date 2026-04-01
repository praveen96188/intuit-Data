--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column ER_FICA_DEFERRAL_ENABLED;
ALTER TABLE PSP_COMPANY_AGENCY
 ADD ER_FICA_DEFERRAL_ENABLED  NUMBER(1,0) DEFAULT 0;

PROMPT finished DBUpgrade_002.020.004.001.sql