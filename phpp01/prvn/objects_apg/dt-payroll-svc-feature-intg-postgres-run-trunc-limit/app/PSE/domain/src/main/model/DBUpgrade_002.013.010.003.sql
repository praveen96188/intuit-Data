--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column PROHIBIT_DEFAULT_IDS;
ALTER TABLE PSP_AGENCY_ID_REQUIREMENT
 ADD (PROHIBIT_DEFAULT_IDS  NUMBER(1) DEFAULT 1 NOT NULL);

PROMPT finished DBUpgrade_002.013.010.003.sql