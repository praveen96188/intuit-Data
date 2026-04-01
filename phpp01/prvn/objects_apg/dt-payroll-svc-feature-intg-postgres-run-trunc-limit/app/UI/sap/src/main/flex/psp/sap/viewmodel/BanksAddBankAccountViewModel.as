package psp.sap.viewmodel
{
    import mx.events.PropertyChangeEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.BankAccountTypeEnum;
    import psp.sap.model.CompanyBankAccount;
    import psp.sap.model.CompanyServiceState;
    import psp.sap.validators.SAPRtnNumberValidator;
    import psp.sap.validators.SAPValidators;

    public class BanksAddBankAccountViewModel
    extends AbstractPartViewModel
    {

        [Bindable] [BackingProperty(context=true)] public var isAddBankAccount:Boolean = false;

        // defaults
        private static const DEFAULT_RTN_NUMBER:String = "";
        private static const DEFAULT_ACCT_NUMBER:String = "";
        private static const DEFAULT_ACCT_TYPE:String = BankAccountTypeEnum.CHECKING;
        private static const DEFAULT_BANK_NAME:String = "";
        private static const DEFAULT_MOVE_PENDING:Boolean = false;
        private static const DEFAULT_CREATE_RANDOM:Boolean = true;

        //data
        private var companyBankAccount:CompanyBankAccount;
        private var mAccountType:String;

        // properties/backing members
        [Bindable] [BackingProperty] public var rtnNumber:String;
        [Bindable] [BackingProperty] public var accountNumber:String;
        [Bindable] [BackingProperty] public var bankName:String;
        [Bindable] [BackingProperty] public var sourceBankName:String;
        [Bindable] [BackingProperty] public var movePending:Boolean;
        [Bindable] [BackingProperty] public var createRandomDebits:Boolean;

        [Bindable] public var canAddAgentVerified:Boolean;
        [Bindable] public var mayAddAgentVerified:Boolean;

        public function BanksAddBankAccountViewModel()
        {
            this.label = CompanyInspectorPageEnum.BANKS_ADD_ACCOUNT;
            this.reloadOnSave = true;

            var rtnNumberValidator:SAPRtnNumberValidator = SAPValidators.createRtnNumberValidator(this, "rtnNumber", true);
            rtnNumberValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
            rtnNumberValidator.trigger = this;
            validators.push(rtnNumberValidator);
            validators.push(SAPValidators.createRequiredFieldValidator(this, "rtnNumber", true));
            validators.push(SAPValidators.createRequiredFieldValidator(this, "accountNumber", true));
            validators.push(SAPValidators.createRequiredFieldValidator(this, "bankName", true));
            validators.push(SAPValidators.createRequiredFieldValidator(this, "sourceBankName", true));
        }

        public static function createActivator(isAddBankAccount:Boolean):Object {
            return {"isAddBankAccount":isAddBankAccount};
        }

        [Bindable] [BackingProperty]
        public function get accountType():String {
            return mAccountType;
        }

        public function set accountType(value:String):void {
            if (value == null){
                value = DEFAULT_ACCT_TYPE;
            }

            mAccountType = value;
        }

        [Bindable ("propertyChange")]
        public function get accountTypes():Array {
            return BankAccountTypeEnum.accountTypes;
        }

        override protected function loadModelData():void {
            SAP.instance.companyService.getCompanyBankAccount(
                    company.companyId, company.sourceSystemCd, createLoadModelDataResponder(onCompanyBankAccountsLoaded));
        }

        public function onCompanyBankAccountsLoaded(e:ResultEvent):void {
            companyBankAccount = e.result as CompanyBankAccount; //may be null
        }

        override protected function initializeBackingProperties():void {
            if(isAddBankAccount){
                rtnNumber = DEFAULT_RTN_NUMBER;
                accountNumber = DEFAULT_ACCT_NUMBER;
                accountType = DEFAULT_ACCT_TYPE;
                bankName = DEFAULT_BANK_NAME;
                sourceBankName = DEFAULT_BANK_NAME;
            }
            else{
                rtnNumber = companyBankAccount.routingNumber;
                accountNumber = companyBankAccount.accountNumber;
                accountType = companyBankAccount.accountType;
                bankName = companyBankAccount.bankName;
                sourceBankName = companyBankAccount.sourceBankAccountName;
            }
            movePending = DEFAULT_MOVE_PENDING;
            createRandomDebits = DEFAULT_CREATE_RANDOM;

            if (company.companyServiceState == CompanyServiceState.AssistedActive) {
                mayAddAgentVerified = SAP.canPerformOperation(OperationsEnum.ADD_BANK_ACCOUNT_BYPASS_RANDOM_DEBITS_POST_BALF);
            } else if (company.companyServiceState == CompanyServiceState.AssistedPending) {
                mayAddAgentVerified = SAP.canPerformOperation(OperationsEnum.ADD_ASSISTED_BANK_ACCOUNT_BYPASS_RANDOM_DOLLAR_DEBIT_PRE_BALF);
            } else {
                mayAddAgentVerified = SAP.canPerformOperation(OperationsEnum.ADD_BANK_ACCOUNT_BY_PASS_RANDOM_DEBITS);
            }

            canAddAgentVerified = isAddBankAccount && mayAddAgentVerified;
        }

        override protected function executeSave():void {
            // save the new account

            //Differentiating if is "Add Bank Account" or "Edit bank account"
            if(isAddBankAccount)
            {
                SAP.instance.companyService.addBankAccount(company.sourceSystemCd,
                        company.companyId,
                        (companyBankAccount != null) ? companyBankAccount.accountId : null,
                        sourceBankName,
                        accountNumber,
                        rtnNumber,
                        accountType,
                        bankName,
                        createRandomDebits,
                        true,
                        movePending && !createRandomDebits,
                        createSaveResponder());
            } else {
                SAP.instance.companyService.editBankAccount(company.sourceSystemCd,
                        company.companyId,
                        (companyBankAccount != null) ? companyBankAccount.accountId : null,
                        sourceBankName,
                        accountNumber,
                        rtnNumber,
                        accountType,
                        bankName,
                        createSaveResponder());
            }
        }

        public function isCurrentAccount():Boolean {
            return rtnNumber == companyBankAccount.routingNumber &&
                   accountNumber == companyBankAccount.accountNumber &&
                   accountType == companyBankAccount.accountType;
        }

    }
}
