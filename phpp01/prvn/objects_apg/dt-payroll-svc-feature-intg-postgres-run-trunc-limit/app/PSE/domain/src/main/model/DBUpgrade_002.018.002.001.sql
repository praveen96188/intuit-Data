--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column APPROVAL_DATE_TIME_END;
ALTER TABLE PSP_PAYCHECK
 ADD (APPROVAL_DATE_TIME_END  TIMESTAMP(6));

PROMPT finished DBUpgrade_002.018.002.001.sql