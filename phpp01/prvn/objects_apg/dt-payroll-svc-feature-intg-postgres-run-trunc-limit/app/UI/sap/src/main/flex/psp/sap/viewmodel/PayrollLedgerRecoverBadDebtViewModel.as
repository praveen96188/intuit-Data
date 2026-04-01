package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.events.PropertyChangeEvent;
    import mx.validators.NumberValidator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.BillingTransaction;
    import psp.sap.model.PayrollBillingTransactions;
    import psp.sap.model.SettlementTypeEnum;
    import psp.sap.validators.SAPDateValidator;
    import psp.sap.validators.SAPValidators;	


    public class PayrollLedgerRecoverBadDebtViewModel
    	extends PayrollMultiItemEnteringPageViewModel
    {            	 							      	       		

        private static const RECOVERY_TYPE_AGENCY:String = "agency";

        [Bindable] [BackingProperty] public var collectionAmountText:String="0.00";

        private var mRecoveryType:String;

        [Bindable] [BackingProperty]
        public function get recoveryType():String {
            return mRecoveryType;
        }

        public function set recoveryType(value:String):void {
            mRecoveryType = value;
            if (isCustomer) {
                collectionAmountText = "0.00";
            }
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this,"isCustomer", null, null));
        }

        [Bindable("propertyChange")]
        public function get isCustomer():Boolean {
            return recoveryType != RECOVERY_TYPE_AGENCY;
        }

        public function PayrollLedgerRecoverBadDebtViewModel()
        {
        	super();
        	label = CompanyInspectorPageEnum.PAYROLL_LEDGER_RECOVER_BAD_DEBT;
            recoveryType = RECOVERY_TYPE_AGENCY;
	    }
	    
	    [Bindable ("propertyChange")]
		override public function get showPaymentAmountBox():Boolean {
			return false;
		}	    	                   
                  
        override protected function loadModelData():void {
            loadCount = 2;
            super.loadModelData();
            SAP.instance.payrollRunService.findPayrollUnrecoveredBalances(	company.sourceSystemCd,
         																	company.companyId,
         																	payrollRun.sourcePayRunId,
         																	createLoadModelDataResponder(onLoadPayrollsSucceeded));
		}


        override protected function initializeDefaults():void {
            collectionAmountText = "0.00";
            super.initializeDefaults();
        }

        override protected function setupValidators():void {
            super.setupValidators();
            validators.push(SAPValidators.createNumberValidator(this, "collectionAmountText", false, 0, null, false, 2));
        }

		override protected function createACHValidator(source:Object, property:String, maxValue:Object = null):NumberValidator {			
			return SAPValidators.createNumberValidator(source, property, false, 0.00, null, false, 2);	
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
					payroll.isCustomer = isCustomer;
					     		
		     		for each (var txn:BillingTransaction in payroll.feeTransactions) {     			
		     			txn.writeValues();
		     		}
		     		applyAmounts.addItem(payroll);
	   			}
   			}
	     	
	     	if(applyAmounts.length > 0){	     	
				SAP.instance.payrollRunService.addRecoverBadDebtTransactions(
													company.sourceSystemCd,
	                                             	company.companyId,	                                             	
	                                             	isCustomer ? SettlementTypeEnum.ACH.code : settlementType.code,
	                                             	dateValue,
	                                             	applyAmounts,
                                                    parseFloat(collectionAmountText),
                                                    createSaveResponder());
			}
													
		}
    }
}
