DECLARE
	
	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_EVENT_TYPE';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_EVENT_TYPE" CASCADE CONSTRAINTS';
	END IF;

	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_EVENT_DETAIL_TYPE';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_EVENT_DETAIL_TYPE" CASCADE CONSTRAINTS';
	END IF;

    SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_EVTDTLTP_EVTTP_ASSOC';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_EVTDTLTP_EVTTP_ASSOC" CASCADE CONSTRAINTS';
	END IF;

    SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_EVTTP_SRCSYS_ASSOC';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_EVTTP_SRCSYS_ASSOC" CASCADE CONSTRAINTS';
	END IF;
END;

/


--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_EVENT_TYPE
(
	"EVENT_TYPE_CD" VARCHAR2(255 CHAR) NOT NULL,
	"VERSION" NUMBER(19,0) NOT NULL, 
	"DESCRIPTION" VARCHAR2(4000 CHAR),  
	"NAME" VARCHAR2(4000 CHAR),
	"EVENT_GROUP_CD" VARCHAR2(255 CHAR) NOT NULL
)
/

CREATE TABLE TEMP_EVENT_DETAIL_TYPE
(
  EVENT_DETAIL_TYPE_CD     VARCHAR2(255 CHAR)   NOT NULL,
  VERSION                  NUMBER(19)           NOT NULL,
  DESCRIPTION              VARCHAR2(160 CHAR),
  NAME                     VARCHAR2(80 CHAR),
  VALUE_CLASS_NAME         VARCHAR2(4000 CHAR)
)
/

CREATE TABLE TEMP_EVTDTLTP_EVTTP_ASSOC
(
  EVENT_DETAIL_TYPE_FK  VARCHAR2(255 CHAR)      NOT NULL,
  EVENT_TYPE_FK         VARCHAR2(255 CHAR)      NOT NULL
)
/

CREATE TABLE TEMP_EVTTP_SRCSYS_ASSOC
(
  INTERESTING_EVENT_TYPES_FK  VARCHAR2(255 CHAR) NOT NULL,
  SOURCE_SYSTEM_FK            VARCHAR2(255 CHAR) NOT NULL
)
/
--------------------------------------------------------
-- TEMP_EVENT_TYPE                                    --
--7/7/13 DWeinberg added [Historical] to description of events that PSP cannot currently create but do exist in prod
--------------------------------------------------------
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'AccountLocked', 0, 'Account Locked', 'Account locked due to failed login attempts', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ACHReturn', 0, 'ACH Return', 'ACH transaction returned.', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ACHReturnStatusChanged', 0, 'ACH Return Status Changed', 'ACH return status changed from {OldPayrollStatus} to {NewPayrollStatus} for a {PayrollRunId}', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'AS400Event', 0, 'AS400 Event', 'An AS400 {AS400EventName} event. [Historical]', 'NonPSP')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'AssistedFailedEnrollment', 0, 'Assisted Failed Enrollment', 'Assisted Payroll Enrollment Failure', 'NonPSP')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'AssistedPayrollConfirmation', 0, 'Assisted Payroll Confirmation', 'Assisted Payroll Confirmation', 'NonPSP')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'AssistedEmployeeMigrationComplete', 0, 'Assisted Employee Migration Complete', 'Assisted Employee Migration Complete [Historical]', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'AssistedPayrollItemMigrationComplete', 0, 'Assisted Payroll Item Migration Complete', 'Assisted Payroll Item Migration Complete [Historical]', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'AuthenticationFailed', 0, 'Authentication Failed', 'Authentication process failed: {FailureReason}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'BackdatedPayrollReceived', 0, 'Backdated Payroll Received', 'Backdated {SourcePayrollRunId} received', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'BankAccountVerified', 0, 'Bank Account Verified', '<a href="event:goBanks">Bank account</a> successfully verified', 'Bank')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'BillPaymentOffloaded', 0, 'Vendor Payment Offloaded', 'A {PayrollRunId,,vendor payment} offloaded', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'BillPaymentRecalled', 0, 'Vendor Payment Recalled', 'A vendor payment was recalled', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'BillPaymentReceived', 0, 'Vendor Payment Received', 'A {SourcePayrollRunId,,vendor payment} was received', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CBAVerifyReturn', 0, 'Company Bank Account Verify Return', 'Bank account verification {FinancialTransactionId} returned ({ACHEventCd})', 'Bank')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ChangeRedebitToWireExpected', 0, 'Change Redebit To Wire Expected', 'Redebit ({CollectionStage}) changed from ACH to non-ACH (Wire Expected {WireExpectedDate,date}) on a {PayrollRunId}', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CoaFeeAccountChange', 0, 'COA Fee Account Change', 'COA fee account changed from "{OldCoaName}" to "{NewCoaName}"', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CoaSalesTaxAccountChange', 0, 'COA Sales Tax Account Change', 'COA sales tax account changed from "{OldCoaName}" to "{NewCoaName}"', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyBankAccountChange', 0, 'Company Bank Account Changed', 'Company <a href="event:goBanks">bank account</a> successfully changed', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyBankAccountStatusChange', 0, 'Company Bank Account Status Change', '<a href="event:goBanks">Bank account</a> status changed from {OldBAStatus} to {NewBAStatus}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyBankAccountVBDStatusChange', 0, 'Company Bank Account VBD Status Change', '<a href="event:goBanks">Bank account</a> VBD status changed from {OldBAStatus} to {NewBAStatus}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SMSToPSPSyncFailure', 0, 'SMS sync failure', 'SMS sync from SMS to PSP has failed : {ReasonDescription} ', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SMSToPSPSyncSuccess', 0, 'SMS sync Success', 'SMS sync from SMS to PSP has succeeded ', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyContactAddressChanged', 0, 'Company Contact Address Changed', 'A Contact''s address changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyContactEmailChanged', 0, 'Company Contact Email Changed', 'A contact''s E-Mail changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyContactPhoneChanged', 0, 'Company Contact Phone Changed', 'A contact''s phone number changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyContactRoleChanged', 0, 'Company Contact Role Changed', 'A contact''s role changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/

INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyContactAdded', 0, 'Company Contact Added', 'A new company contact Added : {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyContactNameChanged', 0, 'Company Contact Name Changed', 'A contact''s name changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyContactJobTitleChanged', 0, 'Company Contact Job title Changed', 'A contact''s Job title changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyContactFaxChanged', 0, 'Company Contact Fax Changed', 'A contact''s Fax number changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyContactDeleted', 0, 'Company Contact Deleted', 'Company Contact Deleted : {OldStringValue}', 'CompanyInfo')
/

INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyFundingModelChanged', 0, 'CompanyFunding Model Changed', 'Funding model changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyLawUpdated', 0, 'Company Laws Changed', 'Company Laws updated with new rates.', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CompanyMatchesFraudulentCompany', 0, 'Company Matches Fraudulent Company', 'This company has matched a fradulent company', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CurrentPayrollPercentageIncrease', 0, 'Current Payroll Percentage Increase', 'A {PayrollRunId} percentage increase: {Details}', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CustomerSignedUp', 0, 'Customer Signed Up', 'Signed up for service', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EntitlementUnitAdded', 0, 'Entitlement Unit Added', 'Entitlement Unit Added', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'UsageBilling25DaysIntoSubscription', 0, 'UsageBilling 25 Days Into Subscription', 'UsageBilling 25 Days Into Subscription', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'UsageBilling15DaysIntoSubscription', 0, 'UsageBilling 15 Days Into Subscription', 'UsageBilling 15 Days Into Subscription', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'DBANameChanged', 0, 'DBA Name Changed', 'DBA name changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'DDDebitReturn', 0, 'DD Debit Return', 'DD Debit {FinancialTransactionId} returned ({ACHEventCd})', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
  'BPIncreasePayrollLimit', 0, 'Bill Payment Increase Payroll Limit', '{LimitType} Bill Payment Limit increased from {OldLimitAmount} to {NewLimitAmount} for a {SourcePayrollRunId}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'DDIncreasePayrollLimit', 0, 'Increase Payroll Limit', '{LimitType} Direct Deposit Limit increased from {OldLimitAmount} to {NewLimitAmount} for a {SourcePayrollRunId}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'DDReject', 0, 'DD Reject', 'DD {FinancialTransactionId} rejected ({ACHEventCd})', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'DeletedPaycheckAlreadyOffloadedToTOK', 0, 'Deleted Paycheck Already Offloaded To TOK', 'Deleted Paycheck Already Offloaded To TOK', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'DepositFrequencyChanged', 0, 'Deposit Frequency Changed', 'Deposit frequency changed', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EINChanged', 0, 'EIN Changed', 'EIN changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EmailAddressChanged', 0, 'Email Address Changed', 'A contact''s E-Mail address changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EmployeeBankAccountChangedSpikeInPay', 0, 'Employee Bank Account Changed Spike In Pay', 'An Employee Bank Account has changed and employee had a spike in pay', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
 'EmployeeBankAccountInTermedCompany', 0, 'Employee Bank Account In Termed Company', 'Employee bank account exists in terminated or fraud company', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
 'EmployeeInTermedCompany', 0, 'Employee Exists in Terminated Company', 'Employee exists in terminated company', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EmployeePaidEvenDollarAmount', 0, 'Employee Paid Even Dollar Amount', 'An employee paid an even dollar amount on a {PayrollRunId}', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EmployeePaidGreaterThanMax', 0, 'Employee Paid Greater Than Max', 'An employee paid greater than the max limit on a {PayrollRunId}', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EmployeePaidPercentageGreaterThanOthers', 0, 'Employee Paid Greater than a Percentage compared to other Employees', 'Employee paid greater than a precentage compared to other employees', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EmployeePaidTooManyTimes', 0, 'Employee Paid Too Many Times in a Short Period', 'Employee paid too many times in a short period', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EmployeesPaidToSameBank', 0, 'High Percentage of Employees Paid to Same Bank', 'High Percentage of Employees Paid to Same Bank', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EmployeesPaidToSameBankAccount', 0, 'Employees paid to same bank account', 'Too many employees paid to same bank account', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EnrollmentStatusChanged', 0, 'Enrollment Status Changed', 'Enrollment status changed', 'PSP')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ERRefundReturn', 0, 'ER Refund Return', 'Employer refund {FinancialTransactionId} returned ({ACHEventCd})', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'FeeCreated', 0, 'Fee Created', 'A {FinancialTransactionId,,fee} has been created', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'FeeOffloaded', 0, 'Fee Offloaded', 'A {FinancialTransactionId,,fee} has been offloaded', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'FeeRebilled', 0, 'Fee Rebilled', 'A {FinancialTransactionId,,fee} has been rebilled', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'FeeRefunded', 0, 'Fee Refunded', 'A {FinancialTransactionId,,fee} has been refunded', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'FeeReturn', 0, 'Fee Return', 'A {FinancialTransactionId,,fee} has been returned ({ACHReturnReasonCode})', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'FirstPayrollReceived', 0, 'First Payroll Received', 'First {SourcePayrollRunId} received', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'FraudFlagRemovedEvent', 0, 'Fraud Flag Removed Event', 'Fraud flag removed', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'HigherTokenSynced', 0, 'Higher Token Synced', 'The token sycned had a higher value ({ReceivedToken}) than expected ({ExpectedToken}): {ReasonDescription} [Historical]', 'PSP')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'IncorrectPIN', 0, 'Incorrrect PIN', 'An incorrect PIN was entered on a login attempt', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'InvalidEmployeeInformation', 0, 'Invalid Employee Information', 'Invalid employee information was submitted', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'InvalidPaycheckInformation', 0, 'Invalid Paycheck Information', 'Invalid paycheck information was submitted', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'InvalidVendorEmail', 0, 'Invalid Vendor Email', 'Invalid vendor email was submitted', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD  ) VALUES (
'KeyPairGenerated', 0, 'Key Pair Generated', 'A key pair for a digital signature created', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'LastChanceNotify', 0, 'Last Chance Notify', 'An email has been generated giving 24 hours for a balance to be wired for a {PayrollRunId}', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'LegalAddressChanged', 0, 'Legal Address Changed', 'Legal address changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ComplianceAddressChanged', 0, 'Compliance Address Changed', 'Compliance address changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'LegalNameChanged', 0, 'Legal Name Changed', 'Legal name changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'LiabilityAdjustmentCreated', 0, 'Liability Adjustment Created', 'A Liability Adjustment has been created', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'LimitViolation', 0, 'Payroll Limit Violation', 'The {LimitType} limit of {LimitAmount} was exceeded with {ViolationAmount} on a {SourcePayrollRunId}', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ManualNoteEvent', 0, 'Manual Note Created', 'Manual note created', 'NonPSP')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ManualRedebitCreated', 0, 'Manual Redebit Created', 'Manual {FinancialTransactionId,,redebit} created', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'MonthlyFeeCreated', 0, 'Monthly Fee Created', 'Minimum Monthly Fee Created', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'MultipleCompanyLawsCreated', 0, 'Multiple Company Laws Created', 'More than one company law record created for Company/Agency/Law', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'NOC', 0, 'NOC', 'NOC ({ACHEventCd} received', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'NOCWithOutChanges', 0, 'NOCWithOutChanges', 'NOC with no change received', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'NonAchPaymentReceived', 0, 'Non-ACH Payment Received', 'Non-ACH payment received and applied on a {FinancialTransactionId}', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'NonPrintChecks', 0, 'To Print Checks Received', 'Checks were received that the customer will need to print in QuickBooks', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'NSF', 0, 'NSF', 'NSF ({ACHEventCd}, {NSFSubType}) received on a {FinancialTransactionId}', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'NumberOfPayrollsPerDayExceeded', 0, 'Number Of Payrolls Per Day Exceeded', '{Details} on a {PayrollRunId}', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'OfferClaimed', 0, 'Offer Claimed', 'An offer ({NewStringValue}) has been claimed', 'Agent')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'OfferingUpdated', 0, 'Offering Updated', 'Offering changed from {OldStringValue} to {NewStringValue}', 'Agent')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'OfferRemoved', 0, 'Offer Removed', 'An offer ({OldStringValue}) has been removed', 'Agent')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PaycheckRecalled', 0, 'Paycheck Recalled', 'A paycheck was recalled', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PaycheckRecalledAfterOffload', 0, 'Paycheck Recalled After Offload', 'A paycheck was recalled after the payroll was offloaded', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayeePaidGreaterThanMax', 0, 'Payee Paid Greater Than Max', 'A payee paid greater than the max limit on a {PayrollRunId}', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayeePaidTooManyTimes', 0, 'Payee Paid Too Many Times in a Short Period', 'Payee paid too many times in a short period', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PaymentMethodChanged', 0, 'Payment Method Changed', 'Payment method modified', 'Agent')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollAdminChanged', 0, 'Payroll Admin Info Changed', 'Payroll admin info changed', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollCancelled', 0, 'Payroll Cancelled', 'A {PayrollRunId} ({PayrollCancellationScope} was cancelled: {PayrollCancellationReason}', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollCancelPending', 0, 'Payroll Cancel Pending', 'A {PayrollRunId} cancel pending for {CancellationDateTime,date}', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollProcessedTooSoon', 0, 'Payroll Processed Too Soon', 'A {PayrollRunId} was processed too soon: {Details}', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollRecalled', 0, 'Payroll Recalled', 'A {PayrollRunId} has been recalled', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollReceived', 0, 'Payroll Received', 'A {SourcePayrollRunId} was received', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollReceivedCloud', 0, 'Cloud Payroll Received', 'A {SourcePayrollRunId} was received', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollReceivedPayCard', 0, 'PayCard Payroll Received', 'A {SourcePayrollRunId} had fees reduced from {PaycheckAmount} PayCard paycheck(s)', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollRejected', 0, 'Payroll Rejected', 'Payroll rejected', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollSubmittedWithPendingNOC', 0, 'Payroll Submitted with Pending NOC', 'A {SourcePayrollRunId} submitted with a pending NOC', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollSubmissionIncludedAllNewEmployees', 0, 'Payroll Submission Included All New Employees', 'Payroll Submission Included All New Employees [Historical]', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PINCreated', 0, 'PIN Created', 'PIN created', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'PINReset', 0, 'PIN Reset', 'PIN reset by {UserId}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PINUpdated', 0, 'PIN Updated', 'PIN updated', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PINUnlocked', 0, 'PIN Unlocked', 'PIN unlocked for one extra attempt', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PositiveCobraReceived', 0, 'Positive COBRA Adjustment Received', 'A positive COBRA adjustment was received: {Details}', 'CompanyInfo') 
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PrefundingReceived', 0, 'Prefunding Received', 'Prefunding transaction recorded', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PSPToAS400HoldSync', 0, 'PSP to AS400 Hold Sync', 'Sync of PSP Hold to AS400 Required', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PSPToAS400HoldRemoveSync', 0, 'PSP to AS400 Hold Sync Removal', 'Sync of PSP Hold Removal to AS400 Required', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'QuickBooksInfoChanged', 0, 'QuickBooks Info Changed', 'Quickbooks info changed', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'RedebitAmountUpdated', 0, 'Redebit Amount Updated', 'Redebit amount updated', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'RedebitDateUpdated', 0, 'Redebit Date Updated', 'A {PayrollRunId} redebit date was updated from {OldDate,date} to {NewDate,date}', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ReversalOK', 0, 'Reversal Successful', 'Reversal {FinancialTransactionId} successfully processed and funds recovered from the employee bank account', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ReversalRequested', 0, 'Reversal Requested', 'Reversal {FinancialTransactionId,,request} sent to bank to recover funds from an employee bank account', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ReversalReturn', 0, 'Reversal Return', 'Reversal {FinancialTransactionId} returned ({ACHEventCd})', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SalesTaxReturn', 0, 'Sales Tax Return', 'Sales tax {FinancialTransactionId,,debit} returned ({ACHEventCd})', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ServiceKeyUpdated', 0, 'Service Key Updated', 'Service Key Updated', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ServiceStatusChange', 0, 'Service Status Change', 'Service status changed from {OldServiceStatus} to {NewServiceStatus}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SingleEmployeePercentageIncrease', 0, 'Single Employee Percentage Increase', 'Increase in single employee percentage on a {PayrollRunId}: {Details}', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'StateIdModified', 0, 'State ID Modified', 'State ID modified', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'Strike', 0, 'Strike', 'Strike: {StrikeReason}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SUIEoqCreditCreated', 0, 'SUI Eoq Credit Created', 'A SUI EOQ credit was created for the company', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SUIEoqDebitCreated', 0, 'SUI Eoq Debit Created', 'A SUI EOQ debit was created for the company', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SUIImmediateCreditCreated', 0, 'SUI Immediate Credit Created', 'A SUI Immediate Credit was created for the company', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SUIImmediateDebitCreated', 0, 'SUI Immediate Debit Created', 'A SUI Immediate Debit was created for the company', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'TaxExemptStatusChanged', 0, 'Tax Exempt Status Changed', 'Tax exempt status changed', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ThresholdExceeded', 0, 'Threshold Exceeded', 'Payment template threshold amount exceeded', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'TOKNotifiedOfCompanyFraud', 0, 'TOK Notified Of Company Fraud', 'TOK Notified Of Company Fraud', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'TotalBillPaymentExceedsLimit', 0, 'Total Vendor Payment Exceeds Limit', 'A {PayrollRunId,,vendor payment} exceeds limit: {Details}', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'TotalPayrollExceedsLimit', 0, 'Total Payroll Exceeds Limit', 'A {PayrollRunId} exceeds limit: {Details}', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'TransmissionError', 0, 'Transmission Error', 'Error in transmission: {ErrorMessage}', 'PSP')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'VoidedPaycheckAlreadyOffloadedToTOK', 0, 'Voided Paychecks Already Offloaded To TOK', 'Voided Paychecks Already Offloaded To TOK', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'WireExpected', 0, 'Wire Expected', 'Wire expected date added or modified to {WireExpectedDate,date}', 'Agent')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ZeroPayrollReceived', 0, 'Zero Payroll Received', 'Zero payroll received.', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EmployeeBankAccountChange', 0, 'Employee Bank Account Change', 'Employee bank account changed', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'Employee401kDataUploaded', 0, 'Initial employee 401k data uploaded to provider', 'Initial employee 401k data uploaded to provider', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PreOffload401kValidationAlert', 0, 'Pre 401k offload validation email', 'Pre 401k offload validation email', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PostOffload401kValidationAlert', 0, 'Post 401k offload validation email', 'Post 401k offload validation email', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CloudResponse', 0, 'Cloud Response', 'Cloud payroll submit verification response', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'InactivityDDPayrollAmountExceeded', 0, 'Inactivity Payroll Amount Exceeded and Emp bank account was updated', 'A {PayrollRunId} exceeded the inactivity payroll amount limit', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'InactivityBPPayrollAmountExceeded', 0, 'Inactivity Vendor Payment Amount Exceeded and Vendor bank account was updated', 'A {PayrollRunId,,vendor payment} exceeded the inactivity vendor payment amount limit', 'Fraud')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'InvalidSourceSystemTransmissionInformation', 0, 'Invalid Source System Transmissoin Information', 'Invalid source system transmission information was submitted', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EntitlementUnitStatusChanged', 0, 'Entitlement Unit Status Changed', 'Company entitlement unit status has changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SubscriptionEndDateChanged', 0, 'Subscription End Date Changed', 'Company subscription end date has changed from {OldDate} to {NewDate}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EntitlementStateChanged', 0, 'Entitlement State Changed', 'Company entitlement state has changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EntitlementCommunication', 0, 'Entitlement Communication', 'An error has occured while communicating with ERS: {ErrorMessage}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SourceCompanyIdChanged', 0, 'Source Company Id Changed', 'Source Company Id has changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EntitlementCodeChanged', 0, 'Entitlement Code changed', 'Entitlement code changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PriceTypeChanged', 0, 'Price Type changed', 'Price Type changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'BalanceFileReceived', 0, 'Balance File Received', 'A balance file submission has been recorded for this company', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'RequestProcessingFlagChanged', 0, 'Request Processing Flag Changed', 'The request processing flag has changed for this company {GenericEventDetail}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'AllowTransmissionsFlagChanged', 0, 'Allow QBDT Transmissions Flag Changed', 'The allow QBDT transmissions flag has changed for this company {GenericEventDetail}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'TaxPaymentStatusChanged', 0, 'Agency Tax Payment Status Changed', '{GenericEventDetail}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'OFXServiceActivated', 0, 'OFX Service Activated', 'DirectDeposit or Tax service added or re-activated', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ERPayableRefundCreated', 0, 'ERPayable Refund Created', 'A {FinancialTransactionId,,refund} was created out of the ERPayable account', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ManualLedgerEntry', 0, 'Manual Ledger Entry', 'A manual ledger entry was created: {NoteText}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollTaxPaymentVoided', 0, 'Payroll Tax Payment Voided', 'Tax payments for a {PayrollRunId} in the amount of {Amount} were voided.', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollTaxPaymentReissued', 0, 'Payroll Tax Payment Reissued', 'Tax payments for a {PayrollRunId} in the amount of {Amount} were reissued.', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ERPayableAppliedToBalanceDue', 0, 'ERPayable Applied to Balance Due', '{Amount} from ER Payable was applied to the balance due on a {PayrollRunId}.', 'FinancialOps')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'AIDUpdated', 0, 'AID Updated', '{UniqueIdentifier} for {AgencyId} updated from {OldStringValue} to {NewStringValue}.', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ManualDataSync', 0, 'Manual Data Sync Recorded', 'A manual data sync was recorded: {NoteText}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'AccountingFinancialLedgerAdjustmentCreated', 0, 'Accounting Financial Ledger Adjustment Created', 'Accounting Financial Ledger Adjustment Created:{NoteText}, Transaction Type {TransactionType}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PSIDMismatch', 0, 'PSID Mismatch Detected', 'The request from EWS contained a PSID that did not match the PSID on the company.', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ERPenaltiesAndInterestRefundCreated', 0, 'Employer Penalties And Interest Refund Created', 'ER Penalties & Interest Refund Created:{NoteText}, PenaltiesRefund:{PenaltiesRefundAmount} InterestRefund:{InterestRefundAmount} Tot. Refund:{TotalRefundAmount}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ERPenaltiesAndInterestRefundDebitCreated', 0, 'Employer Penalties And Interest Refund Debit Created', 'ER Penalties & Interest Refund Debit Created:{NoteText}, RefundDebitAmount:{RefundDebitAmount}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'FileIdChanged', 0, 'QuickBooks File ID Changed', 'QuickBooks File ID was changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'BackdatePriorToProcessingStart', 0, 'Backdate Prior to Processing Start', 'A backdated {PayrollRunId} was sent including taxes for a quarter not previously processed on PSP', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'CreditReduction', 0, 'Credit Reduction', 'A {Law} Credit Reduction of {Amount} applied to {PaycheckDate,date} via a {PayrollRunId}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EoqvBulkUpload', 0, 'EOQV Bulk Upload', 'A {Law} EOQV of {Amount} applied to {PaycheckDate,date} via a {PayrollRunId}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayrollSubmittedWithEmployeeWithPendingReturn', 0, 'Payroll Submitted With Employee With Pending Return', 'A {SourcePayrollRunId} submitted with an employee that has a Pending Return', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'DuplicatePayrollItemReceived', 0, 'A duplicate payroll item was received from the source system', 'A duplicate payroll item ({Description}) was sent by the source system. The new item (id: {NewStringValue}) has replaced the old item (id: {OldStringValue}).', 'PayrollStatus')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ACHEnrollmentStatusChanged', 0, 'ACH Enrollment Status changed', 'ACH enrollment status is changed from {OldStringValue} to {NewStringValue}.', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'VmpSignUpEmployeeEmail', 0, 'Email employee about VMP sign up', 'An employee signed up for VMP, send an email to the employee', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'VmpSignUpEmployerEmail', 0, 'Email employer about VMP sign up', 'An employee signed up for VMP, send an email to their employer', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PaystubCreated', 0, 'A new paystub has been created', 'A new paystub has been created', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'AdditionalFilingAmount', 0, 'An Additional Filing Amount has been updated or created', 'An Additional Filing Amount has been updated or created', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SendEmailFailed', 0, '', 'Send Email Failed for {EmailTemplateType}, Email address:{RecipientEmailAddress}, Error code:{ErrorCode} Message:{ErrorMessage}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SendEmailSkipped', 0, '', 'Send Email skipped for {EmailTemplateType}, Email address:{RecipientEmailAddress}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'ERLoanNOC', 0, 'Employer NOC', 'Employer NOC {ACHEventCd} received', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SUICreditsApplied', 0, 'SUI Credits Applied', '{NewDate} SUI Credits applied for {PaymentTemplate}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'TrialAssetDetected', 0, 'Trial Asset Detected', 'Trial Asset Detected', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'WelcomeEmail', 0, 'Welcome Email', 'Welcome Email', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
  'NewPSIDCreatedForExistingCustomer', 0, 'New PSID Created For Existing Customer', 'PSID changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
  'CompanyIndustryTypeChanged', 0, 'Company IndustryType Changed', 'Company Industry Type changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
  'PrimaryPrincipalSSNChanged', 0, 'Primary Principal SSN Changed', 'Primary Principal SSN changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
  'PrimaryPrincipalDOBChanged', 0, 'Primary Principal DOB Changed', 'Primary Principal DOB changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
  'PrimaryPrincipalNameChanged', 0, 'Primary Principal Name Changed', 'Primary Principal Name Changed from {OldStringValue} to {NewStringValue}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
  'PayeeBankAccountChange', 0, 'Payee BankAccount Change', 'Payee BankAccount Change', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EmployeeAdded', 0, 'Employee Added', 'An employee is added', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EmployeeUpdated', 0, 'Employee Updated', 'An employee is updated', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'EmployeeDeleted', 0, 'Employee Deleted', 'An employee is deleted', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayeeAdded', 0, 'Payee Added', 'A payee is added', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PayeeUpdated', 0, 'Payee Updated', 'A payee is updated', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'DDMigration', 0, 'Direct Deposit Service Migration', '{ServiceStatus}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'SplitMMTReturn', 0, 'Split MMT Return', 'SplitMMTReturn', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'RealmIdAdded', 0, 'Realm Id Added', 'Company Realm Id Added', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'RealmIdUpdated', 0, 'Realm Id Updated', 'Company Realm Id Updated', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'BillingRealmCreated', 0, 'Billing Realm Created', 'Billing Realm created for Payroll Purchase', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'BillingRealmAttached', 0, 'Billing Realm Attached', 'Billing Realm Attached to Data Realm', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'DataRealmAttached', 0, 'Data Realm Attached', 'Data Realm Attached to Billing Realm', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'GrantAttached', 0, 'Grant Attached', 'Grant added to Realm', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'DGDeleteRequest', 0, 'DG Delete Request', 'DGDeleteRequest', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'DGAccessRequest', 0, 'DG Access Request', 'DGAccessRequest', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'AutoEnabledVMP', 0, 'Auto Enabled VMP', 'Auto Enabled VMP Service', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'ConsentChange', 0, 'Consent Change Event', 'Consent value {ConsentValue} for application {AppName} User: {AuthId} and RealmId: {DataRealmId}', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'EmployeeInvited', 0, 'Employee Invited Event', 'Employee Invited', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'EmployeeSignedUp', 0, 'Employee SignedUp Event', 'Employee Signed Up', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'EmployeeBankAccountWalletSuccess', 0, 'Employee BankAccount Wallet Success Event', 'Employee BankAccount Wallet Success', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'EmployeeBankAccountWalletFailure', 0, 'Employee BankAccount Wallet Failure Event', 'Employee BankAccount Wallet Failure', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'CloneEmployeeWalletOnRealmChangeSuccess', 0, 'Clone Employee Wallet On Realm Change Success Event', 'Clone Employee Wallet On Realm Change Success', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'CloneEmployeeWalletOnRealmChangeFailure', 0, 'Clone Employee Wallet On Realm Change Failure Event', 'Clone Employee Wallet On Realm Change Failure', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'AddUserToRealmSuccess', 0, 'Add User To Realm Success Event', 'Add User To Realm Success', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'AddUserToRealmFailure', 0, 'Add User To Realm Failure Event', 'Add User To Realm Failure', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'AddUserToRealmOnRealmChangeSuccess', 0, 'Add User To Realm On Realm Change Success Event', 'Add User To Realm On Realm Change Success', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'AddUserToRealmOnRealmChangeFailure', 0, 'Add User To Realm On Realm Change Failure Event', 'Add User To Realm On Realm Change Failure', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'RiskProfileMigrated', 0, 'Risk Profile Migrated', 'Risk Profile Migrated', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'RiskProfileUnMigrated', 0, 'Risk Profile Un Migrated', 'Risk Profile Un Migrated', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'PSPToSMSMigration', 0, 'Account Profile Migrated to SMS', 'Account Profile Migrated to SMS', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'PSPToSMSMigrationRevert', 0, 'Account Profile Migrated to SMS reverted', 'Account Profile Migrated to SMS reverted', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'SMSRealmIdUpdated', 0, 'SMS Company RealmId Updated', 'SMS Company RealmId Updated', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'UpdateConsumerRealmId', 0, 'Consumer realm updated', 'Consumer realm updated', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'VendorBankAccountWalletFailure', 0, 'Vendor Bank Account Wallet Failure', 'Vendor Bank Account Wallet Failure', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'VendorBankAccountWalletSuccess', 0, 'Vendor Bank Account Wallet Success', 'Vendor Bank Account Wallet Success', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'CloneVendorWalletOnRealmChangeFailure', 0, 'Clone Vendor Wallet On Realm Change Failure', 'Clone Vendor Wallet On Realm Change Failure', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'CloneVendorWalletOnRealmChangeSuccess', 0, 'Clone Vendor Wallet On Realm Change Success', 'Clone Vendor Wallet On Realm Change Success', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'LegacySubscriptionMigration', 0, 'Legacy Subscription Migration', 'Legacy Subscription Migration', 'CompanyInfo')
/
INSERT INTO TEMP_EVENT_TYPE (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD) VALUES (
'UnsyncedEmployeeInvite', 0, 'Employee Unsynced Event', 'Employee Unsynced Invited or to be Invited', 'CompanyInfo')
/ 
INSERT INTO TEMP_EVENT_TYPE ( EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD ) VALUES (
'PendingPaymentRefunded', 0, 'Pending Payment Refund', 'A Pending Payment was Refunded: {NoteText}', 'CompanyInfo')
/
--------------------------------------------------------
-- TEMP_EVENT_DETAIL_TYPE                             --
--------------------------------------------------------
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ACHEventCd', 0, 'ACH Return CD', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ACHReturnReasonCode', 0, 'ACH Return Reason Code', '', 'com.intuit.sbd.payroll.psp.domain.ACHReturnReason')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'Amount', 0, 'Amount', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'AgencyId', 0, 'Agency Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'AS400EventToken', 0, 'AS400 event token [Historical]', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'AS400EventName', 0, 'AS400 event name [Historical]', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'AS400EventOFX', 0, 'AS400 event OFX [Historical]', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'BankAccountNumber', 0, 'Bank Account Number', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'BankAccountRoutingNumber', 0, 'Bank Account Routing Number', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'BillPaymentId', 0, 'Vendor Payment Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'CancellationDateTime', 0, 'Cancellation Date Time', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'CaseId', 0, 'Case Id for any update', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'CoaNameChangeByAgent', 0, 'Chart Of Account Name Change By Agent', '', 'java.lang.Boolean')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'CollectionStage', 0, 'Collection Stage', '', 'com.intuit.sbd.payroll.psp.domain.CollectionStageCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'CompanyAgency', 0, 'Company Agency', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'CompanyBankAccountId', 0, 'Company Bank Account Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'CompanyEventId', 0, 'Company Event Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'CompanyServiceId', 0, 'Company Service Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ContactId', 0, 'Contact Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'Details', 0, 'Details', 'Used to provide detailed information regarding the event in complete sentence form', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'EmployeeBankAccountId', 0, 'Employee BankAccount Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'EmployeeId', 0, 'Employee Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'EmployeeName', 0, 'Employee Name', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'EmployeeInvalidReason', 0, 'Reason Employee was invalid', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'EnrollmentType', 0, 'Enrollment Type', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ErrorCode', 0, 'Error Code', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ErrorMessage', 0, 'Error Message', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ExpectedToken', 0, 'Expected Token [Historical]', '', 'java.lang.Integer')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'FailureReason', 0, 'Failure Reason', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'FeeAmount', 0, 'Fee Amount', '', 'com.intuit.spc.foundations.primary.SpcfMoney')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'FeeBillingDetailId', 0, 'Fee Billing Detail Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'FeeType', 0, 'Fee Type', '', 'com.intuit.sbd.payroll.psp.domain.FeeTypeCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'FinancialTransactionId', 0, 'Financial Transaction Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'FraudEventCategory', 0, 'Fraud Event Category', 'Used to differentiate fraud event types', 'com.intuit.sbd.payroll.psp.domain.FraudEventCategory')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'IntuitInitiated', 0, 'Intuit Initiated Reversal', '', 'java.lang.Boolean')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'Law', 0, 'Law', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'LimitAmount', 0, 'Limit Amount', '', 'com.intuit.spc.foundations.primary.SpcfMoney')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'LimitType', 0, 'Limit Type', '', 'com.intuit.sbd.payroll.psp.domain.EventLimitCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ManualStrikeReasonDescription', 0, 'Manual Strike Reason Description', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'MessageLevel', 0, 'Message Level of Error or Warning', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewAccountNumber', 0, 'New Account Number', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewAccountType', 0, 'New Account Type', '', 'com.intuit.sbd.payroll.psp.domain.BankAccountType')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewAchAccountType', 0, 'New Ach Account Type', '', 'com.intuit.sbd.payroll.psp.domain.ACHBankAccountType')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewAmount', 0, 'New Amount', '', 'com.intuit.spc.foundations.primary.SpcfMoney')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewBAStatus', 0, 'New Bank Account Status', '', 'com.intuit.sbd.payroll.psp.domain.BankAccountStatus')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewCoaName', 0, 'New Chart Of Accounts Name', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewDate', 0, 'New Date', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewLimitAmount', 0, 'New Limit Amount', '', 'com.intuit.spc.foundations.primary.SpcfMoney')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewOnHoldReason', 0, 'New OnHold Reason', '', 'com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewPayrollStatus', 0, 'New Payroll Status', '', 'com.intuit.sbd.payroll.psp.domain.PayrollStatus')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewRoutingNumber', 0, 'New Routing Number', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewServiceStatus', 0, 'New Service Status', '', 'com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewStringValue', 0, 'New Value', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NoteText', 0, 'Note Text', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NSFSubType', 0, 'NSF Subtype', '', 'com.intuit.sbd.payroll.psp.domain.NSFSubTypeType')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OffloadDate', 0, 'Offload Date', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldAccountNumber', 0, 'Old Account Number', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldAccountType', 0, 'Old Account Type', '', 'com.intuit.sbd.payroll.psp.domain.BankAccountType')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldAchAccountType', 0, 'Old Ach Account Type', '', 'com.intuit.sbd.payroll.psp.domain.ACHBankAccountType')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldAmount', 0, 'Old Amount', '', 'com.intuit.spc.foundations.primary.SpcfMoney')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldBAStatus', 0, 'Old Bank Account Status', '', 'com.intuit.sbd.payroll.psp.domain.BankAccountStatus')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldCoaName', 0, 'Old Chart Of Accounts Name', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldDate', 0, 'Old Date', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldLimitAmount', 0, 'Old Limit Amount', '', 'com.intuit.spc.foundations.primary.SpcfMoney')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldOnHoldReason', 0, 'Old OnHold Reason', '', 'com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldPayrollStatus', 0, 'Old Payroll Status', '', 'com.intuit.sbd.payroll.psp.domain.PayrollStatus')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldRoutingNumber', 0, 'Old Routing Number', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldServiceStatus', 0, 'Old Service Status', '', 'com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldStringValue', 0, 'Old Value', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OverrideRecipientEmailAddress', 0, 'Override Recipient Email Address ', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PaycheckAmount', 0, 'Paycheck Amount', '', 'com.intuit.spc.foundations.primary.SpcfMoney')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PaycheckDate', 0, 'Paycheck Date', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PaycheckId', 0, 'Paycheck Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PaycheckInvalidReason', 0, 'Reason Paycheck was invalid', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PayeeBankAccountId', 0, 'Payee Bank Account Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PayeeId', 0, 'Payee Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PayeeName', 0, 'Payee Name', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PaymentDueDate', 0, 'Payment Due Date', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PaymentInitiationDate', 0, 'Payment Initiation Date', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PaymentMethod', 0, 'Payment Method', '', 'com.intuit.sbd.payroll.psp.domain.PaymentMethod')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PayrollCancellationReason', 0, 'Cancellation Reason', '', 'com.intuit.sbd.payroll.psp.domain.CancellationReasonCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PayrollCancellationScope', 0, 'Cancellation Scope', '', 'com.intuit.sbd.payroll.psp.domain.CancellationScopeCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PayrollRejectedReason', 0, 'Payroll Rejected Reason', '', 'com.intuit.sbd.payroll.psp.domain.PayrollRejectedReason')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PayrollRunId', 0, 'Payroll Run Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PayrollStatus', 0, 'Payroll Status', '', 'com.intuit.sbd.payroll.psp.domain.PayrollStatus')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'Percentage', 0, 'Percentage', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ReasonDescription', 0, 'Reason Description', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ReceivedToken', 0, 'Received Token [Historical]', '', 'java.lang.Integer')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'RefundAmount', 0, 'Refund Amount', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'RefundedFeeBillingDetailId', 0, 'Refunded Fee Billing Detail Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'RefundStatus', 0, 'Refund Status', '', 'com.intuit.sbd.payroll.psp.domain.RefundStatusType')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'RefundStatusReason', 0, 'Refund Status Reason', '', 'com.intuit.sbd.payroll.psp.domain.RefundStatusReasonType')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ReturnType', 0, 'Return Type', '', 'com.intuit.sbd.payroll.psp.domain.ACHReturnType')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ServiceCode', 0, 'Service Code', '', 'com.intuit.sbd.payroll.psp.domain.ServiceCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ServiceStatus', 0, 'Service Status', '', 'com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'SourceBankAccountId', 0, 'Source Bank Account Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'SourceEmployeeId', 0, 'Source Employee Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'SourcePaycheckId', 0, 'Source Paycheck Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'SourcePayrollRunId', 0, 'Source System Payroll Run Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'StrikeReason', 0, 'Strike Reason', '', 'com.intuit.sbd.payroll.psp.domain.StrikeReason')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'Timestamp', 0, 'Timestamp', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'TransmissionId', 0, 'Transmission Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'UniqueIdentifier', 0, 'Unique Identifier', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'UserId', 0, 'User Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'VerificationStatus', 0, 'Verification Status', '', 'com.intuit.sbd.payroll.psp.domain.VerificationStatusType')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ViolationAmount', 0, 'Violation Amount', '', 'com.intuit.spc.foundations.primary.SpcfMoney')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'WireExpectedDate', 0, 'Wire Expected Date', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'GenericEventDetail', 0, 'Generic Name,Value Pair', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewEffectiveDate', 0, 'New Effective Date', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldEffectiveDate', 0, 'Old Effective Date', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewDepositFrequency', 0, 'New Deposit Frequency', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldDepositFrequency', 0, 'Old Deposit Frequency', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PaymentTemplate', 0, 'Payment Template Code', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldEmployeeBankAccountId', 0, 'Old Employee Bank Account Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewEmployeeBankAccountId', 0, 'New Employee Bank Account Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'SourceSystemTransmissionInvalidReason', 0, 'Reason Source System Transmission was invalid', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PaymentEFTNumber', 0, 'Tax Payment Electronic Funds Transfer Transaction Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PaymentAcknowledgeNumber', 0, 'Tax Payment Agency Acknowledgement Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'MoneyMovementTransactionId', 0, 'Money Movement Transaction Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'CompanyTIN', 0, 'Agency Taxpayer Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PaymentPeriodEndDate', 0, 'Tax Period End Date', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'SourceCompanyId', 0, 'Source Company Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OFXToken', 0, 'OFX Token', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NextEmployeeId', 0, 'NextEmployeeId', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NextPaylineTransactionId', 0, 'NextPaylineTransactionId', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NextPayrollTransactionId', 0, 'NextPayrollTransactionId', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NextPaycheckId', 0, 'NextPaycheckId', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'EntitlementId', 0, 'Entitlement Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'EntitlementUnitId', 0, 'Entitlement Unit Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'Description', 0, 'Description', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'InvalidatedDepositFrequencyId', 0, 'Invalidated Deposit Frequency Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'TransactionType', 0, 'Transaction Type Code', '', 'com.intuit.sbd.payroll.psp.domain.TransactionTypeCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ThresholdPeriodStartDate', 0, 'Threshold Period Start Date', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ThresholdPeriodEndDate', 0, 'Threshold Period End Date', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PermanentPaymentFrequencyId', 0, 'Permanent Payment Deposit Frequency Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ThresholdReversed', 0, 'Threshold reversed', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PenaltiesRefundAmount', 0, 'Penalties refund amount', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'InterestRefundAmount', 0, 'Interest refund amount', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'TotalRefundAmount', 0, 'Total refund amount', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'RefundDebitAmount', 0, 'Refund Debit amount', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ACHEnrollmentId', 0, 'ACH Enrollment Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'RecipientEmailAddress', 0, 'Recipient Email Address', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'EmailTemplateType', 0, 'Event Email Template Type code', '', 'com.intuit.sbd.payroll.psp.domain.EventEmailTemplateTypeCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'VendorInvalidEmail', 0, 'Vendor invalid email', '', 'com.intuit.sbd.payroll.psp.domain.EventEmailTemplateTypeCode')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
  'OldCompanyBankAccountId', 0, 'Old Company Bank Account Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
  'OldPayeeBankAccountNumber', 0, 'Old Payee Bank Account num', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
  'NewPayeeBankAccountNumber', 0, 'New Payee Bank Account num', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
  'OldPayeeBankRoutingNumber', 0, 'Old Payee Bank Routing Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
  'NewPayeeBankRoutingNumber', 0, 'New Payee Bank Routing Id', '', 'com.intuit.spc.foundations.portability.SpcfUniqueId')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
  'FirstPayrollRunDate', 0, 'First Payroll Run Date', '', 'com.intuit.spc.foundations.portability.util.SpcfCalendar')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
  'PayrollCount', 0, 'Payroll Count', '', 'java.lang.Long')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'DataRealmId', 0, 'Company Realm Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'GrantType', 0, 'Grant type ', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'WorkflowId', 0, 'Workflow details', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'AuthId', 0, 'authentication Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'BillingRealmId', 0, 'Billing Realm Id with Payroll purchase', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'WorkOrderId', 0, 'Work Order Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'EmployeeSequence', 0, 'Employee Sequence', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'WorkOrderCreatedTime', 0, 'WorkOrder Created Time', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ServiceKey', 0, 'Service Key', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'CompanySequence', 0, 'Company Sequence', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'CompanyName', 0, 'Company Name', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ConsentValue', 0, 'Consent Value', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'AppName', 0, 'Consent App Name', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'EmailTemplate', 0, 'Email Template Name', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'InvitationSource', 0, 'Invitation Source Name', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'IUSInvitationId', 0, 'IUS Invitation Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PersonaId', 0, 'Persona Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewWalletId', 0, 'New Wallet Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldWalletId', 0, 'Old Wallet Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'CompanyRealmId', 0, 'Company Realm Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldCompanyRealmId', 0, 'Old Company Realm Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'ConsumerRealmId', 0, 'Consumer Realm Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OwnerOldLimit', 0, 'Owner Old Limit', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OwnerNewLimit', 0, 'Owner New Limit', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PayeeOldLimit', 0, 'Payee Old Limit', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'PayeeNewLimit', 0, 'Payee New Limit', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'VendorId', 0, 'Vendor Id', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'BillingFrequencyType', 0, 'Billing Frequency Type', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NextChargeDate', 0, 'Next Charge Date', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'BaseRate', 0, 'Base Rate', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'LicenseNumber', 0, 'License Number', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'EntitlementOfferingCode', 0, 'Entitlement Offering Code', '', 'java.lang.String')
/
INSERT INTO TEMP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'DaysTillRenewal', 0, 'Days Till Renewal', '', 'java.lang.String')
/
--------------------------------------------------------
-- TEMP_EVTTP_SRCSYS_ASSOC                            --
--------------------------------------------------------
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'CBAVerifyReturn', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'DDDebitReturn', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'DDReject', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'ERRefundReturn', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'FeeReturn', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'NOC', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'NSF', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'ReversalReturn', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'DDReject', 'QBDT' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'FeeReturn', 'QBDT' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'NOC', 'QBDT' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'ReversalReturn', 'QBDT' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'PayrollReceived', 'QBDT' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'PayrollRejected', 'QBDT' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'ServiceStatusChange', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'ServiceStatusChange', 'CRIS' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'PayrollAdminChanged', 'CRIS' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'QuickBooksInfoChanged', 'CRIS' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'EINChanged', 'QBDT' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'FeeOffloaded', 'QBDT' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'BankAccountVerified', 'QBDT' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'CompanyBankAccountStatusChange', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'PayrollCancelled', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'ReversalRequested', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'ReversalOK', 'QBOE' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'LegalNameChanged', 'QBDT' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'LegalAddressChanged', 'QBDT' )
/

INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'AS400Event', 'QBDT' )
/
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'ZeroPayrollReceived', 'QBDT' )
/
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'PaycheckRecalled', 'QBDT' )
/
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ( 'PaycheckRecalledAfterOffload', 'QBDT' )
/

--Events that flow to AS400
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ('EINChanged', 'AS400')
/
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ('EntitlementStateChanged', 'AS400')
/
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ('TaxExemptStatusChanged', 'AS400')
/
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ('PayrollAdminChanged', 'AS400')
/
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ('ServiceKeyUpdated', 'AS400')
/
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ('EntitlementCodeChanged', 'AS400')
/
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ('LegalAddressChanged', 'AS400')
/
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ('LegalNameChanged', 'AS400')
/
INSERT INTO TEMP_EVTTP_SRCSYS_ASSOC ( INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK ) VALUES ('DBANameChanged', 'AS400')
/



--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_EVENT_TYPE
   (EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD)
SELECT 
    EVENT_TYPE_CD, VERSION, NAME, DESCRIPTION, EVENT_GROUP_CD
FROM 
   TEMP_EVENT_TYPE tt
WHERE 
   tt.EVENT_TYPE_CD NOT IN (SELECT EVENT_TYPE_CD FROM PSP_EVENT_TYPE)
/

INSERT INTO PSP_EVENT_DETAIL_TYPE
   (EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME)
SELECT
    EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME
FROM
   TEMP_EVENT_DETAIL_TYPE tt
WHERE
   tt.EVENT_DETAIL_TYPE_CD NOT IN (SELECT EVENT_DETAIL_TYPE_CD FROM PSP_EVENT_DETAIL_TYPE)
/

INSERT INTO PSP_EVTTP_SRCSYS_ASSOC
   (INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK)
SELECT
    INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK
FROM
   TEMP_EVTTP_SRCSYS_ASSOC tt
WHERE
   (tt.INTERESTING_EVENT_TYPES_FK, tt.SOURCE_SYSTEM_FK) NOT IN (SELECT INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK FROM PSP_EVTTP_SRCSYS_ASSOC)
/

DELETE FROM PSP_EVTTP_SRCSYS_ASSOC
WHERE (INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK) NOT IN (SELECT INTERESTING_EVENT_TYPES_FK, SOURCE_SYSTEM_FK FROM TEMP_EVTTP_SRCSYS_ASSOC)
/

DELETE FROM PSP_EVENT_TYPE
WHERE EVENT_TYPE_CD NOT IN (SELECT EVENT_TYPE_CD FROM TEMP_EVENT_TYPE)
/

DELETE FROM PSP_EVENT_DETAIL_TYPE
WHERE EVENT_DETAIL_TYPE_CD NOT IN (SELECT EVENT_DETAIL_TYPE_CD FROM TEMP_EVENT_DETAIL_TYPE)
/

UPDATE PSP_EVENT_TYPE rt
SET (rt.VERSION, rt.NAME, rt.DESCRIPTION, rt.EVENT_GROUP_CD) =
(SELECT tt.VERSION, tt.NAME, tt.DESCRIPTION, tt.EVENT_GROUP_CD 
 FROM TEMP_EVENT_TYPE tt WHERE tt.EVENT_TYPE_CD = rt.EVENT_TYPE_CD)
/

UPDATE PSP_EVENT_DETAIL_TYPE rt
SET (rt.VERSION, rt.NAME, rt.DESCRIPTION, rt.VALUE_CLASS_NAME) =
(SELECT tt.VERSION, tt.NAME, tt.DESCRIPTION, tt.VALUE_CLASS_NAME
 FROM TEMP_EVENT_DETAIL_TYPE tt WHERE tt.EVENT_DETAIL_TYPE_CD = rt.EVENT_DETAIL_TYPE_CD)
/


--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_EVENT_TYPE
/

DROP TABLE TEMP_EVENT_DETAIL_TYPE
/

DROP TABLE TEMP_EVTDTLTP_EVTTP_ASSOC
/

DROP TABLE TEMP_EVTTP_SRCSYS_ASSOC
/

COMMIT
