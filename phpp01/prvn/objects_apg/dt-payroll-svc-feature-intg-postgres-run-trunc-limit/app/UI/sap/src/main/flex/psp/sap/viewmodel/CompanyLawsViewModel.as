package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;

    public class CompanyLawsViewModel extends CompositePartViewModel {

        [ArrayElementType("psp.sap.model.CompanyLaw")]
        [Bindable] public var companyLaws:ArrayCollection;

        public function CompanyLawsViewModel() {
            super();
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.findCompanyLaws(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onCompanyLawsLoaded))
        }

        private function onCompanyLawsLoaded(e:ResultEvent):void {
            companyLaws = ArrayCollection(e.result);
        }

    }
}