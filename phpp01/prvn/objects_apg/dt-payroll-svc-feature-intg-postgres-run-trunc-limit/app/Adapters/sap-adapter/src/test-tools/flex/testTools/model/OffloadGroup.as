package testTools.model
{
	import intuit.sbd.flex.framework.model.ValueObject;

	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools.TTOffloadGroup")]
	public class OffloadGroup extends ValueObject
	{
	    public var groupCode:String;
	    public var groupName:String;
	    public var groupDescription:String;
	    public var cutoffTime:String;
	    
	    override public function toString():String {
	    	return groupCode;
	    }
	}
}