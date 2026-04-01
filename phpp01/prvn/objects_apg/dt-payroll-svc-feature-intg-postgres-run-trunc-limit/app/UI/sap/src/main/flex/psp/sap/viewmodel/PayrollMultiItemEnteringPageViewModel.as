package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    import mx.events.PropertyChangeEvent;
    import mx.formatters.CurrencyFormatter;
    import mx.formatters.DateFormatter;
    import mx.rpc.events.ResultEvent;
    import mx.validators.NumberValidator;
    import mx.validators.Validator;
    
    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.application.events.SAPEvent;
    import psp.sap.model.ActionEvent;
    import psp.sap.model.BillingTransaction;
    import psp.sap.model.CompanyBankAccount;
    import psp.sap.model.PayrollBillingTransactions;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.SettlementTypeEnum;
    import psp.sap.validators.SAPDateValidator;
    import psp.sap.validators.SAPValidators;


    public class PayrollMultiItemEnteringPageViewModel
    extends AbstractPartViewModel
    {

        [Bindable] [BackingProperty (context=true)] public var payrollRun:PayrollRun;
        [Bindable] [BackingProperty (context=true)] public var action:ActionEvent;

		public static const EVENT_DATE_WARNING:String = "dateWarning";
		
		public static const DEFAULT_AMOUNT_STRING:String = "0.00";
		public static const DEFAULT_AMOUNT:Number = 0;				
		private static const DEFAULT_BANK_ACCOUNT_LABEL:String = "[No Active Bank Account]";
					
		private var mAllowSelectableSettlementTypes:Boolean;		
		private var mPayrolls:ArrayCollection;
		protected var mSettlementType:SettlementTypeEnum;
		private var mPaymentTotal:Number;
        private var mAppliedTotal:Number;        
        private var mBankAccountLabel:String;
        private var mHasActiveBankAccount:Boolean = false;
        private var mCanExecute:Boolean = true;
        private var mHasTransactions:Boolean = false;
        protected var mUpdateBlocking:Boolean = false;
        private var mPrevWarnDate:Date = SAP.instance.PSPDate;        
        private var mSettlementTypes:Array = SettlementTypeEnum.values;
        protected var mDefaultSettlementType:SettlementTypeEnum = SettlementTypeEnum.ACH;
        private var mTotalPayment:String;
        
        protected var mDate:String;


        [Bindable]
        public var totalAmountValidator:NumberValidator;
        
        [Bindable]
        public var totalAmountRequiredValidator:Validator;
        
        [Bindable]
        public var dateFormatter:DateFormatter = new DateFormatter();
        
        [Bindable]
        public var balance:Number;
               
        public function PayrollMultiItemEnteringPageViewModel()
        {
            super();                        
            
            dateFormatter.formatString = SAP.instance.configuration.dateFormatShort;
            
            totalAmountValidator = SAPValidators.createNumberValidator(this, "totalPayment", false, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2);
            totalAmountValidator.enabled = showPaymentAmountBox;
            totalAmountRequiredValidator = SAPValidators.createRequiredFieldValidator(this, "totalPayment", true);
            totalAmountRequiredValidator.enabled = showPaymentAmountBox;
					
			setupValidators();
           			
			this.reloadOnSave = true;
	    }

        public static function createActivator(payrollRun:PayrollRun, action:ActionEvent):Object {
            return {"payrollRun":payrollRun, "action":action};
        }

	    
	    [Bindable]
	    public function get totalPayment():String {
	    	return mTotalPayment;
	    }
	    
	    public function set totalPayment(value:String):void {
	    	mTotalPayment = value;
	    	if(settlementType != SettlementTypeEnum.ACH){
	    		allocateAmount();
	    		computeTotal();
	    	}
	    }
	    
	    [Bindable]		
		public function get previousWarnDate():Date {
			return mPrevWarnDate;
		}
         
        public function set previousWarnDate(value:Date):void {
        	mPrevWarnDate = value;
        }
        
        [Bindable]
        public function get settlementTypes():Array {
        	return mSettlementTypes;
        }
        
        public function set settlementTypes(value:Array):void {
        	mSettlementTypes = value;        	
        }
        
        [Bindable]
		public function get settlementType():SettlementTypeEnum {
			return mSettlementType;
		}
		 
		public function set settlementType(value:SettlementTypeEnum):void {
			mUpdateBlocking = true;
			
			if (value == null)
				value = mDefaultSettlementType;												
			
			paymentAmountTotal = 0;
			appliedAmountTotal = 0;
			
			if(mSettlementType == SettlementTypeEnum.ACH && value != SettlementTypeEnum.ACH){
				totalPayment = DEFAULT_AMOUNT_STRING;
			}										
			
			// enable/disable initaitionDate validators
			for each(var payroll:PayrollBillingTransactions in payrolls){
				payroll.enableDisableValidators(value, overrideShowDateField);				
				// non-ach -> ach read all values
				if(mSettlementType != SettlementTypeEnum.ACH && value == SettlementTypeEnum.ACH){
					payroll.initiationDateString = dateFormatter.format(SAP.instance.PSPDate);
					readAll(payroll);
				}			
				else if(overrideShowDateField){
					payroll.initiationDateString = dateFormatter.format(SAP.instance.PSPDate);
				}
			}
			
			mSettlementType = value;
			
			mUpdateBlocking = false;									
			
			computeTotal();
		}		
		
		public function get defaultSettlementType():SettlementTypeEnum {
			return mDefaultSettlementType;
		}
		
		public function set defaultSettlementType(value:SettlementTypeEnum):void {
			mDefaultSettlementType = value;
		}				        
		
		[Bindable]
		public function get payrolls():ArrayCollection {
			return mPayrolls;
		}
		
		public function set payrolls(value:ArrayCollection):void {
			mPayrolls = value;							
		}				
			
		[Bindable]
		public function get hasActiveBankAccount():Boolean {
			return mHasActiveBankAccount;
		}
	
		public function set hasActiveBankAccount(value:Boolean):void {
			mHasActiveBankAccount = value;
		}
				
		[Bindable]
		public function get canExecute():Boolean {
			return mCanExecute;
		}
		
		public function set canExecute(value:Boolean):void {
			mCanExecute = value;
		}
		
		private function updateCanExecute():void {
			// ACH cannot be executed if there is no active bank account 
			canExecute = settlementType == SettlementTypeEnum.ACH ? hasActiveBankAccount : true;
		}
		        
    	// date field	
		[Bindable]
		public function get date():String {
			return mDate;
		}
		
		public function set date(value:String):void {
			mDate = dateFormatter.format(value);
			updateCanSave();			
		}
		
		public function get dateValue():Date {
			return new Date(mDate);
		}			

        [Bindable] public var bankAccountLabel:String;

		[Bindable]
		public function get hasTransactions():Boolean {
			return mHasTransactions;
		}
		
		public function set hasTransactions(value:Boolean):void {
			mHasTransactions = value;
		}
		
		[Bindable]
		public function get paymentAmountTotal():Number {
			return mPaymentTotal;
		}
		
		public function set paymentAmountTotal(value:Number):void {
			mPaymentTotal = value;
		}				
		
		[Bindable]
		public function get appliedAmountTotal():Number {
			return mAppliedTotal;
		}
		
		public function set appliedAmountTotal(value:Number):void {
			mAppliedTotal = value;
		}				
		
		override protected function onActivated():void {
			previousWarnDate = SAP.instance.PSPDate;			
		}
		
		// sub class overrides this
		[Bindable ("propertyChange")]
		public function get showPaymentAmountBox():Boolean {
			return true;
		}
		
		// sub class overrides this
		[Bindable ("propertyChange")]
		public function get editAmounts():Boolean {
			return true;
		}
		
		// sub class overrides this
		[Bindable ("propertyChange")]
		public function get overrideShowDateField():Boolean {
			return false;
		}


        override protected function loadModelData():void {
            SAP.instance.companyService.getActiveBankAccount(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onActiveBankAccountLoaded));
        }

        private function onActiveBankAccountLoaded(e:ResultEvent):void {
            var activeBankAccount:CompanyBankAccount = CompanyBankAccount(e.result);
            hasActiveBankAccount = activeBankAccount != null;
            if (hasActiveBankAccount) {
                bankAccountLabel = activeBankAccount.toString();
            }
        }

        override protected function initializeBackingProperties():void {
			date = dateFormatter.format(SAP.instance.PSPDate);
			paymentAmountTotal = DEFAULT_AMOUNT;
			appliedAmountTotal = DEFAULT_AMOUNT;
			totalPayment = DEFAULT_AMOUNT_STRING;
			
			// this must be set after the has active bank account because it uses it
			settlementType = defaultSettlementType;		
				
			for each(var payroll:PayrollBillingTransactions in payrolls){				
				payroll.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onInitaitionDateChanged, false, 0, true);
								
				if (payroll.ddTransactions != null) {
					payroll.ddTransactions.addEventListener(CollectionEvent.COLLECTION_CHANGE, onTransactionCollectionChanged, false, 0, true);
				}
				
				if (payroll.taxTransaction != null) {														
					payroll.taxTransaction.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onFinancialTransactionChanged, false, 0, true);					
				}
				
				if (payroll.feeTransactions != null) {					
					// listen for changes that require recalculating the sub-totals and totals
					payroll.feeTransactions.addEventListener(CollectionEvent.COLLECTION_CHANGE, onTransactionCollectionChanged, false, 0, true);
				}
                if (payroll.handlingFeeTransaction != null) {
                    payroll.handlingFeeTransaction.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onFinancialTransactionChanged, false, 0, true);
                }


                hasTransactions = hasTransactions || ((payroll.ddTransactions != null && payroll.ddTransactions.length > 0) || payroll.taxTransaction
                        || (payroll.feeTransactions != null && payroll.feeTransactions.length > 0)
                        || (payroll.handlingFeeTransaction));
			}
			
			computeTotal();			
		}
		
		protected function onTransactionCollectionChanged(event:CollectionEvent):void {
			onTransactionChanged();
		}
				
		// Action for Fees Input Change
		protected function onFinancialTransactionChanged(event:PropertyChangeEvent):void {
			if(event.property != "financialReturnAmountString"){
				return;
			} 
			
	 		onTransactionChanged();
        }
         
         private function onTransactionChanged():void {		 	
		 	computeTotal();	         	
         }
         
         private function onInitaitionDateChanged(event:PropertyChangeEvent):void {
         	if(event.property != "initiationDate"){
		 		return;
		 	} 	
		 	updateCanSave();
         }                           
         
      	// Payroll Loaded
		protected function onLoadPayrollsSucceeded(e:ResultEvent):void {						
			payrolls = e.result as ArrayCollection;
			setupValidators();
			
			// enable/disable initaitionDate validators
			for each(var payroll:PayrollBillingTransactions in payrolls){
				payroll.enableDisableValidators(settlementType, overrideShowDateField);				
				// non-ach -> ach read all values
				if(settlementType != SettlementTypeEnum.ACH){
					payroll.initiationDateString = dateFormatter.format(SAP.instance.PSPDate);
					readAll(payroll);
				}			
				else if(overrideShowDateField){
					payroll.initiationDateString = dateFormatter.format(SAP.instance.PSPDate);
				}
			}
			
			computeTotal();		
			dispatchEvent(SAPEvent.createDataLoadCompletedEvent());

		}
		
		virtual protected function createDateValidator(billingTransactions:PayrollBillingTransactions):SAPDateValidator {
			throw new Error("sub-classes must override createDateValidator");
		} 
		
		protected function setupValidators():void {
			validators.length = 0;

            validators.push(totalAmountValidator);            
            validators.push(totalAmountRequiredValidator);								
			
			setupTxnValidators();
		}
		
		protected function setupTxnValidators():void {
			for each(var billingTransactions:PayrollBillingTransactions in payrolls){
				if (billingTransactions != null) {
					billingTransactions.achDDValidators = new ArrayCollection();
                    billingTransactions.nonAchDDValidators = new ArrayCollection();


					// initiation date validator
					billingTransactions.dateValidator = createDateValidator(billingTransactions);
					validators.push(billingTransactions.dateValidator);
					billingTransactions.dateRequiredValidator = SAPValidators.createRequiredFieldValidator(billingTransactions, "initiationDate", true);
					validators.push(billingTransactions.dateRequiredValidator);
					
					// dd amount validators

                    for each (var ddTxn:BillingTransaction in billingTransactions.ddTransactions) {
                        var validator:Validator = createACHValidator(ddTxn, "financialReturnAmountString", ddTxn.financialAmount);
                        billingTransactions.achDDValidators.addItem(validator);
                        validators.push(validator);

                        validator = createNonACHValidator(ddTxn, "financialReturnAmountString", ddTxn.financialAmount);
                        billingTransactions.nonAchDDValidators.addItem(validator);
                        validators.push(validator);
                    }


					// tax amount validators
					if (billingTransactions.taxTransaction != null) {						 					
						billingTransactions.taxTransactionACHValidator = createACHValidator(billingTransactions.taxTransaction, "financialReturnAmountString", billingTransactions.taxTransaction.financialAmount);
						billingTransactions.taxTransactionNonACHValidator = createNonACHValidator(billingTransactions.taxTransaction, "financialReturnAmountString", billingTransactions.taxTransaction.financialAmount);
		        		validators.push(billingTransactions.taxTransactionACHValidator);
		        		validators.push(billingTransactions.taxTransactionNonACHValidator);												
					}
				
					for each (var txn:BillingTransaction in billingTransactions.feeTransactions) {
						// fee amount validator
						if (txn.financialTxnId != null) {
		        			var feeValidator:NumberValidator = createACHValidator(txn, "financialReturnAmountString", txn.financialAmount);
		        			validators.push(feeValidator);			
						}
						
						// sales tax amount validator
						if (txn.salesTaxTxnId != null) {
							var taxValidator:NumberValidator = createACHValidator(txn, "salesTaxReturnAmountString", txn.salesTaxAmount);
		        			validators.push(taxValidator);						
						}
					}

                    if (billingTransactions.handlingFeeTransaction != null) {
                        var handlingFeeValidator:NumberValidator = createACHValidator(billingTransactions.handlingFeeTransaction, "financialReturnAmountString", billingTransactions.handlingFeeTransaction.financialAmount);
                        validators.push(handlingFeeValidator);
                    }

					billingTransactions.enableDisableValidators(settlementType, overrideShowDateField);
				}
			}
		}
		
		virtual protected function createACHValidator(source:Object, property:String, maxValue:Object = null):NumberValidator {
			throw new Error("sub-classes must override createValidator(..)"); 
		}		
		
		virtual protected function createNonACHValidator(source:Object, property:String, maxValue:Object = null):NumberValidator {
			throw new Error("sub-classes must override createValidator(..)"); 
		}
			
		override protected function evaluateCanSave():Boolean {
			updateCanExecute();
			return (super.evaluateCanSave()
					&& hasTransactions 
					&& paymentAmountTotal >= SAP.instance.configuration.minAllowedCurrencyValue
					&& ((settlementType == SettlementTypeEnum.ACH && hasActiveBankAccount) || (settlementType != SettlementTypeEnum.ACH && balance == 0)));
		}
		
		// to ensure that the evaluateCanSave reflects the result of Validations only.
		override public function get hasChanged():Boolean {
			return true;
		}				
	
    	// Compute the sub totals for amount and sales tax and the grand total
		public function computeTotal():void {
			if(!mUpdateBlocking){
				paymentAmountTotal = 0;			
				appliedAmountTotal = 0;			
				
				for each(var payroll:PayrollBillingTransactions in payrolls){
					payroll.computeTotals();
					if(settlementType == SettlementTypeEnum.ACH){
						paymentAmountTotal += payroll.amountsTotal + payroll.salesTaxTotal;
					}
					else{
						appliedAmountTotal += payroll.amountsTotal + payroll.salesTaxTotal;
					}			
				}
				
				if(settlementType != SettlementTypeEnum.ACH){
					paymentAmountTotal = parseFloat(totalPayment != "" ? totalPayment : DEFAULT_AMOUNT_STRING);
				}
				else {
					totalPayment = paymentAmountTotal.toFixed(2);
				}
				
				if(!showPaymentAmountBox && settlementType != SettlementTypeEnum.ACH){
					paymentAmountTotal = appliedAmountTotal;					
				}
				
				// round to 2 decimals
				balance = Math.round(paymentAmountTotal*100 - appliedAmountTotal*100)/100;
				
				updateCanSave();
			} 			 
		}
		
		public function clearAll(payroll:PayrollBillingTransactions):void {
			mUpdateBlocking = true;
			for each (var ddTxn:BillingTransaction in payroll.ddTransactions) {
				ddTxn.financialReturnAmountString = DEFAULT_AMOUNT_STRING;
			}
			if(payroll.taxTransaction != null){
				payroll.taxTransaction.financialReturnAmountString = DEFAULT_AMOUNT_STRING;
			}
			
			for each (var txn:BillingTransaction in payroll.feeTransactions) {
				txn.financialReturnAmountString = DEFAULT_AMOUNT_STRING;
				txn.salesTaxReturnAmountString = DEFAULT_AMOUNT_STRING;
			}
            if (payroll.handlingFeeTransaction != null) {
                payroll.handlingFeeTransaction.financialReturnAmountString = DEFAULT_AMOUNT_STRING;
            }
			mUpdateBlocking = false;
			computeTotal();
		}
		
		public function readAll(payroll:PayrollBillingTransactions):void {
			mUpdateBlocking = true;
			for each (var ddTxn:BillingTransaction in payroll.ddTransactions) {
				ddTxn.readValues();
			}
			if(payroll.taxTransaction != null){
				payroll.taxTransaction.readValues();
			}
			
			for each (var txn:BillingTransaction in payroll.feeTransactions) {
				txn.readValues();
			}
            if (payroll.handlingFeeTransaction != null) {
                payroll.handlingFeeTransaction.readValues();
            }
			mUpdateBlocking = false;			
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
                    if (payroll.handlingFeeTransaction != null) {
                        payroll.handlingFeeTransaction.writeValues();
                    }
		     		applyAmounts.addItem(payroll);
	   			}
   			}
	     	
	     	if(applyAmounts.length > 0){
				SAP.instance.payrollRunService.redebitPayrollTransactions(
													company.sourceSystemCd,
													company.companyId,
													settlementType.code,
													dateValue, 		// date used for all settlement types other than ach
													applyAmounts,
													createSaveResponder());
			}
													
		}
		
		private function allocateAmount():void {
			var tempAmount:Number = isNaN(parseFloat(totalPayment)) ? 0 : parseFloat(totalPayment);
			for each(var payroll:PayrollBillingTransactions in payrolls){
				// tax transaction
				if(payroll.taxTransaction != null && payroll.taxTransaction.hasFinancialTxn){						
					if(tempAmount > payroll.taxTransaction.financialAmount){
						payroll.taxTransaction.financialReturnAmountString = payroll.taxTransaction.financialAmount.toFixed(2);
						tempAmount -= payroll.taxTransaction.financialAmount; 
					}
					else{
						payroll.taxTransaction.financialReturnAmountString = tempAmount.toFixed(2);
						tempAmount -= tempAmount;
					}												 
				}
				
				// dd transactions
                for each(var ddTxn:BillingTransaction in payroll.ddTransactions){
                    if(ddTxn != null && ddTxn.hasFinancialTxn){
                        if(tempAmount > ddTxn.financialAmount){
                            ddTxn.financialReturnAmountString = ddTxn.financialAmount.toFixed(2);
                            tempAmount -= ddTxn.financialAmount;
                        }
                        else{
                            ddTxn.financialReturnAmountString = tempAmount.toFixed(2);
                            tempAmount -= tempAmount;
                        }
                    }
				}
								
				// fee transactions
				for each(var feeTxn:BillingTransaction in payroll.feeTransactions){
					if(feeTxn != null && feeTxn.hasFinancialTxn){						
						if(tempAmount > feeTxn.financialAmount){
							feeTxn.financialReturnAmountString = feeTxn.financialAmount.toFixed(2);
							tempAmount -= feeTxn.financialAmount; 
						}
						else{
							feeTxn.financialReturnAmountString = tempAmount.toFixed(2);
							tempAmount -= tempAmount;
						}												 
					}
					if(feeTxn != null && feeTxn.hasSalesTaxTxn){						
						if(tempAmount > feeTxn.salesTaxAmount){
							feeTxn.salesTaxReturnAmountString = feeTxn.salesTaxAmount.toFixed(2);
							tempAmount -= feeTxn.salesTaxAmount; 
						}
						else{
							feeTxn.salesTaxReturnAmountString = tempAmount.toFixed(2);
							tempAmount -= tempAmount;
						}												 
					}
				}
                if (payroll.handlingFeeTransaction != null) {
                    if (tempAmount > payroll.handlingFeeTransaction.financialAmount) {
                        payroll.handlingFeeTransaction.financialReturnAmountString = payroll.handlingFeeTransaction.financialAmount.toFixed(2);
                        tempAmount -= payroll.handlingFeeTransaction.financialAmount;
                    } else {
                        payroll.handlingFeeTransaction.financialReturnAmountString = tempAmount.toFixed(2);
                        tempAmount -= tempAmount;
                    }
                }
			}					
		}
				
		[Bindable ("propertyChange")]
		public function get canSelectNonStandardSettlementTypes():Boolean {
            return SAP.canPerformOperation(OperationsEnum.SELECT_NON_STANDARD_SETTLEMENT_TYPE);
        }
        
        public function goToWireExpected():void {
            topic.findPage(CompanyInspectorPageEnum.PAYROLL_CREATE_EXPECTED_WIRE_DATE).activatePage(PayrollExpectedWireViewModel.createActivator(payrollRun, action));
       	}
    }
}
