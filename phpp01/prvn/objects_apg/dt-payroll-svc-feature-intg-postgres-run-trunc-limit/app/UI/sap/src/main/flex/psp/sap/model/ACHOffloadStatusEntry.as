package psp.sap.model
{
	/**
	 * THIS IS NOT A DTO -- this is a presentation only class
	 * for the OffloadStatus screen
	 */
	[Bindable]
	public class ACHOffloadStatusEntry
	{
		public static function createFromStepEntry(jobStepLogEntry:ACHOffloadJobStepLogEntry):ACHOffloadStatusEntry
		{	
			var statusEntry:ACHOffloadStatusEntry = new ACHOffloadStatusEntry();
			statusEntry.jobName = jobStepLogEntry.stepName
			statusEntry.startDateTime = jobStepLogEntry.stepBeginDateTime;
			statusEntry.isStep = true;			
			return statusEntry;
			
		}
		
		public static function createFromJobEntry(jobLogEntry:ACHOffloadJobLogEntry):ACHOffloadStatusEntry {
			var statusEntry:ACHOffloadStatusEntry = new ACHOffloadStatusEntry();
			statusEntry.jobName = jobLogEntry.jobName;
			statusEntry.startDateTime = jobLogEntry.startDateTime;
			statusEntry.finishDateTime = jobLogEntry.finishDateTime;
			statusEntry.estimatedRunTimeInMillis = jobLogEntry.estimatedRunTimeInMillis;
			statusEntry.actualRunTimeInMillis = jobLogEntry.actualRunTimeInMillis;			
			return statusEntry;
		}

		public var jobName:String;
		public var startDateTime:Date;
		public var finishDateTime:Date;
		public var estimatedRunTimeInMillis:Number;
		public var actualRunTimeInMillis:Number;
		public var isStep:Boolean;
		
		public function get status():String {
			var statusName:String = "";
			
			if (!startDateTime)
				statusName = "Pending";
			else if (startDateTime && !finishDateTime)
				statusName = "Executing";
			else if (startDateTime && finishDateTime)
				statusName = "Completed";	

			return statusName;		
		}
		
		public function get expectedFinishDateTime():Date {
			if (!startDateTime)
				return null;

			if (!estimatedRunTimeInMillis)
				return null;
				
			var estimated:Date = new Date(startDateTime.time + estimatedRunTimeInMillis);
			return estimated;
		}
		
		
	}
}