package test.sap
{
    import flexunit.framework.TestSuite;

    import mx.rpc.AsyncToken;
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.remoting.RemoteObject;

    import psp.sap.application.SAP;
    import psp.sap.application.events.SAPEvent;

    import test.sap.application.MRUListTest;
    import test.sap.service.AdministrationServicesTest;
    import test.sap.service.BillingServiceTest;
    import test.sap.service.CompanyServiceSearchTest;
    import test.sap.service.CompanyServiceTest;
    import test.sap.service.PayrollRunServiceTest;
    import test.sap.service.UserServiceTest;
    import test.sap.viewModel.ACHOffloadViewModelTest;
    import test.sap.viewModel.AdministrationManageRolesViewModelTest;
    import test.sap.viewModel.AdministrationSettingsPageEditViewModelTest;
    import test.sap.viewModel.AdministrationSettingsPageViewModelTest;
    import test.sap.viewModel.BanksAddBankAccountViewModelTest;
    import test.sap.viewModel.ChaseReportViewModelTest;
    import test.sap.viewModel.CompanyBankViewModelTest;
    import test.sap.viewModel.CompanyEditSubscriptionStatusViewModelTest;
    import test.sap.viewModel.CompanyEventLogViewModelTest;
    import test.sap.viewModel.CompanyFundingModelHistoryViewModelTest;
    import test.sap.viewModel.DDMUserViewModelTest;
    import test.sap.viewModel.DateSelectionViewModelTest;
    import test.sap.viewModel.FraudDetailViewModelTest;
    import test.sap.viewModel.FraudSearchViewModelTest;
    import test.sap.viewModel.LoginViewModelTest;
    import test.sap.viewModel.PayrollACHDetailViewModelTest;
    import test.sap.viewModel.PayrollACHTransactionsListViewModelTest;
    import test.sap.viewModel.PayrollAddRedebitViewModelTest;
    import test.sap.viewModel.PayrollChangeRedebitViewModelTest;
    import test.sap.viewModel.PayrollRefundRebillViewModelTest;
    import test.sap.viewModel.PayrollTransactionCancelViewModelTest;
    import test.sap.viewModel.PayrollTransactionCreateFeeViewModelTest;
    import test.sap.viewModel.PayrollTransactionHistoryViewModelTest;
    import test.sap.viewModel.PayrollTransactionIssueReissueRefundPageViewModelTest;
    import test.sap.viewModel.PayrollTransactionReverseViewModelTest;
    import test.sap.viewModel.PayrollTransactionsListViewModelTest;
    import test.sap.viewModel.PayrollsListViewModelTest;
    import test.sap.viewModel.UniversalSearchFieldViewModelTest;

    public class SAPTestSuite
	{
		public function SAPTestSuite()
		{
		}
		
		private var callbackFunction:Function;

		// After everything is built, configure the test
		// runner to use the appropriate test suite and
		// kick off the unit tests
		public function prepareForTests(callback:Function):void
		{
			callbackFunction = callback;
			SAP.instance.testMode = true;
			SAP.instance.resetForTesting();
			var dataLoader:RemoteObject = new RemoteObject("dataloaderservice");
			var token:AsyncToken = AsyncToken(dataLoader.runDataLoader("Load User Data Only"));
			token.addResponder(new Responder(onLoadUsersComplete, onLoadUsersFault));
		}
			
		private function onLoadUsersFault(e:FaultEvent):void {
			trace("Error loading user data!");
		}
		
		private function onLoadUsersComplete(e:ResultEvent):void {
			// log in a test user

			SAP.instance.session.login(
				"AutoLogin",
				"admin", 
				onDataLoaderComplete, 
				onDataLoaderFault);
		}

		private function onDataLoaderComplete(e:ResultEvent):void {
			// tests will fail unless all lookup data is loaded first
			SAP.instance.lookupService.addEventListener(SAPEvent.DATA_LOAD_COMPLETED, callbackFunction);
			SAP.instance.lookupService.loadData();
 		}

 		private function onDataLoaderFault(e:FaultEvent):void {
 			trace("Error starting test cases because data loader service failed.");
 		}

		// Creates the test suite to run
		public function createSuite():TestSuite {

 			var ts:TestSuite = new TestSuite();
			
 			// TODO: Add more tests here to test more classes
 			// by calling addTest as often as necessary 						 				 			
 			
			//-----------
 			// viewmodels
 			//-----------
           	ts.addTest( ACHOffloadViewModelTest.suite() );
            ts.addTest( AdministrationManageRolesViewModelTest.suite() );
            ts.addTest( AdministrationSettingsPageEditViewModelTest.suite() );
			ts.addTest( AdministrationSettingsPageViewModelTest.suite() );
			ts.addTest( BanksAddBankAccountViewModelTest.suite() ); 
			// todo rewrite this ts.addTest( BanksCompanyAccountHistoryViewModelTest.suite() );
			// todo rewrite this ts.addTest( BankReturnsSearchExplorerViewModelTest.suite() );
			ts.addTest( ChaseReportViewModelTest.suite() );			
			ts.addTest( CompanyBankViewModelTest.suite() );			
            ts.addTest( CompanyEditSubscriptionStatusViewModelTest.suite() );
			ts.addTest( CompanyEventLogViewModelTest.suite() );
			// todo rewrite this ts.addTest( CompanyInspectorViewModelTest.suite() );
			// todo remove offering from the company and fix this test ts.addTest( CompanyOffersViewModelTest.suite() );
			ts.addTest( CompanyFundingModelHistoryViewModelTest.suite() );
			ts.addTest( DateSelectionViewModelTest.suite() );
 			ts.addTest( DDMUserViewModelTest.suite() );
            ts.addTest( FraudSearchViewModelTest.suite() );
            ts.addTest( FraudDetailViewModelTest.suite() );
 			ts.addTest( LoginViewModelTest.suite() );
 			ts.addTest( PayrollAddRedebitViewModelTest.suite() );
 			ts.addTest( PayrollACHTransactionsListViewModelTest.suite() );
 			ts.addTest( PayrollACHDetailViewModelTest.suite() );
 			ts.addTest( PayrollChangeRedebitViewModelTest.suite() );
 			ts.addTest( PayrollsListViewModelTest.suite() ); 				
			// todo rewrite this test ts.addTest( PayrollRefundFraudEscalationViewModelTest.suite() );
			ts.addTest( PayrollRefundRebillViewModelTest.suite() );
			ts.addTest( PayrollTransactionCancelViewModelTest.suite() );
			ts.addTest( PayrollTransactionCreateFeeViewModelTest.suite() ); 
			ts.addTest( PayrollTransactionHistoryViewModelTest.suite() );               
			ts.addTest( PayrollTransactionReverseViewModelTest.suite() );
			ts.addTest( PayrollTransactionsListViewModelTest.suite() );
			// todo re-write this ts.addTest( PayrollLedgerFeeTransferViewModelTest.suite() );
			ts.addTest( PayrollTransactionIssueReissueRefundPageViewModelTest.suite() );
			ts.addTest( UniversalSearchFieldViewModelTest.suite() );				

			//-----------
 			// tax viewmodels
 			//--------
//            ts.addTest( ActivationsQueueViewModelTest.suite() );
//            ts.addTest( EmployeeDetailViewModelTest.suite() );
//            ts.addTest( EmployeeProfileHistoryViewModelTest.suite() );
//            ts.addTest( GlobalPaymentsExceptionTabViewModelTest.suite() );
//            ts.addTest( GlobalPaymentsExecutedTabViewModelTest.suite() );
//            ts.addTest( GlobalPaymentsPendingTabViewModelTest.suite() );
//            ts.addTest( GlobalPaymentsSuccessfulTabViewModelTest.suite() );
//            ts.addTest( HistoricalLiabilitiesPaymentsViewModelTest.suite() );
//            ts.addTest( TaxCompletedFilingsViewModelTest.suite() );
//            ts.addTest( TaxFilingsUploadViewModelTest.suite() );
//            ts.addTest( TaxPaymentsSummaryViewModelTest.suite() );
//            ts.addTest( TaxPaymentsDetailViewModelTest.suite() );
//            ts.addTest( TaxLedgerDetailViewModelTest.suite() );
//            ts.addTest( TaxLedgerViewModelTest.suite() );
// 			  ts.addTest( NonACHTaxPaymentsClearingViewModelTest.suite() );
// 			  ts.addTest( PaymentMadeByCheckWireViewModelTest.suite() );
// 		      ts.addTest( PayrollAdjustmentsSummaryViewModelTest.suite() );            

			//-----------
			// application
			//-----------
			// rewrite this test without test.data.CompanyService ts.addTest( CollectionViewModelTest.suite() );
            ts.addTest( MRUListTest.suite() );

            //-----------
			// services
			//-----------
		 	ts.addTest( BillingServiceTest.suite() );
			ts.addTest( CompanyServiceSearchTest.suite() );
			ts.addTest( CompanyServiceTest.suite() );
			ts.addTest( PayrollRunServiceTest.suite() );
			ts.addTest( AdministrationServicesTest.suite() );
			ts.addTest( UserServiceTest.suite() );

            //-----------
			// tax services
			//-----------
//            ts.addTest( TaxServiceTest.suite() );
            
            // special unit test to close the flex cover app 
            // ** keep this as the last test it flushes the coverage 
            // ** data and closes the flex cover viewer
            ts.addTest( FlexCoverClose.suite() );                        					
			
			
            return ts;
 		}
	}
}
