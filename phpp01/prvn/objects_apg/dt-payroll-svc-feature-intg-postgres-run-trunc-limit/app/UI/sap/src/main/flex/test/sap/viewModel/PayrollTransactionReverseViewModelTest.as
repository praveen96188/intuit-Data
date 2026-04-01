package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.application.SAP;
	import psp.sap.model.Company;
	import psp.sap.model.CompanyBankAccount;
	import psp.sap.model.PayrollEmployeeTransaction;
	import psp.sap.model.PayrollRun;
	import psp.sap.model.SettlementTypeEnum;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
	import psp.sap.viewmodel.PayrollTransactionReverseViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockPayrollRunService;
import test.mock.data.PayrollData;

public class PayrollTransactionReverseViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollTransactionReverseViewModel;
		private var mDataService:MockPayrollRunService;
		private var mCompany:Company;
		private var mPayroll:PayrollRun;

		public function PayrollTransactionReverseViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollTransactionReverseViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

            mSAP.setPSPDate(new Date("2005/03/12").getTime());
            mViewModel  = new PayrollTransactionReverseViewModel();


			trackedProperties = [
				"accountNumber",
				"allowInitiateReversal",
				"boolSettlementDateVisible",
				"canSelectNonStandardSettlementTypes",
				"chargeReversalFee",
				"checkBoxEnabled",
				"custRequested",
				"employeeTransactions",
				"hasActiveBankAccount",
				"initiateReversal",
				"isACHType",
				"payrollRun",
				"reversalExpectedDate",
				"selected",
				"settlementType",
				"settlementTypes",
				"transactionDate",
				"canSave"
			];

			mDataService = mSAP.payrollRunService as MockPayrollRunService;
			
			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompany.companyId = "1234";
			var bankAccount:CompanyBankAccount = new CompanyBankAccount();
			bankAccount.accountNumber = "158";

			mViewModel.company = mCompany;
			
			mPayroll = new PayrollRun();
			mPayroll.sourcePayRunId = "payrollRun123";
			mPayroll.payrollRunStatus = "OffloadedAll";

            mViewModel.payrollRun = mPayroll;

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mDataService.expectsFindReversableEmployeeTransactions(mCompany.companyId,
														                        mCompany.sourceSystemCd,
														                        mPayroll.sourcePayRunId,
														                        null,
														                        null).willReturnAsync(PayrollData.getPayrollEmployeeTransactions());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			checkInitValues();

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();
															
			testBindableProperty(mViewModel, "chargeReversalFee", false);			
			testBindableProperty(mViewModel, "checkBoxEnabled", false);			
			testBindableProperty(mViewModel, "custRequested", "blah");			
			testBindableProperty(mViewModel, "employeeTransactions", new ArrayCollection());			
			testBindableProperty(mViewModel, "hasActiveBankAccount", false);			
			testBindableProperty(mViewModel, "initiateReversal", true);			
			testBindableProperty(mViewModel, "isACHType", false);															
			testBindableProperty(mViewModel, "selected", true);			
			testBindableProperty(mViewModel, "settlementType", SettlementTypeEnum.WIRE);			
			testBindableProperty(mViewModel, "settlementTypes", SettlementTypeEnum.ach_wire);			
			testBindableProperty(mViewModel, "transactionDate", "05/05/2005");

			testValidators();
		}
		
		override protected function testValidators():void { 
            // init not checked
            assertEquals("selected", false, mViewModel.selected);
            assertEquals("canSave", false, mViewModel.canSave);
            
			// test selection check boxes
            // check the column header
            mViewModel.selectAll = true;            
            assertEquals("selected", true,  mViewModel.selected);                       
            for each(var txn:PayrollEmployeeTransaction in mViewModel.employeeTransactions){
                assertEquals("selectedTxn", true, txn.selected);
            }
            assertEquals("canSave", true, mViewModel.canSave);

            // assumes the previous lines passed
            var transaction:PayrollEmployeeTransaction = mViewModel.employeeTransactions[0];
            // un-check the only txn
            transaction.selected = false;			
            assertEquals("selected", false, mViewModel.selected);
            assertEquals("canSave", false, mViewModel.canSave);

            // re-check the box
            transaction.selected = true;            
            assertEquals("selected", true, mViewModel.selected);
            assertEquals("canSave", true, mViewModel.canSave);
            
            // uncheck all of the transactions
            mViewModel.selectAll = false;            
            assertEquals("selected", false, mViewModel.selected);                        
            for each(var txn2:PayrollEmployeeTransaction in mViewModel.employeeTransactions){
                assertEquals("selectedTxn", txn2.selected, false);
            }
            assertEquals("canSave", false, mViewModel.canSave);
            
            mViewModel.selectAll = true;
            mViewModel.settlementType = SettlementTypeEnum.WIRE;            
            mViewModel.transactionDate = dateFormatter.format(SAP.instance.PSPDate);
            assertEquals("canSave", true, mViewModel.canSave);
            
            testDateValidator(mViewModel, "transactionDate", 45, 0);
            
        	testSave();
		}

		override protected function testSave():void {
			var currentDate:Date = SAP.instance.PSPDate;
			mViewModel.transactionDate = dateFormatter.format(currentDate);				
			assertEquals("can save", true, mViewModel.canSave);

			mDataService.expectsReversePayrollRunTransactions(mCompany.sourceSystemCd,
																		mCompany.companyId,
												                        new ArrayCollection(["123456"]),
												                        mPayroll.sourcePayRunId,
												                        true,
												                        new Date(dateFormatter.format(currentDate)),
												                        SettlementTypeEnum.WIRE.code,
												                        false);
			mDataService.expectsFindReversableEmployeeTransactions(mCompany.companyId,
														                        mCompany.sourceSystemCd,
														                        mPayroll.sourcePayRunId,
														                        null,
														                        null).willReturnAsync(PayrollData.getPayrollEmployeeTransactions());
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

			checkInitValues();
		}
		
		private function checkInitValues():void {
			assertEquals("employeeTransactions", 1, mViewModel.employeeTransactions.length);

			for each(var txn:PayrollEmployeeTransaction in mViewModel.employeeTransactions){
                assertEquals("selectedTxn", false, txn.selected);
                assertEquals("enabledTxn", true, txn.enabled);
            }
            
            assertEquals("selected", false, mViewModel.selected);
            assertEquals("canSave", false, mViewModel.canSave);					
			            
            assertEquals("initiateReversal", false, mViewModel.initiateReversal);
            assertEquals("chargeReversalFee", true, mViewModel.chargeReversalFee);
            assertEquals("checkBoxEnabled", true, mViewModel.checkBoxEnabled);
            assertEquals("settlementType", SettlementTypeEnum.ACH, mViewModel.settlementType);
            assertEquals("isACHType", true, mViewModel.isACHType);
            assertEquals("transactionDate", "", mViewModel.transactionDate);
            assertEquals("custRequested", "(Customer Requested)", mViewModel.custRequested);
            assertEquals("allowInitiateReversal", false, mViewModel.payrollRunStatus.allowInitiateReversal);


		}

	}
}