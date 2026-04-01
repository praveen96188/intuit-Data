package psp.sap.viewmodel.events
{
	import mx.rpc.Fault;
	import mx.rpc.events.FaultEvent;
	
	public class ViewModelFaultEvent extends ViewModelMessageEvent
	{
		
		public function ViewModelFaultEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false, faultObject:Fault=null)
		{
			var errorMessage:String = "";
			
			if(faultObject != null)
			{
				errorMessage = faultObject.message;
			}
			
			super(type, bubbles, cancelable, errorMessage);
				
			if(faultObject != null)
			{
				this.data = faultObject;
			}	
		}
		
		public static const LOGIN_FAULTED:String = "loginFaulted";
		public static const SEARCH_FAULTED:String = "searchFaulted";
		public static const SAVE_FAULTED:String = "saveFaulted";
		
		private static function createViewModelFaultEvent(type:String, e:FaultEvent):ViewModelFaultEvent {
			var event:ViewModelFaultEvent = new ViewModelFaultEvent(type, false, false, e.fault);
			return event;			
		}
		
		public static function createLoginFaultedEvent(e:FaultEvent):ViewModelFaultEvent {
			return createViewModelFaultEvent(LOGIN_FAULTED, e);
		}
		
		public static function createSaveFaultedEvent(e:FaultEvent):ViewModelFaultEvent {
			return createViewModelFaultEvent(SAVE_FAULTED, e);
		}
	}
}