package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.model.Company;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyFundingModelHistoryViewModel;
	
	import test.mock.MockCompanyService;
import test.mock.MockPropertyAuditService;
import test.mock.MockRepository;
import test.mock.data.CompanyData;

public class CompanyFundingModelHistoryViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:CompanyFundingModelHistoryViewModel = new CompanyFundingModelHistoryViewModel();
		private var mDataService:MockPropertyAuditService;
		
		private var mCompany:Company;

		public function CompanyFundingModelHistoryViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( CompanyFundingModelHistoryViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"propertyHistory",
				"canSave"
			];
			
			mDataService = mSAP.propertyAuditService as MockPropertyAuditService;
			
			// setup model
			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompany.companyId = "123456";
			mViewModel.company = mCompany;

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mDataService.expectsGetFundingModelHistory(mCompany.companyId, mCompany.sourceSystemCd, null).willReturnAsync(CompanyData.getPropertyAuditCollection());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("propertyHistory",
				(MockRepository.instance.getTestObject(MockRepository.TEST_PROPERTY_AUDIT) as ArrayCollection).length,
				mViewModel.propertyHistory.length);

			testBindableProperties();
		}		

	}
}
