package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyLawRateDetail")]
    public class CompanyLawRateDetail {
        public var lawName:String;
        public var lawId:String;
        public var rate:Number;
        public var effectiveQuarter:Quarter;
        public var changeDate:Date;
        public var changedBy:String;
        public var invalidDate:Date;
        public var isCurrent:Boolean;
        public var agencyId:String;
        public var exempt:Boolean;
        public var inactive:Boolean;
        public var reimbursable:Boolean;
        public var sourceLawID:String;
        public var sourceLawDescription:String;
        public var createdDate:Date;
        public var createdBy:String;
    }
}