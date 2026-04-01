--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
ALTER TABLE PSP_COMPANY_AGENCY
 DROP CONSTRAINT PSP_COMPANY_AGENCY_FK4;

ALTER TABLE PSP_COMPANY_AGENCY
 DROP CONSTRAINT PSP_COMPANY_AGENCY_FK5;

Prompt drop Index PSP_COMPANY_AGENCY_FK4;
DROP INDEX PSP_COMPANY_AGENCY_FK4;

--Prompt drop Index PSP_COMPANY_AGENCY_FK5;
-- DROP INDEX PSP_COMPANY_AGENCY_FK5;

Prompt Table PSP_SERVICE_CHECK_LIST_ITEM;
--
-- PSP_SERVICE_CHECK_LIST_ITEM  (Table)
--
CREATE TABLE PSP_SERVICE_CHECK_LIST_ITEM
(
  CHECK_LIST_ITEM_CD     VARCHAR2(255 CHAR)     NOT NULL,
  VERSION                NUMBER(19)             NOT NULL,
  REALM_ID               NUMBER(19)             DEFAULT -1                    NOT NULL,
  ITEM_DESCRIPTION       VARCHAR2(4000 CHAR),
  CHECK_LIST_ITEM_GROUP  VARCHAR2(255 CHAR),
  CATEGORY               VARCHAR2(255 CHAR),
  DEFAULT_STATUS         VARCHAR2(255 CHAR),
  ITEM_ORDER             NUMBER(10),
  SERVICE_FK             VARCHAR2(255 CHAR)     NOT NULL
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

Prompt Index PSP_SERVICE_APPLICABLE_CHE_FK1;
--
-- PSP_SERVICE_APPLICABLE_CHE_FK1  (Index)
--
CREATE INDEX PSP_SERVICE_APPLICABLE_CHE_FK1 ON PSP_SERVICE_CHECK_LIST_ITEM
(SERVICE_FK, REALM_ID)
LOGGING
NOPARALLEL;

ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_SERVICE_CHECK_LIST_I0
 CHECK (CHECK_LIST_ITEM_GROUP IN('RAAForms'));

ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_SERVICE_CHECK_LIST_I1
 CHECK (CATEGORY IN('Activation', 'PostActivation'));

ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_SERVICE_CHECK_LIST_I2
 CHECK (DEFAULT_STATUS IN('Complete', 'Incomplete', 'Rejected', 'InProgress', 'AwaitingSignedForm', 'AwaitingReview', 'ReadyForRAF', 'ReadyForTape'));

ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_SERVICE_CHECK_LIST_I3
 CHECK (CHECK_LIST_ITEM_CD IN('EFTPSEnrollment', 'EmployeeInfo', 'HistoricalPayrollData', 'Activated', 'ACHEnrollment', 'StateIDReceived', 'UnemploymentRate', 'BankAccountVerify', 'RAAEnrollment8655', 'RAFEnrollment', 'TaxAudit', 'RAAEnrollmentLPOA', 'HPDE'));

ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD PRIMARY KEY
 (CHECK_LIST_ITEM_CD, REALM_ID);

Prompt Table PSP_SERVICE_CHECK_LIST_STATUS;
--
-- PSP_SERVICE_CHECK_LIST_STATUS  (Table)
--
CREATE TABLE PSP_SERVICE_CHECK_LIST_STATUS
(
  STATUS                      VARCHAR2(255 CHAR) NOT NULL,
  VERSION                     NUMBER(19)        NOT NULL,
  REALM_ID                    NUMBER(19)        DEFAULT -1                    NOT NULL,
  STATUS_ALERT_LEVEL          VARCHAR2(255 CHAR),
  UPDATE_TYPE                 VARCHAR2(255 CHAR),
  SERVICE_CHECK_LIST_ITEM_FK  VARCHAR2(255 CHAR) NOT NULL
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

Prompt Index PSP_SERVICE_CHECK_LIST_APP_FK1;
--
-- PSP_SERVICE_CHECK_LIST_APP_FK1  (Index)
--
CREATE INDEX PSP_SERVICE_CHECK_LIST_APP_FK1 ON PSP_SERVICE_CHECK_LIST_STATUS
(SERVICE_CHECK_LIST_ITEM_FK, REALM_ID)
LOGGING
NOPARALLEL;

ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD CONSTRAINT C_PSP_SERVICE_CHECK_LIST_S0
 CHECK (STATUS IN('Complete', 'Incomplete', 'Rejected', 'InProgress', 'AwaitingSignedForm', 'AwaitingReview', 'ReadyForRAF', 'ReadyForTape'));

ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD CONSTRAINT C_PSP_SERVICE_CHECK_LIST_S1
 CHECK (STATUS_ALERT_LEVEL IN('Error', 'Warning', 'None'));

ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD CONSTRAINT C_PSP_SERVICE_CHECK_LIST_S2
 CHECK (UPDATE_TYPE IN('System', 'Agent', 'AgentOrSystem'));

ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD PRIMARY KEY
 (STATUS, REALM_ID);

Prompt Table PSP_AGENCY_CHECK_LIST_ITEM;
--
-- PSP_AGENCY_CHECK_LIST_ITEM  (Table)
--
CREATE TABLE PSP_AGENCY_CHECK_LIST_ITEM
(
  CHECK_LIST_ITEM_CD     VARCHAR2(255 CHAR)     NOT NULL,
  VERSION                NUMBER(19)             NOT NULL,
  REALM_ID               NUMBER(19)             DEFAULT -1                    NOT NULL,
  CATEGORY               VARCHAR2(255 CHAR),
  ITEM_DESCRIPTION       VARCHAR2(100 CHAR),
  CHECK_LIST_ITEM_GROUP  VARCHAR2(255 CHAR),
  DEFAULT_STATUS         VARCHAR2(255 CHAR),
  ITEM_CLASS_NAME        VARCHAR2(150 CHAR),
  ITEM_ORDER             NUMBER(10),
  AGENCY_FK              VARCHAR2(255 CHAR)     NOT NULL
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

Prompt Index PSP_AGENCY_APPLICABLE_CHEC_FK1;
--
-- PSP_AGENCY_APPLICABLE_CHEC_FK1  (Index)
--
CREATE INDEX PSP_AGENCY_APPLICABLE_CHEC_FK1 ON PSP_AGENCY_CHECK_LIST_ITEM
(AGENCY_FK, REALM_ID)
LOGGING
NOPARALLEL;

ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_AGENCY_CHECK_LIST_IT0
 CHECK (CATEGORY IN('Activation', 'PostActivation'));

ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_AGENCY_CHECK_LIST_IT1
 CHECK (CHECK_LIST_ITEM_CD IN('EFTPSEnrollment', 'EmployeeInfo', 'HistoricalPayrollData', 'Activated', 'ACHEnrollment', 'StateIDReceived', 'UnemploymentRate', 'BankAccountVerify', 'RAAEnrollment8655', 'RAFEnrollment', 'TaxAudit', 'RAAEnrollmentLPOA', 'HPDE'));

ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_AGENCY_CHECK_LIST_IT2
 CHECK (CHECK_LIST_ITEM_GROUP IN('RAAForms'));

ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_AGENCY_CHECK_LIST_IT3
 CHECK (DEFAULT_STATUS IN('Complete', 'Incomplete', 'Rejected', 'InProgress', 'AwaitingSignedForm', 'AwaitingReview', 'ReadyForRAF', 'ReadyForTape'));

ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD PRIMARY KEY
 (CHECK_LIST_ITEM_CD, REALM_ID);

Prompt Column R_A_F_ENROLLMENT_FK;
-- Column to be dropped is part of a multi-column constraint.
-- Oracle requires that the constraint be dropped first.
-- There may be another statement later in the script that tries to drop
-- the constraint again.  Errors produced by it can be ignored.
ALTER TABLE PSP_COMPANY_AGENCY DROP CONSTRAINT PSP_COMPANY_AGENCY_FK3;
ALTER TABLE PSP_COMPANY_AGENCY DROP COLUMN R_A_F_ENROLLMENT_FK;

Prompt Column R_A_A_ENROLLMENT_FK;
-- Column to be dropped is part of a multi-column constraint.
-- Oracle requires that the constraint be dropped first.
-- There may be another statement later in the script that tries to drop
-- the constraint again.  Errors produced by it can be ignored.
-- ALTER TABLE PSP_COMPANY_AGENCY DROP CONSTRAINT PSP_COMPANY_AGENCY_FK4;
ALTER TABLE PSP_COMPANY_AGENCY DROP COLUMN R_A_A_ENROLLMENT_FK;

Prompt Table PSP_ACTIVATION_CHECK_LIST;
--
-- PSP_ACTIVATION_CHECK_LIST  (Table)
--
CREATE TABLE PSP_ACTIVATION_CHECK_LIST
(
  ACTIVATION_CHECK_LIST_SEQ    VARCHAR2(255 CHAR) NOT NULL,
  VERSION                      NUMBER(19)       NOT NULL,
  CREATOR_ID                   VARCHAR2(30 CHAR),
  CREATED_DATE                 TIMESTAMP(6)     NOT NULL,
  MODIFIER_ID                  VARCHAR2(30 CHAR),
  MODIFIED_DATE                TIMESTAMP(6)     NOT NULL,
  REALM_ID                     NUMBER(19)       DEFAULT -1                    NOT NULL,
  START_DATE                   TIMESTAMP(6),
  END_DATE                     TIMESTAMP(6),
  POST_ACTIVATION_STATUS_DATE  TIMESTAMP(6),
  ACTIVATION_STATUS_DATE       TIMESTAMP(6),
  ACTIVATION_STATUS            VARCHAR2(255 CHAR),
  POST_ACTIVATION_STATUS       VARCHAR2(255 CHAR),
  AUTH_USER_FK                 VARCHAR2(255 CHAR),
  COMPANY_FK                   VARCHAR2(255 CHAR) NOT NULL
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

Prompt Index PSP_ACTIVATION_CHECK_LIST_FK2;
--
-- PSP_ACTIVATION_CHECK_LIST_FK2  (Index)
--
CREATE INDEX PSP_ACTIVATION_CHECK_LIST_FK2 ON PSP_ACTIVATION_CHECK_LIST
(COMPANY_FK, REALM_ID)
LOGGING
NOPARALLEL;

Prompt Index PSP_ACTIVATION_CHECK_LIST_FK1;
--
-- PSP_ACTIVATION_CHECK_LIST_FK1  (Index)
--
CREATE INDEX PSP_ACTIVATION_CHECK_LIST_FK1 ON PSP_ACTIVATION_CHECK_LIST
(AUTH_USER_FK, REALM_ID)
LOGGING
NOPARALLEL;

ALTER TABLE PSP_ACTIVATION_CHECK_LIST
 ADD CONSTRAINT C_PSP_ACTIVATION_CHECK_LIST0
 CHECK (ACTIVATION_STATUS IN('Incomplete', 'Complete', 'InProgress'));

ALTER TABLE PSP_ACTIVATION_CHECK_LIST
 ADD CONSTRAINT C_PSP_ACTIVATION_CHECK_LIST1
 CHECK (POST_ACTIVATION_STATUS IN('Incomplete', 'Complete', 'InProgress'));

ALTER TABLE PSP_ACTIVATION_CHECK_LIST
 ADD PRIMARY KEY
 (ACTIVATION_CHECK_LIST_SEQ, REALM_ID);

Prompt Index PSP_COMPANY_AGENCY_FK3;
Prompt drop Index PSP_COMPANY_AGENCY_FK3;
-- DROP INDEX PSP_COMPANY_AGENCY_FK3;
Prompt Index PSP_COMPANY_AGENCY_FK3;
--
-- PSP_COMPANY_AGENCY_FK3  (Index)
--
CREATE INDEX PSP_COMPANY_AGENCY_FK3 ON PSP_COMPANY_AGENCY
(PAYMENT_TEMPLATE_FK, REALM_ID)
LOGGING
NOPARALLEL;

Prompt Table PSP_AGENCY_CHECK_LIST_STATUS;
--
-- PSP_AGENCY_CHECK_LIST_STATUS  (Table)
--
CREATE TABLE PSP_AGENCY_CHECK_LIST_STATUS
(
  STATUS                     VARCHAR2(255 CHAR) NOT NULL,
  VERSION                    NUMBER(19)         NOT NULL,
  REALM_ID                   NUMBER(19)         DEFAULT -1                    NOT NULL,
  STATUS_ALERT_LEVEL         VARCHAR2(255 CHAR),
  UPDATE_TYPE                VARCHAR2(255 CHAR),
  AGENCY_CHECK_LIST_ITEM_FK  VARCHAR2(255 CHAR) NOT NULL
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

Prompt Index PSP_AGENCY_CHECK_LIST_APPL_FK1;
--
-- PSP_AGENCY_CHECK_LIST_APPL_FK1  (Index)
--
CREATE INDEX PSP_AGENCY_CHECK_LIST_APPL_FK1 ON PSP_AGENCY_CHECK_LIST_STATUS
(AGENCY_CHECK_LIST_ITEM_FK, REALM_ID)
LOGGING
NOPARALLEL;

ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD CONSTRAINT C_PSP_AGENCY_CHECK_LIST_ST0
 CHECK (STATUS_ALERT_LEVEL IN('Error', 'Warning', 'None'));

ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD CONSTRAINT C_PSP_AGENCY_CHECK_LIST_ST1
 CHECK (STATUS IN('Complete', 'Incomplete', 'Rejected', 'InProgress', 'AwaitingSignedForm', 'AwaitingReview', 'ReadyForRAF', 'ReadyForTape'));

ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD CONSTRAINT C_PSP_AGENCY_CHECK_LIST_ST2
 CHECK (UPDATE_TYPE IN('System', 'Agent', 'AgentOrSystem'));

ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD PRIMARY KEY
 (STATUS, REALM_ID);

Prompt Column COMPANY_AGENCY_FK;
ALTER TABLE PSP_RAFENROLLMENT
 ADD (COMPANY_AGENCY_FK  VARCHAR2(255 CHAR));

Prompt Column COMPANY_AGENCY_FK;
ALTER TABLE PSP_RAAENROLLMENT
 ADD (COMPANY_AGENCY_FK  VARCHAR2(255 CHAR));

Prompt Table PSP_ACTIVATION_CHECK_LIST_ITEM;
--
-- PSP_ACTIVATION_CHECK_LIST_ITEM  (Table)
--
CREATE TABLE PSP_ACTIVATION_CHECK_LIST_ITEM
(
  ACTIVATION_CHECK_LIST_ITEM_SEQ  VARCHAR2(255 CHAR) NOT NULL,
  VERSION                         NUMBER(19)    NOT NULL,
  CREATOR_ID                      VARCHAR2(30 CHAR),
  CREATED_DATE                    TIMESTAMP(6)  NOT NULL,
  MODIFIER_ID                     VARCHAR2(30 CHAR),
  MODIFIED_DATE                   TIMESTAMP(6)  NOT NULL,
  REALM_ID                        NUMBER(19)    DEFAULT -1                    NOT NULL,
  CHECK_LIST_ITEM                 VARCHAR2(255 CHAR),
  ACTIVATION_CHECK_LIST_FK        VARCHAR2(255 CHAR) NOT NULL,
  AGENCY_CHECK_LIST_ITEM_FK       VARCHAR2(255 CHAR),
  SERVICE_CHECK_LIST_ITEM_FK      VARCHAR2(255 CHAR)
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

Prompt Index PSP_ACTIVATION_CHECK_LIST__FK3;
--
-- PSP_ACTIVATION_CHECK_LIST__FK3  (Index)
--
CREATE INDEX PSP_ACTIVATION_CHECK_LIST__FK3 ON PSP_ACTIVATION_CHECK_LIST_ITEM
(SERVICE_CHECK_LIST_ITEM_FK, REALM_ID)
LOGGING
NOPARALLEL;

Prompt Index PSP_ACTIVATION_CHECK_LIST__FK2;
--
-- PSP_ACTIVATION_CHECK_LIST__FK2  (Index)
--
CREATE INDEX PSP_ACTIVATION_CHECK_LIST__FK2 ON PSP_ACTIVATION_CHECK_LIST_ITEM
(AGENCY_CHECK_LIST_ITEM_FK, REALM_ID)
LOGGING
NOPARALLEL;

Prompt Index PSP_ACTIVATION_CHECK_LIST__FK1;
--
-- PSP_ACTIVATION_CHECK_LIST__FK1  (Index)
--
CREATE INDEX PSP_ACTIVATION_CHECK_LIST__FK1 ON PSP_ACTIVATION_CHECK_LIST_ITEM
(ACTIVATION_CHECK_LIST_FK, REALM_ID)
LOGGING
NOPARALLEL;

ALTER TABLE PSP_ACTIVATION_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_ACTIVATION_CHECK_LIS0
 CHECK (CHECK_LIST_ITEM IN('EFTPSEnrollment', 'EmployeeInfo', 'HistoricalPayrollData', 'Activated', 'ACHEnrollment', 'StateIDReceived', 'UnemploymentRate', 'BankAccountVerify', 'RAAEnrollment8655', 'RAFEnrollment', 'TaxAudit', 'RAAEnrollmentLPOA', 'HPDE'));

ALTER TABLE PSP_ACTIVATION_CHECK_LIST_ITEM
 ADD PRIMARY KEY
 (ACTIVATION_CHECK_LIST_ITEM_SEQ, REALM_ID);

Prompt Index PSP_RAFENROLLMENT_FK1;
--
-- PSP_RAFENROLLMENT_FK1  (Index)
--
CREATE INDEX PSP_RAFENROLLMENT_FK1 ON PSP_RAFENROLLMENT
(COMPANY_AGENCY_FK, REALM_ID)
LOGGING
NOPARALLEL;

Prompt Index PSP_RAAENROLLMENT_FK1;
--
-- PSP_RAAENROLLMENT_FK1  (Index)
--
CREATE INDEX PSP_RAAENROLLMENT_FK1 ON PSP_RAAENROLLMENT
(COMPANY_AGENCY_FK, REALM_ID)
LOGGING
NOPARALLEL;

Prompt Table PSP_ENROLLMENT_CHECK_LIST_ITEM;
--
-- PSP_ENROLLMENT_CHECK_LIST_ITEM  (Table)
--
CREATE TABLE PSP_ENROLLMENT_CHECK_LIST_ITEM
(
  ENROLLMENT_CHECK_LIST_ITEM_SEQ  VARCHAR2(255 CHAR) NOT NULL,
  REALM_ID                        NUMBER(19)    DEFAULT -1                    NOT NULL
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

ALTER TABLE PSP_ENROLLMENT_CHECK_LIST_ITEM
 ADD PRIMARY KEY
 (ENROLLMENT_CHECK_LIST_ITEM_SEQ, REALM_ID);

Prompt Table PSP_BASIC_CHECK_LIST_ITEM;
--
-- PSP_BASIC_CHECK_LIST_ITEM  (Table)
--
CREATE TABLE PSP_BASIC_CHECK_LIST_ITEM
(
  BASIC_CHECK_LIST_ITEM_SEQ  VARCHAR2(255 CHAR) NOT NULL,
  REALM_ID                   NUMBER(19)         DEFAULT -1                    NOT NULL,
  STATUS_DATE                TIMESTAMP(6),
  STATUS_REASON              VARCHAR2(255 CHAR),
  STATUS                     VARCHAR2(255 CHAR)
)
LOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
MONITORING;

ALTER TABLE PSP_BASIC_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_BASIC_CHECK_LIST_ITEM0
 CHECK (STATUS_REASON IN('NameMismatch', 'AgeOut', 'ErrorCodeMismatch', 'EINMismatch', 'AgentRejected', 'AgencyRejected'));

ALTER TABLE PSP_BASIC_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_BASIC_CHECK_LIST_ITEM1
 CHECK (STATUS IN('Complete', 'Incomplete', 'Rejected', 'InProgress', 'AwaitingSignedForm', 'AwaitingReview', 'ReadyForRAF', 'ReadyForTape'));

ALTER TABLE PSP_BASIC_CHECK_LIST_ITEM
 ADD PRIMARY KEY
 (BASIC_CHECK_LIST_ITEM_SEQ, REALM_ID);

Prompt Constraint C_PSP_EVENT_TYPE0;
ALTER TABLE PSP_EVENT_TYPE
 DROP CONSTRAINT C_PSP_EVENT_TYPE0;
ALTER TABLE PSP_EVENT_TYPE
 ADD CONSTRAINT C_PSP_EVENT_TYPE0
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PayrollOffloaded', 'CompanyContactEmailChanged', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'FeeRefunded', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PaycheckCancelled', 'PayrollRecalledAfterOffload', 'PaycheckRecalledAfterOffload', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'EnrollmentStatusChanged', 'ActivationCheckListStatusChanged', 'ActivationCheckListItemStatusChanged', 'PostActivationCheckListStatusChanged'));

Prompt Constraint C_PSP_EVENT_DETAIL_TYPE0;
ALTER TABLE PSP_EVENT_DETAIL_TYPE
 DROP CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0;
ALTER TABLE PSP_EVENT_DETAIL_TYPE
 ADD CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldServiceStatus', 'NewServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'FeeAmount', 'OriginalTransactionDateTime', 'RefundedFeeBillingDetailId', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'BankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount', 'ExpectedToken', 'ReceivedToken', 'Timestamp', 'UniqueIdentifier', 'UserId', 'ServiceCode', 'AgencyId', 'EnrollmentType', 'CheckListItemType'));

Prompt Constraint C_PSP_COMPANY_EVENT1;
ALTER TABLE PSP_COMPANY_EVENT
 DROP CONSTRAINT C_PSP_COMPANY_EVENT1;
ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT1
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PayrollOffloaded', 'CompanyContactEmailChanged', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'FeeRefunded', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PaycheckCancelled', 'PayrollRecalledAfterOffload', 'PaycheckRecalledAfterOffload', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'EnrollmentStatusChanged', 'ActivationCheckListStatusChanged', 'ActivationCheckListItemStatusChanged', 'PostActivationCheckListStatusChanged'));

Prompt Constraint PSP_COMPANY_AGENCY_FK3;
-- ALTER TABLE PSP_COMPANY_AGENCY DROP CONSTRAINT PSP_COMPANY_AGENCY_FK3;
ALTER TABLE PSP_COMPANY_AGENCY
 ADD CONSTRAINT PSP_COMPANY_AGENCY_FK3
 FOREIGN KEY (PAYMENT_TEMPLATE_FK, REALM_ID)
 REFERENCES PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD,REALM_ID);

Prompt Constraint C_PSP_COMPANY_EVENT_DETAIL0;
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 DROP CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0;
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 ADD CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldServiceStatus', 'NewServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'FeeAmount', 'OriginalTransactionDateTime', 'RefundedFeeBillingDetailId', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'BankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount', 'ExpectedToken', 'ReceivedToken', 'Timestamp', 'UniqueIdentifier', 'UserId', 'ServiceCode', 'AgencyId', 'EnrollmentType', 'CheckListItemType'));

ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD CONSTRAINT PSP_SERVICE_APPLICABLE_CHE_FK1
 FOREIGN KEY (SERVICE_FK, REALM_ID)
 REFERENCES PSP_SERVICE (SERVICE_CD,REALM_ID);

ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD CONSTRAINT PSP_SERVICE_CHECK_LIST_APP_FK1
 FOREIGN KEY (SERVICE_CHECK_LIST_ITEM_FK, REALM_ID)
 REFERENCES PSP_SERVICE_CHECK_LIST_ITEM (CHECK_LIST_ITEM_CD,REALM_ID);

ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD CONSTRAINT PSP_AGENCY_APPLICABLE_CHEC_FK1
 FOREIGN KEY (AGENCY_FK, REALM_ID)
 REFERENCES PSP_AGENCY (AGENCY_ID,REALM_ID);

ALTER TABLE PSP_ACTIVATION_CHECK_LIST
 ADD CONSTRAINT PSP_ACTIVATION_CHECK_LIST_FK1
 FOREIGN KEY (AUTH_USER_FK, REALM_ID)
 REFERENCES PSP_AUTH_USER (AUTH_USER_SEQ,REALM_ID);

ALTER TABLE PSP_ACTIVATION_CHECK_LIST
 ADD CONSTRAINT PSP_ACTIVATION_CHECK_LIST_FK2
 FOREIGN KEY (COMPANY_FK, REALM_ID)
 REFERENCES PSP_COMPANY (COMPANY_SEQ,REALM_ID);

ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD CONSTRAINT PSP_AGENCY_CHECK_LIST_APPL_FK1
 FOREIGN KEY (AGENCY_CHECK_LIST_ITEM_FK, REALM_ID)
 REFERENCES PSP_AGENCY_CHECK_LIST_ITEM (CHECK_LIST_ITEM_CD,REALM_ID);

ALTER TABLE PSP_ACTIVATION_CHECK_LIST_ITEM
 ADD CONSTRAINT PSP_ACTIVATION_CHECK_LIST__FK1
 FOREIGN KEY (ACTIVATION_CHECK_LIST_FK, REALM_ID)
 REFERENCES PSP_ACTIVATION_CHECK_LIST (ACTIVATION_CHECK_LIST_SEQ,REALM_ID);

ALTER TABLE PSP_ACTIVATION_CHECK_LIST_ITEM
 ADD CONSTRAINT PSP_ACTIVATION_CHECK_LIST__FK2
 FOREIGN KEY (AGENCY_CHECK_LIST_ITEM_FK, REALM_ID)
 REFERENCES PSP_AGENCY_CHECK_LIST_ITEM (CHECK_LIST_ITEM_CD,REALM_ID);

ALTER TABLE PSP_ACTIVATION_CHECK_LIST_ITEM
 ADD CONSTRAINT PSP_ACTIVATION_CHECK_LIST__FK3
 FOREIGN KEY (SERVICE_CHECK_LIST_ITEM_FK, REALM_ID)
 REFERENCES PSP_SERVICE_CHECK_LIST_ITEM (CHECK_LIST_ITEM_CD,REALM_ID);

ALTER TABLE PSP_ENROLLMENT_CHECK_LIST_ITEM
 ADD CONSTRAINT PSP_ENROLLMENT_CHECK_LIST__FK1
 FOREIGN KEY (ENROLLMENT_CHECK_LIST_ITEM_SEQ, REALM_ID)
 REFERENCES PSP_ACTIVATION_CHECK_LIST_ITEM (ACTIVATION_CHECK_LIST_ITEM_SEQ,REALM_ID);

ALTER TABLE PSP_BASIC_CHECK_LIST_ITEM
 ADD CONSTRAINT PSP_BASIC_CHECK_LIST_ITEM_FK1
 FOREIGN KEY (BASIC_CHECK_LIST_ITEM_SEQ, REALM_ID)
 REFERENCES PSP_ACTIVATION_CHECK_LIST_ITEM (ACTIVATION_CHECK_LIST_ITEM_SEQ,REALM_ID);

ALTER TABLE PSP_RAFENROLLMENT
 ADD CONSTRAINT PSP_RAFENROLLMENT_FK1
 FOREIGN KEY (COMPANY_AGENCY_FK, REALM_ID)
 REFERENCES PSP_COMPANY_AGENCY (COMPANY_AGENCY_SEQ,REALM_ID);

ALTER TABLE PSP_RAAENROLLMENT
 ADD CONSTRAINT PSP_RAAENROLLMENT_FK1
 FOREIGN KEY (COMPANY_AGENCY_FK, REALM_ID)
 REFERENCES PSP_COMPANY_AGENCY (COMPANY_AGENCY_SEQ,REALM_ID);

 
PROMPT finishedDBUpgrade_002.000.000.027.sql