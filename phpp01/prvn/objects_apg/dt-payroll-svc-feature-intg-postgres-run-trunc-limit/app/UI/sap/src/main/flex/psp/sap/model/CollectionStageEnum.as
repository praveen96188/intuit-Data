package psp.sap.model
{
	public class CollectionStageEnum
	{
		public static const FIRST:CollectionStageEnum = new CollectionStageEnum("FirstCollectionAttempt","1st Collection Attempt");    //"1st Collection Attempt");
		public static const SECOND:CollectionStageEnum = new CollectionStageEnum("SecondCollectionAttempt","2nd Collection Attempt");  //"2nd Collection Attempt");
		public static const TERM:CollectionStageEnum = new CollectionStageEnum("TerminationExpected","Termination Expected");          //"Termination Expected");
		public static const values:Array = [FIRST, SECOND, TERM];
		
		private var mCode:String;
		private var mLabel:String;
		
		//private var mIndex:int;
		
		public function CollectionStageEnum(code:String, label:String)
		{
			mCode = code;
			mLabel = label;
					
		//	mIndex = index;
		}

		public function get code():String {
			return mCode;
		}
		
		public function set code(value:String):void {
			mCode = code;
		}
		
		public function get label():String {
			return mLabel;
		}
		
		public function set label(value:String):void {
			mLabel = value;
		}
		
	/*	[Bindable]
		public function get index():int {
			return mIndex;
		}
		
		public function set index(value:int):void {
			mIndex = index;
		}
	*/	
	
		public function toString():String {
			return mCode;
		}
	}
}
