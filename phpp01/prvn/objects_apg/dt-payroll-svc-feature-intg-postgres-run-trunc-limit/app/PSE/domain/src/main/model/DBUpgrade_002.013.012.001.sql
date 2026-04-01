--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column IS_VIEWING_PAYSTUB_DISABLED;
ALTER TABLE PSP_EMPLOYEE
 ADD (IS_VIEWING_PAYSTUB_DISABLED  NUMBER(1) DEFAULT 0);

PROMPT finished DBUpgrade_002.013.012.001.sql