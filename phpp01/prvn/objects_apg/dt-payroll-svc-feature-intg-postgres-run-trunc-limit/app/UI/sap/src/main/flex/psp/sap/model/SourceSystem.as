package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSourceSystem")]
	public class SourceSystem
	{
		public var name: String;
		public var description: String;
		public var sourceSystemCd: String;
	}
}