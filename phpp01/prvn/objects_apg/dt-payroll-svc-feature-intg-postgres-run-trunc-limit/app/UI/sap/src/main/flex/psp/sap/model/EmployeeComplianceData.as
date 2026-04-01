package psp.sap.model
{
    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeComplianceData")]
	public class EmployeeComplianceData
	{
		public var state:String = "";
		public var wagePlanDomain:String = "";
	    public var name:String = "";
	    public var wagePlanValue:String = "";
	    public var description:String = "";
	    public var rulesVersion:String = "";
        public var id:String = "";
	}
}