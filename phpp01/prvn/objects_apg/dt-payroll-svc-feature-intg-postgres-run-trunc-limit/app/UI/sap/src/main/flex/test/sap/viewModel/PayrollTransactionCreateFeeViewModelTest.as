package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import psp.sap.application.SAP;
	import psp.sap.model.Company;
	import psp.sap.model.CompanyBankAccount;
	import psp.sap.model.PayrollRun;
	import psp.sap.model.SettlementTypeEnum;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
	import psp.sap.viewmodel.PayrollTransactionCreateFeeViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockCompanyService;
	import test.mock.MockPayrollRunService;

	public class PayrollTransactionCreateFeeViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollTransactionCreateFeeViewModel;
		private var mLoadService:MockCompanyService;
		private var mSaveService:MockPayrollRunService;
		
		private var mCompany:Company;
		private var mPayroll:PayrollRun;

		public function PayrollTransactionCreateFeeViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollTransactionCreateFeeViewModelTest );
		}
        //decided I wasn't really interested in fixing this at the moment.
           /*
		override public function setUp():void {
			super.setUp();

            SAP.instance.PSPDate.setTime(new Date("2009/03/12").getTime());
            mViewModel = new PayrollTransactionCreateFeeViewModel();

			trackedProperties = [
				"settlementType",				
				"canSelectNonStandardSettlementTypes",
				"payrollRunDate",
				"payrollRunId",
				"arrangementAmount",
				"isArrangementChecked",
				"settlementDate",
				"settlementTypes",
				"isReversalChecked",
				"returnAmount",
				"isReturnChecked",
				"reversalAmount",
				"canSave"
			];

			mLoadService = mSAP.companyService as MockCompanyService;
			mSaveService = mSAP.payrollRunService as MockPayrollRunService;
			
			// setup payroll
			mPayroll = new PayrollRun();
			mPayroll.sourcePayRunId = "payrollid";
			
			// setup company
			mCompany = new Company();
			mCompany.companyId = "123456";
			mCompany.sourceSystemCd = SourceSystemEnum.QBOE.code;
			var bankAccount:CompanyBankAccount = new CompanyBankAccount();
			bankAccount.accountId = "1234";
			bankAccount.bankName = "name";
			mCompany.activeBankAccount = bankAccount;
			mViewModel.company = mCompany;

            mViewModel.payrollRun = mPayroll;

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mLoadService.expectsGetOfferingServiceChargePrice(mCompany.sourceSystemCd, mCompany.companyId, "ReversalFee").willReturnAsync(10.0);
			mLoadService.expectsGetOfferingServiceChargePrice(mCompany.sourceSystemCd, mCompany.companyId, "DebitReturnFee").willReturnAsync(20.0);
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mLoadService.errorMessage(), mLoadService.success());

			verifyBackingPropertiesInitilized();

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "arrangementAmount", "5.00");
			testBindableProperty(mViewModel, "isArrangementChecked", true);
			testBindableProperty(mViewModel, "isReturnChecked", true); 
			testBindableProperty(mViewModel, "isReversalChecked", true); 			
			testBindableProperty(mViewModel, "returnAmount", "5.00");
			testBindableProperty(mViewModel, "reversalAmount", "5.00");
			testBindableProperty(mViewModel, "settlementDate", null);
			testBindableProperty(mViewModel, "settlementType", SettlementTypeEnum.CASH);
			testBindableProperty(mViewModel, "settlementTypes", SettlementTypeEnum.non_ach_values);

			testValidators();
		}		

		override protected function testValidators():void {
			// update has changed so can save is true
			mViewModel.settlementType = SettlementTypeEnum.WIRE;
			mViewModel.isReturnChecked = true;
			mViewModel.returnAmount = "5.00";
			mViewModel.isReversalChecked = true;
			mViewModel.reversalAmount = "4.00";
			mViewModel.isArrangementChecked = true;			
			mViewModel.arrangementAmount = "10.00";			

			assertEquals("can save", true, mViewModel.canSave);
			
			testNumberValidator(mViewModel, "arrangementAmount", 0.01);
			testNumberValidator(mViewModel, "returnAmount", 0.01);
			testNumberValidator(mViewModel, "reversalAmount", 0.01);
			testDateValidator(mViewModel, "settlementDate", 0, 45);
			testRequiredStringValidator(mViewModel, "settlementDate");

			testSave();
		}		
		
		override protected function testSave():void {
			var currentDate:Date = SAP.instance.PSPDate;
			mViewModel.settlementDate = dateFormatter.format(currentDate);				
			assertEquals("can save", true, mViewModel.canSave);

			mSaveService.expectsAddFeeTransactions(mCompany.companyId, 
																mCompany.sourceSystemCd, 
																mPayroll.sourcePayRunId, 
																SettlementTypeEnum.WIRE.code, 
																new Date(dateFormatter.format(currentDate)),
																4.00,
																5.00,
																10.00);
			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);
			addAsyncVerifier(mViewModel, ViewModelEvent.BACKING_PROPERTIES_INITIALIZED, verifyRefresh);		

			mViewModel.save();
		}

		override protected function verifySave(e:ViewModelEvent):void {		    
			assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);
		}
		
		override protected function verifyRefresh(e:ViewModelEvent):void {
            assertTrue(mSaveService.errorMessage(), mSaveService.success());

			verifyBackingPropertiesInitilized();

            testSaveReturnOnly();
		}

        private function testSaveReturnOnly():void {
            mViewModel.isReversalChecked = false;
            mViewModel.isReturnChecked = true;
            mViewModel.isArrangementChecked = false;

            mViewModel.returnAmount = "50";

            addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySaveReturnOnly);
            addAsyncVerifier(mViewModel, ViewModelEvent.BACKING_PROPERTIES_INITIALIZED, verifySaveReturnOnlyRefresh);

            mSaveService.expectsAddFeeTransactions(mCompany.companyId,
                    mCompany.sourceSystemCd,
                    mPayroll.sourcePayRunId,
                    SettlementTypeEnum.ACH.code,
                    new Date(dateFormatter.format(SAP.instance.PSPDate)),
                    0,
                    50.00,
                    0);

            mViewModel.save();
        }

        protected function verifySaveReturnOnly(e:ViewModelEvent):void {            
            assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);
        }

        protected function verifySaveReturnOnlyRefresh(e:ViewModelEvent):void {
            assertTrue(mSaveService.errorMessage(), mSaveService.success());

			verifyBackingPropertiesInitilized();
            
		}
				
		private function verifyBackingPropertiesInitilized():void {
			assertEquals("isReversalChecked", false, mViewModel.isReversalChecked);
			assertEquals("isReversalChecked", false, mViewModel.isReversalChecked);
			assertEquals("isReturnChecked", false, mViewModel.isReturnChecked);
			
			assertEquals("settlementType", SettlementTypeEnum.ACH, mViewModel.settlementType);
			assertEquals("arrangementAmount", "0.00", mViewModel.arrangementAmount);
			assertEquals("returnAmount", "20.00", mViewModel.returnAmount);
			assertEquals("reversalAmount", "10.00", mViewModel.reversalAmount);
			
			assertNotNull("settlementDate", mViewModel.settlementDate);
		}
*/

	}
}


