package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.CompanyLedgerAccount;

    public class LedgerEntriesViewModel extends AbstractPartViewModel
    {

        [Bindable] [BackingProperty (context=true)] public var companyLedgerAccount:CompanyLedgerAccount;

        [ArrayElementType("psp.sap.model.PayrollTransaction")]
        private var mLedgerEntries:ArrayCollection = new ArrayCollection();


        public function LedgerEntriesViewModel() {
            this.shallowCopyFields = ["name", "ledgerAccountCode"];
        }

        public static function createActivator(companyLedgerAccount:CompanyLedgerAccount):Object {
            return {"companyLedgerAccount":companyLedgerAccount};
        }


        [Bindable]
        public function get ledgerEntries():ArrayCollection {
            return mLedgerEntries;
        }

        public function set ledgerEntries(value:ArrayCollection):void {
            mLedgerEntries = value;

            var sortDate:Sort = new Sort();
            var sortDateField:SortField = new SortField("createdDate",false,true,true);
            sortDate.fields = [sortDateField];
            mLedgerEntries.sort = sortDate;
            mLedgerEntries.refresh();
        }

        [Bindable(event="contextPropertyChanged")]
        public function get pageLabel():String {
            return companyLedgerAccount == null ? "" : "Ledger Entries for the " + companyLedgerAccount.name + " Account";
        }

        override protected function loadModelData():void {
            SAP.instance.payrollRunService.findTransactionsByLedgerAccount(
                    company.sourceSystemCd, company.companyId, companyLedgerAccount.ledgerAccountCode,
                    createLoadModelDataResponder(onLedgerEntriesLoaded));
        }

        public function onLedgerEntriesLoaded(e:ResultEvent):void {
            ledgerEntries = e.result as ArrayCollection;
        }

    }
}