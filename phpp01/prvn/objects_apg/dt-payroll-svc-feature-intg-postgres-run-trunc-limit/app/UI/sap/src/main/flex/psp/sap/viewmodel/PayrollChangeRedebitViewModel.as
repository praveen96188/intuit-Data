package psp.sap.viewmodel
{
	import mx.validators.NumberValidator;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.model.PayrollBillingTransactions;
	import psp.sap.model.SettlementTypeEnum;
	import psp.sap.validators.SAPDateValidator;
	import psp.sap.validators.SAPValidators;

	public class PayrollChangeRedebitViewModel extends PayrollMultiItemEnteringPageViewModel
	{		 
		public function PayrollChangeRedebitViewModel()
		{
			super();
			label = CompanyInspectorPageEnum.PAYROLL_CHANGE_REDEBIT;
		}
		
		override protected function createDateValidator(billingTransactions:PayrollBillingTransactions):SAPDateValidator {
			// an agent can select any day from today forward as a valid date
			return SAPValidators.createDateValidator(billingTransactions, "initiationDate", false, 0, 365, SAP.instance.PSPDate);
		}
		
		[Bindable ("propertyChange")]
		override public function get editAmounts():Boolean {
			return false;
		}
		
		override public function set settlementType(value:SettlementTypeEnum):void {
			mUpdateBlocking = true;
			
			if (value == null)
				value = mDefaultSettlementType;																																
			
			// enable/disable initaitionDate validators
			for each(var payroll:PayrollBillingTransactions in payrolls){
				payroll.enableDisableValidators(value, overrideShowDateField);								
				readAll(payroll);										
			}
			
			mSettlementType = value;
			
			mUpdateBlocking = false;									
			
			computeTotal();
		}				
		
		override protected function loadModelData():void {
            loadCount = 2;
            super.loadModelData();
            SAP.instance.payrollRunService.getRedebitTransactionsForPayroll(company.sourceSystemCd,
         																	company.companyId,
         																	payrollRun.sourcePayRunId,
         																	createLoadModelDataResponder(onLoadPayrollsSucceeded));
        }
                
		override protected function createACHValidator(source:Object, property:String, maxValue:Object = null):NumberValidator {			
			return SAPValidators.createNumberValidator(source, property, false, 0.00, maxValue, false, 2);	
		}
		
		override protected function createNonACHValidator(source:Object, property:String, maxValue:Object = null):NumberValidator {			
			return SAPValidators.createNumberValidator(source, property, false, 0.00, null, false, 2);	
		}		 		
	}
}