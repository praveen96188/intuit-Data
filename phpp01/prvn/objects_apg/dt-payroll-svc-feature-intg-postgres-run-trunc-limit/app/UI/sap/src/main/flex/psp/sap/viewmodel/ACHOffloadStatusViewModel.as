package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.model.ACHOffloadJobLogEntry;
	import psp.sap.model.ACHOffloadJobStepLogEntry;
	import psp.sap.model.ACHOffloadStatus;
	import psp.sap.model.ACHOffloadStatusEntry;
	
	public class ACHOffloadStatusViewModel extends AbstractPartViewModel
	{
		public function ACHOffloadStatusViewModel()
		{
		}

		[Bindable]		
		public var offloadStatus:ACHOffloadStatus;
		
		[Bindable]
		public var offloadStatusEntries:ArrayCollection;
		
		[Bindable]
		public var estimatedTransactionCount:Number;
				
		override protected function loadModelData():void {
			SAP.instance.administrationService.getOffloadStatus(SAP.instance.PSPDate, createLoadModelDataResponder(onStatusLoaded));
		}
		
		protected function onStatusLoaded(e:ResultEvent):void {
			var status:ACHOffloadStatus = e.result as ACHOffloadStatus;
			if (status == null)
				return;
				
			estimatedTransactionCount = status.estimatedTransactionCount;

			// convert all log entries to 'status entries'
			// -- convert the Job Log entry to a status entry and add to list
			// --   for each step in the Job Entry, insert a 'sub' step status entry (displays indentend on UI)
			// --   for each step, set its 'finish time' to the next steps start time 
			var entries:ArrayCollection = new ArrayCollection(); 						
			for (var i:int = 0; i < status.jobLogEntries.length; i++) {
				var jobLogEntry:ACHOffloadJobLogEntry = status.jobLogEntries.getItemAt(i) as ACHOffloadJobLogEntry;
				entries.addItem( ACHOffloadStatusEntry.createFromJobEntry(jobLogEntry) );
				
				for (var j:int = 0; j < jobLogEntry.stepLogs.length; j++) {
					var stepLogEntry:ACHOffloadJobStepLogEntry = jobLogEntry.stepLogs.getItemAt(j) as ACHOffloadJobStepLogEntry;
					var statusEntry:ACHOffloadStatusEntry = ACHOffloadStatusEntry.createFromStepEntry(stepLogEntry); 
					
					if (j == jobLogEntry.stepLogs.length - 1)
						statusEntry.finishDateTime = jobLogEntry.finishDateTime;
					else
						statusEntry.finishDateTime = (jobLogEntry.stepLogs.getItemAt(j + 1) as ACHOffloadJobStepLogEntry).stepBeginDateTime;
					
					entries.addItem(statusEntry);
				}
			}
			
			offloadStatusEntries = entries;			
		}
	}
}
