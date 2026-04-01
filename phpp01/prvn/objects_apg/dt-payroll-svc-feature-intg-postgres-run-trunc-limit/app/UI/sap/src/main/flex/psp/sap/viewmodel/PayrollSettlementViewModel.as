package psp.sap.viewmodel
{
    import mx.events.PropertyChangeEvent;
    import mx.formatters.NumberFormatter;
    import mx.rpc.events.ResultEvent;
    import mx.validators.NumberValidator;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.CompanyBankAccount;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.SettlementTypeEnum;
    import psp.sap.validators.SAPDateValidator;
    import psp.sap.validators.SAPValidators;

    public class PayrollSettlementViewModel extends AbstractPartViewModel
    {

        [Bindable] [BackingProperty (context=true)] public var payrollRun:PayrollRun;
        public var canDdTaxSplit:Boolean = false;

        private const DEFAULT_TX_AMOUNT:String = "";
        private var DEFAULT_TX_DATE:Date;
        protected var DEFAULT_SETTLEMENT_TYPE:SettlementTypeEnum = SettlementTypeEnum.ACH;
        //noinspection JSFieldCanBeLocalInspection
        private var DEFAULT_ALLOW_AMOUNT_CHANGE:Boolean = true;

        [Bindable] public var ddTaxSplit:Boolean;
        [Bindable] public var settlementTypes:Array;
        private var mSettlementType:SettlementTypeEnum;
        [Bindable] [BackingProperty] public var amount:String;
        [Bindable] [BackingProperty] public var taxAmount:String;
        [Bindable] [BackingProperty] public var settlementDate:String;
        [Bindable]  public var allowAmountChange:Boolean;

        protected var mNumberFormatter:NumberFormatter = new NumberFormatter();

        [Bindable] public var bankAccountLabel:String;

        [Bindable] public var amountValidator:NumberValidator;
        [Bindable] public var taxAmountValidator:Validator;
        [Bindable] public var amountRequiredValidator:Validator;
        [Bindable] public var settlementDateRequiredValidator:Validator;
        [Bindable] public var settlementDateValidator:SAPDateValidator;

        public function PayrollSettlementViewModel()
        {

            settlementTypes = SettlementTypeEnum.values;

			// core counts current day as one of the 45 days in the past... so we allow 44 days in the past
            settlementDateValidator = SAPValidators.createDateValidator(this, "settlementDate", false, 44, 0);
            settlementDateValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
            settlementDateValidator.trigger = this;
            validators.push(settlementDateValidator);

            mNumberFormatter.useThousandsSeparator = false;
            mNumberFormatter.precision = 2;

            amountValidator = SAPValidators.createNumberValidator(this, "amount", false, "0.01", SAP.instance.configuration.maxAllowedCurrencyValue, false, 2);
            validators.push(amountValidator);

            taxAmountValidator = SAPValidators.createNumberValidator(this, "taxAmount", false, "0.01", SAP.instance.configuration.maxAllowedCurrencyValue, false, 2);
            validators.push(taxAmountValidator);

            amountRequiredValidator = new Validator();
            amountRequiredValidator.source = this;
            amountRequiredValidator.property = "amount";
            amountRequiredValidator.required = true;
            validators.push(amountRequiredValidator);

            settlementDateRequiredValidator = new Validator();
            settlementDateRequiredValidator.source = this;
            settlementDateRequiredValidator.property = "settlementDate";
            settlementDateRequiredValidator.required = true;
            validators.push(settlementDateRequiredValidator);

            DEFAULT_SETTLEMENT_TYPE = SettlementTypeEnum.ACH;
        }

        public static function createActivator(payrollRun:PayrollRun):Object {
            return {"payrollRun":payrollRun};
        }


        [Bindable]
        [BackingProperty]
        public function get settlementType():SettlementTypeEnum {
            return mSettlementType;
        }

        public function set settlementType(value:SettlementTypeEnum):void {
            if (value == null)
                value = DEFAULT_SETTLEMENT_TYPE;

                // if old value was ACH, reset the transaction amount/date to defaults
            if (mSettlementType == SettlementTypeEnum.ACH) {
                initializeBackingProperties();
            }

            mSettlementType = value;

            // if new value is ACH, set transaction amount to payrollTransaction amount and date to today
            // amount and date are not required
            if (mSettlementType == SettlementTypeEnum.ACH) {
                initializeBackingProperties();
                amountRequiredValidator.enabled = false;
                settlementDateRequiredValidator.enabled = false;
                settlementDateValidator.enabled = false;
            } else {
                amountRequiredValidator.enabled = !ddTaxSplit;
                settlementDateRequiredValidator.enabled = true;
                settlementDateValidator.enabled = true;
            }
        }

        protected function get settlementTypeCode():String {
            return mSettlementType.code;
        }

        public function get canSelectNonStandardSettlementTypes():Boolean {
            return SAP.canPerformOperation(OperationsEnum.SELECT_NON_STANDARD_SETTLEMENT_TYPE);
        }


        protected function get amountValue():Number {
            return parseFloat(mNumberFormatter.format( amount != DEFAULT_TX_AMOUNT ? amount : "0.00"));
        }

        protected function get taxAmountValue():Number {
            return parseFloat(mNumberFormatter.format( taxAmount != DEFAULT_TX_AMOUNT ? taxAmount : "0.00"));
        }

        protected function get settlementDateValue():Date {
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(settlementDate);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        override protected function onActivating():void {
            settlementType = DEFAULT_SETTLEMENT_TYPE;
        }


        override protected function loadModelData():void {
            SAP.instance.companyService.getActiveBankAccount(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onActiveBankAccountLoaded));
        }

        private function onActiveBankAccountLoaded(e:ResultEvent):void {
            var activeBankAccount:CompanyBankAccount = CompanyBankAccount(e.result);
            bankAccountLabel = activeBankAccount != null ? activeBankAccount.toString() : "[No Active Bank Account]";
        }

        override protected function initializeBackingProperties():void {
            DEFAULT_TX_DATE = SAP.instance.PSPDate;
            DEFAULT_TX_DATE.setHours(0, 0, 0, 0);

            // NOTE: settlement type intentionally only initialized on activated()
            // 		 so that screen doesn't 'jump' when post save of a non-ACH tx

            allowAmountChange = DEFAULT_ALLOW_AMOUNT_CHANGE;
            amount = DEFAULT_TX_AMOUNT;
            taxAmount = DEFAULT_TX_AMOUNT;
            settlementDate = SAPDateFormatters.dateFormatShort.format(DEFAULT_TX_DATE);
            ddTaxSplit = canDdTaxSplit && payrollRun != null && payrollRun.hasDDTransactions && payrollRun.hasTaxTransactions;
        }

        override public function get hasChanged():Boolean {
            if (mSettlementType == SettlementTypeEnum.ACH) {
                return true;
            } else {
                return amount != DEFAULT_TX_AMOUNT || taxAmount != DEFAULT_TX_AMOUNT
                        || settlementDateValue.time != DEFAULT_TX_DATE.time;
            }
        }

        public function goToPayrollTransactions():void {
            topic.findPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_LIST).activatePage(PayrollTransactionsListViewModel.createActivator(payrollRun.sourcePayRunId, payrollRun.paycheckDate));
        }

    }
}