--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 
PROMPT started DBUpgrade_002.020.003.001.sql

Prompt Column SESSION_ID;
ALTER TABLE PSP_BANK_ACCOUNT
 ADD (SESSION_ID  VARCHAR2(30 CHAR));

Prompt Column O_I_I_FLAG;
ALTER TABLE PSP_COMPANY
 ADD (O_I_I_FLAG  VARCHAR2(32 CHAR));

Prompt Column SESSION_ID;
ALTER TABLE PSP_COMPANY_BANK_ACCOUNT
 ADD (SESSION_ID  VARCHAR2(30 CHAR));

Prompt Column RETRY_COUNT;
ALTER TABLE PSP_ENTITY_UPDATE
MODIFY(RETRY_COUNT NUMBER(10));


Prompt Column SESSION_ID;
ALTER TABLE PSP_PAYEE_BANK_ACCOUNT
 ADD (SESSION_ID  VARCHAR2(30 CHAR));

Prompt Column SESSION_ID;
ALTER TABLE PSP_EMPLOYEE_BANK_ACCOUNT
 ADD (SESSION_ID  VARCHAR2(30 CHAR));

Prompt Column BILLING_REALM_ID;
ALTER TABLE PSP_ENTITLEMENT
 ADD (BILLING_REALM_ID  VARCHAR2(30 CHAR));

Prompt Column O_I_I_BILLING_FLAG;
ALTER TABLE PSP_ENTITLEMENT
 ADD (O_I_I_BILLING_FLAG  VARCHAR2(32 CHAR));

Prompt Column SESSION_ID;
ALTER TABLE PSP_BILL_PAYMENT
 ADD (SESSION_ID  VARCHAR2(30 CHAR));

Prompt Column TRANSACTION_NUMBER;
ALTER TABLE PSP_MONEY_MOVEMENT_TRANSACTION
 ADD (TRANSACTION_NUMBER  VARCHAR2(30 CHAR));

Prompt Column SESSION_ID;
ALTER TABLE PSP_PAYCHECK
 ADD (SESSION_ID  VARCHAR2(30 CHAR));

Prompt Constraint C_PSP_EVENT_TYPE0;
ALTER TABLE PSP_EVENT_TYPE
 DROP CONSTRAINT C_PSP_EVENT_TYPE0;

ALTER TABLE PSP_EVENT_TYPE
 ADD CONSTRAINT C_PSP_EVENT_TYPE0
  CHECK (EVENT_TYPE_CD IN('FileIdChanged', 'SendEmailFailed', 'PaycheckRecalledAfterOffload', 'SendEmailSkipped', 'InvalidVendorEmail', 'ERLoanNOC', 'NewPSIDCreatedForExistingCustomer', 'PrimaryPrincipalNameChanged', 'PayeeBankAccountChange', 'BackdatePriorToProcessingStart', 'SubscriptionEndDateChanged', 'SUICreditsApplied', 'TrialAssetDetected', 'WelcomeEmail', 'AccountLocked', 'ACHReturnStatusChanged', 'EINChanged', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BackdatedPayrollReceived', 'ACHReturn', 'FeeCreated', 'IncorrectPIN', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollRejected', 'CompanyContactEmailChanged', 'TaxExemptStatusChanged', 'CustomerSignedUp', 'PayrollCancelPending', 'FeeRefunded', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'DBANameChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'CompanyIndustryTypeChanged', 'PrimaryPrincipalSSNChanged', 'PrimaryPrincipalDOBChanged', 'EnrollmentStatusChanged', 'StateIdModified', 'TaxPaymentStatusChanged', 'PaymentMethodChanged'
, 'DepositFrequencyChanged', 'ThresholdExceeded', 'AssistedEmployeeMigrationComplete', 'OfferingUpdated', 'OfferClaimed', 'OfferRemoved', 'EmployeeInTermedCompany', 'EmployeeBankAccountInTermedCompany', 'PrefundingReceived', 'EmployeePaidTooManyTimes', 'EmployeePaidPercentageGreaterThanOthers', 'EmployeesPaidToSameBank', 'EmployeeBankAccountChangedSpikeInPay', 'EmployeesPaidToSameBankAccount', 'InvalidEmployeeInformation', 'PINUnlocked', 'Employee401kDataUploaded', 'EmployeeBankAccountChange', 'TotalBillPaymentExceedsLimit', 'PayeePaidGreaterThanMax', 'PayeePaidTooManyTimes', 'BillPaymentReceived', 'InvalidPaycheckInformation', 'VoidedPaycheckAlreadyOffloadedToTOK', 'DeletedPaycheckAlreadyOffloadedToTOK', 'TOKNotifiedOfCompanyFraud', 'BillPaymentOffloaded', 'PreOffload401kValidationAlert', 'BillPaymentRecalled', 'NonPrintChecks', 'PayrollReceivedCloud', 'PostOffload401kValidationAlert', 'InvalidSourceSystemTransmissionInformation', 'CloudResponse', 'InactivityDDPayrollAmountExceeded', 'InactivityBPPayrollAmountExceeded', 'AssistedPayrollItemMigrationComplete', 'ERPayableRefundCreated', 'MultipleCompanyLawsCreated', 'BalanceFileReceived', 'CompanyLawUpdated', 'PayrollReceivedPayCard', 'LiabilityAdjustmentCreated', 'PSPToAS400HoldSync', 'PSPToAS400HoldRemoveSync', 'OFXServiceActivated', 'PositiveCobraReceived', 'ManualLedgerEntry', 'PayrollTaxPaymentVoided', 'PayrollTaxPaymentReissued', 'ERPayableAppliedToBalanceDue', 'AIDUpdated', 'AssistedFailedEnrollment', 'AssistedPayrollConfirmation', 'EntitlementStateChanged', 'EntitlementUnitStatusChanged', 'SourceCompanyIdChanged', 'ServiceKeyUpdated', 'EntitlementCodeChanged', 'PriceTypeChanged', 'EntitlementCommunication', 'ManualDataSync', 'AccountingFinancialLedgerAdjustmentCreated', 'SUIEoqDebitCreated', 'SUIEoqCreditCreated', 'SUIImmediateDebitCreated', 'SUIImmediateCreditCreated', 'CompanyContactAdded', 'CompanyContactJobTitleChanged', 'PayrollSubmissionIncludedAllNewEmployees', 'PSIDMismatch', 'CompanyContactDeleted', 'CompanyContactNameChanged', 'CompanyContactFaxChanged', 'ERPenaltiesAndInterestRefundCreated', 'ERPenaltiesAndInterestRefundDebitCreated', 'EntitlementUnitAdded', 'UsageBilling25DaysIntoSubscription', 'UsageBilling15DaysIntoSubscription', 'CreditReduction', 'RequestProcessingFlagChanged'
, 'PayrollSubmittedWithEmployeeWithPendingReturn', 'DuplicatePayrollItemReceived', 'VmpSignUpEmployeeEmail', 'VmpSignUpEmployerEmail', 'ACHEnrollmentStatusChanged', 'PaystubCreated', 'AdditionalFilingAmount', 'BPIncreasePayrollLimit', 'AllowTransmissionsFlagChanged', 'MonthlyFeeCreated', 'EmployeeAdded', 'EmployeeDeleted', 'EmployeeUpdated', 'PayeeAdded', 'PayeeUpdated', 'DDMigration', 'SplitMMTReturn', 'RealmIdAdded', 'RealmIdUpdated')) 
ENABLE NOVALIDATE;

Prompt Constraint C_PSP_ENTITY_UPDATE0;
ALTER TABLE PSP_ENTITY_UPDATE
 DROP CONSTRAINT C_PSP_ENTITY_UPDATE0;

ALTER TABLE PSP_ENTITY_UPDATE
 ADD CONSTRAINT C_PSP_ENTITY_UPDATE0
  CHECK (STATUS IN('Created', 'Published', 'Failed', 'IQFailed', 'IQPublished')) 
  ENABLE NOVALIDATE;

Prompt Constraint C_PSP_COMPANY_EVENT1;
ALTER TABLE PSP_COMPANY_EVENT
 DROP CONSTRAINT C_PSP_COMPANY_EVENT1;

ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT1
  CHECK (EVENT_TYPE_CD IN('FileIdChanged', 'SendEmailFailed', 'PaycheckRecalledAfterOffload', 'SendEmailSkipped', 'InvalidVendorEmail', 'ERLoanNOC', 'NewPSIDCreatedForExistingCustomer', 'PrimaryPrincipalNameChanged', 'PayeeBankAccountChange', 'BackdatePriorToProcessingStart', 'SubscriptionEndDateChanged', 'SUICreditsApplied', 'TrialAssetDetected', 'WelcomeEmail', 'AccountLocked', 'ACHReturnStatusChanged', 'EINChanged', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BackdatedPayrollReceived', 'ACHReturn', 'FeeCreated', 'IncorrectPIN', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollRejected', 'CompanyContactEmailChanged', 'TaxExemptStatusChanged', 'CustomerSignedUp', 'PayrollCancelPending', 'FeeRefunded', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'DBANameChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'CompanyIndustryTypeChanged', 'PrimaryPrincipalSSNChanged', 'PrimaryPrincipalDOBChanged', 'EnrollmentStatusChanged', 'StateIdModified', 'TaxPaymentStatusChanged', 'PaymentMethodChanged'
, 'DepositFrequencyChanged', 'ThresholdExceeded', 'AssistedEmployeeMigrationComplete', 'OfferingUpdated', 'OfferClaimed', 'OfferRemoved', 'EmployeeInTermedCompany', 'EmployeeBankAccountInTermedCompany', 'PrefundingReceived', 'EmployeePaidTooManyTimes', 'EmployeePaidPercentageGreaterThanOthers', 'EmployeesPaidToSameBank', 'EmployeeBankAccountChangedSpikeInPay', 'EmployeesPaidToSameBankAccount', 'InvalidEmployeeInformation', 'PINUnlocked', 'Employee401kDataUploaded', 'EmployeeBankAccountChange', 'TotalBillPaymentExceedsLimit', 'PayeePaidGreaterThanMax', 'PayeePaidTooManyTimes', 'BillPaymentReceived', 'InvalidPaycheckInformation', 'VoidedPaycheckAlreadyOffloadedToTOK', 'DeletedPaycheckAlreadyOffloadedToTOK', 'TOKNotifiedOfCompanyFraud', 'BillPaymentOffloaded', 'PreOffload401kValidationAlert', 'BillPaymentRecalled', 'NonPrintChecks', 'PayrollReceivedCloud', 'PostOffload401kValidationAlert', 'InvalidSourceSystemTransmissionInformation', 'CloudResponse', 'InactivityDDPayrollAmountExceeded', 'InactivityBPPayrollAmountExceeded', 'AssistedPayrollItemMigrationComplete', 'ERPayableRefundCreated', 'MultipleCompanyLawsCreated', 'BalanceFileReceived', 'CompanyLawUpdated', 'PayrollReceivedPayCard', 'LiabilityAdjustmentCreated', 'PSPToAS400HoldSync', 'PSPToAS400HoldRemoveSync', 'OFXServiceActivated', 'PositiveCobraReceived', 'ManualLedgerEntry', 'PayrollTaxPaymentVoided', 'PayrollTaxPaymentReissued', 'ERPayableAppliedToBalanceDue', 'AIDUpdated', 'AssistedFailedEnrollment', 'AssistedPayrollConfirmation', 'EntitlementStateChanged', 'EntitlementUnitStatusChanged', 'SourceCompanyIdChanged', 'ServiceKeyUpdated', 'EntitlementCodeChanged', 'PriceTypeChanged', 'EntitlementCommunication', 'ManualDataSync', 'AccountingFinancialLedgerAdjustmentCreated', 'SUIEoqDebitCreated', 'SUIEoqCreditCreated', 'SUIImmediateDebitCreated', 'SUIImmediateCreditCreated', 'CompanyContactAdded', 'CompanyContactJobTitleChanged', 'PayrollSubmissionIncludedAllNewEmployees', 'PSIDMismatch', 'CompanyContactDeleted', 'CompanyContactNameChanged', 'CompanyContactFaxChanged', 'ERPenaltiesAndInterestRefundCreated', 'ERPenaltiesAndInterestRefundDebitCreated', 'EntitlementUnitAdded', 'UsageBilling25DaysIntoSubscription', 'UsageBilling15DaysIntoSubscription', 'CreditReduction', 'RequestProcessingFlagChanged'
, 'PayrollSubmittedWithEmployeeWithPendingReturn', 'DuplicatePayrollItemReceived', 'VmpSignUpEmployeeEmail', 'VmpSignUpEmployerEmail', 'ACHEnrollmentStatusChanged', 'PaystubCreated', 'AdditionalFilingAmount', 'BPIncreasePayrollLimit', 'AllowTransmissionsFlagChanged', 'MonthlyFeeCreated', 'EmployeeAdded', 'EmployeeDeleted', 'EmployeeUpdated', 'PayeeAdded', 'PayeeUpdated', 'DDMigration', 'SplitMMTReturn', 'RealmIdAdded', 'RealmIdUpdated')) 
ENABLE NOVALIDATE;

Prompt Constraint C_PSP_COMPANY_EVENT_EMAIL_0;
ALTER TABLE PSP_COMPANY_EVENT_EMAIL_PARAM
 DROP CONSTRAINT C_PSP_COMPANY_EVENT_EMAIL_0;

ALTER TABLE PSP_COMPANY_EVENT_EMAIL_PARAM
 ADD CONSTRAINT C_PSP_COMPANY_EVENT_EMAIL_0
  CHECK (PARAM_TYPE_CD IN('VendorInvalidEmailAddress', 'SUICreditAmount', 'PaymentAmount', 'ServiceType', 'ExtensionKey', 'AdjustedPayrollDebitAmount', 'BilledFeeList', 'CompanyBalanceDue', 'CompanyBankAccountLastFour', 'CompanyID', 'CompanyLegalName', 'CurrentEmail', 'EffectiveCreditPostingDate', 'EmailFirstName', 'EmailLastName', 'EmployeeFirstName', 'EmployeeLastNameFirstInitial', 'EmployeeList', 'FailureReason', 'IntuitHandlingFee', 'NextBusinessDate', 'NonPayrollFeeAmount', 'NonPayrollFeeSettlementDate', 'NonPayrollFeeType', 'NumberOfStrikes', 'PayrollRunTime', 'PrimaryPrincipalEmail', 'PrimaryPrincipalFirstName', 'PrimaryPrincipalLastName', 'PriorEmail', 'RedebitCompletedDate', 'RedebitSettlementDate', 'RefundedFeeList', 'ReversalFailedList', 'ReversalPendingList', 'ReversalSuccessfulList', 'SourcePayrollSystem', 'TodaysDate', 'TodaysDatePlus14CalendarDays', 'WireExpectedDate', 'PaycheckSettlementDate', 'PayrollAdminEmail', 'PayrollAdminFirstName', 'PayrollAdminLastName', 'PayrollCancelDate', 'PayrollDebitAmount', 'PayrollDebitSettlementDate', 'PayrollRunDate', 'CompanyDBAName', 'BillingContactName', 'CompanyEIN', 'PayPeriodBeginDate', 'PayPeriodEndDate', 'EmployeeLastName', 'Four01kTransmissionDate', 'VoidOrDelete', 'HoldReason', 'ReferenceNumber', 'VendorPaymentList', 'Memo', 'VendorAccountNumber', 'VendorBankAccountLastFour', 'ServiceKey', 'LicenseNumber', 'AgreementNumber', 'LawId', 'Quarter', 'Year', 'DebitSettlementDate', 'Amount', 'CustomerAccountNnumber', 'SubTypeDescription', 'SubscriptionStartDate', 'RecipientEmail', 'RecipientFirstName', 'RecipientLastName', 'PayeeList', 'SalesTaxAmount', 'TransactionNumber')) 
  ENABLE NOVALIDATE;

Prompt Constraint C_PSP_FRAUD_EVENT1;
ALTER TABLE PSP_FRAUD_EVENT
 DROP CONSTRAINT C_PSP_FRAUD_EVENT1;

ALTER TABLE PSP_FRAUD_EVENT
 ADD CONSTRAINT C_PSP_FRAUD_EVENT1
  CHECK (EVENT_TYPE_CD IN('FileIdChanged', 'SendEmailFailed', 'PaycheckRecalledAfterOffload', 'SendEmailSkipped', 'InvalidVendorEmail', 'ERLoanNOC', 'NewPSIDCreatedForExistingCustomer', 'PrimaryPrincipalNameChanged', 'PayeeBankAccountChange', 'BackdatePriorToProcessingStart', 'SubscriptionEndDateChanged', 'SUICreditsApplied', 'TrialAssetDetected', 'WelcomeEmail', 'AccountLocked', 'ACHReturnStatusChanged', 'EINChanged', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BackdatedPayrollReceived', 'ACHReturn', 'FeeCreated', 'IncorrectPIN', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollRejected', 'CompanyContactEmailChanged', 'TaxExemptStatusChanged', 'CustomerSignedUp', 'PayrollCancelPending', 'FeeRefunded', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'DBANameChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'CompanyIndustryTypeChanged', 'PrimaryPrincipalSSNChanged', 'PrimaryPrincipalDOBChanged', 'EnrollmentStatusChanged', 'StateIdModified', 'TaxPaymentStatusChanged', 'PaymentMethodChanged'
, 'DepositFrequencyChanged', 'ThresholdExceeded', 'AssistedEmployeeMigrationComplete', 'OfferingUpdated', 'OfferClaimed', 'OfferRemoved', 'EmployeeInTermedCompany', 'EmployeeBankAccountInTermedCompany', 'PrefundingReceived', 'EmployeePaidTooManyTimes', 'EmployeePaidPercentageGreaterThanOthers', 'EmployeesPaidToSameBank', 'EmployeeBankAccountChangedSpikeInPay', 'EmployeesPaidToSameBankAccount', 'InvalidEmployeeInformation', 'PINUnlocked', 'Employee401kDataUploaded', 'EmployeeBankAccountChange', 'TotalBillPaymentExceedsLimit', 'PayeePaidGreaterThanMax', 'PayeePaidTooManyTimes', 'BillPaymentReceived', 'InvalidPaycheckInformation', 'VoidedPaycheckAlreadyOffloadedToTOK', 'DeletedPaycheckAlreadyOffloadedToTOK', 'TOKNotifiedOfCompanyFraud', 'BillPaymentOffloaded', 'PreOffload401kValidationAlert', 'BillPaymentRecalled', 'NonPrintChecks', 'PayrollReceivedCloud', 'PostOffload401kValidationAlert', 'InvalidSourceSystemTransmissionInformation', 'CloudResponse', 'InactivityDDPayrollAmountExceeded', 'InactivityBPPayrollAmountExceeded', 'AssistedPayrollItemMigrationComplete', 'ERPayableRefundCreated', 'MultipleCompanyLawsCreated', 'BalanceFileReceived', 'CompanyLawUpdated', 'PayrollReceivedPayCard', 'LiabilityAdjustmentCreated', 'PSPToAS400HoldSync', 'PSPToAS400HoldRemoveSync', 'OFXServiceActivated', 'PositiveCobraReceived', 'ManualLedgerEntry', 'PayrollTaxPaymentVoided', 'PayrollTaxPaymentReissued', 'ERPayableAppliedToBalanceDue', 'AIDUpdated', 'AssistedFailedEnrollment', 'AssistedPayrollConfirmation', 'EntitlementStateChanged', 'EntitlementUnitStatusChanged', 'SourceCompanyIdChanged', 'ServiceKeyUpdated', 'EntitlementCodeChanged', 'PriceTypeChanged', 'EntitlementCommunication', 'ManualDataSync', 'AccountingFinancialLedgerAdjustmentCreated', 'SUIEoqDebitCreated', 'SUIEoqCreditCreated', 'SUIImmediateDebitCreated', 'SUIImmediateCreditCreated', 'CompanyContactAdded', 'CompanyContactJobTitleChanged', 'PayrollSubmissionIncludedAllNewEmployees', 'PSIDMismatch', 'CompanyContactDeleted', 'CompanyContactNameChanged', 'CompanyContactFaxChanged', 'ERPenaltiesAndInterestRefundCreated', 'ERPenaltiesAndInterestRefundDebitCreated', 'EntitlementUnitAdded', 'UsageBilling25DaysIntoSubscription', 'UsageBilling15DaysIntoSubscription', 'CreditReduction', 'RequestProcessingFlagChanged'
, 'PayrollSubmittedWithEmployeeWithPendingReturn', 'DuplicatePayrollItemReceived', 'VmpSignUpEmployeeEmail', 'VmpSignUpEmployerEmail', 'ACHEnrollmentStatusChanged', 'PaystubCreated', 'AdditionalFilingAmount', 'BPIncreasePayrollLimit', 'AllowTransmissionsFlagChanged', 'MonthlyFeeCreated', 'EmployeeAdded', 'EmployeeDeleted', 'EmployeeUpdated', 'PayeeAdded', 'PayeeUpdated', 'DDMigration', 'SplitMMTReturn', 'RealmIdAdded', 'RealmIdUpdated')) 
ENABLE NOVALIDATE;


PROMPT finished DBUpgrade_002.020.003.001.sql