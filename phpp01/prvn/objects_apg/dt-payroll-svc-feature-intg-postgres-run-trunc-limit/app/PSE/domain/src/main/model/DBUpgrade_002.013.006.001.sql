--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

prompt expiration_date;
alter table psp_company_law_rate drop column expiration_date;

prompt ach_enrollment;
alter table psp_achenrollment add EFFECTIVE_DATE TIMESTAMP(6);

PROMPT finishedDBUpgrade_002.013.006.001.sql