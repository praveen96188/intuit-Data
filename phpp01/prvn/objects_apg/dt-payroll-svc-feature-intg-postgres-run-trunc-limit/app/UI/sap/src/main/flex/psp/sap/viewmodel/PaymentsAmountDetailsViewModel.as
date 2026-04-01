package psp.sap.viewmodel {
import mx.collections.ArrayCollection;
import mx.rpc.events.ResultEvent;

import psp.sap.application.SAP;
import psp.sap.application.enums.PaymentsPageEnum;
import psp.sap.model.Payment;

public class PaymentsAmountDetailsViewModel extends AbstractPartViewModel {

        [Bindable] [BackingProperty] public var payment:Payment;
        [Bindable] [BackingProperty] public var paymentDetails:ArrayCollection;

        public function PaymentsAmountDetailsViewModel() {
            super();
            this.label = PaymentsPageEnum.AMOUNT_POPUP;
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getPaymentAmountDetails(payment.companyKey.sourceSystemCd, payment.companyId, payment.paymentId, createLoadModelDataResponder(onSearchCompleted));
        }

        /*  Callback function for back-end calls    */
        private function onSearchCompleted(e:ResultEvent):void {
            paymentDetails = e.result as ArrayCollection;
        }
    }
}