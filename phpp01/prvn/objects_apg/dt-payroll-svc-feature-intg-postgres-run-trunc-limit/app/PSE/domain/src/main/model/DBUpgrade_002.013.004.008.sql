--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column OBSOLETE;
ALTER TABLE PSP_PMT_TEMPLATE_FREQUENCY
 ADD (OBSOLETE  NUMBER(1));

PROMPT finishedDBUpgrade_002.013.004.008.sql