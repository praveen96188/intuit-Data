package psp.sap.application.events
{
	import flash.events.Event;

	public class SAPEvent extends Event
	{
		public static const SESSION_STARTED:String = "sessionStarted";
		public static const SESSION_ENDED:String = "sessionEnded";
		
		public static const DATA_LOAD_INITIATED:String = "dataLoadIntitiated";
		public static const DATA_LOAD_COMPLETED:String = "dataLoadCompleted";
		public static const DATA_LOAD_FAULTED:String = "dataLoadFaulted";
		
		public static const EXPLORER_CHANGED:String = "explorerChanged";
		
		public function SAPEvent(type:String, data:Object = null, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.data = data;
		}
		
		public var data:Object;
		
		override public function clone():Event {
			throw new Error("subclasses of SAPEvent must override clone");
		}
		
		public static function createSessionStartedEvent():SAPEvent {
			return new SAPEvent(SESSION_STARTED);
		}		
		
		public static function createSessionEndedEvent():SAPEvent {
			return new SAPEvent(SESSION_ENDED);
		}
		
		public static function createDataLoadInitiatedEvent():SAPEvent {
			return new SAPEvent(DATA_LOAD_INITIATED);
		}				
		
		public static function createDataLoadCompletedEvent():SAPEvent {
			return new SAPEvent(DATA_LOAD_COMPLETED);	
		}				
		
		public static function createDataLoadFaultedEvent():SAPEvent  {
			return new SAPEvent(DATA_LOAD_FAULTED);
		}
		
		public static function createExplorerChangedEvent():SAPEvent  {
			return new SAPEvent(EXPLORER_CHANGED);
		}				
	}
}