package psp.sap.viewmodel
{
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.CompanyLedgerAccount;
    import psp.sap.model.OfferingServiceChargeTypeEnum;
    import psp.sap.model.SettlementTypeEnum;
    import psp.sap.validators.SAPValidators;

    public class PayrollLedgerFeeTransferViewModel extends PayrollSettlementViewModel
    {

        protected var DEFAULT_OFFERING_SERVICE_CHARGE:String = OfferingServiceChargeTypeEnum.NSF.code;

        public function PayrollLedgerFeeTransferViewModel()
        {
            settlementTypes = SettlementTypeEnum.non_ach_values;
            DEFAULT_SETTLEMENT_TYPE = SettlementTypeEnum.WIRE;
        }

        private var mOfferingServiceChargeTypeCd:String = DEFAULT_OFFERING_SERVICE_CHARGE;


        [Bindable]
        public function get offeringServiceChargeTypeCd():String {
            return mOfferingServiceChargeTypeCd;
        }

        public function set offeringServiceChargeTypeCd(value:String):void {
            mOfferingServiceChargeTypeCd = value;
            updateCanSave();
        }

        override protected function initializeBackingProperties():void {
            super.initializeBackingProperties();
            offeringServiceChargeTypeCd = DEFAULT_OFFERING_SERVICE_CHARGE;
        }

        override protected function executeSave():void {
            SAP.instance.payrollRunService.addFeeTransferTransaction(
                    company.sourceSystemCd,
                    company.companyId,
                    payrollRun.sourcePayRunId,
                    amountValue,
                    offeringServiceChargeTypeCd,
                    createSaveResponder());

        }

        override protected function loadModelData():void {
            loadCount = 2;
            super.loadModelData();

            // load the ledger balance to set a limit on the amount of the fee transfer
            SAP.instance.payrollRunService.findLedgerAccountByPayrollAndLedgerCode(
                    company.companyId,
                    company.sourceSystemCd,
                    payrollRun.sourcePayRunId,
                    "ERReturnReceivable",
                    createLoadModelDataResponder(onLedgerBalanceResult))
        }

        private function onLedgerBalanceResult(e:ResultEvent):void {
            var ledgerAccount:CompanyLedgerAccount = e.result as CompanyLedgerAccount;
            var tempAmount:Number = ledgerAccount.balance;
            amountValidator.maxValue = tempAmount;
            SAPValidators.updateNumberValidatorErrorMessage(amountValidator);
        }

    }
}