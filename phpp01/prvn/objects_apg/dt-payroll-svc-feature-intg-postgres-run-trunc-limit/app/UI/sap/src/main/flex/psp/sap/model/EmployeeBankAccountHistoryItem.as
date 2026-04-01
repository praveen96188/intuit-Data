package psp.sap.model
{
	import psp.sap.application.SAP;
	

	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeBankAccountHistoryItem")]
	public class EmployeeBankAccountHistoryItem
	{
		public var changeDate:Date;
		public var changedBy:String;
		
		public var accountNumber:String;
	    public var routingNumber:String;
	    public var accountTypeCd:String;

	    public var oldAccountNumber:String;
	    public var oldRoutingNumber:String;
	    public var oldAccountTypeCd:String;
	}
}