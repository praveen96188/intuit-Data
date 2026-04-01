--CONSTRAINTS



ALTER TABLE pspadm.psp_accounting_report_file DROP CONSTRAINT c_psp_accounting_report_fi0 ;
--CHECK (status IN ('New', 'Created', 'Transmitted', 'Archived')) NOT VALID;

ALTER TABLE pspadm.psp_accounting_report_file DROP CONSTRAINT c_psp_accounting_report_fi1 ;
--CHECK (type IN ('PositivePay', 'PrintedCheckReconPlus', 'TaxAccountsReconPlus', 'ReturnsAccountsReconPlus')) NOT VALID;

ALTER TABLE pspadm.psp_accounting_report_file--ADD --PRIMARY KEY (accounting_report_file_seq);

ALTER TABLE pspadm.psp_ach_transaction_code DROP CONSTRAINT c_psp_ach_transaction_code0 ;
--CHECK (ach_account_type_cd IN ('Savings', 'Ledger', 'Loan', 'Checking')) NOT VALID;

ALTER TABLE pspadm.psp_ach_transaction_code DROP CONSTRAINT c_psp_ach_transaction_code1 ;
--CHECK (credit_debit_indicator IN ('Credit', 'Debit')) NOT VALID;

ALTER TABLE pspadm.psp_ach_transaction_code--ADD --PRIMARY KEY (transaction_code);

ALTER TABLE pspadm.psp_achenrollment DROP CONSTRAINT c_psp_achenrollment0 ;
--CHECK (status IN ('Cancelled', 'Deleted', 'EnrollmentRejected', 'Enrolled', 'PendingEnrollmentResponse', 'PendingDelete', 'PendingEnrollment')) NOT VALID;



ALTER TABLE pspadm.psp_achenrollment_file DROP CONSTRAINT c_psp_achenrollment_file0 ;
--CHECK (status IN ('Archived', 'Processed', 'PendingTransmission', 'SentToAgency', 'UploadedByAgent', 'Error')) NOT VALID;

ALTER TABLE pspadm.psp_achenrollment_file DROP CONSTRAINT c_psp_achenrollment_file1 ;
--CHECK (type IN ('Add', 'Delete', 'Response')) NOT VALID;



ALTER TABLE pspadm.psp_action_event DROP CONSTRAINT c_psp_action_event0 ;
--CHECK (code IN ('VoidTORTransaction', 'RefundERPayableCancel', 'FinancialTransactionVoidTx', 'FinancialTransactionCancel', 'IssueReissueRefundEr', 'TxStateHistory', 'DDTransactionCancel', 'DDTransactionReverse', 'DDRedebitAdd', 'DDRedebitRecord', 'ERFeeAdd', 'BadDebtWriteOff', 'BadDebtRecover', 'EEReturnTransfer', 'FeeTransfer', 'Intuit5DayReturnTransfer', 'DDRefund', 'ERReturnRefund', 'EEReturnRefund', 'ERWireExpected', 'RefundRebillFee', 'DDRedebitEdit', 'ERFraudOrEscalationRefund', 'BadDebtWriteOffEEReturn', 'RecordPrefundingWire', 'CancelAdjustment', 'VoidPayrollTaxPayment', 'ReissuePayrollTaxPayment', 'ApplyERPayableToBalanceDue', 'RefundDebit', 'ERFeeCancel')) NOT VALID;


ALTER TABLE pspadm.psp_action_event DROP CONSTRAINT c_psp_action_event1 ;
--CHECK (type IN ('FinancialTransaction', 'PayrollRun', 'LedgerAccount'));



ALTER TABLE pspadm.psp_agency DROP CONSTRAINT c_psp_agency0 ;
--CHECK (default_r_a_a_form IN ('LPOA', 'Federal8655'));



ALTER TABLE pspadm.psp_agency_id_requirement DROP CONSTRAINT c_psp_agency_id_requirement0 ;
--CHECK (custom_requirement IN ('MustNotInExemptedIdList', 'MustNotContainFedTaxId', 'IFNotPatternMustFollowFedTaxId', 'IfNotMEorTRMustFollowFedTaxId', 'MustNotFollowFedTaxId', 'MustStartWithFedTaxId', 'MustFollowFedTaxId', 'Digits4Through12FollowFedTaxId', 'Digits2Through10FollowFedTaxId', 'None', 'MustNotFollowFedTaxIdSubstitueIf8Digits', 'Digits3Through11FollowFedTaxId')) NOT VALID;

ALTER TABLE pspadm.psp_agency_id_requirement--ADD --PRIMARY KEY (agency_id_requirement_seq);

ALTER TABLE pspadm.psp_agency_rate_request DROP CONSTRAINT c_psp_agency_rate_request0 ;
--CHECK (status IN ('Created', 'GeneratingRequest', 'RequestGenerated', 'RequestSent', 'ResponseReceived', 'ResponseVerified', 'ResponseApplying', 'ResponseApplied', 'Cancelled')) NOT VALID;



ALTER TABLE pspadm.psp_annual_billing_batch DROP CONSTRAINT c_psp_annual_billing_batch0 ;
--CHECK (form_type_cd IN ('W2')) NOT VALID;

ALTER TABLE pspadm.psp_annual_billing_batch DROP CONSTRAINT c_psp_annual_billing_batch1 ;
--CHECK (annual_billing_batch_status_cd IN ('Completed', 'Pending')) NOT VALID;



ALTER TABLE pspadm.psp_annual_billing_item DROP CONSTRAINT c_psp_annual_billing_item0 ;
--CHECK (annual_billing_item_status_cd IN ('Pending', 'Error', 'Completed', 'Skipped')) NOT VALID;


ALTER TABLE pspadm.psp_applied_database_patch DROP CONSTRAINT c_psp_applied_database_pat0 ;
--CHECK (database_patch_type_cd IN ('SchemaUpgrade', 'DataMigration'));



ALTER TABLE pspadm.psp_assisted_bundle_bill DROP CONSTRAINT c_psp_assisted_bundle_bill0 ;
--CHECK (asst_status IN ('Open', 'ProcessingFailed', 'SentToBRM', 'SentToBRMFailed', 'Processed'));



ALTER TABLE pspadm.psp_asst_bundle_comp_usage DROP CONSTRAINT c_psp_asst_bundle_comp_usa0 ;
--CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO'));


ALTER TABLE pspadm.psp_atfdata_extract_batch DROP CONSTRAINT c_psp_atfdata_extract_batch0 ;
--CHECK (run_type IN ('QuarterlyData', 'UpdatedData', 'AnnualData')) NOT VALID;

ALTER TABLE pspadm.psp_atfdata_extract_batch DROP CONSTRAINT c_psp_atfdata_extract_batch1 ;
--CHECK (batch_status IN ('Submitted', 'Completed', 'InProgress', 'Aborted'));


ALTER TABLE pspadm.psp_atfdata_extract_file DROP CONSTRAINT c_psp_atfdata_extract_file0 ;
--CHECK (file_type IN ('CompanyInfo', 'EmployeeInfo', 'CompanyTaxInfo', 'CompanyTaxRateInfo', 'CompanyLiabilitiesInfo', 'CompanyPaymentsInfo', 'CompanyDepFreqInfo', 'EmployeeTotalsInfo', 'CompanyAdjustmentsInfo', 'W2CountInfo', 'WageLimitsInfo', 'CompanyPayrollItemInfo'));

ALTER TABLE pspadm.psp_atfdata_extract_file DROP CONSTRAINT c_psp_atfdata_extract_file1 ;
--CHECK (file_status IN ('Started', 'Extracted', 'Failed', 'Completed', 'Aborted'));



ALTER TABLE pspadm.psp_auth_operation DROP CONSTRAINT c_psp_auth_operation0 ;
--CHECK (operation_id IN ('EditQBFileID', 'VoidTORTransaction', 'EditAllowTransmissions', 'RemoveTaxTableSuspension', 'ExecuteRTBAutomationJob', 'ScheduleATFExtract', 'BankReturnUpdate', 'AccessApplication', 'DDLimitUpdate', 'FundingModelUpdate', 'DDStatusUpdate', 'StrikeAdd', 'StrikeCancel', 'ExecuteRTBJob', 'SettingUpdate', 'ViewFullBankAccountNumbers', 'UpdateCancelTermCompany', 'GeneratePin', 'AddBankAccountRandomDebits', 'AddOffering', 'AddOffer', 'EditChartOfAccounts', 'AddBankAccountByPassRandomDebits', 'BankReturnView', 'RecordNonACHRedebitTransaction', 'CreateFeeTransaction', 'CreateReversalTransaction', 'DDTransactionCancel', 'TransactionCancel', 'LedgerView', 'SelectNonStandardSettlementType', 'CreateRefundTransaction', 'VoidTransaction', 'BookTransferTransaction', 'ActivateBankAccount', 'WriteoffBadDebtTransaction', 'RecoverBadDebtTransaction', 'EscalationCreditTransaction', 'IssueRedebitTransaction', 'AuthAccessApplication', 'AuthAddUpdateUsers', 'AuthRemoveUsers', 'AuthAddUpdateHelpDesk', 'AuthAddRemoveHelpDesk', 'AuthAddUpdateDataCustodian', 'AuthRemoveDataCustodian', 'EditCompanyLegalInformation', 'EditCompanyContactInformation', 'ViewTransactionHistory', 'ViewVerificationDebits', 'ResetVerificationAmounts', 'GenerateRandomDebits', 'RefundERPayable', 'ViewPayrollScreen', 'EnterWireExpectedDate', 'ViewSignupFraudQueue', 'RemoveFromSignupFraudHold', 'UploadToGems', 'ViewOFX', 'RequestSecondOffload', 'ViewChaseReport', 'PrintChaseReport', 'AgentInitiatesRefundRebill', 'SavePrintOFX', 'ConfirmOffload', 'RefundEmployerFraudEscalation', 'AddAssistedEIN', 'AddToEINDIY', 'AddToEINAssisted', 'MoveEINDIYDIY', 'MoveEINDIYAssisted', 'DeactivateEIN', 'DeactivateEINPendingActivation', 'DeactivateEINActive', 'ReactivateEINDIY', 'ReactivateEINAssisted', 'EditTokens', 'EditDebugLogging', 'AssignChecklist', 'CreateRAFFile', 'CreateACHFile', 'ViewOperatorTab', 'AddCheckDistributionService', 'DecryptText', 'WriteoffEmployeeBadDebtTransaction', 'RecordPrefundingWire', 'ViewOffloadStatus', 'AddVendorPaymentService', 'AddAS400Company', 'ViewCheckPrintSignature', 'AddUpdateCheckPrintSignature', 'ViewCheckPrintQueue', 'UpdateCheckPrintBatchStatus', 'TaxCreditsWOTC', 'ViewVMPData', 'CancelCloud', 'ViewGlobalEnrollments', 'ResolveEFTPSReject', 'ManageRAFEnrollment', 'ManageTaxPayments', 'ViewTaxLedger', 'ViewCompanyTaxPayments', 'CreateManualLedgerEntry', 'ViewAgencyInfo', 'ViewEEPII', 'ViewOverpayments', 'ViewGlobalTaxPayments', 'EditVMPData', 'ViewMoneyMovementScreen', 'ExecuteSQL', 'ViewSystemParameters', 'EditTaxExemptFlag', 'EditPrincipalContactsDIYOnly', 'EditAssistedPayrollContactsInPendingActivation', 'EditAssistedPayrollContactsInActiveStatus', 'EditAssistedEINPendingActivation', 'EditAssistedEINActive', 'EditAssistedCompanyLegalInfo', 'AddUpdatePriceType', 'AddDIYEIN', 'AccessDataSyncTool', 'CreateFLA', 'RecalculateLedgerBalances', 'CreateBookTransfer', 'ManageSUITaxPayments', 'ReportFileDownload', 'EditAssistedPrincipalContacts', 'EditAssistedPrincipalContactsInPendingActivation', 'EditAssistedCompanyLegalInfoPendingActivation', 'CreateERPenaltiesAndInterestRefunds', 'AddManualFeeTransactions', 'CreateMultipleBackdatingRefunds', 'CreateCourtesyRefund', 'AddRestrictedOffer', 'EditProcessTransmissions', 'UpdateComplianceData', 'AddAssistedBankAccountPreBALF', 'AddAssistedBankAccountBypassRandomDollarDebitPreBALF', 'AddBankAccountByPassRandomDebitsPostBALF', 'AddBankAccountRandomDebitsPostBALF', 'AddAssistedOfferPreBALF', 'AddAssistedOfferPostBALF', 'AddAssistedOfferingPreBALF', 'AddAssistedOfferingPostBALF', 'LedgerOperations', 'SearchBySSN', 'CreateTOR', 'EmployerFeeDebitCancel', 'ManualEFTPSEnrollments', 'EditEntityChangeInfo', 'EditCancellationInfo', 'EditACHRegFlag', 'EditAgencyIDs', 'EditDepositFreq', 'EditFilerType', 'EditFilingAmts', 'EditFilingAmtsOtherQtr', 'EditFilingFlags', 'EditRatesOtherLaws', 'EditSUIRateCurrQTR', 'EditRatesInOtherQTRs', 'RateSuperUser', 'IPBasedFraudFilteringView'));


ALTER TABLE pspadm.psp_bank_account DROP CONSTRAINT c_psp_bank_account0 ;
--CHECK (a_c_h_account_type_cd IN ('Savings', 'Ledger', 'Loan', 'Checking')) NOT VALID;

ALTER TABLE pspadm.psp_bank_account DROP CONSTRAINT c_psp_bank_account1 ;
--CHECK (account_type_cd IN ('Checking', 'Savings')) NOT VALID;

ALTER TABLE pspadm.psp_bank_account DROP CONSTRAINT c_psp_bank_account2 ;
--CHECK (a_c_h_entry_class IN ('CCD', 'PPD')) NOT VALID;


ALTER TABLE pspadm.psp_batch_job_setup DROP CONSTRAINT c_psp_batch_job_setup0;

ALTER TABLE pspadm.psp_batch_job_setup DROP CONSTRAINT c_psp_batch_job_setup0 ;
--CHECK (job_type IN ('FailedPayrollPlSqlJobsProcessor', 'PayrollFraudBatchPurgePlSqlJobsProcessor', 'PSPEventLogPurgePlSqlJobsProcessor', 'DailyPayrollStatsPlSqlJobsProcessor', 'EFTPSOnHoldPaymentPlSqlJobsProcessor', 'ValidateEmployeeWagePlansPlSqlJobsProcessor', 'EmployeePayrollItemTotalsCalcProcess', 'SendW2AnnualDataToTFSMonitor', 'SendW2PreviewDataToTFSProcessor', 'SendW2PreviewDataToTFSMonitor', 'SUICreditsBatchJob', 'AnnualBillingMonitor', 'SalesTaxExceptionMonitor', 'EnrollmentDeleteSelectionProcessor', 'NCDFixPlSqlJobsProcessor', 'OfferingUpdateUsageBillingPlSqlJobsProcessor', 'BRMUsageErrorFileProcessor', 'EDRAssociationFixPlSqlJobsProcessor', 'RetryEntitlementActivationPlSqlJobsProcessor', 'NCDFixALLPlSqlJobsProcessor', 'CostCoPlSqlJobsProcessor', 'EmployeeTotalsCalculationProcess', 'EmployeeTotalsCalculationMonitor', 'RTBAutomation', 'AchReturnsMonitor', 'EdiPaymentMonitor', 'AchTransactionsMonitor', 'BalanceFileMonitor', 'EmailGateway', 'EmailGatewayMonitor', 'FeeEvents', 'FeeEventsMonitor', 'FraudPayrolls', 'FraudPayrollsMonitor', 'GemsAccountsReceivable', 'GemsAccountsReceivableMonitor', 'GemsGeneralLedger', 'GemsGeneralLedgerMonitor', 'LedgerBalance', 'MissedPayrollsMonitor', 'MissedTransactionsMonitor', 'NightlyBatchJobs', 'NightlyBatchJobsMonitor', 'PrimaryAchOffloadMonitor', 'PrimaryDailyBatchJobs', 'PrimaryDailyBatchJobsMonitor', 'ScheduledAchOffloadMonitor', 'ScheduledDailyBatchJobs', 'ScheduledDailyBatchJobsMonitor', 'As400EventSync', 'GemsGeneralLedgerUpload', 'GemsGeneralLedgerUploadMonitor', 'AchOffloadCompleteMonitor', 'EventsGateway', 'RAFWriter', 'EftpsEnrollmentsAgeOutMonitor', 'EftpsPaymentMonitor', 'PSPToAs400DataSyncMonitor', 'EftpsEnrollmentsAgeOut', 'EftpsPayment', 'EftpsEnrollments', 'OFACReportProcessor', 'AMLReportProcessor', 'IndustryReportProcessor', 'OFACReportMonitor', 'AMLReportMonitor', 'IndustryReportMonitor', 'BRMAssistedUsageErrorFileProcessor', 'ATFDataExtract', 'TaxPaymentSubmission', 'TaxPaymentSynchronization', 'TriggerAmendments', 'HPDEBatchProcessor', 'SalesTaxExceptionProcessor', 'TPSUReportProcessor', 'TPSUReportMonitor', 'ATFDepositFrequencyExtract', 'PrimaryDailyForecast', 'CheckPrint', 'CheckPrintMonitor', 'TaxCreditsEchoSignMonitor', 'OffloadedTransactionsEvents', 'OffloadedTransactionsEventsMonitor', 'ThirdParty401kOffload', 'ThirdParty401kSignup', 'ThirdParty401kValidation', 'ThirdParty401kOffloadMonitor', 'ThirdParty401kSignupMonitor', 'ThirdParty401kValidationMonitor', 'AssistedUsageReportingToBRMProcessor', 'AssistedUsageDataSyncProcessor', 'As400DataSync', 'As400DataSyncMonitor', 'TaxCreditsEchoSign', 'SoxDBUserReport', 'QbdtUnprocessedRequestsRetry', 'EftpsEnrollmentsMonitor', 'IOPDataSync', 'PSPToAs400DataSync', 'IOPDataSyncMonitor', 'ATFCompanyLiabilityExtract', 'ATFCompanyPaymentExtract', 'AchTaxPaymentOffloadMonitor', 'PrintedCheckBatch', 'PrintedCheckBatchMonitor', 'AchTaxPaymentOffload', 'AchZeroPayments', 'AchZeroPaymentsMonitor', 'ReconPlus', 'ReconPlusMonitor', 'StateReportMonitor', 'StateReport', 'StateCouponMonitor', 'StateCoupon', 'ComplianceToolKit', 'PSPToAs400', 'PSPToAs400Monitor', 'AMOMessageProcessorMonitor', 'EftpsResponse', 'EntitlementProcessor', 'EntitlementProcessorMonitor', 'AS400EventSyncMonitor', 'AMOMessageProcessor', 'EftpsResponseMonitor', 'EftpsSend', 'EftpsSendMonitor', 'EdiResponse', 'EdiResponseMonitor', 'EdiSend', 'EdiSendMonitor', 'EdiPayment', 'AchDebitOffload', 'AchDebitOffloadMonitor', 'EoqSUIAdjustments', 'EoqSUIAdjustmentsMonitor', 'EntityEvent', 'EMSBSToBRMDataSyncProcessor', 'IRSDepositFrequencyFileProcessorMonitor', 'IRSDepositFrequencyFileProcessor', 'PSPToEMSBSDataSyncProcessor', 'MonthlyFee', 'MonthlyFeeMonitor', 'ATFWageLimitsExtract', 'ATFCompanyInfoExtract', 'ATFEmployeeInfoExtract', 'ATFEmployeeTotalsExtract', 'ATFCompanyTaxExtract', 'ATFCompanyTaxRateExtract', 'ATFEmployeeTotalsCalculation', 'EmployeeW2TotalsCalculationMonitor', 'EmployeeW2TotalsCalculationProcessor', 'SendW2AnnualDataToTFSProcessor', 'FsetFilingProcessor', 'FsetFilingMonitor', 'FsetResponseProcessor', 'FsetResponseMonitor', 'ScheduledEmails', 'LedgerBalanceMonitor', 'LedgerOperations', 'W2CountsExtract', 'WorkersCompProcessor', 'WorkersCompMonitor', 'AnnualBillingProcessor', 'SendMonthlyDataToTFSMonitor', 'SendMonthlyDataToTFSProcessor', 'ACHDeEnrollmentBatchJob', 'ACHEnrollmentResponseBatchJob', 'ACHEnrollmentBatchJob', 'IamEmailAddressMonitor', 'IamEmailAddressProcessor', 'EnrollmentDeleteSelectionMonitor', 'DDMAchOffloadMonitor', 'DDMDailyBatchJobs', 'DataPartitionProcessor', 'EntityEventRetry', 'ACHTraceIdProcessor', 'ATFCompanyPayrollItemExtract', 'MassCancelAutoProcessor', 'AccountServiceSyncExceptionProcessor', 'SoxReport', 'MtlTransactionReportEnrichProcessor', 'EntityInitialLoadProcessor', 'EVSCompanyProcessor', 'DataReencryptionProcessor', 'BulkWorkforceInviteProcessor', 'MTLCompanyToOnHoldProcessor', 'PSPToSMSMigrationProcessor', 'EMSBSToBRMDataSyncProcessorMonitor', 'RiskProfileMigrationProcessor', 'CompanyMigrationProcessor', 'RealTimeEntityEventRetryProcessor')) NOT VALID;



ALTER TABLE pspadm.psp_batch_job_status DROP CONSTRAINT c_psp_batch_job_status0 ;
--CHECK (job_type IN ('FailedPayrollPlSqlJobsProcessor', 'PayrollFraudBatchPurgePlSqlJobsProcessor', 'PSPEventLogPurgePlSqlJobsProcessor', 'DailyPayrollStatsPlSqlJobsProcessor', 'EFTPSOnHoldPaymentPlSqlJobsProcessor', 'ValidateEmployeeWagePlansPlSqlJobsProcessor', 'EmployeePayrollItemTotalsCalcProcess', 'SendW2AnnualDataToTFSMonitor', 'SendW2PreviewDataToTFSProcessor', 'SendW2PreviewDataToTFSMonitor', 'SUICreditsBatchJob', 'AnnualBillingMonitor', 'SalesTaxExceptionMonitor', 'EnrollmentDeleteSelectionProcessor', 'NCDFixPlSqlJobsProcessor', 'OfferingUpdateUsageBillingPlSqlJobsProcessor', 'BRMUsageErrorFileProcessor', 'EDRAssociationFixPlSqlJobsProcessor', 'RetryEntitlementActivationPlSqlJobsProcessor', 'NCDFixALLPlSqlJobsProcessor', 'CostCoPlSqlJobsProcessor', 'EmployeeTotalsCalculationProcess', 'EmployeeTotalsCalculationMonitor', 'RTBAutomation', 'AchReturnsMonitor', 'EdiPaymentMonitor', 'AchTransactionsMonitor', 'BalanceFileMonitor', 'EmailGateway', 'EmailGatewayMonitor', 'FeeEvents', 'FeeEventsMonitor', 'FraudPayrolls', 'FraudPayrollsMonitor', 'GemsAccountsReceivable', 'GemsAccountsReceivableMonitor', 'GemsGeneralLedger', 'GemsGeneralLedgerMonitor', 'LedgerBalance', 'MissedPayrollsMonitor', 'MissedTransactionsMonitor', 'NightlyBatchJobs', 'NightlyBatchJobsMonitor', 'PrimaryAchOffloadMonitor', 'PrimaryDailyBatchJobs', 'PrimaryDailyBatchJobsMonitor', 'ScheduledAchOffloadMonitor', 'ScheduledDailyBatchJobs', 'ScheduledDailyBatchJobsMonitor', 'As400EventSync', 'GemsGeneralLedgerUpload', 'GemsGeneralLedgerUploadMonitor', 'AchOffloadCompleteMonitor', 'EventsGateway', 'RAFWriter', 'EftpsEnrollmentsAgeOutMonitor', 'EftpsPaymentMonitor', 'PSPToAs400DataSyncMonitor', 'EftpsEnrollmentsAgeOut', 'EftpsPayment', 'EftpsEnrollments', 'OFACReportProcessor', 'AMLReportProcessor', 'IndustryReportProcessor', 'OFACReportMonitor', 'AMLReportMonitor', 'IndustryReportMonitor', 'BRMAssistedUsageErrorFileProcessor', 'ATFDataExtract', 'TaxPaymentSubmission', 'TaxPaymentSynchronization', 'TriggerAmendments', 'HPDEBatchProcessor', 'SalesTaxExceptionProcessor', 'TPSUReportProcessor', 'TPSUReportMonitor', 'ATFDepositFrequencyExtract', 'PrimaryDailyForecast', 'CheckPrint', 'CheckPrintMonitor', 'TaxCreditsEchoSignMonitor', 'OffloadedTransactionsEvents', 'OffloadedTransactionsEventsMonitor', 'ThirdParty401kOffload', 'ThirdParty401kSignup', 'ThirdParty401kValidation', 'ThirdParty401kOffloadMonitor', 'ThirdParty401kSignupMonitor', 'ThirdParty401kValidationMonitor', 'AssistedUsageReportingToBRMProcessor', 'AssistedUsageDataSyncProcessor', 'As400DataSync', 'As400DataSyncMonitor', 'TaxCreditsEchoSign', 'SoxDBUserReport', 'QbdtUnprocessedRequestsRetry', 'EftpsEnrollmentsMonitor', 'IOPDataSync', 'PSPToAs400DataSync', 'IOPDataSyncMonitor', 'ATFCompanyLiabilityExtract', 'ATFCompanyPaymentExtract', 'AchTaxPaymentOffloadMonitor', 'PrintedCheckBatch', 'PrintedCheckBatchMonitor', 'AchTaxPaymentOffload', 'AchZeroPayments', 'AchZeroPaymentsMonitor', 'ReconPlus', 'ReconPlusMonitor', 'StateReportMonitor', 'StateReport', 'StateCouponMonitor', 'StateCoupon', 'ComplianceToolKit', 'PSPToAs400', 'PSPToAs400Monitor', 'AMOMessageProcessorMonitor', 'EftpsResponse', 'EntitlementProcessor', 'EntitlementProcessorMonitor', 'AS400EventSyncMonitor', 'AMOMessageProcessor', 'EftpsResponseMonitor', 'EftpsSend', 'EftpsSendMonitor', 'EdiResponse', 'EdiResponseMonitor', 'EdiSend', 'EdiSendMonitor', 'EdiPayment', 'AchDebitOffload', 'AchDebitOffloadMonitor', 'EoqSUIAdjustments', 'EoqSUIAdjustmentsMonitor', 'EntityEvent', 'EMSBSToBRMDataSyncProcessor', 'IRSDepositFrequencyFileProcessorMonitor', 'IRSDepositFrequencyFileProcessor', 'PSPToEMSBSDataSyncProcessor', 'MonthlyFee', 'MonthlyFeeMonitor', 'ATFWageLimitsExtract', 'ATFCompanyInfoExtract', 'ATFEmployeeInfoExtract', 'ATFEmployeeTotalsExtract', 'ATFCompanyTaxExtract', 'ATFCompanyTaxRateExtract', 'ATFEmployeeTotalsCalculation', 'EmployeeW2TotalsCalculationMonitor', 'EmployeeW2TotalsCalculationProcessor', 'SendW2AnnualDataToTFSProcessor', 'FsetFilingProcessor', 'FsetFilingMonitor', 'FsetResponseProcessor', 'FsetResponseMonitor', 'ScheduledEmails', 'LedgerBalanceMonitor', 'LedgerOperations', 'W2CountsExtract', 'WorkersCompProcessor', 'WorkersCompMonitor', 'AnnualBillingProcessor', 'SendMonthlyDataToTFSMonitor', 'SendMonthlyDataToTFSProcessor', 'ACHDeEnrollmentBatchJob', 'ACHEnrollmentResponseBatchJob', 'ACHEnrollmentBatchJob', 'IamEmailAddressMonitor', 'IamEmailAddressProcessor', 'EnrollmentDeleteSelectionMonitor', 'DDMAchOffloadMonitor', 'DDMDailyBatchJobs', 'DataPartitionProcessor', 'EntityEventRetry', 'ACHTraceIdProcessor', 'ATFCompanyPayrollItemExtract', 'MassCancelAutoProcessor', 'AccountServiceSyncExceptionProcessor', 'SoxReport', 'MtlTransactionReportEnrichProcessor', 'EntityInitialLoadProcessor', 'EVSCompanyProcessor', 'DataReencryptionProcessor', 'BulkWorkforceInviteProcessor', 'MTLCompanyToOnHoldProcessor', 'PSPToSMSMigrationProcessor', 'EMSBSToBRMDataSyncProcessorMonitor', 'RiskProfileMigrationProcessor', 'CompanyMigrationProcessor', 'RealTimeEntityEventRetryProcessor')) NOT VALID;

ALTER TABLE pspadm.psp_batch_job_status DROP CONSTRAINT c_psp_batch_job_status1 UNIQUE (job_type);



ALTER TABLE pspadm.psp_bill_payment DROP CONSTRAINT c_psp_bill_payment0 ;
--CHECK (status IN ('Active', 'Inactive')) NOT VALID;

ALTER TABLE pspadm.psp_bill_payment DROP CONSTRAINT c_psp_bill_payment1 ;
--CHECK (transaction_type IN ('PayBills', 'WriteChecks')) NOT VALID;



ALTER TABLE pspadm.psp_billing_detail DROP CONSTRAINT c_psp_billing_detail0 ;
--CHECK (offering_service_charge_type IN ('CourtesyRefund', 'PerPayroll', 'PerPaycheck', 'PerTransmission', 'AmendedSSN', 'PerPayment', 'ReversalFee', 'DebitReturnFee', 'ManualServicingFee', 'ChaseReportFeeUpTo3Payrolls', 'ChaseReportFeeUpTo6Payrolls', 'ChaseReportFeeUpTo15Payrolls', 'ChaseReportFeeUpTo20Payrolls', 'ChaseReportFeeOver20Payrolls', 'BankVerificationDebit', 'PaymentArrangementFee', 'PerBatch', 'CompanyUpdates', 'EmployeesAdded', 'EmployeesUpdated', 'BackdatedPayroll', 'DirectDepositFee', 'EmployeesPaid', 'MonthlyFee', 'Amendments', 'EntityChange', 'ExtraCopies', 'PayrollAdjustment', 'PenaltiesAndInterest', 'ExtraStateFee', 'OtherFee', 'W2Fee', 'EmployeeOrganizerFee', 'W2Correction', 'W2BaseFee', 'BankVerificationCredit')) NOT VALID;



ALTER TABLE pspadm.psp_check_print_batch DROP CONSTRAINT c_psp_check_print_batch0 ;
--CHECK (check_print_batch_status_code IN ('Pending', 'SentToPrinter', 'Error')) NOT VALID;

ALTER TABLE pspadm.psp_check_print_batch--ADD --PRIMARY KEY (check_print_batch_seq);

ALTER TABLE pspadm.psp_check_print_paycheck DROP CONSTRAINT c_psp_check_print_paycheck0 ;
--CHECK (cp_paycheck_status_code IN ('ReceivedWithNoCheckNumber', 'VoidedBeforePrinting', 'AddedToPrintBatch', 'DeletedBeforePrinting')) NOT VALID;



ALTER TABLE pspadm.psp_collection_stage DROP CONSTRAINT c_psp_collection_stage0 ;
--CHECK (collection_stage_code IN ('FirstCollectionAttempt', 'SecondCollectionAttempt', 'TerminationExpected'));


ALTER TABLE pspadm.psp_comp_pmttemplate_pmtmethod DROP CONSTRAINT c_psp_comp_pmttemplate_pmt0 ;
--CHECK (payment_method IN ('ACHDebit', 'ACHCredit', 'CheckPayment', 'PostBalfHPDE', 'PostBalfHPDERefund', 'ACHDirectDeposit', 'WirePayment', 'EFE', 'HPDERefund', 'HPDE', 'EFTPS', 'EFTPSDirectDebit', 'EDI', 'SuperCheck')) NOT VALID;



ALTER TABLE pspadm.psp_company DROP CONSTRAINT c_psp_company0 ;
--CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) NOT VALID;

ALTER TABLE pspadm.psp_company DROP CONSTRAINT c_psp_company1 ;
--CHECK (tax_exempt_status IN ('Exempt', 'New', 'NonExempt')) NOT VALID;


ALTER TABLE pspadm.psp_company_bank_account DROP CONSTRAINT c_psp_company_bank_account0 ;
--CHECK (status_cd IN ('PendingVerification', 'Active', 'Inactive'));



ALTER TABLE pspadm.psp_company_event_email DROP CONSTRAINT c_psp_company_event_email0 ;
--CHECK (status_cd IN ('SendFailedInvalidEmailId', 'SendSkippedInvalidEmailId', 'Pending', 'Sent', 'Ignore', 'Resend', 'SendFailed', 'GroupIncomplete', 'FormatError', 'PendingResend')) NOT VALID;

ALTER TABLE pspadm.psp_company_event_email DROP CONSTRAINT c_psp_company_event_email1 ;
--CHECK (email_template_type_cd IN ('AdditionalMedicareTaxDebitNotification', 'VendorInvalidEmail', 'EmployerNOC52LoanAccount', 'SameDayMoFedAssessmentDebit', 'SUICreditNotification', 'SymphonyWelcomeOneMonthReactivation', 'SameDayNVBondDebitNotification', 'BulkCreditDebitNotification', 'RTBAutomationCleanUp', 'ServiceCancelledConfirmation1', 'AssistedFailedEnrollment', 'BulkCreditDebitNotificationSUPNY', 'AssistedPayrollConfirmation', 'AllPaycheckReversalsFailed', 'AllPaycheckReversalsSuccessful', 'AutoRedebit', 'AutoRedebitFourStrikes', 'BankVerificationFailed', 'BilledNonPayrollRelatedFee', 'CustomerInitiatedDDReversal', 'DDBankVerificationReminder', 'DDBankVerificationSuccessful', 'DDERBankAccountChange', 'DDPINChangeConfirmation', 'DDServiceCancelledConfirmation', 'DDSignupConfirmation', 'ERandEENOC2', 'LastChanceEmail', 'ManualRedebit', 'NonACHPaymentReceivedInFull', 'NonACHPaymentReceivedInFullActionRequired', 'NonACHPaymentReceivedLiabilityOutstanding', 'PartialPaycheckReversal', 'PayrollCancellationNotification', 'DebitReturned', 'DebitReturnedFourStrikes', 'EEDDREJECT', 'EmailChangeNotification', 'EmployeeNOC', 'EmployeeNOC2', 'EmployerNOC', 'ERandEENOC', 'PayrollCancelledNotification', 'QBDTPayrollConfirmation', 'RedebitFailed', 'RefundedFeeAmount', 'RefundWithRebillFeeAmount', 'WireExpectedNotification', 'EFTPSEnrollmentRejectedEIN', 'EFTPSEnrollmentRejectedName', 'BankVerifyAttemptFailed', 'DDEEBankAccountChange', 'TOKFraudNotification', 'TOKVoidDelete', 'VendorPaymentSignupConfirmation', 'VendorPaymentReceived', 'VendorPaymentOffloaded1', 'ManualRedebit2', 'DebitReturnedFourStrikes3', 'WireExpectedNotification3', 'DebitReturned3', 'AutoRedebit2', 'LastChanceEmail3', 'PayrollCancellationNotification2', 'PayrollCancelledNotification2', 'NonPrintChecks', 'Correct401kEmployeeInfo', 'Correct401kEmployeeInfoAfterSend', 'VendorPaymentReceived1', 'VendorPaymentOffloadedForWriteChecks', 'VendorPaymentOffloadedForPayBills', 'SKDiskDeliveryKey1', 'SKBasicKey1', 'SKFreeBasicKey1', 'SKEnhancedKey1', 'SKEnhancedKeyAccount1', 'SKStandardKey1', 'SKDefaultKey1', 'SameDaySUIDebitNotification3', 'SUIRefundNotification3', 'EndofQuarterSUIDebitNotification3', 'DDERBankAccountChangeAssisted', 'EmployeeNOCAssisted', 'EmployerNOC1', 'LastChanceEmail1', 'LastChanceEmail4', 'ManualRedebit3', 'NonACHPaymentReceivedInFull1', 'NonACHPMTReceivedLiabOutstanding1', 'PartialPaycheckReversal1', 'RedebitFailed1', 'WireExpectedNotification4', 'AllPaycheckReversalsFailed1', 'AllPaycheckReversalsSuccessful1', 'AutoRedebit3', 'BankVerificationFailed1', 'BilledNonPayrollRelatedFee1', 'CustomerInitiatedDDReversal1', 'DDBankVerificationReminder1', 'DDPINChangeConfirmation1', 'DebitReturned1', 'DebitReturned4', 'EEDDREJECT1', 'RefundedFeeAmount1', 'RefundWithRebillFeeAmount1', 'DDEEBankAccountChange1', 'SymphonyWelcomeNoTrial', 'SymphonyBillingDetailsMonthly', 'UsageBillingMidTrial', 'SymphonyWelcomeFreeTrial', 'CreditReductionGeneric', 'CreditReductionFUTA', 'SymphonyBillingDetailsAnnual', 'FUTACreditReduction', 'BilledNonPayrollRelatedFee2', 'SUIRefundNotification4', 'SameDaySUIDebitNotification4', 'EndofQuarterSUIDebitNotification4', 'VmpEmployeeWelcome', 'VmpEmployerWelcome', 'VmpPaystubNotification', 'SameDayMAUHIDebitNotification', 'MinimumMonthlyBilling', 'DesktopAMLHoldRemoved', 'DesktopAMLHoldApplied', 'DDPayeeBankAccountChange', 'NewPayrollAccountAddedToEntitlement', 'QBDTPayrollConfirmationMTL', 'VendorPaymentReceived1MTL', 'SKAssistedKey1')) NOT VALID;


ALTER TABLE pspadm.psp_company_law DROP CONSTRAINT c_psp_company_law1 ;
--CHECK (status IN ('Active', 'Inactive')) NOT VALID;

ALTER TABLE pspadm.psp_company_law DROP CONSTRAINT c_psp_company_law2 ;
--CHECK (filing_status IN ('Active', 'Inactive')) NOT VALID;

ALTER TABLE pspadm.psp_company_law DROP CONSTRAINT c_psp_company_law3 ;
--CHECK (reimbursable_status IN ('Reimbursable', 'NotReimbursable')) NOT VALID;



ALTER TABLE pspadm.psp_company_law_rate DROP CONSTRAINT c_psp_company_law_rate0 ;
--CHECK (rate_type IN ('MoneyType', 'Percentage'));



ALTER TABLE pspadm.psp_company_payroll_item DROP CONSTRAINT c_psp_company_payroll_item0 ;
--CHECK (status IN ('Active', 'Inactive')) NOT VALID;

ALTER TABLE pspadm.psp_company_payroll_item--ADD --PRIMARY KEY (company_payroll_item_seq);

ALTER TABLE pspadm.psp_company_pin DROP CONSTRAINT c_psp_company_pin0 ;
--CHECK (hash_type IN ('SHA256', 'SHA', 'SHA512', 'AS400')) NOT VALID;



ALTER TABLE pspadm.psp_company_rate_request DROP CONSTRAINT c_psp_company_rate_request0 ;
--CHECK (status IN ('Waiting', 'Applied', 'NoChange', 'Error')) NOT VALID;



ALTER TABLE pspadm.psp_company_service DROP CONSTRAINT c_psp_company_service0 ;
--CHECK (status_cd IN ('AchRejectOther', 'AchRejectR1R9', 'ActiveCurrent', 'ActiveSeasonal', 'Cancelled', 'DirectDepositLimit', 'Fraud', 'AuditCorrections', 'FraudReview', 'IntuitCollections', 'MissingPaperwork', 'PendingBalanceFile', 'PendingBankVerification', 'PendingFirstPayroll', 'PendingPinCreation', 'PendingTermination', 'RiskAssessment', 'RiskCollections', 'SuspendedDirectDeposit', 'Terminated', 'AMLHold', 'PendingTaxAcceptance', 'PendingEnrollment', 'PendingPrefundingWire', 'AS400Hold', 'AS400DirectDepositLimitHold', 'PendingSetup', 'BillPaymentLimit', 'MTLHold')) NOT VALID;



ALTER TABLE pspadm.psp_company_tfssubmission DROP CONSTRAINT c_psp_company_tfssubmission0 ;
--CHECK (submission_status IN ('Submitted', 'Error', 'Pending')) NOT VALID;

ALTER TABLE pspadm.psp_company_tfssubmission--ADD --PRIMARY KEY (company_tfssubmission_seq);

ALTER TABLE pspadm.psp_company_usage DROP CONSTRAINT c_psp_company_usage0 ;
--CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) NOT VALID;


ALTER TABLE pspadm.psp_contact DROP CONSTRAINT c_psp_contact0 ;
--CHECK (contact_role_cd IN ('PayrollAdmin', 'Other', 'PrimaryPrincipal', 'SecondaryPrincipal'));



ALTER TABLE pspadm.psp_deposit_frequency DROP CONSTRAINT c_psp_deposit_frequency0 ;
--CHECK (deposit_frequency_code IN ('ACCELERATED', 'ANNUAL', 'EARLYFILER', 'EIGHTHMONTHLY', 'FIVEBANKINGDAY', 'MONTHLY', 'MONTHLYACCELERATED', 'NEXTBANKINGDAY', 'QUADMONTHLY', 'QUARTERLY', 'QUARTERMONTHLY', 'SEMIANNUAL', 'SEMIMONTHLY', 'SEMIWEEKLY', 'SPLITMONTHLY', 'THREEBANKINGDAY', 'TWICEMONTHLY', 'NOCALC', 'WEEKLY')) NOT VALID;



ALTER TABLE pspadm.psp_deposit_frequency_file DROP CONSTRAINT c_psp_deposit_frequency_fi0 ;
--CHECK (status IN ('Processed', 'Received', 'Skipped')) NOT VALID;



ALTER TABLE pspadm.psp_deposit_frequency_file_rec DROP CONSTRAINT c_psp_deposit_frequency_fi1 ;
--CHECK (status IN ('Error', 'Processed', 'Received', 'SkippedCompanyDoesNotExist', 'InvalidData', 'SkippedUpdating')) NOT VALID;



ALTER TABLE pspadm.psp_deposit_frequency_req DROP CONSTRAINT c_psp_deposit_frequency_req0 ;
--CHECK (prohibited_deposit_frequency IN ('ACCELERATED', 'ANNUAL', 'EARLYFILER', 'EIGHTHMONTHLY', 'FIVEBANKINGDAY', 'MONTHLY', 'MONTHLYACCELERATED', 'NEXTBANKINGDAY', 'QUADMONTHLY', 'QUARTERLY', 'QUARTERMONTHLY', 'SEMIANNUAL', 'SEMIMONTHLY', 'SEMIWEEKLY', 'SPLITMONTHLY', 'THREEBANKINGDAY', 'TWICEMONTHLY', 'NOCALC', 'WEEKLY')) NOT VALID;


ALTER TABLE pspadm.psp_dicrfile DROP CONSTRAINT c_psp_dicrfile0 ;
--CHECK (status IN ('Processed', 'Archived'));



ALTER TABLE pspadm.psp_edi_payment_detail DROP CONSTRAINT c_psp_edi_payment_detail0 ;
--CHECK (status_cd IN ('Ignore', 'RejectedByAgency', 'SentToAgency', 'AcknowledgedByAgency', 'ReturnedTaxNotPaid', 'ReturnedTaxPaid', 'ReadyToSend', 'OnHold', 'None', 'ATFFinalized')) NOT VALID;



ALTER TABLE pspadm.psp_edi_tax_file DROP CONSTRAINT c_psp_edi_tax_file0 ;
--CHECK (file_type IN ('StateEdiPayment', 'StateEdiPaymentAck', 'StateEdiPaymentResponse', 'EftpsEnrollmentResponse', 'EftpsForecast', 'EftpsPayment', 'EftpsPaymentResponse', 'EftpsPaymentConfirmation', 'EftpsPaymentReturn', 'EftpsEnrollment', 'EftpsEnrollmentAck', 'EftpsEnrollmentResponseAck', 'EftpsForecastAck', 'EftpsPaymentAck', 'EftpsPaymentConfirmationAck', 'EftpsPaymentResponseAck', 'EftpsPaymentReturnAck')) NOT VALID;

ALTER TABLE pspadm.psp_edi_tax_file DROP CONSTRAINT c_psp_edi_tax_file1 ;
--CHECK (status_cd IN ('InProcess', 'PendingTransmission', 'Completed', 'Error', 'Archived', 'SendToAS400')) NOT VALID;

ALTER TABLE pspadm.psp_edi_tax_file DROP CONSTRAINT c_psp_edi_tax_file2 ;
--CHECK (system_owner IN ('PSP', 'AS400')) NOT VALID;


ALTER TABLE pspadm.psp_eftps_enrollment DROP CONSTRAINT c_psp_eftps_enrollment0 ;
--CHECK (status_cd IN ('PendingEnrollment', 'Cancelled', 'AgedOut', 'PendingAcceptance', 'Enrolled', 'Rejected', 'Invalid', 'None')) NOT VALID;


ALTER TABLE pspadm.psp_eftps_enrollment_detail DROP CONSTRAINT c_psp_eftps_enrollment_det0 ;
--CHECK (status_cd IN ('PendingEnrollment', 'Cancelled', 'AgedOut', 'PendingAcceptance', 'Enrolled', 'Rejected', 'Invalid', 'None')) NOT VALID;



ALTER TABLE pspadm.psp_eftps_file DROP CONSTRAINT c_psp_eftps_file0 ;
--CHECK (file_subtype IN ('PaymentNextDay', 'PaymentSameDay', 'Payment100k', 'None')) NOT VALID;



ALTER TABLE pspadm.psp_eftps_payment_detail DROP CONSTRAINT c_psp_eftps_payment_detail0 ;
--CHECK (return_cd IN ('R01', 'R02', 'R03', 'R04', 'R05', 'R06', 'R07', 'R08', 'R09', 'R20', 'R24', 'R28', 'R29', 'R10', 'R12', 'R13', 'R14', 'R15', 'R16', 'R18', 'R11', 'R17', 'R19', 'R21', 'R22', 'R23', 'R25', 'R26', 'R27', 'R30', 'R31', 'R32', 'R33', 'R34', 'R35', 'R36', 'R37', 'R38', 'R39', 'R40', 'R41', 'R42', 'R43', 'R44', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R51', 'R52', 'R53', 'C01', 'R61', 'R62', 'R63', 'C02', 'C03', 'C04', 'C05', 'C06', 'C07', 'R64', 'R65', 'R66', 'R67', 'R68', 'R69', 'R70', 'R71', 'R72', 'R73', 'R74', 'R75', 'R76', 'R80', 'R81', 'C08', 'C09', 'C10', 'R82', 'R83', 'R84', 'C11', 'C12', 'C13', 'C61', 'C62', 'C63', 'R99', 'C64', 'C65', 'C66', 'C67', 'C68', 'C69', 'C99')) NOT VALID;

ALTER TABLE pspadm.psp_eftps_payment_detail DROP CONSTRAINT c_psp_eftps_payment_detail1 ;
--CHECK (status_cd IN ('Ignore', 'RejectedByAgency', 'SentToAgency', 'AcknowledgedByAgency', 'ReturnedTaxNotPaid', 'ReturnedTaxPaid', 'ReadyToSend', 'OnHold', 'None', 'ATFFinalized')) NOT VALID;


ALTER TABLE pspadm.psp_emp_totals_payroll_run DROP CONSTRAINT c_psp_emp_totals_payroll_r0 ;
--CHECK (status IN ('Pending', 'Processed')) NOT VALID;



ALTER TABLE pspadm.psp_employee DROP CONSTRAINT c_psp_employee0 ;
--CHECK (status_cd IN ('Active', 'Inactive'));

ALTER TABLE pspadm.psp_employee DROP CONSTRAINT c_psp_employee1 ;
--CHECK (pay_period IN ('Annually', 'SemiAnnually', 'Quarterly', 'Monthly', 'SemiMonthly', 'BiWeekly', 'Weekly', 'Daily')) NOT VALID;

ALTER TABLE pspadm.psp_employee DROP CONSTRAINT psp_emp_comp_fk_src_emp_id UNIQUE (company_fk, source_employee_id);


ALTER TABLE pspadm.psp_employee_accrual DROP CONSTRAINT c_psp_employee_accrual0 ;
--CHECK (accrual_period IN ('Hourly', 'Payroll', 'Yearly')) NOT VALID;

ALTER TABLE pspadm.psp_employee_accrual DROP CONSTRAINT c_psp_employee_accrual1 ;
--CHECK (accrual_type IN ('Sick', 'Vacation')) NOT VALID;



ALTER TABLE pspadm.psp_employee_bank_account DROP CONSTRAINT c_psp_employee_bank_account0 ;
--CHECK (status_cd IN ('PendingVerification', 'Active', 'Inactive'));

ALTER TABLE pspadm.psp_employee_bank_account DROP CONSTRAINT c_psp_employee_bank_account1 ;
--CHECK (amount_type IN ('MoneyType', 'Percentage')) NOT VALID;



ALTER TABLE pspadm.psp_employee_payroll_item DROP CONSTRAINT c_psp_employee_payroll_item0 ;
--CHECK (type IN ('Adjustment', 'Wage')) NOT VALID;

ALTER TABLE pspadm.psp_employee_payroll_item DROP CONSTRAINT c_psp_employee_payroll_item1 ;
--CHECK (amount_type IN ('MoneyType', 'Percentage')) NOT VALID;

ALTER TABLE pspadm.psp_employee_payroll_item DROP CONSTRAINT c_psp_employee_payroll_item2 ;
--CHECK (limit_type IN ('MoneyType', 'Percentage')) NOT VALID;


ALTER TABLE pspadm.psp_employee_tax DROP CONSTRAINT c_psp_employee_tax0 ;
--CHECK (tax_type IN ('SIT', 'FIT', 'FICA', 'MED', 'FUTA', 'SUI', 'SDI', 'Other')) NOT VALID;

ALTER TABLE pspadm.psp_employee_tax DROP CONSTRAINT c_psp_employee_tax1 ;
--CHECK (extra_withholding_type IN ('MoneyType', 'Percentage')) NOT VALID;


ALTER TABLE pspadm.psp_employee_wage_plan DROP CONSTRAINT c_psp_employee_wage_plan0 ;
--CHECK (name IN ('WPC', 'GC', 'OC', 'FCC')) NOT VALID;

ALTER TABLE pspadm.psp_employee_wage_plan DROP CONSTRAINT c_psp_employee_wage_plan1 ;
--CHECK (wage_plan_domain IN ('WorkOrLiveState', 'WorkState')) NOT VALID;


ALTER TABLE pspadm.psp_entitlement DROP CONSTRAINT c_psp_entitlement0 ;
--CHECK (entitlement_state IN ('Disabled', 'Enabled')) NOT VALID;

ALTER TABLE pspadm.psp_entitlement DROP CONSTRAINT c_psp_entitlement1 ;
--CHECK (payment_method_type IN ('EFT', 'CC', 'PAPER;
--CHECK')) NOT VALID;

ALTER TABLE pspadm.psp_entitlement DROP CONSTRAINT c_psp_entitlement2 ;
--CHECK (order_source_cd IN ('Siebel', 'EStore', 'FallDM2011')) NOT VALID;


ALTER TABLE pspadm.psp_entitlement_code DROP CONSTRAINT c_psp_entitlement_code0 ;
--CHECK (asset_type_cd IN ('WorkersComp', 'Payroll', 'Usage', 'Trial')) NOT VALID;

ALTER TABLE pspadm.psp_entitlement_code DROP CONSTRAINT c_psp_entitlement_code1 ;
--CHECK (asset_item_cd IN ('Assisted', 'AssistedAdvantage', 'DIY', 'DIYDiskDelivery', 'EmployeeOrganizer', 'EmploymentRegulation'));

ALTER TABLE pspadm.psp_entitlement_code DROP CONSTRAINT c_psp_entitlement_code2 ;
--CHECK (edition_type IN ('EnhancedAccountantProAdvisor', 'Basic', 'Enhanced', 'EnhancedAccountant', 'Standard'));

ALTER TABLE pspadm.psp_entitlement_code DROP CONSTRAINT c_psp_entitlement_code3 ;
--CHECK (number_of_employees_type IN ('ONE', 'UPTO3', 'UNLIMITED'));

ALTER TABLE pspadm.psp_entitlement_code DROP CONSTRAINT c_psp_entitlement_code4 ;
--CHECK (billing_frequency_type IN ('Monthly', 'Annually'));



ALTER TABLE pspadm.psp_entitlement_code_offering DROP CONSTRAINT c_psp_entitlement_code_off0 ;
--CHECK (service_cd IN ('DirectDeposit', 'Tax', 'BillPayment', 'ThirdParty401k', 'CheckDistribution', 'Cloud', 'RiskAssessment', 'WorkersComp', 'ViewMyPaycheck', 'CloudV2', 'Guideline401k'));



ALTER TABLE pspadm.psp_entitlement_message DROP CONSTRAINT c_psp_entitlement_message0 ;
--CHECK (status IN ('SkippedEntitlementNotFound', 'SkippedOldTimestamp', 'New', 'Processed', 'Error')) NOT VALID;



ALTER TABLE pspadm.psp_entitlement_unit DROP CONSTRAINT c_psp_entitlement_unit0 ;
--CHECK (entitlement_unit_status IN ('ActivationHold', 'DeactivationHold', 'Deactivated', 'Activated', 'PendingActivation', 'PendingDeactivation', 'PendingReactivation', 'ErrorDeactivating', 'ErrorActivating', 'Historic')) NOT VALID;


ALTER TABLE pspadm.psp_entity_update_hist DROP CONSTRAINT c_psp_entity_update0 ;
--CHECK (status IN ('Created', 'Published', 'Failed', 'IQFailed', 'IQPublished', 'InProgress')) NOT VALID;

ALTER TABLE pspadm.psp_entity_update_hist DROP CONSTRAINT c_psp_entity_update1 ;
--CHECK (event_type IN ('EntityCreate', 'EntityUpdate', 'EntityDelete'));


ALTER TABLE pspadm.psp_entry_detail_record DROP CONSTRAINT sys_c008379 --PRIMARY KEY (company_fk, entry_detail_record_seq);

ALTER TABLE pspadm.psp_event_as400_sync DROP CONSTRAINT c_psp_event_as400_sync0 ;
--CHECK (status_cd IN ('Pending', 'Complete', 'Error', 'NoSend')) NOT VALID;



ALTER TABLE pspadm.psp_event_detail_type DROP CONSTRAINT c_psp_event_detail_type0 ;
--CHECK (event_detail_type_cd IN ('EmailTemplateType', 'VendorInvalidEmail', 'OldAchAccountType', 'NewAchAccountType', 'OldCompanyBankAccountId', 'OldPayeeBankAccountNumber', 'ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'CancellationDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'FeeAmount', 'RefundedFeeBillingDetailId', 'StrikeReason', 'ReasonDescription', 'NoteText', 'FailureReason', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldServiceStatus', 'NewServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount', 'ExpectedToken', 'ReceivedToken', 'Timestamp', 'UniqueIdentifier', 'UserId', 'ServiceCode', 'ThresholdPeriodStartDate', 'AgencyId', 'EnrollmentType', 'PaymentEFTNumber', 'PaymentAcknowledgeNumber', 'MoneyMovementTransactionId', 'CompanyTIN', 'PaymentPeriodEndDate', 'NewPayeeBankAccountNumber', 'PaymentInitiationDate', 'PaymentDueDate', 'PaymentMethod', 'OldPayeeBankRoutingNumber', 'NewPayeeBankRoutingNumber', 'GenericEventDetail', 'NewEffectiveDate', 'NewDepositFrequency', 'OldEffectiveDate', 'OldDepositFrequency', 'PaymentTemplate', 'Percentage', 'EmployeeInvalidReason', 'SourceEmployeeId', 'MessageLevel', 'OldEmployeeBankAccountId', 'NewEmployeeBankAccountId', 'BillPaymentId', 'PayeeId', 'PayeeName', 'PayeeBankAccountId', 'PaycheckInvalidReason', 'OverrideRecipientEmailAddress', 'SourceSystemTransmissionInvalidReason', 'CompanyAgency', 'Law', 'NextPaycheckId', 'SourceCompanyId', 'OFXToken', 'NextEmployeeId', 'NextPaylineTransactionId', 'NextPayrollTransactionId', 'Amount', 'Description', 'EntitlementId', 'EntitlementUnitId', 'InvalidatedDepositFrequencyId', 'TransactionType', 'PermanentPaymentFrequencyId', 'ThresholdPeriodEndDate', 'ThresholdReversed', 'PenaltiesRefundAmount', 'InterestRefundAmount', 'TotalRefundAmount', 'RefundDebitAmount', 'ACHEnrollmentId', 'RecipientEmailAddress', 'CaseId', 'FirstPayrollRunDate', 'PayrollCount', 'GrantType', 'WorkflowId', 'AuthId', 'BillingRealmId', 'DataRealmId', 'WorkOrderId', 'EmployeeSequence', 'WorkOrderCreatedTime', 'ServiceKey', 'CompanySequence', 'CompanyName', 'AppName', 'ConsentValue', 'EmailTemplate', 'InvitationSource', 'IUSInvitationId', 'PersonaId', 'NewWalletId', 'OldWalletId', 'CompanyRealmId', 'OldCompanyRealmId', 'ConsumerRealmId', 'OwnerOldLimit', 'OwnerNewLimit', 'PayeeOldLimit', 'PayeeNewLimit', 'VendorId')) NOT VALID;



ALTER TABLE pspadm.psp_event_log DROP CONSTRAINT c_psp_event_log0 ;
--CHECK (event_log_type_cd IN ('Error', 'Debug', 'Warn', 'Info', 'Fatal', 'Statistic')) NOT VALID;

ALTER TABLE pspadm.psp_event_log--ADD --PRIMARY KEY (event_log_seq);

ALTER TABLE pspadm.psp_event_type DROP CONSTRAINT c_psp_event_type0 ;
--CHECK (event_type_cd IN ('FileIdChanged', 'SendEmailFailed', 'PaycheckRecalledAfterOffload', 'SendEmailSkipped', 'InvalidVendorEmail', 'ERLoanNOC', 'NewPSIDCreatedForExistingCustomer', 'PrimaryPrincipalNameChanged', 'PayeeBankAccountChange', 'BackdatePriorToProcessingStart', 'SubscriptionEndDateChanged', 'SUICreditsApplied', 'TrialAssetDetected', 'WelcomeEmail', 'AccountLocked', 'ACHReturnStatusChanged', 'EINChanged', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BackdatedPayrollReceived', 'ACHReturn', 'FeeCreated', 'IncorrectPIN', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollRejected', 'CompanyContactEmailChanged', 'TaxExemptStatusChanged', 'CustomerSignedUp', 'PayrollCancelPending', 'FeeRefunded', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'DBANameChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'CompanyIndustryTypeChanged', 'PrimaryPrincipalSSNChanged', 'PrimaryPrincipalDOBChanged', 'EnrollmentStatusChanged', 'StateIdModified', 'TaxPaymentStatusChanged', 'PaymentMethodChanged', 'DepositFrequencyChanged', 'ThresholdExceeded', 'AssistedEmployeeMigrationComplete', 'OfferingUpdated', 'OfferClaimed', 'OfferRemoved', 'EmployeeInTermedCompany', 'EmployeeBankAccountInTermedCompany', 'PrefundingReceived', 'EmployeePaidTooManyTimes', 'EmployeePaidPercentageGreaterThanOthers', 'EmployeesPaidToSameBank', 'EmployeeBankAccountChangedSpikeInPay', 'EmployeesPaidToSameBankAccount', 'InvalidEmployeeInformation', 'PINUnlocked', 'Employee401kDataUploaded', 'EmployeeBankAccountChange', 'TotalBillPaymentExceedsLimit', 'PayeePaidGreaterThanMax', 'PayeePaidTooManyTimes', 'BillPaymentReceived', 'InvalidPaycheckInformation', 'VoidedPaycheckAlreadyOffloadedToTOK', 'DeletedPaycheckAlreadyOffloadedToTOK', 'TOKNotifiedOfCompanyFraud', 'BillPaymentOffloaded', 'PreOffload401kValidationAlert', 'BillPaymentRecalled', 'NonPrintChecks', 'PayrollReceivedCloud', 'PostOffload401kValidationAlert', 'InvalidSourceSystemTransmissionInformation', 'CloudResponse', 'InactivityDDPayrollAmountExceeded', 'InactivityBPPayrollAmountExceeded', 'AssistedPayrollItemMigrationComplete', 'ERPayableRefundCreated', 'MultipleCompanyLawsCreated', 'BalanceFileReceived', 'CompanyLawUpdated', 'PayrollReceivedPayCard', 'LiabilityAdjustmentCreated', 'PSPToAS400HoldSync', 'PSPToAS400HoldRemoveSync', 'OFXServiceActivated', 'PositiveCobraReceived', 'ManualLedgerEntry', 'PayrollTaxPaymentVoided', 'PayrollTaxPaymentReissued', 'ERPayableAppliedToBalanceDue', 'AIDUpdated', 'AssistedFailedEnrollment', 'AssistedPayrollConfirmation', 'EntitlementStateChanged', 'EntitlementUnitStatusChanged', 'SourceCompanyIdChanged', 'ServiceKeyUpdated', 'EntitlementCodeChanged', 'PriceTypeChanged', 'EntitlementCommunication', 'ManualDataSync', 'AccountingFinancialLedgerAdjustmentCreated', 'SUIEoqDebitCreated', 'SUIEoqCreditCreated', 'SUIImmediateDebitCreated', 'SUIImmediateCreditCreated', 'CompanyContactAdded', 'CompanyContactJobTitleChanged', 'PayrollSubmissionIncludedAllNewEmployees', 'PSIDMismatch', 'CompanyContactDeleted', 'CompanyContactNameChanged', 'CompanyContactFaxChanged', 'ERPenaltiesAndInterestRefundCreated', 'ERPenaltiesAndInterestRefundDebitCreated', 'EntitlementUnitAdded', 'UsageBilling25DaysIntoSubscription', 'UsageBilling15DaysIntoSubscription', 'CreditReduction', 'RequestProcessingFlagChanged', 'PayrollSubmittedWithEmployeeWithPendingReturn', 'DuplicatePayrollItemReceived', 'VmpSignUpEmployeeEmail', 'VmpSignUpEmployerEmail', 'ACHEnrollmentStatusChanged', 'PaystubCreated', 'AdditionalFilingAmount', 'BPIncreasePayrollLimit', 'AllowTransmissionsFlagChanged', 'MonthlyFeeCreated', 'EmployeeAdded', 'EmployeeDeleted', 'EmployeeUpdated', 'PayeeAdded', 'PayeeUpdated', 'DDMigration', 'SplitMMTReturn', 'RealmIdAdded', 'RealmIdUpdated', 'BillingRealmCreated', 'BillingRealmAttached', 'DataRealmAttached', 'GrantAttached', 'DGDeleteRequest', 'SMSToPSPSyncFailure', 'SMSToPSPSyncSuccess', 'CompanyBankAccountVBDStatusChange', 'ComplianceAddressChanged', 'DGAccessRequest', 'AutoEnabledVMP', 'ConsentChange', 'EmployeeInvited', 'EmployeeSignedUp', 'EmployeeBankAccountWalletSuccess', 'EmployeeBankAccountWalletFailure', 'CloneEmployeeWalletOnRealmChangeSuccess', 'CloneEmployeeWalletOnRealmChangeFailure', 'AddUserToRealmSuccess', 'AddUserToRealmFailure', 'AddUserToRealmOnRealmChangeSuccess', 'AddUserToRealmOnRealmChangeFailure', 'RiskProfileMigrated', 'RiskProfileUnMigrated', 'PSPToSMSMigration', 'PSPToSMSMigrationRevert', 'SMSRealmIdUpdated', 'UpdateConsumerRealmId', 'VendorBankAccountWalletFailure', 'VendorBankAccountWalletSuccess', 'CloneVendorWalletOnRealmChangeFailure', 'CloneVendorWalletOnRealmChangeSuccess')) NOT VALID;

ALTER TABLE pspadm.psp_event_type DROP CONSTRAINT c_psp_event_type1 ;
--CHECK (event_group_cd IN ('Agent', 'Bank', 'CompanyInfo', 'FinancialOps', 'Fraud', 'NonPSP', 'PayrollStatus', 'PSP'));



ALTER TABLE pspadm.psp_failed_payroll_run DROP CONSTRAINT c_psp_failed_payroll_run0 ;
--CHECK (status_token IN ('Pending', 'Complete', 'Error', 'NoSend')) NOT VALID;



ALTER TABLE pspadm.psp_fee DROP CONSTRAINT c_psp_fee0 ;
--CHECK (fee_cd IN ('ReverseFee', 'NSFFee', 'CopyFee', 'FeeOnlyNSFFee')) NOT VALID;





ALTER TABLE pspadm.psp_forecast DROP CONSTRAINT c_psp_forecast0 ;
--CHECK (status IN ('Open', 'Closed', 'Error'));



ALTER TABLE pspadm.psp_fraud_bank_account DROP CONSTRAINT c_psp_fraud_bank_account0 ;
--CHECK (account_type_cd IN ('Checking', 'Savings')) NOT VALID;

ALTER TABLE pspadm.psp_fraud_bank_account DROP CONSTRAINT c_psp_fraud_bank_account1 ;
--CHECK (fraud_bank_account_reason IN ('EmployeeBankAccountOfTerminatedCompany', 'EmployerBankAccountOfTerminatedCompany')) NOT VALID;


ALTER TABLE pspadm.psp_fraud_event DROP CONSTRAINT c_psp_fraud_event0 ;
--CHECK (event_status_cd IN ('Active', 'Inactive')) NOT VALID;

ALTER TABLE pspadm.psp_fraud_event DROP CONSTRAINT c_psp_fraud_event1 ;
--CHECK (event_type_cd IN ('FileIdChanged', 'SendEmailFailed', 'PaycheckRecalledAfterOffload', 'SendEmailSkipped', 'InvalidVendorEmail', 'ERLoanNOC', 'NewPSIDCreatedForExistingCustomer', 'PrimaryPrincipalNameChanged', 'PayeeBankAccountChange', 'BackdatePriorToProcessingStart', 'SubscriptionEndDateChanged', 'SUICreditsApplied', 'TrialAssetDetected', 'WelcomeEmail', 'AccountLocked', 'ACHReturnStatusChanged', 'EINChanged', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BackdatedPayrollReceived', 'ACHReturn', 'FeeCreated', 'IncorrectPIN', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollRejected', 'CompanyContactEmailChanged', 'TaxExemptStatusChanged', 'CustomerSignedUp', 'PayrollCancelPending', 'FeeRefunded', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'DBANameChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'CompanyIndustryTypeChanged', 'PrimaryPrincipalSSNChanged', 'PrimaryPrincipalDOBChanged', 'EnrollmentStatusChanged', 'StateIdModified', 'TaxPaymentStatusChanged', 'PaymentMethodChanged', 'DepositFrequencyChanged', 'ThresholdExceeded', 'AssistedEmployeeMigrationComplete', 'OfferingUpdated', 'OfferClaimed', 'OfferRemoved', 'EmployeeInTermedCompany', 'EmployeeBankAccountInTermedCompany', 'PrefundingReceived', 'EmployeePaidTooManyTimes', 'EmployeePaidPercentageGreaterThanOthers', 'EmployeesPaidToSameBank', 'EmployeeBankAccountChangedSpikeInPay', 'EmployeesPaidToSameBankAccount', 'InvalidEmployeeInformation', 'PINUnlocked', 'Employee401kDataUploaded', 'EmployeeBankAccountChange', 'TotalBillPaymentExceedsLimit', 'PayeePaidGreaterThanMax', 'PayeePaidTooManyTimes', 'BillPaymentReceived', 'InvalidPaycheckInformation', 'VoidedPaycheckAlreadyOffloadedToTOK', 'DeletedPaycheckAlreadyOffloadedToTOK', 'TOKNotifiedOfCompanyFraud', 'BillPaymentOffloaded', 'PreOffload401kValidationAlert', 'BillPaymentRecalled', 'NonPrintChecks', 'PayrollReceivedCloud', 'PostOffload401kValidationAlert', 'InvalidSourceSystemTransmissionInformation', 'CloudResponse', 'InactivityDDPayrollAmountExceeded', 'InactivityBPPayrollAmountExceeded', 'AssistedPayrollItemMigrationComplete', 'ERPayableRefundCreated', 'MultipleCompanyLawsCreated', 'BalanceFileReceived', 'CompanyLawUpdated', 'PayrollReceivedPayCard', 'LiabilityAdjustmentCreated', 'PSPToAS400HoldSync', 'PSPToAS400HoldRemoveSync', 'OFXServiceActivated', 'PositiveCobraReceived', 'ManualLedgerEntry', 'PayrollTaxPaymentVoided', 'PayrollTaxPaymentReissued', 'ERPayableAppliedToBalanceDue', 'AIDUpdated', 'AssistedFailedEnrollment', 'AssistedPayrollConfirmation', 'EntitlementStateChanged', 'EntitlementUnitStatusChanged', 'SourceCompanyIdChanged', 'ServiceKeyUpdated', 'EntitlementCodeChanged', 'PriceTypeChanged', 'EntitlementCommunication', 'ManualDataSync', 'AccountingFinancialLedgerAdjustmentCreated', 'SUIEoqDebitCreated', 'SUIEoqCreditCreated', 'SUIImmediateDebitCreated', 'SUIImmediateCreditCreated', 'CompanyContactAdded', 'CompanyContactJobTitleChanged', 'PayrollSubmissionIncludedAllNewEmployees', 'PSIDMismatch', 'CompanyContactDeleted', 'CompanyContactNameChanged', 'CompanyContactFaxChanged', 'ERPenaltiesAndInterestRefundCreated', 'ERPenaltiesAndInterestRefundDebitCreated', 'EntitlementUnitAdded', 'UsageBilling25DaysIntoSubscription', 'UsageBilling15DaysIntoSubscription', 'CreditReduction', 'RequestProcessingFlagChanged', 'PayrollSubmittedWithEmployeeWithPendingReturn', 'DuplicatePayrollItemReceived', 'VmpSignUpEmployeeEmail', 'VmpSignUpEmployerEmail', 'ACHEnrollmentStatusChanged', 'PaystubCreated', 'AdditionalFilingAmount', 'BPIncreasePayrollLimit', 'AllowTransmissionsFlagChanged', 'MonthlyFeeCreated', 'EmployeeAdded', 'EmployeeDeleted', 'EmployeeUpdated', 'PayeeAdded', 'PayeeUpdated', 'DDMigration', 'SplitMMTReturn', 'RealmIdAdded', 'RealmIdUpdated', 'BillingRealmCreated', 'BillingRealmAttached', 'DataRealmAttached', 'GrantAttached', 'DGDeleteRequest', 'SMSToPSPSyncFailure', 'SMSToPSPSyncSuccess', 'CompanyBankAccountVBDStatusChange', 'ComplianceAddressChanged', 'DGAccessRequest', 'AutoEnabledVMP', 'ConsentChange', 'EmployeeInvited', 'EmployeeSignedUp', 'EmployeeBankAccountWalletSuccess', 'EmployeeBankAccountWalletFailure', 'CloneEmployeeWalletOnRealmChangeSuccess', 'CloneEmployeeWalletOnRealmChangeFailure', 'AddUserToRealmSuccess', 'AddUserToRealmFailure', 'AddUserToRealmOnRealmChangeSuccess', 'AddUserToRealmOnRealmChangeFailure', 'RiskProfileMigrated', 'RiskProfileUnMigrated', 'PSPToSMSMigration', 'PSPToSMSMigrationRevert', 'SMSRealmIdUpdated', 'UpdateConsumerRealmId', 'VendorBankAccountWalletFailure', 'VendorBankAccountWalletSuccess', 'CloneVendorWalletOnRealmChangeFailure', 'CloneVendorWalletOnRealmChangeSuccess')) NOT VALID;

ALTER TABLE pspadm.psp_fraud_rule DROP CONSTRAINT c_psp_fraud_rule0 ;
--CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) NOT VALID;



ALTER TABLE pspadm.psp_fraud_value DROP CONSTRAINT c_psp_fraud_value0 ;
--CHECK (name IN ('FraudBPXPayrollAmount', 'FraudBPInactivityDays', 'FraudBPInactivityPayrollAmount', 'FraudBPMax', 'FraudBPMaxXPayrolls', 'FraudBPNumberOfDaysForXPayments', 'FraudBPNumberOfPaymentsInXDays', 'FraudDDInactivityDays', 'FraudDDInactivityPayrollAmount', 'FraudEENewEmployeeAddedXDays', 'FraudEENumberOfDaysBankAcctUpdated', 'FraudEENumberOfDaysMultiplePaychecks', 'FraudEENumberOfPaychecksSpikeInPay', 'FraudEEPaidMax', 'FraudPRMax', 'FraudPRMaxXPayrolls', 'FraudPRNumberOfDaysForXPayrolls', 'FraudPRNumberOfPayrollsInXDays', 'FraudPRNumberOfPayrollsToCheckSameBank', 'FraudPRPercentEmployeesPaidSameBank', 'FraudPRPercentIncreaseMax', 'FraudPRPercentIncreaseMaxXPayrolls', 'FraudEEPaidMaxXPayrolls', 'FraudEEPaidXTimes', 'FraudEEPercentGreaterThanAverage', 'FraudEEPercentGreaterThanOtherEEs', 'FraudEEPercentIncreaseMax', 'FraudEEPercentIncreaseMaxXPayrolls', 'FraudEERoundPaidXPayrolls', 'FraudPREmployeesSameBankAccountMax', 'FraudPRTotalEmployeesToCheckSameBank', 'FraudPayeeNumberOfDaysMultiplePayments', 'FraudPayeePaidMax', 'FraudPayeePaidMaxXPayrolls', 'FraudPayeePaidXTimes', 'FraudBPRoundPaidXPayrolls', 'FraudBPNumberOfPaymentsToCheckSameBank', 'FraudBPPercentPayeesPaidSameBank', 'FraudBPTotalPayeesToCheckSameBank', 'FraudEERoundPaidXAmount', 'FraudBPRoundPaidXAmount', 'FraudPRXPayrollAmount', 'FraudBPAcctUpdateMax', 'FraudBPAcctUpdateXDays', 'FraudEEAcctUpdateMax', 'FraudEEAcctUpdateXDays'));



ALTER TABLE pspadm.psp_fset_file DROP CONSTRAINT c_psp_fset_file0 ;
--CHECK (file_type IN ('FsetReturns', 'FsetAck')) NOT VALID;

ALTER TABLE pspadm.psp_fset_file DROP CONSTRAINT c_psp_fset_file1 ;
--CHECK (status_cd IN ('Archived', 'PendingTransmission', 'SentToAgency', 'Error', 'ReceivedByAgency', 'Completed')) NOT VALID;



ALTER TABLE pspadm.psp_fset_filing_detail DROP CONSTRAINT c_psp_fset_filing_detail0 ;
--CHECK (status IN ('AcceptedByAgency', 'SentToAgency', 'RejectedByAgency')) NOT VALID;



ALTER TABLE pspadm.psp_gems_ledger_posting_rule DROP CONSTRAINT c_psp_gems_ledger_posting_0 ;
--CHECK (reporting_type IN ('Tax', 'DirectDeposit')) NOT VALID;



ALTER TABLE pspadm.psp_gems_upload_batch DROP CONSTRAINT c_psp_gems_upload_batch0 ;
--CHECK (batch_type IN ('Daily', 'Monthly'));

ALTER TABLE pspadm.psp_gems_upload_batch DROP CONSTRAINT c_psp_gems_upload_batch1 ;
--CHECK (upload_status IN ('InProcess', 'Empty', 'Finalized', 'PendingTransmission', 'Transmitted', 'Archived', 'Superceded'));



ALTER TABLE pspadm.psp_hours_worked_exception DROP CONSTRAINT c_psp_hours_worked_excepti0 ;
--CHECK (pay_type IN ('REG', 'SICK', 'VAC')) NOT VALID;

ALTER TABLE pspadm.psp_hours_worked_exception DROP CONSTRAINT c_psp_hours_worked_excepti1 ;
--CHECK (payroll_item_cd IN ('Tp401kEmployeeDeferral', 'Tp401kEmployerMatch', 'Tp401kLoanPayment', 'Tp401kProfitSharing', 'Tp401kRoth', 'Tp401kSafeHarbor', 'Salary', 'Hourly', 'OtherPreTaxDeduction', 'Compensation', 'OtherPostTaxDeduction', 'OtherTaxableEmployerContribution', 'OtherNonTaxableEmployerContribution', 'Bonus', 'Commission', 'OtherAdditionPreTax', 'OtherAdditionPostTax', 'DirectDeposit')) NOT VALID;



ALTER TABLE pspadm.psp_individual DROP CONSTRAINT c_psp_individual0 ;
--CHECK (gender_cd IN ('Male', 'Female'));

ALTER TABLE pspadm.psp_individual DROP CONSTRAINT c_psp_individual1 ;
--CHECK (communication_type_preference IN ('Phone', 'Email'));



ALTER TABLE pspadm.psp_industry_type DROP CONSTRAINT c_psp_industry_type1 ;
--CHECK (LENGTH(standard_industry_code) > 3) NOT VALID;



ALTER TABLE pspadm.psp_intuit_ba_bt_ft DROP CONSTRAINT c_psp_intuit_ba_bt_ft0 ;
--CHECK (file_type IN ('CCD', 'PPD', 'CCDPlus')) NOT VALID;

ALTER TABLE pspadm.psp_intuit_ba_bt_ft DROP CONSTRAINT c_psp_intuit_ba_bt_ft1 ;
--CHECK (n_a_c_h_a_batch_type IN ('BookTransfer', 'Payroll', 'Reversal', 'RetryPayment', 'TaxPayment')) NOT VALID;



ALTER TABLE pspadm.psp_intuit_bank_acc_txn_type DROP CONSTRAINT c_psp_intuit_bank_acc_txn_0 ;
--CHECK (credit_debit_ind IN ('Credit', 'Debit'));



ALTER TABLE pspadm.psp_iopsync_company DROP CONSTRAINT c_psp_iopsync_company1 ;
--CHECK (status IN ('Pending', 'InProcess', 'Failed', 'Synced'));

ALTER TABLE pspadm.psp_iopsync_company--ADD --PRIMARY KEY (iopsync_company_seq);

ALTER TABLE pspadm.psp_law DROP CONSTRAINT c_psp_law0 ;
--CHECK (law_category_code IN ('Withholding', 'SocialSecurityEmployee', 'SocialSecurityEmployer', 'Local', 'Supplemental', 'UnemploymentEmployer', 'WorkersCompensationEmployee', 'DisabilityEmployer', 'UnemploymentEmployee', 'MedicareEmployee', 'DisabilityEmployee', 'UnemploymentHealthInsurance', 'Unused', 'MedicareEmployer', 'TransitTax', 'PaidLeave')) NOT VALID;


ALTER TABLE pspadm.psp_ledger_account DROP CONSTRAINT c_psp_ledger_account0 ;
--CHECK (ledger_account_cd IN ('AgencyTaxRefund', 'DDFutureReceivable', 'DDFutureLiability', 'DDCurrentCash', 'DDCurrentLiability', 'ERReturnReceivable', 'ERReturnCash', 'SalesAndUseTax', 'EEReturnCash', 'EEReturnLiablility', 'FeeCashRevenue', 'FeeCashBalanceSheet', 'FeeIncome', 'BadDebt', 'TaxFutureReceivable', 'TaxFutureLiability', 'TaxCurrentCash', 'TaxCurrentLiability', 'ERPayable', 'ERLiabilityOffset', 'CollectionExpense', 'ERSUITaxDue', 'TaxInterestExpense', 'TaxPenaltiesExpense')) NOT VALID;

ALTER TABLE pspadm.psp_ledger_account DROP CONSTRAINT c_psp_ledger_account1 ;
--CHECK (balance_calculation_rule IN ('CreditAddsToBalance', 'DebitAddsToBalance'));

ALTER TABLE pspadm.psp_ledger_account DROP CONSTRAINT c_psp_ledger_account2 ;
--CHECK (ledger_account_type IN ('SUTax', 'Income'));

ALTER TABLE pspadm.psp_ledger_account DROP CONSTRAINT c_psp_ledger_account3 ;
--CHECK (reporting_frequency IN ('Daily', 'Monthly'));



ALTER TABLE pspadm.psp_ledger_account_action DROP CONSTRAINT c_psp_ledger_account_action0 ;
--CHECK (credit_debit_indicator IN ('Credit', 'Debit'));




ALTER TABLE pspadm.psp_ledger_operation DROP CONSTRAINT c_psp_ledger_operation0 ;
--CHECK (source_system_code IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) NOT VALID;

ALTER TABLE pspadm.psp_ledger_operation DROP CONSTRAINT c_psp_ledger_operation1 ;
--CHECK (status IN ('Created', 'InProgress', 'Completed', 'Error'));


ALTER TABLE pspadm.psp_ledger_operation_job DROP CONSTRAINT c_psp_ledger_operation_job0 ;
--CHECK (status IN ('Deleted', 'Queued', 'Created', 'InProgress', 'Complete')) NOT VALID;

ALTER TABLE pspadm.psp_ledger_operation_job DROP CONSTRAINT c_psp_ledger_operation_job1 ;
--CHECK (job_type IN ('BulkDebit', 'TOR', 'DepositFrequencyUpdate', 'RateUpdate', 'AdditionalFilingAmountUpdate')) NOT VALID;



ALTER TABLE pspadm.psp_liability_check DROP CONSTRAINT c_psp_liability_check0 ;
--CHECK (type IN ('EmployerFee', 'EmployerDebit', 'EFTPSDirectDebit', 'EmployerDDDebit', 'EmployerTaxFee')) NOT VALID;



ALTER TABLE pspadm.psp_limit_rule DROP CONSTRAINT c_psp_limit_rule0 ;
--CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) NOT VALID;



ALTER TABLE pspadm.psp_limit_value DROP CONSTRAINT c_psp_limit_value0 ;
--CHECK (name IN ('MinPayrollRunsForLimitAutoIncrease', 'ConsecutiveLimitViolationLimit', 'CompanyBankAccountVerificationAttemptLimit', 'CompanyBankAccountDurationLimitForVerification', 'MinimumNonSuspectPayrollAmount', 'AutoLimitIncreaseMinPayrolls', 'AutoLimitIncreaseMinEarliestPayrollRunDays', 'AutoLimitIncreaseIncreaseMultiplier', 'AutoLimitIncreaseMaxCompanyLimit', 'AutoLimitIncreaseMaxEmployeeLimit', 'MaxCompanyLimitDefault', 'CompanyLimitDuration', 'EmployeeLimitDuration', 'DefaultCompanyLimit', 'DefaultEmployeeLimit')) NOT VALID;



ALTER TABLE pspadm.psp_message_log DROP CONSTRAINT c_psp_message_log0 ;
--CHECK (flow_type IN ('EIAM', 'SMS'));



ALTER TABLE pspadm.psp_nachafile DROP CONSTRAINT c_psp_nachafile0 ;
--CHECK (status IN ('Archived', 'Acknowledged', 'PendingAcknowledgement', 'PendingTransmission', 'Finalized', 'InProcess', 'Transmitted')) NOT VALID;

ALTER TABLE pspadm.psp_nachafile DROP CONSTRAINT c_psp_nachafile1 ;
--CHECK (file_type IN ('CCD', 'PPD', 'CCDPlus')) NOT VALID;



ALTER TABLE pspadm.psp_offer DROP CONSTRAINT c_psp_offer0 ;
--CHECK (discount_type IN ('AmountOff', 'PercentOff', 'AltPrice')) NOT VALID;

ALTER TABLE pspadm.psp_offer DROP CONSTRAINT c_psp_offer1 ;
--CHECK (begin_event IN ('SignupEvent', 'ActivationEvent', 'FirstUseEvent', 'RedemptionEvent'));

ALTER TABLE pspadm.psp_offer DROP CONSTRAINT c_psp_offer2 ;
--CHECK (end_event IN ('DateEvent', 'DurationEvent', 'PayrollUsageEvent'));

ALTER TABLE pspadm.psp_offer DROP CONSTRAINT c_psp_offer3 ;
--CHECK (offer_restriction IN ('Open', 'Restricted', 'SalesOps')) NOT VALID;


ALTER TABLE pspadm.psp_offer_price DROP CONSTRAINT c_psp_offer_price0 ;
--CHECK (fee_type IN ('CourtesyRefund', 'PerPayroll', 'PerPaycheck', 'PerTransmission', 'AmendedSSN', 'PerPayment', 'ReversalFee', 'DebitReturnFee', 'ManualServicingFee', 'ChaseReportFeeUpTo3Payrolls', 'ChaseReportFeeUpTo6Payrolls', 'ChaseReportFeeUpTo15Payrolls', 'ChaseReportFeeUpTo20Payrolls', 'ChaseReportFeeOver20Payrolls', 'BankVerificationDebit', 'PaymentArrangementFee', 'PerBatch', 'CompanyUpdates', 'EmployeesAdded', 'EmployeesUpdated', 'BackdatedPayroll', 'DirectDepositFee', 'EmployeesPaid', 'MonthlyFee', 'Amendments', 'EntityChange', 'ExtraCopies', 'PayrollAdjustment', 'PenaltiesAndInterest', 'ExtraStateFee', 'OtherFee', 'W2Fee', 'EmployeeOrganizerFee', 'W2Correction', 'W2BaseFee', 'BankVerificationCredit')) NOT VALID;



ALTER TABLE pspadm.psp_offering DROP CONSTRAINT c_psp_offering1 ;
--CHECK (offering_code IN ('DIYDDYEAREND', 'DIYDDFY143', 'COSTCO69FY16', 'AP79FY16', 'AP79FY14', 'AP79MEFY14', 'AP89FY14', 'PAP75FY14', 'DIYDDFY14', 'SYM3FY14', 'BillPaymentSTD3FY14', 'SYMFY14', 'COSTCO54', 'COSTCO64', 'AP89FY16', 'COSTCO84', 'COSTCO74', 'COSTCO572', 'COSTCO672', 'BillPaymentSTDFY15', 'DIYDDFY15', 'DIYDDFY153', 'AP79FY15', 'AP89FY15', 'AP99FY15', 'AP79MEFY15', 'AP89MEFY15', 'PAP84FY15', 'COSTCO57FY15', 'COSTCO67FY15', 'COSTCO79FY16', 'AP79MEFY16', 'AP89MEFY16', 'AP99FY16', 'AP99MEFY16', 'BillPaymentSTDFY16', 'DIYDDFY16', 'DIYDDFY163', 'PAP84FY16', 'DIYDDSTD', 'DIYDDSTD3', 'QBOEDD', 'CheckDistribution', 'ThirdParty401k', 'BillPaymentSTD3', 'Tax', 'Cloud', 'AssistedBundle', 'RiskAssessment', 'AP69MEFY13', 'PAPAV1142', 'AP63EEEO', 'AP79FY13', 'MAJORACCT', 'APAV115', 'APAV125ME2', 'APAV1352', 'SUP125TEST', 'APDIOCESE', 'APPAP99YR', 'ASST60', 'ASSTAD2P3', 'ASSTEOSUP', 'COSTCO49', 'COSTCO59', 'PAP71FY13', 'PAP582', 'PAP58DD145', 'PAP58DD2', 'AP59ME2', 'AP69DD145', 'AP69DD1502', 'AP59MED145', 'AP692', 'AP69DD2', 'AP69W22', 'UsageBilling', 'SYM1FY13', 'SYM2FY13', 'WorkersComp', 'COSTCO57', 'COSTCO67', 'ViewMyPaycheck', 'PAP67FY13', 'COSTCO52', 'COSTCO62', 'BillPaymentSTD4', 'CloudV2', 'SYMPAPFY14', 'SYMPAP92FY18', 'SYMPAP87FY18', 'PAP92FY18', 'AP109FY18', 'SYM109FY18'));

ALTER TABLE pspadm.psp_offering DROP CONSTRAINT c_psp_offering2 ;
--CHECK (service_code IN ('DirectDeposit', 'Tax', 'BillPayment', 'ThirdParty401k', 'CheckDistribution', 'Cloud', 'RiskAssessment', 'WorkersComp', 'ViewMyPaycheck', 'CloudV2', 'Guideline401k'));



ALTER TABLE pspadm.psp_offering_svcchg DROP CONSTRAINT c_psp_offering_svcchg0 ;
--CHECK (sku_type IN ('Payroll', 'NonPayroll'));



ALTER TABLE pspadm.psp_offering_svcchg_grp DROP CONSTRAINT c_psp_offering_svcchg_grp0 ;
--CHECK (applies_to IN ('CourtesyRefund', 'PerPayroll', 'PerPaycheck', 'PerTransmission', 'AmendedSSN', 'PerPayment', 'ReversalFee', 'DebitReturnFee', 'ManualServicingFee', 'ChaseReportFeeUpTo3Payrolls', 'ChaseReportFeeUpTo6Payrolls', 'ChaseReportFeeUpTo15Payrolls', 'ChaseReportFeeUpTo20Payrolls', 'ChaseReportFeeOver20Payrolls', 'BankVerificationDebit', 'PaymentArrangementFee', 'PerBatch', 'CompanyUpdates', 'EmployeesAdded', 'EmployeesUpdated', 'BackdatedPayroll', 'DirectDepositFee', 'EmployeesPaid', 'MonthlyFee', 'Amendments', 'EntityChange', 'ExtraCopies', 'PayrollAdjustment', 'PenaltiesAndInterest', 'ExtraStateFee', 'OtherFee', 'W2Fee', 'EmployeeOrganizerFee', 'W2Correction', 'W2BaseFee', 'BankVerificationCredit')) NOT VALID;

ALTER TABLE pspadm.psp_offering_svcchg_grp--ADD --PRIMARY KEY (offering_svcchg_grp_seq);

ALTER TABLE pspadm.psp_offload_batch DROP CONSTRAINT c_psp_offload_batch0 ;
--CHECK (status_cd IN ('Completed', 'InProcess')) NOT VALID;



ALTER TABLE pspadm.psp_on_hold_reason DROP CONSTRAINT c_psp_on_hold_reason0 ;
--CHECK (on_hold_reason_cd IN ('AchRejectOther', 'AchRejectR1R9', 'ActiveCurrent', 'ActiveSeasonal', 'Cancelled', 'DirectDepositLimit', 'Fraud', 'AuditCorrections', 'FraudReview', 'IntuitCollections', 'MissingPaperwork', 'PendingBalanceFile', 'PendingBankVerification', 'PendingFirstPayroll', 'PendingPinCreation', 'PendingTermination', 'RiskAssessment', 'RiskCollections', 'SuspendedDirectDeposit', 'Terminated', 'AMLHold', 'PendingTaxAcceptance', 'PendingEnrollment', 'PendingPrefundingWire', 'AS400Hold', 'AS400DirectDepositLimitHold', 'PendingSetup', 'BillPaymentLimit', 'MTLHold')) NOT VALID;



ALTER TABLE pspadm.psp_pay_item DROP CONSTRAINT c_psp_pay_item0 ;
--CHECK (pay_item_cd IN ('HIREAct', 'Tips')) NOT VALID;





ALTER TABLE pspadm.psp_payee_bank_account DROP CONSTRAINT c_psp_payee_bank_account0 ;
--CHECK (status_cd IN ('PendingVerification', 'Active', 'Inactive')) NOT VALID;



ALTER TABLE pspadm.psp_payment_template DROP CONSTRAINT c_psp_payment_template0 ;
--CHECK (category IN ('Other', 'SUI', 'Withholding')) NOT VALID;



ALTER TABLE pspadm.psp_payroll_item DROP CONSTRAINT c_psp_payroll_item0 ;
--CHECK (payroll_item_code IN ('Tp401kEmployeeDeferral', 'Tp401kEmployerMatch', 'Tp401kLoanPayment', 'Tp401kProfitSharing', 'Tp401kRoth', 'Tp401kSafeHarbor', 'Salary', 'Hourly', 'OtherPreTaxDeduction', 'Compensation', 'OtherPostTaxDeduction', 'OtherTaxableEmployerContribution', 'OtherNonTaxableEmployerContribution', 'Bonus', 'Commission', 'OtherAdditionPreTax', 'OtherAdditionPostTax', 'DirectDeposit')) NOT VALID;

ALTER TABLE pspadm.psp_payroll_item DROP CONSTRAINT c_psp_payroll_item1 ;
--CHECK (payroll_item_type IN ('Compensation', 'Deduction', 'EmployerContribution')) NOT VALID;



ALTER TABLE pspadm.psp_payroll_run DROP CONSTRAINT c_psp_payroll_run0 ;
--CHECK (collection_stage_cd IN ('FirstCollectionAttempt', 'SecondCollectionAttempt', 'TerminationExpected')) NOT VALID;

ALTER TABLE pspadm.psp_payroll_run DROP CONSTRAINT c_psp_payroll_run1 ;
--CHECK (payroll_run_status IN ('Superseded', 'Complete', 'Canceled', 'DebitReturnedCanceled', 'DebitReturned', 'NSFCanceled', 'OffloadedAll', 'OffloadedDebit', 'Pending', 'WrittenOff', 'PendingReversals', 'PendingAutoRedebit', 'AutoRedebitOffloaded', 'PendingRedebit', 'RedebitOffloaded', 'PendingWire', 'ReversalsOffloaded', 'ReversalsFinished', 'ReturnedTwice', 'PendingToDD', 'SentToDD', 'None')) NOT VALID;

ALTER TABLE pspadm.psp_payroll_run DROP CONSTRAINT c_psp_payroll_run2 ;
--CHECK (payroll_run_type IN ('FeeOnly', 'Regular', 'Adjustment', 'BillPayment', 'CloudOnly')) NOT VALID;

ALTER TABLE pspadm.psp_payroll_run DROP CONSTRAINT c_psp_payroll_run3 ;
--CHECK (d_d_status IN ('Pending', 'PendingToDD', 'SentToDD', 'Canceled', 'Complete', 'Fail', 'OffloadedDebit', 'OffloadedCredit', 'OffloadedAll', 'PendingVoid', 'SentVoid', 'PendingCompleteToDD', 'SentCompleteToDD', 'SentSupersededToDD', 'PendingSupersededToDD', 'Superseded', 'None', 'PendingPartialVoid', 'SentPartialVoid', 'ReversalsFinished', 'DebitReturnedCanceled', 'ReturnedTwice', 'PendingAutoRedebit', 'NSFCanceled', 'WrittenOff', 'PendingWire', 'PendingReversals', 'PendingRedebit', 'RedebitOffloaded', 'DebitReturned', 'AutoRedebitOffloaded', 'ReversalsOffloaded')) NOT VALID;

ALTER TABLE pspadm.psp_payroll_run DROP CONSTRAINT c_psp_payroll_run4 ;
--CHECK (tax_and_fees_status IN ('Superseded', 'Complete', 'Canceled', 'DebitReturnedCanceled', 'DebitReturned', 'NSFCanceled', 'OffloadedAll', 'OffloadedDebit', 'Pending', 'WrittenOff', 'PendingReversals', 'PendingAutoRedebit', 'AutoRedebitOffloaded', 'PendingRedebit', 'RedebitOffloaded', 'PendingWire', 'ReversalsOffloaded', 'ReversalsFinished', 'ReturnedTwice', 'PendingToDD', 'SentToDD', 'None')) NOT VALID;

ALTER TABLE pspadm.psp_payroll_run DROP CONSTRAINT c_psp_payroll_run5 ;
--CHECK (d_d_message_status IN ('PendingPartialVoid', 'SentPartialVoid', 'PendingCompleteToDD', 'SentCompleteToDD', 'SentSupersededToDD', 'PendingSupersededToDD', 'Superseded', 'OffloadedDebit', 'Pending', 'Canceled', 'Complete', 'None', 'Fail', 'PendingToDD', 'SentToDD', 'PendingVoid', 'SentVoid', 'OffloadedCredit', 'OffloadedAll')) NOT VALID;



ALTER TABLE pspadm.psp_payroll_run_action DROP CONSTRAINT c_psp_payroll_run_action0 ;
--CHECK (status IN ('Superseded', 'Complete', 'Canceled', 'DebitReturnedCanceled', 'DebitReturned', 'NSFCanceled', 'OffloadedAll', 'OffloadedDebit', 'Pending', 'WrittenOff', 'PendingReversals', 'PendingAutoRedebit', 'AutoRedebitOffloaded', 'PendingRedebit', 'RedebitOffloaded', 'PendingWire', 'ReversalsOffloaded', 'ReversalsFinished', 'ReturnedTwice', 'PendingToDD', 'SentToDD', 'None')) NOT VALID;



ALTER TABLE pspadm.psp_payroll_subtype DROP CONSTRAINT c_psp_payroll_subtype0 ;
--CHECK (payroll_subtype_cd IN ('FreeBasic1', 'BasicLimited', 'BasicUnlimited', 'Enhanced', 'EnhancedAccountant', 'EnhancedUnlimited', 'NewBasicUnlimited', 'Standard', 'Basic0to3Emp', 'Enhanced0to3Emp', 'PAPEnhAcct', 'Assisted', 'AssistedAdv', 'MonthlyBasic0to3Emp', 'MonthlyBasicUnlimited', 'MonthlyEnhanced0to3Emp', 'MonthlyEnhancedUnlimited')) NOT VALID;



ALTER TABLE pspadm.psp_pmt_template_bankaccount DROP CONSTRAINT c_psp_pmt_template_bankacc0 ;
--CHECK (status_cd IN ('PendingVerification', 'Active', 'Inactive')) NOT VALID;

ALTER TABLE pspadm.psp_pmt_template_bankaccount--ADD --PRIMARY KEY (pmt_template_bankaccount_seq);

ALTER TABLE pspadm.psp_pmt_template_frequency DROP CONSTRAINT c_psp_pmt_template_frequen0 ;
--CHECK (payment_frequency_id IN ('ACCELERATED', 'ANNUAL', 'EARLYFILER', 'EIGHTHMONTHLY', 'FIVEBANKINGDAY', 'MONTHLY', 'MONTHLYACCELERATED', 'NEXTBANKINGDAY', 'QUADMONTHLY', 'QUARTERLY', 'QUARTERMONTHLY', 'SEMIANNUAL', 'SEMIMONTHLY', 'SEMIWEEKLY', 'SPLITMONTHLY', 'THREEBANKINGDAY', 'TWICEMONTHLY', 'NOCALC', 'WEEKLY')) NOT VALID;


ALTER TABLE pspadm.psp_pmt_template_paymentmethod DROP CONSTRAINT c_psp_pmt_template_payment0 ;
--CHECK (payment_method IN ('ACHDebit', 'ACHCredit', 'CheckPayment', 'PostBalfHPDE', 'PostBalfHPDERefund', 'ACHDirectDeposit', 'WirePayment', 'EFE', 'HPDERefund', 'HPDE', 'EFTPS', 'EFTPSDirectDebit', 'EDI', 'SuperCheck')) NOT VALID;



ALTER TABLE pspadm.psp_pstub_employee_preference DROP CONSTRAINT emp_pref_unq_indx UNIQUE (app_name, preference_name, employee_fk);



ALTER TABLE pspadm.psp_pstub_msg DROP CONSTRAINT c_psp_pstub_msg0 ;
--CHECK (type IN ('Company', 'User')) NOT VALID;



ALTER TABLE pspadm.psp_qbdt_employee_info DROP CONSTRAINT c_psp_qbdt_employee_info0 ;
--CHECK (employee_type IN ('REG', 'OFFICER', 'STATUTORY', 'OWNER', 'REP')) NOT VALID;




ALTER TABLE pspadm.psp_qbdt_payroll_item_info DROP CONSTRAINT c_psp_qbdt_payroll_item_in0 ;
--CHECK (pay_type IN ('REG', 'SICK', 'VAC')) NOT VALID;

ALTER TABLE pspadm.psp_qbdt_payroll_item_info DROP CONSTRAINT c_psp_qbdt_payroll_item_in1 ;
--CHECK (special_type IN ('COMCARE', 'COSSEC', 'EEMCARE', 'EESSEC', 'FEDTAX', 'FUTA', 'SALARY', 'SICKSALARY', 'VACSALARY', 'SICKHRLY', 'VACHRLY', 'AEIC', 'DIRDEP', 'WORKERCOMP')) NOT VALID;

ALTER TABLE pspadm.psp_qbdt_payroll_item_info DROP CONSTRAINT c_psp_qbdt_payroll_item_in2 ;
--CHECK (default_rate_type IN ('MoneyType', 'Percentage')) NOT VALID;



ALTER TABLE pspadm.psp_qbdt_payroll_transaction DROP CONSTRAINT c_psp_qbdt_payroll_transac0 ;
--CHECK (transaction_type IN ('FundsTransfer', 'PriorPayment', 'LiabilityAdjustment', 'DDReturn', 'Refund', 'LiabilityCheck')) NOT VALID;


ALTER TABLE pspadm.psp_qbdt_unprocessed_request DROP CONSTRAINT c_psp_qbdt_unprocessed_req0 ;
--CHECK (status IN ('Processed', 'Queued', 'Error', 'Processing')) NOT VALID;



ALTER TABLE pspadm.psp_rafenrollment DROP CONSTRAINT c_psp_rafenrollment0 ;
--CHECK (status IN ('PendingEnrollment', 'PendingEnrollmentTape', 'PendingEnrollmentResponse', 'Enrolled', 'Rejected', 'Cancelled', 'PendingDeleteTape', 'Deleted')) NOT VALID;



ALTER TABLE pspadm.psp_rafenrollment_file DROP CONSTRAINT c_psp_rafenrollment_file0 ;
--CHECK (status IN ('Completed', 'Error', 'Initiated', 'RecreationInitiated', 'Transmitted', 'Finalized', 'Emailed', 'PendingTransmission')) NOT VALID;

ALTER TABLE pspadm.psp_rafenrollment_file DROP CONSTRAINT c_psp_rafenrollment_file1 ;
--CHECK (r_a_f_action_code IN ('Add', 'Delete')) NOT VALID;

ALTER TABLE pspadm.psp_return_reason_desc DROP CONSTRAINT c_psp_return_reason_desc0 ;
--CHECK (reason_cd IN ('R01', 'R02', 'R03', 'R04', 'R05', 'R06', 'R07', 'R08', 'R09', 'R20', 'R24', 'R28', 'R29', 'R10', 'R12', 'R13', 'R14', 'R15', 'R16', 'R18', 'R11', 'R17', 'R19', 'R21', 'R22', 'R23', 'R25', 'R26', 'R27', 'R30', 'R31', 'R32', 'R33', 'R34', 'R35', 'R36', 'R37', 'R38', 'R39', 'R40', 'R41', 'R42', 'R43', 'R44', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R51', 'R52', 'R53', 'C01', 'R61', 'R62', 'R63', 'C02', 'C03', 'C04', 'C05', 'C06', 'C07', 'R64', 'R65', 'R66', 'R67', 'R68', 'R69', 'R70', 'R71', 'R72', 'R73', 'R74', 'R75', 'R76', 'R80', 'R81', 'C08', 'C09', 'C10', 'R82', 'R83', 'R84', 'C11', 'C12', 'C13', 'C61', 'C62', 'C63', 'R99', 'C64', 'C65', 'C66', 'C67', 'C68', 'C69', 'C99')) NOT VALID;



ALTER TABLE pspadm.psp_role_sub_status DROP CONSTRAINT c_psp_role_sub_status0 ;
--CHECK (allowed_change_type IN ('CanMoveFromSubStatus', 'CanMoveToSubStatus'));



ALTER TABLE pspadm.psp_rtbautomationbackup DROP CONSTRAINT c_psp_rtbautomationbackup0 ;
--CHECK (event_type IN ('DUPLICATEPITEM', 'DUPLICATEEMPLOYEE', 'ERROR2108', 'VMPSERVICEEVENT')) NOT VALID;



ALTER TABLE pspadm.psp_serv_stat_txn_sku_type DROP CONSTRAINT c_psp_serv_stat_txn_sku_ty0 ;
--CHECK (sku_type IN ('Payroll', 'NonPayroll'));

ALTER TABLE pspadm.psp_serv_stat_txn_sku_type DROP CONSTRAINT c_psp_serv_stat_txn_sku_ty1 ;
--CHECK (offering_service_charge_type IN ('CourtesyRefund', 'PerPayroll', 'PerPaycheck', 'PerTransmission', 'AmendedSSN', 'PerPayment', 'ReversalFee', 'DebitReturnFee', 'ManualServicingFee', 'ChaseReportFeeUpTo3Payrolls', 'ChaseReportFeeUpTo6Payrolls', 'ChaseReportFeeUpTo15Payrolls', 'ChaseReportFeeUpTo20Payrolls', 'ChaseReportFeeOver20Payrolls', 'BankVerificationDebit', 'PaymentArrangementFee', 'PerBatch', 'CompanyUpdates', 'EmployeesAdded', 'EmployeesUpdated', 'BackdatedPayroll', 'DirectDepositFee', 'EmployeesPaid', 'MonthlyFee', 'Amendments', 'EntityChange', 'ExtraCopies', 'PayrollAdjustment', 'PenaltiesAndInterest', 'ExtraStateFee', 'OtherFee', 'W2Fee', 'EmployeeOrganizerFee', 'W2Correction', 'W2BaseFee', 'BankVerificationCredit')) NOT VALID;



ALTER TABLE pspadm.psp_service DROP CONSTRAINT c_psp_service0 ;
--CHECK (service_cd IN ('DirectDeposit', 'Tax', 'BillPayment', 'ThirdParty401k', 'CheckDistribution', 'Cloud', 'RiskAssessment', 'WorkersComp', 'ViewMyPaycheck', 'CloudV2', 'Guideline401k'));



ALTER TABLE pspadm.psp_service_status DROP CONSTRAINT c_psp_service_status0 ;
--CHECK (service_status_cd IN ('Active', 'Cancelled', 'OnHold', 'PendingActivation', 'Terminated'));



ALTER TABLE pspadm.psp_service_sub_status DROP CONSTRAINT c_psp_service_sub_status0 ;
--CHECK (service_sub_status_cd IN ('AchRejectOther', 'AchRejectR1R9', 'ActiveCurrent', 'ActiveSeasonal', 'Cancelled', 'DirectDepositLimit', 'Fraud', 'AuditCorrections', 'FraudReview', 'IntuitCollections', 'MissingPaperwork', 'PendingBalanceFile', 'PendingBankVerification', 'PendingFirstPayroll', 'PendingPinCreation', 'PendingTermination', 'RiskAssessment', 'RiskCollections', 'SuspendedDirectDeposit', 'Terminated', 'AMLHold', 'PendingTaxAcceptance', 'PendingEnrollment', 'PendingPrefundingWire', 'AS400Hold', 'AS400DirectDepositLimitHold', 'PendingSetup', 'BillPaymentLimit', 'MTLHold')) NOT VALID;


ALTER TABLE pspadm.psp_smsmigration DROP CONSTRAINT c_psp_smsmigration0 ;
--CHECK (migration_status IN ('ValidationInProgress', 'NeedsValidation', 'ValidationSuccess', 'ValidationError', 'ValidationInternalError', 'MigrationError', 'MigrationInProgress', 'MigrationComplete', 'DataCollectionComplete', 'MigrationReverted', 'MigrationOnHold', 'LimitsMigrationError', 'LimitsMigrationRetryableError', 'LimitsMigrationOnHold', 'LimitsMigrationComplete', 'LimitsMigrationIrresolvableError')) NOT VALID;



ALTER TABLE pspadm.psp_smssync_failure DROP CONSTRAINT c_psp_smssync_failure0 ;
--CHECK (sync_direction IN ('PSPToAS', 'ASToPSP'));

ALTER TABLE pspadm.psp_smssync_failure DROP CONSTRAINT c_psp_smssync_failure1 ;
--CHECK (status IN ('Pending', 'NeverRetry', 'Done', 'InProcess'));



ALTER TABLE pspadm.psp_source_payroll_parameter DROP CONSTRAINT c_psp_source_payroll_param0 ;
--CHECK (parameter_cd IN ('MinQBVersionSupported', 'BookTransferEntryDescription', 'ReversalEntryDescription', 'MaxNumberOfFailedLoginAttempts', 'ShouldAddCompanyToPSP', 'PayrollEntryDescription', 'AllowMultipleFundingModels', 'MaxWarehouseTransactionDays', 'DefaultFundingModel', 'LockAccountDuration', 'AllowReverifyBankAccount', 'MinimumEarliestPayrollRunDays', 'DeactiveBankAccountOnReturnedVerificationDebit', 'UnsupportedQBVersionList', 'ResolveEmployeeNOC', 'AllowDuplicatePaycheckIdsIfStatusIsCancelled', 'AutomaticCompanyBankAccountVerification', 'QBVersionSunsetString', 'RetryPaymentEntryDescription', 'AllowBackdatedPayrolls', 'AllowOneOffUntimelyPayrolls', 'MinSupportedTaxTableVersion', 'ThirdParty401kCutoffTime', 'ThirdParty401kOffloadWaitPeriod', 'DefaultRACompanyLimit', 'TaxPaymentEntryDescription', 'TransmitterFEIN', 'TransmitterName', 'TransmitterAddress', 'TransmitterCity', 'TransmitterState', 'TransmitterZip', 'TransmitterZipExtension', 'SyncBillPayments', 'UnsupportedTaxTableList')) NOT VALID;

ALTER TABLE pspadm.psp_source_payroll_parameter DROP CONSTRAINT c_psp_source_payroll_param1 ;
--CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) NOT VALID;



ALTER TABLE pspadm.psp_source_system DROP CONSTRAINT c_psp_source_system0 ;
--CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) NOT VALID;





ALTER TABLE pspadm.psp_sourcesys_printedchk_info DROP CONSTRAINT c_psp_sourcesys_printedchk0 ;
--CHECK (source_system_code IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) NOT VALID;



ALTER TABLE pspadm.psp_state_report_output DROP CONSTRAINT c_psp_state_report_output0 ;
--CHECK (report_type IN ('Recon', 'Coupon', 'ZeroCoupon')) NOT VALID;



ALTER TABLE pspadm.psp_suicredits_job DROP CONSTRAINT c_psp_suicredits_job0 ;
--CHECK (status IN ('Created', 'InProcess', 'Complete', 'Error'));



ALTER TABLE pspadm.psp_system_capability DROP CONSTRAINT c_psp_system_capability0 ;
--CHECK (system_capability_cd IN ('ChangeEmployerBankAccount', 'RefundOrCredit', 'SubmitPayroll', 'SynchronizeAccount', 'AddService', 'CancelService', 'ChangeCompanyInfo', 'ChangeEmployeeBankAccount', 'UpgradeFundingModel', 'VoidPayroll', 'VerifyCompanyBankAccount', 'RecallPayroll', 'UpdatePIN', 'AddEmployerBankAccount', 'SubmitPayment', 'RecallPayment')) NOT VALID;


ALTER TABLE pspadm.psp_system_payment_requirement DROP CONSTRAINT c_psp_system_payment_requi0 ;
--CHECK (system_requirement_type IN ('LAAIDDF', 'EFTPSEnrollment', 'ACHEnrollment')) NOT VALID;



ALTER TABLE pspadm.psp_system_requirement DROP CONSTRAINT c_psp_system_requirement0 ;
--CHECK (system_requirement_type IN ('LAAIDDF', 'EFTPSEnrollment', 'ACHEnrollment')) NOT VALID;





ALTER TABLE pspadm.psp_tax_company_service_info DROP CONSTRAINT c_psp_tax_company_service_0 ;
--CHECK (w2_delivery_preference_cd IN ('Mail', 'Electronic')) NOT VALID;

ALTER TABLE pspadm.psp_tax_company_service_info DROP CONSTRAINT c_psp_tax_company_service_1 ;
--CHECK (client_packet_delivery_pref_cd IN ('Mail', 'Electronic')) NOT VALID;


ALTER TABLE pspadm.psp_tax_payment_on_hold_reason DROP CONSTRAINT c_psp_tax_payment_on_hold_0 ;
--CHECK (on_hold_reason_cd IN ('Enrollment', 'Agent', 'Company', 'Amount', 'BackDate')) NOT VALID;

ALTER TABLE pspadm.psp_tax_payment_on_hold_reason--ADD --PRIMARY KEY (tax_payment_on_hold_reason_seq);

ALTER TABLE pspadm.psp_tax_penalty_interest DROP CONSTRAINT c_psp_tax_penalty_interest0 ;
--CHECK (type IN ('Penalty', 'Interest'));

ALTER TABLE pspadm.psp_tax_penalty_interest DROP CONSTRAINT c_psp_tax_penalty_interest1 ;
--CHECK (payment_method IN ('ACHDebit', 'ACHCredit', 'CheckPayment', 'PostBalfHPDE', 'PostBalfHPDERefund', 'ACHDirectDeposit', 'WirePayment', 'EFE', 'HPDERefund', 'HPDE', 'EFTPS', 'EFTPSDirectDebit', 'EDI', 'SuperCheck')) NOT VALID;

ALTER TABLE pspadm.psp_tax_penalty_interest DROP CONSTRAINT c_psp_tax_penalty_interest2 ;
--CHECK (period_type IN ('Quarter', 'Week', 'Month', 'Annual'));



ALTER TABLE pspadm.psp_third_party401k_batch DROP CONSTRAINT c_psp_third_party401k_batch0 ;
--CHECK (upload_status_cd IN ('Archived', 'Empty', 'Finalized', 'InProcess', 'PendingTransmission', 'Superceded', 'Transmitted', 'Pending')) NOT VALID;



ALTER TABLE pspadm.psp_tp401k_paycheck DROP CONSTRAINT c_psp_tp401k_paycheck0 ;
--CHECK (current_state_cd IN ('Ineligible', 'Cancelled', 'None', 'Pending', 'Sent', 'InvalidPaycheckData', 'InvalidEmployeeData')) NOT VALID;



ALTER TABLE pspadm.psp_tp401k_paycheck_pending DROP CONSTRAINT c_psp_tp401k_paycheck_pend0 ;
--CHECK (state_cd IN ('Ineligible', 'Cancelled', 'None', 'Pending', 'Sent', 'InvalidPaycheckData', 'InvalidEmployeeData')) NOT VALID;


ALTER TABLE pspadm.psp_tp401k_paycheck_state DROP CONSTRAINT c_psp_tp401k_paycheck_state0 ;
--CHECK (state_cd IN ('Ineligible', 'Cancelled', 'None', 'Pending', 'Sent', 'InvalidPaycheckData', 'InvalidEmployeeData')) NOT VALID;



ALTER TABLE pspadm.psp_tp401k_signup_batch DROP CONSTRAINT c_psp_tp401k_signup_batch0 ;
--CHECK (download_status_cd IN ('Archived', 'Empty', 'Finalized', 'InProcess', 'PendingTransmission', 'Superceded', 'Transmitted', 'Pending')) NOT VALID;



ALTER TABLE pspadm.psp_tp401k_signup_queue DROP CONSTRAINT c_psp_tp401k_signup_queue0 ;
--CHECK (status IN ('Processed', 'Pending', 'Cancelled')) NOT VALID;

ALTER TABLE pspadm.psp_transaction_return DROP CONSTRAINT c_psp_transaction_return0 ;
--CHECK (return_status_cd IN ('Created', 'Error', 'Open', 'Resolved'));



ALTER TABLE pspadm.psp_transaction_return_batch DROP CONSTRAINT c_psp_transaction_return_b0 ;
--CHECK (status_cd IN ('Persisted', 'Completed', 'Received', 'Processed')) NOT VALID;

ALTER TABLE pspadm.psp_transaction_return_batch--ADD --PRIMARY KEY (transaction_return_batch_seq);

ALTER TABLE pspadm.psp_transaction_state DROP CONSTRAINT c_psp_transaction_state0 ;
--CHECK (transaction_state_cd IN ('Created', 'Executed', 'Cancelled', 'Returned', 'Completed', 'Voided'));



ALTER TABLE pspadm.psp_transaction_type DROP CONSTRAINT c_psp_transaction_type0 ;
--CHECK (transaction_type_cd IN ('FLAdERLOcERPAY', 'FLAdERLOcTXCC', 'FLAdERPAYcERLO', 'BadDebtRecovery', 'EmployeeDdCredit', 'FLAdERPAYcTXCL', 'FLAdFCRcFI', 'EmployeeDdReversalDebit', 'EmployeeEscalationCredit', 'EmployerDdDebit', 'EmployerDdRedebit', 'EmployerTaxRefundCredit', 'EmployerDdRefundCredit', 'EmployerDdRejectRefundCredit', 'EmployerDdReturnedRefundCredit', 'EmployerDdReversalRefundCredit', 'EmployerDoublePaymentRefundCredit', 'EmployerEscalationCredit', 'EmployerFeeDebit', 'EmployerFeeRedebit', 'EmployerFeeRefundCredit', 'DdFraud', 'EmployerWriteOff', 'Intuit5DayReturnTransfer', 'IntuitEmployeeReturnTransfer', 'IntuitFeeTransfer', 'EmployerFeeReturnedRefundCredit', 'IntuitEmployerVerificationReturnTransfer', 'EmployerVerificationDebit', 'AgencyHPDEWarehousedTaxPayment', 'BadDebtCustomerRecoverySalesAndUseTax', 'IntuitTaxVoidTransfer', 'ThirdPartyCollectionExpense', 'ServiceSalesAndUseTax', 'ServiceSalesAndUseTaxRefundCredit', 'UntimelyReturnPostWriteOff', 'UntimelyReturnPreWriteOff', 'ServiceSalesAndUseTaxRedebit', 'ServiceSalesAndUseTaxReturnedRefundCredit', 'BadDebtRecoveryFee', 'BadDebtRecoverySalesAndUseTax', 'EmployerWriteOffFee', 'EmployerWriteOffSalesAndUseTax', 'EmployerFraudOrEscalationRefundCredit', 'EmployerTaxRedebit', 'EmployerTaxCredit', 'EmployerTaxReturnedCredit', 'AgencyTaxDebit', 'AgencyTaxCredit', 'EmployerTaxDebit', 'EmployerTaxReturnedRefundCredit', 'BadDebtCustomerRecovery', 'EmployerTaxDirectOverpaymentApplied', 'AgencyHPDETaxRefund', 'AgencyTaxRecredit', 'AgencyTaxRedebit', 'FLAdERRCcERRR', 'FLAdEERCcERRR', 'EmployerCobraPaymentAdjustmentDebit', 'AgencyCobraPaymentAdjustmentCredit', 'BadDebtCustomerRecoveryFee', 'EmployerPenaltiesRefundCredit', 'EmployerInterestRefundCredit', 'AgencyInterestCredit', 'FLAdERPAYcTXCC', 'FLAdATRcTXCL', 'FLAdFCBcCOGSINT', 'FLAdTXCCcERSUI', 'FLAdERSUIcTXCC', 'AgencyHPDETaxPayment', 'EmployerCreditBalanceCarryForwardCredit', 'AgencyCreditBalanceCarryForwardDebit', 'FLAdERSUIcTXCL', 'FLAdERSUIcATR', 'FLAdERSUIcERLO', 'FLAdERSUIcERPAY', 'ReissueAgencyTaxDebitOffset', 'FLAdTXCLcERSUI', 'AgencyRefundTOR', 'ReissueTaxLiabilityTransfer', 'FLAdATRcERSUI', 'FLAdERPAYcERSUI', 'FLAdBDcERRR', 'EmployeeReversalFailedWriteOff', 'ERPayableAppliedBalanceDue', 'Intuit5DayFeeReturnTransfer', 'Intuit5DaySalesTaxReturnTransfer', 'FLAdBDcERRC', 'FLAdBDcEERL', 'EmployerTaxDirectDebit', 'AgencyDirectCredit', 'EmployerTaxCreditApplied', 'EmployerTaxOverpaymentApplied', 'AgencyTaxOverpayment', 'AgencyTaxOverpaymentApplied', 'FLAdBDcTXCC', 'FLAdTXCLcTXCC', 'AgencyHPDEPriorPaymentApplied', 'AgencyDirectOverpayment', 'AgencyPostBALFHPDETaxRefund', 'AgencyDirectDebit', 'AgencyPostBALFHPDETaxPayment', 'FLAdTXCLcERLO', 'FLAdERRRcERRC', 'FLAdTXCCcTXCL', 'FLAdATRcERPAY', 'FLAdDDCLcDDCC', 'FLAdDDCCcDDCL', 'FLATemp1', 'FLATemp2', 'FLATemp3', 'FLATemp4', 'FLATemp5', 'EmployerSUITaxReceivable', 'EmployerSUITaxCollection', 'EmployerSUITaxRefund', 'EmployerSUITaxPayable', 'GlobalBookTransfer', 'EmployerVerificationCredit', 'BadDebtCustomerRecoveryTax', 'EmployerTaxDoublePaymentRefundCredit', 'EmployerTaxFraudOrEscalationRefundCredit', 'EmployerWriteOffTax', 'BadDebtRecoveryTax', 'FLAdTXCLcATR', 'FLAdEERLcEERC', 'FLAdTXCLcERPAY', 'FLAdBDcTXCL', 'FLAdTXCCcERPAY', 'FLAdERLOcTXCL', 'EmployerTaxCreditReturnedTransfer', 'EmployerInterestRefundDebit', 'EmployerPenaltiesRefundDebit', 'ERCourtesyRefundCredit', 'FLAdBDcERPAY', 'FLAdERLOcERSUI', 'FLAdTXCCcERLO', 'FLAdBDcATR', 'EmployerVerificationCreditReturnTransfer', 'FLAdBDcERLO', 'FLAdBDcERSUI', 'FLAdERRRcBD', 'FLAdERRCcBD', 'FLAdEERLcBD', 'FLAdTXCCcBD', 'FLAdTXCLcBD', 'FLAdERPAYcBD', 'FLAdATRcBD', 'FLAdERLOcBD', 'FLAdERSUIcBD', 'FLAdATRcTXCC', 'FLAdTXCCcATR', 'FLAdEERCcEERL'));

ALTER TABLE pspadm.psp_transaction_type DROP CONSTRAINT c_psp_transaction_type1 ;
--CHECK (transaction_category IN ('Intuit', 'Employer', 'Employee', 'Agency'));

ALTER TABLE pspadm.psp_transaction_type DROP CONSTRAINT c_psp_transaction_type2 ;
--CHECK (association_type IN ('Reversal', 'Refund', 'Reissue', 'Redebit', 'None', 'Impound', 'FinancialLedgerAdjustment')) NOT VALID;

ALTER TABLE pspadm.psp_transaction_type DROP CONSTRAINT c_psp_transaction_type3 ;
--CHECK (n_a_c_h_a_batch_type IN ('BookTransfer', 'Payroll', 'Reversal', 'RetryPayment', 'TaxPayment')) NOT VALID;

ALTER TABLE pspadm.psp_transaction_type DROP CONSTRAINT c_psp_transaction_type4 ;
--CHECK (transaction_type_group_cd IN ('Debit', 'Redebit', 'Credit', 'Recredit', 'Writeoff', 'Recovery', 'EscalationOrFraud', 'Other', 'CustomerRecovery', 'FinancialLedgerAdjustment', 'SUIPayments')) NOT VALID;



ALTER TABLE pspadm.psp_transmission_payroll_run DROP CONSTRAINT c_psp_transmission_payroll0 ;
--CHECK (payroll_process IN ('SubmitPayroll', 'ReverseTransaction', 'RecallTransaction', 'UpdateTransactionVoidFlag', 'CancelTransaction'));

ALTER TABLE pspadm.psp_wc_company DROP CONSTRAINT c_psp_wc_company0 ;
--CHECK (subs_type_cd IN ('Next', 'SplitLimit'));



ALTER TABLE pspadm.psp_wc_paycheck DROP CONSTRAINT c_psp_wc_paycheck0 ;
--CHECK (current_state_cd IN ('PendingEdit', 'Cancelled', 'PendingNew', 'Sent', 'PendingDelete')) NOT VALID;

ALTER TABLE pspadm.psp_wc_paycheck--ADD --PRIMARY KEY (wc_paycheck_seq);

ALTER TABLE pspadm.psp_wc_paycheck_pending DROP CONSTRAINT c_psp_wc_paycheck_pending0 ;
--CHECK (state_cd IN ('PendingEdit', 'Cancelled', 'PendingNew', 'Sent', 'PendingDelete')) NOT VALID;

ALTER TABLE pspadm.psp_wc_paycheck_pending--ADD --PRIMARY KEY (wc_paycheck_pending_seq);

ALTER TABLE pspadm.psp_wc_paycheck_state DROP CONSTRAINT c_psp_wc_paycheck_state0 ;
--CHECK (state_cd IN ('PendingEdit', 'Cancelled', 'PendingNew', 'Sent', 'PendingDelete')) NOT VALID;



