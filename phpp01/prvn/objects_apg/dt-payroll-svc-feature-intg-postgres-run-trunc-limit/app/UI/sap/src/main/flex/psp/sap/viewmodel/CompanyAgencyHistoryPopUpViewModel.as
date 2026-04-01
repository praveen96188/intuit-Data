package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
import psp.sap.model.PaymentTemplate;

public class CompanyAgencyHistoryPopUpViewModel extends AbstractPartViewModel {
        public function CompanyAgencyHistoryPopUpViewModel() {
            super();
        }

        [Bindable]
        [ArrayElementType("psp.sap.model.PropertyAudit")]
        public var audits:ArrayCollection;

        override protected function loadModelData():void {
            SAP.instance.taxService.getCompanyAgencyHistory(companyKey.sourceSystemCd, companyKey.companyId, "IRS", createLoadModelDataResponder(onSearchCompleted));
        }

        private function onSearchCompleted(e:ResultEvent):void {
            audits = e.result as ArrayCollection;
        }
    }
}