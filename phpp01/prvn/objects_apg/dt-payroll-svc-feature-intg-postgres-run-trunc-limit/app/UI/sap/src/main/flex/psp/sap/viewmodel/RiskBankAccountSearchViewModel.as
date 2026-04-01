package psp.sap.viewmodel {
    import mx.rpc.events.ResultEvent;
    import mx.utils.StringUtil;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.PaginationCollection;
    import psp.sap.application.enums.RiskInspectorPageEnum;
    import psp.sap.model.SearchResults;

    public class RiskBankAccountSearchViewModel extends AbstractPartViewModel {
        private const EMPTY_STRING:String = "";

        // last search criteria
        private var mLastRoutingNumber:String;
        private var mLastAccountNumber:String;

        [Bindable] [ArrayElementType ("psp.sap.model.BankAccountSearchResult")]
        public var searchResults:PaginationCollection = new PaginationCollection();

        [Bindable] [BackingProperty (hasChanged=false)] public var routingNumber:String;
        [Bindable] [BackingProperty (hasChanged=false)] public var accountNumber:String;

        [Bindable] public var canUpdateBatchStatus:Boolean = false;

        public function RiskBankAccountSearchViewModel() {
            this.label = RiskInspectorPageEnum.BANK_ACCOUNT_SEARCH;
            this.reloadOnActivate = false;
			this.loadOnActivate = false;
            this.watchedEntities = []; //this keeps this from loading on activate when an external entity changes
			this.reloadOnSave = true;

            initDefaults();
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        public function getFilterString():String {
            var filterString:String = EMPTY_STRING;
            if(StringUtil.trim(routingNumber).length > 0) {
                filterString += "Routing Number: " + routingNumber + ", ";
            }
            if(StringUtil.trim(accountNumber).length > 0) {
                filterString += "Account Number: " + accountNumber + ", ";
            }

            filterString = filterString.substr(0, filterString.length - 2);
            return filterString;
        }

        public function search():void {
            searchResults.reset();

            // keep last search results so that we can use them for F9
            mLastRoutingNumber = routingNumber;
            mLastAccountNumber = accountNumber;

            refresh();
        }

        override protected function evaluateCanSave():Boolean {
            return super.evaluateCanSave() && (StringUtil.trim(routingNumber).length > 0 || StringUtil.trim(accountNumber).length > 0);
        }

        override protected function loadModelData():void {
            // change inputs back to last search inputs for F9 refresh
            routingNumber = mLastRoutingNumber;
            accountNumber = mLastAccountNumber;
            SAP.instance.companyService.findBankAccounts(
                    routingNumber,
                    accountNumber,
                    searchResults.sortBy,
                    searchResults.sortDesc,
                    searchResults.startIndex,
                    searchResults.pageSize,
                    createLoadModelDataResponder(onSearchCompleted));
        }

        private function initDefaults():void {
            searchResults = new PaginationCollection();

            routingNumber = EMPTY_STRING;
            accountNumber = EMPTY_STRING;

            mLastRoutingNumber = EMPTY_STRING;
            mLastAccountNumber = EMPTY_STRING;
        }

        protected function onSearchCompleted(e:ResultEvent):void {
            var searchReturn:SearchResults = e.result as SearchResults;
            searchResults.totalRecords = searchReturn.totalRecords;
            searchResults.source = searchReturn.returnsList.source;
        }
    }
}
