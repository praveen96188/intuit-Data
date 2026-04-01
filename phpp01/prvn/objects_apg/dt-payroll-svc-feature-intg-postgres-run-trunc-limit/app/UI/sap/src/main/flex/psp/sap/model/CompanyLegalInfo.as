package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyLegalInfo")]	
	public class CompanyLegalInfo
	{
		public function CompanyLegalInfo()
		{
		}
		
		public var legalName:String;
		public var doingBusinessAs:String;
		public var address:Address;
        public var ein:String;
        public var psid:String;
		public var einEffectiveDate:Date;
        public var isOldEinError:Boolean;
        public var industryType:String;

	}
}