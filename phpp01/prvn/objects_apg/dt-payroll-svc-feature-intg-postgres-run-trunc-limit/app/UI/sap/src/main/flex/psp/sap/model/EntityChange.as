package psp.sap.model {
    import psp.sap.application.SAP;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEntityChangeDTO")]
    public class EntityChange {
        public var companyKey:CompanyKey;
        public var oldEIN:String;
        public var effectiveDate:Date;
        public var changeDate:Date;
        public var newEIN:String;
        public var agentId:String;
        public var isSuccessor:Boolean;
        public var hasNewDataFile:Boolean;
        public var isError:Boolean;
    }
}