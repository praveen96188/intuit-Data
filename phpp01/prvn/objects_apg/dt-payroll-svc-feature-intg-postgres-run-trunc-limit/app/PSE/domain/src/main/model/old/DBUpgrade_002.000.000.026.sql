--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
ALTER TABLE PSP_RAAENROLLMENT
 DROP CONSTRAINT PSP_RAAENROLLMENT_FK1;

Prompt drop Index PSP_RAAENROLLMENT_FK1;
DROP INDEX PSP_RAAENROLLMENT_FK1;

Prompt drop TABLE PSP_FORM;
ALTER TABLE PSP_FORM
 DROP PRIMARY KEY CASCADE;
DROP TABLE PSP_FORM CASCADE CONSTRAINTS;

Prompt Column NAME;
ALTER TABLE PSP_AGENCY
 ADD (NAME  VARCHAR2(300 CHAR));

Prompt Column DEFAULT_R_A_A_FORM;
ALTER TABLE PSP_AGENCY
 ADD (DEFAULT_R_A_A_FORM  VARCHAR2(255 CHAR));

Prompt Column A_C_H_ENROLLMENT_REQUIRED;
ALTER TABLE PSP_AGENCY
 ADD (A_C_H_ENROLLMENT_REQUIRED  NUMBER(1));

Prompt Column R_A_A_ENROLLMENT_REQUIRED;
ALTER TABLE PSP_AGENCY
 ADD (R_A_A_ENROLLMENT_REQUIRED  NUMBER(1));

Prompt Column R_A_F_ENROLLMENT_REQUIRED;
ALTER TABLE PSP_AGENCY
 ADD (R_A_F_ENROLLMENT_REQUIRED  NUMBER(1));

Prompt Table PSP_REPOSITORY_DOCUMENT;
--
-- PSP_REPOSITORY_DOCUMENT  (Table)
--
CREATE TABLE PSP_REPOSITORY_DOCUMENT
(
  REPOSITORY_DOCUMENT_SEQ  VARCHAR2(255 CHAR)   NOT NULL,
  VERSION                  NUMBER(19)           NOT NULL,
  CREATOR_ID               VARCHAR2(30 CHAR),
  CREATED_DATE             TIMESTAMP(6)         NOT NULL,
  MODIFIER_ID              VARCHAR2(30 CHAR),
  MODIFIED_DATE            TIMESTAMP(6)         NOT NULL,
  REALM_ID                 NUMBER(19)           DEFAULT -1                    NOT NULL,
  CLIENT_ID                VARCHAR2(80 CHAR),
  DOC_IMAGE                CLOB,
  DOC_TYPE                 VARCHAR2(80 CHAR),
  MIME_TYPE                VARCHAR2(80 CHAR),
  COMPANY_ID               VARCHAR2(80 CHAR),
  AGENCY_ID                VARCHAR2(80 CHAR)
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

ALTER TABLE PSP_REPOSITORY_DOCUMENT
 ADD PRIMARY KEY
 (REPOSITORY_DOCUMENT_SEQ, REALM_ID);

Prompt Table PSP_RAFENROLLMENT_BATCH;
--
-- PSP_RAFENROLLMENT_BATCH  (Table)
--
CREATE TABLE PSP_RAFENROLLMENT_BATCH
(
  RAFENROLLMENT_BATCH_SEQ  VARCHAR2(255 CHAR)   NOT NULL,
  VERSION                  NUMBER(19)           NOT NULL,
  CREATOR_ID               VARCHAR2(30 CHAR),
  CREATED_DATE             TIMESTAMP(6)         NOT NULL,
  MODIFIER_ID              VARCHAR2(30 CHAR),
  MODIFIED_DATE            TIMESTAMP(6)         NOT NULL,
  REALM_ID                 NUMBER(19)           DEFAULT -1                    NOT NULL,
  BATCH_FILE_PATH          VARCHAR2(1024 CHAR),
  START_TIME               TIMESTAMP(6),
  END_TIME                 TIMESTAMP(6)
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

ALTER TABLE PSP_RAFENROLLMENT_BATCH
 ADD PRIMARY KEY
 (RAFENROLLMENT_BATCH_SEQ, REALM_ID);

Prompt Column STATUS_EFFECTIVE_DATE;
ALTER TABLE PSP_RAFENROLLMENT
 ADD (STATUS_EFFECTIVE_DATE  TIMESTAMP(6));

Prompt Column BATCH_ID;
ALTER TABLE PSP_RAFENROLLMENT DROP COLUMN BATCH_ID;

Prompt Column BATCH_DATE;
ALTER TABLE PSP_RAFENROLLMENT DROP COLUMN BATCH_DATE;

Prompt Column STATUS_EFFECTIVE_DATE;
ALTER TABLE PSP_RAAENROLLMENT
 ADD (STATUS_EFFECTIVE_DATE  TIMESTAMP(6));

Prompt Column FORM;
ALTER TABLE PSP_RAAENROLLMENT
 ADD (FORM  VARCHAR2(255 CHAR));

Prompt Column FORM_FK;
-- Column to be dropped is part of a multi-column constraint.
-- Oracle requires that the constraint be dropped first.
-- There may be another statement later in the script that tries to drop
-- the constraint again.  Errors produced by it can be ignored.
-- ALTER TABLE PSP_RAAENROLLMENT DROP CONSTRAINT PSP_RAAENROLLMENT_FK1;
ALTER TABLE PSP_RAAENROLLMENT DROP COLUMN FORM_FK;

Prompt Table PSP_RAFENROLLMENT_BATCH_ASSOC;
--
-- PSP_RAFENROLLMENT_BATCH_ASSOC  (Table)
--
CREATE TABLE PSP_RAFENROLLMENT_BATCH_ASSOC
(
  R_A_F_ENROLLMENT_FK        VARCHAR2(255 CHAR) NOT NULL,
  R_A_F_ENROLLMENT_BATCH_FK  VARCHAR2(255 CHAR) NOT NULL,
  REALM_ID                   NUMBER(19)         DEFAULT -1                    NOT NULL
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

Prompt Index PSP_RAFENROLLMENT_BATCH_FK2;
--
-- PSP_RAFENROLLMENT_BATCH_FK2  (Index)
--
CREATE INDEX PSP_RAFENROLLMENT_BATCH_FK2 ON PSP_RAFENROLLMENT_BATCH_ASSOC
(R_A_F_ENROLLMENT_BATCH_FK, REALM_ID)
LOGGING
NOPARALLEL;

Prompt Index PSP_RAFENROLLMENT_BATCH_FK1;
--
-- PSP_RAFENROLLMENT_BATCH_FK1  (Index)
--
CREATE INDEX PSP_RAFENROLLMENT_BATCH_FK1 ON PSP_RAFENROLLMENT_BATCH_ASSOC
(R_A_F_ENROLLMENT_FK, REALM_ID)
LOGGING
NOPARALLEL;

ALTER TABLE PSP_RAFENROLLMENT_BATCH_ASSOC
 ADD PRIMARY KEY
 (R_A_F_ENROLLMENT_FK, R_A_F_ENROLLMENT_BATCH_FK, REALM_ID);

Prompt Column EVENTS_GATEWAY_RETRY_COUNT;
ALTER TABLE PSP_COMPANY_EVENT
 ADD (EVENTS_GATEWAY_RETRY_COUNT  NUMBER(10));

Prompt Column EVTGTWY_STATUS_EFF_DT;
ALTER TABLE PSP_COMPANY_EVENT
 ADD (EVTGTWY_STATUS_EFF_DT  TIMESTAMP(6));

Prompt Column EVENTS_GATEWAY_STATUS;
ALTER TABLE PSP_COMPANY_EVENT
 ADD (EVENTS_GATEWAY_STATUS  VARCHAR2(255 CHAR));

Prompt Constraint C_PSP_EVENT_DETAIL_TYPE0;
ALTER TABLE PSP_EVENT_DETAIL_TYPE
 DROP CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0;
ALTER TABLE PSP_EVENT_DETAIL_TYPE
 ADD CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldServiceStatus', 'NewServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'FeeAmount', 'OriginalTransactionDateTime', 'RefundedFeeBillingDetailId', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'BankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount', 'ExpectedToken', 'ReceivedToken', 'Timestamp', 'UniqueIdentifier', 'UserId', 'ServiceCode', 'AgencyId', 'EnrollmentType'));

Prompt Constraint C_PSP_BATCH_JOB_SETUP0;
ALTER TABLE PSP_BATCH_JOB_SETUP
 DROP CONSTRAINT C_PSP_BATCH_JOB_SETUP0;
ALTER TABLE PSP_BATCH_JOB_SETUP
 ADD CONSTRAINT C_PSP_BATCH_JOB_SETUP0
 CHECK (JOB_TYPE IN('AchReturnsMonitor', 'AchTransactionsMonitor', 'As400ToCris', 'BalanceFileMonitor', 'CrisToSourceSystem', 'EmailGateway', 'EmailGatewayMonitor', 'FeeEvents', 'FeeEventsMonitor', 'FraudPayrolls', 'FraudPayrollsMonitor', 'GemsAccountsReceivable', 'GemsAccountsReceivableMonitor', 'GemsGeneralLedger', 'GemsGeneralLedgerMonitor', 'LedgerBalance', 'LedgerBalanceMonitor', 'MissedPayrollsMonitor', 'MissedTransactionsMonitor', 'NightlyBatchJobs', 'NightlyBatchJobsMonitor', 'PrimaryAchOffloadMonitor', 'PrimaryDailyBatchJobs', 'PrimaryDailyBatchJobsMonitor', 'PspToCris', 'ScheduledAchOffloadMonitor', 'ScheduledDailyBatchJobs', 'ScheduledDailyBatchJobsMonitor', 'As400EventSync', 'GemsGeneralLedgerUpload', 'GemsGeneralLedgerUploadMonitor', 'AchOffloadCompleteMonitor', 'EventsGateway', 'RAFWriter'));

ALTER TABLE PSP_AGENCY
 ADD CONSTRAINT C_PSP_AGENCY0
 CHECK (DEFAULT_R_A_A_FORM IN('LPOA', 'Federal8655'));

Prompt Constraint C_PSP_RAAENROLLMENT0;
ALTER TABLE PSP_RAAENROLLMENT
 DROP CONSTRAINT C_PSP_RAAENROLLMENT0;
ALTER TABLE PSP_RAAENROLLMENT
 ADD CONSTRAINT C_PSP_RAAENROLLMENT0
 CHECK (STATUS IN('Incomplete', 'AwaitingSignedForm', 'Received', 'Rejected', 'Complete'));

ALTER TABLE PSP_RAAENROLLMENT
 ADD CONSTRAINT C_PSP_RAAENROLLMENT1
 CHECK (FORM IN('LPOA', 'Federal8655'));

ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT3
 CHECK (EVENTS_GATEWAY_STATUS IN('Pending', 'Posted', 'Alert', 'Notified'));

Prompt Constraint C_PSP_COMPANY_EVENT_DETAIL0;
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 DROP CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0;
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 ADD CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldServiceStatus', 'NewServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'FeeAmount', 'OriginalTransactionDateTime', 'RefundedFeeBillingDetailId', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'BankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount', 'ExpectedToken', 'ReceivedToken', 'Timestamp', 'UniqueIdentifier', 'UserId', 'ServiceCode', 'AgencyId', 'EnrollmentType'));

ALTER TABLE PSP_RAFENROLLMENT_BATCH_ASSOC
 ADD CONSTRAINT PSP_RAFENROLLMENT_BATCH_FK1
 FOREIGN KEY (R_A_F_ENROLLMENT_FK, REALM_ID)
 REFERENCES PSP_RAFENROLLMENT (RAFENROLLMENT_SEQ,REALM_ID);

ALTER TABLE PSP_RAFENROLLMENT_BATCH_ASSOC
 ADD CONSTRAINT PSP_RAFENROLLMENT_BATCH_FK2
 FOREIGN KEY (R_A_F_ENROLLMENT_BATCH_FK, REALM_ID)
 REFERENCES PSP_RAFENROLLMENT_BATCH (RAFENROLLMENT_BATCH_SEQ,REALM_ID);

PROMPT finishedDBUpgrade_002.000.000.026.sql
