package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.collections.HierarchicalData;
	import mx.utils.ObjectUtil;
	
	import psp.sap.model.Company;
	import psp.sap.model.CompanyEventGroup;
	import psp.sap.model.CompanyEventGroupItem;
	import psp.sap.model.CompanyEventItem;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.CompanyEventLogViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockCompanyService;
	import test.mock.data.CompanyData;

	public class CompanyEventLogViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:CompanyEventLogViewModel; 
		private var mDataService:MockCompanyService
		
		private var mCompany:Company;

		public function CompanyEventLogViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( CompanyEventLogViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			mSAP.setPSPDate(new Date("05/05/2009").getTime());

			mViewModel = new CompanyEventLogViewModel();
			//because of nasty data provider such and such, the view will set the creator to null when the dp (creators) change
			//so let's simulate that
			BindingUtils.bindProperty(this, "simulateCreators", mViewModel, "creators");


			trackedProperties = [
				"companyEventsHierarchicalData",
				"creator",
				"creators",
				"endDate",
				"eventGroups",
				"startDate",
				"canSave"
			];
			
			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompany.companyId = "1234";
			mViewModel.company = mCompany;

			mDataService = mSAP.companyService as MockCompanyService;

			// set the view model to test
			viewModelToTest(mViewModel);
		}

		public function set simulateCreators(value:ArrayCollection):void {
			mViewModel.creator = null;			
		}	

		override public function testViewModel():void {
			mDataService.expectsFindCompanyEvents(mCompany.sourceSystemCd, mCompany.companyId, new Date("05/05/2008"), new Date("05/05/2009"), null, null, false).willReturnAsync(CompanyData.getCompanyEvents());
			
			testActivationSequence();
		}
		


		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertEquals("05/05/2008", mViewModel.startDate);
			assertEquals("05/05/2009", mViewModel.endDate);
			assertNotNull(mViewModel.creators);
			assertNotNull(mViewModel.eventGroups);
			assertNull(mViewModel.creator);
			assertNotNull(mViewModel.companyEventsHierarchicalData);

			//groups should be sorted alpha name
			assertEquals(getGroupAt(0).name, "BandInstruments");
			assertEquals(getGroupAt(1).name, "Fish");
			assertEquals(getGroupAt(2).name, "OrchestraInstruments");			
			//but apparently we don't care about the events

			//events should be sorted by date desc (by default, ADG will sort how it feels)
			assertEquals(getEventAt(0).creatorId, "Rutherford Hayes");
			assertEquals(getEventAt(1).creatorId, "James Polk");

			assertTrue(mViewModel.eventsShowing);

			testBindableProperties();
		}
		
		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();
			
			testBindableProperty(mViewModel, "companyEventsHierarchicalData", new HierarchicalData());
			testBindableProperty(mViewModel, "creator", "Jim");
			testBindableProperty(mViewModel, "creators", new ArrayCollection(["Jim","James"]));
			testBindableProperty(mViewModel, "endDate", new Date("05/05/2009"));
			testBindableProperty(mViewModel, "eventGroups", new ArrayCollection(["1","2"]));
			testBindableProperty(mViewModel, "startDate", new Date("01/01/2009"));

			verifyHasChangedLogic();
		}

		override protected function verifyHasChangedLogic():void {
			//no has changed

			testValidators();
		}

		override protected function testValidators():void {
			assertEquals("can save", true, mViewModel.canSave);

			testDateValidator(mViewModel, "endDate", NaN, NaN);
			testDateValidator(mViewModel, "startDate", NaN, NaN);
			testSAPStartEndDateValidator(mViewModel, "startDate", "endDate");

			testSearchWithNewDates();
		}

		private function testSearchWithNewDates():void {
			mViewModel.startDate = "01/01/2009";
			mViewModel.endDate = "12/31/2009";
			
			mDataService.expectsFindCompanyEvents(mCompany.sourceSystemCd, mCompany.companyId, new Date("01/01/2009"), new Date("12/31/2009"), null, null, false).willReturnAsync(CompanyData.getCompanyEvents());
			
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifySearchWithNewDates);
			mViewModel.search();
		}
		
		
		private function verifySearchWithNewDates(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			testSearchWithGroupsAndCreator();
		}

		private function testSearchWithGroupsAndCreator():void {
			mViewModel.creator = "Rutherford Hayes";
			getGroupAt(2).checked = true;
			getGroupItemAt(2,0).checked = true;
			getGroupItemAt(2,1).checked = true;
			getGroupItemAt(2,2).checked = true;
			
			//return a slight variance just to make sure it's mutable 
			mDataService.expectsFindCompanyEvents(mCompany.sourceSystemCd, mCompany.companyId, new Date("01/01/2009"), new Date("12/31/2009"), "Rutherford Hayes", new ArrayCollection(["Viola", "Cello", "DoubleBass"]), false)
				.willReturnAsync(CompanyData.getCompanyEventsOther());
			
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifySearchWithGroupsAndCreator);
			mViewModel.search();			
		}
		
		private function verifySearchWithGroupsAndCreator(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertEquals(getEventAt(0).creatorId, "Rutherford Hayes");
			assertEquals("just one event in this set", 1, (mViewModel.companyEventsHierarchicalData.source as ArrayCollection).length);
			
			//make sure everything is perserved
			assertEquals("Rutherford Hayes", mViewModel.creator);
			assertTrue(getGroupAt(2).checked);
			assertTrue(getGroupItemAt(2,0).checked);
			assertTrue(getGroupItemAt(2,1).checked);
			assertTrue(getGroupItemAt(2,2).checked);
			
			testRefreshWithInvalidDates();			
		}
	
		private function testRefreshWithInvalidDates():void {
			mViewModel.startDate = "smarch 4th";
			mViewModel.endDate = "Ride Your Giant Non-Sentient Child to Work Day.";
			
			mDataService.expectsFindCompanyEvents(mCompany.sourceSystemCd, mCompany.companyId, new Date("01/01/2009"), new Date("12/31/2009"), "Rutherford Hayes", new ArrayCollection(["Viola", "Cello", "DoubleBass"]), false)
				.willReturnAsync(CompanyData.getCompanyEventsOther());
			
			assertFalse(mViewModel.canSave);
			mViewModel.refresh();
			
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefreshWithInvalidDates);
		}

		private function verifyRefreshWithInvalidDates(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertEquals("01/01/2009", mViewModel.startDate);
			assertEquals("12/31/2009", mViewModel.endDate);
		}
		
		private function getGroupAt(i:int):CompanyEventGroup {
			return mViewModel.eventGroups.getItemAt(i) as CompanyEventGroup;			
		}
		private function getGroupItemAt(i:int, j:int):CompanyEventGroupItem {
			return getGroupAt(i).children.getItemAt(j) as CompanyEventGroupItem;
		}
		
		private function getEventAt(i:int):CompanyEventItem {
			return (mViewModel.companyEventsHierarchicalData.source as ArrayCollection).getItemAt(i) as CompanyEventItem;
		}

	}
}
