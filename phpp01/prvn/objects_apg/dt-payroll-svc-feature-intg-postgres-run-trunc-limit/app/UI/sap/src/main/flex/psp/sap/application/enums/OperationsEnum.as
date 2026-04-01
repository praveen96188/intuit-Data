package psp.sap.application.enums
{
	public class OperationsEnum
	{
		public static const ACCESS_APPLICATION:String = "AccessApplication";
		public static const LIMITS_UPDATE:String = "DDLimitUpdate";
		public static const STATUS_UPDATE:String = "DDStatusUpdate";
		public static const FUNDING_MODEL_UPDATE:String = "FundingModelUpdate";
		public static const VIEW_FULL_BANK_ACCOUNT_NUMBERS:String = "ViewFullBankAccountNumbers";
		public static const RECORD_ESCALATIONS:String = "EscalationCreditTransaction";
		public static const STRIKE_ADD:String = "StrikeAdd";
		public static const STRIKE_CANCEL:String = "StrikeCancel";
		public static const VIEW_LEDGER:String = "LedgerView";
		public static const VIEW_BANK_RETURNS:String = "BankReturnView";
		public static const UPDATE_OR_VIEW_SETTINGS:String = "SettingUpdate";
		public static const BANK_RETURN_UPDATE:String = "BankReturnUpdate";
		public static const SELECT_NON_STANDARD_SETTLEMENT_TYPE:String = "SelectNonStandardSettlementType";
		public static const EDIT_COMPANY_LEGAL_INFORMATION:String = "EditCompanyLegalInformation";
        public static const EDIT_COMPANY_CONTACT_INFORMATION:String = "EditCompanyContactInformation";
        public static const VIEW_TRANSACTION_HISTORY:String = "ViewTransactionHistory";
        public static const VIEW_VERIFICATION_DEBITS:String = "ViewVerificationDebits";
        public static const RESET_VERIFICATION_AMOUNTS:String = "ResetVerificationAmounts";
        public static const GENERATE_RANDOM_DEBITS:String = "GenerateRandomDebits";
        public static const GENERATE_PIN:String = "GeneratePin";
        public static const ADD_BANK_ACCOUNT_RANDOM_DEBITS:String = "AddBankAccountRandomDebits";
        public static const ADD_OFFERING:String = "AddOffering";
        public static const ADD_OFFER:String = "AddOffer";
        public static const EDIT_CHART_OF_ACCOUNTS:String = "EditChartOfAccounts";
        public static const EDIT_QB_FILE_ID:String = "EditQBFileID";
        public static const ADD_BANK_ACCOUNT_BY_PASS_RANDOM_DEBITS:String = "AddBankAccountByPassRandomDebits";
		public static const VIEW_PAYROLL_SCREEN:String = "ViewPayrollScreen";
        public static const ENTER_WIRE_EXPECTED_DATE:String = "EnterWireExpectedDate";
        public static const VIEW_SIGNUP_FRAUD_QUEUE:String = "ViewSignupFraudQueue";
        public static const REMOVE_FROM_SIGNUP_FRAUD_HOLD:String = "RemoveFromSignupFraudHold";
        public static const UPLOAD_TO_GEMS:String = "UploadToGems";
        public static const VIEW_OFX:String = "ViewOFX";
        public static const REQUEST_SECOND_OFFLOAD:String = "RequestSecondOffload";
        public static const VIEW_CHASE_REPORT:String = "ViewChaseReport";
        public static const PRINT_CHASE_REPORT:String = "PrintChaseReport";
        public static const AGENT_INITATES_REFUND_REBILL:String = "AgentInitiatesRefundRebill";
        public static const SAVE_PRINT_OFX:String = "SavePrintOFX";
        public static const CONFIRM_OFFLOAD:String = "ConfirmOffload";
        public static const REFUND_EMPLOYER_FRAUD_ESCALATION:String = "RefundEmployerFraudEscalation";
        public static const EDIT_TOKENS:String = "EditTokens";
        public static const EDIT_DEBUG_LOGGING:String = "EditDebugLogging";
        public static const EDIT_PROCESS_TRANSMISSIONS:String = "EditProcessTransmissions";
        public static const EDIT_ALLOW_TRANSMISSIONS:String = "EditAllowTransmissions";
        public static const REMOVE_TAX_TABLE_SUSPENSION:String = "RemoveTaxTableSuspension";
        public static const VIEW_OPERATOR_TAB:String = "ViewOperatorTab";
        public static const CREATE_RAF_FILE:String = "CreateRAFFile";
        public static const CREATE_ACH_FILE:String = "CreateACHFile";
        public static const ASSIGN_CHECKLIST:String = "AssignChecklist";
        public static const VIEW_OFFLOAD_STATUS:String = "ViewOffloadStatus";
        public static const ADD_VENDOR_PAYMENT_SERVICE:String = "AddVendorPaymentService";
        public static const ADD_AS400_COMPANY:String = "AddAS400Company";
        public static const ADD_CHECK_DISTRIBUTION_SERVICE:String = "AddCheckDistributionService";
        public static const VIEW_CHECK_PRINT_SIGNATURE:String = "ViewCheckPrintSignature";
        public static const ADD_UPDATE_CHECK_PRINT_SIGNATURE:String = "AddUpdateCheckPrintSignature";
        public static const VIEW_CHECK_PRINT_QUEUE:String = "ViewCheckPrintQueue";
        public static const UPDATE_CHECK_PRINT_BATCH_STATUS:String = "UpdateCheckPrintBatchStatus";
        public static const TAX_CREDITS_WOTC:String = "TaxCreditsWOTC";
        public static const SCHEDULE_ATF_EXTRACT:String = "ScheduleATFExtract";
        public static const LEDGER_OPERATIONS:String = "LedgerOperations";
        public static const CREATE_TOR:String = "CreateTOR";
        public static const VOID_TOR:String = "VoidTORTransaction";

        public static const VIEW_GLOBAL_ENROLLMENTS:String = "ViewGlobalEnrollments";
        public static const RESOLVE_EFTPS_REJECT:String = "ResolveEFTPSReject";
        public static const MANAGE_RAF_ENROLLMENT:String = "ManageRAFEnrollment";
        public static const MANUAL_EFTPS_ENROLLMENTS:String = "ManualEFTPSEnrollments";
        public static const VIEW_GLOBAL_TAX_PAYMENTS:String = "ViewGlobalTaxPayments";
        public static const MANAGE_TAX_PAYMENTS:String = "ManageTaxPayments";
        public static const VIEW_COMPANY_TAX_PAYMENTS:String = "ViewCompanyTaxPayments";
        public static const VIEW_TAX_LEDGER:String = "ViewTaxLedger";
        public static const VIEW_AGENCY_INFO:String = "ViewAgencyInfo";
        public static const CREATE_MANUAL_LEDGER_ENTRY:String = "CreateManualLedgerEntry";
        public static const VIEW_EE_PII:String = "ViewEEPII";
        public static const VIEW_OVERPAYMENTS:String = "ViewOverpayments";
        public static const VIEW_MONEY_MOVEMENT_SCREEN:String = "ViewMoneyMovementScreen";
        public static const CREATE_FLA:String = "CreateFLA";
        public static const RECALCULATE_LEDGER_BALANCES:String = "RecalculateLedgerBalances";
		public static const CREATE_GLOBAL_BOOK_TRANSFER:String = "CreateBookTransfer";
        public static const MANAGE_SUI_TAX_PAYMENTS:String = "ManageSUITaxPayments";
        public static const CREATE_ER_PENALTIES_AND_INTEREST_REFUNDS:String = "CreateERPenaltiesAndInterestRefunds";
        public static const ADD_MANUAL_FEE_TRANSACTIONS:String = "AddManualFeeTransactions";
        public static const CREATE_COURTESY_REFUNDS:String = "CreateCourtesyRefund";
        public static const EDIT_ACH_REG_FLAG:String = "EditACHRegFlag";
        public static const EDIT_AGENCY_IDS:String = "EditAgencyIDs";
        public static const EDIT_DEPOSIT_FREQ:String = "EditDepositFreq";
        public static const EDIT_FILER_TYPE:String = "EditFilerType";
        public static const EDIT_FILING_AMTS:String = "EditFilingAmts";
        public static const EDIT_FILING_AMTS_OTHER_QTR:String = "EditFilingAmtsOtherQtr";
        public static const EDIT_FILING_FLAGS:String = "EditFilingFlags";
        public static const EDIT_RATES_OTHER_LAWS:String = "EditRatesOtherLaws";
        public static const EDIT_SUI_RATE_CURR_QTR:String = "EditSUIRateCurrQTR";
        public static const EDIT_RATES_IN_OTHER_QTRS:String = "EditRatesInOtherQTRs";
        public static const RATE_SUPER_USER:String = "RateSuperUser";
        public static const EDIT_CANCELLATION_INFO:String = "EditCancellationInfo";
        public static const IP_BASED_FRAUD_FILTERING_VIEW:String = "IPBasedFraudFilteringView";


        public static const ADD_ASSISTED_BANK_ACCOUNT_PRE_BALF:String = "AddAssistedBankAccountPreBALF";
        public static const ADD_ASSISTED_BANK_ACCOUNT_BYPASS_RANDOM_DOLLAR_DEBIT_PRE_BALF:String = "AddAssistedBankAccountBypassRandomDollarDebitPreBALF";
        public static const ADD_BANK_ACCOUNT_BYPASS_RANDOM_DEBITS_POST_BALF:String = "AddBankAccountByPassRandomDebitsPostBALF";
        public static const ADD_BANK_ACCOUNT_RANDOM_DEBITS_POST_BALF:String = "AddBankAccountRandomDebitsPostBALF";
        public static const ADD_ASSISTED_OFFER_PRE_BALF:String = "AddAssistedOfferPreBALF";
        public static const ADD_ASSISTED_OFFER_POST_BALF:String = "AddAssistedOfferPostBALF";
        public static const ADD_ASSISTED_OFFERING_PRE_BALF:String = "AddAssistedOfferingPreBALF";
        public static const ADD_ASSISTED_OFFERING_POST_BALF:String = "AddAssistedOfferingPostBALF";
        public static const UPDATE_COMPLIANCE_DATA:String = "UpdateComplianceData";


		//For actions
		public static const CANCEL_DD_TRANSACTION:String = "DDTransactionCancel";
		public static const CREATE_REVERSAL_TRANSACTION:String = "CreateReversalTransaction";
		public static const ISSUE_REDEBIT_TRANSACTION:String = "IssueRedebitTransaction";
		public static const RECORD_NON_ACH_REDEBIT_TRANSACTION:String = "RecordNonACHRedebitTransaction";
		public static const CREATE_FEE_TRANSACTION:String = "CreateFeeTransaction";
		public static const CREATE_REFUND_TRANSACTION:String = "CreateRefundTransaction";
		public static const WRITE_OFF_BAD_DEBT_TRANSACTION:String = "WriteoffBadDebtTransaction";
		public static const WRITE_OFF_EMPLOYEE_BAD_DEBT_TRANSACTION:String = "WriteoffEmployeeBadDebtTransaction";
		public static const CANCEL_TRANSACTION:String = "TransactionCancel";
		public static const VOID_TRANSACTION:String = "VoidTransaction";
		public static const BOOK_TRANSFER_TRANSACTION:String = "BookTransferTransaction";
		public static const RECOVER_BAD_DEBT_TRANSACTION:String = "RecoverBadDebtTransaction";
		public static const RECORD_PREFUNDING_WIRE:String = "RecordPrefundingWire";
        public static const REFUND_ER_PAYABLE:String = "RefundERPayable";
        public static const DATA_SYNC_TOOL:String = "AccessDataSyncTool";
		public static const EMPLOYER_FEE_DEBIT_CANCEL:String = "EmployerFeeDebitCancel";

		//For auth
		public static const AUTH_ACCESS_APPLICATION:String = "AuthAccessApplication";
		public static const AUTH_ADD_UPDATE_USERS:String = "AuthAddUpdateUsers";
		public static const AUTH_REMOVE_USERS:String = "AuthRemoveUsers";
		public static const AUTH_ADD_UPDATE_HELP_DESK:String = "AuthAddUpdateHelpDesk";
		public static const AUTH_REMOVE_HELP_DESK:String = "AuthAddRemoveHelpDesk";
		public static const AUTH_ADD_UPDATE_DATACUSTODIAN:String = "AuthAddUpdateDataCustodian";
		public static const AUTH_REMOVE_DATACUSTODIAN:String = "AuthRemoveDataCustodian";

        //10.1
        public static const EDIT_PRINCIPAL_CONTACTS_DIY:String = "EditPrincipalContactsDIYOnly";
        public static const EDIT_ASSISTED_CONTACTS_PENDING:String = "EditAssistedPayrollContactsInPendingActivation";
        public static const EDIT_ASSISTED_CONTACTS_ACTIVE:String = "EditAssistedPayrollContactsInActiveStatus";
        public static const EDIT_ASSISTED_EIN_PENDING:String = "EditAssistedEINPendingActivation";
        public static const EDIT_ASSISTED_EIN_ACTIVE:String = "EditAssistedEINActive";
        public static const UPDATE_PRICE_TYPE:String = "AddUpdatePriceType";
        public static const ADD_DIY_EIN:String = "AddDIYEIN";
        public static const ADD_ASSISTED_EIN:String = "AddAssistedEIN";
        public static const ADD_TO_DIY_EIN:String = "AddToEINDIY";
        public static const ADD_TO_ASSISTED_EIN:String = "AddToEINAssisted";
        public static const MOVE_EIN_DIY_DIY:String = "MoveEINDIYDIY";
        public static const MOVE_EIN_DIY_ASSISTED:String = "MoveEINDIYAssisted";
        public static const DEACTIVATE_EIN:String = "DeactivateEIN";
        public static const DEACTIVATE_EIN_PENDING:String = "DeactivateEINPendingActivation";
        public static const DEACTIVATE_EIN_ACTIVE:String = "DeactivateEINActive";
        public static const REACTIVATE_EIN_DIY:String = "ReactivateEINDIY";
        public static const REACTIVATE_EIN_ASSISTED:String = "ReactivateEINAssisted";
        public static const EXECUTE_SQL:String = "ExecuteSQL";
        public static const VIEW_SYSTEM_PARAMETERS:String = "ViewSystemParameters";
        public static const EDIT_TAX_EXEMPT_FLAG:String = "EditTaxExemptFlag";
        public static const EDIT_ASSISTED_COMPANY_LEGAL_INFO_PENDING_ACTIVATION:String = "EditAssistedCompanyLegalInfoPendingActivation";
        public static const EDIT_ASSISTED_COMPANY_LEGAL_INFO:String = "EditAssistedCompanyLegalInfo";
        public static const EDIT_ASSISTED_COMPANY_PRINCIPAL_CONTACTS:String = "EditAssistedPrincipalContacts";
        public static const EDIT_ASSISTED_COMPANY_PRINCIPAL_CONTACTS_PENDING_ACTIVATION:String = "EditAssistedPrincipalContactsInPendingActivation";
        public static const EDIT_ENTITY_CHANGE_INFO:String = "EditEntityChangeInfo";

        //For View My Paycheck (VMP) service
        public static const VIEW_VMP_DATA:String = "ViewVMPData";
        public static const EDIT_VMP_DATA:String = "EditVMPData";
        public static const SEARCH_BY_SSN:String = "SearchBySSN";


        //RTB Job Action
        public static const EXECUTE_RTB_JOB:String = "ExecuteRTBJob";
        public static const DECRYPT_TEXT:String = "DecryptText";
        public static const EXECUTE_RTB_AUTOMATION_JOB:String = "ExecuteRTBAutomationJob";

        //Report file Download
        public static const REPORT_FILE_DOWNLOAD:String ="ReportFileDownload";
        public static const SAP_LIMIT_OVERRIDE:String ="SAPLimitOverride";

	}
}
