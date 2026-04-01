package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRAFEnrollmentHistory")]
    public class RAFEnrollmentHistory {
        public var canRe_enroll:Boolean;
        [ArrayElementType("psp.sap.model.RAFEnrollmentHistoryItem")]
        public var enrollments:ArrayCollection;
    }
}