package psp.sap.model
{
	import flash.events.EventDispatcher;
	
	public class OfferingServiceChargeTypeEnum extends EventDispatcher
	{
		public static const NSF:OfferingServiceChargeTypeEnum = new OfferingServiceChargeTypeEnum("DebitReturnFee", "Return");
		public static const Reversal:OfferingServiceChargeTypeEnum = new OfferingServiceChargeTypeEnum("ReversalFee", "Reverse");
		
		private var mCode:String;
		private var mLabel:String;
		
		public function OfferingServiceChargeTypeEnum(code:String, label:String)
		{
			mCode = code;
			mLabel = label;
		}
		
		[Bindable("propertyChange")]
		public function get code():String {
			return mCode;
		}
		
		[Bindable("propertyChange")]
		public function get label():String {
			return mLabel;
		}
	}
}