package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPACHOffloadStatus")]
	[Bindable]
	public class ACHOffloadStatus
	{
		public var estimatedTransactionCount:Number;
		public var actualTransactionCount:Number;
		
		[ArrayElementType("psp.sap.model.ACHOffloadJobLogEntry")]
		public var jobLogEntries:ArrayCollection;
		
		[Transient]
		[Bindable("propertyChange")]
		public function get estimatedTimeInMillis():Number {
			var total:Number = 0;
			for each (var jobEntry:ACHOffloadJobLogEntry in jobLogEntries) {
				total += jobEntry.estimatedRunTimeInMillis;
			}
			return total;
		}		
	}
}