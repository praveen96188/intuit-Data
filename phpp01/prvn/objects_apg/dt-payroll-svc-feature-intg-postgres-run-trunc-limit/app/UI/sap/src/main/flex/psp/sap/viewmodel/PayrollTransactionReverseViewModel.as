package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    import mx.events.PropertyChangeEvent;
    import mx.formatters.DateFormatter;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.CompanyBankAccount;
    import psp.sap.model.PayrollEmployeeTransaction;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.PayrollRunStatusTypeEnum;
    import psp.sap.model.SettlementTypeEnum;
    import psp.sap.validators.SAPDateValidator;
    import psp.sap.validators.SAPValidators;

    public class PayrollTransactionReverseViewModel
    extends AbstractPartViewModel
    {

        [Bindable] [BackingProperty (context=true)] public var payrollRun:PayrollRun;

        private static const millisecondsPerDay:int = 1000 * 60 * 60 * 24;
        private const DAYSTOADD:int = 4;
        private const SATURDAY:String = "6";
        private const SUNDAY:String = "0";        
        private const REQUESTED:String = "(Customer Requested)";
        private const NOTREQUESTED:String = "(Not Customer Requested)";
        private var dateFormatter:DateFormatter;
        protected var DEFAULT_SETTLEMENT_TYPE:SettlementTypeEnum = SettlementTypeEnum.ACH;
        private var mSettlementType:SettlementTypeEnum;
        private var mInitiateReversal:Boolean;
        private var mSettlementDateLabelVisible:Boolean;
        private var mTransactionDate:String;
        private var mDateValidator:SAPDateValidator;

        [Bindable] public var chargeReversalFee:Boolean;
        [Bindable] public var selected:Boolean = false;
        [Bindable] public var checkBoxEnabled:Boolean = true;
        [Bindable] public var isACHType:Boolean = false;
        [Bindable] public var settlementTypes:Array;
        [Bindable] public var custRequested:String;
        [Bindable] public var payrollRunStatus:PayrollRunStatusTypeEnum;

        // has active bank account
        [Bindable]  public var hasActiveBankAccount:Boolean = false;

        public static function createActivator(payrollRun:PayrollRun):Object {
            return {"payrollRun":payrollRun};
        }

        
        [Bindable]
        public function get initiateReversal():Boolean {
            return mInitiateReversal;
        }

        public function set initiateReversal(value:Boolean):void {
            mInitiateReversal = value;
            if (value) {
                custRequested = NOTREQUESTED;
                selectAll = true;
                enableAll = false;
            }
            else {
                custRequested = REQUESTED;
                selectAll = false;
                enableAll = true;
            }
        }

        [Bindable]
        public function get settlementType():SettlementTypeEnum {
            return mSettlementType;
        }

        public function set settlementType(value:SettlementTypeEnum):void {
            if (value == null)
                value = DEFAULT_SETTLEMENT_TYPE;

            mSettlementType = value;
            isACHType = (mSettlementType == SettlementTypeEnum.ACH);
            if(isACHType){
            	mDateValidator.enabled = false;
            }
            else{
            	mDateValidator.enabled = true;
            }
            
            updateSelectAllAndCanSave();
        }

        [Bindable ("propertyChange")]
        public function get canSelectNonStandardSettlementTypes():Boolean {
            return SAP.canPerformOperation(OperationsEnum.SELECT_NON_STANDARD_SETTLEMENT_TYPE);
        }

        [Bindable ("propertyChange")]
        public function get reversalExpectedDate():String {

            var tempDate:Date = SAP.instance.PSPDate; //logic is settlementDate + 4 business days.
            return dateFormatter.format(addBusinessDays(tempDate, DAYSTOADD));
        }

        private function addBusinessDays(value:Date, numDays:int):Date {
            for (var i:int = 0; i < numDays; i++) {
                value.setTime(value.getTime() + millisecondsPerDay);
                if (value.day.toString() == SATURDAY || value.day.toString() == SUNDAY) {
                    i--;
                }
            }
            return value;
        }

        // date field
        [Bindable]
        public function get transactionDate():String {
            return mTransactionDate;
        }

        public function set transactionDate(value:String):void {
            mTransactionDate = dateFormatter.format(value);
            updateSelectAllAndCanSave();
        }

        // validators
        [Bindable ("propertyChange")]
        public function get dateValidator():SAPDateValidator {
            return mDateValidator;
        }

        [Bindable]
        public var accountNumber:String;

        public function PayrollTransactionReverseViewModel()
        {
            this.reloadOnSave = true;
            settlementTypes = SettlementTypeEnum.values;
            DEFAULT_SETTLEMENT_TYPE = SettlementTypeEnum.ACH;
            chargeReversalFee = true;
            dateFormatter = new DateFormatter();
            dateFormatter.formatString = SAP.instance.configuration.dateFormatShort;
            mDateValidator = SAPValidators.createDateValidator(this, "transactionDate", true, 45, 0);
            mDateValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
            mDateValidator.trigger = this;
            validators.push(mDateValidator);
        }

        [ArrayElementType("psp.sap.model.PayrollEmployeeTransaction")]
        private var mEmployeeTransactions:ArrayCollection =  null;

        [Bindable]
        public function get employeeTransactions():ArrayCollection {
            return mEmployeeTransactions;
        }

        public function set employeeTransactions(value:ArrayCollection):void {
            // only show completed or executed transactions ** logic from DDREPUI ReverseDirectDepositTransactionsAction.java:97
            var filteredCollection:ArrayCollection = new ArrayCollection();
            for each(var transaction:PayrollEmployeeTransaction in value){
                if(transaction.txnType == "EmployeeDdCredit" &&
                   (transaction.status == "Executed" || transaction.status == "Completed")) {
                    // add selected property
                    transaction.selected = false;
                    filteredCollection.addItem(transaction);
                }
            }
            mEmployeeTransactions = filteredCollection;
        }

        override protected function loadModelData():void {
            loadCount = 2;
            SAP.instance.payrollRunService.findReversableEmployeeTransactions(this.company.companyId,
                    this.company.sourceSystemCd,
                    payrollRun.sourcePayRunId,
                    null,
                    null,
                    createLoadModelDataResponder(onEmployeeTransactionResults));
            SAP.instance.companyService.getActiveBankAccount(company.sourceSystemCd, company.companyId, createLoadModelDataResponder(onActiveBankAccountLoaded));
        }

        public function onEmployeeTransactionResults(e:ResultEvent):void {
            employeeTransactions = ArrayCollection(e.result);
            employeeTransactions.addEventListener(CollectionEvent.COLLECTION_CHANGE, onCollectionChanged, false, 0, true);
        }

        private function onActiveBankAccountLoaded(e:ResultEvent):void {
            var companyBankAccount:CompanyBankAccount = CompanyBankAccount(e.result);
            hasActiveBankAccount = companyBankAccount != null;
            if (hasActiveBankAccount) {
                accountNumber = companyBankAccount.accountNumber;
            }
        }


        /**
         * Reset inputs to default values
         */
        override protected function initializeDefaults():void {            
            // this line makes sure that the initiateReversal = false will set the other properties with in it
            initiateReversal = true;
            initiateReversal = false;
            
            chargeReversalFee = true;
            settlementType = DEFAULT_SETTLEMENT_TYPE;
            transactionDate = "";            
            updateSelectAllAndCanSave();
        }


        override protected function initializeBackingProperties():void {
            payrollRunStatus = new PayrollRunStatusTypeEnum(payrollRun.payrollRunStatus);
            updateSelectAllAndCanSave();
        }

        [Bindable(event="contextPropertyChanged")]
        public function get pageLabel():String {
            var paycheckDate:String = payrollRun == null ? "" : SAPDateFormatters.dateFormatMedium.format(payrollRun.paycheckDate);
            return "Reverse Direct Deposit Transactions for Paycheck Date: " + paycheckDate;
        }

        /**
         * Update each item and the global value bound to the checkbox
         */
        public function set selectAll(value:Boolean):void {
            selected = value;
            for each(var txn:PayrollEmployeeTransaction in employeeTransactions){
                txn.selected = value;
            }
        }

        public function set enableAll(value:Boolean):void {
            checkBoxEnabled = value;
            for each(var txn:PayrollEmployeeTransaction in employeeTransactions){
                txn.enabled = value;
            }
        }

        /**
         * update the header checkbox with the check box lines
         */
        private function onCollectionChanged(event:CollectionEvent): void {
            for each(var item:PropertyChangeEvent in event.items){
                if(item.property == "selected"){
                    updateSelectAllAndCanSave();
                    // we only need to do this once
                    return;
                }
            }
        }

        /**
         * Update the checkbox header and cancel button
         * This is combined because I did not see a reason to go through
         * the collection twice. I just update the both wheater or not they
         * both need to be updated.
         */
        public function updateSelectAllAndCanSave():void {
            // temp used so the user does not see the bound objects flash
            var tempSelected:Boolean = true;            
            for each(var txn:PayrollEmployeeTransaction in employeeTransactions){
                if(!txn.selected){
                    tempSelected = false;
                }                
            }

            // update selected
            if(tempSelected != selected) selected = tempSelected;
            
            updateCanSave();             
        }
        
        override public function get hasChanged():Boolean {
			for each(var txn:PayrollEmployeeTransaction in employeeTransactions){
                if(txn.selected){
                    return true;
                }                
            }
            return false;
		}

        /**
         * Submit the request to cancel the selected transactions
         */
        override protected function executeSave():void {

            var transactions:ArrayCollection = new ArrayCollection();
            for each(var txn:PayrollEmployeeTransaction in employeeTransactions){
                if(txn.selected){
                    transactions.addItem(txn.transactionId);
                }
            }

            if(transactions.length > 0){
                // cancel the transactions
                SAP.instance.payrollRunService.reversePayrollRunTransactions(
                        this.company.sourceSystemCd,
                        this.company.companyId,
                        transactions,                        
                        this.payrollRun.sourcePayRunId,
                    // cannot charge fee if there is no active bank account
                        chargeReversalFee && hasActiveBankAccount,
                        (!isACHType && transactionDate.length > 0) ? new Date(Date.parse(transactionDate)) : null,
                        settlementType.code,
                        mInitiateReversal,
                        createSaveResponder(null, onTransactionsFaulted));
            }
        }

        private function onTransactionsFaulted(e:FaultEvent):void {
            updateSelectAllAndCanSave();
        }
    }
}
