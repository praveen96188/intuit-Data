/**
 * User: kperry
 * Date: 8/6/12
 * Time: 4:00 PM
 */
package psp.sap.viewmodel {
    import mx.rpc.events.ResultEvent;
    import mx.collections.ArrayCollection;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.PaymentsPageEnum;

    import psp.sap.model.PaymentSearch;
    import psp.sap.model.SearchResults;

    public class UpdatePaymentMethodsViewModel extends AbstractPartViewModel {

        [Bindable] public var searchCriteria:PaymentSearch;
        [Bindable] public var searchResults:SearchResults;

        [Bindable] public var paymentsChanged:int;

        [Bindable]
        [ArrayElementType("String")]
        public var paymentMethods:ArrayCollection;

        [Bindable]
        [BackingProperty]
        public var newPaymentMethod:String = "";

        public function UpdatePaymentMethodsViewModel() {
            super();
            this.label = PaymentsPageEnum.UPDATE_PAYMENT_METHODS;
        }

        override protected function loadModelData():void {
             SAP.instance.taxService.getValidPaymentMethodsByTemplate(searchCriteria.paymentTemplate, createLoadModelDataResponder(onPaymentMethodResults));
        }

        public function onPaymentMethodResults(e:ResultEvent):void {
            paymentMethods = e.result as ArrayCollection;
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        override protected function initializeDefaults():void {
            this.newPaymentMethod = paymentMethods.getItemAt(0).toString();
        }

        override protected function executeSave():void {
            SAP.instance.taxService.updateGroupPaymentMethods(searchCriteria, newPaymentMethod, createSaveResponder(onPaymentsChanged));
        }

        private function onPaymentsChanged(e:ResultEvent):void {
            paymentsChanged = e.result as int;
            // setting the saveMsg in PaymentsViewModel
            this.host.host.host.host.host.host.saveMsg = paymentsChanged + " of " + searchResults.totalRecords + " payments changed to " + newPaymentMethod + ".";
        }

    }
}
