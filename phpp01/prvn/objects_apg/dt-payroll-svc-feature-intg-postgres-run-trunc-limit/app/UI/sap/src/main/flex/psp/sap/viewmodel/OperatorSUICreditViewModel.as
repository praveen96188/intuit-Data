/**
 * User: dweinberg
 * Date: 12/5/12
 * Time: 12:42 PM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.Quarter;
    import psp.sap.validators.SAPValidators;

    public class OperatorSUICreditViewModel extends AbstractPartViewModel {

        [ArrayElementType("String")]
        [Bindable] public var availableTemplates:ArrayCollection;

        [Bindable]
        [BackingProperty]
        public var selectedPaymentTemplate:String;

        [Bindable]
        [BackingProperty]
        public var selectedYear:String;

        [Bindable]
        public var quarterList:Array = ["", "Q1", "Q2", "Q3", "Q4"];

        [Bindable]
        [BackingProperty]
        public var selectedQuarter:String;

        [Bindable]
        [ArrayElementType("psp.sap.model.SUICreditsJob")]
        public var currentJobs:ArrayCollection;

        public function OperatorSUICreditViewModel() {
            reloadOnSave = true;

            validators.push(SAPValidators.createNumberValidator(this, "selectedYear", true, 1900, 2100, false, 1));
            validators.push(SAPValidators.createRequiredFieldValidator(this, "selectedQuarter"));
        }


        override protected function onActivating():void {
            selectedQuarter = quarterList[0];
        }

        override protected function loadModelData():void {
            loadCount++;
            SAP.instance.administrationService.getAvailableSUICreditTemplates(createLoadModelDataResponder(onPaymentTemplatesLoaded));
            SAP.instance.administrationService.getSUICreditsJobList(createLoadModelDataResponder(onJobsLoaded));
        }

        private function onPaymentTemplatesLoaded(e:ResultEvent):void {
            var tempTemplates:ArrayCollection = ArrayCollection(e.result);
            tempTemplates.addItemAt("", 0);
            availableTemplates = tempTemplates;
        }

        private function onJobsLoaded(e:ResultEvent):void {
            currentJobs = ArrayCollection(e.result);
        }

        override protected function initializeBackingProperties():void {
            selectedPaymentTemplate = String(availableTemplates.getItemAt(0));
        }

        override protected function executeSave():void {
            SAP.instance.administrationService.createSUICreditsJob(new Quarter(parseInt(selectedYear), quarterList.indexOf(selectedQuarter)),
                    selectedPaymentTemplate,
                    createSaveResponder())
        }
    }
}
