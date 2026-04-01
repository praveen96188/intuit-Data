package psp.sap.viewmodel {
    import psp.sap.application.SAP;
    import psp.sap.model.RAFEnrollmentDetail;
    import psp.sap.model.RAFEnrollmentSearch;

    public class RAFPendingTapeViewModel extends RAFEnrollmentsViewModel {
        public function RAFPendingTapeViewModel() {
            super();
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getRAFEnrollmentsByStatusAndCompany(new RAFEnrollmentSearch("PendingEnrollmentTape", companyIds, null, null, null, null), false, enrollmentsSearchResults.startIndex, pageSize, createLoadModelDataResponder(onSearchCompleted));
        }

        override protected function executeSave():void {
            SAP.instance.taxService.updateRAFEnrollmentStatus(rafEnrollmentDetail.companyKey.sourceSystemCd, rafEnrollmentDetail.companyKey.companyId, "PendingEnrollment", createSaveResponder(refresh));
        }

        public function doMove(data:RAFEnrollmentDetail):void {
            rafEnrollmentDetail = data;
            save();
        }
    }
}