package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.PaymentTemplate;

    public class DepositFrequencyHistoryPopUpViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty(context=true)]
        public var paymentTemplate:PaymentTemplate;

        public static function createActivator(paymentTemplate:PaymentTemplate):Object {
            return {"paymentTemplate":paymentTemplate};
        }

        [Bindable]
        [ArrayElementType("psp.sap.model.DepositFrequency")]
        public var depositFrequencies:ArrayCollection;

        [Bindable]
        public var defaultDepositFrequency:String;

        override protected function loadModelData():void {
            var paymentTemplateCd:String = paymentTemplate.followsFedDepositFrequency ? "IRS-941-PAYMENT" : paymentTemplate.paymentTemplateCd;

            loadCount = 2;
            SAP.instance.taxService.getDefaultDepositFrequency(paymentTemplateCd, createLoadModelDataResponder(onDefaultDepositFrequencyLoaded));
            SAP.instance.taxService.getDepositFrequencyHistory(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplateCd, createLoadModelDataResponder(onSearchCompleted));
        }

        private function onDefaultDepositFrequencyLoaded(e:ResultEvent):void {
            defaultDepositFrequency = String(e.result);
        }

        private function onSearchCompleted(e:ResultEvent):void {
            depositFrequencies = e.result as ArrayCollection;
        }
    }
}
