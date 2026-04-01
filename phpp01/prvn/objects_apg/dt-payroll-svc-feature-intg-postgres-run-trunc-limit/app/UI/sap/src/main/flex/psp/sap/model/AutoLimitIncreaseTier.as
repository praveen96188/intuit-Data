package psp.sap.model
{
	import intuit.sbd.flex.framework.model.EntityObject;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAutoLimitIncreaseTier")]
	public class AutoLimitIncreaseTier
	{
		public var level:String;
		public var sourceSystemCd:String;
	    public var payrollsRun:String;
	    public var daysSinceFirstPayroll:String;
	    public var increaseMultiplier:String;
	    public var companyCap:String;
	    public var employeeCap:String;
	       
	    [Transient]
	    static public var propNames:Array = [	"level", 
								    			"sourceSystemCd", 
								    			"payrollsRun", 
								    			"daysSinceFirstPayroll", 
								    			"increaseMultiplier", 
								    			"companyCap", 
								    			"employeeCap"];
	}
}