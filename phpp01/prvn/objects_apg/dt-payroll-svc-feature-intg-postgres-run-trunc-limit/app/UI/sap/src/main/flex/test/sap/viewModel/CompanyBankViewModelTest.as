package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.model.Company;
	import psp.sap.model.CompanyBankAccount;
	import psp.sap.model.EmployeeInfo;
	import psp.sap.model.RandomDebitTransaction;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.view.UIUtils;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyBankViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockCompanyService;
	import test.mock.data.CompanyData;
	import test.mock.data.UserData;
	
	public class CompanyBankViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:CompanyBankViewModel;
		private var mDataService:MockCompanyService;
		
		private var mCompany:Company;

		public function CompanyBankViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( CompanyBankViewModelTest );
		}

		override public function setUp():void {
			super.setUp();
			
			//mSAP.session.user = UserData.getUser("DirectDepositTierIII");
			mSAP.session.user = UserData.getSuperDuperUser();

			mViewModel = new CompanyBankViewModel();			

			trackedProperties = [
				"bankAccount",
				"bankAccountExists",
				"bankAccountStatusColor",
				"bankAccountStatusText",
				"canAddEdit",
				"canGenerateDebits",
				"canResetAttempts",
				"employeeHeaderText",
				"employees",
				"isPendingRandomDebit",
				"isPendingRandomDebitString",
				"isPendingVerification",
				"mayAddEdit",
				"mayGenerateDebits",
				"mayResetAttempts",
				"verificationDebitsVisible",
				"randomVerificationTxns",
				"showQBAccount",
                "vendorHeaderText",
				"vendors",
				"canSave"
			];
			
			mTrackedActionEvents.push("onReinitiateSucceeded");
			
			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompany.companyId = "1234";
			mViewModel.company = mCompany;

			mDataService = mSAP.companyService as MockCompanyService;
			
			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {						
			mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccount());		            
			mDataService.expectsGetEmployees(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getEmployees());
            mDataService.expectsGetVendors(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getVendors());
			mDataService.expectsIsPendingRandomDebit(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(false);
			mDataService.expectsGetRandomDebitTransactions(mCompany.companyId, mCompany.sourceSystemCd, "978").willReturnAsync(CompanyData.getRandomDebitTransactions());
			
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {			
			assertTrue(mDataService.errorMessage(), mDataService.success());

			//a lot of this depends on the data, and we'll test that specifically later
			//right now let's just test the stuff for the "saves"
			
			assertTrue(mViewModel.canResetAttempts);
			assertTrue(mViewModel.canGenerateDebits);

			testBindableProperties();
		}

		override protected function testBindableProperties():void {			
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "bankAccount", new CompanyBankAccount()); 
			testBindableBoolean(mViewModel,  "bankAccountExists"); 
			testBindableProperty(mViewModel, "bankAccountStatusColor", UIUtils.COLOR_GREEN);
			testBindableProperty(mViewModel, "bankAccountStatusText", "text");
			testBindableBoolean(mViewModel,  "canAddEdit"); 
			testBindableBoolean(mViewModel,  "canGenerateDebits");
			testBindableBoolean(mViewModel,  "canResetAttempts");
			testBindableProperty(mViewModel, "employeeHeaderText", "text");
			testBindableProperty(mViewModel, "employees", new ArrayCollection());
			testBindableBoolean(mViewModel,  "isPendingRandomDebit");
			testBindableProperty(mViewModel, "isPendingRandomDebitString", "text");
			testBindableBoolean(mViewModel,  "isPendingVerification");
			testBindableBoolean(mViewModel,  "mayAddEdit");
			testBindableBoolean(mViewModel,  "mayGenerateDebits");
			testBindableBoolean(mViewModel,  "mayResetAttempts");
			testBindableBoolean(mViewModel,  "verificationDebitsVisible");
			testBindableProperty(mViewModel, "randomVerificationTxns", new ArrayCollection());
			testBindableBoolean(mViewModel,  "showQBAccount");

			testResetAttempts();
		}
		
		private function testResetAttempts():void {			
			mDataService.expectsResetVerifyAttempts(mCompany.companyId, mCompany.sourceSystemCd, "587");
			
			mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccountNoRetries());			
			mDataService.expectsGetEmployees(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getEmployees());
            mDataService.expectsGetVendors(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getVendors());
			mDataService.expectsIsPendingRandomDebit(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(false);
			mDataService.expectsGetRandomDebitTransactions(mCompany.companyId, mCompany.sourceSystemCd, "978").willReturnAsync(CompanyData.getRandomDebitTransactions());

			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifyResetAttempts);
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefreshAfterResetAttempts);
			mViewModel.resetAttempts();			 
		}
		
		private function verifyResetAttempts(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);
		}

		private function verifyRefreshAfterResetAttempts(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);
            assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertFalse("cannot reset attempts because 0", mViewModel.canResetAttempts);
			
			testGenerateRandomDebits();					
		}	
		
		private function testGenerateRandomDebits():void {														
			mDataService.expectsReinitiateRandomDebit(mCompany.companyId, mCompany.sourceSystemCd, "587");
			
			mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccountNoRetries());
			mDataService.expectsGetEmployees(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getEmployees());
            mDataService.expectsGetVendors(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getVendors());
			mDataService.expectsIsPendingRandomDebit(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(true);			
			mDataService.expectsGetRandomDebitTransactions(mCompany.companyId, mCompany.sourceSystemCd, "978").willReturnAsync(CompanyData.getRandomDebitTransactions());


			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifyGenerateRandomDebits);
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefreshAfterGeneratingRandomDebits);
			mViewModel.createRandomDebits();			 
		}
		
		private function verifyGenerateRandomDebits(e:ViewModelEvent):void {
			assertEventHistory(["onReinitiateSucceeded", ViewModelEvent.SAVE_SUCCEEDED]);
		}

		private function verifyRefreshAfterGeneratingRandomDebits(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);
			assertTrue(mDataService.errorMessage(), mDataService.success());
			assertFalse("cannot generate debits because pending", mViewModel.canGenerateDebits);
		}	
		
		public function testNoBank():void {									
			mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(null);
			mDataService.expectsGetEmployees(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(new ArrayCollection());
            mDataService.expectsGetVendors(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getVendors());
			
			addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyNoBank);
			mViewModel.activate();
		}
		
		private function verifyNoBank(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertEquals("no ees", 0, mViewModel.employees.length);
			assertEquals("no txns loaded", 0, mViewModel.randomVerificationTxns.length);
			assertNull("no bank", mViewModel.bankAccount);
			assertFalse("no bank", mViewModel.bankAccountExists);
			assertFalse("nothing to be pending verififaction", mViewModel.isPendingVerification);			
			assertFalse("cannot add bank account if there is no existing one", mViewModel.canAddEdit);
			assertFalse("no debits", mViewModel.verificationDebitsVisible);
			assertEquals("0 Employees", mViewModel.employeeHeaderText);
			//all other properties are not visible when !bankAccountExists || !isPendingVerification			
		}
				
		public function testActiveBank():void {
			mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccountActive());			
			mDataService.expectsGetEmployees(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getEmployees());
            mDataService.expectsGetVendors(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getVendors());
			mDataService.expectsIsPendingRandomDebit(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(false);
			mDataService.expectsGetRandomDebitTransactions(mCompany.companyId, mCompany.sourceSystemCd, "978").willReturnAsync(CompanyData.getRandomDebitTransactions());
			
			addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyActiveBank);
			mViewModel.activate();
									
		}
		
		private function verifyActiveBank(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertTrue(mViewModel.bankAccountExists);
			assertFalse(mViewModel.isPendingRandomDebit);
			assertEquals("Active", mViewModel.bankAccountStatusText);
			assertFalse(mViewModel.isPendingVerification);
			assertEquals(UIUtils.COLOR_GREEN, mViewModel.bankAccountStatusColor);
			assertTrue(mViewModel.showQBAccount);
			assertEquals("2 Employees", mViewModel.employeeHeaderText);
			assertFalse(mViewModel.verificationDebitsVisible);
			
			assertTrue(mViewModel.canAddEdit);

			//all other properties not visible because isPendingRandomDebit is false			
		}
		
		public function testPendingVerificationBankPendingRandomTransactions():void {
			mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccount());			
			mDataService.expectsGetEmployees(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getEmployees());
            mDataService.expectsGetVendors(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getVendors());
			mDataService.expectsIsPendingRandomDebit(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(true);
			mDataService.expectsGetRandomDebitTransactions(mCompany.companyId, mCompany.sourceSystemCd, "978").willReturnAsync(CompanyData.getRandomDebitTransactions());
			
			addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyPendingVerificationBankPendingRandomTransactions);
			mViewModel.activate();
									
		}
		
		private function verifyPendingVerificationBankPendingRandomTransactions(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertTrue(mViewModel.bankAccountExists);
			assertTrue(mViewModel.isPendingRandomDebit);
			assertEquals("Pending Verification", mViewModel.bankAccountStatusText);
			assertTrue(mViewModel.isPendingVerification);
			assertEquals(UIUtils.COLOR_RED, mViewModel.bankAccountStatusColor);
			assertTrue(mViewModel.showQBAccount);
			assertEquals("2 Employees", mViewModel.employeeHeaderText);
			assertTrue(mViewModel.verificationDebitsVisible);
			assertEquals("Yes",mViewModel.isPendingRandomDebitString);
			
			assertTrue(mViewModel.canAddEdit);
			
			assertTrue(mViewModel.canResetAttempts);
			assertFalse("Debits not sent to bank", mViewModel.canGenerateDebits);		
		}
		
		public function testPendingVerificationBankNoPendingRandomTransactions():void {
			mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccount());			
			mDataService.expectsGetEmployees(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getEmployees1Employee());
            mDataService.expectsGetVendors(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getVendors());
			mDataService.expectsIsPendingRandomDebit(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(false);
			mDataService.expectsGetRandomDebitTransactions(mCompany.companyId, mCompany.sourceSystemCd, "978").willReturnAsync(CompanyData.getRandomDebitTransactions());
			
			addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyPendingVerificationBankNoPendingRandomTransactions);
			mViewModel.activate();
									
		}
		
		private function verifyPendingVerificationBankNoPendingRandomTransactions(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertTrue(mViewModel.bankAccountExists);
			assertFalse(mViewModel.isPendingRandomDebit);
			assertEquals("Pending Verification", mViewModel.bankAccountStatusText);
			assertTrue(mViewModel.isPendingVerification);
			assertEquals(UIUtils.COLOR_RED, mViewModel.bankAccountStatusColor);
			assertTrue(mViewModel.showQBAccount);
			assertEquals("1 Employee", mViewModel.employeeHeaderText);
			assertTrue(mViewModel.verificationDebitsVisible);
			assertEquals("No",mViewModel.isPendingRandomDebitString);
			
			assertTrue(mViewModel.canAddEdit);
			
			assertTrue(mViewModel.canResetAttempts);
			assertTrue(mViewModel.canGenerateDebits);		
		}		
		
		public function testPendingVerificationBankNoMaxRetries():void {
			mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccountAtRetryLimit());			
			mDataService.expectsGetEmployees(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getEmployees1Employee());
            mDataService.expectsGetVendors(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getVendors());
			mDataService.expectsIsPendingRandomDebit(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(false);
			mDataService.expectsGetRandomDebitTransactions(mCompany.companyId, mCompany.sourceSystemCd, "978").willReturnAsync(CompanyData.getRandomDebitTransactions());
			
			addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyPendingVerificationBankNoMaxRetries);
			mViewModel.activate();
									
		}
		
		private function verifyPendingVerificationBankNoMaxRetries(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertEquals("There have been 2 or more attempts to verify this account", mViewModel.bankAccountStatusText);
			assertEquals(UIUtils.COLOR_RED, mViewModel.bankAccountStatusColor);		
		}		
				
		public function testEmployeeSorting():void {
			mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccountAtRetryLimit());			
			mDataService.expectsGetEmployees(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getEmployees4Employees());
            mDataService.expectsGetVendors(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getVendors());
			mDataService.expectsIsPendingRandomDebit(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(false);
			mDataService.expectsGetRandomDebitTransactions(mCompany.companyId, mCompany.sourceSystemCd, "978").willReturnAsync(CompanyData.getRandomDebitTransactions());
			
			addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyEmployeeSorting);
			mViewModel.activate();
									
		}
		
		private function verifyEmployeeSorting(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertEquals("4 Employees", mViewModel.employeeHeaderText);
			//sorts by last name, first name; but only need to check 1 field equality for order
			assertEquals("John", (mViewModel.employees.getItemAt(0) as EmployeeInfo).firstName);
			assertEquals("Jonny", (mViewModel.employees.getItemAt(1) as EmployeeInfo).firstName);
			assertEquals("James", (mViewModel.employees.getItemAt(2) as EmployeeInfo).firstName);
			assertEquals("Little", (mViewModel.employees.getItemAt(3) as EmployeeInfo).firstName);
		}			
		
		public function testRandomDebitFormatters():void {
			var txn:RandomDebitTransaction = new RandomDebitTransaction();
			txn.offloadedDate = new Date(2009,5,5);
			assertEquals("06/05/2009", CompanyBankViewModel.formatOffloadDate(txn));
			txn.offloadedDate = null;
			assertEquals("Pending Offload", CompanyBankViewModel.formatOffloadDate(txn));
			
			txn.settlementDate = new Date(2009,5,5);
			assertEquals("06/05/2009", CompanyBankViewModel.formatSettlementDate(txn, null));
			
			txn.amount1 = "0.53";
			assertEquals("$0.53", CompanyBankViewModel.formatAmount1(txn, null));
			
			txn.amount2 = "0.12";
			assertEquals("$0.12", CompanyBankViewModel.formatAmount2(txn, null));
		}
		
		public function testEmployeeFormatter():void {
			var eInfo:EmployeeInfo = CompanyData.getEmployees().getItemAt(0) as EmployeeInfo;
			assertEquals("Tables, Little Bobby", CompanyBankViewModel.getFullNameDisplay(eInfo));
		}
		
		//not testing the mapping, just that these can be set when going from most permissive (admin's set) to most restrictive (none)
		public function testPermissionsPermissive():void {			
			mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccount());			
			mDataService.expectsGetEmployees(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getEmployees());
            mDataService.expectsGetVendors(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getVendors());
			mDataService.expectsIsPendingRandomDebit(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(true);
			mDataService.expectsGetRandomDebitTransactions(mCompany.companyId, mCompany.sourceSystemCd, "978").willReturnAsync(CompanyData.getRandomDebitTransactions());
			
			addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyPermissionsPermissive);
			mViewModel.activate();			 	
		}
		
		private function verifyPermissionsPermissive(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertTrue(mViewModel.mayAddEdit);
			assertTrue(mViewModel.mayGenerateDebits);
			assertTrue(mViewModel.mayResetAttempts);
			assertTrue(mViewModel.verificationDebitsVisible);
		}
		
		private var oldPermissions:ArrayCollection;
		public function testPermissionsRestrictive():void {
			oldPermissions = mSAP.session.user.grantedOperations;
			mSAP.session.user.grantedOperations = new ArrayCollection();		
			
			mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccount());			
			mDataService.expectsGetEmployees(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getEmployees());
            mDataService.expectsGetVendors(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getVendors());
			mDataService.expectsIsPendingRandomDebit(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(true);
			mDataService.expectsGetRandomDebitTransactions(mCompany.companyId, mCompany.sourceSystemCd, "978").willReturnAsync(CompanyData.getRandomDebitTransactions());
			
			addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyPermissionsRestrictive);
			mViewModel.activate();			 	
		}
		
		private function verifyPermissionsRestrictive(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertFalse(mViewModel.mayAddEdit);
			assertFalse(mViewModel.mayGenerateDebits);
			assertFalse(mViewModel.mayResetAttempts);
			assertFalse(mViewModel.verificationDebitsVisible);
			
			mSAP.session.user.grantedOperations = oldPermissions;
		}
		
	}
}
