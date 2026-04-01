--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column FILE_NAME;
ALTER TABLE PSP_FSET_FILE
MODIFY(FILE_NAME VARCHAR2(250 CHAR));

PROMPT finished DBUpgrade_002.017.009.001.sql