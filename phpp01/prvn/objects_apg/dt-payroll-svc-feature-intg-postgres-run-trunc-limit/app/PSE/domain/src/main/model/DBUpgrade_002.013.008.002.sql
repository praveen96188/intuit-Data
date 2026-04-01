--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column WATERMARK_DATE;
ALTER TABLE PSP_QUICKBOOKS_INFO
 ADD (WATERMARK_DATE  TIMESTAMP(6));

PROMPT finished DBUpgrade_002.013.008.002.sql