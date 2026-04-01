package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEntitlementCodeInfo")]
    public class EntitlementCodeInfo {
        public var edition:String;
        public var numberOfEmployees:String;
        public var quickBooksSubtype:String;
        public var subtypeDescription:String;
        public var assetItemCode:String;
        public var assetItemNumber:String;
    }
}