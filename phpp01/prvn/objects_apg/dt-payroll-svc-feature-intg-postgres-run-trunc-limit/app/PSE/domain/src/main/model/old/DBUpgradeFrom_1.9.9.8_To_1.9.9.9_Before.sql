--
-- This script will be executed BEFORE the automatically generated
-- C:\dev\psp\main\pse\domain\src\main\model\DBUpgradeFrom_1.9.9.8_To_1.9.9.9.sql
--
-- Developers can hand code logic here for data migration purposes
--

Prompt Constraint C_PSP_EVENT_TYPE0;

ALTER TABLE PSP_EVENT_TYPE
 DROP CONSTRAINT C_PSP_EVENT_TYPE0;

Prompt Constraint C_PSP_EVENT_DETAIL_TYPE0;

ALTER TABLE PSP_EVENT_DETAIL_TYPE
 DROP CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0;

Prompt Constraint C_PSP_COMPANY_EVENT1

ALTER TABLE PSP_COMPANY_EVENT
 DROP CONSTRAINT C_PSP_COMPANY_EVENT1;

Prompt Constraint C_PSP_COMPANY_EVENT2

ALTER TABLE PSP_COMPANY_EVENT
 DROP CONSTRAINT C_PSP_COMPANY_EVENT2;

Prompt Constraint C_PSP_COMPANY_EVENT_DETAIL0

ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 DROP CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0;

PROMPT updating existing RefundIssued events

UPDATE PSP_COMPANY_EVENT
SET event_type_cd = 'FeeRefunded'
WHERE event_type_cd = 'RefundIssued';

Prompt updating existing WireReceived events

UPDATE PSP_COMPANY_EVENT
SET event_type_cd = 'NonAchPaymentReceived'
WHERE event_type_cd = 'WireReceived';

PROMPT updating existing RefundIssued events

UPDATE PSP_EVENT_TYPE
SET event_type_cd = 'FeeRefunded'
WHERE event_type_cd = 'RefundIssued';

Prompt updating existing WireReceived events

UPDATE PSP_EVENT_TYPE
SET event_type_cd = 'NonAchPaymentReceived'
WHERE event_type_cd = 'WireReceived';

DELETE FROM PSP_EVENT_DETAIL_TYPE
WHERE event_detail_type_cd = 'RefundIssued';

Prompt Constraint C_PSP_EVENT_TYPE0

ALTER TABLE PSP_EVENT_TYPE
 ADD CONSTRAINT C_PSP_EVENT_TYPE0
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PayrollOffloaded', 'CompanyContactEmailChanged', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'FeeRefunded', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PaycheckCancelled', 'PayrollRecalledAfterOffload', 'PaycheckRecalledAfterOffload', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled'));

Prompt Constraint C_PSP_EVENT_DETAIL_TYPE0
ALTER TABLE PSP_EVENT_DETAIL_TYPE
 ADD CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldDDServiceStatus', 'NewDDServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'FeeAmount', 'OriginalTransactionDateTime', 'RefundedFeeBillingDetailId', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'BankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount'));

Prompt Constraint C_PSP_COMPANY_EVENT1
ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT1
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PayrollOffloaded', 'CompanyContactEmailChanged', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'FeeRefunded', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PaycheckCancelled', 'PayrollRecalledAfterOffload', 'PaycheckRecalledAfterOffload', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled'));

Prompt Constraint C_PSP_COMPANY_EVENT2
ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT2
 CHECK (EMAIL_STATUS IN('Pending', 'Sent', 'Ignore', 'Resend', 'SendFailed', 'GroupIncomplete'));

Prompt Constraint C_PSP_COMPANY_EVENT_DETAIL0
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 ADD CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldDDServiceStatus', 'NewDDServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'FeeAmount', 'OriginalTransactionDateTime', 'RefundedFeeBillingDetailId', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'BankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount'));
