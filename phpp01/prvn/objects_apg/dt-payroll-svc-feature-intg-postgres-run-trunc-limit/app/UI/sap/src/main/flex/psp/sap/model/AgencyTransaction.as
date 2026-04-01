package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAgencyTransaction")]
	public class AgencyTransaction extends PayrollTransaction
	{
		public var taxDescription:String;
    	public var taxAbbreviation:String;
    	public var agencyName:String;
    	public var agencyAbbreviation:String;
	}
}