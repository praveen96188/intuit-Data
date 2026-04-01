/**
 * User: dweinberg
 * Date: 12/5/12
 * Time: 12:42 PM
 */
package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.validators.NumberValidator;

    import psp.sap.application.SAP;
    import psp.sap.application.events.SAPEvent;
    import psp.sap.model.Agency;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.validators.SAPValidators;

    public class OperatorLedgerTORViewModel extends AbstractPartViewModel {

        [ArrayElementType("psp.sap.model.Agency")]
        [Bindable] public var agencyList:ArrayCollection;

        private var mSelectedAgency:Agency;
        [Bindable]
        [BackingProperty]
        public function get selectedAgency():Agency {
            return mSelectedAgency;
        }

        public function set selectedAgency(value:Agency):void {
            mSelectedAgency = value;

            if(mSelectedAgency != null && mSelectedAgency.paymentTemplates != null){
                paymentTemplates = mSelectedAgency.paymentTemplates;
            }
            else {
                paymentTemplates = new ArrayCollection();
            }
        }

        [Bindable]
        public var paymentTemplates:ArrayCollection;

        [Bindable]
        [BackingProperty]
        public var selectedPaymentTemplate:PaymentTemplate;

        [Bindable]
        [BackingProperty]
        public var selectedYear:String;

        [Bindable]
        public var quarterList:Array = ["", "Q1", "Q2", "Q3", "Q4"];

        [Bindable]
        [BackingProperty]
        public var selectedQuarter:String;

        [Bindable]
        public var yearValidator:NumberValidator;

        public function OperatorLedgerTORViewModel() {
            yearValidator = SAPValidators.createNumberValidator(this, "selectedYear", false, 1900, 2100, false, 1);
            validators.push(yearValidator);
            validators.push(SAPValidators.createRequiredFieldValidator(this, "selectedAgency"));
            validators.push(SAPValidators.createRequiredFieldValidator(this, "selectedPaymentTemplate"));
            validators.push(SAPValidators.createRequiredFieldValidator(this, "selectedYear"));
            validators.push(SAPValidators.createRequiredFieldValidator(this, "selectedQuarter"));
        }


        override protected function preActivation():void {
            if (SAP.instance.lookupService.agencyList == null) {
                SAP.instance.lookupService.addEventListener(SAPEvent.DATA_LOAD_COMPLETED, function(e:Event):void {
                    setAgencyListPreActivation();
                });
            } else {
                setAgencyListPreActivation();
            }
        }

        private function setAgencyListPreActivation():void {
            agencyList = SAP.instance.lookupService.agencyList;
            preActivationComplete();
        }

        override protected function onActivating():void {
            selectedQuarter = quarterList[0];
        }

        override protected function initializeBackingProperties():void {
            if (agencyList != null && agencyList.length > 0) {
                selectedAgency = Agency(agencyList.getItemAt(0));
            }
        }

        override protected function executeSave():void {
            var selectedQuarterDate:Date = new Date(selectedYear, quarterList.indexOf(selectedQuarter) * 3, 0, 0, 0, 0, 0);
            SAP.instance.administrationService.createTORLedgerOperationJob(
                    selectedPaymentTemplate.paymentTemplateCd,
                    selectedQuarterDate,
                    createSaveResponder());
        }
    }
}
