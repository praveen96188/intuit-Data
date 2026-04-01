package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import psp.sap.application.collections.PaginationCollection;
	import psp.sap.model.Agency;
	import psp.sap.model.PaymentTemplate;
	import psp.sap.model.TaxPaymentStatus;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.GlobalPaymentsExecutedTabViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockTaxService;

	// todo add import statements
	public class GlobalPaymentsExecutedTabViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:GlobalPaymentsExecutedTabViewModel = new GlobalPaymentsExecutedTabViewModel();
		private var mDataService:MockTaxService;

		public function GlobalPaymentsExecutedTabViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( GlobalPaymentsExecutedTabViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"agencyList",
				"dateFormatter",
				"payDateValidator",
				"payEndDate",
				"payEndDateValidator",
				"payStartDate",
				"payStartDateValidator",
				"paymentDueDateValidator",
				"paymentDueEndDate",
				"paymentDueEndDateValidator",
				"paymentDueStartDate",
				"paymentDueStartDateValidator",
				"searchResults",
				"selectedTaxAgency",
				"selectedTaxPaymentStatus",
				"selectedTaxPaymentType",
				"showAllSelected",
				"canSave"
			];

			mDataService = mSAP.taxService as MockTaxService; // todo setup data service ex. mSAP.administrationService as MockAdministrationService;

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			//mDataService.expects("getExecutedTaxQueueItems").withArgs(null, null, null,  null, null, null, null,  null, false, 0, 25);
					
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			// todo also test if backing properties were initilized properly
			testLookupServiceLoaded();
			testBindableProperties();
		}

		protected function testLookupServiceLoaded():void {
			assertNotNull(getIRSFromLookup());
			assertNotNull(getIRS940PaymentType());
		}

		protected function getIRSFromLookup():Agency {
			return mSAP.lookupService.agencyList.getItemByKey("IRS") as Agency;
		}
		
		protected function getIRS940PaymentType():PaymentTemplate {
			var irsAgency:Agency = getIRSFromLookup();
			for(var i:int = 0; i < irsAgency.paymentTemplates.length; i++)
			{
				var pmtTemplate:PaymentTemplate = irsAgency.paymentTemplates.getItemAt(i) as PaymentTemplate;
				if(pmtTemplate.paymentTemplateCd == "IRS-940-PAYMENT")
					return pmtTemplate;
			}
			return null;
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();
					
			testBindableProperty(mViewModel, "payEndDate", new Date(2009, 12, 1)); // todo add a valid value here
			testBindableProperty(mViewModel, "payStartDate", new Date(2009, 1, 1)); // todo add a valid value here
			testBindableProperty(mViewModel, "paymentDueEndDate", new Date(2009, 12, 1)); // todo add a valid value here
			testBindableProperty(mViewModel, "paymentDueStartDate", new Date(2009, 1, 1)); // todo add a valid value here
			testBindableProperty(mViewModel, "searchResults", new PaginationCollection()); // todo add a valid value here
			testBindableProperty(mViewModel, "selectedTaxAgency", getIRSFromLookup()); // todo add a valid value here
			testBindableProperty(mViewModel, "selectedTaxPaymentStatus", TaxPaymentStatus.SUCCESSFUL_EXECUTED);
			testBindableProperty(mViewModel, "selectedTaxPaymentType", getIRS940PaymentType());
			testBindableProperty(mViewModel, "showAllSelected", false); 
			testValidators();
		}

		override protected function testValidators():void {
			assertEquals("can save", true, mViewModel.canSave);

			testSAPStartEndDateValidator(mViewModel, "payStartDate", "payEndDate"); // todo add min and max numbers
			testSAPStartEndDateValidator(mViewModel, "paymentDueStartDate", "paymentDueEndDate"); 

			assertEquals("can save", true, mViewModel.canSave);

			testSave();
		}

		override protected function testSave():void {
			assertEquals("can save", true, mViewModel.canSave);
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_LOADED, verifySave);		
			mViewModel.search();
		}

		override protected function verifySave(e:ViewModelEvent):void {
			//mDataService.expects("getExecutedTaxQueueItems").withArgs(null, null, null,  null, null, null, null,  null, false, 0, 25);
		}

		override protected function verifyRefresh(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);
		}
	}
}
