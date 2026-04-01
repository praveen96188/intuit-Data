package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import psp.sap.application.enums.CompanySearchEnum;
	import psp.sap.view.controls.UniversalSearchFieldViewModel;
	
	import test.sap.application.SAPTestBase;
	
		
	public class UniversalSearchFieldViewModelTest extends SAPTestBase
	{
		
		private var viewModel:UniversalSearchFieldViewModel;
		
		public function UniversalSearchFieldViewModelTest(methodName:String=null)
		{
			super(methodName);		
		}
		
		public static function suite() : TestSuite {
			return new TestSuite( UniversalSearchFieldViewModelTest );
		}
		
		override public function setUp():void {
			super.setUp();
			
			viewModel = new UniversalSearchFieldViewModel();
		}
		
		
		
		public function testCanSearch():void {
			
			var goodValues:Array = ["hello","goodbye","100","10000","  hello  ", "123-45-6789","---"];
			var badValues:Array = ["hi",""," ","10","        "];
			
			//nothing on creation, so not searchable
			assertFalse(viewModel.canSearch);
			
			//switch back and forth
			for (var i:int = 0; i < Math.max(goodValues.length, badValues.length); i++) {
				viewModel.searchText = goodValues[i % goodValues.length];
				assertTrue(goodValues[i % goodValues.length] + " is valid",viewModel.canSearch);
				viewModel.searchText = badValues[i % badValues.length];
				assertFalse(badValues[i % badValues.length] + "is not valid",viewModel.canSearch);
			}						
			
		}
				
		public function testGuesses():void {
			var eins:Array = ["123456789","123-45-6789","1-2-3-4-5-6-7-8-9","123 45 6789","123-45 6789","123456789 ","123-456-789 "];
			var psids:Array = ["123","123456","12-345","123-567","123 ","123 45","123-5 53 3","1-2-3-4-5-6-7", "G12345678"];
			var legalNames:Array = ["pizza loadletter","craig's landscaping","123 cleaners","1 hour photo","15-day photo","$2 Deluxe Hugs"];
			
			for (var i:int = 0; i < Math.max(eins.length, psids.length,legalNames.length); i++) {
				viewModel.searchText = eins[i % eins.length];
				assertEquals(eins[i % eins.length] + " is an ein",CompanySearchEnum.SEARCH_BY_EIN,viewModel.searchType);
				
				viewModel.searchText = psids[i % psids.length];
				assertEquals(psids[i % psids.length] + " is a psid",CompanySearchEnum.SEARCH_BY_PSID,viewModel.searchType);
				
				viewModel.searchText = legalNames[i % legalNames.length];
				assertEquals(legalNames[i % legalNames.length] + " is a legal name",CompanySearchEnum.SEARCH_BY_LEGAL_NAME,viewModel.searchType);				
			}
		}			

	}
}