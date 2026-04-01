package psp.sap.viewmodel {
    import psp.sap.application.SAP;
    import psp.sap.model.ACHEnrollmentDetail;

    public class ACHEnrollmentsDeleteViewModel extends ACHEnrollmentsTabViewModel {
        public function ACHEnrollmentsDeleteViewModel() {
            super();
            reloadOnSave = true;
            statusToFind = "PendingDelete";
        }

        private var achEnrollmentDetail:ACHEnrollmentDetail;

        override protected function executeSave():void {
            SAP.instance.taxService.cancelDeleteACHEnrollment(achEnrollmentDetail.companyKey.sourceSystemCd, achEnrollmentDetail.companyKey.companyId, achEnrollmentDetail.enrollmentId, createSaveResponder());
        }

        public function cancelDelete(data:ACHEnrollmentDetail):void {
            achEnrollmentDetail = data;
            forceSave();
        }
    }
}