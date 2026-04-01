/**
 * User: dweinberg
 * Date: 3/4/13
 * Time: 4:25 PM
 */
package psp.sap.viewmodel {
    import flash.events.Event;
    import flash.events.TimerEvent;
    import flash.utils.Timer;

    import mx.collections.ArrayCollection;
    import mx.rpc.Responder;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.AgencyIdRequirement;
    import psp.sap.model.CompanyAgencyPaymentTemplateAgencyId;
    import psp.sap.model.PaymentMethodAgencyIdRequirements;
    import psp.sap.model.PaymentTemplate;

    public class EditAgencyIdViewModel extends AbstractPartViewModel {

        public static const REQUIREMENTS_UPDATED:String = "requirementsUpdated";

        [Bindable]
        [BackingProperty(context=true)]
        public var paymentTemplate:PaymentTemplate;

        public static function createActivator(paymentTemplate:PaymentTemplate):Object {
            return {"paymentTemplate":paymentTemplate};
        }

        public function EditAgencyIdViewModel() {
            checkTimer = new Timer(500, 1);
            checkTimer.addEventListener(TimerEvent.TIMER, function(e:Event):void {
                SAP.instance.taxService.checkAgencyIDs(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, agencyIds, new Responder(onAgencyIdCheckLoaded, onLoadModelDataFaulted));
            });
        }

        [Bindable]
        [BackingProperty]
        [ArrayElementType("psp.sap.model.CompanyAgencyPaymentTemplateAgencyId")]
        public var agencyIds:ArrayCollection;

        private var hasAnyRequirements:Boolean;

        private var checkTimer:Timer;

        override protected function loadModelData():void {
            SAP.instance.taxService.findAgencyIDs(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, createLoadModelDataResponder(onAgencyIdsLoaded));
        }

        private function onAgencyIdsLoaded(e:ResultEvent):void {
            agencyIds = ArrayCollection(e.result);
        }

        override protected function initializeBackingProperties():void {
            updateWarnings();
        }

        public function onAgencyIdUserChange():void {
            if (hasAnyRequirements) { //no need to call server if nothing to update
                checkTimer.stop();
                checkTimer.reset();
                checkTimer.start();
            }
        }

        private function onAgencyIdCheckLoaded(e:ResultEvent):void {
            //don't want the repeaters to re-execute, so will just update the relevant information
            //best code ever
            var tempAgencyIds:ArrayCollection = ArrayCollection(e.result);
            for (var i:int = 0; i < tempAgencyIds.length; i++) {
                var agencyId:CompanyAgencyPaymentTemplateAgencyId = CompanyAgencyPaymentTemplateAgencyId(tempAgencyIds.getItemAt(i));
                for (var j:int = 0; j < agencyId.paymentMethodRequirements.length; j++) {
                    var paymentMethodRequirements:PaymentMethodAgencyIdRequirements = PaymentMethodAgencyIdRequirements(agencyId.paymentMethodRequirements.getItemAt(j));
                    for (var k:int = 0; k < paymentMethodRequirements.requirements.length; k++) {
                        var requirement:AgencyIdRequirement = AgencyIdRequirement(paymentMethodRequirements.requirements.getItemAt(k));
                        AgencyIdRequirement(PaymentMethodAgencyIdRequirements(CompanyAgencyPaymentTemplateAgencyId(agencyIds.getItemAt(i)).paymentMethodRequirements.getItemAt(j)).requirements.getItemAt(k)).isFulfilled = requirement.isFulfilled;
                    }
                }
            }

            dispatchEvent(new Event(REQUIREMENTS_UPDATED));
            updateWarnings();
        }

        private function updateWarnings():void {
            hasAnyRequirements = false;
            for each (var agencyId:CompanyAgencyPaymentTemplateAgencyId in agencyIds) {
                agencyId.warningText = null;
                for each (var paymentMethodRequirements:PaymentMethodAgencyIdRequirements in agencyId.paymentMethodRequirements) {
                    for each (var requirement:AgencyIdRequirement in paymentMethodRequirements.requirements) {
                        if (!requirement.isFulfilled) {
                            agencyId.warningText = "One or more payment methods will be disabled";
                        }
                        hasAnyRequirements = true;
                    }
                }
            }
        }


        override protected function executeSave():void {
            SAP.instance.taxService.updateAgencyIDs(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, agencyIds, createSaveResponder());
        }
    }
}
