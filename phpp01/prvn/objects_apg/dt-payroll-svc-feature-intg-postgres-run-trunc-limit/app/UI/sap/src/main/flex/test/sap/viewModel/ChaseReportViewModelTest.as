package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.application.SAP;
	import psp.sap.model.Company;
	import psp.sap.model.DateRangeEnum;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.ChaseReportViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockPayrollRunService;
	import test.mock.data.CompanyData;
	
	public class ChaseReportViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:ChaseReportViewModel;
		private var mDataService:MockPayrollRunService;
		private var mCompany:Company;

		public function ChaseReportViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( ChaseReportViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"canPrint",
				"dateSelectionViewModel",
				"reports",
				"canSave"
			];


			mDataService = mSAP.payrollRunService as MockPayrollRunService;
            mSAP.setPSPDate(new Date("10/10/2009").getTime());
            mViewModel = new ChaseReportViewModel();

			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompany.companyId = "1234";
			mViewModel.company = mCompany;			

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mDataService.expectsFindChaseReportForDateRange(mCompany.sourceSystemCd, mCompany.companyId, new Date("10/03/2009"), new Date("10/10/2009")).willReturnAsync(CompanyData.getChaseReports());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("reports",	CompanyData.getChaseReports().length, mViewModel.reports.length);
			assertEquals("canPrint", true, mViewModel.canPrint);
			assertEquals("startDateValue", DateRangeEnum.LAST_7_DAYS.startDate, mViewModel.dateSelectionViewModel.startDate);
			assertEquals("endDateValue", DateRangeEnum.LAST_7_DAYS.endDate, mViewModel.dateSelectionViewModel.endDate);			

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "canPrint", false);
			testBindableProperty(mViewModel, "reports", new ArrayCollection());

			verifyHasChangedLogic();
		}		

		override protected function testValidators():void {
			assertEquals("can save", true, mViewModel.canSave);
			
			localTestDateValidator(mViewModel, mViewModel.dateSelectionViewModel, "endDate", NaN, NaN); 
			localTestDateValidator(mViewModel, mViewModel.dateSelectionViewModel, "startDate", NaN, NaN);
			clearPropertyChangeEventHistory();
			testRefresh();
		}

		private function testRefresh():void {
			assertEquals("can save", true, mViewModel.canSave);

			mDataService.expectsFindChaseReportForDateRange(mCompany.sourceSystemCd, mCompany.companyId, new Date("10/03/2009"), new Date("10/10/2009")).willReturnAsync(CompanyData.getChaseReports());
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);

			mViewModel.searchForReports();
		}		

		override protected function verifyRefresh(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);
            assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("reports",	CompanyData.getChaseReports().length, mViewModel.reports.length);
			assertEquals("canPrint", true, mViewModel.canPrint);
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

	}
}
