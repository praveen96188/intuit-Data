package psp.sap.viewmodel {

import mx.validators.StringValidator;

import psp.sap.application.SAP;
import psp.sap.application.enums.PaymentsPageEnum;
import psp.sap.model.Payment;
import psp.sap.validators.SAPValidators;

public class RefundPaymentViewModel extends AbstractPartViewModel {
        [Bindable]
        [BackingProperty]
        public var payment:Payment;

        [Bindable]
        [BackingProperty]
        public var refundReason:String = "";

        [Bindable]
        public var reasonValidator:StringValidator;
        private const MAX_LENGTH:Number = 3398;

        public function RefundPaymentViewModel() {
            super();
            this.label = PaymentsPageEnum.REFUND_PAYMENT_POPUP;
            reasonValidator = SAPValidators.createStringValidator(this, "refundReason", true, 0, MAX_LENGTH);
            validators.push(reasonValidator);
            this.reloadOnSave = true;
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        override protected function initializeDefaults():void {
            this.refundReason = "";
        }

        override protected function executeSave():void {
            SAP.instance.taxService.createPendingTaxRefund("QBDT", payment.companyId, payment.paymentId, refundReason, createSaveResponder());
        }


    }
}
