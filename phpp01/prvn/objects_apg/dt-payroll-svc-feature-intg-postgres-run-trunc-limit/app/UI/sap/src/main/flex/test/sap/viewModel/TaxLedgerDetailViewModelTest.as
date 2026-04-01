package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.model.Company;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.TaxLedgerDetailViewModel;
	
	import test.mock.MockTaxService;

	// todo add import statements
	public class TaxLedgerDetailViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:TaxLedgerDetailViewModel = new TaxLedgerDetailViewModel();
		private var mDataService:MockTaxService;
		
		private var mCompany:Company;

		public function TaxLedgerDetailViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( TaxLedgerDetailViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"employeeLedgerItems",
				"isQTD",
				"isYTD",
				"payrollDate",
				"canSave"
			];

			mDataService = mSAP.taxService as MockTaxService;
			
			mCompany = new Company();
			mCompany.companyId = "1567";
			mCompany.sourceSystemCd = SourceSystemEnum.GEMINI.code;
			mViewModel.company = mCompany;
			
			mViewModel.payrollRunId = "1357";
			mViewModel.voidId = "123"
			mViewModel.templateCd = "code";
			mViewModel.lawId = "57";

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
//			mDataService.expects("findEmployeeLedgerItems").withArgs(mCompany.sourceSystemCd, mCompany.companyId, "1357", "123", "code", "57", false, false);
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("employeeLedgerItems",
			100,
			mViewModel.employeeLedgerItems.length);			

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "employeeLedgerItems", new ArrayCollection());
			testBindableProperty(mViewModel, "isQTD", true);
			testBindableProperty(mViewModel, "isYTD", true);
			testBindableProperty(mViewModel, "payrollDate", new Date());

		}

	}
}
