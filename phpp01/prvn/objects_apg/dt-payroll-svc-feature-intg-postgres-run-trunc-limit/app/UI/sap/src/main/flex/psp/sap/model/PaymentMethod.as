package psp.sap.model {
    import mx.collections.ArrayCollection;

    import psp.sap.application.SAP;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentMethod")]
    public class PaymentMethod {
        public var modifiedDate:Date;
        public var paymentMethodName:String;
        public var isAgentEnabled:Boolean;
        public var isEnabled:Boolean;
        public var changedBy:String;
        public var requirements:ArrayCollection;
        public var additionalRequirements:ArrayCollection;
        public var paymentMethodOrder:int;
        public var hasManualRequirement:Boolean;
    }
}
