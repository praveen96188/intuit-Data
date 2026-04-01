package psp.taxcredits.dto {
    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.taxcredits.dto.EmployerInfo")]
    public class EmployerInfo {

        public var contactName:String;
        public var telephoneNumber:String;
        public var telephoneExtension:String;
        public var ein:String;
        public var legalAddress:Address;        
        public var companyLegalName:String;
        public var contactEmail:String;
        public var offerCode:String;
        public var companyType:String;
        public var authSignerEmail:String;        
        public var fiscalYearStartDateString:String;
    }
}