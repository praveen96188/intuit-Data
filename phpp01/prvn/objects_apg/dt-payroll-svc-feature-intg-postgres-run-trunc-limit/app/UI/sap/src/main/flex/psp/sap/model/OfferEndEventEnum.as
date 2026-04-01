package psp.sap.model
{
	public class OfferEndEventEnum
	{
		public static const DATEEVENT:OfferEndEventEnum = new OfferEndEventEnum("DateEvent");
		public static const DURATIONEVENT:OfferEndEventEnum = new OfferEndEventEnum("DurationEvent");
		public static const PAYROLLUSAGEEVENT:OfferEndEventEnum = new OfferEndEventEnum("PayrollUsageEvent");
		
		public static const accountTypes:Array = [DATEEVENT, DURATIONEVENT, PAYROLLUSAGEEVENT];
		private var mCode:String;
		
		public function OfferEndEventEnum(code:String)
		{
			mCode = code;
		}
			
		public function toString():String {
			return mCode;
		}		
	}
}