package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyLaw")]
    public class CompanyLaw {
        public var sourceId:String;
        public var description:String;
        public var lawType:String;
        public var status:String;
        public var taxFormLine:String;
        public var lawId:Number;
        public var latestId:String;
        public var agencyId:String;
        public var pendingPush:String;
        public var deleteStatus:String;
        public var token:String;
        public var coaExpense:String;
        public var coaLiability:String;
        public var iisemp:Boolean;
    }
}