package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.domain.CompanyNoteSAP")]
	public class Note
	{
		public var insertUserId: String;
		public var notes: String;
	}
}