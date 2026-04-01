package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.events.PropertyChangeEvent;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.model.BankReturn;
	import psp.sap.viewmodel.BankReturnsSearchViewModel;
	
	import test.sap.application.SAPTestBase;
	
	public class BankReturnsSearchExplorerViewModelTest extends SAPTestBase
	{				
		private const COMPANY_EIN:String = "222222223";
		
		public function BankReturnsSearchExplorerViewModelTest(methodName:String=null)
		{
			super(methodName);
		}
		
  		public static function suite():TestSuite {
   			return new TestSuite( BankReturnsSearchExplorerViewModelTest );
   		}
   		
   		private var mViewModel:BankReturnsSearchViewModel;

   		
   		/**                 
        * START
        */
        public function testBankReturnsSearch():void {
        	runDataLoader("Payroll :: Add Redebit Test", testBankReturnsSearch_Step2, 10);
        }
        
        private function testBankReturnsSearch_Step2(e:ResultEvent):void {
        	login(testBankReturnsSearch_validationTests);
        }
 
 
 /*@TODO:Hookup */
        
        
        /**
        * Test validators and bindings of input fields
        */ 
        

        private function testBankReturnsSearch_validationTests(e:ResultEvent):void {
        	// setup
        	mViewModel = new BankReturnsSearchViewModel();
        	mViewModel.selectedSearchViewModel.resetToDefaults();
        	
        	// check can search * defaults should be valid * default ein is empty 
        	mViewModel.selectedSearchViewModel.resetToDefaults();
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, true);
		    
		    // check ein validation and binding
		    // invalid on first key entry
		    mViewModel.selectedSearchViewModel.ein = "9";
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, false);
		    mViewModel.selectedSearchViewModel.ein = "";
		    
		    // non digits is invalid
		    mViewModel.selectedSearchViewModel.ein = "a";
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, false);
		    mViewModel.selectedSearchViewModel.ein = "";
		    
		    // 9 digits is valid
		    mViewModel.selectedSearchViewModel.ein = "999999999";
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, true);
		    mViewModel.selectedSearchViewModel.ein = "";
		    
		    // 9 digits with dash is valid
		    mViewModel.selectedSearchViewModel.ein = "99-9999999";
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, true);
		    mViewModel.selectedSearchViewModel.ein = "";
		    		    		    
		    // check date validation * default is two empty dates
		    // default is valid
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, true);
		    
		    // invalid start date entry
		    mViewModel.selectedSearchViewModel.startDate = "abc";
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, false);
		    mViewModel.selectedSearchViewModel.startDate = "";
		    
		    // invalid end date entry
		    mViewModel.selectedSearchViewModel.endDate = "abc";		    
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, false);
		    mViewModel.selectedSearchViewModel.endDate = "";
		    
		    // start date with no end date is valid
		    mViewModel.selectedSearchViewModel.startDate = "06/10/2008";
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, true);
		    mViewModel.selectedSearchViewModel.startDate = "";
		    
		    // end date with no start date is valid		   
		    mViewModel.selectedSearchViewModel.endDate = "06/10/2008";
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, true);
		    mViewModel.selectedSearchViewModel.endDate = "";
		    
		    // start date == end date is valid
		   	mViewModel.selectedSearchViewModel.startDate = "06/10/2008";
		   	mViewModel.selectedSearchViewModel.endDate = "06/10/2008";
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, true);
		    mViewModel.selectedSearchViewModel.startDate = "";
		    mViewModel.selectedSearchViewModel.endDate = "";
		    
		    // start date < end date is valid
		   	mViewModel.selectedSearchViewModel.startDate = "06/10/2008";
		   	mViewModel.selectedSearchViewModel.endDate = "06/11/2008";
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, true);
		    mViewModel.selectedSearchViewModel.startDate = "";
		    mViewModel.selectedSearchViewModel.endDate = "";
		    
		   	// start date > end date is invalid
		   	mViewModel.selectedSearchViewModel.startDate = "06/11/2008";
		   	mViewModel.selectedSearchViewModel.endDate = "06/10/2008";
		    assertEquals("Can search", mViewModel.selectedSearchViewModel.canSearch, false);
		    mViewModel.selectedSearchViewModel.startDate = "";
		    mViewModel.selectedSearchViewModel.endDate = "";
		    
		    // preform search for next tests
		    mViewModel.selectedSearchViewModel.ein = COMPANY_EIN;
		 	
		 	//Make it 15 seconds
		 	this.asyncTimeout = 15000;
		 	
		    addAsyncVerifier(mViewModel.selectedSearchViewModel, PropertyChangeEvent.PROPERTY_CHANGE, testBankReturnsSearch_searchResults);
		    mViewModel.selectedSearchViewModel.coreSearchBankReturns(false);
		}			

        private function testBankReturnsSearch_searchResults(e:PropertyChangeEvent):void {
        	// when the search is done            
            if(e.property == "isSearching" && !mViewModel.selectedSearchViewModel.isSearching){
	            // check results
				assertTrue(mViewModel.selectedSearchViewModel.searchResults != null);
				
				// with the data loader there should be only one result
				// check the display indices				
	        	assertEquals("Start Index", mViewModel.selectedSearchViewModel.searchResults.startIndex, 0);
	        	assertEquals("Last Index", mViewModel.selectedSearchViewModel.searchResults.lastIndex, 1);
	        	assertEquals("Total Records", mViewModel.selectedSearchViewModel.searchResults.totalRecords, 1);
	        	
	        	// check result values
	        	assertTrue(mViewModel.selectedSearchViewModel.searchResults.getItemAt(0) is BankReturn);
	        	var bankReturn:BankReturn = mViewModel.selectedSearchViewModel.searchResults.getItemAt(0) as BankReturn;
	        	assertTrue(nothingNull(bankReturn));	        	
            }
        } 
        

        
        /** END **/
	}
}
