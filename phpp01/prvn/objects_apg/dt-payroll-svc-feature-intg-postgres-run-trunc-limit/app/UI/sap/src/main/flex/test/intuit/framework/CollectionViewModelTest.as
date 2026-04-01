package test.intuit.framework
{
	import mx.collections.ArrayCollection;
	import flexunit.framework.TestCase;
	import flexunit.framework.TestSuite;

	import psp.sap.model.Company;
	import intuit.sbd.flex.framework.viewmodel.CollectionViewModel;	

	import test.data.MockCompanyService;
	


	/**
	 * Simple FlexUnit test example.
	 */
	public class CollectionViewModelTest extends TestCase
	{
		public function CollectionViewModelTest() {

		}
		
        public static function suite() : TestSuite {
			return new TestSuite( CollectionViewModelTest );            
        }
        
        private var mCompanyList: CollectionViewModel;
        
        override public function setUp() : void {
        	var companies: ArrayCollection = MockCompanyService.DB;
        	mCompanyList = new CollectionViewModel(Company, companies.toArray());
        }
        
        public function testEmptyList() : void {
     	
        	var companyList: CollectionViewModel = new CollectionViewModel(Company,[]);
        	
        	assertTrue(companyList.isEmpty);
        	assertFalse(companyList.canSelectPrevious);
        	assertFalse(companyList.canSelectNext);
        	assertTrue(companyList.isFirst);
        	assertTrue(companyList.isLast);
        	
        	assertTrue(companyList.selectedIndex == -1);
        	assertTrue(companyList.selectedItem == null);
        	
        	assertTrue(companyList.selectNext() == -1);
        	assertTrue(companyList.selectPrevious() == -1);
        	assertTrue(companyList.selectFirst() == -1);
        	assertTrue(companyList.selectLast() == -1);
        	
        	// try to select a company not in the list
        	companyList.selectedItem = new Company();
        	assertTrue(companyList.selectedIndex == -1);
        	assertTrue(companyList.selectedItem == null);
        	
        	// try to select an index
        	companyList.selectedIndex = 0;
        	assertTrue(companyList.selectedIndex == -1);
        }
        
        public function testSelectCompanyByCompany() : void {
        	mCompanyList.selectFirst();
        	assertTrue(mCompanyList.selectedIndex == 0);
        	
        	// get the last company in the list
        	var companyToSelect: Company = 
        		mCompanyList.getItemAt(mCompanyList.length - 1) as Company;
        	
        	mCompanyList.selectedItem = companyToSelect;
        	
        	assertTrue(mCompanyList.selectedItem == companyToSelect);
        	assertTrue(mCompanyList.selectedIndex == mCompanyList.length - 1);
        	assertTrue(mCompanyList.isLast);  
        	assertFalse(mCompanyList.canSelectNext);
        	
        	// try to select a company not in the list
        	var newCompany: Company = new Company();
        	newCompany.fein = "99-234234";
        	var selectedIndex: int = mCompanyList.selectedIndex;
        	mCompanyList.selectedItem = newCompany;
        	assertTrue(mCompanyList.selectedItem == companyToSelect);
        	assertTrue(mCompanyList.selectedIndex == selectedIndex);
        }
        
        public function testSelectNext() : void {
        	var lastIndex: int = mCompanyList.length - 1;
        	
        	mCompanyList.selectFirst();
        	for (var i: int = 0; i < lastIndex; i++) {
        		assertTrue(mCompanyList.canSelectNext);
        		mCompanyList.selectNext();
        	}
        	
        	assertFalse(mCompanyList.canSelectNext);
        	assertTrue(mCompanyList.isLast);
        	assertTrue(mCompanyList.selectedIndex == lastIndex);
        }
        
        
        
	}
}