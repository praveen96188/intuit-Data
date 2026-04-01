package test.sap.application
{
	import flexunit.framework.TestSuite;
	
	import mx.events.PropertyChangeEvent;

	import psp.sap.application.SAP;
	import psp.sap.application.enums.ExplorerEnum;
    import psp.sap.viewmodel.AbstractExplorer;

    //todo this test isn't testing anything--needs to be retested after finishing universal search and landing pages
	public class SAPActiveExplorerTest extends SAPTestBase
	{
		public function SAPActiveExplorerTest(methodName:String=null)
		{
			super(methodName);
		}

  		public static function suite():TestSuite {
   			return new TestSuite( SAPActiveExplorerTest );
   		}
		
		public function testAppInitialization(): void {
			assertNotNull("activeExplorer", mSAP.activeExplorer);
		}
		
		public function testDirectSet(): void {
			mSAP.activeExplorer = null;
			assertNull("activeExplorer", mSAP.activeExplorer);
					
		}
		
		public function testExplorerActivate(): void {
			mSAP.activeExplorer = null;
			
		}
		
		public function testExplorers():void {
			var explorer:AbstractExplorer = SAP.instance.explorers.getExplorer(ExplorerEnum.COMPANY);
			explorer.activate();
		}
		
		private function assertActiveExplorer(expectedCurrent:AbstractExplorer, expectedLast:AbstractExplorer): void {
			assertTrue(lastEventCaught is PropertyChangeEvent);
			assertEquals("eventType", SAP.ACTIVE_EXPLORER_CHANGED, PropertyChangeEvent(lastEventCaught).type);
			assertEquals("event - old value", expectedLast, PropertyChangeEvent(lastEventCaught).oldValue);
			assertEquals("event - new value", expectedCurrent, PropertyChangeEvent(lastEventCaught).newValue);
			assertEquals("activeExplorer", expectedCurrent, mSAP.activeExplorer);			
		}		
	}
}