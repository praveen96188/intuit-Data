package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.EnrollmentsPageEnum;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.EftpsEnrollmentHistory;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class EFTPSEnrollmentsHistoryViewModel extends CompositePartViewModel {

        private var mManualEFTPSEnrollmentPopUp:PopUpPartViewModel;
        private var mManualEFTPSEnrollmentViewModel:EFTPSManualEnrollmentViewModel;

        public function EFTPSEnrollmentsHistoryViewModel() {
            super();
            this.label = EnrollmentsPageEnum.EFTPS_HISTORY;
            this.reloadOnSave = true;

            mManualEFTPSEnrollmentPopUp = addPopUpPart(CompanyInspectorPageEnum.TAX_EFTPS_MANUAL_ENROLLMENT);
            mManualEFTPSEnrollmentViewModel = mManualEFTPSEnrollmentPopUp.addNewPart(EFTPSManualEnrollmentViewModel, CompanyInspectorPageEnum.TAX_EFTPS_MANUAL_ENROLLMENT) as EFTPSManualEnrollmentViewModel;
            mManualEFTPSEnrollmentViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);
        }

        private var mEnrollmentsSearchResults:EftpsEnrollmentHistory;
        public var targetCompanyKey:CompanyKey;

        [Bindable]
        public var enrollments:ArrayCollection;

        /*  Can an EFTPS re-Enrollment be initiated on this company?    */
        [Bindable]
        public var canRe_Enroll:Boolean;

        [Bindable]
        public function get enrollmentsSearchResults():EftpsEnrollmentHistory {
            return mEnrollmentsSearchResults;
        }

        public function set enrollmentsSearchResults(value:EftpsEnrollmentHistory):void {
            mEnrollmentsSearchResults = value;
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getEftpsEnrollmentsHistory(targetCompanyKey.sourceSystemCd, targetCompanyKey.companyId, createLoadModelDataResponder(onSearchCompleted));
        }

        /*  Callback function for back-end calls    */
        private function onSearchCompleted(e:ResultEvent):void {
            enrollmentsSearchResults = e.result as EftpsEnrollmentHistory;
            enrollments = enrollmentsSearchResults.enrollments;
            canRe_Enroll = enrollmentsSearchResults.canRe_enroll;
        }

        override public function get isValid():Boolean {
            return canRe_Enroll;
        }

        override protected function executeSave():void {
            SAP.instance.taxService.initiateReEnrollment(targetCompanyKey.sourceSystemCd, targetCompanyKey.companyId, createSaveResponder());
        }

        public function initiateReEnrollment():void {
            save();
        }

        public function createManualEFTPSEnrollment():void {
            mManualEFTPSEnrollmentPopUp.displayPopUp();
        }

        private function onViewModelDeactivated(e:Event):void {
            PopUpPartViewModel.refreshIfSaved(AbstractPartViewModel(e.target), this);
        }

    }
}