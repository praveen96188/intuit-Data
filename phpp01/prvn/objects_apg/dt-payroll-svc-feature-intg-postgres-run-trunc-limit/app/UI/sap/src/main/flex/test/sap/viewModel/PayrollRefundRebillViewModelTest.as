package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import psp.sap.model.Company;
	import psp.sap.model.FeeDetail;
	import psp.sap.model.PayrollRun;
	import psp.sap.model.PayrollTransaction;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
	import psp.sap.viewmodel.PayrollRefundRebillViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockBillingService;
	import test.mock.MockPayrollRunService;
	import test.mock.data.BillingData;

	public class PayrollRefundRebillViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollRefundRebillViewModel = new PayrollRefundRebillViewModel();
		private var mDataService:MockBillingService;
		private var mSaveService:MockPayrollRunService;
		private var mCompany:Company;
		private var mPayrollTransaction:PayrollTransaction;
		private var mPayrollRun:PayrollRun;

		public function PayrollRefundRebillViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollRefundRebillViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"feeDetail",
				"isRebill",
				"payrollTransaction",
				"rebillRefundTotal",
				"rebillTotalPrice",
				"rebillUnitPrice",
				"refundTotal",
				"canSave"
			];

			mDataService = mSAP.billingService as MockBillingService;
			mSaveService = mSAP.payrollRunService as MockPayrollRunService;
			
			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompany.companyId = "15678";
			mViewModel.company = mCompany;
				
			mPayrollRun = new PayrollRun();
			mPayrollRun.sourcePayRunId = "payrun1";

            mViewModel.payrollRun = mPayrollRun;            
								
			mPayrollTransaction = new PayrollTransaction();
			mPayrollTransaction.id = "txnId";
            mViewModel.payrollTransaction = mPayrollTransaction;
						

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mDataService.expectsFindFeeDetail(mPayrollTransaction.id,mCompany.companyId).willReturnAsync(BillingData.getFeeDetail());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			checkInitalData();

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();
			
			testBindableProperty(mViewModel, "feeDetail", new FeeDetail());			
			testBindableProperty(mViewModel, "isRebill", true);							
			testBindableProperty(mViewModel, "rebillRefundTotal", 100);
			testBindableProperty(mViewModel, "rebillTotalPrice", 100);			
			testBindableProperty(mViewModel, "rebillUnitPrice", "5.00");			
			testBindableProperty(mViewModel, "refundTotal", 100);

			verifyHasChangedLogic();
		}

		override protected function verifyHasChangedLogic():void {
			// hard coded to true
			testValidators();
		}

		override protected function testValidators():void {			
			mViewModel.isRebill = true;
			mViewModel.rebillUnitPrice = "5.00";
			assertEquals("rebillTotalPrice", 10, mViewModel.rebillTotalPrice);
			assertEquals("rebillRefundTotal", -10, mViewModel.rebillRefundTotal);			
			assertEquals("can save", true, mViewModel.canSave);
			
			testNumberValidator(mViewModel, "rebillUnitPrice", 0.01);
			
			// set unit price equal to the orginal price
			mViewModel.rebillUnitPrice = "10.00";
			assertEquals("rebillTotalPrice", 20, mViewModel.rebillTotalPrice);
			assertEquals("rebillRefundTotal", 0, mViewModel.rebillRefundTotal);
			assertEquals("can save", false, mViewModel.canSave);
			
			// uncheck rebill and check
			mViewModel.isRebill = false;
			assertEquals("refundTotal", 20, mViewModel.refundTotal);
			assertEquals("can save", true, mViewModel.canSave);
			
			// reset for save
			mViewModel.isRebill = true;
			mViewModel.rebillUnitPrice = "5.00";			
			assertEquals("rebillTotalPrice", 10, mViewModel.rebillTotalPrice);
			assertEquals("rebillRefundTotal", -10, mViewModel.rebillRefundTotal);
			
			testSave();
		}

		override protected function testSave():void {
			
			assertEquals("can save", true, mViewModel.canSave);

			mSaveService.expectsAddRefundRebillTransaction(mCompany.sourceSystemCd,
																		mCompany.companyId,
																		mPayrollRun.sourcePayRunId,
																		20.00,
																		mSAP.PSPDate,
																		true,
																		mPayrollTransaction.id,																		
																		5.00);
			mDataService.expectsFindFeeDetail(mPayrollTransaction.id,mCompany.companyId).willReturnAsync(BillingData.getFeeDetail());
			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);

			mViewModel.save();
		}

		override protected function verifySave(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);

			assertTrue(mSaveService.errorMessage(), mSaveService.success());			
		}

		override protected function verifyRefresh(e:ViewModelEvent):void {			
			assertTrue(mDataService.errorMessage(), mDataService.success());		
		}
		
		private function checkInitalData():void {
			assertEquals("rebillUnitPrice", BillingData.getFeeDetail().currentUnitPrice, mViewModel.rebillUnitPrice);
			assertEquals("refundTotal", BillingData.getFeeDetail().totalPrice, mViewModel.refundTotal);
			assertFalse("isRebill", mViewModel.isRebill);
			assertEquals("can save", true, mViewModel.canSave);
		}

	}
}
