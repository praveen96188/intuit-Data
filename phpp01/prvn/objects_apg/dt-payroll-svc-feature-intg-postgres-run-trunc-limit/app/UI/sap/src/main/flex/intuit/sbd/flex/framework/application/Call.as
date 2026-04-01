package intuit.sbd.flex.framework.application
{
	import flash.events.Event;
	import flash.events.EventDispatcher;
	
	public class Call
	{
		
		private var func:Function;
		private var target:EventDispatcher;
		private var caller:Caller;		
		private var eventString:String;
		
		public function Call(func:Function)
		{
			this.func = func;	
		}
		
		public function andOn(target:EventDispatcher, event:String):Caller{
			this.target = target;
			this.eventString = event;
			target.addEventListener(event,onEvent,false,0,false);									
			caller = new Caller(this);
			return caller;				
		}
		
		private function onEvent(event:Event):void{
			target.removeEventListener(eventString,onEvent);
			caller.doCall();			
		}
		
		internal function doCall():void{			
			func();
		}		

	}
}