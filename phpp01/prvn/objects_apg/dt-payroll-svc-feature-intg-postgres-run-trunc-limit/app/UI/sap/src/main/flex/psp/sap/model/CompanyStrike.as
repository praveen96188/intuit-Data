package psp.sap.model
{
	import psp.sap.application.SAP;
	import psp.sap.model.companyevents.CompanyEvent;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyStrike")]
	public class CompanyStrike extends CompanyEvent
	{
		public var strikeReason: String;
        public var strikeDate: Date;
        public var createdByUserId:String;
        public var manualDescription: String;
        public var spcfUniqueId: String;
        public var newStrike: Boolean = true;
        public var cancelled: Boolean = false;
        public var cancelledByUserId:String;
        public var financialTransactionId:String;
    }
}