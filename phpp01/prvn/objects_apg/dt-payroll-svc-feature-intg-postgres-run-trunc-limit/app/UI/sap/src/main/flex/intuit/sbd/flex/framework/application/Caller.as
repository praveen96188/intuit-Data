package intuit.sbd.flex.framework.application
{
	import flash.events.Event;
	import flash.events.EventDispatcher;
	
	internal class Caller extends EventDispatcher
	{
		private var mCall:Call;
		private var func:Function;
		
		public function Caller(pCall:Call)
		{
			mCall=pCall;
		}
	

		public function call(func:Function):void{
			this.func = func;
			mCall.doCall();
		}
		
		internal function doCall():void{
			func();
		}

	}
}