package psp.sap.model {
    import psp.sap.application.SAP;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEnrollmentStatusChange")]
    public class EnrollmentStatusChange {
        public var changeDate:Date;
        public var modifiedBy:String;
        public var status:String;
    }
}