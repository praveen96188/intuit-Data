--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Table PSP_ASST_BUNDLE_COMP_USAGE;
CREATE TABLE PSP_ASST_BUNDLE_COMP_USAGE
(
  ASST_BUNDLE_COMP_USAGE_SEQ  VARCHAR2(255 CHAR) NOT NULL,
  VERSION                     NUMBER(19)        NOT NULL,
  CREATOR_ID                  VARCHAR2(30 CHAR),
  CREATED_DATE                TIMESTAMP(6)      NOT NULL,
  MODIFIER_ID                 VARCHAR2(30 CHAR),
  MODIFIED_DATE               TIMESTAMP(6)      NOT NULL,
  REALM_ID                    NUMBER(19)        DEFAULT -1                    NOT NULL,
  SOURCE_COMPANY_ID           VARCHAR2(50 CHAR),
  SOURCE_SYSTEM_CD            VARCHAR2(255 CHAR),
  ENTITLEMENT_ID              VARCHAR2(20 CHAR),
  LICENSE_ID                  VARCHAR2(20 CHAR)
)
NOPARALLEL;

ALTER TABLE PSP_ASST_BUNDLE_COMP_USAGE
 ADD CONSTRAINT C_PSP_ASST_BUNDLE_COMP_USA0
  CHECK (SOURCE_SYSTEM_CD IN('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO'));

ALTER TABLE PSP_ASST_BUNDLE_COMP_USAGE
 ADD PRIMARY KEY
  (ASST_BUNDLE_COMP_USAGE_SEQ, REALM_ID)
  USING INDEX;

Prompt Table PSP_ASSISTED_BUNDLE_BILL;
CREATE TABLE PSP_ASSISTED_BUNDLE_BILL
(
  ASSISTED_BUNDLE_BILL_SEQ   VARCHAR2(255 CHAR) NOT NULL,
  VERSION                    NUMBER(19)         NOT NULL,
  CREATOR_ID                 VARCHAR2(30 CHAR),
  CREATED_DATE               TIMESTAMP(6)       NOT NULL,
  MODIFIER_ID                VARCHAR2(30 CHAR),
  MODIFIED_DATE              TIMESTAMP(6)       NOT NULL,
  REALM_ID                   NUMBER(19)         DEFAULT -1                    NOT NULL,
  BILL_DATE                  TIMESTAMP(6),
  TOTAL_COUNT                NUMBER(10),
  TOTAL_AMOUNT               NUMBER(19,4),
  ASST_STATUS                VARCHAR2(255 CHAR),
  ASST_BUNDLE_COMP_USAGE_FK  VARCHAR2(255 CHAR) NOT NULL
)
NOPARALLEL;

Prompt Index PSP_ASSISTEDBUNDLEBILL_FK1;
CREATE INDEX PSP_ASSISTEDBUNDLEBILL_FK1 ON PSP_ASSISTED_BUNDLE_BILL
(ASST_BUNDLE_COMP_USAGE_FK, REALM_ID)
NOPARALLEL;

ALTER TABLE PSP_ASSISTED_BUNDLE_BILL
 ADD CONSTRAINT C_PSP_ASSISTED_BUNDLE_BILL0
  CHECK (ASST_STATUS IN('Open', 'ProcessingFailed', 'SentToBRM', 'SentToBRMFailed', 'Processed'));

ALTER TABLE PSP_ASSISTED_BUNDLE_BILL
 ADD PRIMARY KEY
  (ASSISTED_BUNDLE_BILL_SEQ, REALM_ID)
  USING INDEX;

Prompt Table PSP_ASST_BUNDLE_BILL_DETAIL;
CREATE TABLE PSP_ASST_BUNDLE_BILL_DETAIL
(
  ASST_BUNDLE_BILL_DETAIL_SEQ  VARCHAR2(255 CHAR) NOT NULL,
  VERSION                      NUMBER(19)       NOT NULL,
  CREATOR_ID                   VARCHAR2(30 CHAR),
  CREATED_DATE                 TIMESTAMP(6)     NOT NULL,
  MODIFIER_ID                  VARCHAR2(30 CHAR),
  MODIFIED_DATE                TIMESTAMP(6)     NOT NULL,
  REALM_ID                     NUMBER(19)       DEFAULT -1                    NOT NULL,
  BILLING_DETAIL_ID            VARCHAR2(4000 CHAR),
  ASSISTED_BUNDLE_BILL_FK      VARCHAR2(255 CHAR) NOT NULL
)
NOPARALLEL;

Prompt Index PSP_ASSTBUNDLEBILLDETAIL_FK1;
CREATE INDEX PSP_ASSTBUNDLEBILLDETAIL_FK1 ON PSP_ASST_BUNDLE_BILL_DETAIL
(ASSISTED_BUNDLE_BILL_FK, REALM_ID)
NOPARALLEL;

ALTER TABLE PSP_ASST_BUNDLE_BILL_DETAIL
 ADD PRIMARY KEY
  (ASST_BUNDLE_BILL_DETAIL_SEQ, REALM_ID)
  USING INDEX;

Prompt Column ASSISTED_USAGE_BILLING_TOKEN;
ALTER TABLE PSP_PAYROLL_RUN
 ADD (ASSISTED_USAGE_BILLING_TOKEN  NUMBER(19) DEFAULT 0 NOT NULL);

Prompt Constraint C_PSP_BATCH_JOB_STATUS0;
ALTER TABLE PSP_BATCH_JOB_STATUS
 DROP CONSTRAINT C_PSP_BATCH_JOB_STATUS0;

ALTER TABLE PSP_BATCH_JOB_STATUS
 ADD CONSTRAINT C_PSP_BATCH_JOB_STATUS0
  CHECK (JOB_TYPE IN('FailedPayrollPlSqlJobsProcessor', 'PayrollFraudBatchPurgePlSqlJobsProcessor', 'PSPEventLogPurgePlSqlJobsProcessor', 'DailyPayrollStatsPlSqlJobsProcessor', 'EFTPSOnHoldPaymentPlSqlJobsProcessor', 'ValidateEmployeeWagePlansPlSqlJobsProcessor', 'EmployeePayrollItemTotalsCalcProcess', 'SendW2AnnualDataToTFSMonitor', 'SendW2PreviewDataToTFSProcessor', 'SendW2PreviewDataToTFSMonitor', 'SUICreditsBatchJob', 'AnnualBillingMonitor', 'SalesTaxExceptionMonitor', 'EnrollmentDeleteSelectionProcessor', 'NCDFixPlSqlJobsProcessor', 'OfferingUpdateUsageBillingPlSqlJobsProcessor', 'BRMUsageErrorFileProcessor', 'EDRAssociationFixPlSqlJobsProcessor', 'RetryEntitlementActivationPlSqlJobsProcessor', 'NCDFixALLPlSqlJobsProcessor', 'CostCoPlSqlJobsProcessor', 'EmployeeTotalsCalculationProcess', 'EmployeeTotalsCalculationMonitor', 'RTBAutomation', 'AchReturnsMonitor', 'EdiPaymentMonitor', 'AchTransactionsMonitor', 'BalanceFileMonitor', 'EmailGateway', 'EmailGatewayMonitor', 'FeeEvents', 'FeeEventsMonitor', 'FraudPayrolls', 'FraudPayrollsMonitor', 'GemsAccountsReceivable', 'GemsAccountsReceivableMonitor', 'GemsGeneralLedger', 'GemsGeneralLedgerMonitor', 'LedgerBalance', 'MissedPayrollsMonitor', 'MissedTransactionsMonitor', 'NightlyBatchJobs', 'NightlyBatchJobsMonitor', 'PrimaryAchOffloadMonitor', 'PrimaryDailyBatchJobs', 'PrimaryDailyBatchJobsMonitor', 'ScheduledAchOffloadMonitor', 'ScheduledDailyBatchJobs', 'ScheduledDailyBatchJobsMonitor', 'As400EventSync', 'GemsGeneralLedgerUpload', 'GemsGeneralLedgerUploadMonitor', 'AchOffloadCompleteMonitor', 'EventsGateway', 'RAFWriter', 'EftpsEnrollmentsAgeOutMonitor', 'EftpsPaymentMonitor', 'PSPToAs400DataSyncMonitor', 'EftpsEnrollmentsAgeOut', 'EftpsPayment', 'EftpsEnrollments', 'OFACReportProcessor', 'AMLReportProcessor', 'IndustryReportProcessor', 'OFACReportMonitor', 'AMLReportMonitor', 'IndustryReportMonitor', 'BRMAssistedUsageErrorFileProcessor', 'ATFDataExtract', 'TaxPaymentSubmission', 'TaxPaymentSynchronization', 'TriggerAmendments', 'HPDEBatchProcessor', 'SalesTaxExceptionProcessor', 'TPSUReportProcessor', 'TPSUReportMonitor', 'ATFDepositFrequencyExtract', 'PrimaryDailyForecast', 'CheckPrint', 'CheckPrintMonitor', 'TaxCreditsEchoSignMonitor'
, 'OffloadedTransactionsEvents', 'OffloadedTransactionsEventsMonitor', 'ThirdParty401kOffload', 'ThirdParty401kSignup', 'ThirdParty401kValidation', 'ThirdParty401kOffloadMonitor', 'ThirdParty401kSignupMonitor', 'ThirdParty401kValidationMonitor', 'AssistedUsageReportingToBRMProcessor', 'AssistedUsageDataSyncProcessor', 'As400DataSync', 'As400DataSyncMonitor', 'TaxCreditsEchoSign', 'SoxDBUserReport', 'QbdtUnprocessedRequestsRetry', 'EftpsEnrollmentsMonitor', 'IOPDataSync', 'PSPToAs400DataSync', 'IOPDataSyncMonitor', 'ATFCompanyLiabilityExtract', 'ATFCompanyPaymentExtract', 'AchTaxPaymentOffloadMonitor', 'PrintedCheckBatch', 'PrintedCheckBatchMonitor', 'AchTaxPaymentOffload', 'AchZeroPayments', 'AchZeroPaymentsMonitor', 'ReconPlus', 'ReconPlusMonitor', 'StateReportMonitor', 'StateReport', 'StateCouponMonitor', 'StateCoupon', 'PSPToAs400', 'PSPToAs400Monitor', 'AMOMessageProcessorMonitor', 'EftpsResponse', 'EntitlementProcessor', 'EntitlementProcessorMonitor', 'AS400EventSyncMonitor', 'AMOMessageProcessor', 'EftpsResponseMonitor', 'EftpsSend', 'EftpsSendMonitor', 'EdiResponse', 'EdiResponseMonitor', 'EdiSend', 'EdiSendMonitor', 'EdiPayment', 'AchDebitOffload', 'AchDebitOffloadMonitor', 'EoqSUIAdjustments', 'EoqSUIAdjustmentsMonitor', 'EMSBSToBRMDataSyncProcessor', 'IRSDepositFrequencyFileProcessorMonitor', 'IRSDepositFrequencyFileProcessor', 'PSPToEMSBSDataSyncProcessor', 'MonthlyFee', 'MonthlyFeeMonitor', 'ATFWageLimitsExtract', 'ATFCompanyInfoExtract', 'ATFEmployeeInfoExtract', 'ATFEmployeeTotalsExtract', 'ATFCompanyTaxExtract', 'ATFCompanyTaxRateExtract', 'ATFEmployeeTotalsCalculation', 'EmployeeW2TotalsCalculationMonitor', 'EmployeeW2TotalsCalculationProcessor', 'SendW2AnnualDataToTFSProcessor', 'FsetFilingProcessor', 'FsetFilingMonitor', 'FsetResponseProcessor', 'FsetResponseMonitor', 'ScheduledEmails', 'LedgerBalanceMonitor', 'LedgerOperations', 'W2CountsExtract', 'WorkersCompProcessor', 'WorkersCompMonitor', 'AnnualBillingProcessor', 'SendMonthlyDataToTFSMonitor', 'SendMonthlyDataToTFSProcessor', 'ACHDeEnrollmentBatchJob', 'ACHEnrollmentResponseBatchJob', 'ACHEnrollmentBatchJob', 'IamEmailAddressMonitor', 'IamEmailAddressProcessor', 'EnrollmentDeleteSelectionMonitor', 'DDMAchOffloadMonitor', 'DDMDailyBatchJobs', 'DataPartitionProcessor'));

Prompt Constraint C_PSP_OFFERING1;
ALTER TABLE PSP_OFFERING
 DROP CONSTRAINT C_PSP_OFFERING1;

ALTER TABLE PSP_OFFERING
 ADD CONSTRAINT C_PSP_OFFERING1
  CHECK (OFFERING_CODE IN('DIYDDYEAREND', 'DIYDDFY143', 'COSTCO69FY16', 'AP79FY16', 'AP79FY14', 'AP79MEFY14', 'AP89FY14', 'PAP75FY14', 'DIYDDFY14', 'SYM3FY14', 'BillPaymentSTD3FY14', 'SYMFY14', 'COSTCO54', 'COSTCO64', 'AP89FY16', 'COSTCO84', 'COSTCO74', 'COSTCO572', 'COSTCO672', 'BillPaymentSTDFY15', 'DIYDDFY15', 'DIYDDFY153', 'AP79FY15', 'AP89FY15', 'AP99FY15', 'AP79MEFY15', 'AP89MEFY15', 'PAP84FY15', 'COSTCO57FY15', 'COSTCO67FY15', 'COSTCO79FY16', 'AP79MEFY16', 'AP89MEFY16', 'AP99FY16', 'AP99MEFY16', 'BillPaymentSTDFY16', 'DIYDDFY16', 'DIYDDFY163', 'PAP84FY16', 'DIYDDSTD', 'DIYDDSTD3', 'QBOEDD', 'CheckDistribution', 'ThirdParty401k', 'BillPaymentSTD3', 'Tax', 'Cloud', 'AssistedBundle', 'RiskAssessment', 'AP69MEFY13', 'PAPAV1142', 'AP63EEEO', 'AP79FY13', 'MAJORACCT', 'APAV115', 'APAV125ME2', 'APAV1352', 'SUP125TEST', 'APDIOCESE', 'APPAP99YR', 'ASST60', 'ASSTAD2P3', 'ASSTEOSUP', 'COSTCO49', 'COSTCO59', 'PAP71FY13', 'PAP582', 'PAP58DD145', 'PAP58DD2', 'AP59ME2', 'AP69DD145', 'AP69DD1502', 'AP59MED145', 'AP692', 'AP69DD2', 'AP69W22', 'UsageBilling', 'SYM1FY13', 'SYM2FY13', 'WorkersComp', 'COSTCO57', 'COSTCO67', 'ViewMyPaycheck', 'PAP67FY13', 'COSTCO52', 'COSTCO62', 'BillPaymentSTD4', 'CloudV2', 'SYMPAPFY14', 'SYMPAP92FY18', 'SYMPAP87FY18', 'PAP92FY18', 'AP109FY18', 'SYM109FY18'));

Prompt Constraint C_PSP_BATCH_JOB_SETUP0;
ALTER TABLE PSP_BATCH_JOB_SETUP
 DROP CONSTRAINT C_PSP_BATCH_JOB_SETUP0;

ALTER TABLE PSP_BATCH_JOB_SETUP
 ADD CONSTRAINT C_PSP_BATCH_JOB_SETUP0
  CHECK (JOB_TYPE IN('FailedPayrollPlSqlJobsProcessor', 'PayrollFraudBatchPurgePlSqlJobsProcessor', 'PSPEventLogPurgePlSqlJobsProcessor', 'DailyPayrollStatsPlSqlJobsProcessor', 'EFTPSOnHoldPaymentPlSqlJobsProcessor', 'ValidateEmployeeWagePlansPlSqlJobsProcessor', 'EmployeePayrollItemTotalsCalcProcess', 'SendW2AnnualDataToTFSMonitor', 'SendW2PreviewDataToTFSProcessor', 'SendW2PreviewDataToTFSMonitor', 'SUICreditsBatchJob', 'AnnualBillingMonitor', 'SalesTaxExceptionMonitor', 'EnrollmentDeleteSelectionProcessor', 'NCDFixPlSqlJobsProcessor', 'OfferingUpdateUsageBillingPlSqlJobsProcessor', 'BRMUsageErrorFileProcessor', 'EDRAssociationFixPlSqlJobsProcessor', 'RetryEntitlementActivationPlSqlJobsProcessor', 'NCDFixALLPlSqlJobsProcessor', 'CostCoPlSqlJobsProcessor', 'EmployeeTotalsCalculationProcess', 'EmployeeTotalsCalculationMonitor', 'RTBAutomation', 'AchReturnsMonitor', 'EdiPaymentMonitor', 'AchTransactionsMonitor', 'BalanceFileMonitor', 'EmailGateway', 'EmailGatewayMonitor', 'FeeEvents', 'FeeEventsMonitor', 'FraudPayrolls', 'FraudPayrollsMonitor', 'GemsAccountsReceivable', 'GemsAccountsReceivableMonitor', 'GemsGeneralLedger', 'GemsGeneralLedgerMonitor', 'LedgerBalance', 'MissedPayrollsMonitor', 'MissedTransactionsMonitor', 'NightlyBatchJobs', 'NightlyBatchJobsMonitor', 'PrimaryAchOffloadMonitor', 'PrimaryDailyBatchJobs', 'PrimaryDailyBatchJobsMonitor', 'ScheduledAchOffloadMonitor', 'ScheduledDailyBatchJobs', 'ScheduledDailyBatchJobsMonitor', 'As400EventSync', 'GemsGeneralLedgerUpload', 'GemsGeneralLedgerUploadMonitor', 'AchOffloadCompleteMonitor', 'EventsGateway', 'RAFWriter', 'EftpsEnrollmentsAgeOutMonitor', 'EftpsPaymentMonitor', 'PSPToAs400DataSyncMonitor', 'EftpsEnrollmentsAgeOut', 'EftpsPayment', 'EftpsEnrollments', 'OFACReportProcessor', 'AMLReportProcessor', 'IndustryReportProcessor', 'OFACReportMonitor', 'AMLReportMonitor', 'IndustryReportMonitor', 'BRMAssistedUsageErrorFileProcessor', 'ATFDataExtract', 'TaxPaymentSubmission', 'TaxPaymentSynchronization', 'TriggerAmendments', 'HPDEBatchProcessor', 'SalesTaxExceptionProcessor', 'TPSUReportProcessor', 'TPSUReportMonitor', 'ATFDepositFrequencyExtract', 'PrimaryDailyForecast', 'CheckPrint', 'CheckPrintMonitor', 'TaxCreditsEchoSignMonitor'
, 'OffloadedTransactionsEvents', 'OffloadedTransactionsEventsMonitor', 'ThirdParty401kOffload', 'ThirdParty401kSignup', 'ThirdParty401kValidation', 'ThirdParty401kOffloadMonitor', 'ThirdParty401kSignupMonitor', 'ThirdParty401kValidationMonitor', 'AssistedUsageReportingToBRMProcessor', 'AssistedUsageDataSyncProcessor', 'As400DataSync', 'As400DataSyncMonitor', 'TaxCreditsEchoSign', 'SoxDBUserReport', 'QbdtUnprocessedRequestsRetry', 'EftpsEnrollmentsMonitor', 'IOPDataSync', 'PSPToAs400DataSync', 'IOPDataSyncMonitor', 'ATFCompanyLiabilityExtract', 'ATFCompanyPaymentExtract', 'AchTaxPaymentOffloadMonitor', 'PrintedCheckBatch', 'PrintedCheckBatchMonitor', 'AchTaxPaymentOffload', 'AchZeroPayments', 'AchZeroPaymentsMonitor', 'ReconPlus', 'ReconPlusMonitor', 'StateReportMonitor', 'StateReport', 'StateCouponMonitor', 'StateCoupon', 'PSPToAs400', 'PSPToAs400Monitor', 'AMOMessageProcessorMonitor', 'EftpsResponse', 'EntitlementProcessor', 'EntitlementProcessorMonitor', 'AS400EventSyncMonitor', 'AMOMessageProcessor', 'EftpsResponseMonitor', 'EftpsSend', 'EftpsSendMonitor', 'EdiResponse', 'EdiResponseMonitor', 'EdiSend', 'EdiSendMonitor', 'EdiPayment', 'AchDebitOffload', 'AchDebitOffloadMonitor', 'EoqSUIAdjustments', 'EoqSUIAdjustmentsMonitor', 'EMSBSToBRMDataSyncProcessor', 'IRSDepositFrequencyFileProcessorMonitor', 'IRSDepositFrequencyFileProcessor', 'PSPToEMSBSDataSyncProcessor', 'MonthlyFee', 'MonthlyFeeMonitor', 'ATFWageLimitsExtract', 'ATFCompanyInfoExtract', 'ATFEmployeeInfoExtract', 'ATFEmployeeTotalsExtract', 'ATFCompanyTaxExtract', 'ATFCompanyTaxRateExtract', 'ATFEmployeeTotalsCalculation', 'EmployeeW2TotalsCalculationMonitor', 'EmployeeW2TotalsCalculationProcessor', 'SendW2AnnualDataToTFSProcessor', 'FsetFilingProcessor', 'FsetFilingMonitor', 'FsetResponseProcessor', 'FsetResponseMonitor', 'ScheduledEmails', 'LedgerBalanceMonitor', 'LedgerOperations', 'W2CountsExtract', 'WorkersCompProcessor', 'WorkersCompMonitor', 'AnnualBillingProcessor', 'SendMonthlyDataToTFSMonitor', 'SendMonthlyDataToTFSProcessor', 'ACHDeEnrollmentBatchJob', 'ACHEnrollmentResponseBatchJob', 'ACHEnrollmentBatchJob', 'IamEmailAddressMonitor', 'IamEmailAddressProcessor', 'EnrollmentDeleteSelectionMonitor', 'DDMAchOffloadMonitor', 'DDMDailyBatchJobs', 'DataPartitionProcessor'));

ALTER TABLE PSP_ASSISTED_BUNDLE_BILL
 ADD CONSTRAINT PSP_ASSISTEDBUNDLEBILL_FK1 
  FOREIGN KEY (ASST_BUNDLE_COMP_USAGE_FK, REALM_ID) 
  REFERENCES PSP_ASST_BUNDLE_COMP_USAGE (ASST_BUNDLE_COMP_USAGE_SEQ,REALM_ID);

ALTER TABLE PSP_ASST_BUNDLE_BILL_DETAIL
 ADD CONSTRAINT PSP_ASSTBUNDLEBILLDETAIL_FK1 
  FOREIGN KEY (ASSISTED_BUNDLE_BILL_FK, REALM_ID) 
  REFERENCES PSP_ASSISTED_BUNDLE_BILL (ASSISTED_BUNDLE_BILL_SEQ,REALM_ID);

PROMPT finished DBUpgrade_002.019.010.001.sql