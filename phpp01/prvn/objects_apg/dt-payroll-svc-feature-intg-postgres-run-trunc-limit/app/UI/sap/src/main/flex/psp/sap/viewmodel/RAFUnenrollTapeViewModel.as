package psp.sap.viewmodel {
    import psp.sap.application.SAP;
    import psp.sap.model.RAFEnrollmentDetail;
    import psp.sap.model.RAFEnrollmentSearch;

    public class RAFUnenrollTapeViewModel extends RAFEnrollmentsViewModel {
        public function RAFUnenrollTapeViewModel() {
            super();
            reloadOnSave = true;
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getRAFEnrollmentsByStatusAndCompany(new RAFEnrollmentSearch("PendingDeleteTape", companyIds, creationDateStartValue, creationDateEndValue, lastUpdateStartValue, lastUpdateEndValue), false, enrollmentsSearchResults.startIndex, pageSize, createLoadModelDataResponder(onSearchCompleted));
        }

        override protected function executeSave():void {
            SAP.instance.taxService.cancelDeleteRAFEnrollment(rafEnrollmentDetail.companyKey.sourceSystemCd, rafEnrollmentDetail.companyKey.companyId, rafEnrollmentDetail.enrollmentId, createSaveResponder());
        }

        public function cancelDelete(data:RAFEnrollmentDetail):void {
            rafEnrollmentDetail = data;
            forceSave();
        }
    }
}