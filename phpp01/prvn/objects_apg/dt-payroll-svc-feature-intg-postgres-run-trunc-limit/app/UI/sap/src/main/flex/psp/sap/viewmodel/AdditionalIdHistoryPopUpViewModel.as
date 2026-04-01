package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.CompanyKey;

    public class AdditionalIdHistoryPopUpViewModel extends AbstractPartViewModel {

        [Bindable]
        public var targetCompanyKey:CompanyKey;
        [Bindable]
        public var templateCode:String;

        [Bindable]
        [ArrayElementType("psp.sap.model.CompanyAgencyPaymentTemplateAgencyId")]
        public var additionalIdsHistory:ArrayCollection;

        public function AdditionalIdHistoryPopUpViewModel() {
            super();
            this.label = CompanyInspectorPageEnum.PSP_ADDITIONAL_AGENCY_IDS;
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getAdditionalAgencyIdsHistory(targetCompanyKey.sourceSystemCd, targetCompanyKey.companyId, templateCode, createLoadModelDataResponder(onAdditionalAgencyIdHistoryLoadCompleted));
        }

        private function onAdditionalAgencyIdHistoryLoadCompleted(e:ResultEvent):void {
            additionalIdsHistory = e.result as ArrayCollection;
        }
    }
}
