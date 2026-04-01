package psp.sap.application.modules
{
	import flash.events.IEventDispatcher;
	import flash.utils.Dictionary;
	
	import psp.sap.application.collections.ExplorersCollection;
	import psp.sap.view.ViewStackControlEx;

	[Bindable]
	public interface IModuleInterface extends IEventDispatcher
	{
		function get moduleName():String;
		function get moduleUrl():String;
		
		function initializeModule():void;
		function unloadModule():void;
		
		/* If this is set to true, it is not loaded unless user chooses for it to be */
		function optionalLoad():Boolean; 
		
		//The flag as to whether this module is set to optionally load.
		function get moduleActive():Boolean;
		function set moduleActive(value:Boolean):void;
		
		function initializeDisplays(value:Dictionary):Dictionary;
		
		function syncCompanyViewsToModuleOverlay(value:ViewStackControlEx):ViewStackControlEx;

		function createModuleViews(viewStack:ViewStackControlEx, explorersCollection:ExplorersCollection):void;

	}
}