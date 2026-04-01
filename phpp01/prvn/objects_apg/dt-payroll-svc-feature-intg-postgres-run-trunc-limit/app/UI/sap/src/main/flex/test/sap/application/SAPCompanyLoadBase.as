package test.sap.application
{
	import flash.utils.getQualifiedClassName;
	
	import flexunit.framework.Assert;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.Responder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.model.Company;
	
	public class SAPCompanyLoadBase extends SAPTestBase
	{
		public function SAPCompanyLoadBase(methodName:String=null)
		{
			super(methodName);
		}
		
		protected var mCompany:Company;
		
		override public function tearDown():void {
			super.tearDown();
			
			mCompany = null;			
		}		
		
		protected function doCompanySearch(nextFunction:Function):void {
			mSAP.companyService.findCompany("QBOE","1234567",
				new Responder(addAsync(onSetupComplete, DEFAULT_ASYNC_TIMEOUT, nextFunction), onSetupFailed));
		}
		
		protected virtual function onSetupComplete(e:ResultEvent, data:Object):void {			
			mCompany = e.result as Company;
			data();
		}
		
		protected virtual function onSetupFailed(e:FaultEvent):void {
			Assert.fail(getQualifiedClassName(this) + " setUp failed: " + e.message); 
		}
		
		
	}
}