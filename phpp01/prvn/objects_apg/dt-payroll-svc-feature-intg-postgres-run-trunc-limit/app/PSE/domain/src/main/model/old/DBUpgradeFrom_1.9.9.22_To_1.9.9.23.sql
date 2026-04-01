--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
Prompt Column PRIVATE_KEY;
ALTER TABLE PSP_COMPANY
 ADD (PRIVATE_KEY  VARCHAR2(100 CHAR));

Prompt Column PUBLIC_KEY;
ALTER TABLE PSP_COMPANY
 ADD (PUBLIC_KEY  VARCHAR2(100 CHAR));

Prompt Column P_I_N;
ALTER TABLE PSP_COMPANY DROP COLUMN P_I_N;

Prompt Table PSP_COMPANY_PIN;
--
-- PSP_COMPANY_PIN  (Table)
--
CREATE TABLE PSP_COMPANY_PIN
(
  COMPANY_PIN_SEQ  VARCHAR2(255 CHAR)           NOT NULL,
  VERSION          NUMBER(19)                   NOT NULL,
  CREATOR_ID       VARCHAR2(30 CHAR),
  CREATED_DATE     TIMESTAMP(6)                 NOT NULL,
  MODIFIER_ID      VARCHAR2(30 CHAR),
  MODIFIED_DATE    TIMESTAMP(6)                 NOT NULL,
  REALM_ID         NUMBER(19)                   DEFAULT -1                    NOT NULL,
  P_I_N_VALUE      VARCHAR2(30 CHAR),
  COMPANY_FK       VARCHAR2(255 CHAR)           NOT NULL
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

Prompt Index PSP_COMPANY_PIN_FK1;
--
-- PSP_COMPANY_PIN_FK1  (Index)
--
CREATE INDEX PSP_COMPANY_PIN_FK1 ON PSP_COMPANY_PIN
(COMPANY_FK, REALM_ID)
LOGGING
NOPARALLEL;

ALTER TABLE PSP_COMPANY_PIN
 ADD PRIMARY KEY
 (COMPANY_PIN_SEQ, REALM_ID);

Prompt Constraint C_PSP_EVENT_TYPE0;
ALTER TABLE PSP_EVENT_TYPE
 DROP CONSTRAINT C_PSP_EVENT_TYPE0;
ALTER TABLE PSP_EVENT_TYPE
 ADD CONSTRAINT C_PSP_EVENT_TYPE0
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PayrollOffloaded', 'CompanyContactEmailChanged', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'FeeRefunded', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PaycheckCancelled', 'PayrollRecalledAfterOffload', 'PaycheckRecalledAfterOffload', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced'));

Prompt Constraint C_PSP_EVENT_DETAIL_TYPE0;
ALTER TABLE PSP_EVENT_DETAIL_TYPE
 DROP CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0;
ALTER TABLE PSP_EVENT_DETAIL_TYPE
 ADD CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldDDServiceStatus', 'NewDDServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'FeeAmount', 'OriginalTransactionDateTime', 'RefundedFeeBillingDetailId', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'BankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount', 'ExpectedToken', 'ReceivedToken', 'Timestamp', 'UniqueIdentifier'));

Prompt Constraint C_PSP_SOURCE_SYSTEM_TRANSM1;
ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 DROP CONSTRAINT C_PSP_SOURCE_SYSTEM_TRANSM1;
ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 ADD CONSTRAINT C_PSP_SOURCE_SYSTEM_TRANSM1
 CHECK (TYPE IN('Sync', 'PayrollSubmission', 'ZeroPayroll', 'CreateAccount', 'UpdateAccount', 'MigrateAccount', 'ValidateBankAccount', 'CreatePIN', 'ChangePIN', 'UpdateBankAccount', 'EntitlementExtensionUpdate', 'AgreementUpdate', 'AccountUpdate', 'ContactUpdate', 'Unknown', 'CUEVENT', 'CUINFOMOD', 'EntitlementExtensionNew', 'PayrollStatusQuery', 'ResetPIN'));

Prompt Constraint C_PSP_COMPANY_EVENT1;
ALTER TABLE PSP_COMPANY_EVENT
 DROP CONSTRAINT C_PSP_COMPANY_EVENT1;
ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT1
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PayrollOffloaded', 'CompanyContactEmailChanged', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'FeeRefunded', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PaycheckCancelled', 'PayrollRecalledAfterOffload', 'PaycheckRecalledAfterOffload', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced'));

Prompt Constraint C_PSP_COMPANY_EVENT_DETAIL0;
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 DROP CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0;
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 ADD CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldDDServiceStatus', 'NewDDServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'FeeAmount', 'OriginalTransactionDateTime', 'RefundedFeeBillingDetailId', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'BankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount', 'ExpectedToken', 'ReceivedToken', 'Timestamp', 'UniqueIdentifier'));

ALTER TABLE PSP_COMPANY_PIN
 ADD CONSTRAINT PSP_COMPANY_PIN_FK1
 FOREIGN KEY (COMPANY_FK, REALM_ID)
 REFERENCES PSP_COMPANY (COMPANY_SEQ,REALM_ID);

 PROMPT finishedDBUpgradeFrom_1.9.9.22_To_1.9.9.23.sql