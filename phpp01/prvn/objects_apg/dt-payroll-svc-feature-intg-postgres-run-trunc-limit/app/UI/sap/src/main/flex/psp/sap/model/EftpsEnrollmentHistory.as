package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEftpsEnrollmentHistory")]
    public class EftpsEnrollmentHistory {
        public var canRe_enroll:Boolean;
        [ArrayElementType("psp.sap.model.EftpsEnrollmentItem")]
        public var enrollments:ArrayCollection;
    }
}