package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollACHDetailSet")]
	public class PayrollACHDetailSet
	{
		[ArrayElementType("psp.sap.model.PayrollTransaction")]
		public var feeTransactions:ArrayCollection;
				
		[ArrayElementType("psp.sap.model.AgencyTransaction")]
		public var taxTransactions:ArrayCollection;
		
		[ArrayElementType("psp.sap.model.AgencyTransaction")]		
		public var taxCreditTransactions:ArrayCollection;
		
		[ArrayElementType("psp.sap.model.PayrollEmployeeTransaction")]		
		public var ddTransactions:ArrayCollection;
		
		public var feeTransactionsTotal:Number;
    	public var taxTransactionsTotal:Number;
    	public var taxCreditTransactionsTotal:Number;
    	public var ddTransactionsTotal:Number;
	}
}