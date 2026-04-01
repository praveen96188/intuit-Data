package psp.sap.application.events
{
	import mx.events.PropertyChangeEvent;
	import mx.events.PropertyChangeEventKind;

	public class InspectorPropertyChangeEvent extends PropertyChangeEvent
	{
		public static const ACTIVE_PAGE_CHANGED:String = "activePageChanged";
		
		public function InspectorPropertyChangeEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false, kind:String=null, property:Object=null, oldValue:Object=null, newValue:Object=null, source:Object=null)
		{
			super(type, bubbles, cancelable, kind, property, oldValue, newValue, source);
		}
		
		public static function createActivePageChanged(oldValue:Object, newValue:Object):InspectorPropertyChangeEvent {
			return new InspectorPropertyChangeEvent(ACTIVE_PAGE_CHANGED, 
													false, 
													false, 
													PropertyChangeEventKind.UPDATE, 
													"activePage", 
													oldValue, 
													newValue);
		}
		
	}
}