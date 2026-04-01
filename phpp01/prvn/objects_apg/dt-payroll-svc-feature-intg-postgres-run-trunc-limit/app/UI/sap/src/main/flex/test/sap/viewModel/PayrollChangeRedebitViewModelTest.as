package test.sap.viewModel
{
	import flexunit.framework.TestSuite;

    import psp.sap.model.ActionEvent;
    import psp.sap.model.ActionEventCode;
    import psp.sap.model.BillingTransaction;
	import psp.sap.model.Company;
	import psp.sap.model.CompanyBankAccount;
	import psp.sap.model.PayrollBillingTransactions;
	import psp.sap.model.PayrollRun;
	import psp.sap.model.SettlementTypeEnum;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
	import psp.sap.viewmodel.PayrollChangeRedebitViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockPayrollRunService;
	import test.mock.data.PayrollData;

	public class PayrollChangeRedebitViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollChangeRedebitViewModel = new PayrollChangeRedebitViewModel();
		private var mDataService:MockPayrollRunService;
		private var mCompany:Company;
		private var mPayrollRun:PayrollRun;

		public function PayrollChangeRedebitViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollChangeRedebitViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"editAmounts",
				"canSave"
			];

			mDataService = mSAP.payrollRunService as MockPayrollRunService;
			
			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompany.companyId = "46574";
			mViewModel.company = mCompany;
			
			mPayrollRun = new PayrollRun();
			mPayrollRun.sourcePayRunId = "8797";
            mViewModel.payrollRun = mPayrollRun;
            mViewModel.action = new ActionEvent(ActionEventCode.DD_REDEBIT_ADD);

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mDataService.expectsGetRedebitTransactionsForPayroll(mCompany.sourceSystemCd, mCompany.companyId, mPayrollRun.sourcePayRunId).willReturnAsync(PayrollData.getBillingTransactions());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("settlementType", SettlementTypeEnum.ACH, mViewModel.settlementType);
			assertEquals("payrolls", PayrollData.getBillingTransactions().length, mViewModel.payrolls.length);
			for each(var payroll:PayrollBillingTransactions in mViewModel.payrolls){
				//assertEquals("ddTransaction", payroll.ddTransaction.financialAmount, payroll.ddTransaction.financialReturnAmount);
				assertEquals("taxTransaction", payroll.taxTransaction.financialAmount, payroll.taxTransaction.financialReturnAmount);
				for each(var fee:BillingTransaction in payroll.feeTransactions){
					assertEquals("financialAmount", fee.financialAmount, fee.financialReturnAmount);
					assertEquals("salesTaxReturnAmount", fee.salesTaxAmount, fee.salesTaxReturnAmount);
				}
			}			

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			verifyHasChangedLogic();
		}

		override protected function verifyHasChangedLogic():void {			
			testValidators();
		}

		override protected function testValidators():void {			
			testSave();
		}

		override protected function testSave():void {
            mViewModel.date = mViewModel.dateFormatter.format(mSAP.PSPDate);
			assertEquals("can save", true, mViewModel.canSave);            

			mDataService.expectsRedebitPayrollTransactions(mCompany.sourceSystemCd,
																		mCompany.companyId,
																		SettlementTypeEnum.ACH.code,
                                                                        new Date(mViewModel.dateFormatter.format(mSAP.PSPDate)),
																		mViewModel.payrolls);
			mDataService.expectsGetRedebitTransactionsForPayroll(mCompany.sourceSystemCd, mCompany.companyId, mPayrollRun.sourcePayRunId).willReturnAsync(PayrollData.getBillingTransactions());
			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);			
			mViewModel.save();
		}

		override protected function verifySave(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);
		}

		override protected function verifyRefresh(e:ViewModelEvent):void {
            assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);
            assertTrue(mDataService.errorMessage(), mDataService.success());
			assertEquals("settlementType", SettlementTypeEnum.ACH, mViewModel.settlementType);
			assertEquals("payrolls", PayrollData.getBillingTransactions().length, mViewModel.payrolls.length);
			for each(var payroll:PayrollBillingTransactions in mViewModel.payrolls){
				//assertEquals("ddTransaction", payroll.ddTransaction.financialAmount, payroll.ddTransaction.financialReturnAmount);
				assertEquals("taxTransaction", payroll.taxTransaction.financialAmount, payroll.taxTransaction.financialReturnAmount);
				for each(var fee:BillingTransaction in payroll.feeTransactions){
					assertEquals("financialAmount", fee.financialAmount, fee.financialReturnAmount);
					assertEquals("salesTaxReturnAmount", fee.salesTaxAmount, fee.salesTaxReturnAmount);
				}
			}			
		}

	}
}
