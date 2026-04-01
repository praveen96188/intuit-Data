--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column TAX_FUNDING_MODEL;
ALTER TABLE PSP_ENTRY_DETAIL_RECORD
 ADD (TAX_FUNDING_MODEL  VARCHAR2(4000 CHAR));

PROMPT finished DBUpgrade_002.020.010.001.sql