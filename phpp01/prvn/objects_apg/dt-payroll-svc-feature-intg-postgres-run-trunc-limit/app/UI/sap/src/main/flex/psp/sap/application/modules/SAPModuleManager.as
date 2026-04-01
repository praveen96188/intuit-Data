package psp.sap.application.modules
{
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.events.ModuleEvent;
	import mx.modules.IModuleInfo;
	import mx.modules.ModuleManager;
	
	import psp.sap.application.SAP;
	import psp.sap.application.events.SAPEvent;
	import psp.sap.view.ViewStackControlEx;

	public class SAPModuleManager extends EventDispatcher
	{
		
		private static var mInstance:SAPModuleManager = null;
		
		//Strings of modules to load through the module manager
		//public static var modulesToLoad:ArrayCollection = new ArrayCollection(new Array(["TestTools"]));
		private var modulesToLoad:ArrayCollection;
		
		[ArrayElementType("psp.sap.application.modules.IModuleInterface")]
		private var mLoadedModules:Dictionary = new Dictionary(true);

		//Have to keep a temporary hold on the object for event listeners
		private var waitingModuleInfos:Dictionary = new Dictionary(true);

		private var modulesAccountedFor:Number = 0;
		
		
		//Store display registry
		
		public var initialModulesLoaded:Boolean = false;
		
		private static var instanceConstructorCall:Boolean = false;
		
		public function SAPModuleManager()
		{
			super();

			if (!instanceConstructorCall) throw new Error("SAPModuleManager is a singleton class.  Please use SAPModuleManager.instance instead of new SAPModuleManager()");
			if (mInstance != null) throw new Error("SAPModuleManager constructor should only be called once and instance is not null!");
			
			modulesToLoad = new ArrayCollection(new Array(["TestTools"]));
			SAP.instance.session.addEventListener(SAPEvent.SESSION_STARTED, initializeModules, false, 0, true);
			SAP.instance.session.addEventListener(SAPEvent.SESSION_ENDED, uninitializeModules, false, 0, true);
			
			//SAP.instance.addEventListener(, initializeModules, false, 0, true);
		}
		
		public static function get instance():SAPModuleManager {
			if(mInstance == null) {
				instanceConstructorCall = true;
				mInstance = new SAPModuleManager();
				instanceConstructorCall = false;
			}
			return mInstance;
		}
		
		public function initializeModuleViews(viewStack:ViewStackControlEx):void {
			for (var url:Object in mLoadedModules)
			{
				var localModule:AbstractModule = (mLoadedModules[url] as AbstractModule);
				
				if(localModule.moduleActive)
				{
					// move this code to the module
					localModule.createModuleViews(viewStack, SAP.instance.explorers);
				}
			}
		}
		
		public function initializeModules(event:Event):void {	
			mDisplayRegistry = new Dictionary(); // clear out dictionary		
			
			//Initializing syncs up the displays, as well.
			for (var url:Object in mLoadedModules)
			{
				var localModule:AbstractModule = (mLoadedModules[url] as AbstractModule);
				
				if(localModule.moduleActive)
				{
					SAP.instance.displayRegistry = localModule.initializeDisplays(SAP.instance.displayRegistry);
					//localModule.initializeModule();
				}
			}
			
			this.dispatchEvent(ModuleManagerEvent.createModulesInitializedEvent());
			
		}
		
		public function uninitializeModules(event:Event):void {
			for (var url:Object in mLoadedModules)
			{
				var localModule:AbstractModule = (mLoadedModules[url] as AbstractModule);
				
				if(localModule.moduleActive)
				{
					localModule.unloadModule();
				}
			}
		}
		
		public function loadModules():void {
			modulesAccountedFor = 0;
			
			for each(var moduleName:String in modulesToLoad)
			{
				loadModule(moduleName);
			}
		}
		
		public function unloadModules():void {
			for (var url:Object in mLoadedModules)
			{
				unloadModule(url as String);
			}
		}
		
		public function get loadedModules():Dictionary {
			return mLoadedModules;
		}
		
		public function isModuleLoaded(value:String):Boolean {
			return (getLoadedModuleByUrl(value) != null);
		}
				
		
		protected function getModuleInfo(value:String):IModuleInfo {
			return ModuleManager.getModule(value + ".swf");
		}

		public function unloadModule(url:String):void {	
			var iModuleInfo:IModuleInfo = ModuleManager.getModule(url);
			
			if(iModuleInfo.loaded)
			{
				iModuleInfo.addEventListener(ModuleEvent.ERROR, onUnloadError, false, 0, true);
				iModuleInfo.addEventListener(ModuleEvent.UNLOAD, onUnloaded, false, 0, true);
				waitingModuleInfos[iModuleInfo.url] = iModuleInfo;	
				return;	
			}		
		}
		
		public function loadModule(value:String):void {			
			var iModuleInfo:IModuleInfo = getModuleInfo(value);
			iModuleInfo.addEventListener(ModuleEvent.READY, setupModule, false, 0, true);
			iModuleInfo.addEventListener(ModuleEvent.ERROR, onError, false, 0, true);
			iModuleInfo.load();		
			
			waitingModuleInfos[iModuleInfo.url] = iModuleInfo;			
		}
		
		protected function getWaitingModuleInfoByUrl(value:String):IModuleInfo {
			return waitingModuleInfos[value];
		}
		
		
		
		protected function removeLoadedModuleByUrl(url:String):void {
			mLoadedModules[url] = null;
			delete mLoadedModules[url];
		}
		
		protected function removeWaitingModuleInfoByUrl(value:String):void {
			waitingModuleInfos[value] = null;
			delete waitingModuleInfos[value];
		}
		
		
		
		public function getLoadedModuleByUrl(value:String):IModuleInterface {
			return mLoadedModules[value];
		}
		
		public function getLoadedModuleByName(value:String):IModuleInterface {
			for (var url:Object in mLoadedModules)
			{
				var localModule:AbstractModule = (mLoadedModules[url] as AbstractModule);
				
				if(localModule.moduleName == value)
					return mLoadedModules[url];
				
			}
			
			return null;
		}
		
		
		
		public function onError(event:ModuleEvent):void {
			trace("** Error loading module: " + event.toString());
			
			var iModuleInfoFromEvent:IModuleInfo = event.target as IModuleInfo;
			
			//Remove info reference
			removeWaitingModuleInfoByUrl(iModuleInfoFromEvent.url);
			
			modulesAccountedFor++;
			checkAccountedFor();
		}
		
		protected function onUnloaded(event:ModuleEvent):void {
			var iModuleInfoFromEvent:IModuleInfo = event.target as IModuleInfo;
			removeLoadedModuleByUrl(iModuleInfoFromEvent.url);			
			removeWaitingModuleInfoByUrl(iModuleInfoFromEvent.url)
			trace("** Unloaded module: " + event.toString());
			
		}
		
		protected function onUnloadError(event:ModuleEvent):void {
			var iModuleInfoFromEvent:IModuleInfo = event.target as IModuleInfo;
			removeWaitingModuleInfoByUrl(iModuleInfoFromEvent.url)	
			trace("** Error unloading module: " + event.toString());
		}
		
		protected function checkAccountedFor():void {
			if(modulesAccountedFor == modulesToLoad.length)
			{
				this.dispatchEvent(ModuleManagerEvent.createModulesLoadedEvent());
				initialModulesLoaded = true;
			}
		}
		
		
		protected function setupModule(event:ModuleEvent):void {
			
			var iModuleInfoFromEvent:IModuleInfo = event.target as IModuleInfo;
			var iModuleInfo:IModuleInfo = getWaitingModuleInfoByUrl(iModuleInfoFromEvent.url);
							
			var moduleObject:Object = iModuleInfo.factory.create();
			var workingModule:AbstractModule = moduleObject as AbstractModule;

			
			//Remove info reference
			removeWaitingModuleInfoByUrl(iModuleInfo.url);

			//Module is working if it's not null!!!
			if(workingModule != null)
			{
				
				//Add loaded module
				//workingModule.name = AbstractModule(moduleObject).moduleName;
				mLoadedModules[iModuleInfo.url] = workingModule;
				
				//Initialize steps
				//workingModule.helloWorld();
				//SAP.instance.displayRegistry = workingModule.initializeDisplays(SAP.instance.displayRegistry);
			}
			
			modulesAccountedFor++;
			checkAccountedFor();

		}
		
		private var mDisplayRegistry:Dictionary = new Dictionary();
		
		public function addDisplayRegistry(id:String, displayObject:DisplayObject):void {
			mDisplayRegistry[id] = displayObject;
		}
		
		public function getDynamicContentById(id:String):DisplayObject {
			return mDisplayRegistry[id] as DisplayObject;
		}

	}
}