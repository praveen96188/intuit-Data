--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column REPORTING_TYPE;
ALTER TABLE PSP_LEDGER_BALANCE
 ADD (REPORTING_TYPE  VARCHAR2(255 CHAR));

PROMPT finishedDBUpgrade_002.001.001.024.sql