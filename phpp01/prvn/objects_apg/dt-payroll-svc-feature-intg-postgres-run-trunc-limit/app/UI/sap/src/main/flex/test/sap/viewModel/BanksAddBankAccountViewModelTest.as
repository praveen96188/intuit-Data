package test.sap.viewModel
{
    import flexunit.framework.TestSuite;

    import mx.utils.ObjectUtil;

    import psp.sap.model.Company;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.viewmodel.BanksAddBankAccountViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;

    import test.mock.MockCompanyService;
    import test.mock.data.CompanyData;
    import test.mock.data.UserData;

    public class BanksAddBankAccountViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:BanksAddBankAccountViewModel;
		private var mDataService:MockCompanyService;

        private var mCompany:Company;

		public function BanksAddBankAccountViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( BanksAddBankAccountViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

            mSAP.session.user = UserData.getSuperDuperUser();

            mViewModel = new BanksAddBankAccountViewModel();

            mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompany.companyId = "1234";
			mViewModel.company = mCompany;

			trackedProperties = [
				"accountNumber",
				"accountType",
				"accountTypes",
				"bankName",
				"canAddAgentVerified",
				"createRandomDebits",
				"isAddBankAccount",
				"mayAddAgentVerified",
				"movePending",
				"rtnNumber",
				"sourceBankName",
				"canSave"
			];

			mDataService = MockCompanyService(mSAP.companyService);

			// set the view model to test
			viewModelToTest(mViewModel);
		}

		override public function testViewModel():void {
			//first test editing
            mViewModel.isAddBankAccount = false;

            mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccount());

            testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

            verifyEditBackingProperties();

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "accountNumber", 1111);
			testBindableProperty(mViewModel, "accountType", "blah");			
			testBindableProperty(mViewModel, "bankName", "blah");
			testBindableBoolean(mViewModel, "canAddAgentVerified");
			testBindableBoolean(mViewModel, "createRandomDebits");
			testBindableBoolean(mViewModel, "isAddBankAccount");
			testBindableBoolean(mViewModel, "mayAddAgentVerified");
			testBindableBoolean(mViewModel, "movePending");
			testBindableProperty(mViewModel, "rtnNumber", "12");
			testBindableProperty(mViewModel, "sourceBankName", "QB");

			verifyHasChangedLogic();
		}

		override protected function verifyHasChangedLogic():void {
			testHasChanged(mViewModel, "accountNumber", "12");
			testHasChanged(mViewModel, "accountType", "Savings");

			testHasChanged(mViewModel, "bankName", "blah");
			testHasChanged(mViewModel, "rtnNumber", "15");
			testHasChanged(mViewModel, "sourceBankName", "blah");

			testValidators();
		}

		override protected function testValidators():void {
            mViewModel.bankName = "Bank of Cheese";

			assertEquals("can save", true, mViewModel.canSave);

            //we're in edit mode now so these won't be wired, really (yet)
			testRequiredStringValidator(mViewModel, "accountNumber");
			testRequiredStringValidator(mViewModel, "bankName");
			testRoutingNumberValidator(mViewModel, "rtnNumber");
			testRequiredStringValidator(mViewModel, "rtnNumber");
			testRequiredStringValidator(mViewModel, "sourceBankName");

			testSave();
		}

		override protected function testSave():void {
			//let's save both the bank name (set above) and the qb bank name
            mViewModel.sourceBankName = "Bank of Smelly Cheese";
            assertTrue("can save", mViewModel.canSave);

            mDataService.expectsEditBankAccount(mCompany.sourceSystemCd,
                    mCompany.companyId,
                    CompanyData.getCompanyBankAccount().accountId,
                    "Bank of Smelly Cheese",
                    CompanyData.getCompanyBankAccount().accountNumber,
                    CompanyData.getCompanyBankAccount().routingNumber,
                    CompanyData.getCompanyBankAccount().accountType,
                    "Bank of Cheese");

            mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccount());

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

            verifyEditBackingProperties();
            
		}

        //todo i'd like to be able to test the user editing and then coming back and adding, but it is broke to all hell
        public function testAddBank():void {
            mViewModel.isAddBankAccount = true;

            mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccount());

            addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyAddBankAccountActivated);

            mViewModel.activate();
        }

        private function verifyAddBankAccountActivated(e:ViewModelEvent):void {
            assertTrue(mDataService.errorMessage(), mDataService.success());


            verifyAddBackingProperties();

            trackEventsStart(mViewModel);
            //test add has-changed            
            testHasChanged(mViewModel, "accountNumber", "12");
			testHasChanged(mViewModel, "accountType", "Savings");
			testHasChanged(mViewModel, "bankName", "blah");
			testHasChanged(mViewModel, "rtnNumber", "15");
			testHasChanged(mViewModel, "sourceBankName", "blah");

            //set up data to add
            mViewModel.sourceBankName = "Tank Bank";
            mViewModel.accountNumber = "1252352";
            mViewModel.rtnNumber = "222222226";
            mViewModel.accountType = "Savings";
            mViewModel.bankName = "Bank of Tank";
            mViewModel.createRandomDebits = false;
            mViewModel.movePending = false;

            assertTrue(mViewModel.canSave);

            mDataService.expectsAddBankAccount(mCompany.sourceSystemCd,
                    mCompany.companyId,
                    CompanyData.getCompanyBankAccount().accountId,
                    "Tank Bank",
                    "1252352",
                    "222222226",
                    "Savings",
                    "Bank of Tank",
                    false,
                    true,
                    false);
            mDataService.expectsGetCompanyBankAccount(mCompany.companyId, mCompany.sourceSystemCd).willReturnAsync(CompanyData.getCompanyBankAccount());

            mActionEventHistory.removeAll();
            addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifyAddSave);
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyAddRefresh);

            mViewModel.save();
        }

        protected function verifyAddSave(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);
		}

		protected function verifyAddRefresh(e:ViewModelEvent):void {
            assertTrue(mDataService.errorMessage(), mDataService.success());            
            verifyAddBackingProperties();
		}


        private function verifyEditBackingProperties():void {
            assertEquals(CompanyData.getCompanyBankAccount().routingNumber, mViewModel.rtnNumber);
            assertEquals(CompanyData.getCompanyBankAccount().accountNumber, mViewModel.accountNumber);
            assertEquals(CompanyData.getCompanyBankAccount().accountType, mViewModel.accountType);
            assertEquals(CompanyData.getCompanyBankAccount().bankName, mViewModel.bankName);
            assertEquals(CompanyData.getCompanyBankAccount().sourceBankAccountName, mViewModel.sourceBankName);

            assertEquals(0, ObjectUtil.compare(["Checking", "Savings"], mViewModel.accountTypes));

            assertFalse(mViewModel.movePending);
            assertTrue(mViewModel.createRandomDebits);

            assertTrue(mViewModel.mayAddAgentVerified);
            assertFalse("cannot add agent verified because editing", mViewModel.canAddAgentVerified);
        }

        private function verifyAddBackingProperties():void {
            assertEquals("", mViewModel.rtnNumber);
            assertEquals("", mViewModel.accountNumber);
            assertEquals("Checking", mViewModel.accountType);
            assertEquals("", mViewModel.bankName);
            assertEquals("", mViewModel.sourceBankName);

            assertEquals(0, ObjectUtil.compare(["Checking", "Savings"], mViewModel.accountTypes));

            assertFalse(mViewModel.movePending);
            assertTrue(mViewModel.createRandomDebits);

            assertTrue(mViewModel.mayAddAgentVerified);
            assertTrue(mViewModel.canAddAgentVerified);
        }



        protected function testRoutingNumberValidator(viewModel:Object, propertyName:String):void {
   			var temp:Object = viewModel[propertyName];

   			// set field to an invalid rtn number
   			viewModel[propertyName] = "111111111";
   			assertPropertyChangeEventHistory([{property:propertyName, newValue:"111111111"}, {property:"canSave", newValue:false}], true);
   			assertEquals("can save", false, viewModel.canSave);

   			revertProperty(viewModel, propertyName, temp);
   		}
	}
}
