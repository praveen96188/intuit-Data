package psp.sap.model.companyevents
{
	[Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyEventEmailParam")]
	public class CompanyEventEmailParam
	{
		public var paramType:String;
		public var paramValue:String;
	}
}