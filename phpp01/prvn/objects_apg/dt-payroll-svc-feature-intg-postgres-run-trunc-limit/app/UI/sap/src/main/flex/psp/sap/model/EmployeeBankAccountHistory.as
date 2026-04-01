package psp.sap.model
{
	import mx.collections.ArrayCollection;
	

	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeBankAccountHistory")]
	public class EmployeeBankAccountHistory
	{
		public var accountId:String;
		
		[ArrayElementType("psp.sap.model.EmployeeBankAccountHistoryItem")]
		public var employeeBankAccountHistoryItems:ArrayCollection = new ArrayCollection();
	}
}