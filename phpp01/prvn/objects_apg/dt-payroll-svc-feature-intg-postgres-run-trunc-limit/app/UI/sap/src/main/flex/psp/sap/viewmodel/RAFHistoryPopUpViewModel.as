package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.EnrollmentsPageEnum;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.RAFEnrollmentHistory;

    public class RAFHistoryPopUpViewModel extends AbstractPartViewModel {
        public function RAFHistoryPopUpViewModel() {
            super();
            this.label = EnrollmentsPageEnum.RAF_HISTORY;
            this.reloadOnSave = true;

        }

        private var actionType:String;
        private var enrollmentId:String;
        private var mEnrollmentsSearchResults:RAFEnrollmentHistory;
        public var targetCompanyKey:CompanyKey;

        [Bindable]
        [ArrayElementType("psp.sap.model.RAFEnrollmentHistoryItem")]
        public var enrollments:ArrayCollection;

        /*  Can an EFTPS re-Enrollment be initiated on this company?    */
        [Bindable]
        public var canRe_Enroll:Boolean;

        [Bindable]
        public function get enrollmentsSearchResults():RAFEnrollmentHistory {
            return mEnrollmentsSearchResults;
        }

        public function set enrollmentsSearchResults(value:RAFEnrollmentHistory):void {
            mEnrollmentsSearchResults = value;
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getRAFEnrollmentsHistory(targetCompanyKey.sourceSystemCd, targetCompanyKey.companyId, createLoadModelDataResponder(onSearchCompleted));
        }

        /*  Callback function for back-end calls    */
        private function onSearchCompleted(e:ResultEvent):void {
            enrollmentsSearchResults = e.result as RAFEnrollmentHistory;
            enrollments = enrollmentsSearchResults.enrollments;
            canRe_Enroll = enrollmentsSearchResults.canRe_enroll;
        }

        override public function get isValid():Boolean {
            return canRe_Enroll;
        }

        override protected function executeSave():void {
            if (actionType == "InitiateReEnrollment") {
                SAP.instance.taxService.reInitiateRAFEnrollment(targetCompanyKey.sourceSystemCd, targetCompanyKey.companyId, createSaveResponder());
            }
            else {
                if (actionType == "DeleteEnrollment") {
                    SAP.instance.taxService.deleteRAFEnrollment(targetCompanyKey.sourceSystemCd, targetCompanyKey.companyId, this.enrollmentId, createSaveResponder());
                }
            }
            actionType = null;
        }

        public function initiateReEnrollment():void {
            actionType = "InitiateReEnrollment";
            forceSave();
        }

        public function deleteEnrollment(pEnrollmentId:String):void {
            actionType = "DeleteEnrollment";
            this.enrollmentId = pEnrollmentId;
            forceSave();
        }
    }
}