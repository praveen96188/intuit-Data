package psp.taxcredits.dto {
    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.taxcredits.dto.Address")]
    public class Address {
        public var address1:String;
        public var address2:String;
        public var city:String;
        public var state:String;
        public var zip:String;
        public var county:String;
    }
}