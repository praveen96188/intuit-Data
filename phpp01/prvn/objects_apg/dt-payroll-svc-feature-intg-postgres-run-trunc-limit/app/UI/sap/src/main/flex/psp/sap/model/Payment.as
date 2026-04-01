package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayment")]
    public class Payment {

        public var holds:ArrayCollection;
        public var manualHoldReason:String;
        public var manualHoldCreator:String;
        public var status:String;
        public var companyName:String;
        public var companyKey:CompanyKey;
        public var agencyName:String;
        public var agencyId:String;
        public var paymentType:String;
        public var settlementDate:Date;
        public var initiationDate:Date;
        public var dueDate:Date;
        public var amount:Number;
        public var paymentMethod:String;
        public var paymentId:String;
        public var psId:String;
        public var ein:String;
        public var paymentFrequency:String;
        public var isPending:Boolean;
        public var quarter:Quarter;
        public var highestPriorityPaymentMethod:String;
        public var isNotPriorityPaymentMethod:Boolean;
        public var notPriorityPaymentMethodReasons:ArrayCollection;
        public var periodBegin:Date;
        public var periodEnd:Date;
        public var crossesQuarters:Boolean;

        public function get companyId():String
        {
            return companyKey.companyId;
        }

        public function strNotPriorityPaymentMethodReasons():String{
            var reasons:String = "Reason(s) not " + highestPriorityPaymentMethod + ":\n";
            return(notPriorityPaymentMethodReasons != null ? reasons + notPriorityPaymentMethodReasons.toArray().join('\n') : "");
        }
    }
}
