package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.model.Company;
	import psp.sap.model.PayrollTransaction;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
	import psp.sap.viewmodel.PayrollTransactionHistoryViewModel;
	
	import test.mock.MockPayrollRunService;
	import test.mock.data.PayrollData;

	public class PayrollTransactionHistoryViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollTransactionHistoryViewModel = new PayrollTransactionHistoryViewModel();
		private var mDataService:MockPayrollRunService;
		private var mCompany:Company;
		private var mPayrollTransaction:PayrollTransaction;

		public function PayrollTransactionHistoryViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollTransactionHistoryViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"companyName",
				"payrollTransaction",
				"propertyAudit",
				"canSave"
			];

			mDataService = mSAP.payrollRunService as MockPayrollRunService;
			
			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompany.companyId = "1234";
			mCompany.DBA = "Happy Company";
			mViewModel.company = mCompany;
			
			mPayrollTransaction = new PayrollTransaction();
			mPayrollTransaction.id = "txnId123";

            mViewModel.payrollTransaction = mPayrollTransaction;            

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mDataService.expectsGetTransactionHistory(mCompany.sourceSystemCd, mCompany.companyId, mPayrollTransaction.id).willReturnAsync(PayrollData.getPayrollTransactionHistroy());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("propertyAudit", PayrollData.getPayrollTransactionHistroy().length, mViewModel.propertyAudit.length);

			assertEquals("companyName", mCompany.DBA, mViewModel.companyName);

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();
						
			testBindableProperty(mViewModel, "payrollTransaction", new PayrollTransaction());			
			testBindableProperty(mViewModel, "propertyAudit", new ArrayCollection());
		}

	}
}
