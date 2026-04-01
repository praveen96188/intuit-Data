package psp.sap.viewmodel
{
    import psp.sap.application.SAP;

    public class PayrollLedgerEmployerReturnRefundViewModel extends PayrollSettlementViewModel
    {

        override protected function executeSave():void {

            SAP.instance.payrollRunService.addEmployerReturnRefundTransaction(
                    company.sourceSystemCd,
                    company.companyId,
                    payrollRun.sourcePayRunId,
                    amountValue,
                    taxAmountValue,
                    settlementDateValue,
                    settlementTypeCode,
                    createSaveResponder());

        }
    }
}