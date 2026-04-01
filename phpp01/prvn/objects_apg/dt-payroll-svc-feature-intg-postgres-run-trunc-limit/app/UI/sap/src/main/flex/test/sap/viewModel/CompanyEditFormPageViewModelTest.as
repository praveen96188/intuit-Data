package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.application.events.SAPEvent;
	import psp.sap.model.Company;
	import psp.sap.model.CompanyStrike;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	import psp.sap.viewmodel.CompanyEditDDLimitsViewModel;
	import psp.sap.viewmodel.CompanyEditFundingModelViewModel;
	import psp.sap.viewmodel.CompanyManageStrikesViewModel;
	
	
	import test.sap.application.SAPTestBase;

	public class CompanyEditFormPageViewModelTest extends SAPTestBase
	{
		public function CompanyEditFormPageViewModelTest(methodName:String=null)
		{
			super(methodName);
		}
		
		static public function suite():TestSuite {
			return new TestSuite(CompanyEditFormPageViewModelTest);
		}
		
		private var mCompanyInspector:CompanyInspectorViewModel;
		private var mCompany:Company;
		
		override public function setUp():void {
			super.setUp();
			this.asyncTimeout *= 4;			 				
			
			/*var testUser:User = new User();
			testUser.corpId="12345678";
			testUser.firstName="Testy";
			testUser.lastName="Jones";
			testUser.globalUserId=12345678;
			testUser.userName="TJones";
			SAP.instance.session.user = testUser;*/
		}
		
		override public function tearDown():void {
			super.tearDown();
			
			mCompanyInspector = null;
		}
		
		public function testCompanyEditFormPages():void {
			runDataLoader("Company :: Create Basic Data", testCompanyEditFormPages_Step2, 4);
		}
		
		private function testCompanyEditFormPages_Step2(e:ResultEvent):void {
			login(testCompanyEditFormPages_Step3);
		} 
		
		private function testCompanyEditFormPages_Step3(e:ResultEvent):void {	
			// set the date
			changePSPDate(new Date(), getTestResponder(testCompanyEditFormPages_Step4));
		}
		
		private function testCompanyEditFormPages_Step4(e:ResultEvent):void {
			mSAP.companyService.findCompany(
					"QBOE",
                    "1234567",					
					getTestResponder(testCompanyEditFormPages_Step5));
		}
		
		private function testCompanyEditFormPages_Step5(e:ResultEvent):void {
			assertTrue(e.result != null);
        	assertTrue(e.result is Company);
        	mCompanyInspector = new CompanyInspectorViewModel(true);
        	mCompanyInspector.company = Company(e.result);
        	// to make sure the company is loaded before the other
        	// test are run this function calls the other tests
        	testDDLimitEditsViewModel_Step1();
  		}
  		        
		private function testDDLimitEditsViewModel_Step1():void {
			var ddLimitsPage:CompanyEditDDLimitsViewModel = 
				CompanyEditDDLimitsViewModel(mCompanyInspector.getPage(CompanyInspectorPageEnum.DIRECT_DEPOSIT_LIMTS));

			//addAsyncVerifier(ddLimitsPage, ViewModelEvent.ACTIVATED, myTestFunc);
			ddLimitsPage.addEventListener(ViewModelEvent.ACTIVATED, addAsync(myTestFunc, this.asyncTimeout)); 
			//ddLimitsPage.addEventListener(ViewModelEvent.ACTIVATED, myTestFunc);
			ddLimitsPage.activate();						
		}
		
		private function myTestFunc(e:ViewModelEvent):void {
			trace("Executed!!");
		}
		
		private function testDDLimitEditsViewModel_Step2(e:ViewModelEvent):void {	
		  	var ddLimitsPage:CompanyEditDDLimitsViewModel = CompanyEditDDLimitsViewModel(mCompanyInspector.activePage);
			this.trackedProperties = ["canSave"];
			this.trackPropertyChangeEventsStart(ddLimitsPage);
			
		  	assertEquals("canSave", ddLimitsPage.canSave, false);

		  	ddLimitsPage.perEmployeeLimit = "2000";
		  	ddLimitsPage.perPayrollLimit = "4000";
		  	
		  	assertPropertyChangeEventHistory([{property:"canSave", newValue:true}], true);
		  	assertEquals("canSave", ddLimitsPage.canSave, true);
		  	
		  	ddLimitsPage.perEmployeeLimit = mCompanyInspector.company.directDepositService.overrideEmployeeLimitAmount.toString();
		  	ddLimitsPage.perPayrollLimit = mCompanyInspector.company.directDepositService.overrideCompanyLimitAmount.toString();
		  	
		  	assertPropertyChangeEventHistory([{property:"canSave", newValue:false}], true);
		  	assertEquals("canSave", ddLimitsPage.canSave, false);

        	testManageStrikesViewModel_Step1();
		}

		private function testManageStrikesViewModel_Step1():void {			
			var strikesViewModel:CompanyManageStrikesViewModel = 
				CompanyManageStrikesViewModel(mCompanyInspector.getPage(CompanyInspectorPageEnum.STRIKES));
			addAsyncVerifier(strikesViewModel, ViewModelEvent.ACTIVATED, testManageStrikesViewModel_Step2);
			strikesViewModel.activate();
		}
		
		private function testManageStrikesViewModel_Step2(e:ViewModelEvent):void {			
		  	var strikesViewModel:CompanyManageStrikesViewModel = CompanyManageStrikesViewModel(mCompanyInspector.activePage);
			this.trackedProperties = ["canSave"];
			this.trackPropertyChangeEventsStart(strikesViewModel);
			strikesViewModel.strikeDate = dateFormatter.format(new Date());
			
			strikesViewModel.canAddStrikes = true;
			strikesViewModel.canCancelStrikes = true;
		  	
		  	assertEquals("canSave", strikesViewModel.canSave, false);

		  	strikesViewModel.strikeReason = "I have some reason";
		  	
		  	assertPropertyChangeEventHistory([{property:"canSave", newValue:true}], true);
		  	assertEquals("canSave", strikesViewModel.canSave, true);
		  	
		  	strikesViewModel.strikeReason = "";
		  	
		  	assertPropertyChangeEventHistory([{property:"canSave", newValue:false}], true);
		  	assertEquals("canSave", strikesViewModel.canSave, false);
		  	
		  	strikesViewModel.strikeReason = "I have some reason";

		  	assertPropertyChangeEventHistory([{property:"canSave", newValue:true}], true);
		  	assertEquals("canSave", strikesViewModel.canSave, true);

			addAsyncVerifier(strikesViewModel, SAPEvent.DATA_LOAD_COMPLETED, verifyStrikeSaved);
		  	strikesViewModel.save();		  			  			 		
		}
		
		public function verifyStrikeSaved(e:SAPEvent):void {
		  	var strikesViewModel:CompanyManageStrikesViewModel = CompanyManageStrikesViewModel(mCompanyInspector.activePage);
			assertPropertyChangeEventHistory([{property:"canSave", newValue:false}], true);
		  	assertEquals("canSave", strikesViewModel.canSave, false);
		  	assertEquals("strikeReason", strikesViewModel.strikeReason, "");
		  	
		  	var myNewStrike:CompanyStrike = strikesViewModel.strikes.getItemAt(0) as CompanyStrike;
		  	
		  	assertEquals("cancel flag", myNewStrike.cancelled, false);
			assertTrue("cancelled by userId", myNewStrike.cancelledByUserId == null || myNewStrike.cancelledByUserId == "");
			assertTrue("newStrike flag", myNewStrike.newStrike, true);
						
			addAsyncVerifier(strikesViewModel, SAPEvent.DATA_LOAD_COMPLETED, verifyStrikeCancelled); 
		  	strikesViewModel.cancelStrike(myNewStrike);		  			  	
		}
		
		public function verifyStrikeCancelled(e:SAPEvent):void {
		  	var strikesViewModel:CompanyManageStrikesViewModel = CompanyManageStrikesViewModel(mCompanyInspector.activePage);
			var myCancelledStrike:CompanyStrike = strikesViewModel.strikes.getItemAt(0) as CompanyStrike;
			assertEquals("cancel flag", myCancelledStrike.cancelled, true);
			assertTrue("cancelled by userId", myCancelledStrike.cancelledByUserId == "TJones");
			
			testNotesViewModel_Step1();
		}
		
		
		
		private function testEditFundingModelViewModel_Step1():void {
			var fundingModelPage:CompanyEditFundingModelViewModel = 
				CompanyEditFundingModelViewModel(mCompanyInspector.getPage(CompanyInspectorPageEnum.FUNDING_MODEL));
			addAsyncVerifier(fundingModelPage, ViewModelEvent.ACTIVATED, testEditFundingModelViewModel_Step2);
			fundingModelPage.activate();		
		}
		
		private function testEditFundingModelViewModel_Step2(e:ViewModelEvent):void {
		  	var fundingModelPage:CompanyEditFundingModelViewModel = CompanyEditFundingModelViewModel(mCompanyInspector.activePage);
			this.trackedProperties = ["canSave"];
			this.trackPropertyChangeEventsStart(fundingModelPage);
		  	
		  	assertEquals("canSave", fundingModelPage.canSave, false);
		  	
		  	// just change the funding model to the other one (from 5D to 2D or 2D to 5D)
		  	fundingModelPage.fundingModel = (fundingModelPage.fundingModel == "2D" ? "5D" : "2D");
		  	
		  	assertPropertyChangeEventHistory([{property:"canSave", newValue:true}], true);
		  	assertEquals("canSave", fundingModelPage.canSave, true);
		  			  	 
		  	// change it back
		  	fundingModelPage.fundingModel = (fundingModelPage.fundingModel == "2D" ? "5D" : "2D");

		  	assertPropertyChangeEventHistory([{property:"canSave", newValue:false}], true);
		  	assertEquals("canSave", fundingModelPage.canSave, false);
		}				
	}
}
