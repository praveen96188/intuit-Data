package psp.sap.viewmodel.events
{
	import flash.events.Event;
	
	public class ViewModelMessageEvent extends ViewModelEvent
	{	
		private var mMessage:String = null;
		public function ViewModelMessageEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false, message:String = null)
		{
			super(type, bubbles, cancelable);
			mMessage = message;
		}
		
		public function get message():String {
			return mMessage;
		}
		
		override public function clone():Event {
			var e:ViewModelMessageEvent = new ViewModelMessageEvent(type, bubbles, cancelable, message);
			return e; 
		}
		
	}
}