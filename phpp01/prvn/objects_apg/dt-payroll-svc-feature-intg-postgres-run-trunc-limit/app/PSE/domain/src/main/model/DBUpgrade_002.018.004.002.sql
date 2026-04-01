--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column DEBIT_SETTLEMENT_DATE;
ALTER TABLE PSP_PAYROLL_RUN
 ADD (DEBIT_SETTLEMENT_DATE  TIMESTAMP(6));

Prompt Column INITIATION_DATE;
ALTER TABLE PSP_PAYROLL_RUN
 ADD (INITIATION_DATE  TIMESTAMP(6));

PROMPT finished DBUpgrade_002.018.004.002.sql