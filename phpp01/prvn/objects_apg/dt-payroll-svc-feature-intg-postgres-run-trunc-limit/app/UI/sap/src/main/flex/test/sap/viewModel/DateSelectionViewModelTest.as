package test.sap.viewModel
{
    import flexunit.framework.TestSuite;    
    import mx.formatters.DateFormatter;

    import psp.sap.application.SAP;
    import psp.sap.model.DateRangeEnum;
    import psp.sap.viewmodel.DateSelectionViewModel;
    import psp.sap.viewmodel.PayrollConnectionLogViewModel;

    import test.sap.application.SAPTestBase;

    public class DateSelectionViewModelTest extends SAPTestBase
	{
		private var mDateFormatter:DateFormatter = new DateFormatter();
        private var mViewModel:DateSelectionViewModel;
		
		public function DateSelectionViewModelTest(methodName:String=null)
		{
			super(methodName);
		}
		
  		public static function suite():TestSuite {
   			return new TestSuite( DateSelectionViewModelTest );
   		}
   		
   		override public function setUp():void {
   			super.setUp();
   			
   			mDateFormatter.formatString = SAP.instance.configuration.dateFormatShort;   			   		   			   		   			   											
   		}
   		
   		override public function tearDown():void {
   			super.tearDown();
   			if(mViewModel != null)
				trackEventsStop(mViewModel);  							
   		}   		   		
   		
   		public function testDateFunctionality_Step2():void {
   			var page:PayrollConnectionLogViewModel = new PayrollConnectionLogViewModel();
   			mViewModel = new DateSelectionViewModel(page);   			
   			   			   			
   			// set date range select and verify it sets the start and end dates
   			mViewModel.dateRange = DateRangeEnum.TODAY;
   			assertEquals("Start date same", mDateFormatter.format(mSAP.PSPDate), mViewModel.startDate);
   			assertEquals("End date same", mDateFormatter.format(mSAP.PSPDate), mViewModel.endDate);
   			
   			// test switching to custom
   			var tempDate:Date = mSAP.PSPDate;
   			tempDate.time -= SAP.instance.configuration.millisecondsPerDay;
   			mViewModel.startDate = mDateFormatter.format(tempDate);
   			assertEquals("Changed to CUSTOM", DateRangeEnum.CUSTOM, mViewModel.dateRange);
   			
   			// test switch back to reconized range
   			mViewModel.startDate = mDateFormatter.format(mSAP.PSPDate);
   			assertEquals("Changed to TODAY", DateRangeEnum.TODAY, mViewModel.dateRange);
   		}
	}
}