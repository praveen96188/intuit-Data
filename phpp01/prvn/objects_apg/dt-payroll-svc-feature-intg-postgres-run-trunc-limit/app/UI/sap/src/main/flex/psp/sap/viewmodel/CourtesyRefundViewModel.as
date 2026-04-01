/**
 * User: dweinberg
 * Date: 7/2/12
 * Time: 10:52 AM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.application.enums.TransactionPageEnum;
    import psp.sap.model.SettlementTypeEnum;
    import psp.sap.model.Transaction;
    import psp.sap.validators.SAPValidators;

    public class CourtesyRefundViewModel extends CompositePartViewModel {
        static public const MAX_AMOUNT:Number = 100000;

        private const EMPTY_STRING:String = "";

        private var mSettlementType:SettlementTypeEnum;

        protected var DEFAULT_SETTLEMENT_TYPE:SettlementTypeEnum = SettlementTypeEnum.ACH;

        [Bindable]
        [ArrayElementType("psp.sap.model.Transaction")]
        public var transactionResults:ArrayCollection = new ArrayCollection();

        [Bindable]
        [BackingProperty]
        public var amount:String = EMPTY_STRING;

        [Bindable]
        [BackingProperty]
        public var notes:String = EMPTY_STRING;
        [Bindable]
        public var settlementTypes:Array = SettlementTypeEnum.ach_check_wire;

        /*Validators*/
        [Bindable]
        public var amountValidator:Validator;
        [Bindable]
        public var notesValidator:Validator;

        //History pop up
        private var mTransactionHistoryPopUp:PopUpPartViewModel;
        private var mTransactionHistoryPopUpViewModel:TransactionHistoryPopUpViewModel;

        private var action:String;
        private var txnId:String;

        public function CourtesyRefundViewModel() {

            reloadOnSave = true;

            mTransactionHistoryPopUp = addPopUpPart(TransactionPageEnum.TRANSACTION_HISTORY);
            mTransactionHistoryPopUpViewModel = mTransactionHistoryPopUp.addNewPart(TransactionHistoryPopUpViewModel, TransactionPageEnum.TRANSACTION_HISTORY) as TransactionHistoryPopUpViewModel;

            amountValidator = SAPValidators.createNumberValidator(this, "amount", true, 0.00, MAX_AMOUNT, false, 2);
            validators.push(amountValidator);
            notesValidator = SAPValidators.createStringValidator(this, "notes", true, 1, 3900);
            validators.push(notesValidator);
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.findCourtesyRefundTransactions(company.sourceSystemCd, company.companyId, createLoadModelDataResponder(onSearchResults));
        }

        public function onSearchResults(e:ResultEvent):void {
            transactionResults = e.result as ArrayCollection;
        }

        override protected function initializeBackingProperties():void {
            settlementType = DEFAULT_SETTLEMENT_TYPE;
            amount = null;
            notes = null;
        }

        override protected function executeSave():void {
            if (action == "CancelTxn") {
                SAP.instance.payrollRunService.cancelTransaction(company.sourceSystemCd, company.companyId, txnId, createSaveResponder());
            } else if (action == "CreateRefund") {
                SAP.instance.taxService.createCourtesyRefund(company.sourceSystemCd, company.companyId, parseFloat(amount), notes, settlementTypeCode, createSaveResponder());
            } else if (action == "VoidTxn") {
                SAP.instance.payrollRunService.voidTransaction(company.sourceSystemCd, company.companyId, txnId, createSaveResponder());
            }
        }

        public function viewHistory(data:Transaction):void {
            mTransactionHistoryPopUpViewModel.transaction = data;
            mTransactionHistoryPopUp.displayPopUp();
        }

        public function createRefund():void {
            action = "CreateRefund";
            save();
        }

        public function cancelTransaction(transactionId:String):void {
            action = "CancelTxn";
            txnId = transactionId;
            forceSave();
        }

        public function voidTransaction(transactionId:String):void {
            action = "VoidTxn";
            txnId = transactionId;
            forceSave();
        }

        [Bindable("propertyChange")]
        public function get canSelectNonStandardSettlementTypes():Boolean {
            return SAP.canPerformOperation(OperationsEnum.SELECT_NON_STANDARD_SETTLEMENT_TYPE);
        }

        [Bindable]
        public function get settlementType():SettlementTypeEnum {
            return mSettlementType;
        }

        public function set settlementType(value:SettlementTypeEnum):void {
            if (value == null)
                value = DEFAULT_SETTLEMENT_TYPE;

            mSettlementType = value;

        }

        protected function get settlementTypeCode():String {
            return mSettlementType.code;
        }
    }
}
