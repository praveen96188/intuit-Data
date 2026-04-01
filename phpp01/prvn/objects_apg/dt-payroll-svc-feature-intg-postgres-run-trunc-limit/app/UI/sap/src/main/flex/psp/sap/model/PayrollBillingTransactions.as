package psp.sap.model
{
	import flash.events.EventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.formatters.DateFormatter;
	import mx.validators.NumberValidator;
	import mx.validators.Validator;
	
	import psp.sap.application.SAP;
	import psp.sap.validators.SAPDateValidator;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollBillingTransactions")]
	public class PayrollBillingTransactions extends EventDispatcher
	{	
		
		private var dateFormatter:DateFormatter = new DateFormatter();
		
		public function PayrollBillingTransactions():void {
			dateFormatter.formatString = SAP.instance.configuration.dateFormatShort;
		}
			
		public var initiationDate:Date;		
		public function get initiationDateString():String {
			return dateFormatter.format(initiationDate);
		}
		
		public function set initiationDateString(value:String):void {
			if(value == null || value.replace(" ", "").length == 0){
            	initiationDate = null;
            }
            else{
            	initiationDate = new Date(value);
            }
			
		}
							
	    public var payrollRunId:String;
	    public var checkDate:Date;

        [ArrayElementType("psp.sap.model.BillingTransaction")]
	    public var ddTransactions:ArrayCollection;
	    public var taxTransaction:BillingTransaction;
	    [ArrayElementType("psp.sap.model.BillingTransaction")]
    	public var feeTransactions:ArrayCollection;
        public var handlingFeeTransaction:BillingTransaction;

        public var isCustomer:Boolean = false;
    	
    	[Transient]
    	public var dateValidator:SAPDateValidator;    	
    	[Transient]
    	public var dateRequiredValidator:Validator;

        [Transient]
        public var achDDValidators:ArrayCollection=new ArrayCollection();
        [Transient]
        public var nonAchDDValidators:ArrayCollection=new ArrayCollection();

		[Transient]
    	public var taxTransactionACHValidator:NumberValidator;
    	[Transient]
    	public var taxTransactionNonACHValidator:NumberValidator;

     	[Transient]
     	public var amountsTotal:Number;
     	[Transient]
     	public var salesTaxTotal:Number;
     	
     	[Transient]
     	public function computeTotals():void {
			amountsTotal = 0;
			salesTaxTotal = 0;
			
			if(feeTransactions != null){
				for each(var txnItem:BillingTransaction in feeTransactions){
					amountsTotal += txnItem.financialReturnAmountStringValue;
					salesTaxTotal += txnItem.salesTaxReturnAmountStringValue;
				}
			}
            if (handlingFeeTransaction != null) {
                amountsTotal += handlingFeeTransaction.financialReturnAmountStringValue;
                salesTaxTotal += handlingFeeTransaction.salesTaxReturnAmountStringValue;
            }
			if(ddTransactions != null){
				for each(var txnItemDd:BillingTransaction in ddTransactions){
                    amountsTotal += txnItemDd.financialReturnAmountStringValue;
                }
			}
			
			if(taxTransaction != null){
				amountsTotal += taxTransaction.financialReturnAmountStringValue;
			}						 			 
		}
		
		[Transient]
		public function enableDisableValidators(settlementType:SettlementTypeEnum, overrideACHCheck:Boolean):void {
			var validator:Validator;
            if(overrideACHCheck || settlementType == SettlementTypeEnum.ACH){
				dateValidator.enabled = true;
				dateRequiredValidator.enabled = true;

                for each (validator in achDDValidators) {
                    validator.enabled = true;
                }
                for each (validator in nonAchDDValidators) {
                    validator.enabled = false;
                }
				if(taxTransactionNonACHValidator != null){
					taxTransactionACHValidator.enabled = true;
				}
				if(taxTransactionNonACHValidator != null){
					taxTransactionNonACHValidator.enabled = false;
				}
			}
			else{
				dateValidator.enabled = false;
				dateRequiredValidator.enabled = false;
                for each (validator in achDDValidators) {
                    validator.enabled = false;
                }
                for each (validator in nonAchDDValidators) {
                    validator.enabled = true;
                }
				if(taxTransactionNonACHValidator != null){
					taxTransactionACHValidator.enabled = false;
				}
				if(taxTransactionNonACHValidator != null){
					taxTransactionNonACHValidator.enabled = true;
				}
			}
		}     	      	
	     	     	  
	}
}
