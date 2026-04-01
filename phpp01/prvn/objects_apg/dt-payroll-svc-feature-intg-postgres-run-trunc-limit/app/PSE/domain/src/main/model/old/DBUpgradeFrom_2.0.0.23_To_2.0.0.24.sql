--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
Prompt Table PSP_ACHENROLLMENT_BATCH;
--
-- PSP_ACHENROLLMENT_BATCH  (Table) 
--
CREATE TABLE PSP_ACHENROLLMENT_BATCH
(
  ACHENROLLMENT_BATCH_SEQ  VARCHAR2(255 CHAR)   NOT NULL,
  VERSION                  NUMBER(19)           NOT NULL,
  CREATOR_ID               VARCHAR2(30 CHAR),
  CREATED_DATE             TIMESTAMP(6)         NOT NULL,
  MODIFIER_ID              VARCHAR2(30 CHAR),
  MODIFIED_DATE            TIMESTAMP(6)         NOT NULL,
  REALM_ID                 NUMBER(19)           DEFAULT -1                    NOT NULL,
  END_DATE                 TIMESTAMP(6),
  START_DATE               TIMESTAMP(6)
)
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

Prompt Table PSP_ACHENROLLMENT;
--
-- PSP_ACHENROLLMENT  (Table) 
--
CREATE TABLE PSP_ACHENROLLMENT
(
  ACHENROLLMENT_SEQ      VARCHAR2(255 CHAR)     NOT NULL,
  VERSION                NUMBER(19)             NOT NULL,
  CREATOR_ID             VARCHAR2(30 CHAR),
  CREATED_DATE           TIMESTAMP(6)           NOT NULL,
  MODIFIER_ID            VARCHAR2(30 CHAR),
  MODIFIED_DATE          TIMESTAMP(6)           NOT NULL,
  REALM_ID               NUMBER(19)             DEFAULT -1                    NOT NULL,
  STATUS_CD              VARCHAR2(255 CHAR),
  STATUS_EFFECTIVE_DATE  TIMESTAMP(6),
  COMPANY_AGENCY_FK      VARCHAR2(255 CHAR)     NOT NULL
)
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

Prompt Index PSP_ACHENROLLMENT_FK2;
--
-- PSP_ACHENROLLMENT_FK2  (Index) 
--
CREATE INDEX PSP_ACHENROLLMENT_FK2 ON PSP_ACHENROLLMENT
(COMPANY_AGENCY_FK, REALM_ID)
LOGGING
NOPARALLEL;

Prompt Table PSP_ACHENROLLMENT_BATCH_ASSOC;
--
-- PSP_ACHENROLLMENT_BATCH_ASSOC  (Table) 
--
CREATE TABLE PSP_ACHENROLLMENT_BATCH_ASSOC
(
  A_C_H_ENROLLMENT_FK        VARCHAR2(255 CHAR) NOT NULL,
  A_C_H_ENROLLMENT_BATCH_FK  VARCHAR2(255 CHAR) NOT NULL,
  REALM_ID                   NUMBER(19)         DEFAULT -1                    NOT NULL
)
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

Prompt Index PSP_ACHENROL_FK_ACHENROLBATCH;
--
-- PSP_ACHENROL_FK_ACHENROLBATCH  (Index) 
--
CREATE INDEX PSP_ACHENROL_FK_ACHENROLBATCH ON PSP_ACHENROLLMENT_BATCH_ASSOC
(A_C_H_ENROLLMENT_BATCH_FK, REALM_ID)
LOGGING
NOPARALLEL;

Prompt Index PSP_ACHENROL_FK_ACHENROL;
--
-- PSP_ACHENROL_FK_ACHENROL  (Index) 
--
CREATE INDEX PSP_ACHENROL_FK_ACHENROL ON PSP_ACHENROLLMENT_BATCH_ASSOC
(A_C_H_ENROLLMENT_FK, REALM_ID)
LOGGING
NOPARALLEL;

Prompt Constraint C_PSP_EVENT_TYPE0;
ALTER TABLE PSP_EVENT_TYPE
 DROP CONSTRAINT C_PSP_EVENT_TYPE0;
ALTER TABLE PSP_EVENT_TYPE
 ADD CONSTRAINT C_PSP_EVENT_TYPE0
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PayrollOffloaded', 'CompanyContactEmailChanged', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'FeeRefunded', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PaycheckCancelled', 'PayrollRecalledAfterOffload', 'PaycheckRecalledAfterOffload', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'EnrollmentStatusChanged'));

Prompt Constraint C_PSP_EVENT_DETAIL_TYPE0;
ALTER TABLE PSP_EVENT_DETAIL_TYPE
 DROP CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0;
ALTER TABLE PSP_EVENT_DETAIL_TYPE
 ADD CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldServiceStatus', 'NewServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'FeeAmount', 'OriginalTransactionDateTime', 'RefundedFeeBillingDetailId', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'BankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount', 'ExpectedToken', 'ReceivedToken', 'Timestamp', 'UniqueIdentifier', 'UserId', 'ServiceCode', 'EnrollmentType'));

Prompt Constraint C_PSP_COMPANY_EVENT1;
ALTER TABLE PSP_COMPANY_EVENT
 DROP CONSTRAINT C_PSP_COMPANY_EVENT1;
ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT1
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PayrollOffloaded', 'CompanyContactEmailChanged', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'FeeRefunded', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PaycheckCancelled', 'PayrollRecalledAfterOffload', 'PaycheckRecalledAfterOffload', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'EnrollmentStatusChanged'));

Prompt Constraint C_PSP_COMPANY_EVENT_DETAIL0;
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 DROP CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0;
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 ADD CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldServiceStatus', 'NewServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'FeeAmount', 'OriginalTransactionDateTime', 'RefundedFeeBillingDetailId', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'BankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount', 'ExpectedToken', 'ReceivedToken', 'Timestamp', 'UniqueIdentifier', 'UserId', 'ServiceCode', 'EnrollmentType'));

ALTER TABLE PSP_ACHENROLLMENT_BATCH
 ADD PRIMARY KEY
 (ACHENROLLMENT_BATCH_SEQ, REALM_ID);

ALTER TABLE PSP_ACHENROLLMENT
 ADD CONSTRAINT C_PSP_ACHENROLLMENT0
 CHECK (STATUS_CD IN('Incomplete', 'InProgress', 'Downloaded', 'Complete'));

ALTER TABLE PSP_ACHENROLLMENT
 ADD PRIMARY KEY
 (ACHENROLLMENT_SEQ, REALM_ID);

ALTER TABLE PSP_ACHENROLLMENT
 ADD CONSTRAINT PSP_ACHENROLLMENT_FK2 
 FOREIGN KEY (COMPANY_AGENCY_FK, REALM_ID) 
 REFERENCES PSP_COMPANY_AGENCY (COMPANY_AGENCY_SEQ,REALM_ID);

ALTER TABLE PSP_ACHENROLLMENT_BATCH_ASSOC
 ADD PRIMARY KEY
 (A_C_H_ENROLLMENT_FK, A_C_H_ENROLLMENT_BATCH_FK, REALM_ID);

ALTER TABLE PSP_ACHENROLLMENT_BATCH_ASSOC
 ADD CONSTRAINT PSP_ACHENROL_FK_ACHENROL 
 FOREIGN KEY (A_C_H_ENROLLMENT_FK, REALM_ID) 
 REFERENCES PSP_ACHENROLLMENT (ACHENROLLMENT_SEQ,REALM_ID);

ALTER TABLE PSP_ACHENROLLMENT_BATCH_ASSOC
 ADD CONSTRAINT PSP_ACHENROL_FK_ACHENROLBATCH 
 FOREIGN KEY (A_C_H_ENROLLMENT_BATCH_FK, REALM_ID) 
 REFERENCES PSP_ACHENROLLMENT_BATCH (ACHENROLLMENT_BATCH_SEQ,REALM_ID);

PROMPT finishedDBUpgradeFrom_2.0.0.23_To_2.0.0.24.sql