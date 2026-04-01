package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyEventType")]
    public class CompanyEventType {
        public var eventTypeCode:String;
        public var eventTypeName:String;
    }
}