--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
ALTER TABLE PSP_BANK_ACCOUNT
 DROP CONSTRAINT BANKACCOUNTTYPE162;

ALTER TABLE PSP_OFFER
 DROP CONSTRAINT DISCOUNTTYPE95;

ALTER TABLE PSP_OFFER
 DROP CONSTRAINT OFFERBEGINEVENT98;

ALTER TABLE PSP_OFFER
 DROP CONSTRAINT OFFERENDEVENT99;

ALTER TABLE PSP_NACHAFILE
 DROP CONSTRAINT NACHAFILESTATUS88;

ALTER TABLE PSP_NACHAFILE
 DROP CONSTRAINT NACHAFILETYPE89;

ALTER TABLE PSP_GEMS_UPLOAD_BATCH
 DROP CONSTRAINT GEMSUPLOADBATCHSTATUS73;

ALTER TABLE PSP_GEMS_UPLOAD_BATCH
 DROP CONSTRAINT REPORTINGFREQUENCY70;

ALTER TABLE PSP_TRANSACTION_TYPE
 DROP CONSTRAINT NACHABATCHTYPE390;

ALTER TABLE PSP_TRANSACTION_TYPE
 DROP CONSTRAINT TRANSACTIONASSOCIATIONTYPE388;

ALTER TABLE PSP_TRANSACTION_TYPE
 DROP CONSTRAINT TRANSACTIONTYPECODE386;

ALTER TABLE PSP_TRANSACTION_TYPE
 DROP CONSTRAINT TRANSACTIONCATEGORY387;

ALTER TABLE PSP_TRANSACTION_TYPE
 DROP CONSTRAINT TRANSACTIONTYPEGROUPCODE391;

ALTER TABLE PSP_TRANSACTION_STATE
 DROP CONSTRAINT TRANSACTIONSTATECODE383;

ALTER TABLE PSP_SOURCE_SYSTEM
 DROP CONSTRAINT SOURCESYSTEMCODE380;

ALTER TABLE PSP_SERVICE
 DROP CONSTRAINT SERVICECODE377;

ALTER TABLE PSP_LEDGER_ACCOUNT
 DROP CONSTRAINT LEDGERACCOUNTCODE366;

ALTER TABLE PSP_LEDGER_ACCOUNT
 DROP CONSTRAINT LEDGERBALANCECALCULATIONR368;

ALTER TABLE PSP_LEDGER_ACCOUNT
 DROP CONSTRAINT LEDGERACCOUNTTYPE369;

ALTER TABLE PSP_LEDGER_ACCOUNT
 DROP CONSTRAINT REPORTINGFREQUENCY370;

ALTER TABLE PSP_EVENT_TYPE
 DROP CONSTRAINT EVENTTYPECODE359;

ALTER TABLE PSP_SYSTEM_CAPABILITY
 DROP CONSTRAINT SYSTEMCAPABILITYCODE355;

ALTER TABLE PSP_SERVICE_STATUS
 DROP CONSTRAINT SERVICESTATUSCODE346;

ALTER TABLE PSP_EVENT_DETAIL_TYPE
 DROP CONSTRAINT EVENTDETAILTYPECODE335;

ALTER TABLE PSP_COLLECTION_STAGE
 DROP CONSTRAINT COLLECTIONSTAGECODE329;

ALTER TABLE PSP_AUTH_OPERATION
 DROP CONSTRAINT OPERATIONID326;

ALTER TABLE PSP_ACTION_EVENT
 DROP CONSTRAINT ACTIONEVENTCODE320;

ALTER TABLE PSP_ACTION_EVENT
 DROP CONSTRAINT ACTIONTYPE322;

ALTER TABLE PSP_TRANSACTION_RETURN_BATCH
 DROP CONSTRAINT TRANSACTIONRETURNBATCHSTA318;

ALTER TABLE PSP_DICRFILE
 DROP CONSTRAINT DICRFILESTATUS55;

ALTER TABLE PSP_SOURCE_PAYROLL_PARAMETER
 DROP CONSTRAINT SOURCEPAYROLLPARAMETERCODE300;

ALTER TABLE PSP_SOURCE_PAYROLL_PARAMETER
 DROP CONSTRAINT SOURCESYSTEMCODE304;

ALTER TABLE PSP_OFFLOAD_BATCH
 DROP CONSTRAINT OFFLOADBATCHSTATUS275;

ALTER TABLE PSP_INDIVIDUAL
 DROP CONSTRAINT COMMUNICATIONTYPE262;

ALTER TABLE PSP_INDIVIDUAL
 DROP CONSTRAINT GENDER259;

ALTER TABLE PSP_FEE
 DROP CONSTRAINT FEETYPECODE246;

ALTER TABLE PSP_COMPANY
 DROP CONSTRAINT SOURCESYSTEMCODE183;

ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 DROP CONSTRAINT SOURCESYSTEMCODE135;

ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 DROP CONSTRAINT SOURCESYSTEMCODE144;

ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 DROP CONSTRAINT TRANSMISSIONTYPE140;

ALTER TABLE PSP_PAYROLL_SUBTYPE
 DROP CONSTRAINT PAYROLLSUBTYPECODE123;

ALTER TABLE PSP_PAYROLL_RUN_ACTION
 DROP CONSTRAINT PAYROLLSTATUS122;

ALTER TABLE PSP_ON_HOLD_REASON
 DROP CONSTRAINT SERVICESUBSTATUSCODE119;

ALTER TABLE PSP_OFFERING_SVCCHG_GRP
 DROP CONSTRAINT OFFERINGSERVICECHARGETYPE114;

ALTER TABLE PSP_LEDGER_ACCOUNT_ACTION
 DROP CONSTRAINT CREDITDEBITCODE77;

ALTER TABLE PSP_INTUIT_BA_BT_FT
 DROP CONSTRAINT NACHAFILETYPE75;

ALTER TABLE PSP_INTUIT_BA_BT_FT
 DROP CONSTRAINT NACHABATCHTYPE76;

ALTER TABLE PSP_SERVICE_SUB_STATUS
 DROP CONSTRAINT SERVICESUBSTATUSCODE351;

ALTER TABLE PSP_COMPANY_EVENT
 DROP CONSTRAINT COMPANYEVENTSTATUS38;

ALTER TABLE PSP_COMPANY_EVENT
 DROP CONSTRAINT EVENTTYPECODE39;

ALTER TABLE PSP_ASSIGNED_DEPOSIT_FREQUENCY
 DROP CONSTRAINT DEPOSITFREQUENCYSOURCE14;

ALTER TABLE PSP_PAYROLL_RUN
 DROP CONSTRAINT PAYROLLSTATUS295;

ALTER TABLE PSP_MONEY_MOVEMENT_TRANSACTION
 DROP CONSTRAINT PAYMENTSTATUS272;

ALTER TABLE PSP_MONEY_MOVEMENT_TRANSACTION
 DROP CONSTRAINT PAYMENTMETHOD273;

ALTER TABLE PSP_INTUIT_BANK_ACC_TXN_TYPE
 DROP CONSTRAINT CREDITDEBITCODE268;

ALTER TABLE PSP_EMPLOYEE
 DROP CONSTRAINT EMPLOYEESTATUS236;

ALTER TABLE PSP_CONTACT
 DROP CONSTRAINT CONTACTROLE202;

ALTER TABLE PSP_COMPANY_SERVICE
 DROP CONSTRAINT SERVICESUBSTATUSCODE198;

ALTER TABLE PSP_COMPANY_BANK_ACCOUNT
 DROP CONSTRAINT BANKACCOUNTSTATUS190;

ALTER TABLE PSP_ROLE_SUB_STATUS
 DROP CONSTRAINT SUBSTATUSCHANGETYPE132;

ALTER TABLE PSP_ENTRY_DETAIL_RECORD
 DROP CONSTRAINT CREDITDEBITCODE58;

ALTER TABLE PSP_TRANSACTION_RETURN
 DROP CONSTRAINT TRANSACTIONRETURNSTATUSCO314;

ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 DROP CONSTRAINT EVENTDETAILTYPECODE42;

ALTER TABLE PSP_EMPLOYEE_BANK_ACCOUNT
 DROP CONSTRAINT BANKACCOUNTSTATUS242;

ALTER TABLE PSP_FINANCIAL_TRANSACTION
 DROP CONSTRAINT BANKACCOUNTOWNERTYPE251;

ALTER TABLE PSP_FINANCIAL_TRANSACTION
 DROP CONSTRAINT BANKACCOUNTOWNERTYPE252;

ALTER TABLE PSP_FINANCIAL_TRANSACTION
 DROP CONSTRAINT SETTLEMENTTYPE250;

ALTER TABLE PSP_BILLING_DETAIL
 DROP CONSTRAINT OFFERINGSERVICECHARGETYPE33;

ALTER TABLE PSP_BILLING_DETAIL
 DROP CONSTRAINT SERVICECODE20;

Prompt Column EMAIL_STATUS;
ALTER TABLE PSP_COMPANY_EVENT
 ADD (EMAIL_STATUS  VARCHAR2(255 CHAR));

ALTER TABLE PSP_BANK_ACCOUNT
 ADD CONSTRAINT C_PSP_BANK_ACCOUNT0
 CHECK (ACCOUNT_TYPE_CD IN('Checking', 'Savings'));

ALTER TABLE PSP_OFFER
 ADD CONSTRAINT C_PSP_OFFER0
 CHECK (DISCOUNT_TYPE IN('AmountOff', 'PercentOff'));

ALTER TABLE PSP_OFFER
 ADD CONSTRAINT C_PSP_OFFER1
 CHECK (BEGIN_EVENT IN('SignupEvent', 'ActivationEvent', 'FirstUseEvent', 'RedemptionEvent'));

ALTER TABLE PSP_OFFER
 ADD CONSTRAINT C_PSP_OFFER2
 CHECK (END_EVENT IN('DateEvent', 'DurationEvent', 'PayrollUsageEvent'));

ALTER TABLE PSP_NACHAFILE
 ADD CONSTRAINT C_PSP_NACHAFILE0
 CHECK (STATUS IN('Finalized', 'InProcess', 'Transmitted', 'Archived', 'Acknowledged', 'PendingAcknowledgement', 'PendingTransmission'));

ALTER TABLE PSP_NACHAFILE
 ADD CONSTRAINT C_PSP_NACHAFILE1
 CHECK (FILE_TYPE IN('CCD', 'PPD'));

ALTER TABLE PSP_GEMS_UPLOAD_BATCH
 ADD CONSTRAINT C_PSP_GEMS_UPLOAD_BATCH0
 CHECK (BATCH_TYPE IN('Daily', 'Monthly'));

ALTER TABLE PSP_GEMS_UPLOAD_BATCH
 ADD CONSTRAINT C_PSP_GEMS_UPLOAD_BATCH1
 CHECK (UPLOAD_STATUS IN('InProcess', 'Empty', 'Finalized', 'PendingTransmission', 'Transmitted', 'Archived', 'Superceded'));

ALTER TABLE PSP_TRANSACTION_TYPE
 ADD CONSTRAINT C_PSP_TRANSACTION_TYPE0
 CHECK (TRANSACTION_TYPE_CD IN('BadDebtRecovery', 'EmployeeDdCredit', 'EmployeeDdReversalDebit', 'EmployeeEscalationCredit', 'EmployerDdDebit', 'EmployerDdRedebit', 'EmployerDdRefundCredit', 'EmployerDdRejectRefundCredit', 'EmployerDdReturnedRefundCredit', 'EmployerDdReversalRefundCredit', 'EmployerDoublePaymentRefundCredit', 'EmployerEscalationCredit', 'EmployerFeeDebit', 'EmployerFeeRedebit', 'EmployerFeeRefundCredit', 'DdFraud', 'EmployerWriteOff', 'Intuit5DayReturnTransfer', 'IntuitEmployeeReturnTransfer', 'IntuitFeeTransfer', 'EmployerFeeReturnedRefundCredit', 'IntuitEmployerVerificationReturnTransfer', 'EmployerVerificationDebit', 'ServiceSalesAndUseTax', 'ServiceSalesAndUseTaxRefundCredit', 'UntimelyReturnPostWriteOff', 'UntimelyReturnPreWriteOff', 'ServiceSalesAndUseTaxRedebit', 'ServiceSalesAndUseTaxReturnedRefundCredit', 'BadDebtRecoveryFee', 'BadDebtRecoverySalesAndUseTax', 'EmployerWriteOffFee', 'EmployerWriteOffSalesAndUseTax'));

ALTER TABLE PSP_TRANSACTION_TYPE
 ADD CONSTRAINT C_PSP_TRANSACTION_TYPE1
 CHECK (TRANSACTION_CATEGORY IN('Intuit', 'Employer', 'Employee'));

ALTER TABLE PSP_TRANSACTION_TYPE
 ADD CONSTRAINT C_PSP_TRANSACTION_TYPE2
 CHECK (ASSOCIATION_TYPE IN('Reversal', 'Refund', 'Reissue', 'Redebit', 'None'));

ALTER TABLE PSP_TRANSACTION_TYPE
 ADD CONSTRAINT C_PSP_TRANSACTION_TYPE3
 CHECK (N_A_C_H_A_BATCH_TYPE IN('BookTransfer', 'Payroll', 'Reversal'));

ALTER TABLE PSP_TRANSACTION_TYPE
 ADD CONSTRAINT C_PSP_TRANSACTION_TYPE4
 CHECK (TRANSACTION_TYPE_GROUP_CD IN('Debit', 'Redebit', 'Credit', 'Recredit', 'Writeoff', 'Recovery', 'EscalationOrFraud', 'Other'));

ALTER TABLE PSP_TRANSACTION_STATE
 ADD CONSTRAINT C_PSP_TRANSACTION_STATE0
 CHECK (TRANSACTION_STATE_CD IN('Created', 'Executed', 'Cancelled', 'Returned', 'Completed', 'Voided'));

ALTER TABLE PSP_SOURCE_SYSTEM
 ADD CONSTRAINT C_PSP_SOURCE_SYSTEM0
 CHECK (SOURCE_SYSTEM_CD IN('CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400'));

ALTER TABLE PSP_SERVICE
 ADD CONSTRAINT C_PSP_SERVICE0
 CHECK (SERVICE_CD IN('DirectDeposit', 'Tax'));

ALTER TABLE PSP_LEDGER_ACCOUNT
 ADD CONSTRAINT C_PSP_LEDGER_ACCOUNT0
 CHECK (LEDGER_ACCOUNT_CD IN('DDFutureReceivable', 'DDFutureLiability', 'DDCurrentCash', 'DDCurrentLiability', 'ERReturnReceivable', 'ERReturnCash', 'SalesAndUseTax', 'EEReturnCash', 'EEReturnLiablility', 'FeeCashRevenue', 'FeeCashBalanceSheet', 'FeeIncome', 'BadDebt'));

ALTER TABLE PSP_LEDGER_ACCOUNT
 ADD CONSTRAINT C_PSP_LEDGER_ACCOUNT1
 CHECK (BALANCE_CALCULATION_RULE IN('CreditAddsToBalance', 'DebitAddsToBalance'));

ALTER TABLE PSP_LEDGER_ACCOUNT
 ADD CONSTRAINT C_PSP_LEDGER_ACCOUNT2
 CHECK (LEDGER_ACCOUNT_TYPE IN('SUTax', 'Income'));

ALTER TABLE PSP_LEDGER_ACCOUNT
 ADD CONSTRAINT C_PSP_LEDGER_ACCOUNT3
 CHECK (REPORTING_FREQUENCY IN('Daily', 'Monthly'));

ALTER TABLE PSP_EVENT_TYPE
 ADD CONSTRAINT C_PSP_EVENT_TYPE0
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'StrikeRemoved', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PSPSentEmail', 'PayrollOffloaded', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'RefundIssued', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'WireReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged'));

ALTER TABLE PSP_SYSTEM_CAPABILITY
 ADD CONSTRAINT C_PSP_SYSTEM_CAPABILITY0
 CHECK (SYSTEM_CAPABILITY_CD IN('ChangeEmployerBankAccount', 'DebitMonthlySubscriptionFee', 'FileCompanyTaxes', 'OffloadPendingPayrolls', 'PayCompanyTaxes', 'RefundOrCredit', 'SubmitPayroll', 'SynchronizeAccount', 'AddService', 'CancelService', 'ChangeCompanyInfo', 'ChangeEmployeeBankAccount', 'TransmitBalanceFile', 'VerifyCompanyBankAccount', 'UpgradeFundingModel'));

ALTER TABLE PSP_SERVICE_STATUS
 ADD CONSTRAINT C_PSP_SERVICE_STATUS0
 CHECK (SERVICE_STATUS_CD IN('Active', 'Cancelled', 'OnHold', 'PendingActivation', 'Terminated'));

ALTER TABLE PSP_EVENT_DETAIL_TYPE
 ADD CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldServiceStatus', 'NewServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'RefundIssued', 'FeeAmount', 'OriginalTransactionDateTime', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeNumberOfTotal', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus'));

ALTER TABLE PSP_COLLECTION_STAGE
 ADD CONSTRAINT C_PSP_COLLECTION_STAGE0
 CHECK (COLLECTION_STAGE_CODE IN('FirstCollectionAttempt', 'SecondCollectionAttempt', 'TerminationExpected'));

ALTER TABLE PSP_AUTH_OPERATION
 ADD CONSTRAINT C_PSP_AUTH_OPERATION0
 CHECK (OPERATION_ID IN('BankReturnUpdate', 'AccessApplication', 'DDLimitUpdate', 'FundingModelUpdate', 'DDStatusUpdate', 'StrikeAdd', 'StrikeCancel', 'DDStatusPendingActivation', 'DDStatusActive', 'DDStatusPendingTermination', 'DDStatusTerminated', 'DDStatusOnHold', 'DDStatusSuspended', 'DDStatusCancelled', 'SettingUpdate', 'ViewFullBankAccountNumbers', 'BankReturnView', 'RecordNonACHRedebitTransaction', 'CreateFeeTransaction', 'CreateReversalTransaction', 'DDTransactionCancel', 'TransactionCancel', 'LedgerView', 'SelectNonStandardSettlementType', 'CreateRefundTransaction', 'VoidTransaction', 'BookTransferTransaction', 'ActivateBankAccount', 'DeActivateBankAccount', 'WriteoffBadDebtTransaction', 'RecoverBadDebtTransaction', 'EscalationCreditTransaction', 'IssueRedebitTransaction', 'AuthAccessApplication', 'AuthAddUpdateUsers', 'AuthRemoveUsers', 'AuthManageRoles', 'AuthAddUpdateHelpDesk', 'AuthAddRemoveHelpDesk', 'AuthAddUpdateDataCustodian', 'AuthRemoveDataCustodian', 'EditCompanyLegalInformation', 'EditCompanyContactInformation', 'ViewTransactionHistory', 'ViewVerificationDebits', 'ResetVerificationAmounts', 'GenerateRandomDebits', 'EditPayrollContact', 'EditPrincipalContacts', 'GeneratePin', 'AddBankAccountRandomDebits', 'AddOffering', 'AddOffer', 'EditChartOfAccounts', 'AddBankAccountByPassRandomDebits', 'RemoveOffer'));

ALTER TABLE PSP_ACTION_EVENT
 ADD CONSTRAINT C_PSP_ACTION_EVENT0
 CHECK (CODE IN('FinancialTransactionVoidTx', 'FinancialTransactionCancel', 'IssueReissueRefundEr', 'ReissueFee', 'TxStateHistory', 'DDTransactionCancel', 'DDTransactionReverse', 'DDRedebitAdd', 'DDRedebitRecord', 'ERFeeAdd', 'BadDebtWriteOff', 'BadDebtRecover', 'EEReturnTransfer', 'FeeTransfer', 'Intuit5DayReturnTransfer', 'DDRefund', 'ERReturnRefund', 'EEReturnRefund', 'ERWireExpected', 'ERChangeRedebitToWireExpected'));

ALTER TABLE PSP_ACTION_EVENT
 ADD CONSTRAINT C_PSP_ACTION_EVENT1
 CHECK (TYPE IN('FinancialTransaction', 'PayrollRun', 'LedgerAccount'));

ALTER TABLE PSP_TRANSACTION_RETURN_BATCH
 ADD CONSTRAINT C_PSP_TRANSACTION_RETURN_B0
 CHECK (STATUS_CD IN('Pending', 'Completed', 'Error'));

ALTER TABLE PSP_DICRFILE
 ADD CONSTRAINT C_PSP_DICRFILE0
 CHECK (STATUS IN('Processed', 'Archived'));

ALTER TABLE PSP_SOURCE_PAYROLL_PARAMETER
 ADD CONSTRAINT C_PSP_SOURCE_PAYROLL_PARAM0
 CHECK (PARAMETER_CD IN('ACHWaitPeriod', 'CompanyBankAccountDurationLimitForVerification', 'CompanyBankAccountVerificationAttemptLimit', 'ConsecutiveLimitViolationLimit', 'DDCompanyLimitDuration', 'DDEmployeeLimitDuration', 'DefaultDDCompanyLimit', 'DefaultDDEmployeeLimit', 'MaxDDCompanyLimitDefault', 'MinimumNonSuspectPayrollAmount', 'PayrollEntryDescription', 'MinPayrollRunsForLimitAutoIncrease', 'MinQBVersionSupported', 'BookTransferEntryDescription', 'ReversalEntryDescription', 'MaxNumberOfFailedLoginAttempts', 'AllowMultipleFundingModels', 'MaxWarehouseTransactionDays', 'DefaultFundingModel', 'LockAccountDuration', 'ShouldAddCompanyToPSP', 'AllowReverifyBankAccount', 'MinimumEarliestPayrollRunDays', 'DeactiveBankAccountOnReturnedVerificationDebit', 'UnsupportedQBVersionList'));

ALTER TABLE PSP_SOURCE_PAYROLL_PARAMETER
 ADD CONSTRAINT C_PSP_SOURCE_PAYROLL_PARAM1
 CHECK (SOURCE_SYSTEM_CD IN('CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400'));

ALTER TABLE PSP_OFFLOAD_BATCH
 ADD CONSTRAINT C_PSP_OFFLOAD_BATCH0
 CHECK (STATUS_CD IN('InProcess', 'Completed'));

ALTER TABLE PSP_INDIVIDUAL
 ADD CONSTRAINT C_PSP_INDIVIDUAL0
 CHECK (GENDER_CD IN('Male', 'Female'));

ALTER TABLE PSP_INDIVIDUAL
 ADD CONSTRAINT C_PSP_INDIVIDUAL1
 CHECK (COMMUNICATION_TYPE_PREFERENCE IN('Phone', 'Email'));

ALTER TABLE PSP_FEE
 ADD CONSTRAINT C_PSP_FEE0
 CHECK (FEE_CD IN('ReverseFee', 'NSFFee', 'CopyFee'));

ALTER TABLE PSP_COMPANY
 ADD CONSTRAINT C_PSP_COMPANY0
 CHECK (SOURCE_SYSTEM_CD IN('CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400'));

ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 ADD CONSTRAINT C_PSP_SOURCE_SYSTEM_TRANSM0
 CHECK (FROM_SOURCE_SYSTEM IN('CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400'));

ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 ADD CONSTRAINT C_PSP_SOURCE_SYSTEM_TRANSM1
 CHECK (TYPE IN('Sync', 'PayrollSubmission', 'ZeroPayroll', 'CreateAccount', 'UpdateAccount', 'MigrateAccount', 'ValidateBankAccount', 'CreatePIN', 'ChangePIN', 'UpdateBankAccount', 'EntitlementExtensionUpdate', 'AgreementUpdate', 'AccountUpdate', 'ContactUpdate', 'Unknown', 'CUEVENT', 'CUINFOMOD', 'EntitlementExtensionNew'));

ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 ADD CONSTRAINT C_PSP_SOURCE_SYSTEM_TRANSM2
 CHECK (TO_SOURCE_SYSTEM IN('CRIS', 'PSP', 'EWS', 'QBOE', 'QBDT', 'AS400'));

ALTER TABLE PSP_PAYROLL_SUBTYPE
 ADD CONSTRAINT C_PSP_PAYROLL_SUBTYPE0
 CHECK (PAYROLL_SUBTYPE_CD IN('BasicLimited', 'BasicUnlimited', 'Enhanced', 'EnhancedAccountant', 'EnhancedUnlimited', 'NewBasicUnlimited', 'Standard', 'Basic0to3Emp', 'Enhanced0to3Emp', 'PAPEnhAcct'));

ALTER TABLE PSP_PAYROLL_RUN_ACTION
 ADD CONSTRAINT C_PSP_PAYROLL_RUN_ACTION0
 CHECK (STATUS IN('Complete', 'Canceled', 'DebitReturnedCanceled', 'DebitReturned', 'NSFCanceled', 'OffloadedAll', 'OffloadedDebit', 'Pending', 'WrittenOff', 'PendingReversals', 'PendingAutoRedebit', 'AutoRedebitOffloaded', 'PendingRedebit', 'RedebitOffloaded', 'PendingWire', 'ReversalsOffloaded', 'ReversalsFinished', 'ReturnedTwice'));

ALTER TABLE PSP_ON_HOLD_REASON
 ADD CONSTRAINT C_PSP_ON_HOLD_REASON0
 CHECK (ON_HOLD_REASON_CD IN('AchRejectOther', 'AchRejectR1R9', 'ActiveCurrent', 'ActiveSeasonal', 'AuditCorrections', 'Cancelled', 'DirectDepositLimit', 'Fraud', 'FraudReview', 'IntuitCollections', 'MissingPaperwork', 'NoticeOfChange', 'PendingBalanceFile', 'PendingBankVerification', 'PendingFirstPayroll', 'PendingPinCreation', 'PendingTermination', 'RiskAssessment', 'RiskCollections', 'SuspendedDirectDeposit', 'Terminated'));

ALTER TABLE PSP_OFFERING_SVCCHG_GRP
 ADD CONSTRAINT C_PSP_OFFERING_SVCCHG_GRP0
 CHECK (APPLIES_TO IN('PerPayroll', 'PerPaycheck', 'PerTransmission', 'ReversalFee', 'DebitReturnFee', 'ManualServicingFee', 'ChaseReportFeeUpTo3Payrolls', 'ChaseReportFeeUpTo6Payrolls', 'ChaseReportFeeUpTo15Payrolls', 'ChaseReportFeeUpTo20Payrolls', 'ChaseReportFeeOver20Payrolls', 'BankVerificationDebit', 'PaymentArrangementFee'));

ALTER TABLE PSP_LEDGER_ACCOUNT_ACTION
 ADD CONSTRAINT C_PSP_LEDGER_ACCOUNT_ACTION0
 CHECK (CREDIT_DEBIT_INDICATOR IN('Credit', 'Debit'));

ALTER TABLE PSP_INTUIT_BA_BT_FT
 ADD CONSTRAINT C_PSP_INTUIT_BA_BT_FT0
 CHECK (FILE_TYPE IN('CCD', 'PPD'));

ALTER TABLE PSP_INTUIT_BA_BT_FT
 ADD CONSTRAINT C_PSP_INTUIT_BA_BT_FT1
 CHECK (N_A_C_H_A_BATCH_TYPE IN('BookTransfer', 'Payroll', 'Reversal'));

ALTER TABLE PSP_SERVICE_SUB_STATUS
 ADD CONSTRAINT C_PSP_SERVICE_SUB_STATUS0
 CHECK (SERVICE_SUB_STATUS_CD IN('AchRejectOther', 'AchRejectR1R9', 'ActiveCurrent', 'ActiveSeasonal', 'AuditCorrections', 'Cancelled', 'DirectDepositLimit', 'Fraud', 'FraudReview', 'IntuitCollections', 'MissingPaperwork', 'NoticeOfChange', 'PendingBalanceFile', 'PendingBankVerification', 'PendingFirstPayroll', 'PendingPinCreation', 'PendingTermination', 'RiskAssessment', 'RiskCollections', 'SuspendedDirectDeposit', 'Terminated'));

ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT0
 CHECK (STATUS_CD IN('Active', 'Inactive'));

ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT1
 CHECK (EVENT_TYPE_CD IN('AccountLocked', 'AgentNote', 'ACHReturnStatusChanged', 'EINChanged', 'BackdatedPayrollReceived', 'ACHReturn', 'ServiceStatusChange', 'CompanyBankAccountStatusChange', 'LimitViolation', 'ReversalOK', 'DDIncreasePayrollLimit', 'PayrollCancelled', 'Strike', 'ReversalRequested', 'TransmissionError', 'PINCreated', 'PINUpdated', 'PayrollSubmittedWithPendingNOC', 'BankAccountVerified', 'LegalNameChanged', 'LegalAddressChanged', 'CompanyBankAccountChange', 'FeeCreated', 'IncorrectPIN', 'CoaFeeAccountChange', 'CoaSalesTaxAccountChange', 'NOC', 'ReversalReturn', 'FeeReturn', 'ERRefundReturn', 'CBAVerifyReturn', 'DDDebitReturn', 'NSF', 'DDReject', 'StrikeRemoved', 'PayrollReceived', 'FirstPayrollReceived', 'ZeroPayrollReceived', 'PayrollSubmissionFailed', 'PayrollRejected', 'PSPSentEmail', 'PayrollOffloaded', 'FraudDetected', 'TaxExemptStatusChanged', 'OffloadReportPrinted', 'CustomerSignedUp', 'CompanyInformationChanged', 'PayrollCancelPending', 'RefundIssued', 'CBAVerifyExpired', 'EmailAddressChanged', 'ManualRedebitCreated', 'LastChanceNotify', 'WireReceived', 'ErrorInfo', 'DBANameChanged', 'CompanyContactChanged', 'CompanyContactRoleChanged', 'CompanyContactPhoneChanged', 'CompanyContactAddressChanged', 'CompanyFundingModelChanged', 'PayrollAdminChanged', 'QuickBooksInfoChanged'));

ALTER TABLE PSP_COMPANY_EVENT
 ADD CONSTRAINT C_PSP_COMPANY_EVENT2
 CHECK (EMAIL_STATUS IN('Pending', 'Sent', 'Ignore'));

ALTER TABLE PSP_ASSIGNED_DEPOSIT_FREQUENCY
 ADD CONSTRAINT C_PSP_ASSIGNED_DEPOSIT_FRE0
 CHECK (FREQUENCY_SOURCE IN('Agency', 'Company'));

ALTER TABLE PSP_PAYROLL_RUN
 ADD CONSTRAINT C_PSP_PAYROLL_RUN0
 CHECK (PAYROLL_RUN_STATUS IN('Complete', 'Canceled', 'DebitReturnedCanceled', 'DebitReturned', 'NSFCanceled', 'OffloadedAll', 'OffloadedDebit', 'Pending', 'WrittenOff', 'PendingReversals', 'PendingAutoRedebit', 'AutoRedebitOffloaded', 'PendingRedebit', 'RedebitOffloaded', 'PendingWire', 'ReversalsOffloaded', 'ReversalsFinished', 'ReturnedTwice'));

ALTER TABLE PSP_MONEY_MOVEMENT_TRANSACTION
 ADD CONSTRAINT C_PSP_MONEY_MOVEMENT_TRANS0
 CHECK (STATUS IN('Canceled', 'Created', 'Executed'));

ALTER TABLE PSP_MONEY_MOVEMENT_TRANSACTION
 ADD CONSTRAINT C_PSP_MONEY_MOVEMENT_TRANS1
 CHECK (MONEY_MOVEMENT_PAYMENT_METHOD IN('ACHDebit', 'ACHCredit', 'CheckPayment', 'ACHDirectDeposit'));

ALTER TABLE PSP_INTUIT_BANK_ACC_TXN_TYPE
 ADD CONSTRAINT C_PSP_INTUIT_BANK_ACC_TXN_0
 CHECK (CREDIT_DEBIT_IND IN('Credit', 'Debit'));

ALTER TABLE PSP_EMPLOYEE
 ADD CONSTRAINT C_PSP_EMPLOYEE0
 CHECK (STATUS_CD IN('Active', 'Inactive'));

ALTER TABLE PSP_CONTACT
 ADD CONSTRAINT C_PSP_CONTACT0
 CHECK (CONTACT_ROLE_CD IN('PayrollAdmin', 'Other', 'PrimaryPrincipal', 'SecondaryPrincipal'));

ALTER TABLE PSP_COMPANY_SERVICE
 ADD CONSTRAINT C_PSP_COMPANY_SERVICE0
 CHECK (STATUS_CD IN('AchRejectOther', 'AchRejectR1R9', 'ActiveCurrent', 'ActiveSeasonal', 'AuditCorrections', 'Cancelled', 'DirectDepositLimit', 'Fraud', 'FraudReview', 'IntuitCollections', 'MissingPaperwork', 'NoticeOfChange', 'PendingBalanceFile', 'PendingBankVerification', 'PendingFirstPayroll', 'PendingPinCreation', 'PendingTermination', 'RiskAssessment', 'RiskCollections', 'SuspendedDirectDeposit', 'Terminated'));

ALTER TABLE PSP_COMPANY_BANK_ACCOUNT
 ADD CONSTRAINT C_PSP_COMPANY_BANK_ACCOUNT0
 CHECK (STATUS_CD IN('PendingVerification', 'Active', 'Inactive'));

ALTER TABLE PSP_ROLE_SUB_STATUS
 ADD CONSTRAINT C_PSP_ROLE_SUB_STATUS0
 CHECK (ALLOWED_CHANGE_TYPE IN('CanMoveFromSubStatus', 'CanMoveToSubStatus'));

ALTER TABLE PSP_ENTRY_DETAIL_RECORD
 ADD CONSTRAINT C_PSP_ENTRY_DETAIL_RECORD0
 CHECK (CREDIT_DEBIT_INDICATOR IN('Credit', 'Debit'));

ALTER TABLE PSP_TRANSACTION_RETURN
 ADD CONSTRAINT C_PSP_TRANSACTION_RETURN0
 CHECK (RETURN_STATUS_CD IN('Created', 'Error', 'Open', 'Resolved'));

ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 ADD CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldServiceStatus', 'NewServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'RefundIssued', 'FeeAmount', 'OriginalTransactionDateTime', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeNumberOfTotal', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus'));

ALTER TABLE PSP_EMPLOYEE_BANK_ACCOUNT
 ADD CONSTRAINT C_PSP_EMPLOYEE_BANK_ACCOUNT0
 CHECK (STATUS_CD IN('PendingVerification', 'Active', 'Inactive'));

ALTER TABLE PSP_FINANCIAL_TRANSACTION
 ADD CONSTRAINT C_PSP_FINANCIAL_TRANSACTION0
 CHECK (SETTLEMENT_TYPE_CD IN('ACH', 'Wire', 'Cash', 'CheckType', 'Other'));

ALTER TABLE PSP_FINANCIAL_TRANSACTION
 ADD CONSTRAINT C_PSP_FINANCIAL_TRANSACTION1
 CHECK (CREDIT_BANK_ACCOUNT_TYPE IN('Company', 'Employee', 'Intuit', 'TaxAgency'));

ALTER TABLE PSP_FINANCIAL_TRANSACTION
 ADD CONSTRAINT C_PSP_FINANCIAL_TRANSACTION2
 CHECK (DEBIT_BANK_ACCOUNT_TYPE IN('Company', 'Employee', 'Intuit', 'TaxAgency'));

ALTER TABLE PSP_BILLING_DETAIL
 ADD CONSTRAINT C_PSP_BILLING_DETAIL0
 CHECK (SERVICE_CD IN('DirectDeposit', 'Tax'));

ALTER TABLE PSP_BILLING_DETAIL
 ADD CONSTRAINT C_PSP_BILLING_DETAIL1
 CHECK (OFFERING_SERVICE_CHARGE_TYPE IN('PerPayroll', 'PerPaycheck', 'PerTransmission', 'ReversalFee', 'DebitReturnFee', 'ManualServicingFee', 'ChaseReportFeeUpTo3Payrolls', 'ChaseReportFeeUpTo6Payrolls', 'ChaseReportFeeUpTo15Payrolls', 'ChaseReportFeeUpTo20Payrolls', 'ChaseReportFeeOver20Payrolls', 'BankVerificationDebit', 'PaymentArrangementFee'));

 select 'finished DBUpgradeFrom_1.1.0.0_To_1.2.0.0.sql ' || to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') from dual