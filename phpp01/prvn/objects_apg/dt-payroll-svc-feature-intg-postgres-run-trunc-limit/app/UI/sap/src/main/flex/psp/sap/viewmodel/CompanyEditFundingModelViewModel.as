package psp.sap.viewmodel {
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.LookupCollection;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.FundingModel;

    public class CompanyEditFundingModelViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty]
        public var fundingModel:FundingModel;

        private var mFundingModelCd:String;

        public function get fundingModelCd():String {
            return mFundingModelCd;
        }

        public function set fundingModelCd(value:String):void {
            mFundingModelCd = value;

            fundingModel = lookupFundingModel();
        }

        private function lookupFundingModel():FundingModel {
            if (fundingModelCd == null)
                return null;

            return fundingModels.getItemByKey(fundingModelCd) as FundingModel;
        }


        public function CompanyEditFundingModelViewModel() {
            this.label = CompanyInspectorPageEnum.FUNDING_MODEL;
            this.reloadOnSave = true;
        }

        [Bindable("propertyChange")]
        public function get fundingModels():LookupCollection {
            return SAP.instance.lookupService.fundingModels;
        }

        override protected function loadModelData():void {
            SAP.instance.companyService.getFundingModelCd(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onFundingModelLoaded));
        }

        private function onFundingModelLoaded(e:ResultEvent):void {
            fundingModelCd = String(e.result);
        }

        override protected function executeSave():void {
            SAP.instance.companyService.updateCompanyFundingModel(companyKey.sourceSystemCd, companyKey.companyId, fundingModelCd, createSaveResponder());
        }

    }
}