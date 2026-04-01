package psp.sap.model
{
    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeTaxabilityInfo")]
	public class EmployeeTaxabilityInfo
	{
		public var jurisdiction:String;
		public var taxType:String;
	    public var filingStatus:String;
	    public var allowances:int;
	    public var extraWithHolding:Number;
		public var claimDependents:Number;
		public var otherIncome:Number;
		public var deductions:Number;
		public var multipleJobs:Boolean;
		public var fedW4EmployeePref:String;
	    public var subjectTo:Boolean;
	}
}