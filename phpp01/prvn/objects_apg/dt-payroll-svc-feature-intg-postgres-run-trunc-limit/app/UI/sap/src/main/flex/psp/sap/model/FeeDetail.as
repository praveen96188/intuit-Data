package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPFeeDetail")]
	public class FeeDetail
	{
		public var isPayrollFee:Boolean;
	    public var feeName:String;
	    public var units:Number;
	    public var unitPrice:Number;
    	public var totalPrice:Number;
    	public var currentUnitPrice:Number;
	}
}