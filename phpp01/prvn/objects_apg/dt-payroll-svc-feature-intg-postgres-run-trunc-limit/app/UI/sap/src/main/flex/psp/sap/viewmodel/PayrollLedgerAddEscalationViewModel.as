package psp.sap.viewmodel
{
    import psp.sap.application.SAP;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.SettlementTypeEnum;

    public class PayrollLedgerAddEscalationViewModel
        extends PayrollSettlementViewModel
    {

        [Bindable] public var isEmployee:Boolean = false;

        public function PayrollLedgerAddEscalationViewModel()
        {
            settlementTypes = SettlementTypeEnum.non_ach_values;
            DEFAULT_SETTLEMENT_TYPE = SettlementTypeEnum.WIRE;
        }

        override protected function initializeBackingProperties():void {
        	super.initializeBackingProperties();
            settlementType = SettlementTypeEnum.WIRE;
            isEmployee = false;
        }

        [Bindable(event="contextPropertyChanged")]
        public function get pageLabel():String {
            var paycheckDate:String = payrollRun == null ? "" : SAPDateFormatters.dateFormatMedium.format(payrollRun.paycheckDate);
            return "Add Escalation for Payroll Check Date: " + paycheckDate;
        }

        override protected function executeSave():void {

            SAP.instance.payrollRunService.addEscalation(
                    this.company.sourceSystemCd,
                    this.company.companyId,
                    payrollRun.sourcePayRunId,
                    isEmployee,
                    settlementTypeCode,
                    amountValue,
                    settlementDateValue,
                    createSaveResponder());
        }

    }
}
