package psp.sap.viewmodel {
    import psp.sap.application.SAP;
    import psp.sap.model.ACHEnrollmentDetail;

    public class ACHEnrollmentsPendingViewModel extends ACHEnrollmentsTabViewModel {
        public function ACHEnrollmentsPendingViewModel() {
            super();
            reloadOnSave = true;
            statusToFind = "PendingEnrollment";
        }

        private var achEnrollmentDetail:ACHEnrollmentDetail;

        private var actionType:String;

        public function markAsEnrolled(data:ACHEnrollmentDetail):void {
            achEnrollmentDetail = data;
            actionType = "MarkEnrolled";
            save();
        }

        override protected function executeSave():void {
            if (this.actionType == "MarkEnrolled") {
                SAP.instance.taxService.updateACHEnrollmentAsEnrolled(achEnrollmentDetail.companyKey.sourceSystemCd, achEnrollmentDetail.companyKey.companyId, createSaveResponder());
            }
            actionType = null;
        }

    }
}