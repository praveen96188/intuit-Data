package test.sap.viewModel
{
    import flexunit.framework.TestSuite;

    import mx.rpc.events.ResultEvent;

    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.Company;
    import psp.sap.model.CompanyBankAccountHistory;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.viewmodel.BanksCompanyAccountHistoryViewModel;
    import psp.sap.viewmodel.CompanyInspectorViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;

    import test.sap.application.SAPTestBase;

    public class BanksCompanyAccountHistoryViewModelTest extends SAPTestBase
	{
		private const COMPANY_ID:String = "1234567";		
		
		public function BanksCompanyAccountHistoryViewModelTest(methodName:String=null)
		{
			super(methodName);
		}
		
  		public static function suite():TestSuite {
   			return new TestSuite( BanksCompanyAccountHistoryViewModelTest );
   		}
   		
   		private var mViewModel:BanksCompanyAccountHistoryViewModel;

   		
   		override public function setUp():void {
   			super.setUp();   			   			
   			this.asyncTimeout *= 2;   		   					 							
   		}
   		
   		override public function tearDown():void {
   			super.tearDown();
			trackEventsStop(mViewModel);  							
   		}
   		
   		/**                 
        * START
        */
        public function testBanksCompanyAccountHistoryViewModel():void {
        	runDataLoader("Company :: Create Basic Data", testLoadModelData_Step2, 10);
        }   		   		
        
        private function testLoadModelData_Step2(e:ResultEvent):void {
        	login(testLoadModelData_Step3);
        }
   		
   		private function testLoadModelData_Step3(e:ResultEvent):void {
   			var inspectorViewModel:CompanyInspectorViewModel = new CompanyInspectorViewModel();
			var company:Company = new Company();
			company.companyId = COMPANY_ID;
			company.sourceSystemCd = SourceSystemEnum.QBOE.code;
			inspectorViewModel.company = company;
			
   			mViewModel = inspectorViewModel.findPart(CompanyInspectorPageEnum.BANKS_COMPANY_ACCOUNT_HISTORY) as BanksCompanyAccountHistoryViewModel;
			trackEventsStart(mViewModel);
			
   			addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyCompanyAccountHistoryLoaded);
   			mViewModel.activate();   			
   		}	
   		
   		private function verifyCompanyAccountHistoryLoaded(e:ViewModelEvent):void {   			
   			// company bank accounts should be loaded
   			assertNotNull(mViewModel.bankAccounts);
   			
   			// this company has one account
   			assertEquals("Bank Accounts", 1, mViewModel.bankAccounts.length);
   			
   			// check the bank info
   			var bankAccountHistory:CompanyBankAccountHistory = mViewModel.bankAccounts.getItemAt(0) as CompanyBankAccountHistory; 
   			assertNotNull(bankAccountHistory.accountNumber);   			
   			assertNotNull(bankAccountHistory.routingNumber);
   			assertNotNull(bankAccountHistory.accountType);
   			assertNotNull(bankAccountHistory.sourceBankAccountName);
   			assertNotNull(bankAccountHistory.bankName);
   			assertNotNull(bankAccountHistory.bankAccountStatus);
   			
   			// should be 3 property audits
   			assertEquals("Property audit details", 1, bankAccountHistory.propertyAudit.length);   						   			   			   			   			   			   			   	
   		}   		   		   		   			   		   		   		   			   		   		
	}
}