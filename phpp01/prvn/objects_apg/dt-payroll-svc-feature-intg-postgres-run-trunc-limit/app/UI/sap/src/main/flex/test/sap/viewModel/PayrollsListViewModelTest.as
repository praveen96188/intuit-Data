package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;

    import psp.sap.application.SAP;
    import psp.sap.model.Company;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.PayrollsListViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockPayrollRunService;
	import test.mock.MockRepository;
import test.mock.data.PayrollData;
    import test.mock.data.UserData;

    public class PayrollsListViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollsListViewModel;
		private var mDataService:MockPayrollRunService;
		
		private var mCompany:Company;

		public function PayrollsListViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollsListViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

            mSAP.session.user = UserData.getSuperDuperUser();

            mSAP.setPSPDate(new Date("2009/03/12").getTime());
            mViewModel = new PayrollsListViewModel();

			trackedProperties = [
				"payrolls",
				"hasPayrolls",
				"showEntireHistory",
				"canSave"
			];

			mDataService = mSAP.payrollRunService as MockPayrollRunService;
			
			// company used when getting payrolls
			mCompany = new Company();
			mCompany.companyId = "123456";
			mCompany.sourceSystemCd = SourceSystemEnum.QBOE.code;
			mViewModel.company = mCompany;						

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation

			mDataService.expectsFindPayrollRunsByDate(mCompany.companyId, mCompany.sourceSystemCd, new ArrayCollection(["Regular", "CloudOnly"]), new Date("2008/03/12"), new Date("2009/03/12")).willReturnAsync(PayrollData.getCompanyPayrolls());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("payrolls",
				(MockRepository.instance.getTestObject(MockRepository.TEST_COMPANY_PAYROLLS) as ArrayCollection).length,
				mViewModel.payrolls.length);
			
			assertEquals("hasPayrolls",
				true,
				mViewModel.hasPayrolls);
				
			assertEquals("showEntireHistory",
				false,
				mViewModel.showEntireHistory);							


			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "payrolls", new ArrayCollection());
			testBindableProperty(mViewModel, "hasPayrolls", false);
						
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);	

            mDataService.expectsFindPayrollRunsByDate(mCompany.companyId, mCompany.sourceSystemCd, new ArrayCollection(["Regular", "CloudOnly"]), null, null).willReturnAsync(PayrollData.getCompanyPayrolls());
            mViewModel.showEntireHistory = true;
		}
		
		override protected function verifyRefresh(e:ViewModelEvent):void {
   			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);
   			
   			assertEquals("payrolls",
				(MockRepository.instance.getTestObject(MockRepository.TEST_COMPANY_PAYROLLS) as ArrayCollection).length,
				mViewModel.payrolls.length);
			
			assertEquals("hasPayrolls",
				true,
				mViewModel.hasPayrolls);
				
			assertEquals("showEntireHistory",
				true,
				mViewModel.showEntireHistory);
  		}

	}
}
