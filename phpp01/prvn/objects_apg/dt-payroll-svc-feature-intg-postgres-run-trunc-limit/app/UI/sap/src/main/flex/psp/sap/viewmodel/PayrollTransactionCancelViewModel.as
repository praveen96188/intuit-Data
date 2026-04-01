package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    import mx.events.PropertyChangeEvent;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.PayrollEmployeeTransaction;
    import psp.sap.model.PayrollRun;

    public class PayrollTransactionCancelViewModel
    extends AbstractPartViewModel
    {

        [Bindable] [BackingProperty (context=true)] public var payrollRun:PayrollRun;

        [Bindable] public var selected:Boolean = false;

        public function PayrollTransactionCancelViewModel() {
            this.reloadOnSave = true;
        }

        public static function createActivator(payrollRun:PayrollRun):Object {
            return {"payrollRun":payrollRun};
        }

        [ArrayElementType("psp.sap.model.PayrollEmployeeTransaction")]
        private var mEmployeeTransactions:ArrayCollection =  null;

        [Bindable]
        public function get employeeTransactions():ArrayCollection {
            return mEmployeeTransactions;
        }

        public function set employeeTransactions(value:ArrayCollection):void {
            // only show created transactions ** logic from DDREPUI CancelDirectDepositTransactionsAction.java:93
            var filteredCollection:ArrayCollection = new ArrayCollection();
            for each(var transaction:PayrollEmployeeTransaction in value){
                if(transaction.txnType == "EmployeeDdCredit" && transaction.status == "Created"){
                    // add selected property
                    transaction.selected = false;
                    filteredCollection.addItem(transaction);
                }
            }
            mEmployeeTransactions = filteredCollection;
        }

        override protected function loadModelData():void {
            SAP.instance.payrollRunService.findCancelableTransactions(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    payrollRun.sourcePayRunId,
                    createLoadModelDataResponder(onEmployeeTransactionResults));
        }

        public function onEmployeeTransactionResults(e:ResultEvent):void {
            employeeTransactions = ArrayCollection(e.result);
            employeeTransactions.addEventListener(CollectionEvent.COLLECTION_CHANGE, onCollectionChanged, false, 0, true);
            updateSelectAllAndCanSave();
        }

        /**
         * Reset inputs to default values
         */
        override protected function initializeBackingProperties():void {
            selectAll = false;
            updateSelectAllAndCanSave();
        }


        [Bindable(event="contextPropertyChanged")]
        public function get pageLabel():String {
            var paycheckDate:String = payrollRun == null ? "" : SAPDateFormatters.dateFormatMedium.format(payrollRun.paycheckDate);
            return "Cancel Direct Deposit Transactions for Paycheck Date: " + paycheckDate;
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
            var tempCanSave:Boolean = false;
            for each(var txn:PayrollEmployeeTransaction in employeeTransactions){
                if(!txn.selected){
                    tempSelected = false;
                }
                else{
                    tempCanSave = true;
                }
            }
            selected = tempSelected;
            canSave = tempCanSave;
        }

        /**
         * Submit the request to cancel the selected transactions
         */
        override protected function executeSave():void {
            var transactions:ArrayCollection = new ArrayCollection();
            for each(var txn:PayrollEmployeeTransaction in employeeTransactions){
                // add transactions that are selected
                if(txn.selected){
                    // keep a list of ids to cancel
                    transactions.addItem(txn.transactionId);
                }
            }
            if(transactions.length > 0){
                if(payrollRun.isBillPaymentPayroll) {
                    // cancel bill payments
                    SAP.instance.payrollRunService.cancelBillPaymentTransaction(companyKey.companyId,
                            companyKey.sourceSystemCd,
                            payrollRun.sourcePayRunId,
                            transactions,
                            createSaveResponder(null, onTransactionsFaulted));
                }
                else {
                    // cancel employee transactions
                    SAP.instance.payrollRunService.cancelPayrollTransaction(companyKey.companyId,
                            companyKey.sourceSystemCd,
                            transactions,
                            payrollRun.sourcePayRunId,
                            createSaveResponder(null, onTransactionsFaulted));
                }
            }
        }

        private function onTransactionsFaulted(e:FaultEvent):void {
            updateSelectAllAndCanSave();
        }
    }
}
