package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentDetails")]
    public class PaymentDetails {
        public var ftId:String;
        public var createdDate:Date;
        public var checkDate:Date;
        public var amount:Number;
        public var law:String;
        public var lawType:String;
        public var txnType:String;

        [Transient] public var selected:Boolean;
    }
}
