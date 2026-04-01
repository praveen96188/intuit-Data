package psp.sap.application.modules
{
	import flash.utils.Dictionary;
	
	import mx.containers.Canvas;
	import mx.controls.Label;
	import mx.modules.Module;
	
	import psp.sap.application.collections.ExplorersCollection;
	import psp.sap.view.ViewStackControlEx;

	public class AbstractModule extends Module
	{
		private var mModuleActive:Boolean = false;
		
		[Bindable]
		public var displayOverlays:Dictionary = new Dictionary(true);
		
		public function AbstractModule()
		{
			super();
		}
		
		public virtual function get moduleName():String {
			return "";
		}
		
		public final function get moduleUrl():String {
			for (var url:String in SAPModuleManager.instance.loadedModules) {
				if (this.moduleName == AbstractModule(SAPModuleManager.instance.loadedModules[url]).moduleName)
					return url;
			}
			return null;
		}
		
		public virtual function initializeDisplays(value:Dictionary):Dictionary {
			return value;
		}
		
		public virtual function initializeModule():void {
			
		}
		
		public virtual function unloadModule():void {}
		
		/* If this is set to true, it is not loaded unless user chooses for it to be */
		public function optionalLoad():Boolean {
			return true;
		}
		
		[Bindable]
		public function get moduleActive():Boolean {
			return mModuleActive;
		}
		
		public function set moduleActive(value:Boolean):void {
			mModuleActive = value;
		}
		
		public function syncCompanyViewsToModuleOverlay(value:ViewStackControlEx):ViewStackControlEx {
			/* Override me */
			return value;
		}
		
		public function createModuleViews(viewStack:ViewStackControlEx, explorersCollection:ExplorersCollection):void
		{
			// override this in sub-class to add explorer view to viewstack and explorer viewModel to explorerscollection			
		}
		
	}
}