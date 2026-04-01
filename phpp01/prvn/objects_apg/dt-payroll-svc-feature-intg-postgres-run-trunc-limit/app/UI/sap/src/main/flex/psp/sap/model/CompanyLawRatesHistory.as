package psp.sap.model {
    import mx.collections.ArrayCollection;

    import psp.sap.application.SAP;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyLawRatesHistory")]
    public class CompanyLawRatesHistory {
        [ArrayElementType("psp.sap.model.CompanyLawRateDetail")]
        public var companyLawRateDetails:ArrayCollection;
        public var companyLawNames:ArrayCollection;
    }
}