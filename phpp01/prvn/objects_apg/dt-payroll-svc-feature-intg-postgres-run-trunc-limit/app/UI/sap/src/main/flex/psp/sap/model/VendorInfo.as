package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPVendorInfo")]
    public class VendorInfo {
        public var name:String;
        public var email:String;
        public var phone:String;
        public var sourceId:String;
    }
}