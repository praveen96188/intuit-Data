package psp.sap.viewmodel {
    import flash.events.Event;
    import flash.net.URLRequest;
    import flash.net.URLVariables;
    import flash.net.navigateToURL;

    import mx.rpc.events.ResultEvent;
    import mx.utils.StringUtil;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.PaginationCollection;
    import psp.sap.application.enums.EnrollmentsPageEnum;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.RAFEnrollmentDetail;
    import psp.sap.model.SearchResults;
    import psp.sap.viewmodel.events.ViewModelEvent;
    import psp.app.util.CommonUtil;

    public class RAFEnrollmentsViewModel extends CompositePartViewModel {
        private const EMPTY_STRING:String = "";
        [Bindable]
        public var totalRecords:int = 0;

        [Bindable]
        /*  15 is for default resolution of 1024x768, with filterBox collapsed    */
        public var pageSize:int = 15;

        [Bindable]
        public var mCompanyIds:String;

        [Bindable]
        public var searchButtonClicked:Boolean = false;

        private var mEnrollmentsSearchResults:PaginationCollection = new PaginationCollection();
        protected var secondDateLabel:String;

        private var mRAFHistoryPopUp:PopUpPartViewModel;
        private var mRAFHistoryPopUpViewModel:RAFHistoryPopUpViewModel;
        protected var rafEnrollmentDetail:RAFEnrollmentDetail;

        [Bindable]
        [BackingProperty]
        public var creationDateStart:String;
        [Bindable]
        [BackingProperty]
        public var creationDateEnd:String;
        [Bindable]
        [BackingProperty]
        public var lastUpdateStart:String;
        [Bindable]
        [BackingProperty]
        public var lastUpdateEnd:String;

        public function get creationDateStartValue():Date {
            if (creationDateStart == "") {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(creationDateStart);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        public function get creationDateEndValue():Date {
            if (creationDateEnd == "") {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(creationDateEnd);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        public function get lastUpdateStartValue():Date {
            if (lastUpdateStart == "") {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(lastUpdateStart);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        public function get lastUpdateEndValue():Date {
            if (lastUpdateEnd == "") {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(lastUpdateEnd);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        public function getFilterLabelString():String {
            var filterLabelString:String = EMPTY_STRING;
            if (StringUtil.trim(mCompanyIds).length > 0) {
                filterLabelString += "PSID/EIN(s): (" + StringUtil.trim(companyIds).replace(/\s+/g, ", ") + "), ";
            }
            if (StringUtil.trim(creationDateStart).length > 0) {
                filterLabelString += "Creation Date Start: " + creationDateStart + ", ";
            }
            if (StringUtil.trim(creationDateEnd).length > 0) {
                filterLabelString += "Creation Date End: " + creationDateEnd + ", ";
            }
            if (StringUtil.trim(lastUpdateStart).length > 0) {
                filterLabelString += secondDateLabel + " Start: " + lastUpdateStart + ", ";
            }
            if (StringUtil.trim(lastUpdateEnd).length > 0) {
                filterLabelString += secondDateLabel + " End: " + lastUpdateEnd;
            }
            return filterLabelString;
        }

        public function get rafHistoryPopUp():PopUpPartViewModel {
            return mRAFHistoryPopUp;
        }

        public function set rafHistoryPopUp(value:PopUpPartViewModel):void {
            mRAFHistoryPopUp = value;
        }

        public function get rafHistoryPopUpViewModel():RAFHistoryPopUpViewModel {
            return mRAFHistoryPopUpViewModel;
        }

        public function set rafHistoryPopUpViewModel(value:RAFHistoryPopUpViewModel):void {
            mRAFHistoryPopUpViewModel = value;
        }

        public function RAFEnrollmentsViewModel() {
            super();

            rafHistoryPopUp = addPopUpPart(EnrollmentsPageEnum.RAF_HISTORY);
            rafHistoryPopUpViewModel = rafHistoryPopUp.addNewPart(RAFHistoryPopUpViewModel, EnrollmentsPageEnum.RAF_HISTORY) as RAFHistoryPopUpViewModel;
            rafHistoryPopUpViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            this.creationDateStart = "";
            this.creationDateEnd = "";
            this.lastUpdateStart = "";
            this.lastUpdateEnd = "";
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

        [Bindable]
        public function get companyIds():String {
            if (null == mCompanyIds) {
                mCompanyIds = "";
            }
            return mCompanyIds;
        }

        public function set companyIds(value:String):void {
            if (value == null) {
                value = "";
            }
            mCompanyIds = value;
        }

        /*  Callback function for searchComplete    */
        protected function onSearchCompleted(e:ResultEvent):void {
            var searchResults:SearchResults = e.result as SearchResults;
            enrollmentsSearchResults.totalRecords=searchResults.totalRecords;
            enrollmentsSearchResults.source=searchResults.returnsList.source;
            if (searchResults.totalRecords <= pageSize) {
                enrollmentsSearchResults.pageSize = searchResults.totalRecords;
            }
            else {
                enrollmentsSearchResults.pageSize = pageSize;
            }
        }

        public function goToCompanyInfo(enrollmentDetail:RAFEnrollmentDetail):void {
            var companyKey:CompanyKey = enrollmentDetail.companyKey;
            companyKey.display();
        }

        public function viewRAFHistory(data:RAFEnrollmentDetail):void {
            rafHistoryPopUpViewModel.targetCompanyKey = data.companyKey;
            rafHistoryPopUp.displayPopUp();
        }

        private function onViewModelDeactivated(e:Event):void {
            PopUpPartViewModel.refreshIfSaved(AbstractPartViewModel(e.target), this);
        }

        public function exportEnrollments(reportType:String):void {
            var urlRequest:URLRequest = new URLRequest("ViewRAFEnrollments");
            urlRequest.method = "GET";
            var uv:URLVariables = new URLVariables();
            uv.token = blankIfNull(SAP.instance.session.user.authorizationToken);
            uv.enrollmentStatus = reportType;
            uv.psid_ein = blankIfNull(companyIds);
            uv.creationDateStart = blankIfNull(creationDateStart.toString());
            uv.creationDateEnd = blankIfNull(creationDateEnd.toString());
            uv.lastUpdateStart = blankIfNull(lastUpdateStart.toString());
            uv.lastUpdateEnd = blankIfNull(lastUpdateEnd.toString());
            urlRequest.data = uv;
            if(CommonUtil.isDTApp()) {
                CommonUtil.downloadFromSAPURL(urlRequest.url, "ViewRAFEnrollments");
            } else {
                navigateToURL(urlRequest, "_self");
            }
        }

        protected function blankIfNull(c:*):* {
            return (c == null) ? "" : c;
        }

    }
}