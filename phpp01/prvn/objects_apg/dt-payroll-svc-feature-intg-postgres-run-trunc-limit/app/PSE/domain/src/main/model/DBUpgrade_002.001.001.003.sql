--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column PAYROLL_DIRECT_DEPOSIT_AMOUNT;
ALTER TABLE PSP_PAYROLL_RUN RENAME COLUMN PAYROLL_NET_AMOUNT TO PAYROLL_DIRECT_DEPOSIT_AMOUNT;

Prompt Column PAYROLL_DIRECT_DEPOSIT_AMOUNT;
ALTER TABLE PSP_FRAUD_EVENT RENAME COLUMN PAYROLL_NET_AMOUNT TO PAYROLL_DIRECT_DEPOSIT_AMOUNT;

PROMPT finishedDBUpgrade_002.001.001.003.sql