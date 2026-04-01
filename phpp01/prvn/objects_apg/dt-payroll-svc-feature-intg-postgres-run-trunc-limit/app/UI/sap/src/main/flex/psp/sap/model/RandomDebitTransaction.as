package psp.sap.model
{
	[Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRandomDebit")]
	public class RandomDebitTransaction
	{
		public var offloadedDate:Date;
    	public var settlementDate:Date;
    	public var amount1:String;
    	public var amount2:String;
	}
}