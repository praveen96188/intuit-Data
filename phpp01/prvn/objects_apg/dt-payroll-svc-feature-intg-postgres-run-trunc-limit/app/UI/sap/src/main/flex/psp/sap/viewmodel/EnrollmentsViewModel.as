package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.EnrollmentsPageEnum;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.EnrollmentDetail;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class EnrollmentsViewModel extends CompositePartViewModel {
        private var mEnrollmentsSearchResults:ArrayCollection = new ArrayCollection();

        private var mEFTPSEnrollmentHistoryPopUp:PopUpPartViewModel;
        private var mEFTPSEnrollmentHistoryViewModel:EFTPSEnrollmentsHistoryViewModel;

        private var mFilterString:String;

        [Bindable]
        public var totalRecords:int = 0;

        [Bindable]
        public function get enrollmentsSearchResults():ArrayCollection {
            return mEnrollmentsSearchResults;
        }

        public function set enrollmentsSearchResults(value:ArrayCollection):void {
            if (value == null) {
                value = new ArrayCollection();
            }
            mEnrollmentsSearchResults = value;
        }


        [Bindable]
        public function get filterString():String {
            if (null == mFilterString) {
                mFilterString = "";
            }
            return mFilterString;
        }

        public function set filterString(value:String):void {
            if (value == null) {
                value = "";
            }
            mFilterString = value;
            enrollmentsSearchResults.refresh();
        }

        public function EnrollmentsViewModel() {
            super();
            mEFTPSEnrollmentHistoryPopUp = addPopUpPart(EnrollmentsPageEnum.EFTPS_HISTORY);
            mEFTPSEnrollmentHistoryViewModel = mEFTPSEnrollmentHistoryPopUp.addNewPart(EFTPSEnrollmentsHistoryViewModel, EnrollmentsPageEnum.EFTPS_HISTORY) as EFTPSEnrollmentsHistoryViewModel;            
            mEFTPSEnrollmentHistoryViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getEFTPSEnrollmentRejections(createLoadModelDataResponder(onSearchCompleted));
        }

        /*  Callback function for searchComplete    */
        private function onSearchCompleted(e:ResultEvent):void {
            enrollmentsSearchResults = e.result as ArrayCollection;
            enrollmentsSearchResults.filterFunction = processFilter;
            totalRecords = enrollmentsSearchResults.length;
        }

        private function processFilter(item:EnrollmentDetail):Boolean {
            var result:Boolean = false;
            /*  If no filter text, or a match, then true    */
            if (!item.companyId
                    || item.companyId.toUpperCase().indexOf(this.filterString.toUpperCase()) == 0
                    || item.ein.toUpperCase().indexOf(this.filterString.toUpperCase()) == 0) {
                /*  If item's PSID || EIN "starts with" the filter string */
                result = true;
            }

            return result;
        }

        public function viewHistory(data:EnrollmentDetail):void {
            mEFTPSEnrollmentHistoryViewModel.targetCompanyKey = data.companyKey;
            mEFTPSEnrollmentHistoryPopUp.displayPopUp();
        }

        public function goToCompanyInfo(enrollmentDetail:EnrollmentDetail):void {
            var companyKey:CompanyKey = enrollmentDetail.companyKey;
            companyKey.display();
        }

        private function onViewModelDeactivated(e:Event):void {
            PopUpPartViewModel.refreshIfSaved(AbstractPartViewModel(e.target), this);
        }
    }
}