package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeTaxLedgerItem")]
	public class EmployeeTaxLedgerItem
	{
		public var employeeName:String;
		public var socialSecurityNumber:String;
		public var totalWages:Number;
		public var taxableWages:Number;
		public var taxAmount:Number;				
		public var taxTips:Number;
        public var showTaxTips:Boolean;
	}
}