/**
 * User: dweinberg
 * Date: 2/8/11
 * Time: 8:43 PM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.formatters.NumberFormatter;

    import mx.rpc.events.ResultEvent;
    import mx.validators.NumberValidator;
    import mx.validators.Validator;

    import psp.sap.application.CompanyInspectorLinkHandler;
    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.CompanyInspectorTopicEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.formatters.SAPCurrencyFormatters;
    import psp.sap.model.ActionEvent;
    import psp.sap.model.PayrollTransaction;
    import psp.sap.model.SettlementTypeEnum;
    import psp.sap.validators.SAPValidators;

    public class ERPayableRefundsViewModel extends AbstractPartViewModel {

        protected var DEFAULT_SETTLEMENT_TYPE:SettlementTypeEnum = SettlementTypeEnum.ACH;


        [ArrayElementType("psp.sap.model.PayrollTransaction")]
        [Bindable] public var transactions:ArrayCollection = new ArrayCollection();

        [Bindable] public var canCreateRefund:Boolean;

        [Bindable]
        [BackingProperty]
        public var amount:String = "";

        private var mSettlementType:SettlementTypeEnum;

        [Bindable]
        public var settlementTypes:Array = SettlementTypeEnum.values;

        private var erPayableAmount:Number;

        [Bindable]
        public var amountValidator:NumberValidator;

        private var txnId:String;
        private var action:String;

        public function ERPayableRefundsViewModel() {
            super();
            reloadOnSave = true;

            amountValidator = SAPValidators.createNumberValidator(this, "amount", true, 0.00, 1, false, 2);
            validators.push(amountValidator);
        }

        override protected function loadModelData():void {
            loadCount = 2;
            SAP.instance.payrollRunService.getLedgerAccountBalance(companyKey.sourceSystemCd, companyKey.companyId, "ERPayable", createLoadModelDataResponder(onERPayableBalanceLoaded));
            SAP.instance.payrollRunService.findERPayableRefundTransactions(companyKey.companyId, companyKey.sourceSystemCd, createLoadModelDataResponder(onTransactionsLoaded));
        }

        private function onERPayableBalanceLoaded(e:ResultEvent):void {
            erPayableAmount = Number(e.result);
        }

        private function onTransactionsLoaded(e:ResultEvent):void {
            transactions = ArrayCollection(e.result);
        }

        override protected function initializeBackingProperties():void {
            settlementType = DEFAULT_SETTLEMENT_TYPE;
            amount = SAPCurrencyFormatters.currencyFormatterNoSymbolNoComma.format(erPayableAmount);
            amountValidator.maxValue = erPayableAmount;
            amountValidator.exceedsMaxError = "You cannot refund more than the current ER Payable balance of " + SAPCurrencyFormatters.defaultFormatter.format(erPayableAmount);
            canCreateRefund = erPayableAmount > 0 && SAP.canPerformOperation(OperationsEnum.REFUND_ER_PAYABLE);
        }

        [Bindable("propertyChange")]
        public function get canSelectNonStandardSettlementTypes():Boolean {
            return SAP.canPerformOperation(OperationsEnum.SELECT_NON_STANDARD_SETTLEMENT_TYPE);
        }


        override public function get hasChanged():Boolean {
            return true;
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


        override protected function executeSave():void {
            if (action == "CancelTxn") {
                SAP.instance.payrollRunService.cancelTransaction(company.sourceSystemCd, company.companyId, txnId, createSaveResponder());
            } else if (action == "CreateRefund") {
               SAP.instance.payrollRunService.refundERPayable(company.sourceSystemCd, company.companyId, settlementTypeCode, parseFloat(amount), createSaveResponder());
            } else if (action == "VoidTxn") {
                SAP.instance.payrollRunService.voidTransaction(company.sourceSystemCd, company.companyId, txnId, createSaveResponder());
            }
        }

        public function performPayrollTransactionAction(action:ActionEvent, payrollTransaction:PayrollTransaction):void {
            action.performPayrollTransactionAction(inspector, null, payrollTransaction);
        }

    }
}
