package test.sap.service
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.enums.CompanySearchEnum;
	import psp.sap.model.CompanySearchResult;
	import psp.sap.service.interfaces.ICompanyService;
	
	import test.sap.application.SAPTestBase;
	
	public class CompanyServiceSearchTest extends SAPTestBase
	{
		public function CompanyServiceSearchTest(methodName:String=null)
		{
			super(methodName);
		}
		
  		public static function suite():TestSuite {
   			return new TestSuite( CompanyServiceSearchTest );
   		}
   		
   		private var mCompanyService:ICompanyService;
   		[ArrayElementType("psp.sap.model.CompanySearchResult")]
   		private var mSearchResults:ArrayCollection
   		
   		override public function setUp():void {
   			super.setUp();
   			mCompanyService = mSAP.companyService;
   			mSearchResults = new ArrayCollection();   			
   		}
   		
   		override public function tearDown():void {
   			super.tearDown();
   		}
	 	
	 	//---------------------
	 	// EIN search
	 	//---------------------
	 	public static const INTUIT_EIN:String = "123456789";	
   		public function testEINSearch():void {
   			runDataLoader("Company :: Create Basic Data", testEINSearch_Step2, 5); 
   		}			
   		
   		private function testEINSearch_Step2(e:ResultEvent):void {
   			login(testEINSearch_Step3);
   		}
   		
   		private function testEINSearch_Step3(e:ResultEvent):void {
   			mCompanyService.search(	CompanySearchEnum.SEARCH_BY_EIN,
   									INTUIT_EIN, 
   									getTestResponder(testEINSearch_and_testLegalNameExactSearch_Step4));
   		}
   		
   		private function testEINSearch_and_testLegalNameExactSearch_Step4(e:ResultEvent):void {
   			mSearchResults.source = ArrayCollection(e.result).toArray();
   			assertEquals("search result count", 1, mSearchResults.length);
   			assertEquals("company EIN", INTUIT_EIN, CompanySearchResult(mSearchResults.getItemAt(0)).fein);
   		}


		
		//---------------------
	 	// EIN bad search
	 	//---------------------
	 	public static const INTUIT_EIN_BAD:String = "123456780";	
   		public function testEINSearchBad():void { 			
   			runDataLoader("Company :: Create Basic Data", testEINSearch_Step2, 5); 
   		}			
   		
   		private function testEINSearchBad_Step2(e:ResultEvent):void {
   			login(testEINSearchBad_Step3);
   		}
   		
   		private function testEINSearchBad_Step3(e:ResultEvent):void {
   			mCompanyService.search(	CompanySearchEnum.SEARCH_BY_EIN, INTUIT_EIN_BAD, 
   									getTestResponder(testEINSearchBad_Step4));
   		}
   		
   		private function testEINSearchBad_Step4(e:ResultEvent):void {
   			mSearchResults.source = ArrayCollection(e.result).toArray();
   			assertEquals("search result count", 0, mSearchResults.length);
   		}
		

	 	//---------------------
	 	// legalName search
	 	//---------------------   		
   		public static const INTUIT_LEGAL_NAME:String = "Intuit";
   		public function testLegalNameExactSearch():void {
   			runDataLoader("Company :: Create Basic Data", testLegalNameExactSearch_Step2, 5); 
   		}			
   		
   		private function testLegalNameExactSearch_Step2(e:ResultEvent):void {
   			login(testLegalNameExactSearch_Step3);
   		}
   		
   		private function testLegalNameExactSearch_Step3(e:ResultEvent):void {
   			mCompanyService.search(	CompanySearchEnum.SEARCH_BY_LEGAL_NAME, INTUIT_LEGAL_NAME, 
   									getTestResponder(testEINSearch_and_testLegalNameExactSearch_Step4));

   		}
   		  		
   		public static const INTUIT_LEGAL_PARTIAL_NAME:String = "Intu";
   		public function testLegalNamePartialSearch():void {
   			runDataLoader("Company :: Create Basic Data", testLegalNamePartialSearch_Step2, 5); 
   		}			
   		
   		private function testLegalNamePartialSearch_Step2(e:ResultEvent):void {
   			login(testLegalNamePartialSearch_Step3);
   		}
   		
   		private function testLegalNamePartialSearch_Step3(e:ResultEvent):void {
   			// NOTE: intentionally reusing exact name verification method as result handler
   			mCompanyService.search(	CompanySearchEnum.SEARCH_BY_LEGAL_NAME, INTUIT_LEGAL_PARTIAL_NAME,  
   									getTestResponder(testLegalNamePartialSearch_Step4));
   		}
   		
   		private function testLegalNamePartialSearch_Step4(e:ResultEvent):void {
   			mSearchResults.source = ArrayCollection(e.result).toArray();
   			assertEquals("search result count", 1, mSearchResults.length);
   			assertEquals("company legal name", INTUIT_LEGAL_NAME, CompanySearchResult(mSearchResults.getItemAt(0)).legalName);   			
   		}
   		

		public static const INTUIT_LEGAL_NAME_BAD:String = "TacoSauceNumber123491827";
   		public function testLegalNameBadSearch():void {
   			runDataLoader("Company :: Create Basic Data", testLegalNameBadSearch_Step2, 5); 
   		}			
   		
   		private function testLegalNameBadSearch_Step2(e:ResultEvent):void {
   			login(testLegalNameBadSearch_Step3);
   		}
   		
   		private function testLegalNameBadSearch_Step3(e:ResultEvent):void {
   			// NOTE: intentionally reusing exact name verification method as result handler
   			mCompanyService.search(	CompanySearchEnum.SEARCH_BY_LEGAL_NAME, INTUIT_LEGAL_NAME_BAD, 
   									getTestResponder(testLegalNameBadSearch_Step4));

   		}

		private function testLegalNameBadSearch_Step4(e:ResultEvent):void {
   			mSearchResults.source = ArrayCollection(e.result).toArray();

   			assertEquals("search result count", 0, mSearchResults.length);		
   		}

   		
   		
	 	//---------------------
	 	// companyId search
	 	//---------------------
	 	public static const INTUIT_COMPANY_ID:String = "1234567";   		
   		public function testCompanyIdSearch():void {
   			runDataLoader("Company :: Create Basic Data", testCompanyIdSearch_Step2, 10);
   		}
   		
   		private function testCompanyIdSearch_Step2(e:ResultEvent):void {
   			login(testCompanyIdSearch_Step3);
   		}
   			
   		private function testCompanyIdSearch_Step3(e:ResultEvent):void {
   			mCompanyService.search(	CompanySearchEnum.SEARCH_BY_PSID, INTUIT_COMPANY_ID, 
   									getTestResponder(testCompanyIdSearch_Step4));   			
   		}
		
		private function testCompanyIdSearch_Step4(e:ResultEvent):void {
   			mSearchResults.source = ArrayCollection(e.result).toArray();

   			assertEquals("search result count", 1, mSearchResults.length);
   			assertEquals("company id", INTUIT_COMPANY_ID, CompanySearchResult(mSearchResults.getItemAt(0)).key.companyId);			
		}
		
		//---------------------
	 	// companyId BAD search
	 	//---------------------
	 	public static const INTUIT_COMPANY_ID_BAD:String = "12345602918273627";   		
   		public function testCompanyIdSearchBad():void {
   			runDataLoader("Company :: Create Basic Data", testCompanyIdSearchBad_Step2, 5);
   		}			
   		
   		private function testCompanyIdSearchBad_Step2(e:ResultEvent):void {
   			login(testCompanyIdSearchBad_Step3);
   		}
   		
   		private function testCompanyIdSearchBad_Step3(e:ResultEvent):void {
   			mCompanyService.search(	CompanySearchEnum.SEARCH_BY_PSID, INTUIT_COMPANY_ID_BAD,
   									getTestResponder(testCompanyIdSearchBad_Step4, 5));
   		}
		
		private function testCompanyIdSearchBad_Step4(e:ResultEvent):void {			
   			mSearchResults.source = ArrayCollection(e.result).toArray();

   			assertEquals("search result count", 0, mSearchResults.length);			
		}
		
	
				
	}
}