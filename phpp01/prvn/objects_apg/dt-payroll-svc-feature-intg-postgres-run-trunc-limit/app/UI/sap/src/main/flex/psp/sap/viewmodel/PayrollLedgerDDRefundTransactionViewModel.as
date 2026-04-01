package psp.sap.viewmodel
{
    import psp.sap.application.SAP;

    public class PayrollLedgerDDRefundTransactionViewModel extends PayrollSettlementViewModel
    {
        override protected function executeSave():void {

            SAP.instance.payrollRunService.addRefundTransaction(
                    company.sourceSystemCd,
                    company.companyId,
                    payrollRun.sourcePayRunId,
                    amountValue,
                    settlementDateValue,
                    settlementTypeCode,
                    createSaveResponder());
        }
    }
}