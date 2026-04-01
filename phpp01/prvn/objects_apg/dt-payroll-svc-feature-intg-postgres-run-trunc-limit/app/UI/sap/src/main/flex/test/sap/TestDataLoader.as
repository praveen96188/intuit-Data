package test.sap
{
	import flexunit.framework.TestSuite;
	
	import mx.rpc.events.ResultEvent;
	
	import test.sap.application.SAPTestBase;
	
	public class TestDataLoader extends SAPTestBase {
		public static function suite():TestSuite {
			return new TestSuite(TestDataLoader);
		}
    
	    public function testRunBasicDataLoader():void {
	    	runDataLoader("Company :: Create Basic Data", testConfirmDataLoaded, 10);
	    }
	    
	    private function testConfirmDataLoaded(e:ResultEvent):void {
		    assertTrue(true);
		}
	}
}