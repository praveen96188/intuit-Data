--CONSTRAINTS

ALTER TABLE pspadm.gg_heartbeat
ADD PRIMARY KEY (source);

ALTER TABLE pspadm.psp_accounting_report_file
ADD CONSTRAINT c_psp_accounting_report_fi0 CHECK (status IN ('New', 'Created', 'Transmitted', 'Archived')) ;

ALTER TABLE pspadm.psp_accounting_report_file
ADD CONSTRAINT c_psp_accounting_report_fi1 CHECK (type IN ('PositivePay', 'PrintedCheckReconPlus', 'TaxAccountsReconPlus', 'ReturnsAccountsReconPlus')) ;

ALTER TABLE pspadm.psp_accounting_report_file
ADD PRIMARY KEY (accounting_report_file_seq);

ALTER TABLE pspadm.psp_ach_transaction_code
ADD CONSTRAINT c_psp_ach_transaction_code0 CHECK (ach_account_type_cd IN ('Savings', 'Ledger', 'Loan', 'Checking')) ;

ALTER TABLE pspadm.psp_ach_transaction_code
ADD CONSTRAINT c_psp_ach_transaction_code1 CHECK (credit_debit_indicator IN ('Credit', 'Debit')) ;

ALTER TABLE pspadm.psp_ach_transaction_code
ADD PRIMARY KEY (transaction_code);

ALTER TABLE pspadm.psp_achenrollment
ADD CONSTRAINT c_psp_achenrollment0 CHECK (status IN ('Cancelled', 'Deleted', 'EnrollmentRejected', 'Enrolled', 'PendingEnrollmentResponse', 'PendingDelete', 'PendingEnrollment')) ;

ALTER TABLE pspadm.psp_achenrollment
ADD PRIMARY KEY (achenrollment_seq);

ALTER TABLE pspadm.psp_achenrollment_detail
ADD PRIMARY KEY (achenrollment_detail_seq);

ALTER TABLE pspadm.psp_achenrollment_file
ADD CONSTRAINT c_psp_achenrollment_file0 CHECK (status IN ('Archived', 'Processed', 'PendingTransmission', 'SentToAgency', 'UploadedByAgent', 'Error')) ;

ALTER TABLE pspadm.psp_achenrollment_file
ADD CONSTRAINT c_psp_achenrollment_file1 CHECK (type IN ('Add', 'Delete', 'Response')) ;

ALTER TABLE pspadm.psp_achenrollment_file
ADD PRIMARY KEY (achenrollment_file_seq);

ALTER TABLE pspadm.psp_action_event
ADD CONSTRAINT c_psp_action_event0 CHECK (code IN ('VoidTORTransaction', 'RefundERPayableCancel', 'FinancialTransactionVoidTx', 'FinancialTransactionCancel', 'IssueReissueRefundEr', 'TxStateHistory', 'DDTransactionCancel', 'DDTransactionReverse', 'DDRedebitAdd', 'DDRedebitRecord', 'ERFeeAdd', 'BadDebtWriteOff', 'BadDebtRecover', 'EEReturnTransfer', 'FeeTransfer', 'Intuit5DayReturnTransfer', 'DDRefund', 'ERReturnRefund', 'EEReturnRefund', 'ERWireExpected', 'RefundRebillFee', 'DDRedebitEdit', 'ERFraudOrEscalationRefund', 'BadDebtWriteOffEEReturn', 'RecordPrefundingWire', 'CancelAdjustment', 'VoidPayrollTaxPayment', 'ReissuePayrollTaxPayment', 'ApplyERPayableToBalanceDue', 'RefundDebit', 'ERFeeCancel')) ;

ALTER TABLE pspadm.psp_action_event
ADD CONSTRAINT c_psp_action_event1 CHECK (type IN ('FinancialTransaction', 'PayrollRun', 'LedgerAccount'));

ALTER TABLE pspadm.psp_action_event
ADD PRIMARY KEY (code);

ALTER TABLE pspadm.psp_additional_filing_amount
ADD PRIMARY KEY (name);

ALTER TABLE pspadm.psp_address
ADD PRIMARY KEY (address_seq);

ALTER TABLE pspadm.psp_ade_law_map
ADD PRIMARY KEY (ade_law_map_id);

ALTER TABLE pspadm.psp_agency
ADD CONSTRAINT c_psp_agency0 CHECK (default_r_a_a_form IN ('LPOA', 'Federal8655'));

ALTER TABLE pspadm.psp_agency
ADD PRIMARY KEY (agency_id);

ALTER TABLE pspadm.psp_agency_check_batch
ADD PRIMARY KEY (agency_check_batch_seq);

ALTER TABLE pspadm.psp_agency_id_requirement
ADD CONSTRAINT c_psp_agency_id_requirement0 CHECK (custom_requirement IN ('MustNotInExemptedIdList', 'MustNotContainFedTaxId', 'IFNotPatternMustFollowFedTaxId', 'IfNotMEorTRMustFollowFedTaxId', 'MustNotFollowFedTaxId', 'MustStartWithFedTaxId', 'MustFollowFedTaxId', 'Digits4Through12FollowFedTaxId', 'Digits2Through10FollowFedTaxId', 'None', 'MustNotFollowFedTaxIdSubstitueIf8Digits', 'Digits3Through11FollowFedTaxId')) ;

ALTER TABLE pspadm.psp_agency_id_requirement
ADD PRIMARY KEY (agency_id_requirement_seq);

ALTER TABLE pspadm.psp_agency_rate_request
ADD CONSTRAINT c_psp_agency_rate_request0 CHECK (status IN ('Created', 'GeneratingRequest', 'RequestGenerated', 'RequestSent', 'ResponseReceived', 'ResponseVerified', 'ResponseApplying', 'ResponseApplied', 'Cancelled')) ;

ALTER TABLE pspadm.psp_agency_rate_request
ADD PRIMARY KEY (agency_rate_request_seq);

ALTER TABLE pspadm.psp_annual_billing_batch
ADD CONSTRAINT c_psp_annual_billing_batch0 CHECK (form_type_cd IN ('W2')) ;

ALTER TABLE pspadm.psp_annual_billing_batch
ADD CONSTRAINT c_psp_annual_billing_batch1 CHECK (annual_billing_batch_status_cd IN ('Completed', 'Pending')) ;

ALTER TABLE pspadm.psp_annual_billing_batch
ADD PRIMARY KEY (annual_billing_batch_seq);

ALTER TABLE pspadm.psp_annual_billing_item
ADD CONSTRAINT c_psp_annual_billing_item0 CHECK (annual_billing_item_status_cd IN ('Pending', 'Error', 'Completed', 'Skipped')) ;

ALTER TABLE pspadm.psp_annual_billing_item
ADD PRIMARY KEY (annual_billing_item_seq);

ALTER TABLE pspadm.psp_applied_database_patch
ADD CONSTRAINT c_psp_applied_database_pat0 CHECK (database_patch_type_cd IN ('SchemaUpgrade', 'DataMigration'));

ALTER TABLE pspadm.psp_applied_database_patch
ADD PRIMARY KEY (applied_database_patch_seq);

ALTER TABLE pspadm.psp_archive_record
ADD PRIMARY KEY (archive_record_seq);

ALTER TABLE pspadm.psp_assisted_bundle_bill
ADD CONSTRAINT c_psp_assisted_bundle_bill0 CHECK (asst_status IN ('Open', 'ProcessingFailed', 'SentToBRM', 'SentToBRMFailed', 'Processed'));

ALTER TABLE pspadm.psp_assisted_bundle_bill
ADD PRIMARY KEY (assisted_bundle_bill_seq);

ALTER TABLE pspadm.psp_asst_bundle_bill_detail
ADD PRIMARY KEY (asst_bundle_bill_detail_seq);

ALTER TABLE pspadm.psp_asst_bundle_comp_usage
ADD CONSTRAINT c_psp_asst_bundle_comp_usa0 CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO'));

ALTER TABLE pspadm.psp_asst_bundle_comp_usage
ADD PRIMARY KEY (asst_bundle_comp_usage_seq);

ALTER TABLE pspadm.psp_atfdata_extract_batch
ADD CONSTRAINT c_psp_atfdata_extract_batch0 CHECK (run_type IN ('QuarterlyData', 'UpdatedData', 'AnnualData')) ;

ALTER TABLE pspadm.psp_atfdata_extract_batch
ADD CONSTRAINT c_psp_atfdata_extract_batch1 CHECK (batch_status IN ('Submitted', 'Completed', 'InProgress', 'Aborted'));

ALTER TABLE pspadm.psp_atfdata_extract_batch
ADD PRIMARY KEY (atfdata_extract_batch_seq);

ALTER TABLE pspadm.psp_atfdata_extract_file
ADD CONSTRAINT c_psp_atfdata_extract_file0 CHECK (file_type IN ('CompanyInfo', 'EmployeeInfo', 'CompanyTaxInfo', 'CompanyTaxRateInfo', 'CompanyLiabilitiesInfo', 'CompanyPaymentsInfo', 'CompanyDepFreqInfo', 'EmployeeTotalsInfo', 'CompanyAdjustmentsInfo', 'W2CountInfo', 'WageLimitsInfo', 'CompanyPayrollItemInfo'));

ALTER TABLE pspadm.psp_atfdata_extract_file
ADD CONSTRAINT c_psp_atfdata_extract_file1 CHECK (file_status IN ('Started', 'Extracted', 'Failed', 'Completed', 'Aborted'));

ALTER TABLE pspadm.psp_atfdata_extract_file
ADD PRIMARY KEY (atfdata_extract_file_seq);

ALTER TABLE pspadm.psp_atfpayments_to_process
ADD PRIMARY KEY (atfpayments_to_process_seq);

ALTER TABLE pspadm.psp_atfpayrolls_to_process
ADD PRIMARY KEY (atfpayrolls_to_process_seq);

ALTER TABLE pspadm.psp_auth_domain
ADD PRIMARY KEY (domain_id);

ALTER TABLE pspadm.psp_auth_operation
ADD CONSTRAINT c_psp_auth_operation0 CHECK (operation_id IN ('EditQBFileID', 'VoidTORTransaction', 'EditAllowTransmissions', 'RemoveTaxTableSuspension', 'ExecuteRTBAutomationJob', 'ScheduleATFExtract', 'BankReturnUpdate', 'AccessApplication', 'DDLimitUpdate', 'FundingModelUpdate', 'DDStatusUpdate', 'StrikeAdd', 'StrikeCancel', 'ExecuteRTBJob', 'SettingUpdate', 'ViewFullBankAccountNumbers', 'UpdateCancelTermCompany', 'GeneratePin', 'AddBankAccountRandomDebits', 'AddOffering', 'AddOffer', 'EditChartOfAccounts', 'AddBankAccountByPassRandomDebits', 'BankReturnView', 'RecordNonACHRedebitTransaction', 'CreateFeeTransaction', 'CreateReversalTransaction', 'DDTransactionCancel', 'TransactionCancel', 'LedgerView', 'SelectNonStandardSettlementType', 'CreateRefundTransaction', 'VoidTransaction', 'BookTransferTransaction', 'ActivateBankAccount', 'WriteoffBadDebtTransaction', 'RecoverBadDebtTransaction', 'EscalationCreditTransaction', 'IssueRedebitTransaction', 'AuthAccessApplication', 'AuthAddUpdateUsers', 'AuthRemoveUsers', 'AuthAddUpdateHelpDesk', 'AuthAddRemoveHelpDesk', 'AuthAddUpdateDataCustodian', 'AuthRemoveDataCustodian', 'EditCompanyLegalInformation', 'EditCompanyContactInformation', 'ViewTransactionHistory', 'ViewVerificationDebits', 'ResetVerificationAmounts', 'GenerateRandomDebits', 'RefundERPayable', 'ViewPayrollScreen', 'EnterWireExpectedDate', 'ViewSignupFraudQueue', 'RemoveFromSignupFraudHold', 'UploadToGems', 'ViewOFX', 'RequestSecondOffload', 'ViewChaseReport', 'PrintChaseReport', 'AgentInitiatesRefundRebill', 'SavePrintOFX', 'ConfirmOffload', 'RefundEmployerFraudEscalation', 'AddAssistedEIN', 'AddToEINDIY', 'AddToEINAssisted', 'MoveEINDIYDIY', 'MoveEINDIYAssisted', 'DeactivateEIN', 'DeactivateEINPendingActivation', 'DeactivateEINActive', 'ReactivateEINDIY', 'ReactivateEINAssisted', 'EditTokens', 'EditDebugLogging', 'AssignChecklist', 'CreateRAFFile', 'CreateACHFile', 'ViewOperatorTab', 'AddCheckDistributionService', 'DecryptText', 'WriteoffEmployeeBadDebtTransaction', 'RecordPrefundingWire', 'ViewOffloadStatus', 'AddVendorPaymentService', 'AddAS400Company', 'ViewCheckPrintSignature', 'AddUpdateCheckPrintSignature', 'ViewCheckPrintQueue', 'UpdateCheckPrintBatchStatus', 'TaxCreditsWOTC', 'ViewVMPData', 'CancelCloud', 'ViewGlobalEnrollments', 'ResolveEFTPSReject', 'ManageRAFEnrollment', 'ManageTaxPayments', 'ViewTaxLedger', 'ViewCompanyTaxPayments', 'CreateManualLedgerEntry', 'ViewAgencyInfo', 'ViewEEPII', 'ViewOverpayments', 'ViewGlobalTaxPayments', 'EditVMPData', 'ViewMoneyMovementScreen', 'ExecuteSQL', 'ViewSystemParameters', 'EditTaxExemptFlag', 'EditPrincipalContactsDIYOnly', 'EditAssistedPayrollContactsInPendingActivation', 'EditAssistedPayrollContactsInActiveStatus', 'EditAssistedEINPendingActivation', 'EditAssistedEINActive', 'EditAssistedCompanyLegalInfo', 'AddUpdatePriceType', 'AddDIYEIN', 'AccessDataSyncTool', 'CreateFLA', 'RecalculateLedgerBalances', 'CreateBookTransfer', 'ManageSUITaxPayments', 'ReportFileDownload', 'EditAssistedPrincipalContacts', 'EditAssistedPrincipalContactsInPendingActivation', 'EditAssistedCompanyLegalInfoPendingActivation', 'CreateERPenaltiesAndInterestRefunds', 'AddManualFeeTransactions', 'CreateMultipleBackdatingRefunds', 'CreateCourtesyRefund', 'AddRestrictedOffer', 'EditProcessTransmissions', 'UpdateComplianceData', 'AddAssistedBankAccountPreBALF', 'AddAssistedBankAccountBypassRandomDollarDebitPreBALF', 'AddBankAccountByPassRandomDebitsPostBALF', 'AddBankAccountRandomDebitsPostBALF', 'AddAssistedOfferPreBALF', 'AddAssistedOfferPostBALF', 'AddAssistedOfferingPreBALF', 'AddAssistedOfferingPostBALF', 'LedgerOperations', 'SearchBySSN', 'CreateTOR', 'EmployerFeeDebitCancel', 'ManualEFTPSEnrollments', 'EditEntityChangeInfo', 'EditCancellationInfo', 'EditACHRegFlag', 'EditAgencyIDs', 'EditDepositFreq', 'EditFilerType', 'EditFilingAmts', 'EditFilingAmtsOtherQtr', 'EditFilingFlags', 'EditRatesOtherLaws', 'EditSUIRateCurrQTR', 'EditRatesInOtherQTRs', 'RateSuperUser', 'IPBasedFraudFilteringView'));

ALTER TABLE pspadm.psp_auth_operation
ADD PRIMARY KEY (operation_id);

ALTER TABLE pspadm.psp_auth_role
ADD PRIMARY KEY (auth_role_seq);

ALTER TABLE pspadm.psp_auth_user
ADD PRIMARY KEY (auth_user_seq);

ALTER TABLE pspadm.psp_auth_user_auth_role__assoc
ADD PRIMARY KEY (auth_user_fk, auth_role_fk);

ALTER TABLE pspadm.psp_authrole_operation_assoc
ADD PRIMARY KEY (auth_role_fk, auth_operation_fk);

ALTER TABLE pspadm.psp_bank_account
ADD CONSTRAINT c_psp_bank_account0 CHECK (a_c_h_account_type_cd IN ('Savings', 'Ledger', 'Loan', 'Checking')) ;

ALTER TABLE pspadm.psp_bank_account
ADD CONSTRAINT c_psp_bank_account1 CHECK (account_type_cd IN ('Checking', 'Savings')) ;

ALTER TABLE pspadm.psp_bank_account
ADD CONSTRAINT c_psp_bank_account2 CHECK (a_c_h_entry_class IN ('CCD', 'PPD')) ;

ALTER TABLE pspadm.psp_bank_account
ADD PRIMARY KEY (bank_account_seq);

ALTER TABLE pspadm.psp_bank_holiday
ADD PRIMARY KEY (bank_holiday_date);

ALTER TABLE pspadm.psp_batch_job_audit_log
ADD PRIMARY KEY (batch_job_audit_log_seq);

ALTER TABLE pspadm.psp_batch_job_parameter
ADD PRIMARY KEY (id);


--ALTER TABLE pspadm.psp_batch_job_setup
--ADD CONSTRAINT c_psp_batch_job_setup0 CHECK (job_type IN ('FailedPayrollPlSqlJobsProcessor', 'PayrollFraudBatchPurgePlSqlJobsProcessor', 'PSPEventLogPurgePlSqlJobsProcessor', 'DailyPayrollStatsPlSqlJobsProcessor', 'EFTPSOnHoldPaymentPlSqlJobsProcessor', 'ValidateEmployeeWagePlansPlSqlJobsProcessor', 'EmployeePayrollItemTotalsCalcProcess', 'SendW2AnnualDataToTFSMonitor', 'SendW2PreviewDataToTFSProcessor', 'SendW2PreviewDataToTFSMonitor', 'SUICreditsBatchJob', 'AnnualBillingMonitor', 'SalesTaxExceptionMonitor', 'EnrollmentDeleteSelectionProcessor', 'NCDFixPlSqlJobsProcessor', 'OfferingUpdateUsageBillingPlSqlJobsProcessor', 'BRMUsageErrorFileProcessor', 'EDRAssociationFixPlSqlJobsProcessor', 'RetryEntitlementActivationPlSqlJobsProcessor', 'NCDFixALLPlSqlJobsProcessor', 'CostCoPlSqlJobsProcessor', 'EmployeeTotalsCalculationProcess', 'EmployeeTotalsCalculationMonitor', 'RTBAutomation', 'AchReturnsMonitor', 'EdiPaymentMonitor', 'AchTransactionsMonitor', 'BalanceFileMonitor', 'EmailGateway', 'EmailGatewayMonitor', 'FeeEvents', 'FeeEventsMonitor', 'FraudPayrolls', 'FraudPayrollsMonitor', 'GemsAccountsReceivable', 'GemsAccountsReceivableMonitor', 'GemsGeneralLedger', 'GemsGeneralLedgerMonitor', 'LedgerBalance', 'MissedPayrollsMonitor', 'MissedTransactionsMonitor', 'NightlyBatchJobs', 'NightlyBatchJobsMonitor', 'PrimaryAchOffloadMonitor', 'PrimaryDailyBatchJobs', 'PrimaryDailyBatchJobsMonitor', 'ScheduledAchOffloadMonitor', 'ScheduledDailyBatchJobs', 'ScheduledDailyBatchJobsMonitor', 'As400EventSync', 'GemsGeneralLedgerUpload', 'GemsGeneralLedgerUploadMonitor', 'AchOffloadCompleteMonitor', 'EventsGateway', 'RAFWriter', 'EftpsEnrollmentsAgeOutMonitor', 'EftpsPaymentMonitor', 'PSPToAs400DataSyncMonitor', 'EftpsEnrollmentsAgeOut', 'EftpsPayment', 'EftpsEnrollments', 'OFACReportProcessor', 'AMLReportProcessor', 'IndustryReportProcessor', 'OFACReportMonitor', 'AMLReportMonitor', 'IndustryReportMonitor', 'BRMAssistedUsageErrorFileProcessor', 'ATFDataExtract', 'TaxPaymentSubmission', 'TaxPaymentSynchronization', 'TriggerAmendments', 'HPDEBatchProcessor', 'SalesTaxExceptionProcessor', 'TPSUReportProcessor', 'TPSUReportMonitor', 'ATFDepositFrequencyExtract', 'PrimaryDailyForecast', 'CheckPrint', 'CheckPrintMonitor', 'TaxCreditsEchoSignMonitor', 'OffloadedTransactionsEvents', 'OffloadedTransactionsEventsMonitor', 'ThirdParty401kOffload', 'ThirdParty401kSignup', 'ThirdParty401kValidation', 'ThirdParty401kOffloadMonitor', 'ThirdParty401kSignupMonitor', 'ThirdParty401kValidationMonitor', 'AssistedUsageReportingToBRMProcessor', 'AssistedUsageDataSyncProcessor', 'As400DataSync', 'As400DataSyncMonitor', 'TaxCreditsEchoSign', 'SoxDBUserReport', 'QbdtUnprocessedRequestsRetry', 'EftpsEnrollmentsMonitor', 'IOPDataSync', 'PSPToAs400DataSync', 'IOPDataSyncMonitor', 'ATFCompanyLiabilityExtract', 'ATFCompanyPaymentExtract', 'AchTaxPaymentOffloadMonitor', 'PrintedCheckBatch', 'PrintedCheckBatchMonitor', 'AchTaxPaymentOffload', 'AchZeroPayments', 'AchZeroPaymentsMonitor', 'ReconPlus', 'ReconPlusMonitor', 'StateReportMonitor', 'StateReport', 'StateCouponMonitor', 'StateCoupon', 'ComplianceToolKit', 'PSPToAs400', 'PSPToAs400Monitor', 'AMOMessageProcessorMonitor', 'EftpsResponse', 'EntitlementProcessor', 'EntitlementProcessorMonitor', 'AS400EventSyncMonitor', 'AMOMessageProcessor', 'EftpsResponseMonitor', 'EftpsSend', 'EftpsSendMonitor', 'EdiResponse', 'EdiResponseMonitor', 'EdiSend', 'EdiSendMonitor', 'EdiPayment', 'AchDebitOffload', 'AchDebitOffloadMonitor', 'EoqSUIAdjustments', 'EoqSUIAdjustmentsMonitor', 'EntityEvent', 'EMSBSToBRMDataSyncProcessor', 'IRSDepositFrequencyFileProcessorMonitor', 'IRSDepositFrequencyFileProcessor', 'PSPToEMSBSDataSyncProcessor', 'MonthlyFee', 'MonthlyFeeMonitor', 'ATFWageLimitsExtract', 'ATFCompanyInfoExtract', 'ATFEmployeeInfoExtract', 'ATFEmployeeTotalsExtract', 'ATFCompanyTaxExtract', 'ATFCompanyTaxRateExtract', 'ATFEmployeeTotalsCalculation', 'EmployeeW2TotalsCalculationMonitor', 'EmployeeW2TotalsCalculationProcessor', 'SendW2AnnualDataToTFSProcessor', 'FsetFilingProcessor', 'FsetFilingMonitor', 'FsetResponseProcessor', 'FsetResponseMonitor', 'ScheduledEmails', 'LedgerBalanceMonitor', 'LedgerOperations', 'W2CountsExtract', 'WorkersCompProcessor', 'WorkersCompMonitor', 'AnnualBillingProcessor', 'SendMonthlyDataToTFSMonitor', 'SendMonthlyDataToTFSProcessor', 'ACHDeEnrollmentBatchJob', 'ACHEnrollmentResponseBatchJob', 'ACHEnrollmentBatchJob', 'IamEmailAddressMonitor', 'IamEmailAddressProcessor', 'EnrollmentDeleteSelectionMonitor', 'DDMAchOffloadMonitor', 'DDMDailyBatchJobs', 'DataPartitionProcessor', 'EntityEventRetry', 'ACHTraceIdProcessor', 'ATFCompanyPayrollItemExtract', 'MassCancelAutoProcessor', 'AccountServiceSyncExceptionProcessor', 'SoxReport', 'MtlTransactionReportEnrichProcessor', 'EntityInitialLoadProcessor', 'EVSCompanyProcessor', 'DataReencryptionProcessor', 'BulkWorkforceInviteProcessor', 'MTLCompanyToOnHoldProcessor', 'PSPToSMSMigrationProcessor', 'EMSBSToBRMDataSyncProcessorMonitor', 'RiskProfileMigrationProcessor', 'CompanyMigrationProcessor', 'RealTimeEntityEventRetryProcessor')) ;

ALTER TABLE pspadm.psp_batch_job_setup
ADD PRIMARY KEY (job_type);

--ALTER TABLE pspadm.psp_batch_job_status
--ADD CONSTRAINT c_psp_batch_job_status0 CHECK (job_type IN ('FailedPayrollPlSqlJobsProcessor', 'PayrollFraudBatchPurgePlSqlJobsProcessor', 'PSPEventLogPurgePlSqlJobsProcessor', 'DailyPayrollStatsPlSqlJobsProcessor', 'EFTPSOnHoldPaymentPlSqlJobsProcessor', 'ValidateEmployeeWagePlansPlSqlJobsProcessor', 'EmployeePayrollItemTotalsCalcProcess', 'SendW2AnnualDataToTFSMonitor', 'SendW2PreviewDataToTFSProcessor', 'SendW2PreviewDataToTFSMonitor', 'SUICreditsBatchJob', 'AnnualBillingMonitor', 'SalesTaxExceptionMonitor', 'EnrollmentDeleteSelectionProcessor', 'NCDFixPlSqlJobsProcessor', 'OfferingUpdateUsageBillingPlSqlJobsProcessor', 'BRMUsageErrorFileProcessor', 'EDRAssociationFixPlSqlJobsProcessor', 'RetryEntitlementActivationPlSqlJobsProcessor', 'NCDFixALLPlSqlJobsProcessor', 'CostCoPlSqlJobsProcessor', 'EmployeeTotalsCalculationProcess', 'EmployeeTotalsCalculationMonitor', 'RTBAutomation', 'AchReturnsMonitor', 'EdiPaymentMonitor', 'AchTransactionsMonitor', 'BalanceFileMonitor', 'EmailGateway', 'EmailGatewayMonitor', 'FeeEvents', 'FeeEventsMonitor', 'FraudPayrolls', 'FraudPayrollsMonitor', 'GemsAccountsReceivable', 'GemsAccountsReceivableMonitor', 'GemsGeneralLedger', 'GemsGeneralLedgerMonitor', 'LedgerBalance', 'MissedPayrollsMonitor', 'MissedTransactionsMonitor', 'NightlyBatchJobs', 'NightlyBatchJobsMonitor', 'PrimaryAchOffloadMonitor', 'PrimaryDailyBatchJobs', 'PrimaryDailyBatchJobsMonitor', 'ScheduledAchOffloadMonitor', 'ScheduledDailyBatchJobs', 'ScheduledDailyBatchJobsMonitor', 'As400EventSync', 'GemsGeneralLedgerUpload', 'GemsGeneralLedgerUploadMonitor', 'AchOffloadCompleteMonitor', 'EventsGateway', 'RAFWriter', 'EftpsEnrollmentsAgeOutMonitor', 'EftpsPaymentMonitor', 'PSPToAs400DataSyncMonitor', 'EftpsEnrollmentsAgeOut', 'EftpsPayment', 'EftpsEnrollments', 'OFACReportProcessor', 'AMLReportProcessor', 'IndustryReportProcessor', 'OFACReportMonitor', 'AMLReportMonitor', 'IndustryReportMonitor', 'BRMAssistedUsageErrorFileProcessor', 'ATFDataExtract', 'TaxPaymentSubmission', 'TaxPaymentSynchronization', 'TriggerAmendments', 'HPDEBatchProcessor', 'SalesTaxExceptionProcessor', 'TPSUReportProcessor', 'TPSUReportMonitor', 'ATFDepositFrequencyExtract', 'PrimaryDailyForecast', 'CheckPrint', 'CheckPrintMonitor', 'TaxCreditsEchoSignMonitor', 'OffloadedTransactionsEvents', 'OffloadedTransactionsEventsMonitor', 'ThirdParty401kOffload', 'ThirdParty401kSignup', 'ThirdParty401kValidation', 'ThirdParty401kOffloadMonitor', 'ThirdParty401kSignupMonitor', 'ThirdParty401kValidationMonitor', 'AssistedUsageReportingToBRMProcessor', 'AssistedUsageDataSyncProcessor', 'As400DataSync', 'As400DataSyncMonitor', 'TaxCreditsEchoSign', 'SoxDBUserReport', 'QbdtUnprocessedRequestsRetry', 'EftpsEnrollmentsMonitor', 'IOPDataSync', 'PSPToAs400DataSync', 'IOPDataSyncMonitor', 'ATFCompanyLiabilityExtract', 'ATFCompanyPaymentExtract', 'AchTaxPaymentOffloadMonitor', 'PrintedCheckBatch', 'PrintedCheckBatchMonitor', 'AchTaxPaymentOffload', 'AchZeroPayments', 'AchZeroPaymentsMonitor', 'ReconPlus', 'ReconPlusMonitor', 'StateReportMonitor', 'StateReport', 'StateCouponMonitor', 'StateCoupon', 'ComplianceToolKit', 'PSPToAs400', 'PSPToAs400Monitor', 'AMOMessageProcessorMonitor', 'EftpsResponse', 'EntitlementProcessor', 'EntitlementProcessorMonitor', 'AS400EventSyncMonitor', 'AMOMessageProcessor', 'EftpsResponseMonitor', 'EftpsSend', 'EftpsSendMonitor', 'EdiResponse', 'EdiResponseMonitor', 'EdiSend', 'EdiSendMonitor', 'EdiPayment', 'AchDebitOffload', 'AchDebitOffloadMonitor', 'EoqSUIAdjustments', 'EoqSUIAdjustmentsMonitor', 'EntityEvent', 'EMSBSToBRMDataSyncProcessor', 'IRSDepositFrequencyFileProcessorMonitor', 'IRSDepositFrequencyFileProcessor', 'PSPToEMSBSDataSyncProcessor', 'MonthlyFee', 'MonthlyFeeMonitor', 'ATFWageLimitsExtract', 'ATFCompanyInfoExtract', 'ATFEmployeeInfoExtract', 'ATFEmployeeTotalsExtract', 'ATFCompanyTaxExtract', 'ATFCompanyTaxRateExtract', 'ATFEmployeeTotalsCalculation', 'EmployeeW2TotalsCalculationMonitor', 'EmployeeW2TotalsCalculationProcessor', 'SendW2AnnualDataToTFSProcessor', 'FsetFilingProcessor', 'FsetFilingMonitor', 'FsetResponseProcessor', 'FsetResponseMonitor', 'ScheduledEmails', 'LedgerBalanceMonitor', 'LedgerOperations', 'W2CountsExtract', 'WorkersCompProcessor', 'WorkersCompMonitor', 'AnnualBillingProcessor', 'SendMonthlyDataToTFSMonitor', 'SendMonthlyDataToTFSProcessor', 'ACHDeEnrollmentBatchJob', 'ACHEnrollmentResponseBatchJob', 'ACHEnrollmentBatchJob', 'IamEmailAddressMonitor', 'IamEmailAddressProcessor', 'EnrollmentDeleteSelectionMonitor', 'DDMAchOffloadMonitor', 'DDMDailyBatchJobs', 'DataPartitionProcessor', 'EntityEventRetry', 'ACHTraceIdProcessor', 'ATFCompanyPayrollItemExtract', 'MassCancelAutoProcessor', 'AccountServiceSyncExceptionProcessor', 'SoxReport', 'MtlTransactionReportEnrichProcessor', 'EntityInitialLoadProcessor', 'EVSCompanyProcessor', 'DataReencryptionProcessor', 'BulkWorkforceInviteProcessor', 'MTLCompanyToOnHoldProcessor', 'PSPToSMSMigrationProcessor', 'EMSBSToBRMDataSyncProcessorMonitor', 'RiskProfileMigrationProcessor', 'CompanyMigrationProcessor', 'RealTimeEntityEventRetryProcessor')) ;

ALTER TABLE pspadm.psp_batch_job_status
ADD CONSTRAINT c_psp_batch_job_status1 UNIQUE (job_type);

ALTER TABLE pspadm.psp_batch_job_status
ADD PRIMARY KEY (batch_job_status_seq);

ALTER TABLE pspadm.psp_bill
ADD PRIMARY KEY (bill_seq);

ALTER TABLE pspadm.psp_bill_payment
ADD CONSTRAINT c_psp_bill_payment0 CHECK (status IN ('Active', 'Inactive')) ;

ALTER TABLE pspadm.psp_bill_payment
ADD CONSTRAINT c_psp_bill_payment1 CHECK (transaction_type IN ('PayBills', 'WriteChecks')) ;

ALTER TABLE pspadm.psp_bill_payment
ADD PRIMARY KEY (bill_payment_seq);

ALTER TABLE pspadm.psp_bill_payment_split
ADD PRIMARY KEY (bill_payment_split_seq);

ALTER TABLE pspadm.psp_billing_detail
ADD CONSTRAINT c_psp_billing_detail0 CHECK (offering_service_charge_type IN ('CourtesyRefund', 'PerPayroll', 'PerPaycheck', 'PerTransmission', 'AmendedSSN', 'PerPayment', 'ReversalFee', 'DebitReturnFee', 'ManualServicingFee', 'ChaseReportFeeUpTo3Payrolls', 'ChaseReportFeeUpTo6Payrolls', 'ChaseReportFeeUpTo15Payrolls', 'ChaseReportFeeUpTo20Payrolls', 'ChaseReportFeeOver20Payrolls', 'BankVerificationDebit', 'PaymentArrangementFee', 'PerBatch', 'CompanyUpdates', 'EmployeesAdded', 'EmployeesUpdated', 'BackdatedPayroll', 'DirectDepositFee', 'EmployeesPaid', 'MonthlyFee', 'Amendments', 'EntityChange', 'ExtraCopies', 'PayrollAdjustment', 'PenaltiesAndInterest', 'ExtraStateFee', 'OtherFee', 'W2Fee', 'EmployeeOrganizerFee', 'W2Correction', 'W2BaseFee', 'BankVerificationCredit')) ;

ALTER TABLE pspadm.psp_billing_detail
ADD PRIMARY KEY (billing_detail_seq);

ALTER TABLE pspadm.psp_bpcompany_service_info
ADD PRIMARY KEY (bpcompany_service_info_seq);

ALTER TABLE pspadm.psp_cdcompany_service_info
ADD PRIMARY KEY (cdcompany_service_info_seq);

ALTER TABLE pspadm.psp_check_print_batch
ADD CONSTRAINT c_psp_check_print_batch0 CHECK (check_print_batch_status_code IN ('Pending', 'SentToPrinter', 'Error')) ;

ALTER TABLE pspadm.psp_check_print_batch
ADD PRIMARY KEY (check_print_batch_seq);

ALTER TABLE pspadm.psp_check_print_paycheck
ADD CONSTRAINT c_psp_check_print_paycheck0 CHECK (cp_paycheck_status_code IN ('ReceivedWithNoCheckNumber', 'VoidedBeforePrinting', 'AddedToPrintBatch', 'DeletedBeforePrinting')) ;

ALTER TABLE pspadm.psp_check_print_paycheck
ADD PRIMARY KEY (check_print_paycheck_seq);

ALTER TABLE pspadm.psp_check_print_signature
ADD PRIMARY KEY (check_print_signature_seq);

ALTER TABLE pspadm.psp_collection_stage
ADD CONSTRAINT c_psp_collection_stage0 CHECK (collection_stage_code IN ('FirstCollectionAttempt', 'SecondCollectionAttempt', 'TerminationExpected'));

ALTER TABLE pspadm.psp_collection_stage
ADD PRIMARY KEY (collection_stage_code);

ALTER TABLE pspadm.psp_comp_adjust_submission
ADD PRIMARY KEY (comp_adjust_submission_seq);

ALTER TABLE pspadm.psp_comp_pmt_template_agencyid
ADD PRIMARY KEY (comp_pmt_template_agencyid_seq);

ALTER TABLE pspadm.psp_comp_pmttemplate_pmtmethod
ADD CONSTRAINT c_psp_comp_pmttemplate_pmt0 CHECK (payment_method IN ('ACHDebit', 'ACHCredit', 'CheckPayment', 'PostBalfHPDE', 'PostBalfHPDERefund', 'ACHDirectDeposit', 'WirePayment', 'EFE', 'HPDERefund', 'HPDE', 'EFTPS', 'EFTPSDirectDebit', 'EDI', 'SuperCheck')) ;

ALTER TABLE pspadm.psp_comp_pmttemplate_pmtmethod
ADD PRIMARY KEY (comp_pmttemplate_pmtmethod_seq);

ALTER TABLE pspadm.psp_company
ADD CONSTRAINT c_psp_company0 CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) ;

ALTER TABLE pspadm.psp_company
ADD CONSTRAINT c_psp_company1 CHECK (tax_exempt_status IN ('Exempt', 'New', 'NonExempt')) ;

ALTER TABLE pspadm.psp_company
ADD PRIMARY KEY (company_seq);

ALTER TABLE pspadm.psp_company_additional_info
ADD PRIMARY KEY (company_additional_info_seq);

ALTER TABLE pspadm.psp_company_agency
ADD PRIMARY KEY (company_agency_seq);

ALTER TABLE pspadm.psp_company_bank_account
ADD CONSTRAINT c_psp_company_bank_account0 CHECK (status_cd IN ('PendingVerification', 'Active', 'Inactive'));

ALTER TABLE pspadm.psp_company_bank_account
ADD PRIMARY KEY (company_bank_account_seq);

ALTER TABLE pspadm.psp_company_consent
ADD PRIMARY KEY (company_consent_seq);

ALTER TABLE pspadm.psp_company_daily_liability
ADD PRIMARY KEY (company_daily_liability_seq);

ALTER TABLE pspadm.psp_company_event
ADD PRIMARY KEY (company_fk, company_event_seq);


ALTER TABLE pspadm.psp_company_event_detail
ADD PRIMARY KEY (company_fk, company_event_detail_seq);

ALTER TABLE pspadm.psp_company_event_email
ADD CONSTRAINT c_psp_company_event_email0 CHECK (status_cd IN ('SendFailedInvalidEmailId', 'SendSkippedInvalidEmailId', 'Pending', 'Sent', 'Ignore', 'Resend', 'SendFailed', 'GroupIncomplete', 'FormatError', 'PendingResend')) ;

ALTER TABLE pspadm.psp_company_event_email
ADD CONSTRAINT c_psp_company_event_email1 CHECK (email_template_type_cd IN ('AdditionalMedicareTaxDebitNotification', 'VendorInvalidEmail', 'EmployerNOC52LoanAccount', 'SameDayMoFedAssessmentDebit', 'SUICreditNotification', 'SymphonyWelcomeOneMonthReactivation', 'SameDayNVBondDebitNotification', 'BulkCreditDebitNotification', 'RTBAutomationCleanUp', 'ServiceCancelledConfirmation1', 'AssistedFailedEnrollment', 'BulkCreditDebitNotificationSUPNY', 'AssistedPayrollConfirmation', 'AllPaycheckReversalsFailed', 'AllPaycheckReversalsSuccessful', 'AutoRedebit', 'AutoRedebitFourStrikes', 'BankVerificationFailed', 'BilledNonPayrollRelatedFee', 'CustomerInitiatedDDReversal', 'DDBankVerificationReminder', 'DDBankVerificationSuccessful', 'DDERBankAccountChange', 'DDPINChangeConfirmation', 'DDServiceCancelledConfirmation', 'DDSignupConfirmation', 'ERandEENOC2', 'LastChanceEmail', 'ManualRedebit', 'NonACHPaymentReceivedInFull', 'NonACHPaymentReceivedInFullActionRequired', 'NonACHPaymentReceivedLiabilityOutstanding', 'PartialPaycheckReversal', 'PayrollCancellationNotification', 'DebitReturned', 'DebitReturnedFourStrikes', 'EEDDREJECT', 'EmailChangeNotification', 'EmployeeNOC', 'EmployeeNOC2', 'EmployerNOC', 'ERandEENOC', 'PayrollCancelledNotification', 'QBDTPayrollConfirmation', 'RedebitFailed', 'RefundedFeeAmount', 'RefundWithRebillFeeAmount', 'WireExpectedNotification', 'EFTPSEnrollmentRejectedEIN', 'EFTPSEnrollmentRejectedName', 'BankVerifyAttemptFailed', 'DDEEBankAccountChange', 'TOKFraudNotification', 'TOKVoidDelete', 'VendorPaymentSignupConfirmation', 'VendorPaymentReceived', 'VendorPaymentOffloaded1', 'ManualRedebit2', 'DebitReturnedFourStrikes3', 'WireExpectedNotification3', 'DebitReturned3', 'AutoRedebit2', 'LastChanceEmail3', 'PayrollCancellationNotification2', 'PayrollCancelledNotification2', 'NonPrintChecks', 'Correct401kEmployeeInfo', 'Correct401kEmployeeInfoAfterSend', 'VendorPaymentReceived1', 'VendorPaymentOffloadedForWriteChecks', 'VendorPaymentOffloadedForPayBills', 'SKDiskDeliveryKey1', 'SKBasicKey1', 'SKFreeBasicKey1', 'SKEnhancedKey1', 'SKEnhancedKeyAccount1', 'SKStandardKey1', 'SKDefaultKey1', 'SameDaySUIDebitNotification3', 'SUIRefundNotification3', 'EndofQuarterSUIDebitNotification3', 'DDERBankAccountChangeAssisted', 'EmployeeNOCAssisted', 'EmployerNOC1', 'LastChanceEmail1', 'LastChanceEmail4', 'ManualRedebit3', 'NonACHPaymentReceivedInFull1', 'NonACHPMTReceivedLiabOutstanding1', 'PartialPaycheckReversal1', 'RedebitFailed1', 'WireExpectedNotification4', 'AllPaycheckReversalsFailed1', 'AllPaycheckReversalsSuccessful1', 'AutoRedebit3', 'BankVerificationFailed1', 'BilledNonPayrollRelatedFee1', 'CustomerInitiatedDDReversal1', 'DDBankVerificationReminder1', 'DDPINChangeConfirmation1', 'DebitReturned1', 'DebitReturned4', 'EEDDREJECT1', 'RefundedFeeAmount1', 'RefundWithRebillFeeAmount1', 'DDEEBankAccountChange1', 'SymphonyWelcomeNoTrial', 'SymphonyBillingDetailsMonthly', 'UsageBillingMidTrial', 'SymphonyWelcomeFreeTrial', 'CreditReductionGeneric', 'CreditReductionFUTA', 'SymphonyBillingDetailsAnnual', 'FUTACreditReduction', 'BilledNonPayrollRelatedFee2', 'SUIRefundNotification4', 'SameDaySUIDebitNotification4', 'EndofQuarterSUIDebitNotification4', 'VmpEmployeeWelcome', 'VmpEmployerWelcome', 'VmpPaystubNotification', 'SameDayMAUHIDebitNotification', 'MinimumMonthlyBilling', 'DesktopAMLHoldRemoved', 'DesktopAMLHoldApplied', 'DDPayeeBankAccountChange', 'NewPayrollAccountAddedToEntitlement', 'QBDTPayrollConfirmationMTL', 'VendorPaymentReceived1MTL', 'SKAssistedKey1')) ;

ALTER TABLE pspadm.psp_company_event_email
ADD PRIMARY KEY (company_event_email_seq);

ALTER TABLE pspadm.psp_company_event_email_param
ADD PRIMARY KEY (company_fk, company_event_email_param_seq);

ALTER TABLE pspadm.psp_company_filing_amount
ADD PRIMARY KEY (company_filing_amount_seq);

ALTER TABLE pspadm.psp_company_law
ADD CONSTRAINT c_psp_company_law1 CHECK (status IN ('Active', 'Inactive')) ;

ALTER TABLE pspadm.psp_company_law
ADD CONSTRAINT c_psp_company_law2 CHECK (filing_status IN ('Active', 'Inactive')) ;

ALTER TABLE pspadm.psp_company_law
ADD CONSTRAINT c_psp_company_law3 CHECK (reimbursable_status IN ('Reimbursable', 'NotReimbursable')) ;

ALTER TABLE pspadm.psp_company_law
ADD PRIMARY KEY (company_law_seq);

ALTER TABLE pspadm.psp_company_law_rate
ADD CONSTRAINT c_psp_company_law_rate0 CHECK (rate_type IN ('MoneyType', 'Percentage'));

ALTER TABLE pspadm.psp_company_law_rate
ADD PRIMARY KEY (company_law_rate_seq);

ALTER TABLE pspadm.psp_company_note
ADD PRIMARY KEY (company_note_seq);

ALTER TABLE pspadm.psp_company_offer
ADD PRIMARY KEY (company_offer_seq);

ALTER TABLE pspadm.psp_company_offering
ADD PRIMARY KEY (company_offering_seq);

ALTER TABLE pspadm.psp_company_paycheck_batch
ADD PRIMARY KEY (company_paycheck_batch_seq);

ALTER TABLE pspadm.psp_company_payroll_item
ADD CONSTRAINT c_psp_company_payroll_item0 CHECK (status IN ('Active', 'Inactive')) ;

ALTER TABLE pspadm.psp_company_payroll_item
ADD PRIMARY KEY (company_payroll_item_seq);

ALTER TABLE pspadm.psp_company_pin
ADD CONSTRAINT c_psp_company_pin0 CHECK (hash_type IN ('SHA256', 'SHA', 'SHA512', 'AS400')) ;

ALTER TABLE pspadm.psp_company_pin
ADD PRIMARY KEY (company_pin_seq);

ALTER TABLE pspadm.psp_company_rate_request
ADD CONSTRAINT c_psp_company_rate_request0 CHECK (status IN ('Waiting', 'Applied', 'NoChange', 'Error')) ;

ALTER TABLE pspadm.psp_company_rate_request
ADD PRIMARY KEY (company_rate_request_seq);

ALTER TABLE pspadm.psp_company_service
ADD CONSTRAINT c_psp_company_service0 CHECK (status_cd IN ('AchRejectOther', 'AchRejectR1R9', 'ActiveCurrent', 'ActiveSeasonal', 'Cancelled', 'DirectDepositLimit', 'Fraud', 'AuditCorrections', 'FraudReview', 'IntuitCollections', 'MissingPaperwork', 'PendingBalanceFile', 'PendingBankVerification', 'PendingFirstPayroll', 'PendingPinCreation', 'PendingTermination', 'RiskAssessment', 'RiskCollections', 'SuspendedDirectDeposit', 'Terminated', 'AMLHold', 'PendingTaxAcceptance', 'PendingEnrollment', 'PendingPrefundingWire', 'AS400Hold', 'AS400DirectDepositLimitHold', 'PendingSetup', 'BillPaymentLimit', 'MTLHold')) ;

ALTER TABLE pspadm.psp_company_service
ADD PRIMARY KEY (company_service_seq);

ALTER TABLE pspadm.psp_company_service_bank_acct
ADD PRIMARY KEY (company_service_bank_acct_seq);

ALTER TABLE pspadm.psp_company_tfssubmission
ADD CONSTRAINT c_psp_company_tfssubmission0 CHECK (submission_status IN ('Submitted', 'Error', 'Pending')) ;

ALTER TABLE pspadm.psp_company_tfssubmission
ADD PRIMARY KEY (company_tfssubmission_seq);

ALTER TABLE pspadm.psp_company_usage
ADD CONSTRAINT c_psp_company_usage0 CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) ;

ALTER TABLE pspadm.psp_company_usage
ADD PRIMARY KEY (company_usage_seq);

ALTER TABLE pspadm.psp_companyagency_frmtemplate
ADD PRIMARY KEY (companyagency_frmtemplate_seq);

ALTER TABLE pspadm.psp_companyagency_pmttemplate
ADD PRIMARY KEY (companyagency_pmttemplate_seq);

ALTER TABLE pspadm.psp_compensation
ADD PRIMARY KEY (company_fk, compensation_seq);

ALTER TABLE pspadm.psp_contact
ADD CONSTRAINT c_psp_contact0 CHECK (contact_role_cd IN ('PayrollAdmin', 'Other', 'PrimaryPrincipal', 'SecondaryPrincipal'));

ALTER TABLE pspadm.psp_contact
ADD PRIMARY KEY (contact_seq);

ALTER TABLE pspadm.psp_ddcompany_service_info
ADD PRIMARY KEY (ddcompany_service_info_seq);

ALTER TABLE pspadm.psp_deduction
ADD PRIMARY KEY (company_fk, deduction_seq);

ALTER TABLE pspadm.psp_deleted_record
ADD PRIMARY KEY (deleted_record_seq);

ALTER TABLE pspadm.psp_dep_freq_ledger_operation
ADD PRIMARY KEY (dep_freq_ledger_operation_seq);

ALTER TABLE pspadm.psp_deposit_frequency
ADD CONSTRAINT c_psp_deposit_frequency0 CHECK (deposit_frequency_code IN ('ACCELERATED', 'ANNUAL', 'EARLYFILER', 'EIGHTHMONTHLY', 'FIVEBANKINGDAY', 'MONTHLY', 'MONTHLYACCELERATED', 'NEXTBANKINGDAY', 'QUADMONTHLY', 'QUARTERLY', 'QUARTERMONTHLY', 'SEMIANNUAL', 'SEMIMONTHLY', 'SEMIWEEKLY', 'SPLITMONTHLY', 'THREEBANKINGDAY', 'TWICEMONTHLY', 'NOCALC', 'WEEKLY')) ;

ALTER TABLE pspadm.psp_deposit_frequency
ADD PRIMARY KEY (deposit_frequency_code);

ALTER TABLE pspadm.psp_deposit_frequency_file
ADD CONSTRAINT c_psp_deposit_frequency_fi0 CHECK (status IN ('Processed', 'Received', 'Skipped')) ;

ALTER TABLE pspadm.psp_deposit_frequency_file
ADD PRIMARY KEY (deposit_frequency_file_seq);

ALTER TABLE pspadm.psp_deposit_frequency_file_rec
ADD CONSTRAINT c_psp_deposit_frequency_fi1 CHECK (status IN ('Error', 'Processed', 'Received', 'SkippedCompanyDoesNotExist', 'InvalidData', 'SkippedUpdating')) ;

ALTER TABLE pspadm.psp_deposit_frequency_file_rec
ADD PRIMARY KEY (deposit_frequency_file_rec_seq);

ALTER TABLE pspadm.psp_deposit_frequency_req
ADD CONSTRAINT c_psp_deposit_frequency_req0 CHECK (prohibited_deposit_frequency IN ('ACCELERATED', 'ANNUAL', 'EARLYFILER', 'EIGHTHMONTHLY', 'FIVEBANKINGDAY', 'MONTHLY', 'MONTHLYACCELERATED', 'NEXTBANKINGDAY', 'QUADMONTHLY', 'QUARTERLY', 'QUARTERMONTHLY', 'SEMIANNUAL', 'SEMIMONTHLY', 'SEMIWEEKLY', 'SPLITMONTHLY', 'THREEBANKINGDAY', 'TWICEMONTHLY', 'NOCALC', 'WEEKLY')) ;

ALTER TABLE pspadm.psp_deposit_frequency_req
ADD PRIMARY KEY (deposit_frequency_req_seq);

ALTER TABLE pspadm.psp_dicrfile
ADD CONSTRAINT c_psp_dicrfile0 CHECK (status IN ('Processed', 'Archived'));

ALTER TABLE pspadm.psp_dicrfile
ADD PRIMARY KEY (dicrfile_seq);

ALTER TABLE pspadm.psp_disburse_advice
ADD PRIMARY KEY (disburse_advice_seq);

ALTER TABLE pspadm.psp_disburse_advice_tax_liab
ADD PRIMARY KEY (company_fk, disburse_advice_tax_liab_seq);

ALTER TABLE pspadm.psp_edi_payment_detail
ADD CONSTRAINT c_psp_edi_payment_detail0 CHECK (status_cd IN ('Ignore', 'RejectedByAgency', 'SentToAgency', 'AcknowledgedByAgency', 'ReturnedTaxNotPaid', 'ReturnedTaxPaid', 'ReadyToSend', 'OnHold', 'None', 'ATFFinalized')) ;

ALTER TABLE pspadm.psp_edi_payment_detail
ADD PRIMARY KEY (edi_payment_detail_seq);

ALTER TABLE pspadm.psp_edi_tax_file
ADD CONSTRAINT c_psp_edi_tax_file0 CHECK (file_type IN ('StateEdiPayment', 'StateEdiPaymentAck', 'StateEdiPaymentResponse', 'EftpsEnrollmentResponse', 'EftpsForecast', 'EftpsPayment', 'EftpsPaymentResponse', 'EftpsPaymentConfirmation', 'EftpsPaymentReturn', 'EftpsEnrollment', 'EftpsEnrollmentAck', 'EftpsEnrollmentResponseAck', 'EftpsForecastAck', 'EftpsPaymentAck', 'EftpsPaymentConfirmationAck', 'EftpsPaymentResponseAck', 'EftpsPaymentReturnAck')) ;

ALTER TABLE pspadm.psp_edi_tax_file
ADD CONSTRAINT c_psp_edi_tax_file1 CHECK (status_cd IN ('InProcess', 'PendingTransmission', 'Completed', 'Error', 'Archived', 'SendToAS400')) ;

ALTER TABLE pspadm.psp_edi_tax_file
ADD CONSTRAINT c_psp_edi_tax_file2 CHECK (system_owner IN ('PSP', 'AS400')) ;

ALTER TABLE pspadm.psp_edi_tax_file
ADD PRIMARY KEY (edi_tax_file_seq);

ALTER TABLE pspadm.psp_ee_payrollitem_qtrtotals
ADD PRIMARY KEY (ee_payrollitem_qtrtotals_seq);

ALTER TABLE pspadm.psp_effective_deposit_freq
ADD PRIMARY KEY (effective_deposit_freq_seq);

ALTER TABLE pspadm.psp_eftps_enrollment
ADD CONSTRAINT c_psp_eftps_enrollment0 CHECK (status_cd IN ('PendingEnrollment', 'Cancelled', 'AgedOut', 'PendingAcceptance', 'Enrolled', 'Rejected', 'Invalid', 'None')) ;

ALTER TABLE pspadm.psp_eftps_enrollment
ADD PRIMARY KEY (eftps_enrollment_seq);

ALTER TABLE pspadm.psp_eftps_enrollment_detail
ADD CONSTRAINT c_psp_eftps_enrollment_det0 CHECK (status_cd IN ('PendingEnrollment', 'Cancelled', 'AgedOut', 'PendingAcceptance', 'Enrolled', 'Rejected', 'Invalid', 'None')) ;

ALTER TABLE pspadm.psp_eftps_enrollment_detail
ADD PRIMARY KEY (eftps_enrollment_detail_seq);

ALTER TABLE pspadm.psp_eftps_file
ADD CONSTRAINT c_psp_eftps_file0 CHECK (file_subtype IN ('PaymentNextDay', 'PaymentSameDay', 'Payment100k', 'None')) ;

ALTER TABLE pspadm.psp_eftps_file
ADD PRIMARY KEY (eftps_file_seq);

ALTER TABLE pspadm.psp_eftps_payment_detail
ADD CONSTRAINT c_psp_eftps_payment_detail0 CHECK (return_cd IN ('R01', 'R02', 'R03', 'R04', 'R05', 'R06', 'R07', 'R08', 'R09', 'R20', 'R24', 'R28', 'R29', 'R10', 'R12', 'R13', 'R14', 'R15', 'R16', 'R18', 'R11', 'R17', 'R19', 'R21', 'R22', 'R23', 'R25', 'R26', 'R27', 'R30', 'R31', 'R32', 'R33', 'R34', 'R35', 'R36', 'R37', 'R38', 'R39', 'R40', 'R41', 'R42', 'R43', 'R44', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R51', 'R52', 'R53', 'C01', 'R61', 'R62', 'R63', 'C02', 'C03', 'C04', 'C05', 'C06', 'C07', 'R64', 'R65', 'R66', 'R67', 'R68', 'R69', 'R70', 'R71', 'R72', 'R73', 'R74', 'R75', 'R76', 'R80', 'R81', 'C08', 'C09', 'C10', 'R82', 'R83', 'R84', 'C11', 'C12', 'C13', 'C61', 'C62', 'C63', 'R99', 'C64', 'C65', 'C66', 'C67', 'C68', 'C69', 'C99')) ;

ALTER TABLE pspadm.psp_eftps_payment_detail
ADD CONSTRAINT c_psp_eftps_payment_detail1 CHECK (status_cd IN ('Ignore', 'RejectedByAgency', 'SentToAgency', 'AcknowledgedByAgency', 'ReturnedTaxNotPaid', 'ReturnedTaxPaid', 'ReadyToSend', 'OnHold', 'None', 'ATFFinalized')) ;

ALTER TABLE pspadm.psp_eftps_payment_detail
ADD PRIMARY KEY (eftps_payment_detail_seq);

ALTER TABLE pspadm.psp_emp_totals_payroll_run
ADD CONSTRAINT c_psp_emp_totals_payroll_r0 CHECK (status IN ('Pending', 'Processed')) ;

ALTER TABLE pspadm.psp_emp_totals_payroll_run
ADD PRIMARY KEY (emp_totals_payroll_run_seq);

ALTER TABLE pspadm.psp_employee
ADD CONSTRAINT c_psp_employee0 CHECK (status_cd IN ('Active', 'Inactive'));

ALTER TABLE pspadm.psp_employee
ADD CONSTRAINT c_psp_employee1 CHECK (pay_period IN ('Annually', 'SemiAnnually', 'Quarterly', 'Monthly', 'SemiMonthly', 'BiWeekly', 'Weekly', 'Daily')) ;

ALTER TABLE pspadm.psp_employee
ADD CONSTRAINT psp_emp_comp_fk_src_emp_id UNIQUE (company_fk, source_employee_id);

ALTER TABLE pspadm.psp_employee
ADD PRIMARY KEY (employee_seq);

ALTER TABLE pspadm.psp_employee_accrual
ADD CONSTRAINT c_psp_employee_accrual0 CHECK (accrual_period IN ('Hourly', 'Payroll', 'Yearly')) ;

ALTER TABLE pspadm.psp_employee_accrual
ADD CONSTRAINT c_psp_employee_accrual1 CHECK (accrual_type IN ('Sick', 'Vacation')) ;

ALTER TABLE pspadm.psp_employee_accrual
ADD PRIMARY KEY (employee_accrual_seq);

ALTER TABLE pspadm.psp_employee_bank_account
ADD CONSTRAINT c_psp_employee_bank_account0 CHECK (status_cd IN ('PendingVerification', 'Active', 'Inactive'));

ALTER TABLE pspadm.psp_employee_bank_account
ADD CONSTRAINT c_psp_employee_bank_account1 CHECK (amount_type IN ('MoneyType', 'Percentage')) ;

ALTER TABLE pspadm.psp_employee_bank_account
ADD PRIMARY KEY (employee_bank_account_seq);

ALTER TABLE pspadm.psp_employee_custom_field
ADD PRIMARY KEY (employee_custom_field_seq);

ALTER TABLE pspadm.psp_employee_law_qtr_totals
ADD PRIMARY KEY (employee_law_qtr_totals_seq);

ALTER TABLE pspadm.psp_employee_payroll_item
ADD CONSTRAINT c_psp_employee_payroll_item0 CHECK (type IN ('Adjustment', 'Wage')) ;

ALTER TABLE pspadm.psp_employee_payroll_item
ADD CONSTRAINT c_psp_employee_payroll_item1 CHECK (amount_type IN ('MoneyType', 'Percentage')) ;

ALTER TABLE pspadm.psp_employee_payroll_item
ADD CONSTRAINT c_psp_employee_payroll_item2 CHECK (limit_type IN ('MoneyType', 'Percentage')) ;

ALTER TABLE pspadm.psp_employee_payroll_item
ADD PRIMARY KEY (employee_payroll_item_seq);

ALTER TABLE pspadm.psp_employee_tax
ADD CONSTRAINT c_psp_employee_tax0 CHECK (tax_type IN ('SIT', 'FIT', 'FICA', 'MED', 'FUTA', 'SUI', 'SDI', 'Other')) ;

ALTER TABLE pspadm.psp_employee_tax
ADD CONSTRAINT c_psp_employee_tax1 CHECK (extra_withholding_type IN ('MoneyType', 'Percentage')) ;

ALTER TABLE pspadm.psp_employee_tax
ADD PRIMARY KEY (employee_tax_seq);

ALTER TABLE pspadm.psp_employee_usage
ADD PRIMARY KEY (employee_usage_seq);

ALTER TABLE pspadm.psp_employee_w2_totals
ADD PRIMARY KEY (employee_w2_totals_seq);

ALTER TABLE pspadm.psp_employee_wage_plan
ADD CONSTRAINT c_psp_employee_wage_plan0 CHECK (name IN ('WPC', 'GC', 'OC', 'FCC')) ;

ALTER TABLE pspadm.psp_employee_wage_plan
ADD CONSTRAINT c_psp_employee_wage_plan1 CHECK (wage_plan_domain IN ('WorkOrLiveState', 'WorkState')) ;

ALTER TABLE pspadm.psp_employee_wage_plan
ADD PRIMARY KEY (employee_wage_plan_seq);

ALTER TABLE pspadm.psp_employer_contribution
ADD PRIMARY KEY (employer_contribution_seq);

ALTER TABLE pspadm.psp_employer_preference
ADD PRIMARY KEY (employer_preference_seq);

ALTER TABLE pspadm.psp_entitlement
ADD CONSTRAINT c_psp_entitlement0 CHECK (entitlement_state IN ('Disabled', 'Enabled')) ;

ALTER TABLE pspadm.psp_entitlement
ADD CONSTRAINT c_psp_entitlement1 CHECK (payment_method_type IN ('EFT', 'CC', 'PAPERCHECK')) ;

ALTER TABLE pspadm.psp_entitlement
ADD CONSTRAINT c_psp_entitlement2 CHECK (order_source_cd IN ('Siebel', 'EStore', 'FallDM2011')) ;

ALTER TABLE pspadm.psp_entitlement
ADD PRIMARY KEY (entitlement_seq);

ALTER TABLE pspadm.psp_entitlement_code
ADD CONSTRAINT c_psp_entitlement_code0 CHECK (asset_type_cd IN ('WorkersComp', 'Payroll', 'Usage', 'Trial')) ;

ALTER TABLE pspadm.psp_entitlement_code
ADD CONSTRAINT c_psp_entitlement_code1 CHECK (asset_item_cd IN ('Assisted', 'AssistedAdvantage', 'DIY', 'DIYDiskDelivery', 'EmployeeOrganizer', 'EmploymentRegulation'));

ALTER TABLE pspadm.psp_entitlement_code
ADD CONSTRAINT c_psp_entitlement_code2 CHECK (edition_type IN ('EnhancedAccountantProAdvisor', 'Basic', 'Enhanced', 'EnhancedAccountant', 'Standard'));

ALTER TABLE pspadm.psp_entitlement_code
ADD CONSTRAINT c_psp_entitlement_code3 CHECK (number_of_employees_type IN ('ONE', 'UPTO3', 'UNLIMITED'));

ALTER TABLE pspadm.psp_entitlement_code
ADD CONSTRAINT c_psp_entitlement_code4 CHECK (billing_frequency_type IN ('Monthly', 'Annually'));

ALTER TABLE pspadm.psp_entitlement_code
ADD PRIMARY KEY (entitlement_code_seq);

ALTER TABLE pspadm.psp_entitlement_code_offering
ADD CONSTRAINT c_psp_entitlement_code_off0 CHECK (service_cd IN ('DirectDeposit', 'Tax', 'BillPayment', 'ThirdParty401k', 'CheckDistribution', 'Cloud', 'RiskAssessment', 'WorkersComp', 'ViewMyPaycheck', 'CloudV2', 'Guideline401k'));

ALTER TABLE pspadm.psp_entitlement_code_offering
ADD PRIMARY KEY (entitlement_code_offering_seq);

ALTER TABLE pspadm.psp_entitlement_message
ADD CONSTRAINT c_psp_entitlement_message0 CHECK (status IN ('SkippedEntitlementNotFound', 'SkippedOldTimestamp', 'New', 'Processed', 'Error')) ;

--ALTER TABLE pspadm.psp_entitlement_message
--ADD PRIMARY KEY (entitlement_message_seq);

ALTER TABLE pspadm.psp_entitlement_unit
ADD CONSTRAINT c_psp_entitlement_unit0 CHECK (entitlement_unit_status IN ('ActivationHold', 'DeactivationHold', 'Deactivated', 'Activated', 'PendingActivation', 'PendingDeactivation', 'PendingReactivation', 'ErrorDeactivating', 'ErrorActivating', 'Historic')) ;

ALTER TABLE pspadm.psp_entitlement_unit
ADD PRIMARY KEY (entitlement_unit_seq);

ALTER TABLE pspadm.psp_entity_change
ADD PRIMARY KEY (entity_change_seq);

ALTER TABLE pspadm.psp_entity_update_hist
ADD CONSTRAINT c_psp_entity_update0 CHECK (status IN ('Created', 'Published', 'Failed', 'IQFailed', 'IQPublished', 'InProgress')) ;

ALTER TABLE pspadm.psp_entity_update_hist
ADD CONSTRAINT c_psp_entity_update1 CHECK (event_type IN ('EntityCreate', 'EntityUpdate', 'EntityDelete'));

ALTER TABLE pspadm.psp_entity_update_hist
ADD PRIMARY KEY (entity_update_seq);

ALTER TABLE pspadm.psp_entry_detail_record
ADD CONSTRAINT sys_c008379 PRIMARY KEY (company_fk, entry_detail_record_seq);

ALTER TABLE pspadm.psp_event_as400_sync
ADD CONSTRAINT c_psp_event_as400_sync0 CHECK (status_cd IN ('Pending', 'Complete', 'Error', 'NoSend')) ;

ALTER TABLE pspadm.psp_event_as400_sync
ADD PRIMARY KEY (event_as400_sync_seq);

ALTER TABLE pspadm.psp_event_detail_type
ADD CONSTRAINT c_psp_event_detail_type0 CHECK (event_detail_type_cd IN ('EmailTemplateType', 'VendorInvalidEmail', 'OldAchAccountType', 'NewAchAccountType', 'OldCompanyBankAccountId', 'OldPayeeBankAccountNumber', 'ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'CancellationDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'FeeAmount', 'RefundedFeeBillingDetailId', 'StrikeReason', 'ReasonDescription', 'NoteText', 'FailureReason', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldServiceStatus', 'NewServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount', 'ExpectedToken', 'ReceivedToken', 'Timestamp', 'UniqueIdentifier', 'UserId', 'ServiceCode', 'ThresholdPeriodStartDate', 'AgencyId', 'EnrollmentType', 'PaymentEFTNumber', 'PaymentAcknowledgeNumber', 'MoneyMovementTransactionId', 'CompanyTIN', 'PaymentPeriodEndDate', 'NewPayeeBankAccountNumber', 'PaymentInitiationDate', 'PaymentDueDate', 'PaymentMethod', 'OldPayeeBankRoutingNumber', 'NewPayeeBankRoutingNumber', 'GenericEventDetail', 'NewEffectiveDate', 'NewDepositFrequency', 'OldEffectiveDate', 'OldDepositFrequency', 'PaymentTemplate', 'Percentage', 'EmployeeInvalidReason', 'SourceEmployeeId', 'MessageLevel', 'OldEmployeeBankAccountId', 'NewEmployeeBankAccountId', 'BillPaymentId', 'PayeeId', 'PayeeName', 'PayeeBankAccountId', 'PaycheckInvalidReason', 'OverrideRecipientEmailAddress', 'SourceSystemTransmissionInvalidReason', 'CompanyAgency', 'Law', 'NextPaycheckId', 'SourceCompanyId', 'OFXToken', 'NextEmployeeId', 'NextPaylineTransactionId', 'NextPayrollTransactionId', 'Amount', 'Description', 'EntitlementId', 'EntitlementUnitId', 'InvalidatedDepositFrequencyId', 'TransactionType', 'PermanentPaymentFrequencyId', 'ThresholdPeriodEndDate', 'ThresholdReversed', 'PenaltiesRefundAmount', 'InterestRefundAmount', 'TotalRefundAmount', 'RefundDebitAmount', 'ACHEnrollmentId', 'RecipientEmailAddress', 'CaseId', 'FirstPayrollRunDate', 'PayrollCount', 'GrantType', 'WorkflowId', 'AuthId', 'BillingRealmId', 'DataRealmId', 'WorkOrderId', 'EmployeeSequence', 'WorkOrderCreatedTime', 'ServiceKey', 'CompanySequence', 'CompanyName', 'AppName', 'ConsentValue', 'EmailTemplate', 'InvitationSource', 'IUSInvitationId', 'PersonaId', 'NewWalletId', 'OldWalletId', 'CompanyRealmId', 'OldCompanyRealmId', 'ConsumerRealmId', 'OwnerOldLimit', 'OwnerNewLimit', 'PayeeOldLimit', 'PayeeNewLimit', 'VendorId')) ;

ALTER TABLE pspadm.psp_event_detail_type
ADD PRIMARY KEY (event_detail_type_cd);

ALTER TABLE pspadm.psp_event_log
ADD CONSTRAINT c_psp_event_log0 CHECK (event_log_type_cd IN ('Error', 'Debug', 'Warn', 'Info', 'Fatal', 'Statistic')) ;

ALTER TABLE pspadm.psp_event_log
ADD PRIMARY KEY (event_log_seq);

ALTER TABLE pspadm.psp_event_type
ADD CONSTRAINT c_psp_event_type0 CHECK (event_type_cd IN ('FileIdChanged', 'SendEmailFailed', 'PaycheckRecalledAfterOffload', 'SendEmailSkipped', 'InvalidVendorEmail', 'ERLoanNOC', 'NewPSIDCreatedForExistingCustomer', 'PrimaryPrincipalNameChanged', 'PayeeBankAccountChange', 'BackdatePriorToProcessingStart', 'SubscriptionEndDateChanged', 'SUICreditsApplied', 'TrialAssetDetected', 'WelcomeEmail', 'AccountLocked', 'ACHReturnStatusChanged', 'EINChanged', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BackdatedPayrollReceived', 'ACHReturn', 'FeeCreated', 'IncorrectPIN', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollRejected', 'CompanyContactEmailChanged', 'TaxExemptStatusChanged', 'CustomerSignedUp', 'PayrollCancelPending', 'FeeRefunded', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'DBANameChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'CompanyIndustryTypeChanged', 'PrimaryPrincipalSSNChanged', 'PrimaryPrincipalDOBChanged', 'EnrollmentStatusChanged', 'StateIdModified', 'TaxPaymentStatusChanged', 'PaymentMethodChanged', 'DepositFrequencyChanged', 'ThresholdExceeded', 'AssistedEmployeeMigrationComplete', 'OfferingUpdated', 'OfferClaimed', 'OfferRemoved', 'EmployeeInTermedCompany', 'EmployeeBankAccountInTermedCompany', 'PrefundingReceived', 'EmployeePaidTooManyTimes', 'EmployeePaidPercentageGreaterThanOthers', 'EmployeesPaidToSameBank', 'EmployeeBankAccountChangedSpikeInPay', 'EmployeesPaidToSameBankAccount', 'InvalidEmployeeInformation', 'PINUnlocked', 'Employee401kDataUploaded', 'EmployeeBankAccountChange', 'TotalBillPaymentExceedsLimit', 'PayeePaidGreaterThanMax', 'PayeePaidTooManyTimes', 'BillPaymentReceived', 'InvalidPaycheckInformation', 'VoidedPaycheckAlreadyOffloadedToTOK', 'DeletedPaycheckAlreadyOffloadedToTOK', 'TOKNotifiedOfCompanyFraud', 'BillPaymentOffloaded', 'PreOffload401kValidationAlert', 'BillPaymentRecalled', 'NonPrintChecks', 'PayrollReceivedCloud', 'PostOffload401kValidationAlert', 'InvalidSourceSystemTransmissionInformation', 'CloudResponse', 'InactivityDDPayrollAmountExceeded', 'InactivityBPPayrollAmountExceeded', 'AssistedPayrollItemMigrationComplete', 'ERPayableRefundCreated', 'MultipleCompanyLawsCreated', 'BalanceFileReceived', 'CompanyLawUpdated', 'PayrollReceivedPayCard', 'LiabilityAdjustmentCreated', 'PSPToAS400HoldSync', 'PSPToAS400HoldRemoveSync', 'OFXServiceActivated', 'PositiveCobraReceived', 'ManualLedgerEntry', 'PayrollTaxPaymentVoided', 'PayrollTaxPaymentReissued', 'ERPayableAppliedToBalanceDue', 'AIDUpdated', 'AssistedFailedEnrollment', 'AssistedPayrollConfirmation', 'EntitlementStateChanged', 'EntitlementUnitStatusChanged', 'SourceCompanyIdChanged', 'ServiceKeyUpdated', 'EntitlementCodeChanged', 'PriceTypeChanged', 'EntitlementCommunication', 'ManualDataSync', 'AccountingFinancialLedgerAdjustmentCreated', 'SUIEoqDebitCreated', 'SUIEoqCreditCreated', 'SUIImmediateDebitCreated', 'SUIImmediateCreditCreated', 'CompanyContactAdded', 'CompanyContactJobTitleChanged', 'PayrollSubmissionIncludedAllNewEmployees', 'PSIDMismatch', 'CompanyContactDeleted', 'CompanyContactNameChanged', 'CompanyContactFaxChanged', 'ERPenaltiesAndInterestRefundCreated', 'ERPenaltiesAndInterestRefundDebitCreated', 'EntitlementUnitAdded', 'UsageBilling25DaysIntoSubscription', 'UsageBilling15DaysIntoSubscription', 'CreditReduction', 'RequestProcessingFlagChanged', 'PayrollSubmittedWithEmployeeWithPendingReturn', 'DuplicatePayrollItemReceived', 'VmpSignUpEmployeeEmail', 'VmpSignUpEmployerEmail', 'ACHEnrollmentStatusChanged', 'PaystubCreated', 'AdditionalFilingAmount', 'BPIncreasePayrollLimit', 'AllowTransmissionsFlagChanged', 'MonthlyFeeCreated', 'EmployeeAdded', 'EmployeeDeleted', 'EmployeeUpdated', 'PayeeAdded', 'PayeeUpdated', 'DDMigration', 'SplitMMTReturn', 'RealmIdAdded', 'RealmIdUpdated', 'BillingRealmCreated', 'BillingRealmAttached', 'DataRealmAttached', 'GrantAttached', 'DGDeleteRequest', 'SMSToPSPSyncFailure', 'SMSToPSPSyncSuccess', 'CompanyBankAccountVBDStatusChange', 'ComplianceAddressChanged', 'DGAccessRequest', 'AutoEnabledVMP', 'ConsentChange', 'EmployeeInvited', 'EmployeeSignedUp', 'EmployeeBankAccountWalletSuccess', 'EmployeeBankAccountWalletFailure', 'CloneEmployeeWalletOnRealmChangeSuccess', 'CloneEmployeeWalletOnRealmChangeFailure', 'AddUserToRealmSuccess', 'AddUserToRealmFailure', 'AddUserToRealmOnRealmChangeSuccess', 'AddUserToRealmOnRealmChangeFailure', 'RiskProfileMigrated', 'RiskProfileUnMigrated', 'PSPToSMSMigration', 'PSPToSMSMigrationRevert', 'SMSRealmIdUpdated', 'UpdateConsumerRealmId', 'VendorBankAccountWalletFailure', 'VendorBankAccountWalletSuccess', 'CloneVendorWalletOnRealmChangeFailure', 'CloneVendorWalletOnRealmChangeSuccess')) ;

ALTER TABLE pspadm.psp_event_type
ADD CONSTRAINT c_psp_event_type1 CHECK (event_group_cd IN ('Agent', 'Bank', 'CompanyInfo', 'FinancialOps', 'Fraud', 'NonPSP', 'PayrollStatus', 'PSP'));

ALTER TABLE pspadm.psp_event_type
ADD PRIMARY KEY (event_type_cd);

ALTER TABLE pspadm.psp_evttp_srcsys_assoc
ADD PRIMARY KEY (interesting_event_types_fk, source_system_fk);

ALTER TABLE pspadm.psp_failed_payroll_run
ADD CONSTRAINT c_psp_failed_payroll_run0 CHECK (status_token IN ('Pending', 'Complete', 'Error', 'NoSend')) ;

ALTER TABLE pspadm.psp_failed_payroll_run
ADD PRIMARY KEY (failed_payroll_run_seq);

ALTER TABLE pspadm.psp_fee
ADD CONSTRAINT c_psp_fee0 CHECK (fee_cd IN ('ReverseFee', 'NSFFee', 'CopyFee', 'FeeOnlyNSFFee')) ;

ALTER TABLE pspadm.psp_fee
ADD PRIMARY KEY (fee_seq);

ALTER TABLE pspadm.psp_financial_trans_state
ADD CONSTRAINT sys_c008683 PRIMARY KEY (company_fk, financial_trans_state_seq);

ALTER TABLE pspadm.psp_financial_transaction
ADD CONSTRAINT sys_c008675 PRIMARY KEY (company_fk, financial_transaction_seq);


ALTER TABLE pspadm.psp_financial_txn_action
ADD PRIMARY KEY (financial_txn_action_seq);

ALTER TABLE pspadm.psp_fintxn_onholdreason_assoc
ADD PRIMARY KEY (financial_transaction_fk, on_hold_reason_fk);

ALTER TABLE pspadm.psp_forecast
ADD CONSTRAINT c_psp_forecast0 CHECK (status IN ('Open', 'Closed', 'Error'));

ALTER TABLE pspadm.psp_forecast
ADD PRIMARY KEY (forecast_seq);

ALTER TABLE pspadm.psp_forecast_detail
ADD PRIMARY KEY (forecast_detail_seq);

ALTER TABLE pspadm.psp_form_template
ADD PRIMARY KEY (form_template_cd);

ALTER TABLE pspadm.psp_fraud_address
ADD PRIMARY KEY (fraud_address_seq);

ALTER TABLE pspadm.psp_fraud_bank_account
ADD CONSTRAINT c_psp_fraud_bank_account0 CHECK (account_type_cd IN ('Checking', 'Savings')) ;

ALTER TABLE pspadm.psp_fraud_bank_account
ADD CONSTRAINT c_psp_fraud_bank_account1 CHECK (fraud_bank_account_reason IN ('EmployeeBankAccountOfTerminatedCompany', 'EmployerBankAccountOfTerminatedCompany')) ;

ALTER TABLE pspadm.psp_fraud_bank_account
ADD PRIMARY KEY (fraud_bank_account_seq);

ALTER TABLE pspadm.psp_fraud_company
ADD PRIMARY KEY (fraud_company_seq);

ALTER TABLE pspadm.psp_fraud_contact
ADD PRIMARY KEY (fraud_contact_seq);

ALTER TABLE pspadm.psp_fraud_event
ADD CONSTRAINT c_psp_fraud_event0 CHECK (event_status_cd IN ('Active', 'Inactive')) ;

ALTER TABLE pspadm.psp_fraud_event
ADD CONSTRAINT c_psp_fraud_event1 CHECK (event_type_cd IN ('FileIdChanged', 'SendEmailFailed', 'PaycheckRecalledAfterOffload', 'SendEmailSkipped', 'InvalidVendorEmail', 'ERLoanNOC', 'NewPSIDCreatedForExistingCustomer', 'PrimaryPrincipalNameChanged', 'PayeeBankAccountChange', 'BackdatePriorToProcessingStart', 'SubscriptionEndDateChanged', 'SUICreditsApplied', 'TrialAssetDetected', 'WelcomeEmail', 'AccountLocked', 'ACHReturnStatusChanged', 'EINChanged', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BackdatedPayrollReceived', 'ACHReturn', 'FeeCreated', 'IncorrectPIN', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollRejected', 'CompanyContactEmailChanged', 'TaxExemptStatusChanged', 'CustomerSignedUp', 'PayrollCancelPending', 'FeeRefunded', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'NonAchPaymentReceived', 'DBANameChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged', 'WireExpected', 'EmployeePaidEvenDollarAmount', 'NumberOfPayrollsPerDayExceeded', 'EmployeePaidGreaterThanMax', 'TotalPayrollExceedsLimit', 'CurrentPayrollPercentageIncrease', 'SingleEmployeePercentageIncrease', 'PayrollProcessedTooSoon', 'CompanyMatchesFraudulentCompany', 'FraudFlagRemovedEvent', 'SalesTaxReturn', 'FeeOffloaded', 'RedebitAmountUpdated', 'RedebitDateUpdated', 'AS400Event', 'PayrollRecalled', 'PaycheckRecalled', 'ChangeRedebitToWireExpected', 'FeeRebilled', 'ManualNoteEvent', 'PINReset', 'KeyPairGenerated', 'HigherTokenSynced', 'AuthenticationFailed', 'NOCWithOutChanges', 'CompanyIndustryTypeChanged', 'PrimaryPrincipalSSNChanged', 'PrimaryPrincipalDOBChanged', 'EnrollmentStatusChanged', 'StateIdModified', 'TaxPaymentStatusChanged', 'PaymentMethodChanged', 'DepositFrequencyChanged', 'ThresholdExceeded', 'AssistedEmployeeMigrationComplete', 'OfferingUpdated', 'OfferClaimed', 'OfferRemoved', 'EmployeeInTermedCompany', 'EmployeeBankAccountInTermedCompany', 'PrefundingReceived', 'EmployeePaidTooManyTimes', 'EmployeePaidPercentageGreaterThanOthers', 'EmployeesPaidToSameBank', 'EmployeeBankAccountChangedSpikeInPay', 'EmployeesPaidToSameBankAccount', 'InvalidEmployeeInformation', 'PINUnlocked', 'Employee401kDataUploaded', 'EmployeeBankAccountChange', 'TotalBillPaymentExceedsLimit', 'PayeePaidGreaterThanMax', 'PayeePaidTooManyTimes', 'BillPaymentReceived', 'InvalidPaycheckInformation', 'VoidedPaycheckAlreadyOffloadedToTOK', 'DeletedPaycheckAlreadyOffloadedToTOK', 'TOKNotifiedOfCompanyFraud', 'BillPaymentOffloaded', 'PreOffload401kValidationAlert', 'BillPaymentRecalled', 'NonPrintChecks', 'PayrollReceivedCloud', 'PostOffload401kValidationAlert', 'InvalidSourceSystemTransmissionInformation', 'CloudResponse', 'InactivityDDPayrollAmountExceeded', 'InactivityBPPayrollAmountExceeded', 'AssistedPayrollItemMigrationComplete', 'ERPayableRefundCreated', 'MultipleCompanyLawsCreated', 'BalanceFileReceived', 'CompanyLawUpdated', 'PayrollReceivedPayCard', 'LiabilityAdjustmentCreated', 'PSPToAS400HoldSync', 'PSPToAS400HoldRemoveSync', 'OFXServiceActivated', 'PositiveCobraReceived', 'ManualLedgerEntry', 'PayrollTaxPaymentVoided', 'PayrollTaxPaymentReissued', 'ERPayableAppliedToBalanceDue', 'AIDUpdated', 'AssistedFailedEnrollment', 'AssistedPayrollConfirmation', 'EntitlementStateChanged', 'EntitlementUnitStatusChanged', 'SourceCompanyIdChanged', 'ServiceKeyUpdated', 'EntitlementCodeChanged', 'PriceTypeChanged', 'EntitlementCommunication', 'ManualDataSync', 'AccountingFinancialLedgerAdjustmentCreated', 'SUIEoqDebitCreated', 'SUIEoqCreditCreated', 'SUIImmediateDebitCreated', 'SUIImmediateCreditCreated', 'CompanyContactAdded', 'CompanyContactJobTitleChanged', 'PayrollSubmissionIncludedAllNewEmployees', 'PSIDMismatch', 'CompanyContactDeleted', 'CompanyContactNameChanged', 'CompanyContactFaxChanged', 'ERPenaltiesAndInterestRefundCreated', 'ERPenaltiesAndInterestRefundDebitCreated', 'EntitlementUnitAdded', 'UsageBilling25DaysIntoSubscription', 'UsageBilling15DaysIntoSubscription', 'CreditReduction', 'RequestProcessingFlagChanged', 'PayrollSubmittedWithEmployeeWithPendingReturn', 'DuplicatePayrollItemReceived', 'VmpSignUpEmployeeEmail', 'VmpSignUpEmployerEmail', 'ACHEnrollmentStatusChanged', 'PaystubCreated', 'AdditionalFilingAmount', 'BPIncreasePayrollLimit', 'AllowTransmissionsFlagChanged', 'MonthlyFeeCreated', 'EmployeeAdded', 'EmployeeDeleted', 'EmployeeUpdated', 'PayeeAdded', 'PayeeUpdated', 'DDMigration', 'SplitMMTReturn', 'RealmIdAdded', 'RealmIdUpdated', 'BillingRealmCreated', 'BillingRealmAttached', 'DataRealmAttached', 'GrantAttached', 'DGDeleteRequest', 'SMSToPSPSyncFailure', 'SMSToPSPSyncSuccess', 'CompanyBankAccountVBDStatusChange', 'ComplianceAddressChanged', 'DGAccessRequest', 'AutoEnabledVMP', 'ConsentChange', 'EmployeeInvited', 'EmployeeSignedUp', 'EmployeeBankAccountWalletSuccess', 'EmployeeBankAccountWalletFailure', 'CloneEmployeeWalletOnRealmChangeSuccess', 'CloneEmployeeWalletOnRealmChangeFailure', 'AddUserToRealmSuccess', 'AddUserToRealmFailure', 'AddUserToRealmOnRealmChangeSuccess', 'AddUserToRealmOnRealmChangeFailure', 'RiskProfileMigrated', 'RiskProfileUnMigrated', 'PSPToSMSMigration', 'PSPToSMSMigrationRevert', 'SMSRealmIdUpdated', 'UpdateConsumerRealmId', 'VendorBankAccountWalletFailure', 'VendorBankAccountWalletSuccess', 'CloneVendorWalletOnRealmChangeFailure', 'CloneVendorWalletOnRealmChangeSuccess')) ;

ALTER TABLE pspadm.psp_fraud_event
ADD PRIMARY KEY (fraud_event_seq);

ALTER TABLE pspadm.psp_fraud_rule
ADD CONSTRAINT c_psp_fraud_rule0 CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) ;

ALTER TABLE pspadm.psp_fraud_rule
ADD PRIMARY KEY (fraud_rule_seq);

ALTER TABLE pspadm.psp_fraud_value
ADD CONSTRAINT c_psp_fraud_value0 CHECK (name IN ('FraudBPXPayrollAmount', 'FraudBPInactivityDays', 'FraudBPInactivityPayrollAmount', 'FraudBPMax', 'FraudBPMaxXPayrolls', 'FraudBPNumberOfDaysForXPayments', 'FraudBPNumberOfPaymentsInXDays', 'FraudDDInactivityDays', 'FraudDDInactivityPayrollAmount', 'FraudEENewEmployeeAddedXDays', 'FraudEENumberOfDaysBankAcctUpdated', 'FraudEENumberOfDaysMultiplePaychecks', 'FraudEENumberOfPaychecksSpikeInPay', 'FraudEEPaidMax', 'FraudPRMax', 'FraudPRMaxXPayrolls', 'FraudPRNumberOfDaysForXPayrolls', 'FraudPRNumberOfPayrollsInXDays', 'FraudPRNumberOfPayrollsToCheckSameBank', 'FraudPRPercentEmployeesPaidSameBank', 'FraudPRPercentIncreaseMax', 'FraudPRPercentIncreaseMaxXPayrolls', 'FraudEEPaidMaxXPayrolls', 'FraudEEPaidXTimes', 'FraudEEPercentGreaterThanAverage', 'FraudEEPercentGreaterThanOtherEEs', 'FraudEEPercentIncreaseMax', 'FraudEEPercentIncreaseMaxXPayrolls', 'FraudEERoundPaidXPayrolls', 'FraudPREmployeesSameBankAccountMax', 'FraudPRTotalEmployeesToCheckSameBank', 'FraudPayeeNumberOfDaysMultiplePayments', 'FraudPayeePaidMax', 'FraudPayeePaidMaxXPayrolls', 'FraudPayeePaidXTimes', 'FraudBPRoundPaidXPayrolls', 'FraudBPNumberOfPaymentsToCheckSameBank', 'FraudBPPercentPayeesPaidSameBank', 'FraudBPTotalPayeesToCheckSameBank', 'FraudEERoundPaidXAmount', 'FraudBPRoundPaidXAmount', 'FraudPRXPayrollAmount', 'FraudBPAcctUpdateMax', 'FraudBPAcctUpdateXDays', 'FraudEEAcctUpdateMax', 'FraudEEAcctUpdateXDays'));

ALTER TABLE pspadm.psp_fraud_value
ADD PRIMARY KEY (fraud_value_seq);

ALTER TABLE pspadm.psp_fset_file
ADD CONSTRAINT c_psp_fset_file0 CHECK (file_type IN ('FsetReturns', 'FsetAck')) ;

ALTER TABLE pspadm.psp_fset_file
ADD CONSTRAINT c_psp_fset_file1 CHECK (status_cd IN ('Archived', 'PendingTransmission', 'SentToAgency', 'Error', 'ReceivedByAgency', 'Completed')) ;

ALTER TABLE pspadm.psp_fset_file
ADD PRIMARY KEY (fset_file_seq);

ALTER TABLE pspadm.psp_fset_filing_detail
ADD CONSTRAINT c_psp_fset_filing_detail0 CHECK (status IN ('AcceptedByAgency', 'SentToAgency', 'RejectedByAgency')) ;

ALTER TABLE pspadm.psp_fset_filing_detail
ADD PRIMARY KEY (fset_filing_detail_seq);

ALTER TABLE pspadm.psp_funding_model
ADD PRIMARY KEY (funding_model_cd);

ALTER TABLE pspadm.psp_gems_ledger_posting_rule
ADD CONSTRAINT c_psp_gems_ledger_posting_0 CHECK (reporting_type IN ('Tax', 'DirectDeposit')) ;

ALTER TABLE pspadm.psp_gems_ledger_posting_rule
ADD PRIMARY KEY (gems_ledger_posting_rule_seq);

ALTER TABLE pspadm.psp_gems_monthly_balance
ADD PRIMARY KEY (gems_monthly_balance_seq);

ALTER TABLE pspadm.psp_gems_upload_batch
ADD CONSTRAINT c_psp_gems_upload_batch0 CHECK (batch_type IN ('Daily', 'Monthly'));

ALTER TABLE pspadm.psp_gems_upload_batch
ADD CONSTRAINT c_psp_gems_upload_batch1 CHECK (upload_status IN ('InProcess', 'Empty', 'Finalized', 'PendingTransmission', 'Transmitted', 'Archived', 'Superceded'));

ALTER TABLE pspadm.psp_gems_upload_batch
ADD PRIMARY KEY (gems_upload_batch_seq);

ALTER TABLE pspadm.psp_hours_worked_exception
ADD CONSTRAINT c_psp_hours_worked_excepti0 CHECK (pay_type IN ('REG', 'SICK', 'VAC')) ;

ALTER TABLE pspadm.psp_hours_worked_exception
ADD CONSTRAINT c_psp_hours_worked_excepti1 CHECK (payroll_item_cd IN ('Tp401kEmployeeDeferral', 'Tp401kEmployerMatch', 'Tp401kLoanPayment', 'Tp401kProfitSharing', 'Tp401kRoth', 'Tp401kSafeHarbor', 'Salary', 'Hourly', 'OtherPreTaxDeduction', 'Compensation', 'OtherPostTaxDeduction', 'OtherTaxableEmployerContribution', 'OtherNonTaxableEmployerContribution', 'Bonus', 'Commission', 'OtherAdditionPreTax', 'OtherAdditionPostTax', 'DirectDeposit')) ;

ALTER TABLE pspadm.psp_hours_worked_exception
ADD PRIMARY KEY (hours_worked_exception_id);

ALTER TABLE pspadm.psp_individual
ADD CONSTRAINT c_psp_individual0 CHECK (gender_cd IN ('Male', 'Female'));

ALTER TABLE pspadm.psp_individual
ADD CONSTRAINT c_psp_individual1 CHECK (communication_type_preference IN ('Phone', 'Email'));

ALTER TABLE pspadm.psp_individual
ADD PRIMARY KEY (individual_seq);

ALTER TABLE pspadm.psp_industry_type
ADD CONSTRAINT c_psp_industry_type1 CHECK (LENGTH(standard_industry_code) > 3) ;

ALTER TABLE pspadm.psp_industry_type
ADD PRIMARY KEY (industry_type_seq);

ALTER TABLE pspadm.psp_intuit_ba_bt_ft
ADD CONSTRAINT c_psp_intuit_ba_bt_ft0 CHECK (file_type IN ('CCD', 'PPD', 'CCDPlus')) ;

ALTER TABLE pspadm.psp_intuit_ba_bt_ft
ADD CONSTRAINT c_psp_intuit_ba_bt_ft1 CHECK (n_a_c_h_a_batch_type IN ('BookTransfer', 'Payroll', 'Reversal', 'RetryPayment', 'TaxPayment')) ;

ALTER TABLE pspadm.psp_intuit_ba_bt_ft
ADD PRIMARY KEY (intuit_ba_bt_ft_seq);

ALTER TABLE pspadm.psp_intuit_bank_acc_txn_type
ADD CONSTRAINT c_psp_intuit_bank_acc_txn_0 CHECK (credit_debit_ind IN ('Credit', 'Debit'));

ALTER TABLE pspadm.psp_intuit_bank_acc_txn_type
ADD PRIMARY KEY (intuit_bank_acc_txn_type_seq);

ALTER TABLE pspadm.psp_intuit_bank_account
ADD PRIMARY KEY (intuit_bank_account_seq);

ALTER TABLE pspadm.psp_intuit_shipper_info
ADD PRIMARY KEY (intuit_shipper_info_seq);

ALTER TABLE pspadm.psp_iopsync_company
ADD CONSTRAINT c_psp_iopsync_company1 CHECK (status IN ('Pending', 'InProcess', 'Failed', 'Synced'));

ALTER TABLE pspadm.psp_iopsync_company
ADD PRIMARY KEY (iopsync_company_seq);

ALTER TABLE pspadm.psp_law
ADD CONSTRAINT c_psp_law0 CHECK (law_category_code IN ('Withholding', 'SocialSecurityEmployee', 'SocialSecurityEmployer', 'Local', 'Supplemental', 'UnemploymentEmployer', 'WorkersCompensationEmployee', 'DisabilityEmployer', 'UnemploymentEmployee', 'MedicareEmployee', 'DisabilityEmployee', 'UnemploymentHealthInsurance', 'Unused', 'MedicareEmployer', 'TransitTax', 'PaidLeave')) ;

ALTER TABLE pspadm.psp_law
ADD PRIMARY KEY (law_id);

ALTER TABLE pspadm.psp_law_rate_range
ADD PRIMARY KEY (law_rate_range_id);

ALTER TABLE pspadm.psp_law_rate_value
ADD PRIMARY KEY (law_rate_value_id);

ALTER TABLE pspadm.psp_ledger_account
ADD CONSTRAINT c_psp_ledger_account0 CHECK (ledger_account_cd IN ('AgencyTaxRefund', 'DDFutureReceivable', 'DDFutureLiability', 'DDCurrentCash', 'DDCurrentLiability', 'ERReturnReceivable', 'ERReturnCash', 'SalesAndUseTax', 'EEReturnCash', 'EEReturnLiablility', 'FeeCashRevenue', 'FeeCashBalanceSheet', 'FeeIncome', 'BadDebt', 'TaxFutureReceivable', 'TaxFutureLiability', 'TaxCurrentCash', 'TaxCurrentLiability', 'ERPayable', 'ERLiabilityOffset', 'CollectionExpense', 'ERSUITaxDue', 'TaxInterestExpense', 'TaxPenaltiesExpense')) ;

ALTER TABLE pspadm.psp_ledger_account
ADD CONSTRAINT c_psp_ledger_account1 CHECK (balance_calculation_rule IN ('CreditAddsToBalance', 'DebitAddsToBalance'));

ALTER TABLE pspadm.psp_ledger_account
ADD CONSTRAINT c_psp_ledger_account2 CHECK (ledger_account_type IN ('SUTax', 'Income'));

ALTER TABLE pspadm.psp_ledger_account
ADD CONSTRAINT c_psp_ledger_account3 CHECK (reporting_frequency IN ('Daily', 'Monthly'));

ALTER TABLE pspadm.psp_ledger_account
ADD PRIMARY KEY (ledger_account_cd);

ALTER TABLE pspadm.psp_ledger_account_action
ADD CONSTRAINT c_psp_ledger_account_action0 CHECK (credit_debit_indicator IN ('Credit', 'Debit'));

ALTER TABLE pspadm.psp_ledger_account_action
ADD PRIMARY KEY (ledger_account_action_seq);

ALTER TABLE pspadm.psp_ledger_balance
ADD CONSTRAINT sys_c008432 PRIMARY KEY (company_fk, ledger_balance_seq);


ALTER TABLE pspadm.psp_ledger_operation
ADD CONSTRAINT c_psp_ledger_operation0 CHECK (source_system_code IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) ;

ALTER TABLE pspadm.psp_ledger_operation
ADD CONSTRAINT c_psp_ledger_operation1 CHECK (status IN ('Created', 'InProgress', 'Completed', 'Error'));

ALTER TABLE pspadm.psp_ledger_operation
ADD PRIMARY KEY (ledger_operation_seq);

ALTER TABLE pspadm.psp_ledger_operation_job
ADD CONSTRAINT c_psp_ledger_operation_job0 CHECK (status IN ('Deleted', 'Queued', 'Created', 'InProgress', 'Complete')) ;

ALTER TABLE pspadm.psp_ledger_operation_job
ADD CONSTRAINT c_psp_ledger_operation_job1 CHECK (job_type IN ('BulkDebit', 'TOR', 'DepositFrequencyUpdate', 'RateUpdate', 'AdditionalFilingAmountUpdate')) ;

ALTER TABLE pspadm.psp_ledger_operation_job
ADD PRIMARY KEY (ledger_operation_job_seq);

ALTER TABLE pspadm.psp_liab_check_billing_assoc
ADD PRIMARY KEY (liab_check_billing_assoc_seq);

ALTER TABLE pspadm.psp_liability_adjustment
ADD PRIMARY KEY (liability_adjustment_seq);

ALTER TABLE pspadm.psp_liability_check
ADD CONSTRAINT c_psp_liability_check0 CHECK (type IN ('EmployerFee', 'EmployerDebit', 'EFTPSDirectDebit', 'EmployerDDDebit', 'EmployerTaxFee')) ;

ALTER TABLE pspadm.psp_liability_check
ADD PRIMARY KEY (liability_check_seq);

ALTER TABLE pspadm.psp_liability_check_line
ADD PRIMARY KEY (liability_check_line_seq);

ALTER TABLE pspadm.psp_limit_rule
ADD CONSTRAINT c_psp_limit_rule0 CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) ;

ALTER TABLE pspadm.psp_limit_rule
ADD PRIMARY KEY (limit_rule_seq);

ALTER TABLE pspadm.psp_limit_value
ADD CONSTRAINT c_psp_limit_value0 CHECK (name IN ('MinPayrollRunsForLimitAutoIncrease', 'ConsecutiveLimitViolationLimit', 'CompanyBankAccountVerificationAttemptLimit', 'CompanyBankAccountDurationLimitForVerification', 'MinimumNonSuspectPayrollAmount', 'AutoLimitIncreaseMinPayrolls', 'AutoLimitIncreaseMinEarliestPayrollRunDays', 'AutoLimitIncreaseIncreaseMultiplier', 'AutoLimitIncreaseMaxCompanyLimit', 'AutoLimitIncreaseMaxEmployeeLimit', 'MaxCompanyLimitDefault', 'CompanyLimitDuration', 'EmployeeLimitDuration', 'DefaultCompanyLimit', 'DefaultEmployeeLimit')) ;

ALTER TABLE pspadm.psp_limit_value
ADD PRIMARY KEY (limit_value_seq);

ALTER TABLE pspadm.psp_manual_requirement
ADD PRIMARY KEY (manual_requirement_seq);

ALTER TABLE pspadm.psp_message_log
ADD CONSTRAINT c_psp_message_log0 CHECK (flow_type IN ('EIAM', 'SMS'));

ALTER TABLE pspadm.psp_message_log
ADD PRIMARY KEY (message_log_seq);

ALTER TABLE pspadm.psp_money_movement_transaction
ADD CONSTRAINT sys_c008717 PRIMARY KEY (company_fk, money_movement_transaction_seq);

ALTER TABLE pspadm.psp_nachafile
ADD CONSTRAINT c_psp_nachafile0 CHECK (status IN ('Archived', 'Acknowledged', 'PendingAcknowledgement', 'PendingTransmission', 'Finalized', 'InProcess', 'Transmitted')) ;

ALTER TABLE pspadm.psp_nachafile
ADD CONSTRAINT c_psp_nachafile1 CHECK (file_type IN ('CCD', 'PPD', 'CCDPlus')) ;

ALTER TABLE pspadm.psp_nachafile
ADD PRIMARY KEY (nachafile_seq);

ALTER TABLE pspadm.psp_offer
ADD CONSTRAINT c_psp_offer0 CHECK (discount_type IN ('AmountOff', 'PercentOff', 'AltPrice')) ;

ALTER TABLE pspadm.psp_offer
ADD CONSTRAINT c_psp_offer1 CHECK (begin_event IN ('SignupEvent', 'ActivationEvent', 'FirstUseEvent', 'RedemptionEvent'));

ALTER TABLE pspadm.psp_offer
ADD CONSTRAINT c_psp_offer2 CHECK (end_event IN ('DateEvent', 'DurationEvent', 'PayrollUsageEvent'));

ALTER TABLE pspadm.psp_offer
ADD CONSTRAINT c_psp_offer3 CHECK (offer_restriction IN ('Open', 'Restricted', 'SalesOps')) ;

ALTER TABLE pspadm.psp_offer
ADD PRIMARY KEY (offer_seq);

ALTER TABLE pspadm.psp_offer_price
ADD CONSTRAINT c_psp_offer_price0 CHECK (fee_type IN ('CourtesyRefund', 'PerPayroll', 'PerPaycheck', 'PerTransmission', 'AmendedSSN', 'PerPayment', 'ReversalFee', 'DebitReturnFee', 'ManualServicingFee', 'ChaseReportFeeUpTo3Payrolls', 'ChaseReportFeeUpTo6Payrolls', 'ChaseReportFeeUpTo15Payrolls', 'ChaseReportFeeUpTo20Payrolls', 'ChaseReportFeeOver20Payrolls', 'BankVerificationDebit', 'PaymentArrangementFee', 'PerBatch', 'CompanyUpdates', 'EmployeesAdded', 'EmployeesUpdated', 'BackdatedPayroll', 'DirectDepositFee', 'EmployeesPaid', 'MonthlyFee', 'Amendments', 'EntityChange', 'ExtraCopies', 'PayrollAdjustment', 'PenaltiesAndInterest', 'ExtraStateFee', 'OtherFee', 'W2Fee', 'EmployeeOrganizerFee', 'W2Correction', 'W2BaseFee', 'BankVerificationCredit')) ;

ALTER TABLE pspadm.psp_offer_price
ADD PRIMARY KEY (offer_price_seq);

ALTER TABLE pspadm.psp_offer_svcchg_assoc
ADD PRIMARY KEY (offer_fk, offering_service_charge_fk);

ALTER TABLE pspadm.psp_offering
ADD CONSTRAINT c_psp_offering1 CHECK (offering_code IN ('DIYDDYEAREND', 'DIYDDFY143', 'COSTCO69FY16', 'AP79FY16', 'AP79FY14', 'AP79MEFY14', 'AP89FY14', 'PAP75FY14', 'DIYDDFY14', 'SYM3FY14', 'BillPaymentSTD3FY14', 'SYMFY14', 'COSTCO54', 'COSTCO64', 'AP89FY16', 'COSTCO84', 'COSTCO74', 'COSTCO572', 'COSTCO672', 'BillPaymentSTDFY15', 'DIYDDFY15', 'DIYDDFY153', 'AP79FY15', 'AP89FY15', 'AP99FY15', 'AP79MEFY15', 'AP89MEFY15', 'PAP84FY15', 'COSTCO57FY15', 'COSTCO67FY15', 'COSTCO79FY16', 'AP79MEFY16', 'AP89MEFY16', 'AP99FY16', 'AP99MEFY16', 'BillPaymentSTDFY16', 'DIYDDFY16', 'DIYDDFY163', 'PAP84FY16', 'DIYDDSTD', 'DIYDDSTD3', 'QBOEDD', 'CheckDistribution', 'ThirdParty401k', 'BillPaymentSTD3', 'Tax', 'Cloud', 'AssistedBundle', 'RiskAssessment', 'AP69MEFY13', 'PAPAV1142', 'AP63EEEO', 'AP79FY13', 'MAJORACCT', 'APAV115', 'APAV125ME2', 'APAV1352', 'SUP125TEST', 'APDIOCESE', 'APPAP99YR', 'ASST60', 'ASSTAD2P3', 'ASSTEOSUP', 'COSTCO49', 'COSTCO59', 'PAP71FY13', 'PAP582', 'PAP58DD145', 'PAP58DD2', 'AP59ME2', 'AP69DD145', 'AP69DD1502', 'AP59MED145', 'AP692', 'AP69DD2', 'AP69W22', 'UsageBilling', 'SYM1FY13', 'SYM2FY13', 'WorkersComp', 'COSTCO57', 'COSTCO67', 'ViewMyPaycheck', 'PAP67FY13', 'COSTCO52', 'COSTCO62', 'BillPaymentSTD4', 'CloudV2', 'SYMPAPFY14', 'SYMPAP92FY18', 'SYMPAP87FY18', 'PAP92FY18', 'AP109FY18', 'SYM109FY18'));

ALTER TABLE pspadm.psp_offering
ADD CONSTRAINT c_psp_offering2 CHECK (service_code IN ('DirectDeposit', 'Tax', 'BillPayment', 'ThirdParty401k', 'CheckDistribution', 'Cloud', 'RiskAssessment', 'WorkersComp', 'ViewMyPaycheck', 'CloudV2', 'Guideline401k'));

ALTER TABLE pspadm.psp_offering
ADD PRIMARY KEY (offering_seq);

ALTER TABLE pspadm.psp_offering_svcchg
ADD CONSTRAINT c_psp_offering_svcchg0 CHECK (sku_type IN ('Payroll', 'NonPayroll'));

ALTER TABLE pspadm.psp_offering_svcchg
ADD PRIMARY KEY (offering_svcchg_seq);

ALTER TABLE pspadm.psp_offering_svcchg_grp
ADD CONSTRAINT c_psp_offering_svcchg_grp0 CHECK (applies_to IN ('CourtesyRefund', 'PerPayroll', 'PerPaycheck', 'PerTransmission', 'AmendedSSN', 'PerPayment', 'ReversalFee', 'DebitReturnFee', 'ManualServicingFee', 'ChaseReportFeeUpTo3Payrolls', 'ChaseReportFeeUpTo6Payrolls', 'ChaseReportFeeUpTo15Payrolls', 'ChaseReportFeeUpTo20Payrolls', 'ChaseReportFeeOver20Payrolls', 'BankVerificationDebit', 'PaymentArrangementFee', 'PerBatch', 'CompanyUpdates', 'EmployeesAdded', 'EmployeesUpdated', 'BackdatedPayroll', 'DirectDepositFee', 'EmployeesPaid', 'MonthlyFee', 'Amendments', 'EntityChange', 'ExtraCopies', 'PayrollAdjustment', 'PenaltiesAndInterest', 'ExtraStateFee', 'OtherFee', 'W2Fee', 'EmployeeOrganizerFee', 'W2Correction', 'W2BaseFee', 'BankVerificationCredit')) ;

ALTER TABLE pspadm.psp_offering_svcchg_grp
ADD PRIMARY KEY (offering_svcchg_grp_seq);

ALTER TABLE pspadm.psp_offload_batch
ADD CONSTRAINT c_psp_offload_batch0 CHECK (status_cd IN ('Completed', 'InProcess')) ;

ALTER TABLE pspadm.psp_offload_batch
ADD PRIMARY KEY (offload_batch_seq);

ALTER TABLE pspadm.psp_offload_group
ADD PRIMARY KEY (offload_group_seq);

ALTER TABLE pspadm.psp_on_hold_reason
ADD CONSTRAINT c_psp_on_hold_reason0 CHECK (on_hold_reason_cd IN ('AchRejectOther', 'AchRejectR1R9', 'ActiveCurrent', 'ActiveSeasonal', 'Cancelled', 'DirectDepositLimit', 'Fraud', 'AuditCorrections', 'FraudReview', 'IntuitCollections', 'MissingPaperwork', 'PendingBalanceFile', 'PendingBankVerification', 'PendingFirstPayroll', 'PendingPinCreation', 'PendingTermination', 'RiskAssessment', 'RiskCollections', 'SuspendedDirectDeposit', 'Terminated', 'AMLHold', 'PendingTaxAcceptance', 'PendingEnrollment', 'PendingPrefundingWire', 'AS400Hold', 'AS400DirectDepositLimitHold', 'PendingSetup', 'BillPaymentLimit', 'MTLHold')) ;

ALTER TABLE pspadm.psp_on_hold_reason
ADD PRIMARY KEY (on_hold_reason_seq);

ALTER TABLE pspadm.psp_ownership_type
ADD PRIMARY KEY (ownership_type_seq);

ALTER TABLE pspadm.psp_pay_item
ADD CONSTRAINT c_psp_pay_item0 CHECK (pay_item_cd IN ('HIREAct', 'Tips')) ;

ALTER TABLE pspadm.psp_pay_item
ADD PRIMARY KEY (pay_item_seq);

ALTER TABLE pspadm.psp_paycheck
ADD CONSTRAINT sys_c008737 PRIMARY KEY (company_fk, paycheck_seq);

ALTER TABLE pspadm.psp_paycheck_split
ADD CONSTRAINT sys_c008745 PRIMARY KEY (company_fk, paycheck_split_seq);

--ALTER TABLE pspadm.psp_paycheck_usage
--ADD PRIMARY KEY (paycheck_usage_seq);


ALTER TABLE pspadm.psp_paycheck_usage_hist
ADD PRIMARY KEY (paycheck_usage_hist_seq);

ALTER TABLE pspadm.psp_payee
ADD PRIMARY KEY (payee_seq);

ALTER TABLE pspadm.psp_payee_bank_account
ADD CONSTRAINT c_psp_payee_bank_account0 CHECK (status_cd IN ('PendingVerification', 'Active', 'Inactive')) ;

ALTER TABLE pspadm.psp_payee_bank_account
ADD PRIMARY KEY (payee_bank_account_seq);

ALTER TABLE pspadm.psp_payment_batch_assoc
ADD PRIMARY KEY (payment_batch_assoc_seq);

ALTER TABLE pspadm.psp_payment_method_requirement
ADD PRIMARY KEY (payment_method_requirement_seq);

ALTER TABLE pspadm.psp_payment_requirement
ADD PRIMARY KEY (payment_requirement_seq);

ALTER TABLE pspadm.psp_payment_template
ADD CONSTRAINT c_psp_payment_template0 CHECK (category IN ('Other', 'SUI', 'Withholding')) ;

ALTER TABLE pspadm.psp_payment_template
ADD PRIMARY KEY (payment_template_cd);

ALTER TABLE pspadm.psp_payment_template_agency_id
ADD PRIMARY KEY (payment_template_agency_id_seq);

ALTER TABLE pspadm.psp_payroll_fraud_batch
ADD PRIMARY KEY (payroll_fraud_batch_seq);

ALTER TABLE pspadm.psp_payroll_frequency
ADD PRIMARY KEY (payroll_freq_cd);

ALTER TABLE pspadm.psp_payroll_item
ADD CONSTRAINT c_psp_payroll_item0 CHECK (payroll_item_code IN ('Tp401kEmployeeDeferral', 'Tp401kEmployerMatch', 'Tp401kLoanPayment', 'Tp401kProfitSharing', 'Tp401kRoth', 'Tp401kSafeHarbor', 'Salary', 'Hourly', 'OtherPreTaxDeduction', 'Compensation', 'OtherPostTaxDeduction', 'OtherTaxableEmployerContribution', 'OtherNonTaxableEmployerContribution', 'Bonus', 'Commission', 'OtherAdditionPreTax', 'OtherAdditionPostTax', 'DirectDeposit')) ;

ALTER TABLE pspadm.psp_payroll_item
ADD CONSTRAINT c_psp_payroll_item1 CHECK (payroll_item_type IN ('Compensation', 'Deduction', 'EmployerContribution')) ;

ALTER TABLE pspadm.psp_payroll_item
ADD PRIMARY KEY (payroll_item_code);

ALTER TABLE pspadm.psp_payroll_item_taxable_to
ADD PRIMARY KEY (payroll_item_taxable_to_seq);

ALTER TABLE pspadm.psp_payroll_run
ADD CONSTRAINT c_psp_payroll_run0 CHECK (collection_stage_cd IN ('FirstCollectionAttempt', 'SecondCollectionAttempt', 'TerminationExpected')) ;

ALTER TABLE pspadm.psp_payroll_run
ADD CONSTRAINT c_psp_payroll_run1 CHECK (payroll_run_status IN ('Superseded', 'Complete', 'Canceled', 'DebitReturnedCanceled', 'DebitReturned', 'NSFCanceled', 'OffloadedAll', 'OffloadedDebit', 'Pending', 'WrittenOff', 'PendingReversals', 'PendingAutoRedebit', 'AutoRedebitOffloaded', 'PendingRedebit', 'RedebitOffloaded', 'PendingWire', 'ReversalsOffloaded', 'ReversalsFinished', 'ReturnedTwice', 'PendingToDD', 'SentToDD', 'None')) ;

ALTER TABLE pspadm.psp_payroll_run
ADD CONSTRAINT c_psp_payroll_run2 CHECK (payroll_run_type IN ('FeeOnly', 'Regular', 'Adjustment', 'BillPayment', 'CloudOnly')) ;

ALTER TABLE pspadm.psp_payroll_run
ADD CONSTRAINT c_psp_payroll_run3 CHECK (d_d_status IN ('Pending', 'PendingToDD', 'SentToDD', 'Canceled', 'Complete', 'Fail', 'OffloadedDebit', 'OffloadedCredit', 'OffloadedAll', 'PendingVoid', 'SentVoid', 'PendingCompleteToDD', 'SentCompleteToDD', 'SentSupersededToDD', 'PendingSupersededToDD', 'Superseded', 'None', 'PendingPartialVoid', 'SentPartialVoid', 'ReversalsFinished', 'DebitReturnedCanceled', 'ReturnedTwice', 'PendingAutoRedebit', 'NSFCanceled', 'WrittenOff', 'PendingWire', 'PendingReversals', 'PendingRedebit', 'RedebitOffloaded', 'DebitReturned', 'AutoRedebitOffloaded', 'ReversalsOffloaded')) ;

ALTER TABLE pspadm.psp_payroll_run
ADD CONSTRAINT c_psp_payroll_run4 CHECK (tax_and_fees_status IN ('Superseded', 'Complete', 'Canceled', 'DebitReturnedCanceled', 'DebitReturned', 'NSFCanceled', 'OffloadedAll', 'OffloadedDebit', 'Pending', 'WrittenOff', 'PendingReversals', 'PendingAutoRedebit', 'AutoRedebitOffloaded', 'PendingRedebit', 'RedebitOffloaded', 'PendingWire', 'ReversalsOffloaded', 'ReversalsFinished', 'ReturnedTwice', 'PendingToDD', 'SentToDD', 'None')) ;

ALTER TABLE pspadm.psp_payroll_run
ADD CONSTRAINT c_psp_payroll_run5 CHECK (d_d_message_status IN ('PendingPartialVoid', 'SentPartialVoid', 'PendingCompleteToDD', 'SentCompleteToDD', 'SentSupersededToDD', 'PendingSupersededToDD', 'Superseded', 'OffloadedDebit', 'Pending', 'Canceled', 'Complete', 'None', 'Fail', 'PendingToDD', 'SentToDD', 'PendingVoid', 'SentVoid', 'OffloadedCredit', 'OffloadedAll')) ;

ALTER TABLE pspadm.psp_payroll_run
ADD PRIMARY KEY (payroll_run_seq);

ALTER TABLE pspadm.psp_payroll_run_action
ADD CONSTRAINT c_psp_payroll_run_action0 CHECK (status IN ('Superseded', 'Complete', 'Canceled', 'DebitReturnedCanceled', 'DebitReturned', 'NSFCanceled', 'OffloadedAll', 'OffloadedDebit', 'Pending', 'WrittenOff', 'PendingReversals', 'PendingAutoRedebit', 'AutoRedebitOffloaded', 'PendingRedebit', 'RedebitOffloaded', 'PendingWire', 'ReversalsOffloaded', 'ReversalsFinished', 'ReturnedTwice', 'PendingToDD', 'SentToDD', 'None')) ;

ALTER TABLE pspadm.psp_payroll_run_action
ADD PRIMARY KEY (payroll_run_action_seq);

ALTER TABLE pspadm.psp_payroll_subtype
ADD CONSTRAINT c_psp_payroll_subtype0 CHECK (payroll_subtype_cd IN ('FreeBasic1', 'BasicLimited', 'BasicUnlimited', 'Enhanced', 'EnhancedAccountant', 'EnhancedUnlimited', 'NewBasicUnlimited', 'Standard', 'Basic0to3Emp', 'Enhanced0to3Emp', 'PAPEnhAcct', 'Assisted', 'AssistedAdv', 'MonthlyBasic0to3Emp', 'MonthlyBasicUnlimited', 'MonthlyEnhanced0to3Emp', 'MonthlyEnhancedUnlimited')) ;

ALTER TABLE pspadm.psp_payroll_subtype
ADD PRIMARY KEY (payroll_subtype_seq);

ALTER TABLE pspadm.psp_paystub
ADD PRIMARY KEY (company_fk, paystub_seq);

ALTER TABLE pspadm.psp_perf_sst
ADD PRIMARY KEY (time_pacific, transmission_type);

ALTER TABLE pspadm.psp_pmt_template_bankaccount
ADD CONSTRAINT c_psp_pmt_template_bankacc0 CHECK (status_cd IN ('PendingVerification', 'Active', 'Inactive')) ;

ALTER TABLE pspadm.psp_pmt_template_bankaccount
ADD PRIMARY KEY (pmt_template_bankaccount_seq);

ALTER TABLE pspadm.psp_pmt_template_frequency
ADD CONSTRAINT c_psp_pmt_template_frequen0 CHECK (payment_frequency_id IN ('ACCELERATED', 'ANNUAL', 'EARLYFILER', 'EIGHTHMONTHLY', 'FIVEBANKINGDAY', 'MONTHLY', 'MONTHLYACCELERATED', 'NEXTBANKINGDAY', 'QUADMONTHLY', 'QUARTERLY', 'QUARTERMONTHLY', 'SEMIANNUAL', 'SEMIMONTHLY', 'SEMIWEEKLY', 'SPLITMONTHLY', 'THREEBANKINGDAY', 'TWICEMONTHLY', 'NOCALC', 'WEEKLY')) ;

ALTER TABLE pspadm.psp_pmt_template_frequency
ADD PRIMARY KEY (payment_template_frequency_id);

ALTER TABLE pspadm.psp_pmt_template_paymentmethod
ADD CONSTRAINT c_psp_pmt_template_payment0 CHECK (payment_method IN ('ACHDebit', 'ACHCredit', 'CheckPayment', 'PostBalfHPDE', 'PostBalfHPDERefund', 'ACHDirectDeposit', 'WirePayment', 'EFE', 'HPDERefund', 'HPDE', 'EFTPS', 'EFTPSDirectDebit', 'EDI', 'SuperCheck')) ;

ALTER TABLE pspadm.psp_pmt_template_paymentmethod
ADD PRIMARY KEY (pmt_template_paymentmethod_seq);

ALTER TABLE pspadm.psp_pmttemplate_chkinfo_assoc
ADD PRIMARY KEY (pmttemplate_chkinfo_assoc_seq);

ALTER TABLE pspadm.psp_pmttemplate_printedchkinfo
ADD PRIMARY KEY (pmttemplate_printedchkinfo_seq);

ALTER TABLE pspadm.psp_posting_rule
ADD PRIMARY KEY (posting_rule_cd);

ALTER TABLE pspadm.psp_prior_payment_submission
ADD PRIMARY KEY (prior_payment_submission_seq);

ALTER TABLE pspadm.psp_property_audit
ADD PRIMARY KEY (company_fk, property_audit_seq);

ALTER TABLE pspadm.psp_pstub_address
ADD PRIMARY KEY (pstub_address_seq);

ALTER TABLE pspadm.psp_pstub_dditem
ADD PRIMARY KEY (pstub_dditem_seq);

ALTER TABLE pspadm.psp_pstub_employee_info
ADD PRIMARY KEY (company_fk, pstub_employee_info_seq);

ALTER TABLE pspadm.psp_pstub_employee_preference
ADD CONSTRAINT emp_pref_unq_indx UNIQUE (app_name, preference_name, employee_fk);

ALTER TABLE pspadm.psp_pstub_employee_preference
ADD PRIMARY KEY (pstub_employee_preference_seq);

ALTER TABLE pspadm.psp_pstub_employer_info
ADD PRIMARY KEY (pstub_employer_info_seq);

ALTER TABLE pspadm.psp_pstub_msg
ADD CONSTRAINT c_psp_pstub_msg0 CHECK (type IN ('Company', 'User')) ;

ALTER TABLE pspadm.psp_pstub_msg
ADD PRIMARY KEY (pstub_msg_seq);

ALTER TABLE pspadm.psp_pstub_paid_timeoff_item
ADD PRIMARY KEY (company_fk, pstub_paid_timeoff_item_seq);

ALTER TABLE pspadm.psp_pstub_pay_item
ADD PRIMARY KEY (company_fk, pstub_pay_item_seq);

ALTER TABLE pspadm.psp_pstub_state_tax_info
ADD PRIMARY KEY (pstub_state_tax_info_seq);

ALTER TABLE pspadm.psp_qbdt_employee_info
ADD CONSTRAINT c_psp_qbdt_employee_info0 CHECK (employee_type IN ('REG', 'OFFICER', 'STATUTORY', 'OWNER', 'REP')) ;

ALTER TABLE pspadm.psp_qbdt_employee_info
ADD PRIMARY KEY (qbdt_employee_info_seq);

ALTER TABLE pspadm.psp_qbdt_paycheck_info
ADD PRIMARY KEY (company_fk, qbdt_paycheck_info_seq);

ALTER TABLE pspadm.psp_qbdt_payline_info
ADD PRIMARY KEY (company_fk, qbdt_payline_info_seq);


ALTER TABLE pspadm.psp_qbdt_payroll_item_info
ADD CONSTRAINT c_psp_qbdt_payroll_item_in0 CHECK (pay_type IN ('REG', 'SICK', 'VAC')) ;

ALTER TABLE pspadm.psp_qbdt_payroll_item_info
ADD CONSTRAINT c_psp_qbdt_payroll_item_in1 CHECK (special_type IN ('COMCARE', 'COSSEC', 'EEMCARE', 'EESSEC', 'FEDTAX', 'FUTA', 'SALARY', 'SICKSALARY', 'VACSALARY', 'SICKHRLY', 'VACHRLY', 'AEIC', 'DIRDEP', 'WORKERCOMP')) ;

ALTER TABLE pspadm.psp_qbdt_payroll_item_info
ADD CONSTRAINT c_psp_qbdt_payroll_item_in2 CHECK (default_rate_type IN ('MoneyType', 'Percentage')) ;

ALTER TABLE pspadm.psp_qbdt_payroll_item_info
ADD PRIMARY KEY (qbdt_payroll_item_info_seq);

ALTER TABLE pspadm.psp_qbdt_payroll_trans_line
ADD PRIMARY KEY (qbdt_payroll_trans_line_seq);

ALTER TABLE pspadm.psp_qbdt_payroll_transaction
ADD CONSTRAINT c_psp_qbdt_payroll_transac0 CHECK (transaction_type IN ('FundsTransfer', 'PriorPayment', 'LiabilityAdjustment', 'DDReturn', 'Refund', 'LiabilityCheck')) ;

ALTER TABLE pspadm.psp_qbdt_payroll_transaction
ADD PRIMARY KEY (qbdt_payroll_transaction_seq);

ALTER TABLE pspadm.psp_qbdt_request_info
ADD PRIMARY KEY (qbdt_request_info_seq);

ALTER TABLE pspadm.psp_qbdt_transaction_info
ADD PRIMARY KEY (company_fk, qbdt_transaction_info_seq);

ALTER TABLE pspadm.psp_qbdt_unprocessed_request
ADD CONSTRAINT c_psp_qbdt_unprocessed_req0 CHECK (status IN ('Processed', 'Queued', 'Error', 'Processing')) ;

ALTER TABLE pspadm.psp_qbdt_unprocessed_request
ADD PRIMARY KEY (qbdt_unprocessed_request_seq);

ALTER TABLE pspadm.psp_quickbooks_info
ADD PRIMARY KEY (quickbooks_info_seq);

ALTER TABLE pspadm.psp_racompany_service_info
ADD PRIMARY KEY (racompany_service_info_seq);

ALTER TABLE pspadm.psp_rafenrollment
ADD CONSTRAINT c_psp_rafenrollment0 CHECK (status IN ('PendingEnrollment', 'PendingEnrollmentTape', 'PendingEnrollmentResponse', 'Enrolled', 'Rejected', 'Cancelled', 'PendingDeleteTape', 'Deleted')) ;

ALTER TABLE pspadm.psp_rafenrollment
ADD PRIMARY KEY (rafenrollment_seq);

ALTER TABLE pspadm.psp_rafenrollment_detail
ADD PRIMARY KEY (rafenrollment_detail_seq);

ALTER TABLE pspadm.psp_rafenrollment_file
ADD CONSTRAINT c_psp_rafenrollment_file0 CHECK (status IN ('Completed', 'Error', 'Initiated', 'RecreationInitiated', 'Transmitted', 'Finalized', 'Emailed', 'PendingTransmission')) ;

ALTER TABLE pspadm.psp_rafenrollment_file
ADD CONSTRAINT c_psp_rafenrollment_file1 CHECK (r_a_f_action_code IN ('Add', 'Delete')) ;

ALTER TABLE pspadm.psp_rafenrollment_file
ADD PRIMARY KEY (rafenrollment_file_seq);

ALTER TABLE pspadm.psp_rate_ledger_operation
ADD PRIMARY KEY (rate_ledger_operation_seq);

ALTER TABLE pspadm.psp_report_job_setup
ADD PRIMARY KEY (report_name);

ALTER TABLE pspadm.psp_reporting_agent
ADD PRIMARY KEY (reporting_agent_seq);

ALTER TABLE pspadm.psp_return_reason_desc
ADD CONSTRAINT c_psp_return_reason_desc0 CHECK (reason_cd IN ('R01', 'R02', 'R03', 'R04', 'R05', 'R06', 'R07', 'R08', 'R09', 'R20', 'R24', 'R28', 'R29', 'R10', 'R12', 'R13', 'R14', 'R15', 'R16', 'R18', 'R11', 'R17', 'R19', 'R21', 'R22', 'R23', 'R25', 'R26', 'R27', 'R30', 'R31', 'R32', 'R33', 'R34', 'R35', 'R36', 'R37', 'R38', 'R39', 'R40', 'R41', 'R42', 'R43', 'R44', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R51', 'R52', 'R53', 'C01', 'R61', 'R62', 'R63', 'C02', 'C03', 'C04', 'C05', 'C06', 'C07', 'R64', 'R65', 'R66', 'R67', 'R68', 'R69', 'R70', 'R71', 'R72', 'R73', 'R74', 'R75', 'R76', 'R80', 'R81', 'C08', 'C09', 'C10', 'R82', 'R83', 'R84', 'C11', 'C12', 'C13', 'C61', 'C62', 'C63', 'R99', 'C64', 'C65', 'C66', 'C67', 'C68', 'C69', 'C99')) ;

ALTER TABLE pspadm.psp_return_reason_desc
ADD PRIMARY KEY (reason_cd);

ALTER TABLE pspadm.psp_role_sub_status
ADD CONSTRAINT c_psp_role_sub_status0 CHECK (allowed_change_type IN ('CanMoveFromSubStatus', 'CanMoveToSubStatus'));

ALTER TABLE pspadm.psp_role_sub_status
ADD PRIMARY KEY (role_sub_status_seq);

ALTER TABLE pspadm.psp_rtbautomationbackup
ADD CONSTRAINT c_psp_rtbautomationbackup0 CHECK (event_type IN ('DUPLICATEPITEM', 'DUPLICATEEMPLOYEE', 'ERROR2108', 'VMPSERVICEEVENT')) ;

ALTER TABLE pspadm.psp_rtbautomationbackup
ADD PRIMARY KEY (rtbautomationbackup_seq);

ALTER TABLE pspadm.psp_sap_method_call
ADD PRIMARY KEY (sap_method_call_seq);

ALTER TABLE pspadm.psp_saved_reports
ADD PRIMARY KEY (saved_reports_seq);

ALTER TABLE pspadm.psp_second_offload
ADD PRIMARY KEY (second_offload_seq);

ALTER TABLE pspadm.psp_serv_stat_txn_sku_type
ADD CONSTRAINT c_psp_serv_stat_txn_sku_ty0 CHECK (sku_type IN ('Payroll', 'NonPayroll'));

ALTER TABLE pspadm.psp_serv_stat_txn_sku_type
ADD CONSTRAINT c_psp_serv_stat_txn_sku_ty1 CHECK (offering_service_charge_type IN ('CourtesyRefund', 'PerPayroll', 'PerPaycheck', 'PerTransmission', 'AmendedSSN', 'PerPayment', 'ReversalFee', 'DebitReturnFee', 'ManualServicingFee', 'ChaseReportFeeUpTo3Payrolls', 'ChaseReportFeeUpTo6Payrolls', 'ChaseReportFeeUpTo15Payrolls', 'ChaseReportFeeUpTo20Payrolls', 'ChaseReportFeeOver20Payrolls', 'BankVerificationDebit', 'PaymentArrangementFee', 'PerBatch', 'CompanyUpdates', 'EmployeesAdded', 'EmployeesUpdated', 'BackdatedPayroll', 'DirectDepositFee', 'EmployeesPaid', 'MonthlyFee', 'Amendments', 'EntityChange', 'ExtraCopies', 'PayrollAdjustment', 'PenaltiesAndInterest', 'ExtraStateFee', 'OtherFee', 'W2Fee', 'EmployeeOrganizerFee', 'W2Correction', 'W2BaseFee', 'BankVerificationCredit')) ;

ALTER TABLE pspadm.psp_serv_stat_txn_sku_type
ADD PRIMARY KEY (serv_stat_txn_sku_type_seq);

ALTER TABLE pspadm.psp_service
ADD CONSTRAINT c_psp_service0 CHECK (service_cd IN ('DirectDeposit', 'Tax', 'BillPayment', 'ThirdParty401k', 'CheckDistribution', 'Cloud', 'RiskAssessment', 'WorkersComp', 'ViewMyPaycheck', 'CloudV2', 'Guideline401k'));

ALTER TABLE pspadm.psp_service
ADD PRIMARY KEY (service_cd);

ALTER TABLE pspadm.psp_service_status
ADD CONSTRAINT c_psp_service_status0 CHECK (service_status_cd IN ('Active', 'Cancelled', 'OnHold', 'PendingActivation', 'Terminated'));

ALTER TABLE pspadm.psp_service_status
ADD PRIMARY KEY (service_status_cd);

ALTER TABLE pspadm.psp_service_sub_status
ADD CONSTRAINT c_psp_service_sub_status0 CHECK (service_sub_status_cd IN ('AchRejectOther', 'AchRejectR1R9', 'ActiveCurrent', 'ActiveSeasonal', 'Cancelled', 'DirectDepositLimit', 'Fraud', 'AuditCorrections', 'FraudReview', 'IntuitCollections', 'MissingPaperwork', 'PendingBalanceFile', 'PendingBankVerification', 'PendingFirstPayroll', 'PendingPinCreation', 'PendingTermination', 'RiskAssessment', 'RiskCollections', 'SuspendedDirectDeposit', 'Terminated', 'AMLHold', 'PendingTaxAcceptance', 'PendingEnrollment', 'PendingPrefundingWire', 'AS400Hold', 'AS400DirectDepositLimitHold', 'PendingSetup', 'BillPaymentLimit', 'MTLHold')) ;

ALTER TABLE pspadm.psp_service_sub_status
ADD PRIMARY KEY (service_sub_status_cd);

ALTER TABLE pspadm.psp_smsmigration
ADD CONSTRAINT c_psp_smsmigration0 CHECK (migration_status IN ('ValidationInProgress', 'NeedsValidation', 'ValidationSuccess', 'ValidationError', 'ValidationInternalError', 'MigrationError', 'MigrationInProgress', 'MigrationComplete', 'DataCollectionComplete', 'MigrationReverted', 'MigrationOnHold', 'LimitsMigrationError', 'LimitsMigrationRetryableError', 'LimitsMigrationOnHold', 'LimitsMigrationComplete', 'LimitsMigrationIrresolvableError')) ;

ALTER TABLE pspadm.psp_smsmigration
ADD PRIMARY KEY (smsmigration_seq);

ALTER TABLE pspadm.psp_smssync_failure
ADD CONSTRAINT c_psp_smssync_failure0 CHECK (sync_direction IN ('PSPToAS', 'ASToPSP'));

ALTER TABLE pspadm.psp_smssync_failure
ADD CONSTRAINT c_psp_smssync_failure1 CHECK (status IN ('Pending', 'NeverRetry', 'Done', 'InProcess'));

ALTER TABLE pspadm.psp_smssync_failure
ADD PRIMARY KEY (smssync_failure_seq);

ALTER TABLE pspadm.psp_source_payroll_parameter
ADD CONSTRAINT c_psp_source_payroll_param0 CHECK (parameter_cd IN ('MinQBVersionSupported', 'BookTransferEntryDescription', 'ReversalEntryDescription', 'MaxNumberOfFailedLoginAttempts', 'ShouldAddCompanyToPSP', 'PayrollEntryDescription', 'AllowMultipleFundingModels', 'MaxWarehouseTransactionDays', 'DefaultFundingModel', 'LockAccountDuration', 'AllowReverifyBankAccount', 'MinimumEarliestPayrollRunDays', 'DeactiveBankAccountOnReturnedVerificationDebit', 'UnsupportedQBVersionList', 'ResolveEmployeeNOC', 'AllowDuplicatePaycheckIdsIfStatusIsCancelled', 'AutomaticCompanyBankAccountVerification', 'QBVersionSunsetString', 'RetryPaymentEntryDescription', 'AllowBackdatedPayrolls', 'AllowOneOffUntimelyPayrolls', 'MinSupportedTaxTableVersion', 'ThirdParty401kCutoffTime', 'ThirdParty401kOffloadWaitPeriod', 'DefaultRACompanyLimit', 'TaxPaymentEntryDescription', 'TransmitterFEIN', 'TransmitterName', 'TransmitterAddress', 'TransmitterCity', 'TransmitterState', 'TransmitterZip', 'TransmitterZipExtension', 'SyncBillPayments', 'UnsupportedTaxTableList')) ;

ALTER TABLE pspadm.psp_source_payroll_parameter
ADD CONSTRAINT c_psp_source_payroll_param1 CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) ;

ALTER TABLE pspadm.psp_source_payroll_parameter
ADD PRIMARY KEY (source_payroll_parameter_seq);

ALTER TABLE pspadm.psp_source_system
ADD CONSTRAINT c_psp_source_system0 CHECK (source_system_cd IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) ;

ALTER TABLE pspadm.psp_source_system
ADD PRIMARY KEY (source_system_cd);

ALTER TABLE pspadm.psp_source_system_law_assoc
ADD PRIMARY KEY (source_system_law_assoc_seq);

ALTER TABLE pspadm.psp_sourcesys_printedchk_info
ADD CONSTRAINT c_psp_sourcesys_printedchk0 CHECK (source_system_code IN ('ADE', 'CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400', 'GEMINI', 'IOP', 'ERS', 'AMO')) ;

ALTER TABLE pspadm.psp_sourcesys_printedchk_info
ADD PRIMARY KEY (sourcesys_printedchk_info_seq);

ALTER TABLE pspadm.psp_sql_execution_log_entry
ADD PRIMARY KEY (sql_execution_log_entry_seq);

ALTER TABLE pspadm.psp_state_edi_tax_file
ADD PRIMARY KEY (state_edi_tax_file_seq);

ALTER TABLE pspadm.psp_state_report_assoc
ADD PRIMARY KEY (state_report_assoc_seq);

ALTER TABLE pspadm.psp_state_report_output
ADD CONSTRAINT c_psp_state_report_output0 CHECK (report_type IN ('Recon', 'Coupon', 'ZeroCoupon')) ;

ALTER TABLE pspadm.psp_state_report_output
ADD PRIMARY KEY (state_report_output_seq);

ALTER TABLE pspadm.psp_suicredits_job
ADD CONSTRAINT c_psp_suicredits_job0 CHECK (status IN ('Created', 'InProcess', 'Complete', 'Error'));

ALTER TABLE pspadm.psp_suicredits_job
ADD PRIMARY KEY (suicredits_job_seq);

ALTER TABLE pspadm.psp_svcchgprice
ADD PRIMARY KEY (svcchgprice_seq);

ALTER TABLE pspadm.psp_svcstat_srcsys_assoc
ADD PRIMARY KEY (service_sub_status_fk, source_system_fk);

ALTER TABLE pspadm.psp_svcstat_svc_assoc
ADD PRIMARY KEY (service_sub_status_fk, service_fk);

ALTER TABLE pspadm.psp_svcstat_syscap_assoc
ADD PRIMARY KEY (service_sub_status_fk, system_capability_fk);

ALTER TABLE pspadm.psp_system_capability
ADD CONSTRAINT c_psp_system_capability0 CHECK (system_capability_cd IN ('ChangeEmployerBankAccount', 'RefundOrCredit', 'SubmitPayroll', 'SynchronizeAccount', 'AddService', 'CancelService', 'ChangeCompanyInfo', 'ChangeEmployeeBankAccount', 'UpgradeFundingModel', 'VoidPayroll', 'VerifyCompanyBankAccount', 'RecallPayroll', 'UpdatePIN', 'AddEmployerBankAccount', 'SubmitPayment', 'RecallPayment')) ;

ALTER TABLE pspadm.psp_system_capability
ADD PRIMARY KEY (system_capability_cd);

ALTER TABLE pspadm.psp_system_parameter
ADD PRIMARY KEY (system_parameter_seq);

ALTER TABLE pspadm.psp_system_payment_requirement
ADD CONSTRAINT c_psp_system_payment_requi0 CHECK (system_requirement_type IN ('LAAIDDF', 'EFTPSEnrollment', 'ACHEnrollment')) ;

ALTER TABLE pspadm.psp_system_payment_requirement
ADD PRIMARY KEY (system_payment_requirement_seq);

ALTER TABLE pspadm.psp_system_requirement
ADD CONSTRAINT c_psp_system_requirement0 CHECK (system_requirement_type IN ('LAAIDDF', 'EFTPSEnrollment', 'ACHEnrollment')) ;

ALTER TABLE pspadm.psp_system_requirement
ADD PRIMARY KEY (system_requirement_seq);

ALTER TABLE pspadm.psp_tax
ADD PRIMARY KEY (company_fk, tax_seq);

ALTER TABLE pspadm.psp_tax_account_audit
ADD CONSTRAINT psp_tax_acc_aud_pk PRIMARY KEY (audit_id);

ALTER TABLE pspadm.psp_tax_company_service_info
ADD CONSTRAINT c_psp_tax_company_service_0 CHECK (w2_delivery_preference_cd IN ('Mail', 'Electronic')) ;

ALTER TABLE pspadm.psp_tax_company_service_info
ADD CONSTRAINT c_psp_tax_company_service_1 CHECK (client_packet_delivery_pref_cd IN ('Mail', 'Electronic')) ;

ALTER TABLE pspadm.psp_tax_company_service_info
ADD PRIMARY KEY (tax_company_service_info_seq);

ALTER TABLE pspadm.psp_tax_credits9061
ADD PRIMARY KEY (tax_credits9061_seq);

ALTER TABLE pspadm.psp_tax_credits_application
ADD PRIMARY KEY (tax_credits_application_seq);

ALTER TABLE pspadm.psp_tax_payment_on_hold_reason
ADD CONSTRAINT c_psp_tax_payment_on_hold_0 CHECK (on_hold_reason_cd IN ('Enrollment', 'Agent', 'Company', 'Amount', 'BackDate')) ;

ALTER TABLE pspadm.psp_tax_payment_on_hold_reason
ADD PRIMARY KEY (tax_payment_on_hold_reason_seq);

ALTER TABLE pspadm.psp_tax_penalty_interest
ADD CONSTRAINT c_psp_tax_penalty_interest0 CHECK (type IN ('Penalty', 'Interest'));

ALTER TABLE pspadm.psp_tax_penalty_interest
ADD CONSTRAINT c_psp_tax_penalty_interest1 CHECK (payment_method IN ('ACHDebit', 'ACHCredit', 'CheckPayment', 'PostBalfHPDE', 'PostBalfHPDERefund', 'ACHDirectDeposit', 'WirePayment', 'EFE', 'HPDERefund', 'HPDE', 'EFTPS', 'EFTPSDirectDebit', 'EDI', 'SuperCheck')) ;

ALTER TABLE pspadm.psp_tax_penalty_interest
ADD CONSTRAINT c_psp_tax_penalty_interest2 CHECK (period_type IN ('Quarter', 'Week', 'Month', 'Annual'));

ALTER TABLE pspadm.psp_tax_penalty_interest
ADD PRIMARY KEY (tax_penalty_interest_seq);

ALTER TABLE pspadm.psp_tax_table_misc_data
ADD PRIMARY KEY (tax_table_misc_data_seq);

ALTER TABLE pspadm.psp_third_party401k_batch
ADD CONSTRAINT c_psp_third_party401k_batch0 CHECK (upload_status_cd IN ('Archived', 'Empty', 'Finalized', 'InProcess', 'PendingTransmission', 'Superceded', 'Transmitted', 'Pending')) ;

ALTER TABLE pspadm.psp_third_party401k_batch
ADD PRIMARY KEY (third_party401k_batch_seq);

ALTER TABLE pspadm.psp_threshold_requirement
ADD PRIMARY KEY (threshold_requirement_seq);

ALTER TABLE pspadm.psp_tp401k_batch_employee
ADD PRIMARY KEY (tp401k_batch_employee_seq);

ALTER TABLE pspadm.psp_tp401k_batch_paycheck
ADD PRIMARY KEY (tp401k_batch_paycheck_seq);

ALTER TABLE pspadm.psp_tp401k_paycheck
ADD CONSTRAINT c_psp_tp401k_paycheck0 CHECK (current_state_cd IN ('Ineligible', 'Cancelled', 'None', 'Pending', 'Sent', 'InvalidPaycheckData', 'InvalidEmployeeData')) ;

ALTER TABLE pspadm.psp_tp401k_paycheck
ADD PRIMARY KEY (tp401k_paycheck_seq);

ALTER TABLE pspadm.psp_tp401k_paycheck_pending
ADD CONSTRAINT c_psp_tp401k_paycheck_pend0 CHECK (state_cd IN ('Ineligible', 'Cancelled', 'None', 'Pending', 'Sent', 'InvalidPaycheckData', 'InvalidEmployeeData')) ;

ALTER TABLE pspadm.psp_tp401k_paycheck_pending
ADD PRIMARY KEY (tp401k_paycheck_pending_seq);

ALTER TABLE pspadm.psp_tp401k_paycheck_state
ADD CONSTRAINT c_psp_tp401k_paycheck_state0 CHECK (state_cd IN ('Ineligible', 'Cancelled', 'None', 'Pending', 'Sent', 'InvalidPaycheckData', 'InvalidEmployeeData')) ;

ALTER TABLE pspadm.psp_tp401k_paycheck_state
ADD PRIMARY KEY (tp401k_paycheck_state_seq);

ALTER TABLE pspadm.psp_tp401k_signup_batch
ADD CONSTRAINT c_psp_tp401k_signup_batch0 CHECK (download_status_cd IN ('Archived', 'Empty', 'Finalized', 'InProcess', 'PendingTransmission', 'Superceded', 'Transmitted', 'Pending')) ;

ALTER TABLE pspadm.psp_tp401k_signup_batch
ADD PRIMARY KEY (tp401k_signup_batch_seq);

ALTER TABLE pspadm.psp_tp401k_signup_queue
ADD CONSTRAINT c_psp_tp401k_signup_queue0 CHECK (status IN ('Processed', 'Pending', 'Cancelled')) ;

ALTER TABLE pspadm.psp_tp401k_signup_queue
ADD PRIMARY KEY (tp401k_signup_queue_seq);

ALTER TABLE pspadm.psp_tp401kcompany_service_info
ADD PRIMARY KEY (tp401kcompany_service_info_seq);

ALTER TABLE pspadm.psp_transaction_offload_batch
ADD PRIMARY KEY (transaction_offload_batch_seq);

ALTER TABLE pspadm.psp_transaction_response
ADD PRIMARY KEY (transaction_response_seq);

ALTER TABLE pspadm.psp_transaction_return
ADD CONSTRAINT c_psp_transaction_return0 CHECK (return_status_cd IN ('Created', 'Error', 'Open', 'Resolved'));

ALTER TABLE pspadm.psp_transaction_return
ADD PRIMARY KEY (transaction_return_seq);

ALTER TABLE pspadm.psp_transaction_return_batch
ADD CONSTRAINT c_psp_transaction_return_b0 CHECK (status_cd IN ('Persisted', 'Completed', 'Received', 'Processed')) ;

ALTER TABLE pspadm.psp_transaction_return_batch
ADD PRIMARY KEY (transaction_return_batch_seq);

ALTER TABLE pspadm.psp_transaction_state
ADD CONSTRAINT c_psp_transaction_state0 CHECK (transaction_state_cd IN ('Created', 'Executed', 'Cancelled', 'Returned', 'Completed', 'Voided'));

ALTER TABLE pspadm.psp_transaction_state
ADD PRIMARY KEY (transaction_state_cd);

ALTER TABLE pspadm.psp_transaction_type
ADD CONSTRAINT c_psp_transaction_type0 CHECK (transaction_type_cd IN ('FLAdERLOcERPAY', 'FLAdERLOcTXCC', 'FLAdERPAYcERLO', 'BadDebtRecovery', 'EmployeeDdCredit', 'FLAdERPAYcTXCL', 'FLAdFCRcFI', 'EmployeeDdReversalDebit', 'EmployeeEscalationCredit', 'EmployerDdDebit', 'EmployerDdRedebit', 'EmployerTaxRefundCredit', 'EmployerDdRefundCredit', 'EmployerDdRejectRefundCredit', 'EmployerDdReturnedRefundCredit', 'EmployerDdReversalRefundCredit', 'EmployerDoublePaymentRefundCredit', 'EmployerEscalationCredit', 'EmployerFeeDebit', 'EmployerFeeRedebit', 'EmployerFeeRefundCredit', 'DdFraud', 'EmployerWriteOff', 'Intuit5DayReturnTransfer', 'IntuitEmployeeReturnTransfer', 'IntuitFeeTransfer', 'EmployerFeeReturnedRefundCredit', 'IntuitEmployerVerificationReturnTransfer', 'EmployerVerificationDebit', 'AgencyHPDEWarehousedTaxPayment', 'BadDebtCustomerRecoverySalesAndUseTax', 'IntuitTaxVoidTransfer', 'ThirdPartyCollectionExpense', 'ServiceSalesAndUseTax', 'ServiceSalesAndUseTaxRefundCredit', 'UntimelyReturnPostWriteOff', 'UntimelyReturnPreWriteOff', 'ServiceSalesAndUseTaxRedebit', 'ServiceSalesAndUseTaxReturnedRefundCredit', 'BadDebtRecoveryFee', 'BadDebtRecoverySalesAndUseTax', 'EmployerWriteOffFee', 'EmployerWriteOffSalesAndUseTax', 'EmployerFraudOrEscalationRefundCredit', 'EmployerTaxRedebit', 'EmployerTaxCredit', 'EmployerTaxReturnedCredit', 'AgencyTaxDebit', 'AgencyTaxCredit', 'EmployerTaxDebit', 'EmployerTaxReturnedRefundCredit', 'BadDebtCustomerRecovery', 'EmployerTaxDirectOverpaymentApplied', 'AgencyHPDETaxRefund', 'AgencyTaxRecredit', 'AgencyTaxRedebit', 'FLAdERRCcERRR', 'FLAdEERCcERRR', 'EmployerCobraPaymentAdjustmentDebit', 'AgencyCobraPaymentAdjustmentCredit', 'BadDebtCustomerRecoveryFee', 'EmployerPenaltiesRefundCredit', 'EmployerInterestRefundCredit', 'AgencyInterestCredit', 'FLAdERPAYcTXCC', 'FLAdATRcTXCL', 'FLAdFCBcCOGSINT', 'FLAdTXCCcERSUI', 'FLAdERSUIcTXCC', 'AgencyHPDETaxPayment', 'EmployerCreditBalanceCarryForwardCredit', 'AgencyCreditBalanceCarryForwardDebit', 'FLAdERSUIcTXCL', 'FLAdERSUIcATR', 'FLAdERSUIcERLO', 'FLAdERSUIcERPAY', 'ReissueAgencyTaxDebitOffset', 'FLAdTXCLcERSUI', 'AgencyRefundTOR', 'ReissueTaxLiabilityTransfer', 'FLAdATRcERSUI', 'FLAdERPAYcERSUI', 'FLAdBDcERRR', 'EmployeeReversalFailedWriteOff', 'ERPayableAppliedBalanceDue', 'Intuit5DayFeeReturnTransfer', 'Intuit5DaySalesTaxReturnTransfer', 'FLAdBDcERRC', 'FLAdBDcEERL', 'EmployerTaxDirectDebit', 'AgencyDirectCredit', 'EmployerTaxCreditApplied', 'EmployerTaxOverpaymentApplied', 'AgencyTaxOverpayment', 'AgencyTaxOverpaymentApplied', 'FLAdBDcTXCC', 'FLAdTXCLcTXCC', 'AgencyHPDEPriorPaymentApplied', 'AgencyDirectOverpayment', 'AgencyPostBALFHPDETaxRefund', 'AgencyDirectDebit', 'AgencyPostBALFHPDETaxPayment', 'FLAdTXCLcERLO', 'FLAdERRRcERRC', 'FLAdTXCCcTXCL', 'FLAdATRcERPAY', 'FLAdDDCLcDDCC', 'FLAdDDCCcDDCL', 'FLATemp1', 'FLATemp2', 'FLATemp3', 'FLATemp4', 'FLATemp5', 'EmployerSUITaxReceivable', 'EmployerSUITaxCollection', 'EmployerSUITaxRefund', 'EmployerSUITaxPayable', 'GlobalBookTransfer', 'EmployerVerificationCredit', 'BadDebtCustomerRecoveryTax', 'EmployerTaxDoublePaymentRefundCredit', 'EmployerTaxFraudOrEscalationRefundCredit', 'EmployerWriteOffTax', 'BadDebtRecoveryTax', 'FLAdTXCLcATR', 'FLAdEERLcEERC', 'FLAdTXCLcERPAY', 'FLAdBDcTXCL', 'FLAdTXCCcERPAY', 'FLAdERLOcTXCL', 'EmployerTaxCreditReturnedTransfer', 'EmployerInterestRefundDebit', 'EmployerPenaltiesRefundDebit', 'ERCourtesyRefundCredit', 'FLAdBDcERPAY', 'FLAdERLOcERSUI', 'FLAdTXCCcERLO', 'FLAdBDcATR', 'EmployerVerificationCreditReturnTransfer', 'FLAdBDcERLO', 'FLAdBDcERSUI', 'FLAdERRRcBD', 'FLAdERRCcBD', 'FLAdEERLcBD', 'FLAdTXCCcBD', 'FLAdTXCLcBD', 'FLAdERPAYcBD', 'FLAdATRcBD', 'FLAdERLOcBD', 'FLAdERSUIcBD', 'FLAdATRcTXCC', 'FLAdTXCCcATR', 'FLAdEERCcEERL'));

ALTER TABLE pspadm.psp_transaction_type
ADD CONSTRAINT c_psp_transaction_type1 CHECK (transaction_category IN ('Intuit', 'Employer', 'Employee', 'Agency'));

ALTER TABLE pspadm.psp_transaction_type
ADD CONSTRAINT c_psp_transaction_type2 CHECK (association_type IN ('Reversal', 'Refund', 'Reissue', 'Redebit', 'None', 'Impound', 'FinancialLedgerAdjustment')) ;

ALTER TABLE pspadm.psp_transaction_type
ADD CONSTRAINT c_psp_transaction_type3 CHECK (n_a_c_h_a_batch_type IN ('BookTransfer', 'Payroll', 'Reversal', 'RetryPayment', 'TaxPayment')) ;

ALTER TABLE pspadm.psp_transaction_type
ADD CONSTRAINT c_psp_transaction_type4 CHECK (transaction_type_group_cd IN ('Debit', 'Redebit', 'Credit', 'Recredit', 'Writeoff', 'Recovery', 'EscalationOrFraud', 'Other', 'CustomerRecovery', 'FinancialLedgerAdjustment', 'SUIPayments')) ;

ALTER TABLE pspadm.psp_transaction_type
ADD PRIMARY KEY (transaction_type_cd);

ALTER TABLE pspadm.psp_transmission_payroll_run
ADD CONSTRAINT c_psp_transmission_payroll0 CHECK (payroll_process IN ('SubmitPayroll', 'ReverseTransaction', 'RecallTransaction', 'UpdateTransactionVoidFlag', 'CancelTransaction'));

ALTER TABLE pspadm.psp_transmission_payroll_run
ADD PRIMARY KEY (transmission_payroll_run_seq);

ALTER TABLE pspadm.psp_txntype_service_assoc
ADD PRIMARY KEY (transaction_type_fk, service_fk);

ALTER TABLE pspadm.psp_usage_period
ADD PRIMARY KEY (usage_period_seq);

ALTER TABLE pspadm.psp_user_preference
ADD PRIMARY KEY (key);

ALTER TABLE pspadm.psp_user_setting
ADD PRIMARY KEY (user_setting_seq);

ALTER TABLE pspadm.psp_vmp_employee_info
ADD PRIMARY KEY (vmp_employee_info_seq);

ALTER TABLE pspadm.psp_voided_check
ADD PRIMARY KEY (voided_check_seq);

ALTER TABLE pspadm.psp_wage_limit
ADD PRIMARY KEY (wage_limit_id);

ALTER TABLE pspadm.psp_wc_company
ADD CONSTRAINT c_psp_wc_company0 CHECK (subs_type_cd IN ('Next', 'SplitLimit'));

ALTER TABLE pspadm.psp_wc_company
ADD PRIMARY KEY (wc_company_seq);

ALTER TABLE pspadm.psp_wc_paycheck
ADD CONSTRAINT c_psp_wc_paycheck0 CHECK (current_state_cd IN ('PendingEdit', 'Cancelled', 'PendingNew', 'Sent', 'PendingDelete')) ;

ALTER TABLE pspadm.psp_wc_paycheck
ADD PRIMARY KEY (wc_paycheck_seq);

ALTER TABLE pspadm.psp_wc_paycheck_pending
ADD CONSTRAINT c_psp_wc_paycheck_pending0 CHECK (state_cd IN ('PendingEdit', 'Cancelled', 'PendingNew', 'Sent', 'PendingDelete')) ;

ALTER TABLE pspadm.psp_wc_paycheck_pending
ADD PRIMARY KEY (wc_paycheck_pending_seq);

ALTER TABLE pspadm.psp_wc_paycheck_state
ADD CONSTRAINT c_psp_wc_paycheck_state0 CHECK (state_cd IN ('PendingEdit', 'Cancelled', 'PendingNew', 'Sent', 'PendingDelete')) ;

ALTER TABLE pspadm.psp_wc_paycheck_state
ADD PRIMARY KEY (wc_paycheck_state_seq);

ALTER TABLE pspadm.z_temp_diy_reset_company
ADD PRIMARY KEY (source_company_id);


