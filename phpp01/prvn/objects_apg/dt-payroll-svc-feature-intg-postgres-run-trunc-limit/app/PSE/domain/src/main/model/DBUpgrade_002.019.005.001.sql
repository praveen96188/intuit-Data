--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column PRIVATE_KEY_ENC;
ALTER TABLE PSP_COMPANY
 ADD (PRIVATE_KEY_ENC  VARCHAR2(4000 CHAR));

PROMPT finished DBUpgrade_002.019.005.001.sql