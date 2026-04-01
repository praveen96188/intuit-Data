DROP TABLE IF EXISTS TEMP_PSP_AUTH_OPERATION CASCADE;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------

CREATE TABLE TEMP_PSP_AUTH_OPERATION (LIKE PSP_AUTH_OPERATION INCLUDING ALL) ;
--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'BankReturnUpdate', 0, 'Update Bank Returns', 'Manually Update Bank Returns')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AccessApplication', 0, 'Access Application', 'Access application')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'DDLimitUpdate', 0, 'Update DD Limits', 'Update direct deposit $ limits')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'FundingModelUpdate', 0, 'Update DD Funding Model', 'Update ach funding model')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'DDStatusUpdate', 0, 'Update DD Status', 'Update dd subscription status')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'StrikeAdd', 0, 'Add Strikes', 'Add new strike')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'StrikeCancel', 0, 'Cancel Strikes', 'Cancel existing strike')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'SettingUpdate', 0, 'Access DD Settings', 'View or update settings')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewFullBankAccountNumbers', 0, 'View Full Bank Account Numbers', 'View full bank account numbers')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'BankReturnView', 0, 'Access Bank Returns', 'View or search bank returns')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'RecordNonACHRedebitTransaction', 0, 'Record Non-ACH Payments', 'Record a non-ach redebit (wire)')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CreateFeeTransaction', 0, 'Create Fees', 'Create a fee')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CreateReversalTransaction', 0, 'Create Reversals', 'Reversal')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'DDTransactionCancel', 0, 'Cancel Paychecks', 'Cancel employee transactions (partial or entire payroll)')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'TransactionCancel', 0, 'Cancel Non-Paycheck Txs', 'Cancel non-paycheck transactions')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'LedgerView', 0, 'Access Ledger', 'View ledger')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'SelectNonStandardSettlementType', 0, 'Select Non Standard Settlement Types', 'Select other than default settlement type')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CreateRefundTransaction', 0, 'Create Refunds', 'Issue a refund')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'VoidTransaction', 0, 'Void Transactions', 'Void transaction')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'BookTransferTransaction', 0, 'Create Book Transfers', 'Book transfer')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ActivateBankAccount', 0, 'Activate Bank Accounts', 'Activate account')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'WriteoffBadDebtTransaction', 0, 'Create Write offs', 'Writeoff bad debt')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'WriteoffEmployeeBadDebtTransaction', 0, 'Create Employee Reversal Write offs', 'Writeoff reversal bad debt')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'RecoverBadDebtTransaction', 0, 'Record Bad Debt Recovery', 'Recover bad debt')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EscalationCreditTransaction', 0, 'Record Escalations', 'Escalation credit')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'IssueRedebitTransaction', 0, 'Issue ACH Redebit', 'Issue a redebit (ACH)')
;





INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AuthAccessApplication', 0, 'Access Auth Application', 'Access Auth Application')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AuthAddUpdateUsers', 0, 'Add and Update Users', 'Add and Update Users')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AuthRemoveUsers', 0, 'Remove Users', 'Remove Users')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AuthAddUpdateHelpDesk', 0, 'Add and Update Help Desk Auth Users', 'Add and Update Help Desk Auth Users')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AuthAddRemoveHelpDesk', 0, 'Remove Help Desk Auth Users', 'Remove Help Desk Auth Users')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AuthAddUpdateDataCustodian', 0, 'Add and Update Data Custodian Auth Users', 'Add and Update Data Custodian Auth Users')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AuthRemoveDataCustodian', 0, 'Remove Data Custodian Auth Users', 'Remove Data Custodian Auth Users')
;






INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditCompanyLegalInformation', 0, 'Edit Company Legal Information', 'Edit Company Legal Information')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditCompanyContactInformation', 0, 'Edit Company Contact Information', 'Edit Company Contact Information')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewTransactionHistory', 0, 'View Transaction History', 'View Transaction History')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewVerificationDebits', 0, 'View Verification Debits', 'View Verification Debits')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ResetVerificationAmounts', 0, 'Reset Debit Attempt Counter', 'Reset Debit Attempt Counter')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'GenerateRandomDebits', 0, 'Regenerate Random Debits', 'Regenerate Random Debits')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'GeneratePin', 0, 'Manage and Generate Random PIN', 'Manage and Generate Random PIN')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddBankAccountRandomDebits', 0, 'Add Bank Account - Random Debits', 'Add Bank Account - Random Debits')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddBankAccountByPassRandomDebits', 0, 'Add Bank Account - Bypass Random Debits', 'Add Bank Account - Bypass Random Debits')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddOffering', 0, 'Add Offering', 'Add Offering')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddOffer', 0, 'Add Offer', 'Add Offer')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditChartOfAccounts', 0, 'Edit Chart Of Accounts', 'Edit Chart Of Accounts')
;


INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewPayrollScreen', 0, 'View Payroll Screen', 'View Payroll Screen')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EnterWireExpectedDate', 0, 'Enter Wire Expected Date', 'Enter Wire Expected Date')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewSignupFraudQueue', 0, 'View Signup Fraud Queue', 'View Signup Fraud Queue')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'RemoveFromSignupFraudHold', 0, 'Remove From Signup Fraud Hold', 'Remove From Signup Fraud Hold')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'UploadToGems', 0, 'Upload To Gems', 'Upload To Gems')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewOFX', 0, 'View OFX', 'View OFX')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'RequestSecondOffload', 0, 'Request Second Offload', 'Request Second Offload')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewChaseReport', 0, 'View Chase Report', 'View Chase Report')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'PrintChaseReport', 0, 'Print Chase Report', 'Print Chase Report')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AgentInitiatesRefundRebill', 0, 'Agent Initiates Refund Rebill', 'Agent Initiates Refund Rebill')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'SavePrintOFX', 0, 'Save and Print OFX', 'Save and Print OFX')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ConfirmOffload', 0, 'Confirm Offload', 'Confirm Offload')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'RefundEmployerFraudEscalation', 0, 'Refund Employer for Fraud or Escalation', 'Refund Employer for Fraud or Escalation')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditTokens', 0, 'Edit Tokens', 'Edit Tokens')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditDebugLogging', 0, 'Edit Debug Logging', 'Edit Debug Logging')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'RecordPrefundingWire', 0, 'Record Prefunding Wire', 'Record Prefunding Wire')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AssignChecklist', 0, 'Assign Checklist', 'Assign checklist to agents')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CreateRAFFile', 0, 'Create RAF File', 'Create RAF File')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CreateACHFile', 0, 'Create ACH File', 'Create ACH File')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewOperatorTab', 0, 'View Operator Tab', 'View Operator Tab')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewOffloadStatus', 0, 'View Offload Status', 'View Offload Status')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddVendorPaymentService', 0, 'Add Vendor Payment Service', 'Add Vendor Payment Service')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddAS400Company', 0, 'Add AS400 Company', 'Add AS400 Company')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddCheckDistributionService', 0, 'Add Check Distribution Service', 'Add Check Distribution Service')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewCheckPrintSignature', 0, 'View Check Print Signature', 'View Check Print Signature')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddUpdateCheckPrintSignature', 0, 'Add Update Check Print Signature', 'Add Update Check Print Signature')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewCheckPrintQueue', 0, 'View Check Print Queue', 'View Check Print Queue')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'UpdateCheckPrintBatchStatus', 0, 'Update Check Print Batch Status', 'Update Check Print Batch Status')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'TaxCreditsWOTC', 0, 'View Tax Credits WOTC Screens', 'View Tax Credits WOTC Screens')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CancelCloud', 0, 'Cancel Cloud', 'Cancel Cloud service')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'RefundERPayable', 0, 'Refund ER Payable', 'Refund ER Payable')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewGlobalEnrollments', 0, 'View Global Enrollments', 'View Global Enrollments')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ResolveEFTPSReject', 0, 'Resolve EFTPS Reject', 'Resolve EFTPS Reject')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ManageRAFEnrollment', 0, 'Manage RAF Enrollment', 'Manage RAF Enrollment')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewGlobalTaxPayments', 0, 'View Global Tax Payments', 'View Global Tax Payments')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ManageTaxPayments', 0, 'Manage Tax Payments', 'Manage Tax Payments')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ManageSUITaxPayments', 0, 'Manage SUI Tax Payments', 'Manage SUI Tax Payments')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewCompanyTaxPayments', 0, 'View Company Tax Payments', 'View Company Tax Payments')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewTaxLedger', 0, 'View Tax Ledger', 'View Tax Ledger')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewAgencyInfo', 0, 'View Agency Info', 'View Agency Info')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CreateManualLedgerEntry', 0, 'Create Manual Ledger Entry', 'Create Manual Ledger Entry')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewEEPII', 0, 'View EE PII', 'View Employee PII')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewOverpayments', 0, 'View Overpayments', 'View Overpayments')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'UpdateCancelTermCompany', 0, 'Update Cancel/Term Company', 'Make updates to companies that are cancelled or termed')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AccessDataSyncTool', 0, 'Access Data Sync Tool', 'Access the data sync tool to push/stop items')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewMoneyMovementScreen', 0, 'View Money Movement Screen', 'View Money Movement Screen')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditPrincipalContactsDIYOnly', 0, 'Edit Principal Contacts (DIY Only)', 'Edit Principal Contacts on DIY EOC - No Services')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditAssistedPayrollContactsInPendingActivation', 0, 'Edit Assisted Payroll Contacts in Pending Activation', 'Edit Assisted Payroll Contacts in Pending Activation')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditAssistedPayrollContactsInActiveStatus', 0, 'Edit Assisted Payroll Contacts in Active Status', 'Edit Assisted Payroll Contacts in Active Status')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditAssistedEINPendingActivation', 0, 'Edit Assisted EIN in Pending Activation Status', 'Edit Assisted EIN in Pending Activation Status')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditAssistedEINActive', 0, 'Edit Assisted EIN in Active Status', 'Edit Assisted EIN in Active Status')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddUpdatePriceType', 0, 'Add or Update Price Type for Assisted EOC before Service ', 'Add or Update Price Type for Assisted EOC before Service ')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddDIYEIN', 0, 'Add DIY EIN', 'Add DIY EIN')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddAssistedEIN', 0, 'Add Assisted EIN', 'Add Assisted EIN')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddToEINDIY', 0, 'Add to EIN DIY EOC', 'Add to EIN DIY EOC')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddToEINAssisted', 0, 'Add to EIN Assisted EOC', 'Add to EIN Assisted EOC')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'MoveEINDIYDIY', 0, 'Move EIN from DIY EOC to DIY EOC', 'Move EIN from DIY EOC to DIY EOC')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'MoveEINDIYAssisted', 0, 'Move EIN from DIY to Assisted EOC', 'Move EIN from DIY to Assisted EOC')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'DeactivateEIN', 0, 'Deactivate EIN for DIY EOC or Assisted EOC (before Service Signup)', 'Deactivate EIN for DIY EOC or Assisted EOC (before Service Signup)')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'DeactivateEINPendingActivation', 0, 'Deactivate EIN Assisted Service in Pending Activation', 'Deactivate EIN Assisted Service in Pending Activation')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'DeactivateEINActive', 0, 'Deactivate EIN Assisted Service Active', 'Deactivate EIN Assisted Service Active')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ReactivateEINDIY', 0, 'Reactivate EIN on DIY EOC', 'Reactivate EIN on DIY EOC')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ReactivateEINAssisted', 0, 'Reactivate EIN Assisted EOC', 'Reactivate EIN Assisted EOC')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CreateFLA', 0, 'Create FLA', 'Create FLA')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'RecalculateLedgerBalances', 0, 'Recalculate Ledger Balances', 'Recalculate Ledger Balances')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CreateBookTransfer', 0, 'Create Global Book Transfer', 'Create Global Book Transfer')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ExecuteSQL', 0, 'Execute SQL', 'Execute SQL')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewSystemParameters', 0, 'View System Parameters', 'View System Parameters')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditTaxExemptFlag', 0, 'Edit Tax Exempt Flag', 'Edit Tax Exempt Flag')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditAssistedCompanyLegalInfoPendingActivation', 0, 'Edit Assisted CompanyLegalInfo PendingActivation', 'Edit Assisted Legal Info in Pending Activation')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditAssistedCompanyLegalInfo', 0, 'Edit Assisted CompanyLegalInfo', 'Edit Assisted Legal Info')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditAssistedPrincipalContacts', 0, 'Edit Assisted Principal Contacts', 'Edit Assisted Principal Contacts')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditAssistedPrincipalContactsInPendingActivation', 0, 'Edit Assisted Principal Contacts in Pending Activation', 'Edit Assisted Principal Contacts in Pending Activation')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CreateERPenaltiesAndInterestRefunds', 0, 'Create ER Penalties And Interest Refunds', 'Create Employer Penalties And Interest Refunds')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddManualFeeTransactions', 0, 'Add Manual Fee Transactions', 'Add Manual Fee Transactions')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CreateMultipleBackdatingRefunds', 0, 'Create Multiple Backdating Refunds', 'Create Multiple Backdating Refunds')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CreateCourtesyRefund', 0, 'Create Courtesy Refund', 'Create Courtesy Refund')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditProcessTransmissions', 0, 'Edit Process Transmissions', 'Edit Process Transmissions')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditAllowTransmissions', 0, 'Edit Allow QBDT Transmissions', 'Edit Allow QBDT Transmissions')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddRestrictedOffer', 0, 'Add Restricted Offer', 'Add Restricted Offer')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'UpdateComplianceData', 0, 'Update Compliance Data', 'Update Compliance Data')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddAssistedBankAccountPreBALF', 0, 'Add Assisted Bank Account Pre BALF', 'Add Bank Account to Assisted Company Prior to Customer BALF')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddAssistedBankAccountBypassRandomDollarDebitPreBALF', 0, 'Add Assisted Bank Account Bypass Random Dollar Debit Pre BALF', 'Add Bank Account Bypassing Random Debits to an Assisted Company Prior to Customer BALF')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddBankAccountByPassRandomDebitsPostBALF', 0, 'Add Assisted Bank Account Bypass Random Debits Post BALF', 'Add Bank Account Bypassing Random Debits to an Assisted Company After the Customer BALFs')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddBankAccountRandomDebitsPostBALF', 0, 'AddBankAccountRandomDebitsPostBALF', 'Add Bank Account to Assisted Company After Customer BALFs')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddAssistedOfferPreBALF', 0, 'Add Assisted Offer Pre BALF', 'Add Offer to Assisted Company prior to customer BALF')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddAssistedOfferPostBALF', 0, 'Add Assisted Offer Post BALF', 'Add Offer to Assisted Company after customer BALFs')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddAssistedOfferingPreBALF', 0, 'Add Assisted Offering Pre BALF', 'Add Offering to Assisted Company prior to customer BALF')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AddAssistedOfferingPostBALF', 0, 'Add Assisted Offering Post BALF', 'Add Offering to Assisted Company after customer BALFs')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditQBFileID', 0, 'Edit QuickBooks File ID', 'Update the File ID used for blocking submissions when it may be from the wrong QB file')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ScheduleATFExtract', 0, 'Schedule ATF Extract', 'Schedule an ATF Quarterly Data extract to run from Flux')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'LedgerOperations', 0, 'Manage Ledger Operations', 'Create and start ledger operations such as Credit Reduction')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EmployerFeeDebitCancel', 0, 'Cancel Employer Fee Debits', 'Cancel employer fee debits')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CreateTOR', 0, 'Create TOR', 'Create a TOR for a specific company/template/quarter')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ManualEFTPSEnrollments', 0, 'Manual EFTPS Enrollments', 'Create a manual EFTPS enrollment that does not impact payments')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditEntityChangeInfo', 0, 'Edit Entity Change date and flag', 'Edit Entity Change date and flag')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditCancellationInfo', 0, 'Edit Cancellation Info', 'Edit Cancellation Info')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditACHRegFlag', 0, 'Edit ACH Registered Flag', 'Edit ACH Registered Flag')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditAgencyIDs', 0, 'Edit Agency IDs', 'Edit Agency IDs')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditDepositFreq', 0, 'Edit Deposit Frequency', 'Edit Deposit Frequency')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditFilerType', 0, 'Edit Filer Type', 'Edit Filer Type')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditFilingAmts', 0, 'Edit Filing Amounts', 'Edit Filing Amounts')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditFilingAmtsOtherQtr', 0, 'Edit Filing Amounts in Other Quarter', 'Edit Filing Amounts in Other Quarter')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditFilingFlags', 0, 'Edit Filing Flags', 'Edit Filing Flags')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditRatesOtherLaws', 0, 'Edit Rates of Other Laws', 'Edit Rates of Non-SUI Laws')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditSUIRateCurrQTR', 0, 'Edit Rates in Current Quarter', 'Edit Rates in Current Quarter')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditRatesInOtherQTRs', 0, 'Edit Rates in Other Quarters', 'Edit Rates in Q-1, Q, and Q+1')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'RateSuperUser', 0, 'Rate Super User', 'Ignore blackouts, min/max, edit full table, and control QB push')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ViewVMPData', 0, 'View VMP Data', 'View VMP Data')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'EditVMPData', 0, 'Edit VMP Data', 'Edit VMP Data')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'SearchBySSN', 0, 'Search By SSN', 'Search By SSN')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'SAPLimitOverride', 0, 'Ability to override SAP limits', 'Ability to override SAP limits')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'VoidTORTransaction', 0, 'Void TOR Transaction', 'Void TOR transaction')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'RemoveTaxTableSuspension', 0, 'Remove Tax Table Suspension', 'Remove Tax Table Suspension')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ExecuteRTBJob', 0, 'Execute RTB Job Action', 'Execute RTB Job Action')
;

INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
  'DecryptText', 0, 'Decrypt the encrypted text', 'Decrypt the encrypted text')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
  'IPBasedFraudFilteringView', 0, 'IP Based Fraud Filtering View', 'IP Based Fraud Filtering View')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ExecuteRTBAutomationJob', 0, 'Execute RTB Automation Job Action', 'Execute RTB Automation Job Action')
;
INSERT INTO TEMP_PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'ReportFileDownload', 0, 'Download file generated by batch jobs', 'Download file generated by batch jobs')
;


--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_AUTH_OPERATION
   (OPERATION_ID, VERSION, NAME, DESCRIPTION)
SELECT
    OPERATION_ID, VERSION, NAME, DESCRIPTION
FROM
   TEMP_PSP_AUTH_OPERATION tt
WHERE
   tt.OPERATION_ID NOT IN (SELECT OPERATION_ID FROM PSP_AUTH_OPERATION)

;

DELETE FROM PSP_AUTH_OPERATION
WHERE OPERATION_ID NOT IN (SELECT OPERATION_ID FROM TEMP_PSP_AUTH_OPERATION)

;

UPDATE PSP_AUTH_OPERATION rt
SET (VERSION, NAME, DESCRIPTION) =
(SELECT tt.VERSION, tt.NAME, tt.DESCRIPTION
 FROM TEMP_PSP_AUTH_OPERATION tt WHERE tt.OPERATION_ID = rt.OPERATION_ID)
;

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_AUTH_OPERATION

;
COMMIT
