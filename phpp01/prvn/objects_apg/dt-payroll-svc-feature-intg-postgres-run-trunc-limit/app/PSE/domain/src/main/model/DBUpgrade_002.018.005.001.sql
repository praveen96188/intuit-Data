--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column D_D_PUBLISH_FLAG;
ALTER TABLE PSP_COMPANY
 ADD D_D_PUBLISH_FLAG NUMBER(1) DEFAULT 0 NOT NULL;

PROMPT finished DBUpgrade_002.018.005.001.sql