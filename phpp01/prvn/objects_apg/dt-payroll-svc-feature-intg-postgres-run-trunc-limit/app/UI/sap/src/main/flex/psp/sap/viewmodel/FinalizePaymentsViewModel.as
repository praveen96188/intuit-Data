/**
 * User: dweinberg
 * Date: 1/9/12
 * Time: 5:12 PM
 */
package psp.sap.viewmodel {
    import psp.sap.application.SAP;
    import psp.sap.model.PaymentSearch;
    import psp.sap.model.SearchResults;
    import mx.rpc.events.ResultEvent;

    public class FinalizePaymentsViewModel extends AbstractPartViewModel {
        [Bindable] public var searchCriteria:PaymentSearch;
        [Bindable] public var searchResults:SearchResults;
        [Bindable] public var paymentsChanged:int;

        public function  FinalizePaymentsViewModel() {
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        override protected function executeSave():void {
            SAP.instance.taxService.finalizePayments(searchCriteria, createSaveResponder(onPaymentsChanged));
        }

        private function onPaymentsChanged(e:ResultEvent):void {
            paymentsChanged = e.result as int;
            // setting the saveMsg in PaymentsViewModel
            this.host.host.host.host.host.host.saveMsg = paymentsChanged + " of " + searchResults.totalRecords + " payments finalized.";
        }

    }
}
