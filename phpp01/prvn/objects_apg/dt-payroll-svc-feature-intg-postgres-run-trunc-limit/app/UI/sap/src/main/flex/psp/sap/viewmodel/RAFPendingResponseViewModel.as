package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.validators.DateValidator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.EnrollmentsPageEnum;
    import psp.sap.model.RAFEnrollmentDetail;
    import psp.sap.model.RAFEnrollmentSearch;
    import psp.sap.validators.SAPStartEndDateValidator;
    import psp.sap.validators.SAPValidators;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class RAFPendingResponseViewModel extends RAFEnrollmentsViewModel {
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

        private var mRAFRejectionReasonPopUp:PopUpPartViewModel;
        private var mRAFRejectionReasonPopUpViewModel:RAFRejectionReasonPopUpViewModel;

        private var lastSearch:RAFEnrollmentSearch;


        public function RAFPendingResponseViewModel() {
            super();
            mRAFRejectionReasonPopUp = addPopUpPart(EnrollmentsPageEnum.RAF_REJECTION_REASON);
            mRAFRejectionReasonPopUp.closeOnSave = true;
            mRAFRejectionReasonPopUpViewModel = mRAFRejectionReasonPopUp.addNewPart(RAFRejectionReasonPopUpViewModel, EnrollmentsPageEnum.RAF_REJECTION_REASON) as RAFRejectionReasonPopUpViewModel;
            mRAFRejectionReasonPopUpViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);
            reloadOnSave = true;

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
            this.secondDateLabel="Tape Date";
        }

        override protected function loadModelData():void {
            lastSearch = new RAFEnrollmentSearch("PendingEnrollmentResponse", companyIds, creationDateStartValue, creationDateEndValue, lastUpdateStartValue, lastUpdateEndValue);
            SAP.instance.taxService.getRAFEnrollmentsByStatusAndCompany(lastSearch, false, enrollmentsSearchResults.startIndex, pageSize, createLoadModelDataResponder(onSearchCompleted));
        }

        public function viewRejectionReason(data:RAFEnrollmentDetail):void {
            mRAFRejectionReasonPopUpViewModel.targetCompanyKey = data.companyKey;
            mRAFRejectionReasonPopUp.displayPopUp();
        }

        private function onViewModelDeactivated(e:Event):void {
            PopUpPartViewModel.refreshIfSaved(AbstractPartViewModel(e.target), this);
        }

        override protected function executeSave():void {
            SAP.instance.taxService.enrollAllRAFEnrollments(lastSearch, createSaveResponder());
        }

        override protected function get savingMessage():String {
            return "Enrolling All";
        }

        public function saveAllAsEnrolled():void {
            forceSave();
        }
    }
}