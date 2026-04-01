package psp.sap.application.modules
{
	import flash.events.Event;
	
	public class ModuleManagerEvent extends Event
	{
		public static const MODULES_LOADED:String = "modulesLoaded";
		public static const MODULES_INITIALIZED:String = "modulesInitialized";
		
		public function ModuleManagerEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
		
		public static function createModulesLoadedEvent():ModuleManagerEvent {
			return new ModuleManagerEvent(MODULES_LOADED);
		}
		
		public static function createModulesInitializedEvent():ModuleManagerEvent {
			return new ModuleManagerEvent(MODULES_INITIALIZED);
		}
		
				
	}
}