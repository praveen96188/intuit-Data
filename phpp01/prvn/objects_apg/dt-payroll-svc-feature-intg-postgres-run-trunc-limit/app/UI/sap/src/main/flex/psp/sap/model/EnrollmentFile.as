package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEnrollmentFile")]
    public class EnrollmentFile {
        public var createdDate:Date;
        public var fileId:String;
        public var type:String;
    }
}