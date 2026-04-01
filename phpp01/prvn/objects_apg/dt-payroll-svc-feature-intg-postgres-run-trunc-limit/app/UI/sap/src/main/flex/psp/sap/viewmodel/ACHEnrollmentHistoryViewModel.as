package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.EnrollmentsPageEnum;
    import psp.sap.model.ACHEnrollmentHistory;
    import psp.sap.model.CompanyKey;

    public class ACHEnrollmentHistoryViewModel extends AbstractPartViewModel {
        public function ACHEnrollmentHistoryViewModel() {
            super();
            this.label = EnrollmentsPageEnum.ACH_HISTORY;
            this.reloadOnSave = true;
        }

        private var actionType:String;
        private var enrollmentId:String;
        [Bindable] public var enrollmentsSearchResults:ACHEnrollmentHistory;
        public var targetCompanyKey:CompanyKey;

        [Bindable]
        [ArrayElementType("psp.sap.model.ACHEnrollmentHistoryItem")]
        public var enrollments:ArrayCollection;

        [Bindable]
        public var canRe_Enroll:Boolean;

        override public function get hasChanged():Boolean {
            return canRe_Enroll;
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getACHEnrollmentsHistory(targetCompanyKey.sourceSystemCd, targetCompanyKey.companyId, createLoadModelDataResponder(onSearchCompleted));
        }

        private function onSearchCompleted(e:ResultEvent):void {
            enrollmentsSearchResults = e.result as ACHEnrollmentHistory;
            enrollments = enrollmentsSearchResults.enrollments;
            canRe_Enroll = enrollmentsSearchResults.canRe_enroll;
        }

        override protected function executeSave():void {
            if (actionType == "InitiateReEnrollment") {
                SAP.instance.taxService.reInitiateACHEnrollment(targetCompanyKey.sourceSystemCd, targetCompanyKey.companyId, createSaveResponder());
            } else if (actionType == "DeleteEnrollment") {
                SAP.instance.taxService.deleteACHEnrollment(targetCompanyKey.sourceSystemCd, targetCompanyKey.companyId, this.enrollmentId, createSaveResponder());
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