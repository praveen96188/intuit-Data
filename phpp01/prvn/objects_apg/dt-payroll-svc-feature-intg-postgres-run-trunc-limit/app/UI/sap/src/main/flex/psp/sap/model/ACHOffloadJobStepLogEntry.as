package psp.sap.model
{
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPACHOffloadJobStepLogEntry")]
	[Bindable]	
	public class ACHOffloadJobStepLogEntry
	{
		public var jobName:String;
		public var stepName:String;
		public var stepBeginDateTime:Date;
		public var status:String;
	}
}