package psp.sap.viewmodel
{
    import psp.sap.application.SAP;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.PayrollTransaction;

    public class PayrollTransactionIssueReissueRefundPageViewModel
    extends PayrollSettlementViewModel
    {

        [Bindable] [BackingProperty(context=true)] public var payrollTransaction:PayrollTransaction;

        public function PayrollTransactionIssueReissueRefundPageViewModel() {
            this.shallowCopyFields = ["id", "amount"];
        }

        public static function createActivator(payrollRun:PayrollRun, payrollTransaction:PayrollTransaction):Object {
            return {"payrollRun":payrollRun,"payrollTransaction":payrollTransaction};
        }        

        override protected function executeSave():void {
            SAP.instance.payrollRunService.refundEmployerTransaction(
                    company.sourceSystemCd,
                    company.companyId,
                    payrollTransaction.id,
                    amountValue,
                    settlementDateValue,
                    settlementTypeCode,
                    createSaveResponder());            
        }


        override protected function initializeBackingProperties():void {
            super.initializeBackingProperties();
            allowAmountChange = false;
            amount = mNumberFormatter.format(Math.abs(payrollTransaction.amount));
        }
    }
}