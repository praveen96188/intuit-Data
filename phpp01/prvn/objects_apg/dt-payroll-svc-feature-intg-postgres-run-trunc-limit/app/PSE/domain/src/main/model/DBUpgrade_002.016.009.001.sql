PROMPT start DBUpgrade_002.016.009.001.sql

DECLARE 
v_text             varchar2(8000); 
v_column_name      varchar2(250); 
in_text            varchar2(8000); 
query1             varchar2(8000); 
query2             varchar2(8000); 
BEGIN 
  in_text := 'JOB_TYPE IN(''EmployeePayrollItemTotalsCalcProcess'', ''SendW2AnnualDataToTFSMonitor'', ''SendW2PreviewDataToTFSProcessor'', ''SendW2PreviewDataToTFSMonitor'', ''SUICreditsBatchJob'', ''AnnualBillingMonitor'', ''SalesTaxExceptionMonitor'', ''EnrollmentDeleteSelectionProcessor'', ''BRMUsageErrorFileProcessor'', ''EmployeeTotalsCalculationProcess'', ''EmployeeTotalsCalculationMonitor'', ''AchReturnsMonitor'', ''EdiPaymentMonitor'', ''AchTransactionsMonitor'', ''BalanceFileMonitor'', ''EmailGateway'', ''EmailGatewayMonitor'', ''FeeEvents'', ''FeeEventsMonitor'', ''FraudPayrolls'', ''FraudPayrollsMonitor'', ''GemsAccountsReceivable'', ''GemsAccountsReceivableMonitor'', ''GemsGeneralLedger'', ''GemsGeneralLedgerMonitor'', ''LedgerBalance'', ''MissedPayrollsMonitor'', ''MissedTransactionsMonitor'', ''NightlyBatchJobs'', ''NightlyBatchJobsMonitor'', ''PrimaryAchOffloadMonitor'', ''PrimaryDailyBatchJobs'', ''PrimaryDailyBatchJobsMonitor'', ''ScheduledAchOffloadMonitor'', ''ScheduledDailyBatchJobs'', ''ScheduledDailyBatchJobsMonitor'', ''As400EventSync'', ''GemsGeneralLedgerUpload'', ''GemsGeneralLedgerUploadMonitor'', ''AchOffloadCompleteMonitor'', ''EventsGateway'', ''RAFWriter'', ''EftpsEnrollmentsAgeOutMonitor'', ''EftpsPaymentMonitor'', ''PSPToAs400DataSyncMonitor'', ''EftpsEnrollmentsAgeOut'', ''EftpsPayment'', ''EftpsEnrollments'', ''OFACReportProcessor'', ''AMLReportProcessor'', ''IndustryReportProcessor'', ''OFACReportMonitor'', ''AMLReportMonitor'', ''IndustryReportMonitor'', ''ATFDataExtract'', ''TaxPaymentSubmission'', ''TaxPaymentSynchronization'', ''TriggerAmendments'', ''HPDEBatchProcessor'', ''SalesTaxExceptionProcessor'', ''ATFDepositFrequencyExtract'', ''PrimaryDailyForecast'', ''CheckPrint'', ''CheckPrintMonitor'', ''TaxCreditsEchoSignMonitor'', ''OffloadedTransactionsEvents'', ''OffloadedTransactionsEventsMonitor'', ''ThirdParty401kOffload'', ''ThirdParty401kSignup'', ''ThirdParty401kValidation'', ''ThirdParty401kOffloadMonitor'', ''ThirdParty401kSignupMonitor'', ''ThirdParty401kValidationMonitor'', ''As400DataSync'', ''As400DataSyncMonitor'', ''TaxCreditsEchoSign'', ''QbdtUnprocessedRequestsRetry'', ''EftpsEnrollmentsMonitor'', ''IOPDataSync'', ''PSPToAs400DataSync''
, ''IOPDataSyncMonitor'', ''ATFCompanyLiabilityExtract'', ''ATFCompanyPaymentExtract'', ''AchTaxPaymentOffloadMonitor'', ''PrintedCheckBatch'', ''PrintedCheckBatchMonitor'', ''AchTaxPaymentOffload'', ''AchZeroPayments'', ''AchZeroPaymentsMonitor'', ''ReconPlus'', ''ReconPlusMonitor'', ''StateReportMonitor'', ''StateReport'', ''StateCouponMonitor'', ''StateCoupon'', ''PSPToAs400'', ''PSPToAs400Monitor'', ''AMOMessageProcessorMonitor'', ''EftpsResponse'', ''EntitlementProcessor'', ''EntitlementProcessorMonitor'', ''AS400EventSyncMonitor'', ''AMOMessageProcessor'', ''EftpsResponseMonitor'', ''EftpsSend'', ''EftpsSendMonitor'', ''EdiResponse'', ''EdiResponseMonitor'', ''EdiSend'', ''EdiSendMonitor'', ''EdiPayment'', ''AchDebitOffload'', ''AchDebitOffloadMonitor'', ''EoqSUIAdjustments'', ''EoqSUIAdjustmentsMonitor'', ''EMSBSToBRMDataSyncProcessor'', ''IRSDepositFrequencyFileProcessorMonitor'', ''IRSDepositFrequencyFileProcessor'', ''PSPToEMSBSDataSyncProcessor'', ''MonthlyFee'', ''MonthlyFeeMonitor'', ''ATFWageLimitsExtract'', ''ATFCompanyInfoExtract'', ''ATFEmployeeInfoExtract'', ''ATFEmployeeTotalsExtract'', ''ATFCompanyTaxExtract'', ''ATFCompanyTaxRateExtract'', ''ATFEmployeeTotalsCalculation'', ''EmployeeW2TotalsCalculationMonitor'', ''EmployeeW2TotalsCalculationProcessor'', ''SendW2AnnualDataToTFSProcessor'', ''FsetFilingProcessor'', ''FsetFilingMonitor'', ''FsetResponseProcessor'', ''FsetResponseMonitor'', ''ScheduledEmails'', ''LedgerBalanceMonitor'', ''LedgerOperations'', ''W2CountsExtract'', ''WorkersCompProcessor'', ''WorkersCompMonitor'', ''AnnualBillingProcessor'', ''SendMonthlyDataToTFSMonitor'', ''SendMonthlyDataToTFSProcessor'', ''ACHDeEnrollmentBatchJob'', ''ACHEnrollmentResponseBatchJob'', ''ACHEnrollmentBatchJob'', ''IamEmailAddressMonitor'', ''IamEmailAddressProcessor'', ''EnrollmentDeleteSelectionMonitor'')';

    FOR rec in ( SELECT CONSTRAINT_NAME,SEARCH_CONDITION FROM USER_CONSTRAINTS  WHERE CONSTRAINT_TYPE='C' AND CONSTRAINT_NAME LIKE 'C_%' AND TABLE_NAME = 'PSP_BATCH_JOB_STATUS')
    LOOP 
      v_text := replace(replace(rec.search_condition, chr(13),''), chr(10), '');
      in_text := replace(replace(in_text, chr(13),''), chr(10), '');
      v_column_name := substr(rec.search_condition,1, instr(rec.search_condition,' ')-1);
      IF((v_column_name = 'JOB_TYPE') AND (v_text != in_text)) THEN 
        BEGIN 
          query1 := 'ALTER TABLE PSP_BATCH_JOB_STATUS DROP CONSTRAINT '||rec.CONSTRAINT_NAME;
          query2 := 'ALTER TABLE PSP_BATCH_JOB_STATUS ADD CONSTRAINT '||rec.CONSTRAINT_NAME||' CHECK('||in_text||') NOValidate';

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

PROMPT finished DBUpgrade_002.016.009.001.sql