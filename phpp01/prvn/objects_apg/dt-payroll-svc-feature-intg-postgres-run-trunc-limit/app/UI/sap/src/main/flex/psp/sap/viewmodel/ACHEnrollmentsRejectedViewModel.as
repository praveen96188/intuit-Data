package psp.sap.viewmodel {
    import psp.sap.application.SAP;
    import psp.sap.model.ACHEnrollmentDetail;

    public class ACHEnrollmentsRejectedViewModel extends ACHEnrollmentsTabViewModel {
        public function ACHEnrollmentsRejectedViewModel() {
            super();
            reloadOnSave = true;
            statusToFind = "EnrollmentRejected";
        }

        private var achEnrollmentDetail:ACHEnrollmentDetail;

        private var actionType:String;

        public function initiateReEnrollment(data:ACHEnrollmentDetail):void {
            achEnrollmentDetail = data;
            actionType = "InitiateReEnrollment";
            save();
        }

        public function markAsEnrolled(data:ACHEnrollmentDetail):void {
            achEnrollmentDetail = data;
            actionType = "MarkEnrolled";
            save();
        }

        override protected function executeSave():void {
            if (this.actionType == "MarkEnrolled") {
                SAP.instance.taxService.updateACHEnrollmentAsEnrolled(achEnrollmentDetail.companyKey.sourceSystemCd, achEnrollmentDetail.companyKey.companyId, createSaveResponder());
            } else if (this.actionType == "InitiateReEnrollment") {
                SAP.instance.taxService.reInitiateACHEnrollment(achEnrollmentDetail.companyKey.sourceSystemCd, achEnrollmentDetail.companyKey.companyId, createSaveResponder());
            }
            actionType = null;
        }


    }
}