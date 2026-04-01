package psp.sap.application.events
{
	import flash.events.Event;

	/**
	 * Events fired from the IInspector defined methods.
	 */
	public class InspectorActionEvent extends Event
	{
		public static const SAVE_INITIATED_EVENT:String = "saveInitiated";
		public static const SAVE_COMPLETED_EVENT:String = "saveCompleted";
		public static const SAVE_FAULTED:String = "saveFaulted";		
		
		public static const SAVE_CONFIRMATION_EVENT:String = "saveConfirmation";
	
		public static const CLOSE_EVENT:String = "close";
		public static const CLOSED_EVENT:String = "closed";
		
		public function InspectorActionEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}

		public static function createSaveInitiatedEvent():InspectorActionEvent {
			return new InspectorActionEvent(SAVE_INITIATED_EVENT);
		}
		
		public static function createSaveCompletedEvent():InspectorActionEvent {
			return new InspectorActionEvent(SAVE_COMPLETED_EVENT);
		}
		
		public static function createSaveFaultedEvent():InspectorActionEvent {
			return new InspectorActionEvent(SAVE_FAULTED);
		}
		
		public static function createSaveConfirmationEvent():InspectorActionEvent {
			return new InspectorActionEvent(SAVE_CONFIRMATION_EVENT);
		}
		
		public static function createCloseEvent():InspectorActionEvent {
			return new InspectorActionEvent(CLOSE_EVENT);
		}
		
		public static function createClosedEvent():InspectorActionEvent {
			return new InspectorActionEvent(CLOSED_EVENT);
		}
			
	}
}