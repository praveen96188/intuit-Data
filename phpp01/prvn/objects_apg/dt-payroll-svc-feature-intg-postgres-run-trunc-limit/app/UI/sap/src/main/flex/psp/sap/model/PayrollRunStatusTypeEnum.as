package psp.sap.model
{
	public class PayrollRunStatusTypeEnum
	{
		public static const COMPLETE:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("Complete");
		public static const CANCELED:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("Canceled");
		public static const DEBITRETURNEDCANCELED:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("DebitReturnedCanceled");
		public static const DEBITRETURNED:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("DebitReturned");
		public static const NSFCANCELED:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("NSFCanceled");
		public static const OFFLOADEDALL:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("OffloadedAll");
		public static const OFFLOADEDDEBIT:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("OffloadedDebit");
		public static const PENDING:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("Pending");
		public static const WRITTENOFF:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("WrittenOff");
		public static const PENDINGREVERSALS:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("PendingReversals");
		public static const PENDINGAUTOREDEBIT:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("PendingAutoRedebit");
		public static const AUTOREDEBITOFFLOADED:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("AutoRedebitOffloaded");
		public static const PENDINGREDEBIT:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("PendingRedebit");
		public static const REDEBITOFFLOADED:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("RedebitOffloaded");
		public static const PENDINGWIRE:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("PendingWire");
		public static const REVERSALSOFFLOADED:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("ReversalsOffloaded");
		public static const REVERSALSFINISHED:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("ReversalsFinished");
		public static const RETURNEDTWICE:PayrollRunStatusTypeEnum = new PayrollRunStatusTypeEnum("ReturnedTwice");
		
 		private var mCode:String;
		
		public function PayrollRunStatusTypeEnum(code:String)
		{
			mCode = code;
		}
		
		public function get allowInitiateReversal():Boolean {
			var retVal:Boolean = true;
			switch (mCode) {
				case COMPLETE.code :
				case OFFLOADEDALL.code :
				case REDEBITOFFLOADED.code :
				case AUTOREDEBITOFFLOADED.code :
					retVal = false;
					break;
			}
		return retVal;
		}
		
		public function get code():String {
			return mCode;
		}
	
		public function toString():String {
			return mCode;
		}
	}
}