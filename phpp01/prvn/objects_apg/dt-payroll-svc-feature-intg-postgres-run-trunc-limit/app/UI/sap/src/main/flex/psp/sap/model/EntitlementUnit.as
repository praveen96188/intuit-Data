package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEntitlementUnit")]
    public class EntitlementUnit {
        public var id : String;
        public var serviceKey:String;
        public var extensionKey:String;
        public var status:String;
        public var lastValidationDate:Date;
        public var entitlement:EntitlementInfo;       
    }
}