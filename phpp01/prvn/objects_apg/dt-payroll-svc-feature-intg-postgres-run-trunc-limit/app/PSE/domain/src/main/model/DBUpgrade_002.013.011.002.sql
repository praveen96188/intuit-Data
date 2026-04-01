--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column rename PSP_PAYROLL_RUN.COLLECTION_STAGE_FK to COLLECTION_STAGE_CD;
ALTER TABLE PSP_PAYROLL_RUN
 RENAME COLUMN COLLECTION_STAGE_FK TO COLLECTION_STAGE_CD;

Prompt Constraint C_PSP_PAYROLL_RUN0;
ALTER TABLE PSP_PAYROLL_RUN
 DROP CONSTRAINT C_PSP_PAYROLL_RUN0;

ALTER TABLE PSP_PAYROLL_RUN
 ADD CONSTRAINT C_PSP_PAYROLL_RUN0
  CHECK (COLLECTION_STAGE_CD IN('FirstCollectionAttempt', 'SecondCollectionAttempt', 'TerminationExpected')) NOVALIDATE;

Prompt Constraint C_PSP_PAYROLL_RUN1;
ALTER TABLE PSP_PAYROLL_RUN
 DROP CONSTRAINT C_PSP_PAYROLL_RUN1;

ALTER TABLE PSP_PAYROLL_RUN
 ADD CONSTRAINT C_PSP_PAYROLL_RUN1
  CHECK (PAYROLL_RUN_STATUS IN('Complete', 'Canceled', 'DebitReturnedCanceled', 'DebitReturned', 'NSFCanceled', 'OffloadedAll', 'OffloadedDebit', 'Pending', 'WrittenOff', 'PendingReversals', 'PendingAutoRedebit', 'AutoRedebitOffloaded', 'PendingRedebit', 'RedebitOffloaded', 'PendingWire', 'ReversalsOffloaded', 'ReversalsFinished', 'ReturnedTwice')) NOVALIDATE;

ALTER TABLE PSP_PAYROLL_RUN
 ADD CONSTRAINT C_PSP_PAYROLL_RUN2
  CHECK (PAYROLL_RUN_TYPE IN('FeeOnly', 'Regular', 'Adjustment', 'BillPayment', 'CloudOnly')) NOVALIDATE;

PROMPT finished DBUpgrade_002.013.011.002.sql