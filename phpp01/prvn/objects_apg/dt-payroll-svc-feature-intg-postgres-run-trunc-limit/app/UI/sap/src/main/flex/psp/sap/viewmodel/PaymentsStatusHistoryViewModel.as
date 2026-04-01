package psp.sap.viewmodel {
import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.rpc.events.ResultEvent;

import psp.sap.application.SAP;
import psp.sap.application.enums.PaymentsPageEnum;
import psp.sap.model.Payment;

public class PaymentsStatusHistoryViewModel extends AbstractPartViewModel {

        [Bindable] [BackingProperty] public var payment:Payment;
        [ArrayElementType("psp.sap.model.PropertyAudit")]
        [Bindable] [BackingProperty] public var auditData:ArrayCollection;

        public function PaymentsStatusHistoryViewModel() {
            super();
            this.label = PaymentsPageEnum.STATUS_POPUP;
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getStatusHistoryData(payment.paymentId, payment.companyId, createLoadModelDataResponder(onSearchCompleted));
        }

        /*  Callback function for back-end calls    */
        private function onSearchCompleted(e:ResultEvent):void {
            auditData = e.result as ArrayCollection;

            var sort:Sort = new Sort();
            sort.fields = [new SortField("auditDate")];
            auditData.sort = sort;
            auditData.refresh();
        }
    }
}