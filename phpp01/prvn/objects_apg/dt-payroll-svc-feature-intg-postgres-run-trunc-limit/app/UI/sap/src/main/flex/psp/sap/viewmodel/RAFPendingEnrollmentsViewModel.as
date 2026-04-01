package psp.sap.viewmodel {
    import mx.validators.DateValidator;

    import psp.sap.application.SAP;
    import psp.sap.model.RAFEnrollmentDetail;
    import psp.sap.model.RAFEnrollmentSearch;
    import psp.sap.validators.SAPStartEndDateValidator;
    import psp.sap.validators.SAPValidators;

    public class RAFPendingEnrollmentsViewModel extends RAFEnrollmentsViewModel {
        /*  Filter validators    */
        [Bindable]
        public var creationDateStartValidator:DateValidator;
        [Bindable]
        public var creationDateEndValidator:DateValidator;
        [Bindable]
        public var creationDateStartAndEndValidator:SAPStartEndDateValidator;
        [Bindable]
        public var lastUpdateStartValidator:DateValidator;
        [Bindable]
        public var lastUpdateEndValidator:DateValidator;
        [Bindable]
        public var lastUpdateStartAndEndValidator:SAPStartEndDateValidator;

        public function RAFPendingEnrollmentsViewModel() {
            super();

            creationDateStartValidator = SAPValidators.createDefaultDatePropertyChangeValidator(this, "creationDateStart", false);
            this.validators.push(creationDateStartValidator);

            creationDateEndValidator = SAPValidators.createDefaultDatePropertyChangeValidator(this, "creationDateEnd", false);
            this.validators.push(creationDateEndValidator);

            creationDateStartAndEndValidator = SAPValidators.createSAPStartEndDateValidator(this, this, "creationDateStart", "creationDateEnd", false);
            this.validators.push(creationDateStartAndEndValidator);

            lastUpdateStartValidator = SAPValidators.createDefaultDatePropertyChangeValidator(this, "lastUpdateStart", false);
            this.validators.push(lastUpdateStartValidator);

            lastUpdateEndValidator = SAPValidators.createDefaultDatePropertyChangeValidator(this, "lastUpdateEnd", false);
            this.validators.push(lastUpdateEndValidator);

            lastUpdateStartAndEndValidator = SAPValidators.createSAPStartEndDateValidator(this, this, "lastUpdateStart", "lastUpdateEnd", false);
            this.validators.push(lastUpdateStartAndEndValidator);
            this.secondDateLabel="Last Update";
        }

        override protected function loadModelData():void {
            if(searchButtonClicked) {
                SAP.instance.taxService.getRAFEnrollmentsByStatusAndCompany(new RAFEnrollmentSearch("PendingEnrollment", companyIds, creationDateStartValue, creationDateEndValue, lastUpdateStartValue, lastUpdateEndValue), true, enrollmentsSearchResults.startIndex, pageSize, createLoadModelDataResponder(onSearchCompleted));
            } else {
                modelDataLoaded();
            }
        }

        override protected function executeSave():void {
            SAP.instance.taxService.updateRAFEnrollmentStatus(rafEnrollmentDetail.companyKey.sourceSystemCd, rafEnrollmentDetail.companyKey.companyId, "PendingEnrollmentTape", createSaveResponder(refresh));
        }

        public function doMove(data:RAFEnrollmentDetail):void {
            rafEnrollmentDetail = data;
            save();
        }
    }
}