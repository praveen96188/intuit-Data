package psp.sap.model
{
	//TODO: Reset defaults to self
	
	public class TaxPaymentStatus
	{
		public static const PENDING_EXECUTION:TaxPaymentStatus = new TaxPaymentStatus("PendingExecution", "Pending Execution");
		public static const PENDING_REEXECUTION:TaxPaymentStatus = new TaxPaymentStatus("PendingReExecution", "Pending Re-Execution");
		
		public static const SUCCESSFUL_EXECUTED:TaxPaymentStatus = new TaxPaymentStatus("SuccessfulExecuted", "Successful Executed");
		public static const SUCCESSFUL_REEXECUTED:TaxPaymentStatus = new TaxPaymentStatus("SuccessfulReExecuted", "Successful Re-Executed");
		
		public static const AGENCY_REJECTION:TaxPaymentStatus = new TaxPaymentStatus("AgencyRejection", "Agency Rejection");
		public static const AGENCY_HOLD:TaxPaymentStatus = new TaxPaymentStatus("AgencyHold", "Agency Hold");
		public static const COMPANY_HOLD:TaxPaymentStatus = new TaxPaymentStatus("CompanyHold", "Company Hold");
		
		public static const EXECUTED_SENT_TO_AGENCY:TaxPaymentStatus = new TaxPaymentStatus("ExecutedSentToAgency", "Executed - Sent to Agency");
		public static const EXECUTED_SENT_TO_EFE:TaxPaymentStatus = new TaxPaymentStatus("ExecutedSentToEFE", "Executed - Sent to EFE");
		public static const REEXECUTED_SENT_TO_AGENCY:TaxPaymentStatus = new TaxPaymentStatus("ReExecutedSentToAgency", "Re-Executed - Sent to Agency");
		public static const REEXECUTED_SENT_TO_EFE:TaxPaymentStatus = new TaxPaymentStatus("ReExecutedSentToEFE", "Re-Executed - Sent to EFE");
		
		
		public static const ALL_STATUS:TaxPaymentStatus = new TaxPaymentStatus(null, "");

		public static const values:Array = [ALL_STATUS, PENDING_EXECUTION, PENDING_REEXECUTION, SUCCESSFUL_EXECUTED, SUCCESSFUL_REEXECUTED];

		
		public static const pendingQueueValues:Array = [ALL_STATUS, PENDING_EXECUTION, PENDING_REEXECUTION];
		public static const successfulQueueValues:Array = [ALL_STATUS, SUCCESSFUL_EXECUTED, SUCCESSFUL_REEXECUTED];
		public static const exceptionsQueueValues:Array = [ALL_STATUS, AGENCY_HOLD, AGENCY_REJECTION, COMPANY_HOLD];
		public static const executedQueueValues:Array = [ALL_STATUS, EXECUTED_SENT_TO_AGENCY, EXECUTED_SENT_TO_EFE, REEXECUTED_SENT_TO_AGENCY, REEXECUTED_SENT_TO_EFE];
		
		private var mCode:String;
		private var mLabel:String;


		public function TaxPaymentStatus(code:String = null, label:String = null)
		{
			mCode = code;
			mLabel = (label != null ? label : code);
		}

		public function get code():String {
			return mCode;
		}

		public function get label():String {
			return mLabel;
		}

		public function toString():String {
			return label;
		}

		public static function fromLabel(label:String):TaxPaymentStatus {
			for each (var enum:TaxPaymentStatus in values) {
				if (enum.label == label)
					return enum;
			}

			return null;
		}

		public static function valueOf(value:String):TaxPaymentStatus {
			for each (var enum:TaxPaymentStatus in values) {
				if (enum.code == value)
					return enum;
			}

			return null;
		}

	}
}