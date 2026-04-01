package psp.sap.application.events
{
	import flash.events.Event;

	public class SAPSearchEvent extends SAPEvent
	{
		public static const SEARCH_INITIATED:String = "searchInitiatedEvent";
		public static const SEARCH_COMPLETED:String = "searchCompletedEvent";
		public static const SEARCH_FAULTED:String	= "searchFaultedEvent";
		
		public function SAPSearchEvent(type:String, data:Object = null, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, data, bubbles, cancelable);
		}
		
		override public function clone():Event {
			return new SAPSearchEvent(this.type, this.data, this.bubbles, this.cancelable);
		} 
		
		public static function createSearchInitiatedEvent():SAPSearchEvent {
			return new SAPSearchEvent(SEARCH_INITIATED);
		}
		
		public static function createSearchCompletedEvent():SAPSearchEvent {
			return new SAPSearchEvent(SEARCH_COMPLETED);
		}
		
		public static function createSearchFaultedEvent(msg:String = ""):SAPSearchEvent {
			return new SAPSearchEvent(SEARCH_FAULTED, msg);
		}
		
	}
}