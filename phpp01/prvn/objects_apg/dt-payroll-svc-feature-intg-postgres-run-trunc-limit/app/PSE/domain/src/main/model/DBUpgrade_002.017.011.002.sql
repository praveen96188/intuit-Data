DECLARE 
v_text             varchar2(8000); 
v_column_name      varchar2(250); 
in_text            varchar2(8000); 
query1             varchar2(8000); 
query2             varchar2(8000); 
BEGIN 
  in_text := 'EVENT_TYPE_CD IN(''FileIdChanged'', ''SendEmailFailed'', ''PaycheckRecalledAfterOffload'', ''SendEmailSkipped'', ''InvalidVendorEmail'', ''ERLoanNOC'', ''NewPSIDCreatedForExistingCustomer'', ''PrimaryPrincipalNameChanged'', ''PayeeBankAccountChange'', ''BackdatePriorToProcessingStart'', ''SubscriptionEndDateChanged'', ''SUICreditsApplied'', ''TrialAssetDetected'', ''WelcomeEmail'', ''AccountLocked'', ''ACHReturnStatusChanged'', ''EINChanged'', ''ServiceStatusChange'', ''CompanyBankAccountStatusChange'', ''LimitViolation'', ''ReversalOK'', ''DDIncreasePayrollLimit'', ''PayrollCancelled'', ''Strike'', ''ReversalRequested'', ''TransmissionError'', ''PINCreated'', ''PINUpdated'', ''PayrollSubmittedWithPendingNOC'', ''BackdatedPayrollReceived'', ''ACHReturn'', ''FeeCreated'', ''IncorrectPIN'', ''BankAccountVerified'', ''LegalNameChanged'', ''LegalAddressChanged'', ''CompanyBankAccountChange'', ''NOC'', ''ReversalReturn'', ''FeeReturn'', ''ERRefundReturn'', ''CBAVerifyReturn'', ''DDDebitReturn'', ''NSF'', ''DDReject'', ''PayrollReceived'', ''FirstPayrollReceived'', ''ZeroPayrollReceived'', ''PayrollRejected'', ''CompanyContactEmailChanged'', ''TaxExemptStatusChanged'', ''CustomerSignedUp'', ''PayrollCancelPending'', ''FeeRefunded'', ''CoaFeeAccountChange'', ''CoaSalesTaxAccountChange'', ''EmailAddressChanged'', ''ManualRedebitCreated'', ''LastChanceNotify'', ''NonAchPaymentReceived'', ''DBANameChanged'', ''CompanyContactRoleChanged'', ''CompanyContactPhoneChanged'', ''CompanyContactAddressChanged'', ''CompanyFundingModelChanged'', ''PayrollAdminChanged'', ''QuickBooksInfoChanged'', ''WireExpected'', ''EmployeePaidEvenDollarAmount'', ''NumberOfPayrollsPerDayExceeded'', ''EmployeePaidGreaterThanMax'', ''TotalPayrollExceedsLimit'', ''CurrentPayrollPercentageIncrease'', ''SingleEmployeePercentageIncrease'', ''PayrollProcessedTooSoon'', ''CompanyMatchesFraudulentCompany'', ''FraudFlagRemovedEvent'', ''SalesTaxReturn'', ''FeeOffloaded'', ''RedebitAmountUpdated'', ''RedebitDateUpdated'', ''AS400Event'', ''PayrollRecalled'', ''PaycheckRecalled'', ''ChangeRedebitToWireExpected'', ''FeeRebilled'', ''ManualNoteEvent'', ''PINReset'', ''KeyPairGenerated'', ''HigherTokenSynced'', ''AuthenticationFailed'', ''NOCWithOutChanges''
, ''CompanyIndustryTypeChanged'', ''PrimaryPrincipalSSNChanged'', ''PrimaryPrincipalDOBChanged'', ''EnrollmentStatusChanged'', ''StateIdModified'', ''TaxPaymentStatusChanged'', ''PaymentMethodChanged'', ''DepositFrequencyChanged'', ''ThresholdExceeded'', ''AssistedEmployeeMigrationComplete'', ''OfferingUpdated'', ''OfferClaimed'', ''OfferRemoved'', ''EmployeeInTermedCompany'', ''EmployeeBankAccountInTermedCompany'', ''PrefundingReceived'', ''EmployeePaidTooManyTimes'', ''EmployeePaidPercentageGreaterThanOthers'', ''EmployeesPaidToSameBank'', ''EmployeeBankAccountChangedSpikeInPay'', ''EmployeesPaidToSameBankAccount'', ''InvalidEmployeeInformation'', ''PINUnlocked'', ''Employee401kDataUploaded'', ''EmployeeBankAccountChange'', ''TotalBillPaymentExceedsLimit'', ''PayeePaidGreaterThanMax'', ''PayeePaidTooManyTimes'', ''BillPaymentReceived'', ''InvalidPaycheckInformation'', ''VoidedPaycheckAlreadyOffloadedToTOK'', ''DeletedPaycheckAlreadyOffloadedToTOK'', ''TOKNotifiedOfCompanyFraud'', ''BillPaymentOffloaded'', ''PreOffload401kValidationAlert'', ''BillPaymentRecalled'', ''NonPrintChecks'', ''PayrollReceivedCloud'', ''PostOffload401kValidationAlert'', ''InvalidSourceSystemTransmissionInformation'', ''CloudResponse'', ''InactivityDDPayrollAmountExceeded'', ''InactivityBPPayrollAmountExceeded'', ''AssistedPayrollItemMigrationComplete'', ''ERPayableRefundCreated'', ''MultipleCompanyLawsCreated'', ''BalanceFileReceived'', ''CompanyLawUpdated'', ''PayrollReceivedPayCard'', ''LiabilityAdjustmentCreated'', ''PSPToAS400HoldSync'', ''PSPToAS400HoldRemoveSync'', ''OFXServiceActivated'', ''PositiveCobraReceived'', ''ManualLedgerEntry'', ''PayrollTaxPaymentVoided'', ''PayrollTaxPaymentReissued'', ''ERPayableAppliedToBalanceDue'', ''AIDUpdated'', ''AssistedFailedEnrollment'', ''AssistedPayrollConfirmation'', ''EntitlementStateChanged'', ''EntitlementUnitStatusChanged'', ''SourceCompanyIdChanged'', ''ServiceKeyUpdated'', ''EntitlementCodeChanged'', ''PriceTypeChanged'', ''EntitlementCommunication'', ''ManualDataSync'', ''AccountingFinancialLedgerAdjustmentCreated'', ''SUIEoqDebitCreated'', ''SUIEoqCreditCreated'', ''SUIImmediateDebitCreated'', ''SUIImmediateCreditCreated'', ''CompanyContactAdded'', ''CompanyContactJobTitleChanged'', ''PayrollSubmissionIncludedAllNewEmployees''
, ''PSIDMismatch'', ''CompanyContactDeleted'', ''CompanyContactNameChanged'', ''CompanyContactFaxChanged'', ''ERPenaltiesAndInterestRefundCreated'', ''ERPenaltiesAndInterestRefundDebitCreated'', ''EntitlementUnitAdded'', ''UsageBilling25DaysIntoSubscription'', ''UsageBilling15DaysIntoSubscription'', ''CreditReduction'', ''RequestProcessingFlagChanged'', ''PayrollSubmittedWithEmployeeWithPendingReturn'', ''DuplicatePayrollItemReceived'', ''VmpSignUpEmployeeEmail'', ''VmpSignUpEmployerEmail'', ''ACHEnrollmentStatusChanged'', ''PaystubCreated'', ''AdditionalFilingAmount'', ''BPIncreasePayrollLimit'', ''AllowTransmissionsFlagChanged'', ''MonthlyFeeCreated'', ''EmployeeAdded'', ''EmployeeDeleted'', ''EmployeeUpdated'', ''PayeeAdded'', ''PayeeUpdated'', ''DDMigration'')';

   FOR rec in ( SELECT CONSTRAINT_NAME,SEARCH_CONDITION FROM USER_CONSTRAINTS  WHERE CONSTRAINT_TYPE='C' AND CONSTRAINT_NAME LIKE 'C_%' AND TABLE_NAME = 'PSP_COMPANY_EVENT')
    LOOP 
      v_text := replace(replace(rec.search_condition, chr(13),''), chr(10), '');
      in_text := replace(replace(in_text, chr(13),''), chr(10), '');
      v_column_name := substr(rec.search_condition,1, instr(rec.search_condition,' ')-1);
      IF((v_column_name = 'EVENT_TYPE_CD') AND (v_text != in_text)) THEN 
        BEGIN 
          query1 := 'ALTER TABLE PSP_COMPANY_EVENT DROP CONSTRAINT '||rec.CONSTRAINT_NAME;
          query2 := 'ALTER TABLE PSP_COMPANY_EVENT ADD CONSTRAINT '||rec.CONSTRAINT_NAME||' CHECK('||in_text||') NOValidate';

dbms_output.put_line('Query1 -->'||query1);
          EXECUTE IMMEDIATE query1;
          EXECUTE IMMEDIATE query2;

        EXCEPTION 
          WHEN OTHERS THEN RAISE; 
        END; 
      END IF; 
    END LOOP; 
END

PROMPT finished DBUpgrade_002.017.011.002.sql
