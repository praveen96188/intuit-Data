package test.sap.viewModel
{
	import flexunit.framework.TestSuite;

    import mx.collections.ArrayCollection;

    import psp.sap.model.CompanyKey;
    import psp.sap.model.DisplayStatus;
    import psp.sap.model.ServiceStatus;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyEditSubscriptionStatusViewModel;

    import test.mock.MockCompanyService;
    import test.mock.data.CompanyData;

    public class CompanyEditSubscriptionStatusViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:CompanyEditSubscriptionStatusViewModel = new CompanyEditSubscriptionStatusViewModel();
		private var mDataService:MockCompanyService;
        private var mCompanyKey:CompanyKey;

		public function CompanyEditSubscriptionStatusViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( CompanyEditSubscriptionStatusViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"isMultipleSubStatusSelectionAllowed",
				"currentStatus",
				"selectedServiceSubStatusesLabel",
				"selectedServiceStatus",
				"originalSelectedStatus",
				"companyIsOnHold",
				"canUpdateStatus",
				"serviceName",
				"fraudFlag",
				"serviceStatuses",
				"selectedServiceSubStatuses",
				"canSave"
			];

			mDataService = mSAP.companyService as MockCompanyService;
            mCompanyKey = new CompanyKey(SourceSystemEnum.QBDT.code, "123547");
            mViewModel.companyKey = mCompanyKey;

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mDataService.expectsGetCompanyStatus(mCompanyKey.sourceSystemCd, mCompanyKey.companyId, true, false).willReturnAsync(CompanyData.getCompanyStatus());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			var serviceStatus:ServiceStatus = new ServiceStatus();
            serviceStatus.serviceSubStatusList = new ArrayCollection();

			testBindableProperty(mViewModel, "isMultipleSubStatusSelectionAllowed", true);
			testBindableProperty(mViewModel, "currentStatus", new DisplayStatus());
			testBindableProperty(mViewModel, "selectedServiceStatus", serviceStatus);
			testBindableProperty(mViewModel, "originalSelectedStatus", serviceStatus);
			testBindableProperty(mViewModel, "companyIsOnHold", true);
			testBindableProperty(mViewModel, "fraudFlag", true);

		}

	}
}
