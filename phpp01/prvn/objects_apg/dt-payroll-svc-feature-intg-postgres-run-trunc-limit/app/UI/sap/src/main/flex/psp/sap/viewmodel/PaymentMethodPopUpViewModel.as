package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;

    public class PaymentMethodPopUpViewModel extends CompositePartViewModel {

        public function PaymentMethodPopUpViewModel() {
            super();
            this.label = CompanyInspectorPageEnum.PSP_PAYMENT_METHOD;
        }

        [Bindable]
        [BackingProperty(context=true)]
        public var templateCode:String;

        public static function createActivator(templateCode:String):Object {
            return {"templateCode":templateCode};
        }

        [Bindable]
        [ArrayElementType("psp.sap.model.PaymentMethod")]
        public var paymentMethods:ArrayCollection;

        [Bindable]
        [ArrayElementType("psp.sap.model.PaymentMethod")]
        public var paymentMethodsHistory:ArrayCollection;

        override protected function loadModelData():void {
            SAP.instance.taxService.getPaymentMethodsHistory(companyKey.sourceSystemCd, companyKey.companyId, templateCode, "Enabled",createLoadModelDataResponder(onPaymentMethodsHistoryLoadCompleted));
        }

        private function onPaymentMethodsHistoryLoadCompleted(e:ResultEvent):void {
            paymentMethodsHistory = e.result as ArrayCollection;
        }

    }
}
