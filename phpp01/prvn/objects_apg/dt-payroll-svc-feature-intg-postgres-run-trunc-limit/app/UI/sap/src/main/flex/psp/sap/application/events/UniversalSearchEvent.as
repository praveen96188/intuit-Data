package psp.sap.application.events
{
	import flash.events.Event;
	
	public class UniversalSearchEvent extends Event
	{
		
		public var searchValue:String;
		public var searchType:String;
		
		public static const UNIVERSAL_SEARCH:String = "universalSearch";
		
		public function UniversalSearchEvent(type:String, searchValue:String, searchType:String="ein", bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type,bubbles,cancelable);
			this.searchValue = searchValue;
			this.searchType = searchType;						
		}
		
		public static function createUniversalSearchEvent(searchValue:String, searchType:String):UniversalSearchEvent {
			return new UniversalSearchEvent(UNIVERSAL_SEARCH,searchValue,searchType);
		}

	}
}