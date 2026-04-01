package psp.sap.viewmodel
{
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.controls.dataGridClasses.DataGridColumn;
    import mx.rpc.Responder;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.formatters.SAPCurrencyFormatters;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.CompanyBankAccount;
    import psp.sap.model.CompanyServiceState;
    import psp.sap.model.EmployeeInfo;
    import psp.sap.model.VendorInfo;
    import psp.sap.view.UIUtils;


    public class CompanyBankViewModel extends AbstractPartViewModel
	{


		[ArrayElementType("psp.sap.model.EmployeeInfo")]
		[Bindable] public var employees:ArrayCollection = new ArrayCollection();

        [ArrayElementType("psp.sap.model.VendorInfo")]
		[Bindable] public var vendors:ArrayCollection = new ArrayCollection();

		[ArrayElementType("psp.sap.model.RandomDebitTransaction")]
		[Bindable] public var randomVerificationTxns:ArrayCollection = new ArrayCollection();

		[Bindable] public var bankAccount:CompanyBankAccount;
		[Bindable] public var bankAccountExists:Boolean = false;
		[Bindable] public var isPendingRandomDebit:Boolean;
		[Bindable] public var isPendingRandomDebitString:String;
		[Bindable] public var isPendingVerification:Boolean = false;
        [Bindable] public var verificationAttemptLimit:int;
		[Bindable] public var bankAccountStatusText:String;
		[Bindable] public var bankAccountStatusColor:uint;
		[Bindable] public var showQBAccount:Boolean;
		[Bindable] public var employeeHeaderText:String;
        [Bindable] public var vendorHeaderText:String;

		[Bindable] public var verificationDebitsVisible:Boolean;

		//buttons
		[Bindable] public var canAddEdit:Boolean;
		[Bindable] public var mayAddEdit:Boolean;
		[Bindable] public var canResetAttempts:Boolean;
		[Bindable] public var mayResetAttempts:Boolean;
		[Bindable] public var canGenerateDebits:Boolean;
		[Bindable] public var mayGenerateDebits:Boolean;
        [Bindable] public var mayVerifyBank:Boolean;

		//different save actions
		private static const ACTION_RESET_ATTEMPTS:String = "actionResetAttempts";
		private static const ACTION_CREATE_RANDOM_DEBIT:String = "actionCreateRandomDebit";
        private static const ACTION_VERIFY_ACCOUNT:String ="actionVerifyAccount";
		protected var actionCallValue:String;

		public function CompanyBankViewModel()
		{
			super.label = CompanyInspectorPageEnum.COMPANY_BANK;
			super.reloadOnSave = true;
		}

		override protected function loadModelData():void {
			//special case:
			//1. some service calls that must be chained
			//2. some service calls that do not need to be chained
			//3. some service calls that will only be invoked based on previous results
			//because of this, we will take control of entire loadModelData and chain everything

			SAP.instance.companyService.getCompanyBankAccount(
					company.companyId, company.sourceSystemCd, new Responder(onCompanyBankAccountsLoaded, onLoadModelDataFaulted));

		}

		public function onCompanyBankAccountsLoaded(e:ResultEvent):void {
			bankAccount = e.result as CompanyBankAccount; //may be null

            if(bankAccount!= null){
                loadCount = 5;

                SAP.instance.companyService.getBankVerificationLimit(companyKey.companyId, companyKey.sourceSystemCd, createLoadModelDataResponder(function(e:ResultEvent):void {
                    verificationAttemptLimit = int(e.result);
                }));

				SAP.instance.companyService.isPendingRandomDebit(
                        company.companyId, company.sourceSystemCd,
                        createLoadModelDataResponder(onIsPendingRandomDebitLoaded));

                SAP.instance.companyService.getRandomDebitTransactions(
                        company.companyId, company.sourceSystemCd,
                        bankAccount.sourceBankAccountId,
                        createLoadModelDataResponder(onRandomTransactionsLoaded));
            }
            else {
                loadCount = 2;
            }

            SAP.instance.companyService.getEmployees(
                    company.companyId, company.sourceSystemCd,
                    createLoadModelDataResponder(onEmployeesLoaded));

            SAP.instance.companyService.getVendors(
                    company.companyId, company.sourceSystemCd,
                    createLoadModelDataResponder(onVendorsLoaded));

		}

		public function onEmployeesLoaded(e:ResultEvent):void {
			employees = e.result as ArrayCollection;
		}

        public function onVendorsLoaded(e:ResultEvent):void {
			vendors = e.result as ArrayCollection;
		}

		public function onIsPendingRandomDebitLoaded(e:ResultEvent):void {
			isPendingRandomDebit = e.result as Boolean;
		}

		public function onRandomTransactionsLoaded(e:ResultEvent):void {
			randomVerificationTxns = e.result as ArrayCollection;
		}

		override protected function initializeDefaults():void {
			bankAccountExists = false;
			isPendingVerification = false;
			bankAccountStatusText = "";
			showQBAccount = false;
			canAddEdit = false;
			canResetAttempts = false;
			canGenerateDebits = false;
		}

		override protected function initializeBackingProperties():void {
			//company
			if (bankAccount != null) {
				bankAccountExists = true;
				isPendingVerification = bankAccount.isPendingVerification;

				canResetAttempts = bankAccount.verifyRetryCount != 0;

				//if the bank has hit the limit, we will show a message to that effect; otherwise, the bank account status
				bankAccountStatusText = (bankAccount.verifyRetryCount >= verificationAttemptLimit)
								 ? "There have been " + verificationAttemptLimit + " or more attempts to verify this account"
								 : bankAccount.bankAccountStatus;

				isPendingRandomDebitString = isPendingRandomDebit ? "Yes" : "No";
				bankAccountStatusColor = (bankAccount.bankAccountStatusCd == "Active") ? UIUtils.COLOR_GREEN : UIUtils.COLOR_RED;

				canAddEdit = company.isQBDTCompany() && bankAccountExists;
				canGenerateDebits = company.isQBDTCompany() && !isPendingRandomDebit && !company.isMoneyMovementOnboardingEnabled;

				showQBAccount = company.isQBDTCompany();
			}


			//ee banks
			var sort:Sort = new Sort();
			sort.fields = [new SortField("lastName", true), new SortField("firstName", true)];
			employees.sort = sort;
			employees.refresh();

            //vendor banks
			sort = new Sort();
			sort.fields = [new SortField("name", true)];
			vendors.sort = sort;
			vendors.refresh();

			employeeHeaderText = employees.length.toString() + " Employee" + (employees.length != 1 ? "s" : "");
            vendorHeaderText = vendors.length.toString() + " Vendor" + (vendors.length != 1 ? "s" : "");

			//permissions
            if (company.companyServiceState == CompanyServiceState.AssistedActive) {
                mayAddEdit = SAP.canPerformOperation(OperationsEnum.ADD_BANK_ACCOUNT_RANDOM_DEBITS_POST_BALF);
            } else if (company.companyServiceState == CompanyServiceState.AssistedPending) {
                mayAddEdit = SAP.canPerformOperation(OperationsEnum.ADD_ASSISTED_BANK_ACCOUNT_PRE_BALF);
            } else {
                mayAddEdit = SAP.canPerformOperation(OperationsEnum.ADD_BANK_ACCOUNT_RANDOM_DEBITS);
            }

			mayGenerateDebits = SAP.canPerformOperation(OperationsEnum.GENERATE_RANDOM_DEBITS);
			mayResetAttempts = SAP.canPerformOperation(OperationsEnum.RESET_VERIFICATION_AMOUNTS);
			verificationDebitsVisible = SAP.canPerformOperation(OperationsEnum.VIEW_VERIFICATION_DEBITS) && isPendingVerification && !company.isMoneyMovementOnboardingEnabled;

            if (company.companyServiceState == CompanyServiceState.AssistedActive) {
                mayVerifyBank = SAP.canPerformOperation(OperationsEnum.ADD_BANK_ACCOUNT_BYPASS_RANDOM_DEBITS_POST_BALF);
            } else if (company.companyServiceState == CompanyServiceState.AssistedPending) {
                mayVerifyBank = SAP.canPerformOperation(OperationsEnum.ADD_ASSISTED_BANK_ACCOUNT_BYPASS_RANDOM_DOLLAR_DEBIT_PRE_BALF);
            } else {
                mayVerifyBank = SAP.canPerformOperation(OperationsEnum.ADD_BANK_ACCOUNT_BY_PASS_RANDOM_DEBITS);
            }
		}

        override protected function executeSave():void {
            if (actionCallValue == ACTION_RESET_ATTEMPTS && bankAccount != null) {
                SAP.instance.companyService.resetVerifyAttempts(
                        company.companyId, company.sourceSystemCd,
                        bankAccount.accountId,
                        createSaveResponder());
            }
            else if (actionCallValue == ACTION_CREATE_RANDOM_DEBIT && bankAccount != null) {
                SAP.instance.companyService.reinitiateRandomDebit(
                        company.companyId, company.sourceSystemCd,
                        bankAccount.accountId,
                        createSaveResponder(onReinitiateSucceeded));
            } else if (actionCallValue == ACTION_VERIFY_ACCOUNT && bankAccount != null) {
                SAP.instance.companyService.verifyCompanyBankAccount(company.companyId,
                        company.sourceSystemCd,
                        bankAccount.accountId,
                        createSaveResponder());
            }
        }


		protected function onReinitiateSucceeded(e:ResultEvent):void {
			dispatchEvent(new Event("onReinitiateSucceeded"));
		}

		public function createRandomDebits():void{
			actionCallValue = ACTION_CREATE_RANDOM_DEBIT;
			forceSave();
		}

		public function resetAttempts():void{
			actionCallValue = ACTION_RESET_ATTEMPTS;
			forceSave();
		}

        public function verifyAccount():void {
            actionCallValue = ACTION_VERIFY_ACCOUNT;
            forceSave();
        }

		public function goToEmployeeBankHistory(bankInfo:Object):void {
			topic.findPage(CompanyInspectorPageEnum.BANKS_EMPLOYEE_ACCOUNT_HISTORY).activatePage(BanksEmployeeAccountHistoryViewModel.createActivator(bankInfo as EmployeeInfo));
		}

        public function goToVendorBankHistory(bankInfo:Object):void {
			topic.findPage(CompanyInspectorPageEnum.BANKS_VENDOR_ACCOUNT_HISTORY).activatePage(BanksVendorAccountHistoryViewModel.createActivator(bankInfo as VendorInfo));
		}

		public function showAddAccount():void {
			addEdit(true);
		}

		public function showEditAccount():void {
			addEdit(false);
		}

		private function addEdit(add:Boolean):void {
			inspector.getPage(CompanyInspectorPageEnum.BANKS_ADD_ACCOUNT).activatePage(BanksAddBankAccountViewModel.createActivator(add));
		}

		public function goToCompanyBankHistory():void {
			inspector.getPage(CompanyInspectorPageEnum.BANKS_COMPANY_ACCOUNT_HISTORY).activate();
		}


		//DataGrid formatters
	    public static function formatOffloadDate(item:Object):String {
        	return (item.offloadedDate == null)? 'Pending Offload' : (SAPDateFormatters.dateFormatShort.format(item.offloadedDate)).toString();
        }

        //noinspection JSUnusedLocalSymbols
        public static function formatSettlementDate(item:Object,column:DataGridColumn):String {
            return (SAPDateFormatters.dateFormatShort.format(item.settlementDate)).toString();
        }

        //noinspection JSUnusedLocalSymbols
        public static function formatAmount1(item:Object, column:DataGridColumn):String{
            return (SAPCurrencyFormatters.defaultFormatter.format(item.amount1)).toString();
        }

        //noinspection JSUnusedLocalSymbols
        public static function formatAmount2(item:Object, column:DataGridColumn):String{
            return (SAPCurrencyFormatters.defaultFormatter.format(item.amount2)).toString();
        }

		//EE formatters
        public static function getFullNameDisplay(value:Object):String {
            var employeeInfo:EmployeeInfo = (value as EmployeeInfo);
            var fullName:String = employeeInfo.fullName;
            return fullName;
        }

	}
}