package psp.sap.viewmodel
{
    import psp.sap.application.SAP;

    public class PayrollLedgerEmployeeReturnRefundViewModel extends PayrollSettlementViewModel
    {
        override protected function executeSave():void {

            SAP.instance.payrollRunService.addEmployeeReturnRefundTransaction(
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