package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBaseEnrollmentHistoryItem")]
    public class BaseEnrollmentHistoryItem {
        public var enrollmentId:String;
        public var ein:String;
        public var legalName:String;
        public var legalZip:String;
        [ArrayElementType("psp.sap.model.EnrollmentStatusChange")]
        public var statusChanges:ArrayCollection;
    }
}