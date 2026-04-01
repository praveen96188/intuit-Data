package psp.sap.viewmodel {
    import mx.validators.StringValidator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.PaymentsPageEnum;
    import psp.sap.model.Payment;
    import psp.sap.validators.SAPValidators;

    public class PaymentRejectionViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty]
        public var payment:Payment;
        [Bindable]
        [BackingProperty]
        public var rejectionReason:String = "";

        [Bindable]
        public var reasonValidator:StringValidator;
        private const MAX_LENGTH:Number = 3398;

        public function PaymentRejectionViewModel() {
            super();
            this.label = PaymentsPageEnum.REJECTION_POPUP;
            reasonValidator = SAPValidators.createStringValidator(this, "rejectionReason", true, 0, MAX_LENGTH);
            validators.push(reasonValidator);
            this.reloadOnSave = true;
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        override protected function initializeDefaults():void {
            this.rejectionReason = "";
        }

        override protected function executeSave():void {
            SAP.instance.taxService.rejectPayment(payment.paymentId, rejectionReason, payment.companyId, createSaveResponder());
        }
    }
}
