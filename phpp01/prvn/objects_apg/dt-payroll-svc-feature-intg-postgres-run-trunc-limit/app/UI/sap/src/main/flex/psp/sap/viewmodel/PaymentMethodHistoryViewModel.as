package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.PaymentsPageEnum;
    import psp.sap.model.Payment;

    public class PaymentMethodHistoryViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty]
        public var payment:Payment;
        [Bindable]
        [BackingProperty]
        public var auditData:ArrayCollection;

        public function PaymentMethodHistoryViewModel() {
            super();
            this.label = PaymentsPageEnum.PAYMENT_METHOD_POPUP;
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getPaymentMethodAuditHistory(payment.paymentId, payment.companyId, createLoadModelDataResponder(onSearchCompleted));
        }

        /*  Callback function for back-end calls    */
        private function onSearchCompleted(e:ResultEvent):void {
            auditData = e.result as ArrayCollection;
        }
    }
}
