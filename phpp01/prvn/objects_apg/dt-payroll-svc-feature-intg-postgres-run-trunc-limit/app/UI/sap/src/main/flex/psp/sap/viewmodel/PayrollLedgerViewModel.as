package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.CompanyInspectorLinkHandler;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.ActionEvent;
    import psp.sap.model.CompanyLedgerAccount;
    import psp.sap.model.PayrollRun;

    public class PayrollLedgerViewModel extends AbstractPartViewModel
    {

        [Bindable] [BackingProperty (context=true)] public var payrollRun:PayrollRun;

        //Possible actions
        private const ACTION_intuit5DayReturnTransfer:String = "intuit5DayReturnTransfer";
        private const ACTION_employerReturnTransfer:String = "employerReturnTransfer";
        private const ACTION_writeOffBadDebt:String = "writeOffBadDebt";
        private const ACTION_writeOffEmployeeBadDebt:String = "writeOffEmployeeBadDebt";
        private const ACTION_voidPayrollTaxPayment:String = "voidPayrollTaxPayment";
        
        private var mLedgerAction:String = "";
        private var mLedgerAccounts:ArrayCollection = new ArrayCollection();

        public function PayrollLedgerViewModel()
        {
            reloadOnSave = true;
        }

        public static function createActivator(payrollRun:PayrollRun):Object {
            return {"payrollRun":payrollRun};
        }


        [Bindable]
        public function get ledgerAccounts():ArrayCollection {
            return mLedgerAccounts;
        }

        public function set ledgerAccounts(value:ArrayCollection):void {
            mLedgerAccounts = value;
        }

        override protected function loadModelData():void {
            if (company == null)
                return;

            SAP.instance.payrollRunService.findLedgerAccountsByPayroll(
                    company.companyId,
                    company.sourceSystemCd,
                    payrollRun.sourcePayRunId,
                    createLoadModelDataResponder(onLedgerAccountsLoaded));

        }

        public function onLedgerAccountsLoaded(e:ResultEvent):void {
            ledgerAccounts = e.result as ArrayCollection;
        }

        [Bindable("propertyChange")]
        public function get canAddEscalationCredit():Boolean {
            return SAP.canPerformOperation(OperationsEnum.RECORD_ESCALATIONS);
        }

        [Bindable(event="contextPropertyChanged")]
        public function get pageLabel():String {
            var paycheckDate:String = payrollRun == null ? "" : SAPDateFormatters.dateFormatMedium.format(payrollRun.paycheckDate);
            return "Company Ledger for Payroll Check Date: " + paycheckDate;
        }


        public function viewLedgerEntry(ledgerAccount:CompanyLedgerAccount):void {            
            topic.findPage(CompanyInspectorPageEnum.COMPANY_PAYROLL_LEDGER_ACCOUNT_ENTRIES).activatePage(PayrollLedgerEntriesViewModel.createActivator(payrollRun.sourcePayRunId, ledgerAccount));
        }

        public function viewAddEscalation():void {
            topic.findPage(CompanyInspectorPageEnum.COMPANY_PAYROLL_ADD_ESCALATION).activatePage(PayrollSettlementViewModel.createActivator(payrollRun));
        }

        public function performPayrollLedgerAction(action:ActionEvent, ledgerAccount:CompanyLedgerAccount):void {
            action.performPayrollLedgerAction(inspector, payrollRun, ledgerAccount);
		}

        public function viewFinancialLedgerAdjustment():void {
            new CompanyInspectorLinkHandler(CompanyInspectorViewModel(inspector)).goToFinancialLedgerAdjustment(payrollRun.sourcePayRunId);
        }


        public function writeOffBadDebt():void {
            ledgerAction = ACTION_writeOffBadDebt;
            forceSave();
        }
        
        public function writeOffEmployeeBadDebt():void {
        	ledgerAction = ACTION_writeOffEmployeeBadDebt;
            forceSave();
        }

        public function employerReturnTransfer():void {
            ledgerAction = ACTION_employerReturnTransfer;
            forceSave();
        }

        public function intuit5DayReturnTransfer():void {
            ledgerAction = ACTION_intuit5DayReturnTransfer;
            forceSave();
        }

        public function voidPayrollTaxPayment():void {
            ledgerAction = ACTION_voidPayrollTaxPayment;
            forceSave();
        }

        override protected function initializeBackingProperties():void {
            ledgerAction = "";
        }

        private function get ledgerAction():String {
            return mLedgerAction;
        }

        private function set ledgerAction(value:String):void {
            mLedgerAction = value;
            updateIsValid();
        }

        override protected function evaluateIsValid(fireEvents:Boolean=true):Boolean {
            return super.evaluateIsValid(fireEvents) && (ledgerAction != "");
        }

        override protected function executeSave():void {
            if(mLedgerAction == ACTION_intuit5DayReturnTransfer) {
                SAP.instance.payrollRunService.addIntuit5DayReturnTransfer(this.company.sourceSystemCd,
                        this.company.companyId, payrollRun.sourcePayRunId,
                        createSaveResponder());
            } else if(mLedgerAction == ACTION_employerReturnTransfer) {
                SAP.instance.payrollRunService.addEmployeeReturnTransferTransaction(this.company.sourceSystemCd,
                        this.company.companyId, payrollRun.sourcePayRunId,
                        createSaveResponder());
            } else if(mLedgerAction == ACTION_writeOffBadDebt) {
                SAP.instance.payrollRunService.addWriteOffBadDebtTransaction(this.company.sourceSystemCd,
                        this.company.companyId, payrollRun.sourcePayRunId,
                        createSaveResponder());
            } else if(mLedgerAction == ACTION_writeOffEmployeeBadDebt) {
            	SAP.instance.payrollRunService.addWriteOffEmployeeBadDebtTransaction(this.company.sourceSystemCd,
                        this.company.companyId, payrollRun.sourcePayRunId,
                        createSaveResponder());
            } else if (mLedgerAction == ACTION_voidPayrollTaxPayment) {
                SAP.instance.payrollRunService.voidPayrollTaxPayment(this.company.companyId, this.company.sourceSystemCd,
                        payrollRun.sourcePayRunId,
                        createSaveResponder());
            }
        }

    }
}
