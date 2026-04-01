package psp.sap.viewmodel {
    import flash.utils.ByteArray;

    import psp.sap.application.SAP;
    import psp.sap.model.ACHEnrollmentDetail;
    import psp.sap.validators.SAPValidators;

    public class ACHEnrollmentsPendingResponseViewModel extends ACHEnrollmentsTabViewModel {

        [Bindable] [BackingProperty] public var fileName:String = "";
        private var mFile:ByteArray;

        private var achEnrollmentDetail:ACHEnrollmentDetail;
        private var actionType:String;

        public function ACHEnrollmentsPendingResponseViewModel() {
            super();
            reloadOnSave = true;
            statusToFind = "PendingEnrollmentResponse";

            validators.push(SAPValidators.createRequiredFieldValidator(this, "fileName"));
        }

        override protected function initializeBackingProperties():void {
            super.initializeBackingProperties();
            fileName = "";
            file = null;
        }

        override public function get hasChanged():Boolean {
            return file != null;
        }

        public function get file():ByteArray {
            return mFile;
        }

        public function set file(value:ByteArray):void {
            mFile = value;
            updateCanSave();
        }

        public function uploadResponseFile():void {
            actionType = "UploadFile";
            save();
        }

        public function markAsEnrolled(data:ACHEnrollmentDetail):void {
            achEnrollmentDetail = data;
            actionType = "MarkEnrolled";
            forceSave();
        }

        override protected function executeSave():void {
            if (this.actionType == "MarkEnrolled") {
                SAP.instance.taxService.updateACHEnrollmentAsEnrolled(achEnrollmentDetail.companyKey.sourceSystemCd, achEnrollmentDetail.companyKey.companyId, createSaveResponder());
            } else if(this.actionType == "UploadFile") {
                SAP.instance.taxService.uploadACHResponseFile(fileName, file, createSaveResponder());
            }
            actionType = null;
        }
    }
}