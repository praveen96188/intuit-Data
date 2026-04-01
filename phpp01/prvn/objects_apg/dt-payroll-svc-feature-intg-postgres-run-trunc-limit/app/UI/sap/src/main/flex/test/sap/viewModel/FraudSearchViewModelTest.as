package test.sap.viewModel
{
	import flexunit.framework.TestSuite;

    import mx.collections.ArrayCollection;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.ViewModelActivationStateEnum;
    import psp.sap.model.Company;
    import psp.sap.model.FraudEvent;
    import psp.sap.model.FraudIndicatorEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.FraudSearchViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockCompanyService;
	import test.mock.data.CompanyData;

	public class FraudSearchViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:FraudSearchViewModel = new FraudSearchViewModel();
		private var mDataService:MockCompanyService;

		public function FraudSearchViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( FraudSearchViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"ein",
				"fraudIndicator",
				"fraudIndicators",
				"payrollAmount",
				"searchResults",
				"selectedCompany",
				"selectedFraudEvent",
				"selectedIndex",
				"canSave"
			];

			mDataService = mSAP.companyService as MockCompanyService;

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// no data load for this vm
			testActivationSequenceSpecial();
		}

        protected function testActivationSequenceSpecial():void {
   			trackEventsStart(mViewModel);

   			addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyActivationSequence);
   			mViewModel.activate();
   		}

   		private function verifyActivationSequence(e:ViewModelEvent):void {
   			assertEventHistory([ViewModelEvent.ACTIVATED]);

   			assertEquals("activation state", ViewModelActivationStateEnum.ACTIVATED, mViewModel.activationState);

   			verifyModelDataSetup();
   		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("selectedFraudEvent", null, mViewModel.selectedFraudEvent);
			assertEquals("fraudIndicator", FraudIndicatorEnum.ALL, mViewModel.fraudIndicator);
			assertEquals("ein", "", mViewModel.ein);
			assertEquals("startDate", "", mViewModel.dateSelectionViewModel.startDate);
			assertEquals("endDate", "", mViewModel.dateSelectionViewModel.endDate);
			assertEquals("payrollAmount", "", mViewModel.payrollAmount);
			assertEquals("searchResults", 0, mViewModel.searchResults.length);

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "ein", "blah");
			testBindableProperty(mViewModel, "fraudIndicator", new FraudIndicatorEnum("blah"));
			testBindableProperty(mViewModel, "payrollAmount", "10.00");
			testBindableProperty(mViewModel, "searchResults", new ArrayCollection());
			testBindableProperty(mViewModel, "selectedCompany", new Company());
			testBindableProperty(mViewModel, "selectedFraudEvent", new FraudEvent());
			testBindableProperty(mViewModel, "selectedIndex", 5);

			verifyHasChangedLogic();
		}

		override protected function verifyHasChangedLogic():void {
			testValidators();
		}

		override protected function testValidators():void {
			assertTrue("can save", mViewModel.canSave);

			localTestDateValidator(mViewModel, mViewModel.dateSelectionViewModel, "startDate", NaN, NaN);
            localTestDateValidator(mViewModel, mViewModel.dateSelectionViewModel, "endDate", NaN, NaN);
            localSAPStartEndDateValidator(mViewModel, mViewModel.dateSelectionViewModel, "startDate", "endDate");
			testNumberValidator(mViewModel, "payrollAmount");

			testSearch();
		}

		protected function testSearch():void {
			assertTrue("can save", mViewModel.canSave);

			mDataService.expectsFindCompanyFraudEvents(null,
								                                    null,
								                                    -1,
								                                    null,
								                                    null,
                                                                    null).willReturnAsync(CompanyData.getFraudEvents());
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifySearch);

			mViewModel.searchFraud();
		}

		protected function verifySearch(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);

            assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("searchResults", CompanyData.getFraudEvents().length, mViewModel.searchResults.length);

			// check search results order
            var i:int = mViewModel.searchResults.length;
			for each(var fraudEvent:FraudEvent in mViewModel.searchResults) {
				assertEquals("searchResults", i, fraudEvent.payrollAmount);
                i--;
			}

            testEinSearch();
		}

        private function testEinSearch():void {
            mViewModel.ein = "99-9999999";
            assertEquals("ein", "999999999", mViewModel.ein);

            mDataService.expectsFindCompanyFraudEvents("999999999",
								                                    null,
								                                    -1,
								                                    null,
                                                                    null,
								                                    null).willReturn(CompanyData.getFraudEvents());
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyEinSearch);
            mViewModel.searchFraud();
        }

        private function verifyEinSearch(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);

            assertTrue(mDataService.errorMessage(), mDataService.success());

            testPayrollIndicatorSearch();
		}

        private function testPayrollIndicatorSearch():void {
            var startDate:String = "10/10/2010";
            var endDate:String = "10/12/2010";

            mViewModel.fraudIndicator = FraudIndicatorEnum.PAYROLL;
            assertEquals("fraudIndicator", FraudIndicatorEnum.PAYROLL, mViewModel.fraudIndicator);
            mViewModel.dateSelectionViewModel.startDate = startDate;
            assertEquals("startDate", startDate, mViewModel.dateSelectionViewModel.startDate);
            mViewModel.dateSelectionViewModel.endDate = endDate;
            assertEquals("endDate", endDate, mViewModel.dateSelectionViewModel.endDate);
            mViewModel.payrollAmount = "11.01";
            assertEquals("payrollAmount", "11.01", mViewModel.payrollAmount);

            mDataService.expectsFindCompanyFraudEvents("999999999",
								                                    FraudIndicatorEnum.PAYROLL.code,
								                                    11.01,
								                                    new Date(startDate),
								                                    new Date(endDate),
                                                                    null).willReturn(CompanyData.getFraudEvents());
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyPayrollIndicatorSearch);
            mViewModel.searchFraud();
        }

        private function verifyPayrollIndicatorSearch(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);

            assertTrue(mDataService.errorMessage(), mDataService.success());
            
            testSignUpIndicatorSearch();
		}

        private function testSignUpIndicatorSearch():void {
            mViewModel.fraudIndicator = FraudIndicatorEnum.SIGN_UP;
            assertEquals("fraudIndicator", FraudIndicatorEnum.SIGN_UP, mViewModel.fraudIndicator);

            mDataService.expectsFindCompanyFraudEvents("999999999",
								                                    FraudIndicatorEnum.SIGN_UP.code,
								                                    -1,
								                                    null,
								                                    null,
                                                                    null).willReturn(CompanyData.getFraudEvents());
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifySignUpIndicatorSearch);
            mViewModel.searchFraud();
        }

        private function verifySignUpIndicatorSearch(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);

            assertTrue(mDataService.errorMessage(), mDataService.success());

            testF9Refresh();
		}

        private function testF9Refresh():void {
            var startDate:String = "08/10/2010";
            var endDate:String = "08/12/2010";

            mViewModel.ein = "777777777";
            assertEquals("ein", "777777777", mViewModel.ein);
            mViewModel.fraudIndicator = FraudIndicatorEnum.PAYROLL;
            assertEquals("fraudIndicator", FraudIndicatorEnum.PAYROLL, mViewModel.fraudIndicator);
            mViewModel.dateSelectionViewModel.startDate = startDate;
            assertEquals("startDate", startDate, mViewModel.dateSelectionViewModel.startDate);
            mViewModel.dateSelectionViewModel.endDate = endDate;
            assertEquals("endDate", endDate, mViewModel.dateSelectionViewModel.endDate);
            mViewModel.payrollAmount = "15.01";
            assertEquals("payrollAmount", "15.01", mViewModel.payrollAmount);

            mDataService.expectsFindCompanyFraudEvents("999999999",
								                                    FraudIndicatorEnum.SIGN_UP.code,
								                                    -1,
								                                    null,
								                                    null,
                                                                    null).willReturn(CompanyData.getFraudEvents());
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyF9Refresh);
            mViewModel.refresh();
        }

        private function verifyF9Refresh(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);

            assertTrue(mDataService.errorMessage(), mDataService.success());

            assertEquals("ein", "999999999", mViewModel.ein);
            assertEquals("fraudIndicator", FraudIndicatorEnum.SIGN_UP, mViewModel.fraudIndicator);
            assertEquals("startDate", "10/10/2010", mViewModel.dateSelectionViewModel.startDate);
            assertEquals("endDate", "10/12/2010", mViewModel.dateSelectionViewModel.endDate);
            assertEquals("payrollAmount", "11.01", mViewModel.payrollAmount);
		}

        private function localTestDateValidator(viewModel:Object, propertyObject:Object, propertyName:String, daysBefore:Number=NaN, daysAfter:Number=NaN):void {
   			var temp:Object = propertyObject[propertyName];

   			// test min
   			if(!isNaN(daysBefore)){
   				var timmToSubtract:Number = (daysBefore+1)*SAP.instance.configuration.millisecondsPerDay;
   				var minDate:Date = SAP.instance.PSPDate;
   				minDate.setTime(minDate.getTime() - timmToSubtract);
   				propertyObject[propertyName] = minDate;
   				assertEquals("can save", false, viewModel.canSave);

	   			propertyObject[propertyName] = temp;
   			}

   			// test max
   			if(!isNaN(daysAfter)){
   				var timmToAdd:Number = (daysAfter+1)*SAP.instance.configuration.millisecondsPerDay;
   				var maxDate:Date = SAP.instance.PSPDate;
   				maxDate.setTime(maxDate.getTime() + timmToAdd);
   				propertyObject[propertyName] = maxDate;
   				assertEquals("can save", false, viewModel.canSave);

	   			propertyObject[propertyName] = temp;
   			}

			// test default
   			propertyObject[propertyName] = "blah"; // not a valid date format
   			assertEquals("can save", false, viewModel.canSave);
   			propertyObject[propertyName] = temp;
   		}

        private function localSAPStartEndDateValidator(viewModel:Object, propertyObject:Object, startDatePropertyName:String, endDatePropertyName:String):void {
   			var checkDaysBefore:Number = 5;

			var tempStartDate:Object = propertyObject[startDatePropertyName];
			var tempEndDate:Object = propertyObject[endDatePropertyName];

   			var startDate:Date = SAP.instance.PSPDate;
   			startDate.setTime(startDate.getTime() - ((checkDaysBefore)*SAP.instance.configuration.millisecondsPerDay));
   			var endDate:Date = SAP.instance.PSPDate;
   			var startDateString:String = dateFormatter.format(startDate);
   			var endDateString:String = dateFormatter.format(endDate);

   			propertyObject[startDatePropertyName] = startDateString;
   			propertyObject[endDatePropertyName] = endDateString;
   			assertEquals("can save", true, viewModel.canSave);
   			propertyObject[startDatePropertyName] = endDateString;
   			propertyObject[endDatePropertyName] = startDateString;
   			assertEquals("can save", false, viewModel.canSave);
   			propertyObject[startDatePropertyName] = endDateString;
   			propertyObject[endDatePropertyName] = endDateString;
   			assertEquals("can save", true, viewModel.canSave);

   			if (propertyObject[startDatePropertyName] != tempStartDate) {
   				propertyObject[startDatePropertyName] = tempStartDate;
   			}
   			//this may have been set to the same value as before
   			if (propertyObject[endDatePropertyName] != tempEndDate) {
   				propertyObject[endDatePropertyName] = tempEndDate;
   			}
   		}

	}
}
