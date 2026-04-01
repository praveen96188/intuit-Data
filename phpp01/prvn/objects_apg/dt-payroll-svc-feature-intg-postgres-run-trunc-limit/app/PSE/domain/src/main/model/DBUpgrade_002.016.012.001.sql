
PROMPT finished DBUpgrade_002.016.012.001.sql
/
DECLARE 
v_text             varchar2(8000); 
v_column_name      varchar2(250); 
in_text            varchar2(8000); 
query1             varchar2(8000); 
query2             varchar2(8000); 
BEGIN 
  in_text := 'PARAM_TYPE_CD IN(''VendorInvalidEmailAddress'', ''SUICreditAmount'', ''PaymentAmount'', ''ServiceType'', ''ExtensionKey'', ''AdjustedPayrollDebitAmount'', ''BilledFeeList'', ''CompanyBalanceDue'', ''CompanyBankAccountLastFour'', ''CompanyID'', ''CompanyLegalName'', ''CurrentEmail'', ''EffectiveCreditPostingDate'', ''EmailFirstName'', ''EmailLastName'', ''EmployeeFirstName'', ''EmployeeLastNameFirstInitial'', ''EmployeeList'', ''FailureReason'', ''IntuitHandlingFee'', ''NextBusinessDate'', ''NonPayrollFeeAmount'', ''NonPayrollFeeSettlementDate'', ''NonPayrollFeeType'', ''NumberOfStrikes'', ''PayrollRunTime'', ''PrimaryPrincipalEmail'', ''PrimaryPrincipalFirstName'', ''PrimaryPrincipalLastName'', ''PriorEmail'', ''RedebitCompletedDate'', ''RedebitSettlementDate'', ''RefundedFeeList'', ''ReversalFailedList'', ''ReversalPendingList'', ''ReversalSuccessfulList'', ''SourcePayrollSystem'', ''TodaysDate'', ''TodaysDatePlus14CalendarDays'', ''WireExpectedDate'', ''PaycheckSettlementDate'', ''PayrollAdminEmail'', ''PayrollAdminFirstName'', ''PayrollAdminLastName'', ''PayrollCancelDate'', ''PayrollDebitAmount'', ''PayrollDebitSettlementDate'', ''PayrollRunDate'', ''CompanyDBAName'', ''BillingContactName'', ''CompanyEIN'', ''PayPeriodBeginDate'', ''PayPeriodEndDate'', ''EmployeeLastName'', ''Four01kTransmissionDate'', ''VoidOrDelete'', ''HoldReason'', ''ReferenceNumber'', ''VendorPaymentList'', ''Memo'', ''VendorAccountNumber'', ''VendorBankAccountLastFour'', ''ServiceKey'', ''LicenseNumber'', ''AgreementNumber'', ''LawId'', ''Quarter'', ''Year'', ''DebitSettlementDate'', ''Amount'', ''CustomerAccountNnumber'', ''SubTypeDescription'', ''SubscriptionStartDate'', ''RecipientEmail'', ''RecipientFirstName'', ''RecipientLastName'', ''PayeeList'')';

    FOR rec in ( SELECT CONSTRAINT_NAME,SEARCH_CONDITION FROM USER_CONSTRAINTS  WHERE CONSTRAINT_TYPE='C' AND CONSTRAINT_NAME LIKE 'C_%' AND TABLE_NAME = 'PSP_COMPANY_EVENT_EMAIL_PARAM')
    LOOP 
      v_text := replace(replace(rec.search_condition, chr(13),''), chr(10), '');
      in_text := replace(replace(in_text, chr(13),''), chr(10), '');
      v_column_name := substr(rec.search_condition,1, instr(rec.search_condition,' ')-1);
      IF((v_column_name = 'PARAM_TYPE_CD') AND (v_text != in_text)) THEN 
        BEGIN 
          query1 := 'ALTER TABLE PSP_COMPANY_EVENT_EMAIL_PARAM DROP CONSTRAINT '||rec.CONSTRAINT_NAME;
          query2 := 'ALTER TABLE PSP_COMPANY_EVENT_EMAIL_PARAM ADD CONSTRAINT '||rec.CONSTRAINT_NAME||' CHECK('||in_text||') NOValidate';

dbms_output.put_line('Query1 -->'||query1);
          EXECUTE IMMEDIATE query1;
          EXECUTE IMMEDIATE query2;

        EXCEPTION 
          WHEN OTHERS THEN RAISE; 
        END; 
      END IF; 
    END LOOP; 
END; 
/
DECLARE 
v_text             varchar2(8000); 
v_column_name      varchar2(250); 
in_text            varchar2(8000); 
query1             varchar2(8000); 
query2             varchar2(8000); 
BEGIN 
  in_text := 'EMAIL_TEMPLATE_TYPE_CD IN(''AdditionalMedicareTaxDebitNotification'', ''VendorInvalidEmail'', ''EmployerNOC52LoanAccount'', ''SameDayMoFedAssessmentDebit'', ''SUICreditNotification'', ''SymphonyWelcomeOneMonthReactivation'', ''SameDayNVBondDebitNotification'', ''BulkCreditDebitNotification'', ''ServiceCancelledConfirmation1'', ''AssistedFailedEnrollment'', ''BulkCreditDebitNotificationSUPNY'', ''AssistedPayrollConfirmation'', ''AllPaycheckReversalsFailed'', ''AllPaycheckReversalsSuccessful'', ''AutoRedebit'', ''AutoRedebitFourStrikes'', ''BankVerificationFailed'', ''BilledNonPayrollRelatedFee'', ''CustomerInitiatedDDReversal'', ''DDBankVerificationReminder'', ''DDBankVerificationSuccessful'', ''DDERBankAccountChange'', ''DDPINChangeConfirmation'', ''DDServiceCancelledConfirmation'', ''DDSignupConfirmation'', ''ERandEENOC2'', ''LastChanceEmail'', ''ManualRedebit'', ''NonACHPaymentReceivedInFull'', ''NonACHPaymentReceivedInFullActionRequired'', ''NonACHPaymentReceivedLiabilityOutstanding'', ''PartialPaycheckReversal'', ''PayrollCancellationNotification'', ''DebitReturned'', ''DebitReturnedFourStrikes'', ''EEDDREJECT'', ''EmailChangeNotification'', ''EmployeeNOC'', ''EmployeeNOC2'', ''EmployerNOC'', ''ERandEENOC'', ''PayrollCancelledNotification'', ''QBDTPayrollConfirmation'', ''RedebitFailed'', ''RefundedFeeAmount'', ''RefundWithRebillFeeAmount'', ''WireExpectedNotification'', ''EFTPSEnrollmentRejectedEIN'', ''EFTPSEnrollmentRejectedName'', ''BankVerifyAttemptFailed'', ''DDEEBankAccountChange'', ''TOKFraudNotification'', ''TOKVoidDelete'', ''VendorPaymentSignupConfirmation'', ''VendorPaymentReceived'', ''VendorPaymentOffloaded1'', ''ManualRedebit2'', ''DebitReturnedFourStrikes3'', ''WireExpectedNotification3'', ''DebitReturned3'', ''AutoRedebit2'', ''LastChanceEmail3'', ''PayrollCancellationNotification2'', ''PayrollCancelledNotification2'', ''NonPrintChecks'', ''Correct401kEmployeeInfo'', ''Correct401kEmployeeInfoAfterSend'', ''VendorPaymentReceived1'', ''VendorPaymentOffloadedForWriteChecks'', ''VendorPaymentOffloadedForPayBills'', ''SKDiskDeliveryKey1'', ''SKBasicKey1'', ''SKFreeBasicKey1'', ''SKEnhancedKey1'', ''SKEnhancedKeyAccount1'', ''SKStandardKey1'', ''SKDefaultKey1'', ''SameDaySUIDebitNotification3''
, ''SUIRefundNotification3'', ''EndofQuarterSUIDebitNotification3'', ''DDERBankAccountChangeAssisted'', ''EmployeeNOCAssisted'', ''EmployerNOC1'', ''LastChanceEmail1'', ''LastChanceEmail4'', ''ManualRedebit3'', ''NonACHPaymentReceivedInFull1'', ''NonACHPMTReceivedLiabOutstanding1'', ''PartialPaycheckReversal1'', ''RedebitFailed1'', ''WireExpectedNotification4'', ''AllPaycheckReversalsFailed1'', ''AllPaycheckReversalsSuccessful1'', ''AutoRedebit3'', ''BankVerificationFailed1'', ''BilledNonPayrollRelatedFee1'', ''CustomerInitiatedDDReversal1'', ''DDBankVerificationReminder1'', ''DDPINChangeConfirmation1'', ''DebitReturned1'', ''DebitReturned4'', ''EEDDREJECT1'', ''RefundedFeeAmount1'', ''RefundWithRebillFeeAmount1'', ''DDEEBankAccountChange1'', ''SymphonyWelcomeNoTrial'', ''SymphonyBillingDetailsMonthly'', ''UsageBillingMidTrial'', ''SymphonyWelcomeFreeTrial'', ''CreditReductionGeneric'', ''CreditReductionFUTA'', ''SymphonyBillingDetailsAnnual'', ''FUTACreditReduction'', ''BilledNonPayrollRelatedFee2'', ''SUIRefundNotification4'', ''SameDaySUIDebitNotification4'', ''EndofQuarterSUIDebitNotification4'', ''VmpEmployeeWelcome'', ''VmpEmployerWelcome'', ''VmpPaystubNotification'', ''SameDayMAUHIDebitNotification'', ''MinimumMonthlyBilling'', ''DesktopAMLHoldRemoved'', ''DesktopAMLHoldApplied'', ''DDPayeeBankAccountChange'')';

    FOR rec in ( SELECT CONSTRAINT_NAME,SEARCH_CONDITION FROM USER_CONSTRAINTS  WHERE CONSTRAINT_TYPE='C' AND CONSTRAINT_NAME LIKE 'C_%' AND TABLE_NAME = 'PSP_COMPANY_EVENT_EMAIL')
    LOOP 
      v_text := replace(replace(rec.search_condition, chr(13),''), chr(10), '');
      in_text := replace(replace(in_text, chr(13),''), chr(10), '');
      v_column_name := substr(rec.search_condition,1, instr(rec.search_condition,' ')-1);
      IF((v_column_name = 'EMAIL_TEMPLATE_TYPE_CD') AND (v_text != in_text)) THEN 
        BEGIN 
          query1 := 'ALTER TABLE PSP_COMPANY_EVENT_EMAIL DROP CONSTRAINT '||rec.CONSTRAINT_NAME;
          query2 := 'ALTER TABLE PSP_COMPANY_EVENT_EMAIL ADD CONSTRAINT '||rec.CONSTRAINT_NAME||' CHECK('||in_text||') NOValidate';

dbms_output.put_line('Query1 -->'||query1);
          EXECUTE IMMEDIATE query1;
          EXECUTE IMMEDIATE query2;

        EXCEPTION 
          WHEN OTHERS THEN RAISE; 
        END; 
      END IF; 
    END LOOP; 
END; 
/