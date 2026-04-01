package psp.sap.model {
    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBankAccountSearchResult")]
    public class BankAccountSearchResult {
        public var companyLegalName:String;
        public var companyKey:CompanyKey;
        public var accountOwnerName:String;
        public var accountType:String;
        public var accountStatus:String;
    }
}