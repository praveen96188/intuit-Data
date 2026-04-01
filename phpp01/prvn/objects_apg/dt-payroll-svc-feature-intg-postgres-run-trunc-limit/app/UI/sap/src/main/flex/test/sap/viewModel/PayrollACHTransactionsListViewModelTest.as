package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.model.Company;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.PayrollACHTransactionsListViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockPayrollRunService;
	import test.mock.MockRepository;
import test.mock.data.PayrollData;


public class PayrollACHTransactionsListViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollACHTransactionsListViewModel;
		private var mDataService:MockPayrollRunService;
		
		private var mCompany:Company;

		public function PayrollACHTransactionsListViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollACHTransactionsListViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

            mSAP.setPSPDate(new Date("02/23/2008").getTime());

            mViewModel = new PayrollACHTransactionsListViewModel();

			trackedProperties = [
				"transactions",
				"hasTransactions",
				"showEntireHistory"
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
			mDataService.expectsFindMoneyMovementTransactions(mCompany.sourceSystemCd, mCompany.companyId, new Date("02/23/2007")).willReturnAsync(PayrollData.getMoneyMovementTransactions());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("transactions",
				(MockRepository.instance.getTestObject(MockRepository.TEST_PAYROLL_ACH_TRANSACTIONS) as ArrayCollection).length,
				mViewModel.transactions.length);
			
			assertEquals("hasTransactions",
				true,
				mViewModel.hasTransactions);
				
			assertEquals("showEntireHistory",
				false,
				mViewModel.showEntireHistory);

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "transactions", new ArrayCollection()); 
			testBindableProperty(mViewModel, "hasTransactions", false); 			
			
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);

            mDataService.expectsFindMoneyMovementTransactions(mCompany.sourceSystemCd, mCompany.companyId, null).willReturnAsync(PayrollData.getMoneyMovementTransactions());            
			mViewModel.showEntireHistory = true;		
		}
		
		override protected function verifyRefresh(e:ViewModelEvent):void {
            assertTrue(mDataService.errorMessage(), mDataService.success());
   			
            assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);
   			
   			assertEquals("transactions",
				(MockRepository.instance.getTestObject(MockRepository.TEST_PAYROLL_ACH_TRANSACTIONS) as ArrayCollection).length,
				mViewModel.transactions.length);
			
			assertEquals("hasTransactions",
				true,
				mViewModel.hasTransactions);
				
			assertEquals("showEntireHistory",
				true,
				mViewModel.showEntireHistory);
  		}

	}
}
