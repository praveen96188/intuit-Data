package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPACHEnrollmentHistory")]
    public class ACHEnrollmentHistory {
        public var canRe_enroll:Boolean;
        [ArrayElementType("psp.sap.model.ACHEnrollmentHistoryItem")]
        public var enrollments:ArrayCollection;
    }
}