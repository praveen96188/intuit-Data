--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

-- Prompt Column MAX_PROCESSED_TOKEN;
-- ALTER TABLE PSP_PAYROLL_FRAUD_BATCH DROP COLUMN MAX_PROCESSED_TOKEN;

-- Prompt Column PAYROLL_FRAUD_BATCH_TOKEN;
-- ALTER TABLE PSP_PAYROLL_RUN DROP COLUMN PAYROLL_FRAUD_BATCH_TOKEN;

-- Prompt Column RESPONSE_DOCUMENT_OLD;
-- ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION DROP COLUMN RESPONSE_DOCUMENT_OLD;

-- Prompt Column REQUEST_DOCUMENT_OLD;
-- ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION DROP COLUMN REQUEST_DOCUMENT_OLD;

PROMPT finished DBUpgrade_002.013.007.004.sql