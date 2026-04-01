--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column PROCESSED_BY_FRAUD_BATCH_JOB;
ALTER TABLE PSP_PAYROLL_RUN
 ADD PROCESSED_BY_FRAUD_BATCH_JOB NUMBER(1) default 0 NOT NULL;

PROMPT finished DBUpgrade_002.013.007.003.sql