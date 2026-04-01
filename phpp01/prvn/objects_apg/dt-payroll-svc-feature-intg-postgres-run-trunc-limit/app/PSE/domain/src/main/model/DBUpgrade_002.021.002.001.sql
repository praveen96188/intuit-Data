--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Table PSP_SMSMIGRATION;
CREATE TABLE PSP_SMSMIGRATION
(
  SMSMIGRATION_SEQ         VARCHAR2(255 CHAR)   NOT NULL,
  VERSION                  NUMBER(19)           NOT NULL,
  CREATOR_ID               VARCHAR2(30 CHAR),
  CREATED_DATE             TIMESTAMP(6)         NOT NULL,
  MODIFIER_ID              VARCHAR2(30 CHAR),
  MODIFIED_DATE            TIMESTAMP(6)         NOT NULL,
  REALM_ID                 NUMBER(19)           DEFAULT -1                    NOT NULL,
  SOURCE_COMPANY_ID        VARCHAR2(50 CHAR),
  MIGRATION_STATUS         VARCHAR2(255 CHAR),
  VALIDATION_ERROR_RESULT  VARCHAR2(4000 CHAR),
  DISMISSAL_COUNT          NUMBER(10)
)
NOPARALLEL;

ALTER TABLE PSP_SMSMIGRATION
 ADD CONSTRAINT C_PSP_SMSMIGRATION0
  CHECK (MIGRATION_STATUS IN('ValidationInProgress', 'NeedsValidation', 'ValidationSuccess', 'ValidationError', 'ValidationInternalError', 'MigrationError', 'MigrationInProgress', 'MigrationComplete'));

ALTER TABLE PSP_SMSMIGRATION
 ADD PRIMARY KEY
  (SMSMIGRATION_SEQ, REALM_ID)
  USING INDEX;

Prompt Table PSP_OWNERSHIP_TYPE;
CREATE TABLE PSP_OWNERSHIP_TYPE
(
  OWNERSHIP_TYPE_SEQ  VARCHAR2(255 CHAR)        NOT NULL,
  VERSION             NUMBER(19)                NOT NULL,
  REALM_ID            NUMBER(19)                DEFAULT -1                    NOT NULL,
  OWNERSHIP           VARCHAR2(4000 CHAR)
)
NOPARALLEL;

ALTER TABLE PSP_OWNERSHIP_TYPE
 ADD PRIMARY KEY
  (OWNERSHIP_TYPE_SEQ, REALM_ID)
  USING INDEX;

Prompt Column COMPLIANCE_ADDRESS_FK;
ALTER TABLE PSP_COMPANY
 ADD (COMPLIANCE_ADDRESS_FK  VARCHAR2(255 CHAR));

Prompt Column OWNERSHIP_TYPE_FK;
ALTER TABLE PSP_COMPANY_ADDITIONAL_INFO
 ADD (OWNERSHIP_TYPE_FK  VARCHAR2(255 CHAR));

Prompt Index PSP_COMPANY_ADDITIONAL_INF_FK3;
CREATE INDEX PSP_COMPANY_ADDITIONAL_INF_FK3 ON PSP_COMPANY_ADDITIONAL_INFO
(OWNERSHIP_TYPE_FK, REALM_ID)
NOPARALLEL;

Prompt Index PSP_COMPANY_FK8;
CREATE INDEX PSP_COMPANY_FK8 ON PSP_COMPANY
(COMPLIANCE_ADDRESS_FK, REALM_ID)
NOPARALLEL;

ALTER TABLE PSP_COMPANY
 ADD CONSTRAINT PSP_COMPANY_FK8
  FOREIGN KEY (COMPLIANCE_ADDRESS_FK, REALM_ID) 
  REFERENCES PSP_ADDRESS (ADDRESS_SEQ,REALM_ID);

ALTER TABLE PSP_COMPANY_ADDITIONAL_INFO
 ADD CONSTRAINT PSP_COMPANY_ADDITIONAL_INF_FK3
  FOREIGN KEY (OWNERSHIP_TYPE_FK, REALM_ID) 
  REFERENCES PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ,REALM_ID);

Prompt Constraint C_PSP_BATCH_JOB_SETUP0;
ALTER TABLE PSP_BATCH_JOB_SETUP
 DROP CONSTRAINT C_PSP_BATCH_JOB_SETUP0;

ALTER TABLE PSP_BATCH_JOB_SETUP
 ADD CONSTRAINT C_PSP_BATCH_JOB_SETUP0
  CHECK (JOB_TYPE IN('FailedPayrollPlSqlJobsProcessor', 'PayrollFraudBatchPurgePlSqlJobsProcessor', 'PSPEventLogPurgePlSqlJobsProcessor', 'DailyPayrollStatsPlSqlJobsProcessor', 'EFTPSOnHoldPaymentPlSqlJobsProcessor', 'ValidateEmployeeWagePlansPlSqlJobsProcessor', 'EmployeePayrollItemTotalsCalcProcess', 'SendW2AnnualDataToTFSMonitor', 'SendW2PreviewDataToTFSProcessor', 'SendW2PreviewDataToTFSMonitor', 'SUICreditsBatchJob', 'AnnualBillingMonitor', 'SalesTaxExceptionMonitor', 'EnrollmentDeleteSelectionProcessor', 'NCDFixPlSqlJobsProcessor', 'OfferingUpdateUsageBillingPlSqlJobsProcessor', 'BRMUsageErrorFileProcessor', 'EDRAssociationFixPlSqlJobsProcessor', 'RetryEntitlementActivationPlSqlJobsProcessor', 'NCDFixALLPlSqlJobsProcessor', 'CostCoPlSqlJobsProcessor', 'EmployeeTotalsCalculationProcess', 'EmployeeTotalsCalculationMonitor', 'RTBAutomation', 'AchReturnsMonitor', 'EdiPaymentMonitor', 'AchTransactionsMonitor', 'BalanceFileMonitor', 'EmailGateway', 'EmailGatewayMonitor', 'FeeEvents', 'FeeEventsMonitor', 'FraudPayrolls', 'FraudPayrollsMonitor', 'GemsAccountsReceivable', 'GemsAccountsReceivableMonitor', 'GemsGeneralLedger', 'GemsGeneralLedgerMonitor', 'LedgerBalance', 'MissedPayrollsMonitor', 'MissedTransactionsMonitor', 'NightlyBatchJobs', 'NightlyBatchJobsMonitor', 'PrimaryAchOffloadMonitor', 'PrimaryDailyBatchJobs', 'PrimaryDailyBatchJobsMonitor', 'ScheduledAchOffloadMonitor', 'ScheduledDailyBatchJobs', 'ScheduledDailyBatchJobsMonitor', 'As400EventSync', 'GemsGeneralLedgerUpload', 'GemsGeneralLedgerUploadMonitor', 'AchOffloadCompleteMonitor', 'EventsGateway', 'RAFWriter', 'EftpsEnrollmentsAgeOutMonitor', 'EftpsPaymentMonitor', 'PSPToAs400DataSyncMonitor', 'EftpsEnrollmentsAgeOut', 'EftpsPayment', 'EftpsEnrollments', 'OFACReportProcessor', 'AMLReportProcessor', 'IndustryReportProcessor', 'OFACReportMonitor', 'AMLReportMonitor', 'IndustryReportMonitor', 'BRMAssistedUsageErrorFileProcessor', 'ATFDataExtract', 'TaxPaymentSubmission', 'TaxPaymentSynchronization', 'TriggerAmendments', 'HPDEBatchProcessor', 'SalesTaxExceptionProcessor', 'TPSUReportProcessor', 'TPSUReportMonitor', 'ATFDepositFrequencyExtract', 'PrimaryDailyForecast', 'CheckPrint', 'CheckPrintMonitor', 'TaxCreditsEchoSignMonitor'
, 'OffloadedTransactionsEvents', 'OffloadedTransactionsEventsMonitor', 'ThirdParty401kOffload', 'ThirdParty401kSignup', 'ThirdParty401kValidation', 'ThirdParty401kOffloadMonitor', 'ThirdParty401kSignupMonitor', 'ThirdParty401kValidationMonitor', 'AssistedUsageReportingToBRMProcessor', 'AssistedUsageDataSyncProcessor', 'As400DataSync', 'As400DataSyncMonitor', 'TaxCreditsEchoSign', 'SoxDBUserReport', 'QbdtUnprocessedRequestsRetry', 'EftpsEnrollmentsMonitor', 'IOPDataSync', 'PSPToAs400DataSync', 'IOPDataSyncMonitor', 'ATFCompanyLiabilityExtract', 'ATFCompanyPaymentExtract', 'AchTaxPaymentOffloadMonitor', 'PrintedCheckBatch', 'PrintedCheckBatchMonitor', 'AchTaxPaymentOffload', 'AchZeroPayments', 'AchZeroPaymentsMonitor', 'ReconPlus', 'ReconPlusMonitor', 'StateReportMonitor', 'StateReport', 'StateCouponMonitor', 'StateCoupon', 'ComplianceToolKit', 'PSPToAs400', 'PSPToAs400Monitor', 'AMOMessageProcessorMonitor', 'EftpsResponse', 'EntitlementProcessor', 'EntitlementProcessorMonitor', 'AS400EventSyncMonitor', 'AMOMessageProcessor', 'EftpsResponseMonitor', 'EftpsSend', 'EftpsSendMonitor', 'EdiResponse', 'EdiResponseMonitor', 'EdiSend', 'EdiSendMonitor', 'EdiPayment', 'AchDebitOffload', 'AchDebitOffloadMonitor', 'EoqSUIAdjustments', 'EoqSUIAdjustmentsMonitor', 'EntityEvent', 'EMSBSToBRMDataSyncProcessor', 'IRSDepositFrequencyFileProcessorMonitor', 'IRSDepositFrequencyFileProcessor', 'PSPToEMSBSDataSyncProcessor', 'MonthlyFee', 'MonthlyFeeMonitor', 'ATFWageLimitsExtract', 'ATFCompanyInfoExtract', 'ATFEmployeeInfoExtract', 'ATFEmployeeTotalsExtract', 'ATFCompanyTaxExtract', 'ATFCompanyTaxRateExtract', 'ATFEmployeeTotalsCalculation', 'EmployeeW2TotalsCalculationMonitor', 'EmployeeW2TotalsCalculationProcessor', 'SendW2AnnualDataToTFSProcessor', 'FsetFilingProcessor', 'FsetFilingMonitor', 'FsetResponseProcessor', 'FsetResponseMonitor', 'ScheduledEmails', 'LedgerBalanceMonitor', 'LedgerOperations', 'W2CountsExtract', 'WorkersCompProcessor', 'WorkersCompMonitor', 'AnnualBillingProcessor', 'SendMonthlyDataToTFSMonitor', 'SendMonthlyDataToTFSProcessor', 'ACHDeEnrollmentBatchJob', 'ACHEnrollmentResponseBatchJob', 'ACHEnrollmentBatchJob', 'IamEmailAddressMonitor', 'IamEmailAddressProcessor', 'EnrollmentDeleteSelectionMonitor', 'DDMAchOffloadMonitor', 'DDMDailyBatchJobs', 'DataPartitionProcessor'
, 'EntityEventRetry', 'ACHTraceIdProcessor', 'ATFCompanyPayrollItemExtract', 'MassCancelAutoProcessor', 'AccountServiceSyncExceptionProcessor','MtlTransactionReportEnrichProcessor')) ENABLE NOVALIDATE;

Prompt Constraint C_PSP_BATCH_JOB_STATUS0;
ALTER TABLE PSP_BATCH_JOB_STATUS
 DROP CONSTRAINT C_PSP_BATCH_JOB_STATUS0;

ALTER TABLE PSP_BATCH_JOB_STATUS
 ADD CONSTRAINT C_PSP_BATCH_JOB_STATUS0
  CHECK (JOB_TYPE IN('FailedPayrollPlSqlJobsProcessor', 'PayrollFraudBatchPurgePlSqlJobsProcessor', 'PSPEventLogPurgePlSqlJobsProcessor', 'DailyPayrollStatsPlSqlJobsProcessor', 'EFTPSOnHoldPaymentPlSqlJobsProcessor', 'ValidateEmployeeWagePlansPlSqlJobsProcessor', 'EmployeePayrollItemTotalsCalcProcess', 'SendW2AnnualDataToTFSMonitor', 'SendW2PreviewDataToTFSProcessor', 'SendW2PreviewDataToTFSMonitor', 'SUICreditsBatchJob', 'AnnualBillingMonitor', 'SalesTaxExceptionMonitor', 'EnrollmentDeleteSelectionProcessor', 'NCDFixPlSqlJobsProcessor', 'OfferingUpdateUsageBillingPlSqlJobsProcessor', 'BRMUsageErrorFileProcessor', 'EDRAssociationFixPlSqlJobsProcessor', 'RetryEntitlementActivationPlSqlJobsProcessor', 'NCDFixALLPlSqlJobsProcessor', 'CostCoPlSqlJobsProcessor', 'EmployeeTotalsCalculationProcess', 'EmployeeTotalsCalculationMonitor', 'RTBAutomation', 'AchReturnsMonitor', 'EdiPaymentMonitor', 'AchTransactionsMonitor', 'BalanceFileMonitor', 'EmailGateway', 'EmailGatewayMonitor', 'FeeEvents', 'FeeEventsMonitor', 'FraudPayrolls', 'FraudPayrollsMonitor', 'GemsAccountsReceivable', 'GemsAccountsReceivableMonitor', 'GemsGeneralLedger', 'GemsGeneralLedgerMonitor', 'LedgerBalance', 'MissedPayrollsMonitor', 'MissedTransactionsMonitor', 'NightlyBatchJobs', 'NightlyBatchJobsMonitor', 'PrimaryAchOffloadMonitor', 'PrimaryDailyBatchJobs', 'PrimaryDailyBatchJobsMonitor', 'ScheduledAchOffloadMonitor', 'ScheduledDailyBatchJobs', 'ScheduledDailyBatchJobsMonitor', 'As400EventSync', 'GemsGeneralLedgerUpload', 'GemsGeneralLedgerUploadMonitor', 'AchOffloadCompleteMonitor', 'EventsGateway', 'RAFWriter', 'EftpsEnrollmentsAgeOutMonitor', 'EftpsPaymentMonitor', 'PSPToAs400DataSyncMonitor', 'EftpsEnrollmentsAgeOut', 'EftpsPayment', 'EftpsEnrollments', 'OFACReportProcessor', 'AMLReportProcessor', 'IndustryReportProcessor', 'OFACReportMonitor', 'AMLReportMonitor', 'IndustryReportMonitor', 'BRMAssistedUsageErrorFileProcessor', 'ATFDataExtract', 'TaxPaymentSubmission', 'TaxPaymentSynchronization', 'TriggerAmendments', 'HPDEBatchProcessor', 'SalesTaxExceptionProcessor', 'TPSUReportProcessor', 'TPSUReportMonitor', 'ATFDepositFrequencyExtract', 'PrimaryDailyForecast', 'CheckPrint', 'CheckPrintMonitor', 'TaxCreditsEchoSignMonitor'
, 'OffloadedTransactionsEvents', 'OffloadedTransactionsEventsMonitor', 'ThirdParty401kOffload', 'ThirdParty401kSignup', 'ThirdParty401kValidation', 'ThirdParty401kOffloadMonitor', 'ThirdParty401kSignupMonitor', 'ThirdParty401kValidationMonitor', 'AssistedUsageReportingToBRMProcessor', 'AssistedUsageDataSyncProcessor', 'As400DataSync', 'As400DataSyncMonitor', 'TaxCreditsEchoSign', 'SoxDBUserReport', 'QbdtUnprocessedRequestsRetry', 'EftpsEnrollmentsMonitor', 'IOPDataSync', 'PSPToAs400DataSync', 'IOPDataSyncMonitor', 'ATFCompanyLiabilityExtract', 'ATFCompanyPaymentExtract', 'AchTaxPaymentOffloadMonitor', 'PrintedCheckBatch', 'PrintedCheckBatchMonitor', 'AchTaxPaymentOffload', 'AchZeroPayments', 'AchZeroPaymentsMonitor', 'ReconPlus', 'ReconPlusMonitor', 'StateReportMonitor', 'StateReport', 'StateCouponMonitor', 'StateCoupon', 'ComplianceToolKit', 'PSPToAs400', 'PSPToAs400Monitor', 'AMOMessageProcessorMonitor', 'EftpsResponse', 'EntitlementProcessor', 'EntitlementProcessorMonitor', 'AS400EventSyncMonitor', 'AMOMessageProcessor', 'EftpsResponseMonitor', 'EftpsSend', 'EftpsSendMonitor', 'EdiResponse', 'EdiResponseMonitor', 'EdiSend', 'EdiSendMonitor', 'EdiPayment', 'AchDebitOffload', 'AchDebitOffloadMonitor', 'EoqSUIAdjustments', 'EoqSUIAdjustmentsMonitor', 'EntityEvent', 'EMSBSToBRMDataSyncProcessor', 'IRSDepositFrequencyFileProcessorMonitor', 'IRSDepositFrequencyFileProcessor', 'PSPToEMSBSDataSyncProcessor', 'MonthlyFee', 'MonthlyFeeMonitor', 'ATFWageLimitsExtract', 'ATFCompanyInfoExtract', 'ATFEmployeeInfoExtract', 'ATFEmployeeTotalsExtract', 'ATFCompanyTaxExtract', 'ATFCompanyTaxRateExtract', 'ATFEmployeeTotalsCalculation', 'EmployeeW2TotalsCalculationMonitor', 'EmployeeW2TotalsCalculationProcessor', 'SendW2AnnualDataToTFSProcessor', 'FsetFilingProcessor', 'FsetFilingMonitor', 'FsetResponseProcessor', 'FsetResponseMonitor', 'ScheduledEmails', 'LedgerBalanceMonitor', 'LedgerOperations', 'W2CountsExtract', 'WorkersCompProcessor', 'WorkersCompMonitor', 'AnnualBillingProcessor', 'SendMonthlyDataToTFSMonitor', 'SendMonthlyDataToTFSProcessor', 'ACHDeEnrollmentBatchJob', 'ACHEnrollmentResponseBatchJob', 'ACHEnrollmentBatchJob', 'IamEmailAddressMonitor', 'IamEmailAddressProcessor', 'EnrollmentDeleteSelectionMonitor', 'DDMAchOffloadMonitor', 'DDMDailyBatchJobs', 'DataPartitionProcessor'
, 'EntityEventRetry', 'ACHTraceIdProcessor', 'ATFCompanyPayrollItemExtract', 'MassCancelAutoProcessor', 'AccountServiceSyncExceptionProcessor','MtlTransactionReportEnrichProcessor')) ENABLE NOVALIDATE;


PROMPT finished DBUpgrade_002.021.002.001.sql