package test.sap.viewModel
{
    import flexunit.framework.TestSuite;

import mx.collections.ArrayCollection;
import mx.utils.ObjectUtil;

import psp.sap.model.Company;
import psp.sap.model.FraudEvent;
import psp.sap.model.SourceSystemEnum;
    import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.FraudDetailViewModel;

    import test.mock.MockCompanyService;
import test.mock.MockPayrollRunService;
import test.mock.data.CompanyData;
import test.mock.data.PayrollData;

public class FraudDetailViewModelTest extends AbstractPartViewModelTestBase
	{
        private var mViewModel:FraudDetailViewModel = new FraudDetailViewModel();
		private var mDataService:MockCompanyService;
        private var mPayrollRunSevice:MockPayrollRunService;
        private var mCompany:Company;

		public function FraudDetailViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( FraudDetailViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"companyFraudulentPayrolls",
				"companyLegalName",
				"fraudulentPayrollsCollection",
				"selectedPayrollFraudEvent",
				"showPayrollTransactions",
				"canSave"
			];

			mDataService = mSAP.companyService as MockCompanyService;
            mPayrollRunSevice = mSAP.payrollRunService as MockPayrollRunService;

            mCompany = new Company();
            mCompany.companyId = "564";
            mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
            mViewModel.company = mCompany;

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
            // setup load model data mock expectation
			mDataService.expectsFindCompany(mCompany.sourceSystemCd, mCompany.companyId).willReturnAsync(CompanyData.getCompany());
            mDataService.expectsFindCompanyFraudEvents("999999999", "Payroll", -1, null, null, null).willReturnAsync(CompanyData.getFraudEvents());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("company", 0, ObjectUtil.compare(mViewModel.company, CompanyData.getCompany()));
            assertEquals("events", 0, ObjectUtil.compare(mViewModel.companyFraudulentPayrolls, CompanyData.getFraudEvents()));            

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "companyFraudulentPayrolls", new ArrayCollection());
			testBindableProperty(mViewModel, "showPayrollTransactions", true);

            var payrollFraudEvent:FraudEvent = new FraudEvent();
            payrollFraudEvent.sourcePayRunId = "payrun158";
            mPayrollRunSevice.expectsCheckPayrollForSuspectPaychecks(SourceSystemEnum.QBDT.code, "4658", payrollFraudEvent.sourcePayRunId).willReturnAsync(PayrollData.getSuspectPaychecks());
            localTestBindableProperty(mViewModel, "selectedPayrollFraudEvent", payrollFraudEvent);

            testFraudEventNavigation();
		}

        private function testFraudEventNavigation():void {            
            mDataService.expectsFindCompany(SourceSystemEnum.QBDT.code, "4658").willReturnAsync(CompanyData.getCompany());
            // this property is bound to another page via binding utils this test assumes that binding is working
            mViewModel.fraudSearchResults = CompanyData.getFraudEvents();
            assertTrue(mDataService.errorMessage(), mDataService.success());

            assertEquals("selected fraud event", 0, mViewModel.fraudulentPayrollsCollection.selectedIndex);
            assertTrue("move to next fraud event", mViewModel.moveToNextFraudEvent());
            assertEquals("selected fraud event", 1, mViewModel.fraudulentPayrollsCollection.selectedIndex);

            assertTrue("move to previous fraud event", mViewModel.moveToPreviousFraudEvent());
            assertEquals("selected fraud event", 0, mViewModel.fraudulentPayrollsCollection.selectedIndex);

            mViewModel.nextRecord();
            assertEquals("selected fraud event", 1, mViewModel.fraudulentPayrollsCollection.selectedIndex);

            mViewModel.previousRecord();
            assertEquals("selected fraud event", 0, mViewModel.fraudulentPayrollsCollection.selectedIndex);

            assertTrue(mDataService.errorMessage(), mDataService.success());
        }

        private function localTestBindableProperty(viewModel:Object, propertyName:String, newValue:Object):void {
   			viewModel[propertyName] = newValue;
   			assertPropertyChangeEventHistory([{property:propertyName, newValue:newValue}], true);
   		}

	}
}
