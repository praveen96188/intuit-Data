/**
 * User: dweinberg
 * Date: 2/21/13
 * Time: 1:15 PM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.PaymentTemplate;

    public class EditLawFlagsViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty (context=true)]
        public var paymentTemplate:PaymentTemplate;

        public static function createActivator(paymentTemplate:PaymentTemplate):Object {
            return {"paymentTemplate":paymentTemplate};
        }

        public function EditLawFlagsViewModel() {
        }

        [Bindable]
        [BackingProperty (recursive=true)]
        [ArrayElementType("psp.sap.model.LawFlags")]
        public var lawFlags:ArrayCollection;


        override protected function loadModelData():void {
            SAP.instance.taxService.getAllLawFlags(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, createLoadModelDataResponder(onLawFlagsLoaded));
        }

        private function onLawFlagsLoaded(e:ResultEvent):void {
            lawFlags = ArrayCollection(e.result);
        }


        override protected function executeSave():void {
            SAP.instance.taxService.updateLawFlags(companyKey.sourceSystemCd, companyKey.companyId, lawFlags, createSaveResponder());
        }
    }
}
