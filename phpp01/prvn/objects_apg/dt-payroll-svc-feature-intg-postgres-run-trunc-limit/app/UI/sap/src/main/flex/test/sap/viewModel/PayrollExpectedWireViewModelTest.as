package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.formatters.DateFormatter;
	
	import psp.sap.application.SAP;
	import psp.sap.model.ActionEvent;
	import psp.sap.model.CollectionStageEnum;
	import psp.sap.model.Company;
	import psp.sap.model.PayrollRun;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
	import psp.sap.viewmodel.PayrollExpectedWireViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockPayrollRunService;

	// todo add import statements
	public class PayrollExpectedWireViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollExpectedWireViewModel = new PayrollExpectedWireViewModel();
		private var mDataService:MockPayrollRunService;
		
		private var mCompany:Company;
		private var mPayroll:PayrollRun;
		private var mActionEvent:ActionEvent;

		public function PayrollExpectedWireViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollExpectedWireViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"coCollType",
				"collectionTypes",
				"expectedDate",
				"payrollRun",
				"sendLastEmail",
				"canSave"
			];

			mDataService = mSAP.payrollRunService as MockPayrollRunService;
			
			// setup payroll
			mPayroll = new PayrollRun();
			mPayroll.sourcePayRunId = "payrollid";
			mPayroll.collectionStage = CollectionStageEnum.TERM.code;
			
			// setup company
			mCompany = new Company();
			mCompany.companyId = "123456";
			mCompany.sourceSystemCd = SourceSystemEnum.QBOE.code;
			mViewModel.company = mCompany;		

			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			assertEquals("sendLastEmail", false, mViewModel.sendLastEmail);
			
			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "coCollType", CollectionStageEnum.SECOND); 
			testBindableProperty(mViewModel, "collectionTypes", new Array());
			testBindableProperty(mViewModel, "sendLastEmail", true);

			testValidators();
		}

		override protected function testValidators():void {
			var dateFormat:DateFormatter = new DateFormatter();
			dateFormat.formatString = SAP.instance.configuration.dateFormatShort;
			
			var pspDate:Date = SAP.instance.PSPDate;
			var years:Number = pspDate.fullYear + 1;
			pspDate.setFullYear(years, pspDate.getMonth(), pspDate.getDay());
			
			mViewModel.expectedDate = dateFormat.format(pspDate);
			mViewModel.coCollType = null;
			assertEquals("can save", true, mViewModel.canSave);

			testDateValidator(mViewModel, "expectedDate", 0, 365); 
			testRequiredStringValidator(mViewModel, "expectedDate");

			testSave();
		}

		override protected function testSave():void {
			assertEquals("can save", true, mViewModel.canSave);
			
			var strCollectionCode:String = mViewModel.coCollType.code;
			mDataService.expectsAddWireExpectedDateTransaction(mCompany.sourceSystemCd,
																		    mCompany.companyId, 
																		    mPayroll.sourcePayRunId,
																		    strCollectionCode,
																		    mActionEvent.code,
																		    mViewModel.expectedDateValue,
																		    mViewModel.sendLastEmail);
			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);
			mViewModel.save();
		}

		override protected function verifySave(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);
		}

	}
}
