package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.PaymentTemplate;

    public class ACHRegistrationHistoryPopUpViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty(context=true)]
        public var paymentTemplate:PaymentTemplate;

        public static function createActivator(paymentTemplate:PaymentTemplate):Object {
            return {"paymentTemplate":paymentTemplate};
        }

        public function ACHRegistrationHistoryPopUpViewModel() {
            super();
            this.label = CompanyInspectorPageEnum.ACH_REGISTRATION_HISTORY;
        }

        [Bindable]
        [ArrayElementType("psp.sap.model.PaymentMethod")]
        public var achRegistrationHistory:ArrayCollection;

        override protected function loadModelData():void {
            SAP.instance.taxService.getPaymentMethodsHistory(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, "AgentEnabled", createLoadModelDataResponder(onACHRegistrationHistoryLoadCompleted));
        }

        private function onACHRegistrationHistoryLoadCompleted(e:ResultEvent):void {
            achRegistrationHistory = e.result as ArrayCollection;
        }

    }
}