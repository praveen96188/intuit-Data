package psp.sap.viewmodel {
    import psp.sap.application.SAP;
    import psp.sap.model.RAFEnrollmentDetail;
    import psp.sap.model.RAFEnrollmentSearch;

    public class RAFRejectedViewModel extends RAFEnrollmentsViewModel {
        public function RAFRejectedViewModel() {
            super();
            reloadOnSave = true;
        }

        private var actionType:String;

        override protected function loadModelData():void {
            SAP.instance.taxService.getRAFEnrollmentsByStatusAndCompany(new RAFEnrollmentSearch("Rejected", companyIds, creationDateStartValue, creationDateEndValue, lastUpdateStartValue, lastUpdateEndValue), false, enrollmentsSearchResults.startIndex, pageSize, createLoadModelDataResponder(onSearchCompleted));
        }

        public function initiateRAFReEnrollment(data:RAFEnrollmentDetail):void {
            rafEnrollmentDetail = data;
            actionType = "InitiateReEnrollment";
            save();
        }

        override protected function executeSave():void {
            if (this.actionType == "MarkEnrolled") {
                SAP.instance.taxService.updateRAFEnrollmentStatus(rafEnrollmentDetail.companyKey.sourceSystemCd, rafEnrollmentDetail.companyKey.companyId, "Enrolled", createSaveResponder());
            }
            else {
                if (this.actionType == "InitiateReEnrollment") {
                    SAP.instance.taxService.reInitiateRAFEnrollment(rafEnrollmentDetail.companyKey.sourceSystemCd, rafEnrollmentDetail.companyKey.companyId, createSaveResponder());
                }
            }
            actionType = null;
        }

        public function rafEnroll(data:RAFEnrollmentDetail):void {
            rafEnrollmentDetail = data;
            actionType = "MarkEnrolled";
            save();
        }
    }
}