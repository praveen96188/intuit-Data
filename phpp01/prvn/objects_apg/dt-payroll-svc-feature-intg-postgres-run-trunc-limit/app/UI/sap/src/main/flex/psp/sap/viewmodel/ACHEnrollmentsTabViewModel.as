package psp.sap.viewmodel {
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.PaginationCollection;
    import psp.sap.model.ACHEnrollmentDetail;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.SearchResults;

    public class ACHEnrollmentsTabViewModel extends CompositePartViewModel {

        protected var statusToFind:String;

        [Bindable]
        public var totalRecords:int = 0;

        [Bindable]
        public var pageSize:int = 15;

        [Bindable]
        public var canExportResults:Boolean = false;

        private var mEnrollmentsSearchResults:PaginationCollection = new PaginationCollection();

        public function ACHEnrollmentsTabViewModel() {
            super();
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.findACHEnrollments(statusToFind, enrollmentsSearchResults.startIndex, pageSize, enrollmentsSearchResults.sortBy, enrollmentsSearchResults.sortDesc, createLoadModelDataResponder(onSearchCompleted));
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        [Bindable]
        public function get enrollmentsSearchResults():PaginationCollection {
            return mEnrollmentsSearchResults;
        }

        public function set enrollmentsSearchResults(value:PaginationCollection):void {
            if (value == null) {
                value = new PaginationCollection();
            }
            mEnrollmentsSearchResults = value;
        }

        protected function onSearchCompleted(e:ResultEvent):void {
            var searchResults:SearchResults = e.result as SearchResults;
            enrollmentsSearchResults.totalRecords = searchResults.totalRecords;
            enrollmentsSearchResults.source = searchResults.returnsList.source;
            if (searchResults.totalRecords <= pageSize) {
                enrollmentsSearchResults.pageSize = searchResults.totalRecords;
            } else {
                enrollmentsSearchResults.pageSize = pageSize;
            }

            canExportResults = enrollmentsSearchResults.totalRecords > 0;
        }

        public function goToCompanyInfo(enrollmentDetail:ACHEnrollmentDetail):void {
            var companyKey:CompanyKey = enrollmentDetail.companyKey;
            companyKey.display();
        }

        public function viewACHHistory(data:ACHEnrollmentDetail):void {
            ACHEnrollmentsViewModel(host.host).viewACHHistory(data);
        }

    }
}