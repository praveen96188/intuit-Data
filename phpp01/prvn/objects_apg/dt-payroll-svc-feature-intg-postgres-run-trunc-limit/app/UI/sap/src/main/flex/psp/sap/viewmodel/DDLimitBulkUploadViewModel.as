package psp.sap.viewmodel {
    import psp.sap.application.SAP;
    import psp.sap.validators.SAPValidators;

    public class DDLimitBulkUploadViewModel extends AbstractPartViewModel {

        [Bindable] [BackingProperty] public var fileName:String;

        public var fileContents:String;

        public function DDLimitBulkUploadViewModel() {
            validators.push(SAPValidators.createRequiredFieldValidator(this, "fileName"));
        }

        override protected function executeSave():void {
            SAP.instance.administrationService.processBulkDDLimitUpdates(fileContents, createSaveResponder());
        }
    }
}
