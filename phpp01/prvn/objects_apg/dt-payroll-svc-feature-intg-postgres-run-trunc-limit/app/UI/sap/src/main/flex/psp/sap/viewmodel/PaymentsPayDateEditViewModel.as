package psp.sap.viewmodel {
    import mx.events.PropertyChangeEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.PaymentsPageEnum;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.OffloadDate;
    import psp.sap.model.Payment;
    import psp.sap.validators.SAPDateValidator;
    import psp.sap.validators.SAPValidators;

    public class PaymentsPayDateEditViewModel extends AbstractPartViewModel {

        [Bindable] [BackingProperty] public var payment:Payment;
        [Bindable] [BackingProperty] public var newSettlementDate:String="";

        private var offloadDate:Date;

        [Bindable] public var newSettlementDateValidator:SAPDateValidator;

        public function PaymentsPayDateEditViewModel() {
            super();
            this.label = PaymentsPageEnum.EDIT_PAYDATE_POPUP;
            newSettlementDateValidator = SAPValidators.createDateValidator(this, "newSettlementDate", true, 0, 365);
            newSettlementDateValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
            newSettlementDateValidator.trigger = this;
            validators.push(newSettlementDateValidator);
            this.reloadOnSave = true;
        }

        override public function get hasChanged():Boolean {
            return true;
        }
        override protected function initializeDefaults():void {
            this.newSettlementDate= "";
        }

        public function get newSettlementDateValue():Date {
            if(newSettlementDate == ""){
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(newSettlementDate);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }


        override protected function loadModelData():void {
            SAP.instance.taxService.getOffloadDate(payment.paymentMethod,payment.paymentType, createLoadModelDataResponder(afterGettingOffloadDate));
        }

        override protected function executeSave():void{
            SAP.instance.taxService.updatePayDate(payment.paymentId, newSettlementDateValue, payment.companyId, createSaveResponder());
        }


        private function afterGettingOffloadDate(e:ResultEvent):void{
            var searchReturn:OffloadDate = e.result as OffloadDate;
            offloadDate = searchReturn.offloadDate;
            newSettlementDateValidator.daysBeforeAllowedErrorMessage = "Settlement Date cannot be earlier than "+SAPDateFormatters.dateFormatShort.format(searchReturn.offloadDate);
            newSettlementDateValidator.fromValidationDate = searchReturn.offloadDate;

        }
    }
}
