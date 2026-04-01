package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.application.enums.CompanyInspectorTopicEnum;
	import psp.sap.model.BillingTransaction;
	import psp.sap.model.Company;
	import psp.sap.model.PayrollRun;
	import psp.sap.model.SettlementTypeEnum;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
	import psp.sap.viewmodel.PayrollRefundFraudEscalationViewModel;
	
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.data.MockCompanyService;
	import test.sap.application.SAPTestBase;
	
	
	public class PayrollRefundFraudEscalationViewModelTest extends SAPTestBase
	{
		private var mViewModel:PayrollRefundFraudEscalationViewModel;
		private const PAYROLL_RUN:String = "BatchId01";
		private const COMPANY_ID:String = "123272727";
		
		public function PayrollRefundFraudEscalationViewModelTest(methodName:String=null)
		{
			super(methodName);
		}	
		
		public static function suite() : TestSuite {
			return new TestSuite( PayrollRefundFraudEscalationViewModelTest );            
        }		
		
		override public function setUp():void {
   			super.setUp();   			
   			this.asyncTimeout *= 5;
   			trackedEvents = [	   								
   								ViewModelEvent.SAVE_SUCCEEDED,
   								ViewModelEvent.MODEL_DATA_SETUP_COMPLETED  								
   							];			 							
   		}
   		
   		override public function tearDown():void {
   			super.tearDown();
   			if(mViewModel != null)
				trackEventsStop(mViewModel);  							
   		}
   		
		public function testLoadPayrollInfo():void {
			runDataLoader("Payroll :: Refund ER for Fraud or Escalation Data", testLoadPayrollInfo_Step2,10);
		}
		
		private function testLoadPayrollInfo_Step2(e:ResultEvent):void {
			login(testLoadPayrollInfo_Step3);
		}				
				
		private function testLoadPayrollInfo_Step3(e:ResultEvent):void {			
		
			var mCompanyInspector:CompanyInspectorViewModel = createTestCompanyInspector();
			var company:Company = MockCompanyService.DB.getCompanyAt(0);
			
			company.companyId = COMPANY_ID;
			company.sourceSystemCd = SourceSystemEnum.QBDT.code;
			mCompanyInspector.company = company;
			
			mViewModel = PayrollRefundFraudEscalationViewModel(mCompanyInspector.findPart(CompanyInspectorPageEnum.PAYROLL_REFUND_FRAUD_ESCALATION));
   			
			var myPayrollRun:PayrollRun = new PayrollRun();
			myPayrollRun.sourcePayRunId = PAYROLL_RUN;
			
			trackEventsStart(mViewModel);															

   			addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyAmountsLoaded);
   			mViewModel.activate();

		}
		
	   
	   private function verifyAmountsLoaded(e:ViewModelEvent):void { 
	   		//fail('this is generating an error that is not being caught and somehow it still passes this verify');
	   	
	   	
	   		mViewModel.settlementType = SettlementTypeEnum.WIRE;
	   		// should load the values and can save should be true
	   		assertEquals("Can Save", true, mViewModel.canSave);
	   		
	   		// clear all
	   		mViewModel.clearAll();
	   		// should set everything to zero and can save is false
	   		assertEquals("Total Value", 0.00, mViewModel.totalValue);
	   		assertEquals("Can Save", false, mViewModel.canSave);
	   		
	   		// check calculations
	   		mViewModel.payrollAmount = "1.00";
	   		assertEquals("Total Value", 1.00, mViewModel.totalValue);
	   		assertEquals("Can Save", true, mViewModel.canSave);
	   		
	   		// input invalid characters	   			   		   		
	   		mViewModel.payrollAmount = "abc";
	   		assertEquals("Can Save", false, mViewModel.canSave);
	   		mViewModel.payrollAmount = "1.00";
	   		assertEquals("Can Save", true, mViewModel.canSave);
	   		
	   		// check same for fee
	   		var feeAmount:BillingTransaction = mViewModel.txnFeeAmountList.getItemAt(0) as BillingTransaction;
	   		feeAmount.financialReturnAmountString = "abc";
	   		assertEquals("Can Save", false, mViewModel.canSave);
	   		feeAmount.financialReturnAmountString = "1.00";
	   		assertEquals("Can Save", true, mViewModel.canSave);
	   		assertEquals("Total Value", 2.00, mViewModel.totalValue);	   			   		
   			
	   		addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifySave);
   			mViewModel.save();
   			
	   }			
	   
	   // todo test the save... waiting on core 
	   private function verifySave(e:ViewModelEvent):void {
   			this.assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);
   		}
	 	
	}
}