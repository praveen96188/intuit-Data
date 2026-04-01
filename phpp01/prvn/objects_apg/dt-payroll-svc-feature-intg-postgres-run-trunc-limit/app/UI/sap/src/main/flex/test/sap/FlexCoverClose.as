package test.sap
{
	import flash.utils.getDefinitionByName;
	
	import flexunit.framework.TestSuite;
	
	import test.sap.application.SAPTestBase;
	
	public class FlexCoverClose extends SAPTestBase
	{	
		public static function suite():TestSuite {
			return new TestSuite(FlexCoverClose);
		}				
		
		public function testCloseFlexCover():void {
			try {
				getDefinitionByName('com.allurent.coverage.runtime.CoverageManager').exit();
			}			
			catch(e:Error) {
				// ignore not compiled with coverage
			}
		}
	}
}