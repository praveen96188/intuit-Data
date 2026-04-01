package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRAFEnrollmentDetail")]
    public class RAFEnrollmentDetail extends EnrollmentDetail {
        public var rejectionReason:String;
        public var creationDate:Date;
        public var modifiedDate:Date;
        public var companyHasPayrolls:Boolean;
    }
}