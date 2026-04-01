package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.PaymentsPageEnum;
    import psp.sap.model.Payment;
    import psp.sap.model.PaymentMethod;
    import psp.sap.validators.SAPValidators;

    public class PaymentMethodEditViewModel extends AbstractPartViewModel {

        [Bindable]
        public var payment:Payment;

        [Bindable]
        [ArrayElementType("psp.sap.model.PaymentMethod")]
        public var paymentMethods:ArrayCollection;

        [Bindable]
        [ArrayElementType("String")]
        public var validPaymentMethods:ArrayCollection = new ArrayCollection();

        [Bindable]
        [BackingProperty]
        public var newPaymentMethod:String = "";

        public function PaymentMethodEditViewModel() {
            super();
            this.label = PaymentsPageEnum.EDIT_PAYMENT_METHOD_POPUP;
            this.reloadOnSave = true;

            validators.push(SAPValidators.createRequiredFieldValidator(this, "newPaymentMethod"));
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getValidPaymentMethods(payment.paymentId, payment.companyId, createLoadModelDataResponder(onPaymentMethodResults));
        }

        override protected function initializeBackingProperties():void{
            validPaymentMethods = new ArrayCollection();
            if(paymentMethods != null){
                for each(var paymentMethodCounter:PaymentMethod in paymentMethods){
                    if(paymentMethodCounter.isEnabled){
                        validPaymentMethods.addItem(paymentMethodCounter.paymentMethodName)
                    }
                }
            }
        }

        public function onPaymentMethodResults(e:ResultEvent):void {
            paymentMethods = new ArrayCollection();
            paymentMethods = e.result as ArrayCollection;
        }

        override public function get hasChanged():Boolean {
            return newPaymentMethod != payment.paymentMethod;
        }

        override protected function initializeDefaults():void {
            this.newPaymentMethod = payment.paymentMethod;
        }

        override protected function executeSave():void {
            SAP.instance.taxService.updatePaymentMethod(payment.paymentId, newPaymentMethod, payment.companyId, createSaveResponder());
        }

    }
}
