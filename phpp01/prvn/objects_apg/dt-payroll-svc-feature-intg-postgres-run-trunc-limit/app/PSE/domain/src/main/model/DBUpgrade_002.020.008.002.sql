--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column FUNDING_MODEL;
ALTER TABLE PSP_PAYROLL_RUN
 ADD (FUNDING_MODEL  VARCHAR2(4000 CHAR));

Prompt Column SETTLEMENT_DATE;
ALTER TABLE PSP_ENTRY_DETAIL_RECORD
 ADD (SETTLEMENT_DATE  TIMESTAMP(6));

PROMPT finished DBUpgrade_002.020.008.002.sql