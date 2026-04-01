package psp.sap.model {

    import mx.collections.ArrayCollection;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentForVerification")]
    public class PaymentForVerification {
        public var taxPaymentStatus:String;
        public var initiationDate:Date;
        public var dueDate:Date;
        public var settlementDate:Date;
        public var paymentTemplate:PaymentTemplate;
        public var amount:Number;
        public var paymentMethod:String;
        public var paymentId:String;
        public var taxpayerAgencyId:String;
        public var periodBeginDate:Date;
        public var periodEndDate:Date;
        public var debitAccountNumber:String;
        public var creditAccountRouting:String;
        public var creditAccountNumber:String;  
        [ArrayElementType("psp.sap.model.KeyValuePair")]
        public var details:ArrayCollection;

        public function get getTransactionType():String {
            switch(paymentMethod){
                case "ACHCredit":
                    return "ACH";
                case "CheckPayment":
                    return "Payment";
                case "EFTPS":
                case "EFTPSDirectDebit":
                    return "EFTPS Payment";
                case "EDI":
                    return "EDI Payment"
            }
            return "";
        }

    }
}