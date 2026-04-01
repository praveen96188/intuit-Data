package test.sap
{
	import flexunit.framework.TestSuite;
	
	import mx.rpc.AsyncToken;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;
	
	import psp.sap.application.SAP;
	import psp.sap.application.events.SAPEvent;
	
	import test.sap.viewModel.ChaseReportViewModelTest;

	public class SingleSAPTestSuite
	{
		public function SingleSAPTestSuite()
		{
		}
		
		private var callbackFunction:Function;

		// After everything is built, configure the test
		// runner to use the appropriate test suite and
		// kick off the unit tests
		public function prepareForTests(callback:Function):void
		{
			callbackFunction = callback;
			SAP.instance.testMode = true;
			SAP.instance.resetForTesting();
			var dataLoader:RemoteObject = new RemoteObject("dataloaderservice");
			var token:AsyncToken = dataLoader.runDataLoader("Load User Data Only");
			token.addResponder(new mx.rpc.Responder(onLoadUsersComplete, onLoadUsersFault));
		}
			
		private function onLoadUsersFault(e:FaultEvent):void {
			trace("Error loading user data!");
		}
		
		private function onLoadUsersComplete(e:ResultEvent):void {
			// log in a test user

			SAP.instance.session.login(
				"AutoLogin",
				"admin", 
				onDataLoaderComplete, 
				onDataLoaderFault);
		}

		private function onDataLoaderComplete(e:ResultEvent):void {
			// tests will fail unless all lookup data is loaded first
			SAP.instance.lookupService.addEventListener(SAPEvent.DATA_LOAD_COMPLETED, callbackFunction);
			SAP.instance.lookupService.loadData();
 		}

 		private function onDataLoaderFault(e:FaultEvent):void {
 			trace("Error starting test cases because data loader service failed.");
 		}

		// Creates the test suite to run
		public function createSuite():TestSuite {

 			var ts:TestSuite = new TestSuite();
			 			
		 	ts.addTest( ChaseReportViewModelTest.suite() );                         								
			
            return ts;
 		}
	}
}
