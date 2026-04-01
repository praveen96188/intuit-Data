package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSuspectPaycheck")]
	public class SuspectPaycheck
	{
		public var employeeName:String;
    	public var trigger:String;
    	public var amount:Number;
    	public var paycheckId:String;
	}
}