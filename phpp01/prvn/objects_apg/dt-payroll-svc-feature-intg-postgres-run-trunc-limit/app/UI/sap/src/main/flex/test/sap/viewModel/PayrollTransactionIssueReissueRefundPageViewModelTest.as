package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.formatters.NumberFormatter;
	
	import psp.sap.model.Company;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.PayrollTransaction;
	import psp.sap.model.SettlementTypeEnum;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
	import psp.sap.viewmodel.PayrollTransactionIssueReissueRefundPageViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockPayrollRunService;
	
	public class PayrollTransactionIssueReissueRefundPageViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollTransactionIssueReissueRefundPageViewModel;
		private var mDataService:MockPayrollRunService;
		
		private var mTransaction:PayrollTransaction;
		private var mCompany:Company;
		private var mNumberFormatter:NumberFormatter;

		public function PayrollTransactionIssueReissueRefundPageViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollTransactionIssueReissueRefundPageViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

            var newPspDate:Date = new Date("06/23/2009");
            newPspDate.setHours(5,3,1,53);
            mSAP.setPSPDate(newPspDate.getTime());
            mViewModel = new PayrollTransactionIssueReissueRefundPageViewModel();

			trackedProperties = [
				"settlementDate",
				"settlementType",
				"allowAmountChange",
				"settlementTypes",
				"amount",
				"canSave"
			];
			
			mNumberFormatter = new NumberFormatter();
			mNumberFormatter.useThousandsSeparator = false;
            mNumberFormatter.precision = 2;
			
			mDataService = mSAP.payrollRunService as MockPayrollRunService;
			
			// setup model data
			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompany.companyId = "12358";
			
			mTransaction = new PayrollTransaction();
			mTransaction.amount = 5.00;
			mTransaction.id = "123";
			
			mViewModel.company = mCompany;

            mViewModel.payrollTransaction = mTransaction;

            var payrollRun:PayrollRun = new PayrollRun();
            payrollRun.sourcePayRunId = "12456734";

            mViewModel.payrollRun = payrollRun; 

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {						
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertEquals("allowAmountChange", false, mViewModel.allowAmountChange);			
			assertEquals("amount", mNumberFormatter.format(mTransaction.amount), mViewModel.amount);
            assertEquals("settlementDate", "06/23/2009", mViewModel.settlementDate);
			assertEquals("settlementType", SettlementTypeEnum.ACH, mViewModel.settlementType);
			assertEquals("bankAccountLabel", "[No Active Bank Account]", mViewModel.bankAccountLabel);			

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "settlementDate", null); 
			testBindableProperty(mViewModel, "settlementType", SettlementTypeEnum.CHECK);  
			testBindableProperty(mViewModel, "settlementTypes", []); 
			testBindableProperty(mViewModel, "amount", "1.00"); 

			verifyHasChangedLogic();
		}

		override protected function verifyHasChangedLogic():void {			
			mViewModel.settlementType = SettlementTypeEnum.CHECK;
											
			mViewModel.amount = "";
			assertNotNull("settlementDate", mViewModel.settlementDate);
			assertEquals("can save", false, mViewModel.canSave);
				
			clearPropertyChangeEventHistory();
					
			testHasChanged(mViewModel, "amount", "1.00");

			testValidators();
		}

		override protected function testValidators():void {
			mViewModel.settlementType = SettlementTypeEnum.CHECK;
			mViewModel.amount = "2.00";
			
			
			assertEquals("can save", true, mViewModel.canSave);
			
			testDateValidator(mViewModel, "settlementDate", 45, 0);
			testNumberValidator(mViewModel, "amount", 0.01);
			testRequiredStringValidator(mViewModel, "amount");
			testRequiredStringValidator(mViewModel, "settlementDate");

			testSave();
		}

		override protected function testSave():void {
			mViewModel.settlementType = SettlementTypeEnum.ACH;						
			mViewModel.settlementDate = "07/24/2009";
            assertEquals("can save", true, mViewModel.canSave);

			mDataService.expectsRefundEmployerTransaction(mCompany.sourceSystemCd,
													                    mCompany.companyId,
													                    mTransaction.id,
													                    mTransaction.amount,
													                    new Date("07/24/2009"),
													                    SettlementTypeEnum.ACH.code)
			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);

			mViewModel.save();
		}

		override protected function verifySave(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);

			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertEquals("allowAmountChange", false, mViewModel.allowAmountChange);			
			assertEquals("amount", mNumberFormatter.format(mTransaction.amount), mViewModel.amount);				
			assertNotNull("settlementDate",	mViewModel.settlementDate);				
			assertEquals("settlementType", SettlementTypeEnum.ACH, mViewModel.settlementType);			
		}

	}
}
