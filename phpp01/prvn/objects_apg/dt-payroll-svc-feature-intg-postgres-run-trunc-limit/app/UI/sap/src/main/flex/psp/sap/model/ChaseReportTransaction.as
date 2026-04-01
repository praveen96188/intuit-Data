package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPChaseReportTransaction")]
	public class ChaseReportTransaction
	{
		public var settlementDate:Date;
	    public var debitAmount:Number;
	    public var creditAmount:Number;
	    public var debitAccountName:String;
	    public var creditAccountName:String;
	    public var debitAccountRoutingNumber:String;
	    public var creditAccountRoutingNumber:String;
	    public var debitAccountNumber:String;
	    public var creditAccountNumber:String;

	}
}