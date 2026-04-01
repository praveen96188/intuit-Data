/**
 * User: dweinberg
 * Date: 1/9/12
 * Time: 5:09 PM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.formatters.NumberBaseRoundType;
    import mx.formatters.NumberFormatter;
    import mx.rpc.events.ResultEvent;
    import mx.validators.NumberValidator;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.LookupCollection;
    import psp.sap.model.LawAmount;
    import psp.sap.model.Payment;
    import psp.sap.model.PaymentDetails;
    import psp.sap.validators.SAPValidators;

    public class EditPaymentAmountViewModel extends AbstractPartViewModel {
        public static const ACCOUNTING_IMMEDIATE:String = "immediate";
        public static const ACCOUNTING_VARIANCE:String = "varianceAccount";

        [Bindable] [BackingProperty (context=true, hasChanged=false)]
        public var payment:Payment;
        [Bindable] [BackingProperty (context=true, hasChanged=false)]
        public var clearMemo:Boolean = false;

        public static function createActivator(payment:Payment, clearMemo:Boolean):Object {
            return {"payment":payment, "clearMemo":clearMemo};
        }

        [Bindable]
        [ArrayElementType("psp.sap.model.PaymentDetails")]
        public var financialTransactions:ArrayCollection;

        [ArrayElementType("psp.sap.model.LawAmount")]
        private var tempLawAmounts:ArrayCollection;

        [Bindable]
        [ArrayElementType("psp.sap.model.LawAmount")]
        public var lawAmounts:LookupCollection;

        [Bindable] public var varianceAccountAmount:Number;
        [Bindable] public var newVarianceAccountAmount:Number;

        [Bindable] public var currentAmountTotal:Number=0;
        [Bindable] public var newAmountTotal:Number=0;
        [Bindable] public var liabilityAdjustmentsTotal:Number=0;
        [Bindable] public var splitsTotal:Number=0;
        private var mAllowLimit_OutsideOfBoundaries:Boolean = false;

        [Bindable] [BackingProperty (hasChanged=false)] public var selectedAccountingOption:String;
        [Bindable] [BackingProperty (hasChanged=false)] public var memo:String;

        [Bindable]
        [ArrayElementType("mx.validators.Validator")]
        public var liabilityAmountValidators:ArrayCollection = new ArrayCollection();

        [Bindable]
        [ArrayElementType("mx.validators.Validator")]
        public var newLiabilityAmountValidators:ArrayCollection = new ArrayCollection();

        [Bindable] public var splitAmountValidator:NumberValidator;
        [Bindable] public var memoValidator:Validator;

        private var mNumberFormatter:NumberFormatter = new NumberFormatter();

        public function EditPaymentAmountViewModel() {
            splitAmountValidator = SAPValidators.createNumberValidator(this, "splitsTotal", false, null, 0, true);
            splitAmountValidator.lowerThanMinError = "You cannot split out into a negative payment";
            validators.push(splitAmountValidator);
            memoValidator = SAPValidators.createStringValidator(this, "memo", true, null, 2000);
            validators.push(memoValidator);

            mNumberFormatter.precision = 2;
            mNumberFormatter.useThousandsSeparator = false;
            mNumberFormatter.rounding = NumberBaseRoundType.NEAREST;
        }

        override protected function loadModelData():void {
            loadCount = 3;
            SAP.instance.taxService.getPaymentAmountDetails(payment.companyKey.sourceSystemCd, payment.companyKey.companyId, payment.paymentId, createLoadModelDataResponder(onPaymentAmountDetailsLoaded));
            SAP.instance.taxService.getLawAmounts(payment.paymentId, payment.companyId, createLoadModelDataResponder(onLawAmountsLoaded));
            SAP.instance.payrollRunService.getLedgerAccountBalance(payment.companyKey.sourceSystemCd, payment.companyKey.companyId, "ERSUITaxDue", createLoadModelDataResponder(onLedgerBalanceLoaded));
        }

        public function onPaymentAmountDetailsLoaded(e:ResultEvent):void {
            financialTransactions = e.result as ArrayCollection;
        }

        public function onLawAmountsLoaded(e:ResultEvent):void {
            tempLawAmounts = ArrayCollection(e.result);
        }

        public function onLedgerBalanceLoaded(e:ResultEvent):void {
            varianceAccountAmount = Number(e.result) * -1;
        }

        override protected function initializeBackingProperties():void {
            validators.length = 2;
            liabilityAmountValidators = new ArrayCollection();
            newLiabilityAmountValidators = new ArrayCollection();
            for each (var lawAmount:LawAmount in tempLawAmounts) {
                var liabilityAmountValidator:NumberValidator = SAPValidators.createNumberValidator(lawAmount, "liabilityAdjustmentAmount", false, -1000000, 1000000, true, 2);
                liabilityAmountValidators.addItem(liabilityAmountValidator);
                validators.push(liabilityAmountValidator);

                var newLiabilityAmountValidator:NumberValidator = SAPValidators.createNumberValidator(lawAmount, "newAmount", true, 0, null, 2);
                if (lawAmount.amount == 0) {
                    newLiabilityAmountValidator.enabled = false;
                }
                newLiabilityAmountValidators.addItem(newLiabilityAmountValidator);
                validators.push(newLiabilityAmountValidator);
                lawAmount.newAmount = mNumberFormatter.format(lawAmount.amount);
                lawAmount.initializeNumberFormat();
            }

            lawAmounts = new LookupCollection(LawAmount, tempLawAmounts.source, "law");

            selectedAccountingOption = ACCOUNTING_VARIANCE;
            if (clearMemo) {
                memo = "";
            }


            calculate(false);

        }

        public function onSplitSelected(paymentDetails:PaymentDetails, value:Boolean):void {
            paymentDetails.selected = value;
            resetAmount();
            calculate(true);
        }

        private function resetAmount():void {
            for each (var transaction:PaymentDetails in financialTransactions) {
                if (transaction.selected) {
                    var foundLawAmount:LawAmount = LawAmount(lawAmounts.getItemByKey(transaction.lawType));
                    foundLawAmount.liabilityAdjustmentAmount = "";
                }
            }
        }

        public function calculate(newAmountChanging:Boolean):void {
            for each (var lawAmount:LawAmount in lawAmounts) {
                lawAmount.splitAmount = 0;
            }
            for each (var transaction:PaymentDetails in financialTransactions) {
                if (transaction.selected) {
                    var foundLawAmount:LawAmount = LawAmount(lawAmounts.getItemByKey(transaction.lawType));
                    foundLawAmount.splitAmount -= transaction.amount;
                }
            }

            currentAmountTotal = 0;
            newAmountTotal = 0;
            liabilityAdjustmentsTotal = 0;
            splitsTotal = 0;
            for each (lawAmount in lawAmounts) {
                if (newAmountChanging) {
                    lawAmount.calculateNewAmount();
                }
                else {
                    lawAmount.calculateLiability();
                }
                currentAmountTotal += lawAmount.amount;
                newAmountTotal += parseFloat(mNumberFormatter.format(lawAmount.newAmount));
                splitsTotal += lawAmount.splitAmount;
                liabilityAdjustmentsTotal += parseFloat(mNumberFormatter.format(lawAmount.liabilityAdjustmentAmount));
            }

            newVarianceAccountAmount = varianceAccountAmount + liabilityAdjustmentsTotal;
            selectedAccountingOption = ACCOUNTING_VARIANCE;

            updateCanSave();
        }

        [Bindable]
        public function get allowLimitOutsideOfBoundaries():Boolean {
            return mAllowLimit_OutsideOfBoundaries;
        }

        public function set allowLimitOutsideOfBoundaries(value:Boolean):void {
            mAllowLimit_OutsideOfBoundaries = value;
        }


        override public function get hasChanged():Boolean {
            if (super.hasChanged) {
                return true;
            }

            for each (var txn:PaymentDetails in financialTransactions) {
                if (txn.selected) {
                    return true;
                }
            }

            for each (var lawAmount:LawAmount in lawAmounts) {
                if (parseFloat(mNumberFormatter.format(lawAmount.liabilityAdjustmentAmount)) != 0 ) {
                    return true;
                }
            }

            return false;
        }


        override protected function executeSave():void {
            var splitFTs:ArrayCollection = new ArrayCollection();
            for each (var ft:PaymentDetails in financialTransactions) {
                if (ft.selected) {
                    splitFTs.addItem(ft.ftId);
                }
            }

            var liabilityAmounts:ArrayCollection = new ArrayCollection();
            for each (var lawAmount:LawAmount in lawAmounts) {
                if (parseFloat(mNumberFormatter.format(lawAmount.liabilityAdjustmentAmount)) != 0) {
                    var liabilityAmount:LawAmount = new LawAmount();
                    liabilityAmount.law = lawAmount.law;
                    liabilityAmount.lawId = lawAmount.lawId;
                    liabilityAmount.amount = parseFloat(mNumberFormatter.format(lawAmount.liabilityAdjustmentAmount));
                    liabilityAmounts.addItem(liabilityAmount);
                }
            }

            SAP.instance.taxService.editPaymentAmount(payment.paymentId, splitFTs, liabilityAmounts, memo, selectedAccountingOption == ACCOUNTING_IMMEDIATE, allowLimitOutsideOfBoundaries, payment.companyId, createSaveResponder());
        }

    }
}
