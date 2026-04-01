package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSettlementType")]
	public class SettlementType
	{
		public var name: String;
		public var description: String;
		public var settlementTypeCd: String;
	}
}