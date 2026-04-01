package test.sap.viewModel
{
    import flexunit.framework.TestSuite;

    import mx.collections.ArrayCollection;

    import psp.sap.model.CompanyKey;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.PayrollTransaction;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.viewmodel.AbstractPartViewModel;
    import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
    import psp.sap.viewmodel.PayrollTransactionsListViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;

    import test.mock.MockPayrollRunService;
    import test.mock.data.PayrollData;
    import test.mock.data.UserData;

    public class PayrollTransactionsListViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollTransactionsListViewModel = new PayrollTransactionsListViewModel();
		private var mDataService:MockPayrollRunService;
		
		private var mCompanyKey:CompanyKey;
		private var mPayrollRun:PayrollRun;
		
		private var voidedTransaction:Boolean = false;
		private var reissueFee:Boolean = false;

		public function PayrollTransactionsListViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollTransactionsListViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

            mSAP.session.user = UserData.getSuperDuperUser();

			trackedProperties = [
                "agencyTransactions",
				"employeePaychecks",
				"employeeTransactions",
				"employerTransactions",
				"intuitTransactions",
				"payrollRun",
				"payrollRunId",
				"showAgencyTransactions",
				"showEmployeePaychecks",
				"showEmployeeTransactions",
				"showEmployerTransactions",
				"showIntuitTransactions",
				"showVendorTransactions",
				"vendorTransactions"
			];

			mDataService = mSAP.payrollRunService as MockPayrollRunService;
			
			// company used when getting payrolls
			mCompanyKey = new CompanyKey();
			mCompanyKey.companyId = "123456";
			mCompanyKey.sourceSystemCd = SourceSystemEnum.QBOE.code;
			mViewModel.companyKey = mCompanyKey;
			
			// payroll run used when getting transactions
			mPayrollRun = new PayrollRun();
			mPayrollRun.sourcePayRunId = "10111213";

            mViewModel.sourcePayrollRunId = mPayrollRun.sourcePayRunId;
            mViewModel.paycheckDate = new Date();
			

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation                        
			loadModelDataExpectedFunctions();
			
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertEquals("employeePaychecks", 
						 PayrollData.getPayrollPaychecks().length,
						 mViewModel.employeePaychecks.length);
						 
			assertTrue("showEmployeePaychecks",
						 mViewModel.showEmployeePaychecks);
						 
			assertEquals("employeeTransactions", 
						 PayrollData.getPayrollEmployeeTransactions().length,
						 mViewModel.employeeTransactions.length);
			
			assertTrue("showEmployeeTransactions",
						 mViewModel.showEmployeeTransactions);

            assertEquals("vendorTransactions",
						 PayrollData.getPayrollEmployeeTransactions().length,
						 mViewModel.vendorTransactions.length);

			assertTrue("showVendorTransactions",
						 mViewModel.showVendorTransactions);
			
			assertEquals("agencyTransactions", 
						 PayrollData.getAgencyTransactions().length,
						 mViewModel.agencyTransactions.length);
						 
			assertTrue("showAgencyTransactions",
						 mViewModel.showAgencyTransactions);
						 
			assertEquals("employerTransactions", 
						 PayrollData.getPayrollTransactions().length,
						 mViewModel.employerTransactions.length);
						 
			assertTrue("showEmployerTransactions",
						 mViewModel.showEmployerTransactions);			 			
						 
			assertEquals("intuitTransactions", 
						 PayrollData.getPayrollTransactions().length,
						 mViewModel.intuitTransactions.length);
						 
			assertTrue("showIntuitTransactions",
						 mViewModel.showIntuitTransactions);
						 			
			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "payrollRun", new PayrollRun()); 
			testBindableProperty(mViewModel, "employeePaychecks", new ArrayCollection());
			testBindableProperty(mViewModel, "showEmployeePaychecks", false);
			 
			testBindableProperty(mViewModel, "agencyTransactions", new ArrayCollection());
			testBindableProperty(mViewModel, "showAgencyTransactions", false);
						
			testBindableProperty(mViewModel, "intuitTransactions", new ArrayCollection());
			testBindableProperty(mViewModel, "showIntuitTransactions", false);
			
			testBindableProperty(mViewModel, "employerTransactions", new ArrayCollection());
			testBindableProperty(mViewModel, "showEmployerTransactions", false);
			
			testBindableProperty(mViewModel, "employeeTransactions", new ArrayCollection());			
			testBindableProperty(mViewModel, "showEmployeeTransactions", false);

            testBindableProperty(mViewModel, "vendorTransactions", new ArrayCollection());
			testBindableProperty(mViewModel, "showVendorTransactions", false);
			
			testCancelTransaction();
		}
		
		private function testCancelTransaction():void {
			var payrollTransaction:PayrollTransaction = new PayrollTransaction();
			payrollTransaction.id = "cancelTransaction";
					
			mDataService.expectsCancelTransaction(mCompanyKey.sourceSystemCd, mCompanyKey.companyId, payrollTransaction.id);
			loadModelDataExpectedFunctions();
			
   			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);
   			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);   			   			     			
   			mViewModel.cancelTransaction(payrollTransaction);
		}
		
		private function testVoidTransaction():void {
			var payrollTransaction:PayrollTransaction = new PayrollTransaction();
			payrollTransaction.id = "voidTransaction";
					
			mDataService.expectsVoidTransaction(mCompanyKey.sourceSystemCd, mCompanyKey.companyId, payrollTransaction.id);
			loadModelDataExpectedFunctions();
			
   			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);
   			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);   			   			     			
   			mViewModel.voidTransaction(payrollTransaction);
		}
		
		private function testReissueFee():void {
			var payrollTransaction:PayrollTransaction = new PayrollTransaction();
			payrollTransaction.id = "reissueFeeTransaction";
					
			mDataService.expectsAddFeeRedebitTransaction(mCompanyKey.sourceSystemCd, mCompanyKey.companyId, payrollTransaction.id);
			loadModelDataExpectedFunctions();
			
   			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);
   			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);   			   			     			
   			mViewModel.reissueFee(payrollTransaction);
		}		                 				
   		   		
   		override protected function verifySave(e:ViewModelEvent):void {
   			assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);   			   			   			   			   		
   		}
   		
   		override protected function verifyRefresh(e:ViewModelEvent):void {
   			assertTrue(mDataService.errorMessage(), mDataService.success());   			
   			if(!voidedTransaction){
   				voidedTransaction = true;
   				testVoidTransaction();
   			}
   			else if(!reissueFee){
   				reissueFee = true;
   				testReissueFee();
   			}   			
  		}
  		
  		private function loadModelDataExpectedFunctions():void {
  			mDataService.expectsFindEmployerTransactions(mCompanyKey.companyId, mCompanyKey.sourceSystemCd, mPayrollRun.sourcePayRunId, null, null).willReturnAsync(PayrollData.getPayrollTransactions());
			mDataService.expectsFindIntuitTransactions(mCompanyKey.companyId, mCompanyKey.sourceSystemCd, mPayrollRun.sourcePayRunId, null, null).willReturnAsync(PayrollData.getPayrollTransactions());
			mDataService.expectsFindEmployeeTransactions(mCompanyKey.companyId, mCompanyKey.sourceSystemCd, mPayrollRun.sourcePayRunId, null, null).willReturnAsync(PayrollData.getPayrollEmployeeTransactions());
			mDataService.expectsFindPaychecks(mCompanyKey.companyId, mCompanyKey.sourceSystemCd, mPayrollRun.sourcePayRunId).willReturnAsync(PayrollData.getPayrollPaychecks());
			mDataService.expectsFindAgencyTransactions(mCompanyKey.companyId, mCompanyKey.sourceSystemCd, mPayrollRun.sourcePayRunId, null, null).willReturnAsync(PayrollData.getAgencyTransactions());
            mDataService.expectsFindVendorTransactions(mCompanyKey.companyId, mCompanyKey.sourceSystemCd, mPayrollRun.sourcePayRunId, null, null).willReturnAsync(PayrollData.getPayrollEmployeeTransactions());
        }

    }
}
