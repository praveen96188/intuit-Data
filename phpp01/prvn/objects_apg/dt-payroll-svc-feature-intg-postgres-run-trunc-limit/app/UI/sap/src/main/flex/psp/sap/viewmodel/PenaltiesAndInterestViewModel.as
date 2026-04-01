/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 4/12/12
 * Time: 11:11 AM
 */
package psp.sap.viewmodel {

    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.formatters.NumberBaseRoundType;
    import mx.formatters.NumberFormatter;
    import mx.rpc.events.ResultEvent;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.PenaltiesAndInterestPageEnum;
    import psp.sap.application.enums.TransactionPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.SettlementTypeEnum;
    import psp.sap.model.Transaction;
    import psp.sap.validators.SAPValidators;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class PenaltiesAndInterestViewModel extends CompositePartViewModel {
        static public const MAX_AMOUNT:Number = 100000;

        private const EMPTY_STRING:String = "";

        private var mSettlementType:SettlementTypeEnum;

        protected var DEFAULT_SETTLEMENT_TYPE:SettlementTypeEnum = SettlementTypeEnum.ACH;

        private var mNumberFormatter:NumberFormatter = new NumberFormatter();

        [Bindable]
        [ArrayElementType("psp.sap.model.Transaction")]
        public var transactionResults:ArrayCollection = new ArrayCollection();

        private var mPenaltiesAmount:String = EMPTY_STRING;
        private var mInterestAmount:String = EMPTY_STRING;

        [Bindable]
        public var totalAmount:String = EMPTY_STRING;
        [Bindable]
        [BackingProperty]
        public var notes:String = EMPTY_STRING;
        [Bindable]
        public var settlementTypes:Array = SettlementTypeEnum.ach_check_wire;

        /*Validators*/
        [Bindable]
        public var penaltiesAmountValidator:Validator;
        [Bindable]
        public var interestAmountValidator:Validator;
        [Bindable]
        public var totalAmountValidator:Validator;
        [Bindable]
        public var notesValidator:Validator;

        //History pop up
        private var mTransactionHistoryPopUp:PopUpPartViewModel;
        private var mTransactionHistoryPopUpViewModel:TransactionHistoryPopUpViewModel;

        //Refund Debit pop up
        private var mRefundDebitPopUp:PopUpPartViewModel;
        private var mRefundDebitPopUpViewModel:RefundDebitPopUpViewModel;

        private var action:String;
        private var txnId:String;

        [Bindable]
        [BackingProperty]
        public function get penaltiesAmount():String {
            return mPenaltiesAmount;
        }

        public function set penaltiesAmount(value:String):void {
            mPenaltiesAmount = value;
            if (mPenaltiesAmount != null && mPenaltiesAmount.length > 0) {
                onChangeAmount();
            }
        }

        [Bindable]
        [BackingProperty]
        public function get interestAmount():String {
            return mInterestAmount;
        }

        public function set interestAmount(value:String):void {
            mInterestAmount = value;
            if (mInterestAmount != null && mInterestAmount.length > 0) {
                onChangeAmount();
            }
        }

        public function onChangeAmount():void {
            var total:Number = 0.0;
            if (penaltiesAmount != null && penaltiesAmount.length > 0) {
                total = parseFloat(mNumberFormatter.format(penaltiesAmount));
            }
            if (interestAmount != null && interestAmount.length > 0) {
                total += parseFloat(mNumberFormatter.format(interestAmount));
            }
            totalAmount = mNumberFormatter.format(total);
        }


        public function PenaltiesAndInterestViewModel() {

            reloadOnSave = true;

            mTransactionHistoryPopUp = addPopUpPart(TransactionPageEnum.TRANSACTION_HISTORY);
            mTransactionHistoryPopUpViewModel = mTransactionHistoryPopUp.addNewPart(TransactionHistoryPopUpViewModel, TransactionPageEnum.TRANSACTION_HISTORY) as TransactionHistoryPopUpViewModel;

            mRefundDebitPopUp = addPopUpPart(PenaltiesAndInterestPageEnum.REFUND_DEBIT_POPUP);
            mRefundDebitPopUp.closeOnSave = true;
            mRefundDebitPopUpViewModel = mRefundDebitPopUp.addNewPart(RefundDebitPopUpViewModel, PenaltiesAndInterestPageEnum.REFUND_DEBIT_POPUP) as RefundDebitPopUpViewModel;
            mRefundDebitPopUpViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);


            penaltiesAmountValidator = SAPValidators.createNumberValidator(this, "penaltiesAmount", true, 0.00, MAX_AMOUNT, false, 2);
            validators.push(penaltiesAmountValidator);
            interestAmountValidator = SAPValidators.createNumberValidator(this, "interestAmount", true, 0.00, MAX_AMOUNT, false, 2);
            validators.push(interestAmountValidator);
            totalAmountValidator = SAPValidators.createNumberValidator(this, "totalAmount", true, 0.01, MAX_AMOUNT, false, 2);
            validators.push(totalAmountValidator);
            notesValidator = SAPValidators.createStringValidator(this, "notes", true, 1, 3900);
            validators.push(notesValidator);

            mNumberFormatter.precision = 2;
            mNumberFormatter.useThousandsSeparator = false;
            mNumberFormatter.rounding = NumberBaseRoundType.NEAREST;

        }

        override protected function loadModelData():void {
            SAP.instance.taxService.findRefundTransactions(company.sourceSystemCd, company.companyId, createLoadModelDataResponder(onSearchResults));
        }

        public function onSearchResults(e:ResultEvent):void {
            transactionResults = e.result as ArrayCollection;
        }

        override protected function initializeBackingProperties():void {
            settlementType = DEFAULT_SETTLEMENT_TYPE;
            penaltiesAmount = null;
            interestAmount = null;
            totalAmount = null;
            notes = null;
        }

        override protected function executeSave():void {
            if (action == "CancelTxn") {
                SAP.instance.payrollRunService.cancelTransaction(company.sourceSystemCd, company.companyId, txnId, createSaveResponder());
            } else if (action == "CreateRefund") {
                SAP.instance.taxService.createPenaltiesAndInterestRefunds(company.sourceSystemCd, company.companyId, parseFloat(penaltiesAmount), parseFloat(interestAmount), notes, settlementTypeCode, createSaveResponder());
            } else if (action == "VoidTxn") {
                SAP.instance.payrollRunService.voidTransaction(company.sourceSystemCd, company.companyId, txnId, createSaveResponder());
            }
        }

        public function viewHistory(data:Transaction):void {
            mTransactionHistoryPopUpViewModel.transaction = data;
            mTransactionHistoryPopUp.displayPopUp();
        }

        public function createRefunds():void {
            action = "CreateRefund";
            forceSave();
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

        public function createRefundDebit(data:Transaction):void {
            mRefundDebitPopUpViewModel.transaction = data;
            mRefundDebitPopUp.displayPopUp();
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

        private function onViewModelDeactivated(e:Event):void {
            PopUpPartViewModel.refreshIfSaved(AbstractPartViewModel(e.target), this);
        }

    }

}
