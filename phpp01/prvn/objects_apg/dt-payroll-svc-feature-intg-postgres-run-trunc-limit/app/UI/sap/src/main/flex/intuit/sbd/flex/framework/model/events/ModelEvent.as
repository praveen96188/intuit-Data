package intuit.sbd.flex.framework.model.events
{
	import flash.events.Event;

	public class ModelEvent extends Event
	{
		public static const SAVE_SUCCEEDED:String = "savedSucceeded";
		public static const SAVE_FAULTED:String = "saveFaulted";		
		
		public var data:Object = null;
		
		public function ModelEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
		
		public static function createSaveSucceededEvent(token:Object = null):ModelEvent {
			return createEvent(SAVE_SUCCEEDED, token);
		}
		
		public static function createSaveFaultedEvent(token:Object):ModelEvent {
			return createEvent(SAVE_FAULTED, token);
		}
		
		private static function createEvent(type:String, data:Object = null):ModelEvent {
			var event:ModelEvent = new ModelEvent(type);
			event.data = data;
			return event;
		}
		
	}
}