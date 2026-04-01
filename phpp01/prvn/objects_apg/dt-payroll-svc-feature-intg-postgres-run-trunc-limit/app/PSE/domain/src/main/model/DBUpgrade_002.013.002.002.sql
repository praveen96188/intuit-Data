--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 
Prompt Column SECONDARY;
ALTER TABLE PSP_EFTPS_ENROLLMENT
 ADD (SECONDARY  NUMBER(1) DEFAULT 0);

alter table psp_eftps_enrollment_detail modify (parent_file_fk null);

PROMPT finishedDBUpgrade_002.013.002.002.sql