package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.model.Company;
	import psp.sap.model.PaymentTemplate;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.model.TaxPaymentYear;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.TaxPaymentsSummaryViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockRepository;
	import test.mock.MockTaxService;

	public class TaxPaymentsSummaryViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:TaxPaymentsSummaryViewModel = new TaxPaymentsSummaryViewModel();
		private var mDataService:MockTaxService;
		
		private var mCompany:Company;

		public function TaxPaymentsSummaryViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( TaxPaymentsSummaryViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"taxPaymentYears",
				"canSave"
			];

			mDataService = mSAP.taxService as MockTaxService;
			
			// setup company
			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompany.companyId = "12345";
			mViewModel.company = mCompany;

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
//			mDataService.expects("getPaymentTemplateYears").withArgs(mCompany.sourceSystemCd, mCompany.companyId);
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("taxPaymentYears",
			(MockRepository.instance.getTestObject(MockRepository.TEST_TAX_PAYMENT_YEAR) as ArrayCollection).length,
			mViewModel.taxPaymentYears.length);

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "taxPaymentYears", new ArrayCollection());
						
			testGetTemplateYearPayment();
		}
		
		private function testGetTemplateYearPayment():void {
//			mDataService.expects("getTemplateYearPayment").withArgs(mCompany.sourceSystemCd, mCompany.companyId, "2014", "id_20140");
			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifyPaymentLoaded);
			mViewModel.getTemplateYearPayment(TaxPaymentYear(mViewModel.taxPaymentYears.getItemAt(0)), PaymentTemplate(TaxPaymentYear(mViewModel.taxPaymentYears.getItemAt(0)).paymentTemplates.getItemAt(0)));
		}
		
		private function verifyPaymentLoaded(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);

			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertNotNull("template year", PaymentTemplate(TaxPaymentYear(mViewModel.taxPaymentYears.getItemAt(mViewModel.taxPaymentYears.length-1)).paymentTemplates.getItemAt(0)).paymentTemplateYearPayment);
		}

	}
}
