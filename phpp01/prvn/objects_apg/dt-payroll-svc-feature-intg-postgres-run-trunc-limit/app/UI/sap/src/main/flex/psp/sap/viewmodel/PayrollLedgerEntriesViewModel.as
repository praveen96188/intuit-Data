package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.CompanyLedgerAccount;
    import psp.sap.model.PayrollRun;

    public class PayrollLedgerEntriesViewModel extends AbstractPartViewModel
    {

        [Bindable] [BackingProperty (context=true)] public var sourcePayrollRunId:String;
        [Bindable] [BackingProperty (context=true)] public var companyLedgerAccount:CompanyLedgerAccount;


        public function PayrollLedgerEntriesViewModel() {
            this.shallowCopyFields = ["ledgerAccountCode", "name"];
        }

        public static function createActivator(sourcePayrollRunId:String, companyLedgerAccount:CompanyLedgerAccount):Object {
            return {"sourcePayrollRunId":sourcePayrollRunId, "companyLedgerAccount":companyLedgerAccount};
        }


        [ArrayElementType("psp.sap.model.PayrollTransaction")]
        private var mLedgerEntries:ArrayCollection = new ArrayCollection();

        [Bindable]
        public function get ledgerEntries():ArrayCollection {
            return mLedgerEntries;
        }

        public function set ledgerEntries(value:ArrayCollection):void {
            if (value == null)
                value = new ArrayCollection();

            mLedgerEntries = value;

            var sortDate:Sort = new Sort();
            var sortDateField:SortField = new SortField("createdDate",false,true,true);
            sortDate.fields = [sortDateField];
            mLedgerEntries.sort = sortDate;
            mLedgerEntries.refresh();
        }

        override protected function loadModelData():void {
            SAP.instance.payrollRunService.findTransactionsByLedgerAccountAndPayroll(
                    company.sourceSystemCd,
                    company.companyId,
                    companyLedgerAccount.ledgerAccountCode,
                    sourcePayrollRunId,
                    createLoadModelDataResponder(onLedgerEntriesLoaded));
        }

        public function onLedgerEntriesLoaded(e:ResultEvent):void {
            ledgerEntries = e.result as ArrayCollection;
        }


        [Bindable(event="contextPropertyChanged")]
        public function get pageLabel():String {
            return companyLedgerAccount == null ? "" : "Ledger Entries for the " + companyLedgerAccount.name + " account for the selected payroll"
        }
    }
}