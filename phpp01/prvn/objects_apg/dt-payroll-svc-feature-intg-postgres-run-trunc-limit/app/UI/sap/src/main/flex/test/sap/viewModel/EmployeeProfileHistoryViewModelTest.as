package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.model.Company;
	import psp.sap.model.EmployeeInfo;
	import psp.sap.model.PropertyAudit;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.EmployeeProfileHistoryViewModel;
	
	import test.mock.MockEmployeeService;
	import test.mock.MockRepository;

	
	public class EmployeeProfileHistoryViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:EmployeeProfileHistoryViewModel = new EmployeeProfileHistoryViewModel();
		private var mDataService:MockEmployeeService;
		private var mEmployee:EmployeeInfo;
		
		private var mCompany:Company;
		
		public function EmployeeProfileHistoryViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( EmployeeProfileHistoryViewModelTest );
		}
		
		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"currentFilter",
				"filteredPropertyHistory",
				"hasEmployeeProfileHistory",
				"propertyAuditHistory",
				"propertyFilters",
				"employeeInfo"
			];

			mDataService = mSAP.employeeService as MockEmployeeService;
			
			// setup company
			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.GEMINI.code;
			mCompany.companyId = "123456";
			mViewModel.company = mCompany;
			
			mEmployee = new EmployeeInfo();
			mEmployee.employeeGseq = "112321312";
			mViewModel.employeeInfo = mEmployee;

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}
		
		override public function testViewModel():void {
			// setup load model data mock expectation			
			//mDataService.expects("getEmployeeProfileHistory").withArgs(mCompany.sourceSystemCd, mCompany.companyId, mEmployee.employeeGseq );
			testActivationSequence();

		}
		
		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("propertyAuditHistory",
				(MockRepository.instance.getTestObject(MockRepository.TEST_EMPLOYEE_PROFILE_HISTORY) as ArrayCollection).length,
				mViewModel.propertyAuditHistory.length);
				
			assertEquals("currentFilter","",mViewModel.currentFilter);
			
			assertEquals("filteredPropertyHistory",(MockRepository.instance.getTestObject(MockRepository.TEST_EMPLOYEE_PROFILE_HISTORY) as ArrayCollection).length,
				mViewModel.filteredPropertyHistory.length);
				
			assertEquals("hasEmployeeProfileHistory",true,mViewModel.hasEmployeeProfileHistory);
			
			assertEquals("propertyFilters",
				(MockRepository.instance.getTestObject(MockRepository.TEST_EMPLOYEE_PROFILE_HISTORY) as ArrayCollection).length + 1,
				mViewModel.propertyFilters.length);
	
			testBindableProperties();
		}
		
		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();
			
			testBindableProperty(mViewModel, "propertyAuditHistory", new ArrayCollection());
			testBindableProperty(mViewModel, "filteredPropertyHistory", new ArrayCollection());
			testBindableProperty(mViewModel, "propertyFilters", new ArrayCollection());
			testBindableProperty(mViewModel, "currentFilter", "apple");
			testBindableProperty(mViewModel, "hasEmployeeProfileHistory", false);
			
			mViewModel.currentFilter = "Prop Name0";
			
			testFilteredPropertyHistory();
	
		}
		
		private function testFilteredPropertyHistory():void
		{
			assertEquals("filteredPropertyHistoryLength", 1, mViewModel.filteredPropertyHistory.length);
			for each (var propertyAudit:PropertyAudit in mViewModel.filteredPropertyHistory)
			{
				assertEquals("propertyName", "Prop Name0",propertyAudit.propertyName) ;
			}
			
		}
		
	}
}