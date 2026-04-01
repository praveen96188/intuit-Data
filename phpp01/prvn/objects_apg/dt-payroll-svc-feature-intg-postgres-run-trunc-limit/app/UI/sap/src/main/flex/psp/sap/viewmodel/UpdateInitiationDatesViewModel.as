/**
 * User: dweinberg
 * Date: 1/9/12
 * Time: 5:12 PM
 */
package psp.sap.viewmodel {
    import mx.events.PropertyChangeEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.OffloadDate;
    import psp.sap.model.PaymentSearch;
    import psp.sap.model.SearchResults;
    import psp.sap.validators.SAPDateValidator;
    import psp.sap.validators.SAPValidators;

    public class UpdateInitiationDatesViewModel extends AbstractPartViewModel {
        [Bindable] public var searchCriteria:PaymentSearch;
        [Bindable] public var searchResults:SearchResults;

        private var offloadDate:Date;

        [Bindable] public var currentInitiationDate:String;
        [Bindable] [BackingProperty] public var newInitiationDate:String="";

        [Bindable] public var paymentsChanged:int;

        [Bindable] public var newInitiationDateValidator:SAPDateValidator;

        public function UpdateInitiationDatesViewModel() {
            super();
            newInitiationDateValidator = SAPValidators.createDateValidator(this, "newInitiationDate", true, 0, 365);
            newInitiationDateValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
            validators.push(newInitiationDateValidator);
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getNextInitiationDate(searchCriteria.paymentMethod, createLoadModelDataResponder(onOffloadDataLoaded));
        }

        private function onOffloadDataLoaded(e:ResultEvent):void{
            offloadDate = OffloadDate(e.result).offloadDate;
            newInitiationDateValidator.daysBeforeAllowedErrorMessage = "New Initiation Date cannot be earlier than " + SAPDateFormatters.dateFormatShort.format(offloadDate);
            newInitiationDateValidator.daysAfterAllowedErrorMessage = "New Initiation Date cannot be later than " + SAPDateFormatters.dateFormatShort.format(offloadDate);
            newInitiationDateValidator.fromValidationDate = offloadDate;

        }

        override protected function initializeBackingProperties():void {
            currentInitiationDate = SAPDateFormatters.dateFormatMedium.format(this.searchResults.returnsList.getItemAt(0).initiationDate);
            newInitiationDate="";
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        override protected function executeSave():void {
            SAP.instance.taxService.updateInitiationDates(searchCriteria, new Date(newInitiationDate), createSaveResponder(onInitiationDatesChanged));
        }

        private function onInitiationDatesChanged(e:ResultEvent):void {
            paymentsChanged = e.result as int;
            // setting the saveMsg in PaymentsViewModel
            this.host.host.host.host.host.host.saveMsg = paymentsChanged + " of " + searchResults.totalRecords + " payments changed to " + newInitiationDate + ".";
        }

    }
}
