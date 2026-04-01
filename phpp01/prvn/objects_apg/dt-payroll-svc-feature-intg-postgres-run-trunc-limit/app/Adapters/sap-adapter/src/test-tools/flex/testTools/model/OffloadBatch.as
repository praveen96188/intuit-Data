package testTools.model
{
	import intuit.sbd.flex.framework.model.ValueObject;

	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools.TTOffloadBatch")]
	public class OffloadBatch extends ValueObject
	{
	    public var gseq:String;
	    public var offloadGrpCd:String;
	    public var statusCd:String;
	    public var offloadDate:Date;
	    public var statusChangeDate:Date;
	    public var insertDate:Date;
	    
	    override public function toString():String {
	    	return offloadGrpCd;
	    }
	}
}