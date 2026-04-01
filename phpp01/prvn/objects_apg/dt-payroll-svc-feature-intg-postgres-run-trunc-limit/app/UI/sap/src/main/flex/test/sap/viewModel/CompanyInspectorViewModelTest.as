package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import intuit.sbd.flex.framework.model.events.ModelEvent;
	
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.model.CompanyStrike;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.CompanySummaryBannerViewModel;
	
	import test.sap.application.SAPCompanyLoadBase;

	public class CompanyInspectorViewModelTest extends SAPCompanyLoadBase
	{
		public function CompanyInspectorViewModelTest(methodName:String=null)
		{
			super(methodName);
		}
		
		static public function suite():TestSuite {
			return new TestSuite(CompanyInspectorViewModelTest);
		}
		
		private var mCompanyInspector:CompanyInspectorViewModel;
		
		override public function setUp():void {
			super.setUp();
			mCompanyInspector = new CompanyInspectorViewModel();
		}
		
		override public function tearDown():void {
			super.tearDown();

			mCompanyInspector = null;
		}
		
		/**
		 * testInitialPropertyValues -- START
		 */
		public function testInitialPropertyValues():void {
			runDataLoader("Company :: Create Basic Data", testInitialPropertyValues_Step2, 5);
		}
		
		private function testInitialPropertyValues_Step2(e:ResultEvent):void {
			login(testInitialPropertyValues_Step3);
		}
		
		private function testInitialPropertyValues_Step3(e:ResultEvent):void { 
			doCompanySearch(testInitialPropertyValues_Step4);
		}
		
		private function testInitialPropertyValues_Step4():void {
			mCompanyInspector.company = mCompany;
			assertEquals("applicationItem", mCompany, mCompanyInspector.applicationItem); 
			assertEquals("company equals applicationItem", mCompanyInspector.applicationItem,  mCompanyInspector.company);
			
//			assertTrue("active page",  mCompanyInspector.activePage is CompanyInformationViewModel);
//			assertFalse("isDirty", mCompanyInspector.isDirty);
			
			var expectedLabel:String = mCompany.legalName;
			assertEquals("label", expectedLabel, mCompanyInspector.label);			
		}
		/**
		 * testInitialPropertyValues -- END
		 */


//		
//		public function testFormPages():void {
//			// adding a page
//			// removing a page that is active
//			// adding a duplicate page (page with the same name)
//		}
//		
//		public function testActivePage():void {
//			// activate a page by setting member
//			// activate a page by calling activate on page
//			// calling activate on a page that isn't part of pages collection			
//		}
//		
//		public function testIsDirty():void {
//			// starts not dirty
//			// making a change to model -- test if dirty
//			// revert model change, test if not dirty
//			// making a change to viewModel, test if dirty
//			// revert viewModel change, test if not dirty
//		}
//		
//		public function testLabel():void {
//			
//		}
	}
}
