package psp.taxcredits.dto {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.taxcredits.dto.WOTCCategory")]
    public class WOTCCategory {
        public var category:String;
        public var taxRate0:Number;
        public var taxRate1:Number;
        public var taxRate2:Number;
        public var wageBase:Number;
        public var maxCredit:Number;
    }
}