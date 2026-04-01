--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column PUBLISH_STATUS;
ALTER TABLE PSP_PAYEE
 ADD (PUBLISH_STATUS  VARCHAR2(16 CHAR) DEFAULT '0000000000000000');

Prompt Constraint C_PSP_SERVICE_SUB_STATUS0;
ALTER TABLE PSP_SERVICE_SUB_STATUS
 DROP CONSTRAINT C_PSP_SERVICE_SUB_STATUS0;

ALTER TABLE PSP_SERVICE_SUB_STATUS
 ADD CONSTRAINT C_PSP_SERVICE_SUB_STATUS0
  CHECK (SERVICE_SUB_STATUS_CD IN('AchRejectOther', 'AchRejectR1R9', 'ActiveCurrent', 'ActiveSeasonal', 'Cancelled', 'DirectDepositLimit', 'Fraud', 'AuditCorrections', 'FraudReview', 'IntuitCollections', 'MissingPaperwork', 'PendingBalanceFile', 'PendingBankVerification', 'PendingFirstPayroll', 'PendingPinCreation', 'PendingTermination', 'RiskAssessment', 'RiskCollections', 'SuspendedDirectDeposit', 'Terminated', 'AMLHold', 'PendingTaxAcceptance', 'PendingEnrollment', 'PendingPrefundingWire', 'AS400Hold', 'AS400DirectDepositLimitHold', 'PendingSetup', 'BillPaymentLimit', 'MTLHold')) enable novalidate;

Prompt Constraint C_PSP_COMPANY_SERVICE0;
ALTER TABLE PSP_COMPANY_SERVICE
 DROP CONSTRAINT C_PSP_COMPANY_SERVICE0;

ALTER TABLE PSP_COMPANY_SERVICE
 ADD CONSTRAINT C_PSP_COMPANY_SERVICE0
  CHECK (STATUS_CD IN('AchRejectOther', 'AchRejectR1R9', 'ActiveCurrent', 'ActiveSeasonal', 'Cancelled', 'DirectDepositLimit', 'Fraud', 'AuditCorrections', 'FraudReview', 'IntuitCollections', 'MissingPaperwork', 'PendingBalanceFile', 'PendingBankVerification', 'PendingFirstPayroll', 'PendingPinCreation', 'PendingTermination', 'RiskAssessment', 'RiskCollections', 'SuspendedDirectDeposit', 'Terminated', 'AMLHold', 'PendingTaxAcceptance', 'PendingEnrollment', 'PendingPrefundingWire', 'AS400Hold', 'AS400DirectDepositLimitHold', 'PendingSetup', 'BillPaymentLimit', 'MTLHold')) enable novalidate;

Prompt Constraint C_PSP_ON_HOLD_REASON0;
ALTER TABLE PSP_ON_HOLD_REASON
 DROP CONSTRAINT C_PSP_ON_HOLD_REASON0;

ALTER TABLE PSP_ON_HOLD_REASON
 ADD CONSTRAINT C_PSP_ON_HOLD_REASON0
  CHECK (ON_HOLD_REASON_CD IN('AchRejectOther', 'AchRejectR1R9', 'ActiveCurrent', 'ActiveSeasonal', 'Cancelled', 'DirectDepositLimit', 'Fraud', 'AuditCorrections', 'FraudReview', 'IntuitCollections', 'MissingPaperwork', 'PendingBalanceFile', 'PendingBankVerification', 'PendingFirstPayroll', 'PendingPinCreation', 'PendingTermination', 'RiskAssessment', 'RiskCollections', 'SuspendedDirectDeposit', 'Terminated', 'AMLHold', 'PendingTaxAcceptance', 'PendingEnrollment', 'PendingPrefundingWire', 'AS400Hold', 'AS400DirectDepositLimitHold', 'PendingSetup', 'BillPaymentLimit', 'MTLHold')) enable novalidate;

PROMPT finished DBUpgrade_002.021.010.001.sql
