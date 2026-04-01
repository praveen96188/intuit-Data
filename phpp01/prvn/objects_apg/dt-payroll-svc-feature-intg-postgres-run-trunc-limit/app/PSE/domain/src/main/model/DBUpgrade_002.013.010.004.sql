--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column DESCRIPTION;
ALTER TABLE PSP_LEDGER_OPERATION_JOB
 ADD (DESCRIPTION  VARCHAR2(256 CHAR));

Prompt Constraint C_PSP_LEDGER_OPERATION_JOB0;
ALTER TABLE PSP_LEDGER_OPERATION_JOB
 DROP CONSTRAINT C_PSP_LEDGER_OPERATION_JOB0;

ALTER TABLE PSP_LEDGER_OPERATION_JOB
 ADD CONSTRAINT C_PSP_LEDGER_OPERATION_JOB0
  CHECK (STATUS IN('Deleted', 'Queued', 'Created', 'InProgress', 'Complete')) NOVALIDATE;

Prompt Constraint C_PSP_LEDGER_OPERATION_JOB1;
ALTER TABLE PSP_LEDGER_OPERATION_JOB
 DROP CONSTRAINT C_PSP_LEDGER_OPERATION_JOB1;

ALTER TABLE PSP_LEDGER_OPERATION_JOB
 ADD CONSTRAINT C_PSP_LEDGER_OPERATION_JOB1
  CHECK (JOB_TYPE IN('BulkDebit', 'TOR', 'DepositFrequencyUpdate', 'RateUpdate', 'AdditionalFilingAmountUpdate')) NOVALIDATE;

PROMPT finished DBUpgrade_002.013.010.004.sql