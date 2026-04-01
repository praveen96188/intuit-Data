package psp.sap.model {
    import mx.collections.ArrayCollection;
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPItemSet")]
    public class PItemSet {
        [ArrayElementType("psp.sap.model.PItem")]
        public var companyPayrollItems:ArrayCollection;
        [ArrayElementType("psp.sap.model.PItem")]
        public var companyLaws:ArrayCollection;
    }
}