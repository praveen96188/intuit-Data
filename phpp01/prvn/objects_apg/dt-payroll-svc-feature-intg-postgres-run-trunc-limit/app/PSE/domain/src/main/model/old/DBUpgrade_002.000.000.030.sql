--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 DROP PRIMARY KEY CASCADE;

ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 DROP PRIMARY KEY CASCADE;

ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 DROP PRIMARY KEY CASCADE;

ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 DROP PRIMARY KEY CASCADE;

ALTER TABLE PSP_COMPANY_AGENCY
 DROP CONSTRAINT PSP_COMPANY_AGENCY_FK3;

ALTER TABLE PSP_BASIC_CHECK_LIST_ITEM
 DROP CONSTRAINT C_PSP_BASIC_CHECK_LIST_ITEM1;

Prompt drop Index PSP_COMPANY_AGENCY_FK3;
DROP INDEX PSP_COMPANY_AGENCY_FK3;

Prompt Column AGENCY_CHECK_LIST_ITEM_SEQ;
ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD (AGENCY_CHECK_LIST_ITEM_SEQ  VARCHAR2(255 CHAR) NOT NULL);

Prompt Column CREATOR_ID;
ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD (CREATOR_ID  VARCHAR2(30 CHAR));

Prompt Column CREATED_DATE;
ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD (CREATED_DATE  TIMESTAMP(6)                    NOT NULL);

Prompt Column MODIFIER_ID;
ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD (MODIFIER_ID  VARCHAR2(30 CHAR));

Prompt Column MODIFIED_DATE;
ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD (MODIFIED_DATE  TIMESTAMP(6)                   NOT NULL);

Prompt Column CHECK_LIST_ITEM_CD;
ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
MODIFY(CHECK_LIST_ITEM_CD  NULL);


Prompt Column ITEM_CLASS_NAME;
ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
MODIFY(ITEM_CLASS_NAME VARCHAR2(80 CHAR));


Prompt Column ITEM_DESCRIPTION;
ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
MODIFY(ITEM_DESCRIPTION VARCHAR2(300 CHAR));


Prompt Column SERVICE_CHECK_LIST_ITEM_SEQ;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD (SERVICE_CHECK_LIST_ITEM_SEQ  VARCHAR2(255 CHAR) NOT NULL);

Prompt Column CREATOR_ID;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD (CREATOR_ID  VARCHAR2(30 CHAR));

Prompt Column CREATED_DATE;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD (CREATED_DATE  TIMESTAMP(6)                    NOT NULL);

Prompt Column MODIFIER_ID;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD (MODIFIER_ID  VARCHAR2(30 CHAR));

Prompt Column MODIFIED_DATE;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD (MODIFIED_DATE  TIMESTAMP(6)                   NOT NULL);

Prompt Column CHECK_LIST_ITEM_CD;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
MODIFY(CHECK_LIST_ITEM_CD  NULL);


Prompt Column AGENCY_CHECK_LIST_STATUS_SEQ;
ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD (AGENCY_CHECK_LIST_STATUS_SEQ  VARCHAR2(255 CHAR) NOT NULL);

Prompt Column CREATOR_ID;
ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD (CREATOR_ID  VARCHAR2(30 CHAR));

Prompt Column CREATED_DATE;
ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD (CREATED_DATE  TIMESTAMP(6)                    NOT NULL);

Prompt Column MODIFIER_ID;
ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD (MODIFIER_ID  VARCHAR2(30 CHAR));

Prompt Column MODIFIED_DATE;
ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD (MODIFIED_DATE  TIMESTAMP(6)                   NOT NULL);

Prompt Column STATUS;
ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
MODIFY(STATUS  NULL);


Prompt Column SERVICE_CHECK_LIST_STATUS_SEQ;
ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD (SERVICE_CHECK_LIST_STATUS_SEQ  VARCHAR2(255 CHAR) NOT NULL);

Prompt Column CREATOR_ID;
ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD (CREATOR_ID  VARCHAR2(30 CHAR));

Prompt Column CREATED_DATE;
ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD (CREATED_DATE  TIMESTAMP(6)                    NOT NULL);

Prompt Column MODIFIER_ID;
ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD (MODIFIER_ID  VARCHAR2(30 CHAR));

Prompt Column MODIFIED_DATE;
ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD (MODIFIED_DATE  TIMESTAMP(6)                   NOT NULL);

Prompt Column STATUS;
ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
MODIFY(STATUS  NULL);


Prompt Column PAYMENT_TEMPLATE_FK;
-- Column to be dropped is part of a multi-column constraint.
-- Oracle requires that the constraint be dropped first.
-- There may be another statement later in the script that tries to drop
-- the constraint again.  Errors produced by it can be ignored.
-- ALTER TABLE PSP_COMPANY_AGENCY DROP CONSTRAINT PSP_COMPANY_AGENCY_FK3;
ALTER TABLE PSP_COMPANY_AGENCY DROP COLUMN PAYMENT_TEMPLATE_FK;

Prompt Column STATUS_REASON;
ALTER TABLE PSP_ACHENROLLMENT
 ADD (STATUS_REASON  VARCHAR2(1000 CHAR));

Prompt Column STATUS_REASON;
ALTER TABLE PSP_RAFENROLLMENT
 ADD (STATUS_REASON  VARCHAR2(1000 CHAR));

Prompt Column STATUS_REASON;
ALTER TABLE PSP_RAAENROLLMENT
 ADD (STATUS_REASON  VARCHAR2(1000 CHAR));

Prompt Table PSP_ACTIVATION_CHECK_LIST_ITEM;
-- Difference : Column Order (No action taken)

Prompt Index PSP_ACTIVATION_CHECK_LIST__FK3;
Prompt drop Index PSP_ACTIVATION_CHECK_LIST__FK3;
DROP INDEX PSP_ACTIVATION_CHECK_LIST__FK3;
Prompt Index PSP_ACTIVATION_CHECK_LIST__FK3;
--
-- PSP_ACTIVATION_CHECK_LIST__FK3  (Index)
--
-- CREATE INDEX PSP_ACTIVATION_CHECK_LIST__FK3 ON PSP_ACTIVATION_CHECK_LIST_ITEM
-- (AGENCY_CHECK_LIST_ITEM_FK, REALM_ID)
-- LOGGING
-- NOPARALLEL;

-- Prompt Index PSP_ACTIVATION_CHECK_LIST__FK2;
-- Prompt drop Index PSP_ACTIVATION_CHECK_LIST__FK2;
-- DROP INDEX PSP_ACTIVATION_CHECK_LIST__FK2;
-- Prompt Index PSP_ACTIVATION_CHECK_LIST__FK2;
--
-- PSP_ACTIVATION_CHECK_LIST__FK2  (Index)
--
-- CREATE INDEX PSP_ACTIVATION_CHECK_LIST__FK2 ON PSP_ACTIVATION_CHECK_LIST_ITEM
-- (SERVICE_CHECK_LIST_ITEM_FK, REALM_ID)
-- LOGGING
-- NOPARALLEL;

Prompt Column STATUS_REASON;
ALTER TABLE PSP_BASIC_CHECK_LIST_ITEM
MODIFY(STATUS_REASON VARCHAR2(1000 CHAR));


Prompt Constraint C_PSP_EVENT_TYPE0;
ALTER TABLE PSP_EVENT_TYPE
 DROP CONSTRAINT C_PSP_EVENT_TYPE0;
ALTER TABLE PSP_EVENT_TYPE
 ADD CONSTRAINT C_PSP_EVENT_TYPE0
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PayrollOffloaded', 'CompanyContactEmailChanged', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'FeeRefunded', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PaycheckCancelled', 'PayrollRecalledAfterOffload', 'PaycheckRecalledAfterOffload', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'EnrollmentStatusChanged', 'StateIdModified', 'ActivationPending', 'AgentAssignedToCheckList', 'ActivationCheckListStatusChanged', 'ActivationCheckListItemStatusChanged', 'PostActivationCheckListStatusChanged'));

Prompt Constraint C_PSP_AUTH_OPERATION0;
ALTER TABLE PSP_AUTH_OPERATION
 DROP CONSTRAINT C_PSP_AUTH_OPERATION0;
ALTER TABLE PSP_AUTH_OPERATION
 ADD CONSTRAINT C_PSP_AUTH_OPERATION0
 CHECK (OPERATION_ID IN('BankReturnUpdate', 'AccessApplication', 'DDLimitUpdate', 'FundingModelUpdate', 'DDStatusUpdate', 'StrikeAdd', 'StrikeCancel', 'DDStatusPendingActivation', 'DDStatusActive', 'DDStatusPendingTermination', 'DDStatusTerminated', 'DDStatusOnHold', 'DDStatusSuspended', 'DDStatusCancelled', 'SettingUpdate', 'ViewFullBankAccountNumbers', 'BankReturnView', 'RecordNonACHRedebitTransaction', 'CreateFeeTransaction', 'CreateReversalTransaction', 'DDTransactionCancel', 'TransactionCancel', 'LedgerView', 'SelectNonStandardSettlementType', 'CreateRefundTransaction', 'VoidTransaction', 'BookTransferTransaction', 'ActivateBankAccount', 'DeActivateBankAccount', 'WriteoffBadDebtTransaction', 'RecoverBadDebtTransaction', 'EscalationCreditTransaction', 'IssueRedebitTransaction', 'AuthAccessApplication', 'AuthAddUpdateUsers', 'AuthRemoveUsers', 'AuthManageRoles', 'AuthAddUpdateHelpDesk', 'AuthAddRemoveHelpDesk', 'AuthAddUpdateDataCustodian', 'AuthRemoveDataCustodian', 'EditCompanyLegalInformation', 'EditCompanyContactInformation', 'ViewTransactionHistory', 'ViewVerificationDebits', 'ResetVerificationAmounts', 'GenerateRandomDebits', 'EditPayrollContact', 'EditPrincipalContacts', 'GeneratePin', 'AddBankAccountRandomDebits', 'AddOffering', 'AddOffer', 'EditChartOfAccounts', 'AddBankAccountByPassRandomDebits', 'RemoveOffer', 'ViewPayrollScreen', 'EnterWireExpectedDate', 'AdjustRedebitTransaction', 'ViewSignupFraudQueue', 'RemoveFromSignupFraudHold', 'RemoveFromPayrollSubmitFraudHold', 'UploadToGems', 'ViewOFX', 'RequestSecondOffload', 'ViewChaseReport', 'PrintChaseReport', 'AgentInitiatesRefundRebill', 'ViewPayrollStatus', 'ViewPendingReversalsScreen', 'EditPayrollAdminName', 'SavePrintOFX', 'ConfirmOffload', 'RefundEmployerFraudEscalation', 'EditTokens', 'EditDebugLogging', 'Activations', 'AssignChecklist', 'ViewACHSummary', 'ViewAdjustments', 'CreateRAFFile', 'PostActivations', 'CreateACHFile', 'ViewOperatorTab'));

Prompt Constraint C_PSP_SOURCE_PAYROLL_PARAM0;
ALTER TABLE PSP_SOURCE_PAYROLL_PARAMETER
 DROP CONSTRAINT C_PSP_SOURCE_PAYROLL_PARAM0;
ALTER TABLE PSP_SOURCE_PAYROLL_PARAMETER
 ADD CONSTRAINT C_PSP_SOURCE_PAYROLL_PARAM0
 CHECK (PARAMETER_CD IN('ACHWaitPeriod', 'CompanyBankAccountDurationLimitForVerification', 'CompanyBankAccountVerificationAttemptLimit', 'ConsecutiveLimitViolationLimit', 'DDCompanyLimitDuration', 'DDEmployeeLimitDuration', 'DefaultDDCompanyLimit', 'DefaultDDEmployeeLimit', 'MaxDDCompanyLimitDefault', 'MinimumNonSuspectPayrollAmount', 'PayrollEntryDescription', 'MinPayrollRunsForLimitAutoIncrease', 'MinQBVersionSupported', 'BookTransferEntryDescription', 'ReversalEntryDescription', 'MaxNumberOfFailedLoginAttempts', 'FraudEEPaidMax', 'FraudEEPaidMaxXPayrolls', 'FraudEERoundPaidXPayrolls', 'FraudPRMax', 'FraudPRMaxXPayrolls', 'FraudEEPercentIncreaseMax', 'AllowMultipleFundingModels', 'MaxWarehouseTransactionDays', 'DefaultFundingModel', 'LockAccountDuration', 'ShouldAddCompanyToPSP', 'AllowReverifyBankAccount', 'MinimumEarliestPayrollRunDays', 'DeactiveBankAccountOnReturnedVerificationDebit', 'UnsupportedQBVersionList', 'FraudEEPercentIncreaseMaxXPayrolls', 'ResolveEmployeeNOC', 'AllowDuplicatePaycheckIdsIfStatusIsCancelled', 'AutomaticCompanyBankAccountVerification', 'FraudPRPercentIncreaseMax', 'FraudPRPercentIncreaseMaxXPayrolls', 'QBVersionSunsetString'));

Prompt Constraint C_PSP_AGENCY_CHECK_LIST_IT3;
ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 DROP CONSTRAINT C_PSP_AGENCY_CHECK_LIST_IT3;
ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_AGENCY_CHECK_LIST_IT3
 CHECK (DEFAULT_STATUS IN('Downloaded', 'Cancelled', 'AgedOut', 'Received', 'Complete', 'Incomplete', 'Rejected', 'AgencyRejected', 'InProgress', 'AwaitingSignedForm', 'AwaitingReview', 'ReadyForRAF', 'ReadyForTape'));

ALTER TABLE PSP_AGENCY_CHECK_LIST_ITEM
 ADD PRIMARY KEY
 (AGENCY_CHECK_LIST_ITEM_SEQ, REALM_ID);

Prompt Constraint C_PSP_SERVICE_CHECK_LIST_I0;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 DROP CONSTRAINT C_PSP_SERVICE_CHECK_LIST_I0;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_SERVICE_CHECK_LIST_I0
 CHECK (CATEGORY IN('Activation', 'PostActivation'));

Prompt Constraint C_PSP_SERVICE_CHECK_LIST_I1;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 DROP CONSTRAINT C_PSP_SERVICE_CHECK_LIST_I1;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_SERVICE_CHECK_LIST_I1
 CHECK (CHECK_LIST_ITEM_CD IN('EFTPSEnrollment', 'EmployeeInfo', 'HistoricalPayrollData', 'Activated', 'ACHEnrollment', 'StateIDReceived', 'UnemploymentRate', 'BankAccountVerify', 'RAAEnrollment8655', 'RAFEnrollment', 'TaxAudit', 'RAAEnrollmentLPOA', 'ContactInformation', 'LegalAndTaxInformation'));

Prompt Constraint C_PSP_SERVICE_CHECK_LIST_I2;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 DROP CONSTRAINT C_PSP_SERVICE_CHECK_LIST_I2;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_SERVICE_CHECK_LIST_I2
 CHECK (CHECK_LIST_ITEM_GROUP IN('RAAForms', 'CaliforniaInfo'));

Prompt Constraint C_PSP_SERVICE_CHECK_LIST_I3;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 DROP CONSTRAINT C_PSP_SERVICE_CHECK_LIST_I3;
ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_SERVICE_CHECK_LIST_I3
 CHECK (DEFAULT_STATUS IN('Downloaded', 'Cancelled', 'AgedOut', 'Received', 'Complete', 'Incomplete', 'Rejected', 'AgencyRejected', 'InProgress', 'AwaitingSignedForm', 'AwaitingReview', 'ReadyForRAF', 'ReadyForTape'));

ALTER TABLE PSP_SERVICE_CHECK_LIST_ITEM
 ADD PRIMARY KEY
 (SERVICE_CHECK_LIST_ITEM_SEQ, REALM_ID);

Prompt Constraint C_PSP_AGENCY_CHECK_LIST_ST0;
ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 DROP CONSTRAINT C_PSP_AGENCY_CHECK_LIST_ST0;
ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD CONSTRAINT C_PSP_AGENCY_CHECK_LIST_ST0
 CHECK (STATUS IN('Downloaded', 'Cancelled', 'AgedOut', 'Received', 'Complete', 'Incomplete', 'Rejected', 'AgencyRejected', 'InProgress', 'AwaitingSignedForm', 'AwaitingReview', 'ReadyForRAF', 'ReadyForTape'));

Prompt Constraint C_PSP_AGENCY_CHECK_LIST_ST1;
ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 DROP CONSTRAINT C_PSP_AGENCY_CHECK_LIST_ST1;
ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD CONSTRAINT C_PSP_AGENCY_CHECK_LIST_ST1
 CHECK (STATUS_ALERT_LEVEL IN('Error', 'Warning', 'None'));

ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD PRIMARY KEY
 (AGENCY_CHECK_LIST_STATUS_SEQ, REALM_ID);

Prompt Constraint PSP_AGENCY_CHECK_LIST_APPL_FK1;
-- ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
-- DROP CONSTRAINT PSP_AGENCY_CHECK_LIST_APPL_FK1;
ALTER TABLE PSP_AGENCY_CHECK_LIST_STATUS
 ADD CONSTRAINT PSP_AGENCY_CHECK_LIST_APPL_FK1
 FOREIGN KEY (AGENCY_CHECK_LIST_ITEM_FK, REALM_ID)
 REFERENCES PSP_AGENCY_CHECK_LIST_ITEM (AGENCY_CHECK_LIST_ITEM_SEQ,REALM_ID);

Prompt Constraint C_PSP_SERVICE_CHECK_LIST_S0;
ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 DROP CONSTRAINT C_PSP_SERVICE_CHECK_LIST_S0;
ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD CONSTRAINT C_PSP_SERVICE_CHECK_LIST_S0
 CHECK (STATUS IN('Downloaded', 'Cancelled', 'AgedOut', 'Received', 'Complete', 'Incomplete', 'Rejected', 'AgencyRejected', 'InProgress', 'AwaitingSignedForm', 'AwaitingReview', 'ReadyForRAF', 'ReadyForTape'));

ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD PRIMARY KEY
 (SERVICE_CHECK_LIST_STATUS_SEQ, REALM_ID);

Prompt Constraint PSP_SERVICE_CHECK_LIST_APP_FK1;
-- ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
-- DROP CONSTRAINT PSP_SERVICE_CHECK_LIST_APP_FK1;
ALTER TABLE PSP_SERVICE_CHECK_LIST_STATUS
 ADD CONSTRAINT PSP_SERVICE_CHECK_LIST_APP_FK1
 FOREIGN KEY (SERVICE_CHECK_LIST_ITEM_FK, REALM_ID)
 REFERENCES PSP_SERVICE_CHECK_LIST_ITEM (SERVICE_CHECK_LIST_ITEM_SEQ,REALM_ID);

Prompt Constraint C_PSP_COMPANY_EVENT1;
ALTER TABLE PSP_COMPANY_EVENT
 DROP CONSTRAINT C_PSP_COMPANY_EVENT1;
ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT1
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PayrollOffloaded', 'CompanyContactEmailChanged', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'FeeRefunded', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PaycheckCancelled', 'PayrollRecalledAfterOffload', 'PaycheckRecalledAfterOffload', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'EnrollmentStatusChanged', 'StateIdModified', 'ActivationPending', 'AgentAssignedToCheckList', 'ActivationCheckListStatusChanged', 'ActivationCheckListItemStatusChanged', 'PostActivationCheckListStatusChanged'));

Prompt Constraint C_PSP_COMPANY_EVENT3;
ALTER TABLE PSP_COMPANY_EVENT
 DROP CONSTRAINT C_PSP_COMPANY_EVENT3;
ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT3
 CHECK (EVENTS_GATEWAY_STATUS IN('Pending', 'Posted', 'Alert'));

Prompt Constraint C_PSP_RAFENROLLMENT0;
ALTER TABLE PSP_RAFENROLLMENT
 DROP CONSTRAINT C_PSP_RAFENROLLMENT0;
ALTER TABLE PSP_RAFENROLLMENT
 ADD CONSTRAINT C_PSP_RAFENROLLMENT0
 CHECK (STATUS IN('ReadyForTape', 'InProgress', 'Complete', 'AgencyRejected', 'Incomplete', 'Rejected', 'ReadyForRAF'));

Prompt Constraint C_PSP_RAAENROLLMENT0;
ALTER TABLE PSP_RAAENROLLMENT
 DROP CONSTRAINT C_PSP_RAAENROLLMENT0;
ALTER TABLE PSP_RAAENROLLMENT
 ADD CONSTRAINT C_PSP_RAAENROLLMENT0
 CHECK (STATUS IN('Incomplete', 'Received', 'Rejected', 'Complete'));

Prompt Constraint PSP_ACTIVATION_CHECK_LIST__FK2;
-- ALTER TABLE PSP_ACTIVATION_CHECK_LIST_ITEM
-- DROP CONSTRAINT PSP_ACTIVATION_CHECK_LIST__FK2;
ALTER TABLE PSP_ACTIVATION_CHECK_LIST_ITEM
 ADD CONSTRAINT PSP_ACTIVATION_CHECK_LIST__FK2
 FOREIGN KEY (SERVICE_CHECK_LIST_ITEM_FK, REALM_ID)
 REFERENCES PSP_SERVICE_CHECK_LIST_ITEM (SERVICE_CHECK_LIST_ITEM_SEQ,REALM_ID);

Prompt Constraint PSP_ACTIVATION_CHECK_LIST__FK3;
-- ALTER TABLE PSP_ACTIVATION_CHECK_LIST_ITEM
-- DROP CONSTRAINT PSP_ACTIVATION_CHECK_LIST__FK3;
ALTER TABLE PSP_ACTIVATION_CHECK_LIST_ITEM
 ADD CONSTRAINT PSP_ACTIVATION_CHECK_LIST__FK3
 FOREIGN KEY (AGENCY_CHECK_LIST_ITEM_FK, REALM_ID)
 REFERENCES PSP_AGENCY_CHECK_LIST_ITEM (AGENCY_CHECK_LIST_ITEM_SEQ,REALM_ID);

Prompt Constraint C_PSP_BASIC_CHECK_LIST_ITEM0;
ALTER TABLE PSP_BASIC_CHECK_LIST_ITEM
 DROP CONSTRAINT C_PSP_BASIC_CHECK_LIST_ITEM0;
ALTER TABLE PSP_BASIC_CHECK_LIST_ITEM
 ADD CONSTRAINT C_PSP_BASIC_CHECK_LIST_ITEM0
 CHECK (STATUS IN('Downloaded', 'Cancelled', 'AgedOut', 'Received', 'Complete', 'Incomplete', 'Rejected', 'AgencyRejected', 'InProgress', 'AwaitingSignedForm', 'AwaitingReview', 'ReadyForRAF', 'ReadyForTape'));

 
PROMPT finishedDBUpgrade_002.000.000.030.sql
