package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.validators.NumberValidator;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.model.BillingTransaction;
	import psp.sap.model.PayrollBillingTransactions;
	import psp.sap.validators.SAPDateValidator;
	import psp.sap.validators.SAPValidators;

	public class PayrollRefundFraudEscalationViewModel extends PayrollMultiItemEnteringPageViewModel
	{		      	       		
               
        public function PayrollRefundFraudEscalationViewModel()
        {
        	super();
        	label = CompanyInspectorPageEnum.PAYROLL_REFUND_FRAUD_ESCALATION;              						
	    } 
	    
	    [Bindable ("propertyChange")]
		override public function get showPaymentAmountBox():Boolean {
			return false;
		}               
                  
        override protected function loadModelData():void {
            loadCount = 2;
            super.loadModelData();
            SAP.instance.payrollRunService.findPayrollCollectedTransactions(company.sourceSystemCd,
         																	company.companyId,
         																	payrollRun.sourcePayRunId,
         																	createLoadModelDataResponder(onLoadPayrollsSucceeded));
		}				             
		
		override protected function createACHValidator(source:Object, property:String, maxValue:Object = null):NumberValidator {
			return SAPValidators.createNumberValidator(source, property, false, 0.00, maxValue, false, 2);	
		}
		
		override protected function createNonACHValidator(source:Object, property:String, maxValue:Object = null):NumberValidator {			
			return createACHValidator(source, property, maxValue);	
		}
		
		override protected function createDateValidator(billingTransactions:PayrollBillingTransactions):SAPDateValidator {
			// only today
			return SAPValidators.createDateValidator(billingTransactions, "initiationDate", false, 0, 0, SAP.instance.PSPDate);
		}						

     	override protected function executeSave():void {     		
     		var applyAmounts:ArrayCollection = new ArrayCollection();     		
			for each(var payroll:PayrollBillingTransactions in payrolls){
				payroll.computeTotals();
				if(payroll.amountsTotal > 0 || payroll.salesTaxTotal > 0){
                    for each (var ddTxn:BillingTransaction in payroll.ddTransactions) {
                        ddTxn.writeValues();
                    }
					if (payroll.taxTransaction != null) {     		    			     			     		
			     		payroll.taxTransaction.writeValues();			     		
					}
					
					     		
		     		for each (var txn:BillingTransaction in payroll.feeTransactions) {     			
		     			txn.writeValues();
		     		}
		     		applyAmounts.addItem(payroll);
	   			}
   			}
	     	
	     	if(applyAmounts.length > 0){
	     		SAP.instance.payrollRunService.addRefundPayrollTransactions(
	     											company.sourceSystemCd,
	     											company.companyId,
													settlementType.code,
													dateValue,
													applyAmounts,
													createSaveResponder());
			}													
													
		}
		
	}
}