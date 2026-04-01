package psp.sap.viewmodel {
    import mx.events.PropertyChangeEvent;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.EnrollmentsPageEnum;
    import psp.sap.model.CompanyKey;
    import psp.sap.validators.SAPValidators;

    public class RAFRejectionReasonPopUpViewModel extends AbstractPartViewModel {
        public var targetCompanyKey:CompanyKey;

        [Bindable]
        public var rejectionReasonValidator:Validator;

        [Bindable]
        public var rejectionReasonSelectionValidator:Validator;

        [Bindable]
        [BackingProperty]
        public var rejectionReason:String;

        private var mRejectionReasonString:String;

        public function RAFRejectionReasonPopUpViewModel() {
            super();
            this.label = EnrollmentsPageEnum.RAF_REJECTION_REASON;
            this.reloadOnSave = true;
            rejectionReasonValidator = SAPValidators.createRequiredFieldValidator(this, "rejectionReason", true);
            rejectionReasonValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
            rejectionReasonValidator.trigger = this;

            this.validators.push(rejectionReasonValidator);
            rejectionReasonSelectionValidator = SAPValidators.createRequiredFieldValidator(this, "rejectionReasonString", true);
            rejectionReasonSelectionValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
            rejectionReasonSelectionValidator.trigger = this;
            this.validators.push(rejectionReasonSelectionValidator);
        }

        [Bindable]
        public function get rejectionReasonString():String {
            return mRejectionReasonString;
        }

        public function set rejectionReasonString(value:String):void {
            mRejectionReasonString = value;
            if (value == 'Other') {
                rejectionReasonValidator.required = true;
                rejectionReason = "";
            }
            else {
                rejectionReason = value;
                rejectionReasonValidator.required = false;
            }
        }

        override protected function initializeDefaults():void {
            this.rejectionReason = "";
            this.rejectionReasonString = "";
            rejectionReasonValidator.required = false;
        }

        override protected function executeSave():void {
            SAP.instance.taxService.rejectRAFEnrollment(targetCompanyKey.sourceSystemCd, targetCompanyKey.companyId, rejectionReason, createSaveResponder());
        }

        override public function get hasChanged():Boolean {
            return true;
        }
    }
}