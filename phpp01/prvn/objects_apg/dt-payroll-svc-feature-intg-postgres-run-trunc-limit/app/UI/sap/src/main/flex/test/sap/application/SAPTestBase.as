package test.sap.application
{	
	import flash.utils.describeType;
	import flash.utils.getDefinitionByName;
	import flash.utils.getQualifiedClassName;
	
	import mx.collections.ArrayCollection;
	import mx.formatters.DateFormatter;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.Responder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.RemoteObject;
	
	import psp.sap.application.SAP;
	import psp.sap.application.events.SAPEvent;

	public class SAPTestBase extends TestCaseEx
	{
		public function SAPTestBase(methodName:String=null)
		{
			super(methodName);
			dateFormatter.formatString = SAP.instance.configuration.dateFormatShort;
		}
		
   		public const USERNAME:String = "AutoLogin";
   		public const PASSWORD:String = "admin";				
		
		protected var mSAP:SAP = null;		
		
		private var dataLoaderService:RemoteObject = new RemoteObject("dataloaderservice");
		protected var dateFormatter:DateFormatter = new DateFormatter(); 	
		
		public function runDataLoader(key:String, resultFunc:Function, multiplier:Number = 1):void {
			var remoteToken:AsyncToken = AsyncToken(dataLoaderService.runDataLoader(key));
			remoteToken.addResponder(new Responder(addAsync(resultFunc, this.asyncTimeout * multiplier), onRemoteFault));	
		}
		
		public function onRemoteFault(e:FaultEvent):void {
			trace("Fault!  Details: " + e.message);			
		}
		
		public function getTestResponder(resultFunc:Function, multiplier:Number = 1):Responder {
			return new Responder(addAsync(resultFunc, this.asyncTimeout * multiplier), onRemoteFault); 
		}
		
		override public function setUp():void {
			super.setUp();
			
			mSAP = SAP.instance;
			
			// fake login to initialize SAP w/o cost of hitting LDAP, etc.
			mSAP.session.dispatchEvent(SAPEvent.createSessionStartedEvent());
			
			trace("test class: " + getQualifiedClassName(this));
			
			mSAP.addEventListener(SAP.ACTIVE_EXPLORER_CHANGED, onEvent);						
		}
		
		override public function tearDown():void {
			super.tearDown();
			
			mSAP.removeEventListener(SAP.ACTIVE_EXPLORER_CHANGED, onEvent);						
		}
				
		public function assertActiveExplorerType(type:Class):void {
			assertTrue("ActiveExplorer is type: " + type.toString(), SAP.instance.activeExplorer is type);
		}
		
		public function nothingNull(o:Object):Boolean {
			var typeInfo:XML = describeType(o);
			
			var mExcludeList: ArrayCollection = new ArrayCollection();
			addExcludeAndNullOkValues(typeInfo, mExcludeList);
						
			for each (var n:* in typeInfo..accessor) {
				var propertyName: String = n.@name.toString();
				if (mExcludeList.contains(propertyName)) {
					continue;	
				}
				
				if (o[n.@name] == null) {
					trace(n.@name + " was null!  Fails verification.");
					return false;
				}	
			}
			
			return true;
        }
        
        private function addExcludeAndNullOkValues(typeInfo: XML, pExcludeList: ArrayCollection, pParentList: ArrayCollection = null): void {
        	for each (var metadata:* in typeInfo..metadata) {
        		if (metadata.@name == "ValueEquals") {
	        		for each (var valueEqualsArgNode:* in metadata..arg) {
	        			var valueEqualsKey:String = valueEqualsArgNode.@key.toString();
	        			if (valueEqualsKey == "exclude") 
	        				pExcludeList.addItem(valueEqualsArgNode.@value.toString());	        				
	        		}
	        	}
        		if (metadata.@name == "CopyTestCase") {
	        		for each (var copyTestCaseArgNode:* in metadata..arg) {
	        			var copyTestCaseKey:String = copyTestCaseArgNode.@key.toString();
	        			if (copyTestCaseKey == "nullOk") 
	        				pExcludeList.addItem(copyTestCaseArgNode.@value.toString());	        				
	        		}
	        	}
        	}
        	
        	if (pParentList == null)
        		pParentList = new ArrayCollection();
        		
        	for each (var extendsClass:* in typeInfo..extendsClass) {
        		var parentClassName: String = extendsClass.@type.toString();
        		if ((parentClassName != "Class") && (parentClassName != "Object") && (!pParentList.contains(parentClassName))) {
        			pParentList.addItem(parentClassName);
	        		var parentTypeInfo: XML = describeType(getDefinitionByName(parentClassName));
	        		addExcludeAndNullOkValues(parentTypeInfo, pExcludeList, pParentList);
	        	}
        	}	
        }
        
        public function changePSPDate(date:Date, responder:IResponder):void {
				var remoteToken:AsyncToken = dataLoaderService.changePSPDate(date);
				remoteToken.addResponder(responder);
		}
		
		public function login(resultFunction:Function, username:String = null, password:String = null):void {
			var usernameValue:String = username;
			var passwordValue:String = password;
			
			if ((usernameValue == null) || (passwordValue == null)) {
				usernameValue = USERNAME;
				passwordValue = PASSWORD;
			}
			
			SAP.instance.session.login(usernameValue, passwordValue, 
				addAsync(resultFunction, this.asyncTimeout * 25), onRemoteFault, onRemoteFault);
		}
	}				
}