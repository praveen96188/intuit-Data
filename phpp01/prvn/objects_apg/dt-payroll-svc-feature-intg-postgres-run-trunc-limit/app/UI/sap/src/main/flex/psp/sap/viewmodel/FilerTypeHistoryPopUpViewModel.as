package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;

    public class FilerTypeHistoryPopUpViewModel extends AbstractPartViewModel {

        [Bindable]
        [ArrayElementType("psp.sap.model.FilerType")]
        public var filerTypes:ArrayCollection;

        override protected function loadModelData():void {
            SAP.instance.taxService.getFilerTypeHistory(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onSearchCompleted));
        }

        private function onSearchCompleted(e:ResultEvent):void {
            filerTypes = e.result as ArrayCollection;
        }

    }
}