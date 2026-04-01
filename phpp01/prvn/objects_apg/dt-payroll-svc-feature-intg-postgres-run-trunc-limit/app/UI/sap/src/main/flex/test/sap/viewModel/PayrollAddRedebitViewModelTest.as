package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.application.SAP;
    import psp.sap.model.ActionEvent;
    import psp.sap.model.ActionEventCode;
    import psp.sap.model.BillingTransaction;
	import psp.sap.model.Company;
	import psp.sap.model.CompanyBankAccount;
	import psp.sap.model.PayrollBillingTransactions;
	import psp.sap.model.PayrollRun;
	import psp.sap.model.SettlementTypeEnum;
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
	import psp.sap.viewmodel.PayrollAddRedebitViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockPayrollRunService;
import test.mock.data.PayrollData;

public class PayrollAddRedebitViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:PayrollAddRedebitViewModel = new PayrollAddRedebitViewModel();
		private var mDataService:MockPayrollRunService;
		
		private var mCompany:Company;
		private var mPayrollRun:PayrollRun;

		public function PayrollAddRedebitViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( PayrollAddRedebitViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"totalPayment",
				"balance",
				"editAmounts",
				"paymentAmountTotal",
				"previousWarnDate",
				"date",
				"canSelectNonStandardSettlementTypes",
				"showPaymentAmountBox",
				"totalAmountValidator",
				"hasTransactions",
				"hasActiveBankAccount",
				"canExecute",
				"payrolls",
				"totalAmountRequiredValidator",
				"appliedAmountTotal",
				"settlementType",
				"bankAccountLabel",
				"dateFormatter",
				"settlementTypes",
				"canSave"
			];

			mDataService = mSAP.payrollRunService as MockPayrollRunService;
			
			mCompany = new Company();
			mCompany.sourceSystemCd = SourceSystemEnum.QBOE.code;
			mCompany.companyId = "1234";
			mViewModel.company = mCompany;
			
			mPayrollRun = new PayrollRun();
			mPayrollRun.sourcePayRunId = "1527";
            mViewModel.payrollRun = mPayrollRun;
            mViewModel.action = new ActionEvent(ActionEventCode.DD_REDEBIT_ADD);


			mViewModel.settlementTypes = SettlementTypeEnum.values;
			mViewModel.defaultSettlementType = SettlementTypeEnum.WIRE;

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mDataService.expectsFindPayrollUncollectedBalances(mCompany.companyId,
                                                                            mCompany.sourceSystemCd,
         																	mPayrollRun.sourcePayRunId).willReturnAsync(PayrollData.getBillingTransactions());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertNotNull("date", mViewModel.date);
			assertEquals("paymentAmountTotal", 0.00, mViewModel.paymentAmountTotal);
			assertEquals("appliedAmountTotal", 0.00, mViewModel.appliedAmountTotal);
			assertEquals("settlementType", SettlementTypeEnum.WIRE, mViewModel.settlementType);
			assertEquals("hasTransactions", true, mViewModel.hasTransactions);
			assertEquals("totalPayment", "0.00", mViewModel.totalPayment);
			assertEquals("paymentAmountTotal", 0, mViewModel.paymentAmountTotal);
			assertEquals("appliedAmountTotal", 0, mViewModel.appliedAmountTotal);						

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "totalPayment", "0.01");
			testBindableProperty(mViewModel, "paymentAmountTotal", 1);
			testBindableProperty(mViewModel, "date", null);
			testBindableProperty(mViewModel, "hasTransactions", false);
			testBindableProperty(mViewModel, "hasActiveBankAccount", false);
			testBindableProperty(mViewModel, "canExecute", false);
			testBindableProperty(mViewModel, "payrolls", new ArrayCollection());
			testBindableProperty(mViewModel, "appliedAmountTotal", 1);
			testBindableProperty(mViewModel, "settlementType", SettlementTypeEnum.CASH);
			testBindableProperty(mViewModel, "bankAccountLabel", "blah");
			
			testComputeTotalAndAllocateAmounts();
		}

		private function testComputeTotalAndAllocateAmounts():void {
			var payroll:PayrollBillingTransactions = mViewModel.payrolls.getItemAt(0) as PayrollBillingTransactions;
			assertNotNull("payroll", payroll);
			assertEquals("settlementType", SettlementTypeEnum.WIRE, mViewModel.settlementType);
									
			// payment should be applied to taxes first, then dd, then fees, then user specified
			var fee:BillingTransaction;
			mViewModel.totalPayment = "1.00";
			assertEquals("total payment", "1.00", mViewModel.totalPayment);
			assertEquals("tax payment", "1.00", payroll.taxTransaction.financialReturnAmountString);
			//assertEquals("dd payment", "0.00", payroll.ddTransaction.financialReturnAmountString);
			for each(fee in payroll.feeTransactions){
				assertEquals("fee payment", "0.00", fee.financialReturnAmountString);
				assertEquals("sales tax payment", "0.00", fee.salesTaxReturnAmountString);
			}
			assertEquals("payment Amount", 1.00, mViewModel.paymentAmountTotal);
			assertEquals("applied Amount", 1.00, mViewModel.appliedAmountTotal);
			assertEquals("balance", 0, mViewModel.balance);
			assertEquals("can save", true, mViewModel.canSave);
			
			mViewModel.totalPayment = "3.50";
			assertEquals("total payment", "3.50", mViewModel.totalPayment);
			assertEquals("tax payment", "3.00", payroll.taxTransaction.financialReturnAmountString);
			//assertEquals("dd payment", "0.50", payroll.ddTransaction.financialReturnAmountString);
			for each(fee in payroll.feeTransactions){
				assertEquals("fee payment", "0.00", fee.financialReturnAmountString);
				assertEquals("sales tax payment", "0.00", fee.salesTaxReturnAmountString);
			}
			assertEquals("payment Amount", 3.50, mViewModel.paymentAmountTotal);
			assertEquals("applied Amount", 3.50, mViewModel.appliedAmountTotal);
			assertEquals("balance", 0, mViewModel.balance);
			assertEquals("can save", true, mViewModel.canSave);
			
			mViewModel.totalPayment = "7.00";
			assertEquals("total payment", "7.00", mViewModel.totalPayment);
			assertEquals("tax payment", "3.00", payroll.taxTransaction.financialReturnAmountString);
			//assertEquals("dd payment", "1.00", payroll.ddTransaction.financialReturnAmountString);
			for each(fee in payroll.feeTransactions){
				assertEquals("fee payment", "2.00", fee.financialReturnAmountString);
				assertEquals("sales tax payment", "1.00", fee.salesTaxReturnAmountString);
			}
			assertEquals("payment Amount", 7.00, mViewModel.paymentAmountTotal);
			assertEquals("applied Amount", 7.00, mViewModel.appliedAmountTotal);
			assertEquals("balance", 0, mViewModel.balance);
			assertEquals("can save", true, mViewModel.canSave);
			
			mViewModel.totalPayment = "20.00";
			assertEquals("total payment", "20.00", mViewModel.totalPayment);
			assertEquals("tax payment", "3.00", payroll.taxTransaction.financialReturnAmountString);
			//assertEquals("dd payment", "1.00", payroll.ddTransaction.financialReturnAmountString);
			for each(fee in payroll.feeTransactions){
				assertEquals("fee payment", "2.00", fee.financialReturnAmountString);
				assertEquals("sales tax payment", "5.00", fee.salesTaxReturnAmountString);
			}
			assertEquals("payment Amount", 20.00, mViewModel.paymentAmountTotal);
			assertEquals("applied Amount", 11.00, mViewModel.appliedAmountTotal);
			assertEquals("balance", 9.00, mViewModel.balance);
			assertEquals("can save", false, mViewModel.canSave);
			
			// test clear all
			mViewModel.clearAll(payroll);			
			assertEquals("tax payment", "0.00", payroll.taxTransaction.financialReturnAmountString);
			//assertEquals("dd payment", "0.00", payroll.ddTransaction.financialReturnAmountString);
			for each(fee in payroll.feeTransactions){
				assertEquals("fee payment", "0.00", fee.financialReturnAmountString);
				assertEquals("sales tax payment", "0.00", fee.salesTaxReturnAmountString);
			}			
			assertEquals("applied Amount", 0.00, mViewModel.appliedAmountTotal);			
			assertEquals("can save", false, mViewModel.canSave);							
			
			// ach sets all values to value owed
			mViewModel.settlementType = SettlementTypeEnum.ACH;			
			assertEquals("tax payment", "3.00", payroll.taxTransaction.financialReturnAmountString);
			//assertEquals("dd payment", "1.00", payroll.ddTransaction.financialReturnAmountString);
			for each(fee in payroll.feeTransactions){
				assertEquals("fee payment", "2.00", fee.financialReturnAmountString);
				assertEquals("sales tax payment", "5.00", fee.salesTaxReturnAmountString);
			}
			assertEquals("payment Amount", 11.00, mViewModel.paymentAmountTotal);			
			assertEquals("can save", true, mViewModel.canSave);									
			
			// change to non-ach
			mViewModel.settlementType = SettlementTypeEnum.CHECK;
			assertEquals("total payment", "0.00", mViewModel.totalPayment);
			assertEquals("tax payment", "3.00", payroll.taxTransaction.financialReturnAmountString);
			//assertEquals("dd payment", "1.00", payroll.ddTransaction.financialReturnAmountString);
			for each(fee in payroll.feeTransactions){
				assertEquals("fee payment", "2.00", fee.financialReturnAmountString);
				assertEquals("sales tax payment", "5.00", fee.salesTaxReturnAmountString);
			}
			assertEquals("payment Amount", 0.00, mViewModel.paymentAmountTotal);
			assertEquals("applied Amount", 11.00, mViewModel.appliedAmountTotal);
			assertEquals("balance", -11.00, mViewModel.balance);
			assertEquals("can save", false, mViewModel.canSave);
						
			testValidators();
		}		

		override protected function testValidators():void {
			// make can save true
			mViewModel.totalPayment = "1.00";
			assertEquals("can save", true, mViewModel.canSave);
			
			testNumberValidatorLocal(mViewModel, mViewModel, "totalPayment", 0.01);
			testRequiredStringValidator(mViewModel, "totalPayment");
			
			// test billing transactions validators
			var payroll:PayrollBillingTransactions = mViewModel.payrolls.getItemAt(0) as PayrollBillingTransactions;
			assertNotNull("payroll", payroll);
			
			// non-ach
			assertTrue("settlement type", mViewModel.settlementType != SettlementTypeEnum.ACH);
			//testNumberValidatorLocal(mViewModel, payroll.ddTransaction, "financialReturnAmountString", 0.00);
			testNumberValidatorLocal(mViewModel, payroll.taxTransaction, "financialReturnAmountString", 0.00);
			
			// ach
			mViewModel.settlementType = SettlementTypeEnum.ACH;
			assertTrue("settlement type", mViewModel.settlementType == SettlementTypeEnum.ACH);
			assertEquals("can save", true, mViewModel.canSave);
			clearPropertyChangeEventHistory();
			assertNotNull("initiation date", payroll.initiationDate);
			testRequiredValidator(mViewModel, payroll, "initiationDate");
			testDateValidatorLocal(mViewModel, payroll, "initiationDate", 0, 365); 			
			//testNumberValidatorLocal(mViewModel, payroll.ddTransaction, "financialReturnAmountString", 0.00, 1.00);
			testNumberValidatorLocal(mViewModel, payroll.taxTransaction, "financialReturnAmountString", 0.00, 3.00);						
						

			testSave();
		}

		override protected function testSave():void {
			mViewModel.settlementType = SettlementTypeEnum.WIRE;
			mViewModel.totalPayment = "11.00";
            mViewModel.date = mViewModel.dateFormatter.format(mSAP.PSPDate);
			assertEquals("can save", true, mViewModel.canSave);

			mDataService.expectsRedebitPayrollTransactions(mCompany.sourceSystemCd, mCompany.companyId, SettlementTypeEnum.WIRE.code, new Date(mViewModel.dateFormatter.format(mSAP.PSPDate)), mViewModel.payrolls);
			addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);
			addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);

			mViewModel.save();
		}

		override protected function verifySave(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);

			assertTrue(mDataService.errorMessage(), mDataService.success());			
		}

		override protected function verifyRefresh(e:ViewModelEvent):void {
			assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);

			assertNotNull("date", mViewModel.date);
			assertEquals("paymentAmountTotal", 0.00, mViewModel.paymentAmountTotal);
			assertEquals("appliedAmountTotal", 0.00, mViewModel.appliedAmountTotal);
			assertEquals("settlementType", SettlementTypeEnum.WIRE, mViewModel.settlementType);
			assertEquals("hasTransactions", true, mViewModel.hasTransactions);
			assertEquals("totalPayment", "0.00", mViewModel.totalPayment);
			assertEquals("paymentAmountTotal", 0, mViewModel.paymentAmountTotal);
			assertEquals("appliedAmountTotal", 0, mViewModel.appliedAmountTotal);
		}
		
		// special validator tests that exclude the property tracking and allow a source other than the view model
		private function testNumberValidatorLocal(viewModel:Object, source:Object, propertyName:String, minNumber:Number=NaN, maxNumber:Number=NaN, isString:Boolean=false):void {   			   			
   			var temp:Object = source[propertyName];
   			
   			// test min
   			if(!isNaN(minNumber)){
   				minNumber--;
   				var minValue:Object = isString ? minNumber.toFixed(2) : minNumber;   				   					   		
   				source[propertyName] = minValue;   				
   				assertEquals("can save", false, viewModel.canSave);   				
	   			revertProperty(source, propertyName, temp);
   			}
   			
   			// test max
   			if(!isNaN(maxNumber)){
   				maxNumber++;
   				var maxValue:Object = isString ? maxNumber.toFixed(2) : maxNumber;
   				source[propertyName] = maxValue;   				
   				assertEquals("can save", false, viewModel.canSave);   				   				
	   			revertProperty(source, propertyName, temp);
   			}   			   			
   		}
   		
   		override protected function revertProperty(source:Object, propertyName:String, oldValue:Object):void {
   			source[propertyName] = oldValue;
	   		clearPropertyChangeEventHistory();	   		
   		}
   		
   		private function testRequiredValidator(viewModel:Object, source:Object, propertyName:String):void {
   			var temp:Object = source[propertyName];
   			
   			// set field to empty
   			source[propertyName] = null;   			
   			assertEquals("can save", false, viewModel.canSave);
   			
   			revertProperty(source, propertyName, temp);
   		}
   		
   		private function testDateValidatorLocal(viewModel:Object, source:Object, propertyName:String, daysBefore:Number=NaN, daysAfter:Number=NaN):void {   			   			
   			var temp:Object = source[propertyName];
   			
   			// test min
   			if(!isNaN(daysBefore)){
   				var timmToSubtract:Number = (daysBefore+1)*SAP.instance.configuration.millisecondsPerDay;
   				var minDate:Date = SAP.instance.PSPDate;
   				minDate.setTime(minDate.getTime() - timmToSubtract);    			
   				source[propertyName] = minDate;   				
   				assertEquals("can save", false, viewModel.canSave);
   				
	   			revertProperty(source, propertyName, temp);
   			}
   			
   			// test max
   			if(!isNaN(daysAfter)){
   				var timmToAdd:Number = (daysAfter+1)*SAP.instance.configuration.millisecondsPerDay;
   				var maxDate:Date = SAP.instance.PSPDate;
   				maxDate.setTime(maxDate.getTime() + timmToAdd);    			
   				source[propertyName] = maxDate;
   				assertEquals("can save", false, viewModel.canSave);
   				   				
	   			revertProperty(source, propertyName, temp);
   			}   			   			
   		}

	}
}
