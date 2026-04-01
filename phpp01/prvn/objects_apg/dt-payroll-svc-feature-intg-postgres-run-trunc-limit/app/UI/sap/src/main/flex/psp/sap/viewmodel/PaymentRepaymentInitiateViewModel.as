package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;

    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.PaymentsPageEnum;
    import psp.sap.model.Payment;
    import psp.sap.model.PaymentMethod;
    import psp.sap.model.TaxRepaymentOptions;

    public class PaymentRepaymentInitiateViewModel extends AbstractPartViewModel {

        [Bindable] [BackingProperty] public var payment:Payment;

        [Bindable] [ArrayElementType("psp.sap.model.PaymentMethod")]
        public var paymentMethodList:ArrayCollection;

        [Bindable] [BackingProperty] public var newPaymentMethod:PaymentMethod;
        [Bindable] [BackingProperty] public var updatePaymentMethod:Boolean;
        [Bindable] [BackingProperty] public var initiateAll:Boolean;
        [Bindable] [BackingProperty] public var recreate:Boolean;


        public function PaymentRepaymentInitiateViewModel() {
            super();
            this.label = PaymentsPageEnum.REPAYMENT_POPUP;
            this.reloadOnSave = true;
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getValidPaymentMethods(payment.paymentId, payment.companyId, createLoadModelDataResponder(onPaymentMethodResults));
        }

        public function onPaymentMethodResults(e:ResultEvent):void {
            paymentMethodList = e.result as ArrayCollection;
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        override protected function initializeDefaults():void {
            this.newPaymentMethod = new PaymentMethod();
            this.newPaymentMethod = getPaymethodFromList(payment.paymentMethod);
            updatePaymentMethod = false;
            initiateAll = false;
            recreate = false;
        }

        protected function getPaymethodFromList(paymentMethodName:String):PaymentMethod{
            for each(var paymentMethod:PaymentMethod in paymentMethodList){
                if(paymentMethod.paymentMethodName == paymentMethodName){
                    return paymentMethod
                }
            }
            return new PaymentMethod();
        }

        override protected function executeSave():void {
            SAP.instance.taxService.initiateRepayment(payment.paymentId,
                    new TaxRepaymentOptions(updatePaymentMethod ? newPaymentMethod.paymentMethodName : null,
                            initiateAll,
                            recreate), payment.companyId,
                    createSaveResponder());
        }
    }
}
