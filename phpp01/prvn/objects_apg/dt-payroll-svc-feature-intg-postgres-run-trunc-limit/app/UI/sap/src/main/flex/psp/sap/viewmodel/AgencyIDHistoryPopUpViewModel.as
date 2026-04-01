package psp.sap.viewmodel {
import mx.collections.ArrayCollection;
import mx.rpc.events.ResultEvent;

import psp.sap.application.SAP;
import psp.sap.model.PaymentTemplate;

public class AgencyIDHistoryPopUpViewModel extends AbstractPartViewModel {
        public function AgencyIDHistoryPopUpViewModel() {
            super();
        }

        [Bindable]
        public var paymentTemplate:PaymentTemplate;

        [Bindable]
        [ArrayElementType("psp.sap.model.PropertyAudit")]
        public var agencyIds:ArrayCollection;

        override protected function loadModelData():void {
            SAP.instance.taxService.getAgencyIdHistory(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, createLoadModelDataResponder(onSearchCompleted));
        }

        private function onSearchCompleted(e:ResultEvent):void {
            agencyIds = e.result as ArrayCollection;
        }
    }
}
