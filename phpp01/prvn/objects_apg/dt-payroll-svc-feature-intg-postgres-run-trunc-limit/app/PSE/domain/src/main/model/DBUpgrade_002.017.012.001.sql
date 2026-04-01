--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Table TMP_PSP_ENTITLEMENT_CD_OF;
CREATE TABLE TMP_PSP_ENTITLEMENT_CD_OF
(
  ENTITLEMENT_CODE_OFFERING_SEQ  VARCHAR2(255 CHAR),
  CREATED_DATE                   TIMESTAMP(6),
  MODIFIED_DATE                  TIMESTAMP(6),
  SERVICE_CD                     VARCHAR2(255 CHAR),
  EFFECTIVE_DATE                 TIMESTAMP(6),
  OFFERING_FK                    VARCHAR2(255 CHAR),
  ENTITLEMENT_CODE_FK            VARCHAR2(255 CHAR),
  PRICE_TYPE                     VARCHAR2(255 CHAR),
  IS_DEFAULT                     NUMBER(1),
  VERSION                        NUMBER(19)
)
NOPARALLEL;

Prompt Constraint C_PSP_BATCH_JOB_SETUP0;
ALTER TABLE PSP_BATCH_JOB_SETUP
 DROP CONSTRAINT C_PSP_BATCH_JOB_SETUP0;

ALTER TABLE PSP_BATCH_JOB_SETUP
 ADD CONSTRAINT C_PSP_BATCH_JOB_SETUP0
  CHECK (JOB_TYPE IN('EmployeePayrollItemTotalsCalcProcess', 'SendW2AnnualDataToTFSMonitor', 'SendW2PreviewDataToTFSProcessor', 'SendW2PreviewDataToTFSMonitor', 'SUICreditsBatchJob', 'AnnualBillingMonitor', 'SalesTaxExceptionMonitor', 'EnrollmentDeleteSelectionProcessor', 'BRMUsageErrorFileProcessor', 'EmployeeTotalsCalculationProcess', 'EmployeeTotalsCalculationMonitor', 'AchReturnsMonitor', 'EdiPaymentMonitor', 'AchTransactionsMonitor', 'BalanceFileMonitor', 'EmailGateway', 'EmailGatewayMonitor', 'FeeEvents', 'FeeEventsMonitor', 'FraudPayrolls', 'FraudPayrollsMonitor', 'GemsAccountsReceivable', 'GemsAccountsReceivableMonitor', 'GemsGeneralLedger', 'GemsGeneralLedgerMonitor', 'LedgerBalance', 'MissedPayrollsMonitor', 'MissedTransactionsMonitor', 'NightlyBatchJobs', 'NightlyBatchJobsMonitor', 'PrimaryAchOffloadMonitor', 'PrimaryDailyBatchJobs', 'PrimaryDailyBatchJobsMonitor', 'ScheduledAchOffloadMonitor', 'ScheduledDailyBatchJobs', 'ScheduledDailyBatchJobsMonitor', 'As400EventSync', 'GemsGeneralLedgerUpload', 'GemsGeneralLedgerUploadMonitor', 'AchOffloadCompleteMonitor', 'EventsGateway', 'RAFWriter', 'EftpsEnrollmentsAgeOutMonitor', 'EftpsPaymentMonitor', 'PSPToAs400DataSyncMonitor', 'EftpsEnrollmentsAgeOut', 'EftpsPayment', 'EftpsEnrollments', 'OFACReportProcessor', 'AMLReportProcessor', 'IndustryReportProcessor', 'OFACReportMonitor', 'AMLReportMonitor', 'IndustryReportMonitor', 'ATFDataExtract', 'TaxPaymentSubmission', 'TaxPaymentSynchronization', 'TriggerAmendments', 'HPDEBatchProcessor', 'SalesTaxExceptionProcessor', 'TPSUReportProcessor', 'TPSUReportMonitor', 'ATFDepositFrequencyExtract', 'PrimaryDailyForecast', 'CheckPrint', 'CheckPrintMonitor', 'TaxCreditsEchoSignMonitor', 'OffloadedTransactionsEvents', 'OffloadedTransactionsEventsMonitor', 'ThirdParty401kOffload', 'ThirdParty401kSignup', 'ThirdParty401kValidation', 'ThirdParty401kOffloadMonitor', 'ThirdParty401kSignupMonitor', 'ThirdParty401kValidationMonitor', 'As400DataSync', 'As400DataSyncMonitor', 'TaxCreditsEchoSign', 'QbdtUnprocessedRequestsRetry', 'EftpsEnrollmentsMonitor', 'IOPDataSync', 'PSPToAs400DataSync', 'IOPDataSyncMonitor', 'ATFCompanyLiabilityExtract', 'ATFCompanyPaymentExtract', 'AchTaxPaymentOffloadMonitor', 'PrintedCheckBatch'
, 'PrintedCheckBatchMonitor', 'AchTaxPaymentOffload', 'AchZeroPayments', 'AchZeroPaymentsMonitor', 'ReconPlus', 'ReconPlusMonitor', 'StateReportMonitor', 'StateReport', 'StateCouponMonitor', 'StateCoupon', 'PSPToAs400', 'PSPToAs400Monitor', 'AMOMessageProcessorMonitor', 'EftpsResponse', 'EntitlementProcessor', 'EntitlementProcessorMonitor', 'AS400EventSyncMonitor', 'AMOMessageProcessor', 'EftpsResponseMonitor', 'EftpsSend', 'EftpsSendMonitor', 'EdiResponse', 'EdiResponseMonitor', 'EdiSend', 'EdiSendMonitor', 'EdiPayment', 'AchDebitOffload', 'AchDebitOffloadMonitor', 'EoqSUIAdjustments', 'EoqSUIAdjustmentsMonitor', 'EMSBSToBRMDataSyncProcessor', 'IRSDepositFrequencyFileProcessorMonitor', 'IRSDepositFrequencyFileProcessor', 'PSPToEMSBSDataSyncProcessor', 'MonthlyFee', 'MonthlyFeeMonitor', 'ATFWageLimitsExtract', 'ATFCompanyInfoExtract', 'ATFEmployeeInfoExtract', 'ATFEmployeeTotalsExtract', 'ATFCompanyTaxExtract', 'ATFCompanyTaxRateExtract', 'ATFEmployeeTotalsCalculation', 'EmployeeW2TotalsCalculationMonitor', 'EmployeeW2TotalsCalculationProcessor', 'SendW2AnnualDataToTFSProcessor', 'FsetFilingProcessor', 'FsetFilingMonitor', 'FsetResponseProcessor', 'FsetResponseMonitor', 'ScheduledEmails', 'LedgerBalanceMonitor', 'LedgerOperations', 'W2CountsExtract', 'WorkersCompProcessor', 'WorkersCompMonitor', 'AnnualBillingProcessor', 'SendMonthlyDataToTFSMonitor', 'SendMonthlyDataToTFSProcessor', 'ACHDeEnrollmentBatchJob', 'ACHEnrollmentResponseBatchJob', 'ACHEnrollmentBatchJob', 'IamEmailAddressMonitor', 'IamEmailAddressProcessor', 'EnrollmentDeleteSelectionMonitor', 'DDMAchOffloadMonitor', 'DDMDailyBatchJobs', 'DataPartitionProcessor'));

Prompt Constraint C_PSP_BATCH_JOB_STATUS0;
ALTER TABLE PSP_BATCH_JOB_STATUS
 DROP CONSTRAINT C_PSP_BATCH_JOB_STATUS0;

ALTER TABLE PSP_BATCH_JOB_STATUS
 ADD CONSTRAINT C_PSP_BATCH_JOB_STATUS0
  CHECK (JOB_TYPE IN('EmployeePayrollItemTotalsCalcProcess', 'SendW2AnnualDataToTFSMonitor', 'SendW2PreviewDataToTFSProcessor', 'SendW2PreviewDataToTFSMonitor', 'SUICreditsBatchJob', 'AnnualBillingMonitor', 'SalesTaxExceptionMonitor', 'EnrollmentDeleteSelectionProcessor', 'BRMUsageErrorFileProcessor', 'EmployeeTotalsCalculationProcess', 'EmployeeTotalsCalculationMonitor', 'AchReturnsMonitor', 'EdiPaymentMonitor', 'AchTransactionsMonitor', 'BalanceFileMonitor', 'EmailGateway', 'EmailGatewayMonitor', 'FeeEvents', 'FeeEventsMonitor', 'FraudPayrolls', 'FraudPayrollsMonitor', 'GemsAccountsReceivable', 'GemsAccountsReceivableMonitor', 'GemsGeneralLedger', 'GemsGeneralLedgerMonitor', 'LedgerBalance', 'MissedPayrollsMonitor', 'MissedTransactionsMonitor', 'NightlyBatchJobs', 'NightlyBatchJobsMonitor', 'PrimaryAchOffloadMonitor', 'PrimaryDailyBatchJobs', 'PrimaryDailyBatchJobsMonitor', 'ScheduledAchOffloadMonitor', 'ScheduledDailyBatchJobs', 'ScheduledDailyBatchJobsMonitor', 'As400EventSync', 'GemsGeneralLedgerUpload', 'GemsGeneralLedgerUploadMonitor', 'AchOffloadCompleteMonitor', 'EventsGateway', 'RAFWriter', 'EftpsEnrollmentsAgeOutMonitor', 'EftpsPaymentMonitor', 'PSPToAs400DataSyncMonitor', 'EftpsEnrollmentsAgeOut', 'EftpsPayment', 'EftpsEnrollments', 'OFACReportProcessor', 'AMLReportProcessor', 'IndustryReportProcessor', 'OFACReportMonitor', 'AMLReportMonitor', 'IndustryReportMonitor', 'ATFDataExtract', 'TaxPaymentSubmission', 'TaxPaymentSynchronization', 'TriggerAmendments', 'HPDEBatchProcessor', 'SalesTaxExceptionProcessor', 'TPSUReportProcessor', 'TPSUReportMonitor', 'ATFDepositFrequencyExtract', 'PrimaryDailyForecast', 'CheckPrint', 'CheckPrintMonitor', 'TaxCreditsEchoSignMonitor', 'OffloadedTransactionsEvents', 'OffloadedTransactionsEventsMonitor', 'ThirdParty401kOffload', 'ThirdParty401kSignup', 'ThirdParty401kValidation', 'ThirdParty401kOffloadMonitor', 'ThirdParty401kSignupMonitor', 'ThirdParty401kValidationMonitor', 'As400DataSync', 'As400DataSyncMonitor', 'TaxCreditsEchoSign', 'QbdtUnprocessedRequestsRetry', 'EftpsEnrollmentsMonitor', 'IOPDataSync', 'PSPToAs400DataSync', 'IOPDataSyncMonitor', 'ATFCompanyLiabilityExtract', 'ATFCompanyPaymentExtract', 'AchTaxPaymentOffloadMonitor', 'PrintedCheckBatch'
, 'PrintedCheckBatchMonitor', 'AchTaxPaymentOffload', 'AchZeroPayments', 'AchZeroPaymentsMonitor', 'ReconPlus', 'ReconPlusMonitor', 'StateReportMonitor', 'StateReport', 'StateCouponMonitor', 'StateCoupon', 'PSPToAs400', 'PSPToAs400Monitor', 'AMOMessageProcessorMonitor', 'EftpsResponse', 'EntitlementProcessor', 'EntitlementProcessorMonitor', 'AS400EventSyncMonitor', 'AMOMessageProcessor', 'EftpsResponseMonitor', 'EftpsSend', 'EftpsSendMonitor', 'EdiResponse', 'EdiResponseMonitor', 'EdiSend', 'EdiSendMonitor', 'EdiPayment', 'AchDebitOffload', 'AchDebitOffloadMonitor', 'EoqSUIAdjustments', 'EoqSUIAdjustmentsMonitor', 'EMSBSToBRMDataSyncProcessor', 'IRSDepositFrequencyFileProcessorMonitor', 'IRSDepositFrequencyFileProcessor', 'PSPToEMSBSDataSyncProcessor', 'MonthlyFee', 'MonthlyFeeMonitor', 'ATFWageLimitsExtract', 'ATFCompanyInfoExtract', 'ATFEmployeeInfoExtract', 'ATFEmployeeTotalsExtract', 'ATFCompanyTaxExtract', 'ATFCompanyTaxRateExtract', 'ATFEmployeeTotalsCalculation', 'EmployeeW2TotalsCalculationMonitor', 'EmployeeW2TotalsCalculationProcessor', 'SendW2AnnualDataToTFSProcessor', 'FsetFilingProcessor', 'FsetFilingMonitor', 'FsetResponseProcessor', 'FsetResponseMonitor', 'ScheduledEmails', 'LedgerBalanceMonitor', 'LedgerOperations', 'W2CountsExtract', 'WorkersCompProcessor', 'WorkersCompMonitor', 'AnnualBillingProcessor', 'SendMonthlyDataToTFSMonitor', 'SendMonthlyDataToTFSProcessor', 'ACHDeEnrollmentBatchJob', 'ACHEnrollmentResponseBatchJob', 'ACHEnrollmentBatchJob', 'IamEmailAddressMonitor', 'IamEmailAddressProcessor', 'EnrollmentDeleteSelectionMonitor', 'DDMAchOffloadMonitor', 'DDMDailyBatchJobs', 'DataPartitionProcessor'));

Prompt Constraint C_PSP_PAYROLL_RUN_ACTION0;
ALTER TABLE PSP_PAYROLL_RUN_ACTION
 MODIFY CONSTRAINT C_PSP_PAYROLL_RUN_ACTION0
 VALIDATE;

Prompt Constraint C_PSP_PAYROLL_RUN1;
ALTER TABLE PSP_PAYROLL_RUN
 MODIFY CONSTRAINT C_PSP_PAYROLL_RUN1
 VALIDATE;

Prompt Constraint C_PSP_PAYCHECK0;
ALTER TABLE PSP_PAYCHECK
 MODIFY CONSTRAINT C_PSP_PAYCHECK0
 VALIDATE;

PROMPT finished DBUpgrade_002.017.012.001.sql