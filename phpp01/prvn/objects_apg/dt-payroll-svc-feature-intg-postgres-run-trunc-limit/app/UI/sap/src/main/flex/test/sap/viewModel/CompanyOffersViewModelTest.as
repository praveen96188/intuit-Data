package test.sap.viewModel
{
	import flexunit.framework.TestSuite;

    import mx.collections.ArrayCollection;

    import psp.sap.model.Company;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.Offer;
    import psp.sap.model.Offering;
    import psp.sap.model.ServiceCodeEnum;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyOffersViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;

    import test.mock.MockBillingService;
    import test.mock.MockCompanyService;
    import test.mock.data.BillingData;
    import test.mock.data.CompanyData;

    public class CompanyOffersViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:CompanyOffersViewModel = new CompanyOffersViewModel();
		private var mCompanyService:MockCompanyService;
        private var mBillingService:MockBillingService;
        private var mCompanyKey:CompanyKey;
        private var mCompany:Company;

		public function CompanyOffersViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( CompanyOffersViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"canAddOffer",
				"canUpdate",
				"claimedExpirationDate",
				"companyOffers",
				"expirationDate",
				"hasCurrentOffer",
				"offers",
				"selectedOffer",
				"showClaimedExpirationDate",
				"showExpirationDate",
				"canSave"
			];

			mCompanyService = mSAP.companyService as MockCompanyService;
            mBillingService = mSAP.billingService as MockBillingService;

            mCompanyKey = new CompanyKey();
            mCompanyKey.sourceSystemCd = SourceSystemEnum.QBDT.code;
            mCompanyKey.companyId = "1567";
            mViewModel.companyKey = mCompanyKey;            
            mViewModel.company = mCompany;


			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mCompanyService.expectsGetCompanyOffers(mCompanyKey.sourceSystemCd, mCompanyKey.companyId, ServiceCodeEnum.DIRECT_DEPOSIT.code).willReturnAsync(CompanyData.getCompanyOffers());
            mBillingService.expectsFindOffers(mCompanyKey.sourceSystemCd, mCompanyKey.companyId, ServiceCodeEnum.DIRECT_DEPOSIT.code).willReturnAsync(BillingData.getOffers());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mCompanyService.errorMessage(), mCompanyService.success());
            assertTrue(mBillingService.errorMessage(), mBillingService.success());

			assertTrue("hasCurrentOffer", mViewModel.hasCurrentOffer);
            assertTrue("showClaimedExpirationDate", mViewModel.showClaimedExpirationDate);

            assertEquals("companyOffers", CompanyData.getCompanyOffers().length, mViewModel.companyOffers.length);
            assertEquals("offers", BillingData.getOffers().length, mViewModel.offers.length);
            assertNull("selectedOffer", mViewModel.selectedOffer);

			// can save start false
			assertFalse("can save", mViewModel.canSave);

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "canUpdate", true);
			testBindableProperty(mViewModel, "claimedExpirationDate", "blah");
			testBindableProperty(mViewModel, "companyOffers", new ArrayCollection());
			testBindableProperty(mViewModel, "expirationDate", "blah");
			testBindableProperty(mViewModel, "hasCurrentOffer", false);
			testBindableProperty(mViewModel, "offers", new ArrayCollection());
			testBindableProperty(mViewModel, "selectedOffer", new Offer());
			testBindableProperty(mViewModel, "showClaimedExpirationDate", false);
			testBindableProperty(mViewModel, "showExpirationDate", true);

			//verifyHasChangedLogic();
		}

		override protected function verifyHasChangedLogic():void {
			// the generation assumes that all of the bindable properties are tied to has changed
			// make adjustments where needed.
			// todo add a valid values
			testHasChanged(mViewModel, "canAddOffer", null);
			testHasChanged(mViewModel, "canUpdate", null);
			testHasChangedDTO(mViewModel, mViewModel.claimedExpirationDate);
			testHasChangedDTO(mViewModel, mViewModel.companyOffers);
			testHasChangedDTO(mViewModel, mViewModel.expirationDate);
			testHasChanged(mViewModel, "hasCurrentOffer", null);
			testHasChanged(mViewModel, "offers", null);
			testHasChanged(mViewModel, "selectedOffer", null);
			testHasChangedDTO(mViewModel, mViewModel.showClaimedExpirationDate);
			testHasChangedDTO(mViewModel, mViewModel.showExpirationDate);

			testValidators();
		}

		override protected function testValidators():void {
			// update has changed so can save is true
			// todo make cansave true
			// ex. mViewModel.numDaysVerifyLimits = "364";

			assertTrue("can save", mViewModel.canSave);

			// todo test each validator. Test generation tries to guess how to
			// test each validator. It is the testers responsibility make sure the
			// validators are being tested correctly
			// todo add min and max numbers and dto object if needed
			testDateValidator(mViewModel, "expirationDate", NaN, NaN);

			testSave();
		}

		override protected function testSave():void {
			assertTrue("can save", mViewModel.canSave);

			// todo setup expected service save method ex. mDataService.expectsSaveDirectDepositLimitSettings(mViewModel.directDepositLimitSettings, SourceSystemEnum.QBDT.code);
			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);
			// todo only needed if data is reloaded after save addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);

			mViewModel.save();
		}

		override protected function verifySave(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);

			// todo if your page has a refresh remove the next line the mock services will be checked in the refresh method
			//assertTrue(mDataService.errorMessage(), mDataService.success());
			// todo if only the backing properties are reset and no data is reloaded
			// test the reset backing properties here
		}

		override protected function verifyRefresh(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);
			//assertTrue(mDataService.errorMessage(), mDataService.success());

			// todo verify expected properties are set or reset
			// ie assertFalse("can save", mViewModel.canSave);
			/* or assertEquals("numDaysVerifyLimits",
			   mDataService.directDepositLimitSettingsMockData.companyBankAccountDurationLimitForVerification,
			   mViewModel.numDaysVerifyLimits);*/
		}

	}
}
