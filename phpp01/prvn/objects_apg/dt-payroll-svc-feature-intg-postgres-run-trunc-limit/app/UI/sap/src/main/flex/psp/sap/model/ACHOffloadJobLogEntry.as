package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPACHOffloadJobLogEntry")]
	public class ACHOffloadJobLogEntry
	{
		public var jobName:String;
		public var startDateTime:Date;
		public var finishDateTime:Date;
		public var estimatedRunTimeInMillis:Number;
		
		public var elapsedTimeStr:String;
		public var actualRunTimeInMillis:Number;

		[ArrayElementType("psp.sap.model.ACHOffloadJobStepLogEntry")]
		public var stepLogs:ArrayCollection;	
	}
}