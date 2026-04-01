package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import psp.sap.model.Company;
	import psp.sap.model.PayrollRun;
	import psp.sap.model.SettlementTypeEnum;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
	import psp.sap.viewmodel.PayrollLedgerRecoverBadDebtViewModel;
	
	import test.mock.MockPayrollRunService;

	public class PayrollLedgerRecoverBadDebtViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollLedgerRecoverBadDebtViewModel = new PayrollLedgerRecoverBadDebtViewModel();
		private var mDataService:MockPayrollRunService;
		
		private var mCompany:Company;
		private var mPayrollRun:PayrollRun;

		public function PayrollLedgerRecoverBadDebtViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollLedgerRecoverBadDebtViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"canSave"
			];

			mDataService = mSAP.payrollRunService as MockPayrollRunService;
			
			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompany.companyId = "12345";			
			mViewModel.company = mCompany;
			
			mPayrollRun = new PayrollRun();
			mPayrollRun.sourcePayRunId = "6789";
			
			mViewModel.settlementTypes = SettlementTypeEnum.non_ach_values;
			mViewModel.defaultSettlementType = SettlementTypeEnum.WIRE;

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mDataService.expectsFindPayrollUncollectedBalances(mCompany.sourceSystemCd, mCompany.companyId, mPayrollRun.sourcePayRunId).willReturnAsync(PayrollData.getBillingTransactions());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("payrolls",
				(MockRepository.instance.getTestObject(MockRepository.TEST_COMPANY_PAYROLLS) as ArrayCollection).length,
				mViewModel.payrolls.length);

			// todo also test if backing properties were initilized properly

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();


			verifyHasChangedLogic();
		}

		override protected function verifyHasChangedLogic():void {
			// the generation assumes that all of the bindable properties are tied to has changed
			// make adjustments where needed.

			testValidators();
		}

		override protected function testValidators():void {
			// update has changed so can save is true
			// todo make cansave true
			// ex. mViewModel.numDaysVerifyLimits = "364";

			assertEquals("can save", true, mViewModel.canSave);

			// todo test each validator. Test generation trys to guess how to
			// test each validator. It is the testers responsibility make sure the
			// validators are being tested correctly
			testNumberValidator(mViewModel, "totalPayment", NaN, NaN); // todo add min and max numbers
			testRequiredStringValidator(mViewModel, "totalPayment"); // todo make sure this test is correct

			testSave();
		}

		override protected function testSave():void {
			assertEquals("can save", true, mViewModel.canSave);

			// todo setup expected service save method ex. mDataService.expects("saveDirectDepositLimitSettings").withArgs(mViewModel.directDepositLimitSettings, SourceSystemEnum.QBDT.code);
			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);
			// todo only needed if data is reloaded after save addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);

			mViewModel.save();
		}

		override protected function verifySave(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);

			assertTrue(mDataService.errorMessage(), mDataService.success());
			// todo if only the backing properties are reset and no data is reloaded
			// test the reset backing properties here
		}

		override protected function verifyRefresh(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);

			// todo verify expected properties are set or reset
			// ie assertTrue("can save", false, mViewModel.canSave);
			/* or assertEquals("numDaysVerifyLimits",
			   mDataService.directDepositLimitSettingsMockData.companyBankAccountDurationLimitForVerification,
			   mViewModel.numDaysVerifyLimits);*/
		}

	}
}
