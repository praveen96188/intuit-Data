package psp.sap.viewmodel.events
{
	import flash.events.Event;
	
	import mx.events.FlexEvent;
	
	public class ViewModelUserConfirmationEvent extends FlexEvent
	{
		public static const USER_CONFIRMATION:String = "userConfirmation";
		
		public var data:Object = null;
		
		public function ViewModelUserConfirmationEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false, data:Object = null)
		{
			super(type, bubbles, cancelable);
			this.data = data;
		}
		
		override public function clone():Event {
			var e:ViewModelUserConfirmationEvent = new ViewModelUserConfirmationEvent(type, bubbles, cancelable, data);
			return e; 
		}
		
		public static function createEvent(type:String = USER_CONFIRMATION, data:Object = null):ViewModelUserConfirmationEvent {
			return new ViewModelUserConfirmationEvent(type, false, false, data);
		}		

	}
}