--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column INTEGRATION_ID;
ALTER TABLE PSP_WC_COMPANY
 ADD (INTEGRATION_ID  VARCHAR2(255 CHAR));

PROMPT finished DBUpgrade_002.022.008.005.sql