package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.application.enums.ViewModelActivationStateEnum;
	import psp.sap.model.MoneyMovementTransaction;
	import psp.sap.model.PayrollACHDetailSet;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.PayrollACHDetailViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockPayrollRunService;
	import test.mock.MockRepository;
import test.mock.data.PayrollData;

public class PayrollACHDetailViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollACHDetailViewModel = new PayrollACHDetailViewModel();
		private var mDataService:MockPayrollRunService;
		
		private var mMMT:MoneyMovementTransaction;
		private var mockData:PayrollACHDetailSet = MockRepository.instance.getTestObject(MockRepository.TEST_PAYROLL_ACH_TRANSACTION_DETAIL);

		public function PayrollACHDetailViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollACHDetailViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"taxCreditTransactionsTotal",
				"selectedTransaction",
				"taxTransactions",
				"taxCreditTransactions",
				"transactions",
				"showDetail",
				"ddTransactionsTotal",
				"allTaxTransactionsTotal",
				"feeTransactions",
				"taxTransactionsTotal",
				"ddTransactions",
				"compare",
				"feeTransactionsTotal",
				"canSave"
			];

			mDataService = mSAP.payrollRunService as MockPayrollRunService;
			
			// setup expected selected transaction
			mMMT = new MoneyMovementTransaction();
			mMMT.spcfId = "123456";
			mMMT.showDetail = true;
			mViewModel.selectedTransaction = mMMT;
			
			var temp:ArrayCollection = new ArrayCollection();
			temp.addItem(mMMT);
			mViewModel.transactions = temp;			

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mDataService.expectsFindAchDetailTransactions(mMMT.spcfId,"123456789").willReturnAsync(PayrollData.getMoneyMovementTransactionDetail());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("ddTransactions",
				mockData.ddTransactions.length,
				mViewModel.ddTransactions.length);
			assertEquals("ddTransactionsTotal",
				mockData.ddTransactionsTotal,
				mViewModel.ddTransactionsTotal);
			
			assertEquals("feeTransactions",
				mockData.feeTransactions.length,
				mViewModel.feeTransactions.length);
			assertEquals("feeTransactionsTotal",
				mockData.feeTransactionsTotal,
				mViewModel.feeTransactionsTotal);
				
			assertEquals("taxCreditTransactions",
				mockData.taxCreditTransactions.length,
				mViewModel.taxCreditTransactions.length);
			assertEquals("taxCreditTransactionsTotal",
				mockData.taxCreditTransactionsTotal,
				mViewModel.taxCreditTransactionsTotal);
				
			assertEquals("taxTransactions",
				mockData.taxTransactions.length,
				mViewModel.taxTransactions.length);
			assertEquals("taxTransactionsTotal",
				mockData.taxTransactionsTotal,
				mViewModel.taxTransactionsTotal);
				
			assertEquals("allTaxTransactionsTotal",
				mockData.taxTransactionsTotal + mockData.taxCreditTransactionsTotal,
				mViewModel.allTaxTransactionsTotal);			

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();
			 
			testBindableProperty(mViewModel, "selectedTransaction", new MoneyMovementTransaction()); 
			testBindableProperty(mViewModel, "taxTransactions", new ArrayCollection);
			testBindableProperty(mViewModel, "taxTransactionsTotal", 5);  
			testBindableProperty(mViewModel, "taxCreditTransactions", new ArrayCollection());
			testBindableProperty(mViewModel, "taxCreditTransactionsTotal", 5);						
			testBindableProperty(mViewModel, "transactions", new ArrayCollection()); 						
			testBindableProperty(mViewModel, "ddTransactionsTotal", 5);
			testBindableProperty(mViewModel, "ddTransactions", new ArrayCollection());  
			testBindableProperty(mViewModel, "allTaxTransactionsTotal", 10);			 
			testBindableProperty(mViewModel, "compare", true); 
			testBindableProperty(mViewModel, "feeTransactionsTotal", 5);
			testBindableProperty(mViewModel, "feeTransactions", new ArrayCollection());
			
			testShowDetail();
		}
		
		private function testShowDetail():void {									
			mDataService.expectsFindAchDetailTransactions(mMMT.spcfId,"123456789").willReturnAsync(PayrollData.getMoneyMovementTransactionDetail());
			addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyShowDetail);
   			mViewModel.showDetail = true;
		}
		
		private function verifyShowDetail(e:ViewModelEvent):void {			
   			assertEventHistory([ViewModelEvent.MODEL_DATA_LOADED,
   								ViewModelEvent.MODEL_DATA_SETUP_COMPLETED,
   								ViewModelEvent.ACTIVATED]);
   								
   			assertEquals("activation state", ViewModelActivationStateEnum.ACTIVATED, mViewModel.activationState);
   			
   			assertTrue(mDataService.errorMessage(), mDataService.success());
   								
   			assertEquals("show detail", true, mViewModel.showDetail);
   			   			
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifySelectedTransactionChanged);
			
			var mmt:MoneyMovementTransaction = new MoneyMovementTransaction();
			mmt.showDetail = true;
			mmt.spcfId = "123";
			mDataService.expectsFindAchDetailTransactions(mmt.spcfId,"123456789").willReturnAsync(PayrollData.getMoneyMovementTransactionDetail());
			mViewModel.selectedTransaction = mmt;
   		}
   		
   		private function verifySelectedTransactionChanged(e:ViewModelEvent):void {			
   			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);   								
   			
   			assertTrue(mDataService.errorMessage(), mDataService.success());
   		}

	}
}
