package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDisplayStatus")]
	public class DisplayStatus
	{		
		public var displayStatus:String;
    	public var displaySubStatus:String;
    	public var displayDetails:String;
	}
}