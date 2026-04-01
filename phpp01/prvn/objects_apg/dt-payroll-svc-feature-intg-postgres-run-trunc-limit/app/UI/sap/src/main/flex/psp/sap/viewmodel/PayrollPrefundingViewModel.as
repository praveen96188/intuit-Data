package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.events.CloseEvent;
	import mx.rpc.events.ResultEvent;
	import mx.validators.NumberValidator;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.model.BillingTransaction;
	import psp.sap.model.PayrollBillingTransactions;
	import psp.sap.validators.SAPDateValidator;
	import psp.sap.validators.SAPValidators;

	public class PayrollPrefundingViewModel extends PayrollMultiItemEnteringPageViewModel
	{		 
		public function PayrollPrefundingViewModel()
		{
			super();
			label = CompanyInspectorPageEnum.PAYROLL_PREFUNDING;
		}
		
		override public function get overrideShowDateField():Boolean {
			return true;
		}	
		
		override protected function createDateValidator(billingTransactions:PayrollBillingTransactions):SAPDateValidator {		
			// this defaults to the current date				
			return SAPValidators.createDateValidator(billingTransactions, "initiationDate", false, 45, 365, SAP.instance.PSPDate);
		}						
				
		override protected function createACHValidator(source:Object, property:String, maxValue:Object = null):NumberValidator {			
			return SAPValidators.createNumberValidator(source, property, false, maxValue, null, false, 2);	
		}
		
		override protected function createNonACHValidator(source:Object, property:String, maxValue:Object = null):NumberValidator {			
			return SAPValidators.createNumberValidator(source, property, false, maxValue, null, false, 2);	
		}
		
		override protected function loadModelData():void {
            loadCount = 2;
            super.loadModelData();
            SAP.instance.payrollRunService.findPayrollPrefundingTransactions(company.sourceSystemCd,
         																	company.companyId,
         																	payrollRun.sourcePayRunId,
         																	createLoadModelDataResponder(onLoadPayrollsSucceeded));
		}		
		
		override protected function executeSave():void { 
			var willCreateRefund:Boolean = false;
			var applyAmounts:ArrayCollection = new ArrayCollection();     		
			for each(var payroll:PayrollBillingTransactions in payrolls){
				payroll.computeTotals();
				if(payroll.amountsTotal > 0 || payroll.salesTaxTotal > 0){
                    for each (var ddTxn:BillingTransaction in payroll.ddTransactions) {
                        ddTxn.writeValues();
                        willCreateRefund = willCreateRefund || ddTxn.financialReturnAmount > ddTxn.financialAmount;
                    }
					if (payroll.taxTransaction != null) {     		    			     			     		
			     		payroll.taxTransaction.writeValues();			     		
			     		willCreateRefund = willCreateRefund || payroll.taxTransaction.financialReturnAmount > payroll.taxTransaction.financialAmount;
					}
					
					     		
		     		for each (var txn:BillingTransaction in payroll.feeTransactions) {     			
		     			txn.writeValues();
		     			willCreateRefund = willCreateRefund || txn.financialReturnAmount > txn.financialAmount;
		     		}
		     		applyAmounts.addItem(payroll);
	   			}
   			}
	     	
	     	if(applyAmounts.length > 0){	     					     	
	     		// special alert to warn user that current amounts will result in refunds
	     		if(willCreateRefund){
	     			Alert.show(	"The amounts entered are greater than the amount due and refunds will be generated for the excess amounts. Continue with save?",
	                        "Warning",
	                        Alert.YES | Alert.NO,
	                        null,
	                        function (e:CloseEvent):void {
	                            if(e.detail == Alert.YES) {
	                                SAP.instance.payrollRunService.addPrefundPayrollTransactions(
	                                					company.sourceSystemCd,
	     												company.companyId,
														settlementType.code,
														applyAmounts,
														createSaveResponder());
	                            }
	                            else {
	                            	SAP.instance.hideProgress();
	                            }
	                        },
	                        null,
	                        Alert.YES);
	     		}
	     		else{
	     			SAP.instance.payrollRunService.addPrefundPayrollTransactions(
	     												company.sourceSystemCd,
	     												company.companyId,
														settlementType.code,
														applyAmounts,
														createSaveResponder(onSaveSucceeded));
	     		}
     		}												
													
		} 	
		
		protected function onSaveSucceeded(e:ResultEvent):void {
			if(!hasActiveBankAccount){
				saveMsg = "Warning: The company does not have an active bank account refund transactions were not created";				
			}
		}			
	}
}