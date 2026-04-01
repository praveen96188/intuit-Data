package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEntitlementInfo")]
    public class EntitlementInfo {
        public var id:String;
        public var licenseNumber:String;
        public var eoc:String;
        public var orderNumber:String;
        public var customerId:String;
        public var contactEmail:String;        
        public var nextChargeDate:Date;
        public var orderSourceCode:String;
        public var subscriptionNumber:String;
        public var status:String;
        public var billingZipCode:String;
        public var subscriptionStartDate:Date;
        public var entitlementCodeInfo:EntitlementCodeInfo;
        public var subscriptionEndDate:Date;

        // Duplicate field, added to capture asset item number if EntitlementCodeInfo is not present
        public var assetItemNumber:String;
    }
}