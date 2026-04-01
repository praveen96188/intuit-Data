--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
Prompt Sequence SEQ_PAYROLL_FRAUD_BATCH_TOKEN;
--
-- SEQ_PAYROLL_FRAUD_BATCH_TOKEN  (Sequence)
--
CREATE SEQUENCE SEQ_PAYROLL_FRAUD_BATCH_TOKEN
  START WITH 1
  MAXVALUE 9999999999999999
  MINVALUE 1
  CYCLE
  CACHE 20
  NOORDER;

Prompt Table PSP_PAYROLL_FRAUD_BATCH;
--
-- PSP_PAYROLL_FRAUD_BATCH  (Table)
--
CREATE TABLE PSP_PAYROLL_FRAUD_BATCH
(
  PAYROLL_FRAUD_BATCH_SEQ       VARCHAR2(255 CHAR) NOT NULL,
  VERSION                       NUMBER(19)      NOT NULL,
  CREATOR_ID                    VARCHAR2(30 CHAR),
  CREATED_DATE                  TIMESTAMP(6)    NOT NULL,
  MODIFIER_ID                   VARCHAR2(30 CHAR),
  MODIFIED_DATE                 TIMESTAMP(6)    NOT NULL,
  REALM_ID                      NUMBER(19)      DEFAULT -1                    NOT NULL,
  MAX_PROCESSED_TOKEN           NUMBER(19),
  START_TIME                    TIMESTAMP(6),
  END_TIME                      TIMESTAMP(6),
  NUMBER_OF_PAYROLLS_PROCESSED  NUMBER(19)
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

ALTER TABLE PSP_PAYROLL_FRAUD_BATCH
 ADD PRIMARY KEY
 (PAYROLL_FRAUD_BATCH_SEQ, REALM_ID);

Prompt Column IS_FLAGGED_FOR_FRAUD;
ALTER TABLE PSP_COMPANY
 ADD (IS_FLAGGED_FOR_FRAUD  NUMBER(1));

Prompt Column SIGN_UP_DATE;
ALTER TABLE PSP_COMPANY
 ADD (SIGN_UP_DATE  TIMESTAMP(6));

Prompt Column PAYROLL_FRAUD_BATCH_TOKEN;
ALTER TABLE PSP_PAYROLL_RUN
 ADD (PAYROLL_FRAUD_BATCH_TOKEN  NUMBER(19));

Prompt Constraint C_PSP_EVENT_TYPE0;
ALTER TABLE PSP_EVENT_TYPE
 DROP CONSTRAINT C_PSP_EVENT_TYPE0;
ALTER TABLE PSP_EVENT_TYPE
 ADD CONSTRAINT C_PSP_EVENT_TYPE0
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'StrikeRemoved', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PSPSentEmail', 'PayrollOffloaded', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'RefundIssued', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'WireReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent'));

Prompt Constraint C_PSP_EVENT_DETAIL_TYPE0;
ALTER TABLE PSP_EVENT_DETAIL_TYPE
 DROP CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0;
ALTER TABLE PSP_EVENT_DETAIL_TYPE
 ADD CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldDDServiceStatus', 'NewDDServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'RefundIssued', 'FeeAmount', 'OriginalTransactionDateTime', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeNumberOfTotal', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory'));

Prompt Constraint C_PSP_COMPANY_EVENT1;
ALTER TABLE PSP_COMPANY_EVENT
 DROP CONSTRAINT C_PSP_COMPANY_EVENT1;
ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT1
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'StrikeRemoved', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PSPSentEmail', 'PayrollOffloaded', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'RefundIssued', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'WireReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent'));

Prompt Constraint C_PSP_COMPANY_EVENT_DETAIL0;
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 DROP CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0;
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 ADD CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldDDServiceStatus', 'NewDDServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'RefundIssued', 'FeeAmount', 'OriginalTransactionDateTime', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeNumberOfTotal', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory'));

 select 'finished DBUpgradeFrom_1.7.0.0_To_1.8.0.0.sql ' || to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') from dual