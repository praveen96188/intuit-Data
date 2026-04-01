--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column D_D_STATUS;
ALTER TABLE PSP_PAYROLL_RUN
 ADD (D_D_STATUS  VARCHAR2(255 CHAR) DEFAULT 'None' NOT NULL);

Prompt Column TAX_AND_FEES_STATUS;
ALTER TABLE PSP_PAYROLL_RUN
 ADD (TAX_AND_FEES_STATUS  VARCHAR2(255 CHAR) DEFAULT 'None' NOT NULL);

Prompt Column D_D_MESSAGE_STATUS;
ALTER TABLE PSP_PAYROLL_RUN
 ADD (D_D_MESSAGE_STATUS  VARCHAR2(255 CHAR) DEFAULT 'None' NOT NULL);

Prompt Column D_D_MESSAGE_STATUS;
ALTER TABLE PSP_PAYCHECK
 ADD (D_D_MESSAGE_STATUS  VARCHAR2(255 CHAR) DEFAULT 'None' NOT NULL);

ALTER TABLE PSP_PAYROLL_RUN
 ADD CONSTRAINT C_PSP_PAYROLL_RUN3
  CHECK (D_D_STATUS IN('Pending', 'PendingToDD', 'SentToDD', 'Canceled', 'Complete', 'Fail', 'OffloadedDebit', 'OffloadedCredit', 'OffloadedAll', 'PendingVoid', 'SentVoid', 'PendingCompleteToDD', 'SentCompleteToDD', 'SentSupersededToDD', 'PendingSupersededToDD', 'Superseded', 'None', 'PendingPartialVoid', 'SentPartialVoid', 'ReversalsFinished', 'DebitReturnedCanceled', 'ReturnedTwice', 'PendingAutoRedebit', 'NSFCanceled', 'WrittenOff', 'PendingWire', 'PendingReversals', 'PendingRedebit', 'RedebitOffloaded', 'DebitReturned', 'AutoRedebitOffloaded', 'ReversalsOffloaded'))
  ENABLE NOVALIDATE;

ALTER TABLE PSP_PAYROLL_RUN
 ADD CONSTRAINT C_PSP_PAYROLL_RUN4
  CHECK (TAX_AND_FEES_STATUS IN('Superseded', 'Complete', 'Canceled', 'DebitReturnedCanceled', 'DebitReturned', 'NSFCanceled', 'OffloadedAll', 'OffloadedDebit', 'Pending', 'WrittenOff', 'PendingReversals', 'PendingAutoRedebit', 'AutoRedebitOffloaded', 'PendingRedebit', 'RedebitOffloaded', 'PendingWire', 'ReversalsOffloaded', 'ReversalsFinished', 'ReturnedTwice', 'PendingToDD', 'SentToDD', 'None'))
  ENABLE NOVALIDATE;

ALTER TABLE PSP_PAYROLL_RUN
 ADD CONSTRAINT C_PSP_PAYROLL_RUN5
  CHECK (D_D_MESSAGE_STATUS IN('PendingPartialVoid', 'SentPartialVoid', 'PendingCompleteToDD', 'SentCompleteToDD', 'SentSupersededToDD', 'PendingSupersededToDD', 'Superseded', 'OffloadedDebit', 'Pending', 'Canceled', 'Complete', 'None', 'Fail', 'PendingToDD', 'SentToDD', 'PendingVoid', 'SentVoid', 'OffloadedCredit', 'OffloadedAll'))
  ENABLE NOVALIDATE;

ALTER TABLE PSP_PAYCHECK
 ADD CONSTRAINT C_PSP_PAYCHECK1
  CHECK (D_D_MESSAGE_STATUS IN('PendingActive', 'PendingInactive', 'SentInactive', 'SentActive', 'SentDeleted', 'PendingDeleted', 'Deleted', 'Active', 'Inactive', 'None'))
  ENABLE NOVALIDATE;

PROMPT finished DBUpgrade_002.018.005.002.sql
