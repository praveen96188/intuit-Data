package psp.sap.view
{
	import flash.events.Event;
	
	public class ActionLinkEvent extends Event
	{
		public static const ACTION_FIRE:String = "actionLinkFireEvent";
		
		private var mAction:String = "";
		private var mData:Object = null;
		
		public function get action():String
		{
			return mAction;
		} 
		
		public function set action(value:String):void
		{
			mAction = value;
		}
		
		public function get data():Object
		{
			return mData;
		} 
		
		public function set data(value:Object):void
		{
			mData = value;
		}
		
		public function ActionLinkEvent(action:String, data:Object = null, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			this.action = action;
			this.data = data;
			super(ACTION_FIRE, bubbles, cancelable);
		}
		
	}
}