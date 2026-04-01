package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.events.PropertyChangeEvent;
    import mx.rpc.events.ResultEvent;
    import mx.validators.NumberValidator;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.CompanyBankAccount;
    import psp.sap.model.OfferingServiceChargePrice;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.SettlementTypeEnum;
    import psp.sap.validators.SAPDateValidator;
    import psp.sap.validators.SAPValidators;

    public class PayrollTransactionCreateFeeViewModel extends AbstractPartViewModel
    {

        [Bindable] [BackingProperty (context=true, required=false)] public var payrollRun:PayrollRun;

        [ArrayElementType("psp.sap.model.OfferingServiceChargePrice")]
        [Bindable] [BackingProperty] public var fees:ArrayCollection;

        private var mSettlementType:SettlementTypeEnum;

        protected var DEFAULT_SETTLEMENT_TYPE:SettlementTypeEnum = SettlementTypeEnum.ACH;

        [Bindable] public var settlementDateRangeValidator:SAPDateValidator;
        [Bindable] public var settlementTypes:Array = SettlementTypeEnum.values;

        [Bindable] [BackingProperty] public var settlementDate:String;

        [Bindable] public var activeBankAccount:CompanyBankAccount;

        public function PayrollTransactionCreateFeeViewModel()
        {
            settlementDateRangeValidator = SAPValidators.createDateValidator(this, "settlementDate", true, 0, 45, SAP.instance.PSPDate);
            validators.push(settlementDateRangeValidator);

			DEFAULT_SETTLEMENT_TYPE = SettlementTypeEnum.ACH;
        }


        public static function createActivator(payrollRun:PayrollRun):Object {
            return {"payrollRun":payrollRun};
        }

        [Bindable ("propertyChange")]
        public function get canSelectNonStandardSettlementTypes():Boolean {
            return SAP.canPerformOperation(OperationsEnum.SELECT_NON_STANDARD_SETTLEMENT_TYPE);
        }        

        [Bindable("propertyChange")]
        public function get payrollRunDate():Date {
            return payrollRun.payrollRunDate;
        }

        [Bindable] [BackingProperty (hasChanged=false)]
        public function get settlementType():SettlementTypeEnum {
            return mSettlementType;
        }

        public function set settlementType(value:SettlementTypeEnum):void {
            if (value == null)
                value = DEFAULT_SETTLEMENT_TYPE;

            mSettlementType = value;

            resetAmounts();

            settlementDate = SAPDateFormatters.dateFormatShort.format(SAP.instance.PSPDate);
        }


        protected function get settlementDateValue():Date {
            if (settlementDate != null) {
                var formattedDate:String = SAPDateFormatters.dateFormatShort.format(settlementDate);
                var txDate:Date = SAP.instance.PSPDate;
                var time:Number = Date.parse(formattedDate);
                txDate.setTime(time);
                return txDate;
            }
            else {
                return null;
            }
        }

        [Bindable("propertyChange")]
        public function get bankAccountLabel():String {
            return activeBankAccount != null ?
                   activeBankAccount.toString() : "[No Active Bank Account]";
        }

        override protected function loadModelData():void {
            loadCount = 2;
            SAP.instance.companyService.getFeeOfferingServiceChargePrices(
                    this.company.sourceSystemCd,
                    this.company.companyId,
                    payrollRun != null ? payrollRun.id : null,
                    createLoadModelDataResponder(onFeePricesLoaded));
            SAP.instance.companyService.getActiveBankAccount(company.sourceSystemCd, company.companyId, createLoadModelDataResponder(onActiveBankAccountLoaded));
        }

        private function onFeePricesLoaded(e:ResultEvent):void {
            fees = ArrayCollection(e.result);
        }

        private function onActiveBankAccountLoaded(e:ResultEvent):void {
            activeBankAccount = CompanyBankAccount(e.result);
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "bankAccountLabel", null, null));
        }

        override protected function initializeBackingProperties():void {
            validators.length = 1;
            for each (var offeringServiceChargePrice:OfferingServiceChargePrice in fees) {
                var amountValidator:NumberValidator = SAPValidators.createNumberValidator(offeringServiceChargePrice, "chargedPriceString", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2);
                amountValidator.enabled = offeringServiceChargePrice.checked;
                offeringServiceChargePrice.validator = amountValidator;
                validators.push(amountValidator);

                if (offeringServiceChargePrice.requiresMemo) {
                    var memoValidator:Validator = SAPValidators.createStringValidator(offeringServiceChargePrice, "memo", true, null, 30);
                    memoValidator.enabled = offeringServiceChargePrice.checked;
                    offeringServiceChargePrice.memoValidator = memoValidator;
                    validators.push(memoValidator);
                }
            }

            resetAmounts();

            settlementType = DEFAULT_SETTLEMENT_TYPE;
        }

        private function resetAmounts():void {
            for each (var offeringServiceChargePrice:OfferingServiceChargePrice in fees) {
                offeringServiceChargePrice.checked = false;
            }
        }


        override protected function evaluateCanSave():Boolean {
            updateIsValid();
            for each (var offeringServiceChargePrice:OfferingServiceChargePrice in fees) {
                if (offeringServiceChargePrice.checked) {
                    return isValid && hasChanged;
                }
            }
            return false;
        }

        override protected function executeSave():void {
            SAP.instance.payrollRunService.addFeeTransactions(
                    this.company.companyId,
                    this.company.sourceSystemCd,
                    payrollRun != null ? payrollRun.sourcePayRunId : null,
                    mSettlementType.code,
                    settlementDateValue,
                    fees,
                    createSaveResponder());

        }

        public function goToPayrollTransactions():void {
            topic.findPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_LIST).activatePage(PayrollTransactionsListViewModel.createActivator(payrollRun.sourcePayRunId, payrollRun.paycheckDate));
        }
    }
}
