package psp.sap.model.companyevents
{
	
	[Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyEventDetail")]
	public class CompanyEventDetail
	{
		public var eventDetailTypeCd:String;
		public var name:String;
		public var valueClassName:String;
		public var value:String;
	}
}