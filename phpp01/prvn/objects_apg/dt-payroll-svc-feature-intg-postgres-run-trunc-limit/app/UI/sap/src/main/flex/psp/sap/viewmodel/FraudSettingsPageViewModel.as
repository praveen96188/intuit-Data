package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
    import mx.utils.StringUtil;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.AdministrationInspectorPageEnum;
    import psp.sap.model.DirectDepositLimitRule;
    import psp.sap.model.FraudSettings;


    public class FraudSettingsPageViewModel extends AbstractPartViewModel
	{
		private var mFraudEEPaidMax:String = "";
		private var mFraudEEPaidMaxXPayrolls:String = "";
        private var mFraudEEAcctUpdateMax:String = "";
        private var mFraudEEAcctUpdateXDays:String = "";
		private var mFraudEERoundPaidXPayrolls:String = "";
        private var mFraudEERoundPaidXAmount:String = "";
        private var mFraudBPRoundPaidXPayrolls:String = "";
        private var mFraudBPRoundPaidXAmount:String = "";
		private var mFraudPRMax:String = "";
		private var mFraudPRMaxXPayrolls:String = "";
		private var mFraudEEPercentIncreaseMax:String = "";
		private var mFraudEEPercentIncreaseMaxXPayrolls:String = "";
		private var mFraudPRPercentIncreaseMax:String = "";
		private var mFraudPRPercentIncreaseMaxXPayrolls:String = "";
		private var mFraudPRNumberOfDaysForXPayrolls:String = "";
		private var mFraudPRNumberOfPayrollsInXDays:String = "";
		private var mFraudEEPaidXTimes:String = "";
		private var mFraudEENumberOfDaysMultiplePaychecks:String = "";
		private var mFraudEENewEmployeeAddedXDays:String = "";
		private var mFraudEEPercentGreaterThanOtherEEs:String = "";
        private var mFraudBPXPayrollAmount:String = "";
        private var mFraudPRXPayrollAmount:String = "";
        private var mFraudBPAcctUpdateMax:String = "";
        private var mFraudBPAcctUpdateXDays:String = "";

        [Bindable] public var fraudEENumberOfPaychecksSpikeInPay:String = "";
        [Bindable] public var fraudEEPaidXAmtWithinYAcctUpdateDays:String = "";
        [Bindable] public var fraudEEPercentGreaterThanAverage:String= "";
        [Bindable] public var fraudEENumberOfDaysBankAcctUpdated:String= "";
        [Bindable] public var fraudPRNumberOfPayrollsToCheckSameBank:String= "";
        [Bindable] public var fraudBPNumberOfPaymentsToCheckSameBank:String= "";
        [Bindable] public var fraudPRPercentEmployeesPaidSameBank:String= "";
        [Bindable] public var fraudBPPercentPayeesPaidSameBank:String= "";
        [Bindable] public var fraudPRTotalEmployeeToCheckSameBank:String = "";
        [Bindable] public var fraudBPTotalPayeesToCheckSameBank:String = "";
        [Bindable] public var fraudPREmployeesSameBankAccountMax:String = "";
        [Bindable] public var fraudPayeePaidXAmtWithinYAcctUpdateDays:String = "";
        [Bindable] public var fraudPayeePaidMax:String = "";
        [Bindable] public var fraudPayeePaidMaxXPayrolls:String = "";
        [Bindable] public var fraudBPMax:String = "";
        [Bindable] public var fraudBPMaxXPayrolls:String = "";
        [Bindable] public var fraudPayeePaidXTimes:String = "";
        [Bindable] public var fraudPayeeNumberOfDaysMultiplePayments:String = "";
        [Bindable] public var fraudDDInactivityDays:String = "";
        [Bindable] public var fraudDDInactivityPayrollAmount:String = "";
        [Bindable] public var fraudBPInactivityDays:String = "";
        [Bindable] public var fraudBPInactivityPayrollAmount:String = "";
        [Bindable] public var fraudBPNumberOfDaysForXPayments:String = "";
        [Bindable] public var fraudBPNumberOfPaymentsInXDays:String = "";



        [ArrayElementType("psp.sap.model.DirectDepositLimitRule")]
        [Bindable] public var fraudRules:ArrayCollection;


        private var mFraudRule:DirectDepositLimitRule;

		private var mFraudSettings:FraudSettings = new FraudSettings();
		
		public function FraudSettingsPageViewModel()
		{			

		}
		
		

		
		// property for fraudPRPercentIncreaseMaxXPayrolls : String
		[Bindable]
		public function get fraudEEPaidMax(): String{
			return mFraudEEPaidMax;
		}
		
		public function set fraudEEPaidMax(value:String):void {
			mFraudEEPaidMax =  StringUtil.trim(value);			
		}

		// property for fraudEEPaidMaxXPayrolls : String
		[Bindable]
		public function get fraudEEPaidMaxXPayrolls(): String{
			return mFraudEEPaidMaxXPayrolls;
		}
		
		public function set fraudEEPaidMaxXPayrolls(value:String):void {
			mFraudEEPaidMaxXPayrolls =  StringUtil.trim(value);			
		}

        // property for fraudEEAcctUpdateMax : String
        [Bindable]
        public function get fraudEEAcctUpdateMax(): String{
            return mFraudEEAcctUpdateMax;
        }

        public function set fraudEEAcctUpdateMax(value:String):void {
            mFraudEEAcctUpdateMax =  StringUtil.trim(value);
        }

        // property for fraudEEAcctUpdateXDays : String
        [Bindable]
        public function get fraudEEAcctUpdateXDays(): String{
            return mFraudEEAcctUpdateXDays;
        }

        public function set fraudEEAcctUpdateXDays(value:String):void {
            mFraudEEAcctUpdateXDays =  StringUtil.trim(value);
        }

		// property for fraudEERoundPaidXPayrolls : String
		[Bindable]
		public function get fraudEERoundPaidXPayrolls(): String{
			return mFraudEERoundPaidXPayrolls;
		}
		
		public function set fraudEERoundPaidXPayrolls(value:String):void {
			mFraudEERoundPaidXPayrolls = StringUtil.trim(value);			
		}

        [Bindable]
        public function get fraudEERoundPaidXAmount(): String{
            return mFraudEERoundPaidXPayrolls;
        }

        public function set fraudEERoundPaidXAmount(value:String):void {
            mFraudEERoundPaidXPayrolls = StringUtil.trim(value);
        }

        [Bindable]
        public function get fraudBPRoundPaidXPayrolls(): String{
            return mFraudBPRoundPaidXPayrolls;
        }

        public function set fraudBPRoundPaidXPayrolls(value:String):void {
            mFraudBPRoundPaidXPayrolls = StringUtil.trim(value);
        }

        [Bindable]
        public function get fraudBPRoundPaidXAmount(): String{
            return mFraudBPRoundPaidXAmount;
        }

        public function set fraudBPRoundPaidXAmount(value:String):void {
            mFraudBPRoundPaidXAmount = StringUtil.trim(value);
        }

        // property for fraudBPAcctUpdateMax : String
        [Bindable]
        public function get fraudBPAcctUpdateMax(): String{
            return mFraudBPAcctUpdateMax;
        }

        public function set fraudBPAcctUpdateMax(value:String):void {
            mFraudBPAcctUpdateMax =  StringUtil.trim(value);
        }

        // property for fraudBPAcctUpdateXDays : String
        [Bindable]
        public function get fraudBPAcctUpdateXDays(): String{
            return mFraudBPAcctUpdateXDays;
        }

        public function set fraudBPAcctUpdateXDays(value:String):void {
            mFraudBPAcctUpdateXDays =  StringUtil.trim(value);
        }

        // property for fraudEERoundPaidXPayrolls : String
		[Bindable]
		public function get fraudEEPaidXTimes(): String{
			return mFraudEEPaidXTimes;
		}
		
		public function set fraudEEPaidXTimes(value:String):void {
			mFraudEEPaidXTimes = StringUtil.trim(value);			
		}	
	
		// property for fraudEENumberOfDaysMultiplePaychecks : String
		[Bindable]
		public function get fraudEENumberOfDaysMultiplePaychecks(): String{
			return mFraudEENumberOfDaysMultiplePaychecks;
		}
		
		public function set fraudEENumberOfDaysMultiplePaychecks(value:String):void {
			mFraudEENumberOfDaysMultiplePaychecks = StringUtil.trim(value);			
		}	

		// property for fraudEENewEmployeeAddedXDays : String
		[Bindable]
		public function get fraudEENewEmployeeAddedXDays(): String{
			return mFraudEEPercentGreaterThanOtherEEs;
		}
		
		public function set fraudEENewEmployeeAddedXDays(value:String):void {
			mFraudEEPercentGreaterThanOtherEEs = StringUtil.trim(value);			
		}	
	
		// property for fraudEEPercentGreaterThanOtherEEs : String
		[Bindable]
		public function get fraudEEPercentGreaterThanOtherEEs(): String{
			return mFraudEENewEmployeeAddedXDays;
		}
		
		public function set fraudEEPercentGreaterThanOtherEEs(value:String):void {
			mFraudEENewEmployeeAddedXDays = StringUtil.trim(value);			
		}	
						
		// property for fraudPRMax : String
		[Bindable]
		public function get fraudPRMax(): String{
			return mFraudPRMax;
		}
		
		public function set fraudPRMax(value:String):void {
			mFraudPRMax =  StringUtil.trim(value);			
		}		
		
		// property for fraudPRMaxXPayrolls:String
		[Bindable]
		public function get fraudPRMaxXPayrolls():String {
			return mFraudPRMaxXPayrolls;
		}
		
		public function set fraudPRMaxXPayrolls(value:String):void {
			mFraudPRMaxXPayrolls =  StringUtil.trim(value);			
		}		

		// property for fraudEEPercentIncreaseMax:String
		[Bindable]
		public function get fraudEEPercentIncreaseMax(): String{
			return mFraudEEPercentIncreaseMax;
		}
		
		public function set fraudEEPercentIncreaseMax(value:String):void {
			mFraudEEPercentIncreaseMax =  StringUtil.trim(value);			
		}
		
		// property for fraudEEPercentIncreaseMaxXPayrolls:String
		[Bindable]
		public function get fraudEEPercentIncreaseMaxXPayrolls(): String{
			return mFraudEEPercentIncreaseMaxXPayrolls;
		}
		
		public function set fraudEEPercentIncreaseMaxXPayrolls(value:String):void {
			mFraudEEPercentIncreaseMaxXPayrolls =  StringUtil.trim(value);			
		}

		// property for fraudPRPercentIncreaseMax:String
		[Bindable]
		public function get fraudPRPercentIncreaseMax(): String{
			return mFraudPRPercentIncreaseMax;
		}
		
		public function set fraudPRPercentIncreaseMax(value:String):void {
			mFraudPRPercentIncreaseMax =  StringUtil.trim(value);			
		}

        [Bindable]
        public function get fraudBPXPayrollAmount(): String{
            return mFraudBPXPayrollAmount;
        }

        public function set fraudBPXPayrollAmount(value:String):void {
            mFraudBPXPayrollAmount = StringUtil.trim(value);
        }

        [Bindable]
        public function get fraudPRXPayrollAmount(): String{
            return mFraudPRXPayrollAmount;
        }

        public function set fraudPRXPayrollAmount(value:String):void {
            mFraudPRXPayrollAmount = StringUtil.trim(value);
        }


		// property for fraudPRPercentIncreaseMaxXPayrolls : String
		[Bindable]
		public function get fraudPRPercentIncreaseMaxXPayrolls(): String{
			return mFraudPRPercentIncreaseMaxXPayrolls;
		}
		
		public function set fraudPRPercentIncreaseMaxXPayrolls(value:String):void {
			mFraudPRPercentIncreaseMaxXPayrolls =  StringUtil.trim(value);			
		}


		// property for fraudPRNumberOfDaysForXPayrolls : String
		[Bindable]
		public function get fraudPRNumberOfDaysForXPayrolls(): String{
			return mFraudPRNumberOfDaysForXPayrolls;
		}
		
		public function set fraudPRNumberOfDaysForXPayrolls(value:String):void {
			mFraudPRNumberOfDaysForXPayrolls =  StringUtil.trim(value);			
		}

		// property for fraudPRNumberOfPayrollsInXDays : String
		[Bindable]
		public function get fraudPRNumberOfPayrollsInXDays(): String{
			return mFraudPRNumberOfPayrollsInXDays;
		}
		
		public function set fraudPRNumberOfPayrollsInXDays(value:String):void {
			mFraudPRNumberOfPayrollsInXDays =  StringUtil.trim(value);			
		}
		
		protected function get fraudSettings():FraudSettings {
			return mFraudSettings;
		}
		
		protected function set fraudSettings(value:FraudSettings):void {
			 mFraudSettings = value;
		}
		
		override protected function initializeBackingProperties():void {	
			fraudEEPaidMax = fraudSettings.fraudEEPaidMax;
			fraudEEPaidMaxXPayrolls = fraudSettings.fraudEEPaidMaxXPayrolls;
            fraudEEAcctUpdateMax = fraudSettings.fraudEEAcctUpdateMax;
            fraudEEAcctUpdateXDays = fraudSettings.fraudEEAcctUpdateXDays;
			fraudEERoundPaidXPayrolls = fraudSettings.fraudEERoundPaidXPayrolls;
            fraudEERoundPaidXAmount = fraudSettings.fraudEERoundPaidXAmount;
            fraudBPRoundPaidXPayrolls = fraudSettings.fraudBPRoundPaidXPayrolls;
            fraudBPRoundPaidXAmount = fraudSettings.fraudBPRoundPaidXAmount;
			fraudPRMax = fraudSettings.fraudPRMax;
			fraudPRMaxXPayrolls = fraudSettings.fraudPRMaxXPayrolls;
            fraudPRXPayrollAmount = fraudSettings.fraudPRXPayrollAmount;
			fraudEEPercentIncreaseMax = fraudSettings.fraudEEPercentIncreaseMax;
			fraudEEPercentIncreaseMaxXPayrolls = fraudSettings.fraudEEPercentIncreaseMaxXPayrolls;
			fraudPRPercentIncreaseMax = fraudSettings.fraudPRPercentIncreaseMax;
			fraudPRPercentIncreaseMaxXPayrolls = fraudSettings.fraudPRPercentIncreaseMaxXPayrolls;
			fraudPRNumberOfDaysForXPayrolls = fraudSettings.fraudPRNumberOfDaysForXPayrolls;
			fraudPRNumberOfPayrollsInXDays = fraudSettings.fraudPRNumberOfPayrollsInXDays;
			fraudEEPaidXTimes = fraudSettings.fraudEEPaidXTimes;
			fraudEENumberOfDaysMultiplePaychecks = fraudSettings.fraudEENumberOfDaysMultiplePaychecks;
			fraudEENewEmployeeAddedXDays = fraudSettings.fraudEENewEmployeeAddedXDays;
			fraudEEPercentGreaterThanOtherEEs = fraudSettings.fraudEEPercentGreaterThanOtherEEs;
			fraudEEPercentGreaterThanOtherEEs = fraudSettings.fraudEEPercentGreaterThanOtherEEs;
            fraudEEPaidXAmtWithinYAcctUpdateDays=fraudSettings.fraudEEPaidXAmtWithinYAcctUpdateDays;
            fraudEENumberOfPaychecksSpikeInPay = fraudSettings.fraudEENumberOfPaychecksSpikeInPay;
			fraudEEPercentGreaterThanAverage = fraudSettings.fraudEEPercentGreaterThanAverage;
	        fraudEENumberOfDaysBankAcctUpdated = fraudSettings.fraudEENumberOfDaysBankAcctUpdated;
	        fraudPRNumberOfPayrollsToCheckSameBank = fraudSettings.fraudPRNumberOfPayrollsToCheckSameBank;
            fraudBPNumberOfPaymentsToCheckSameBank = fraudSettings.fraudBPNumberOfPaymentsToCheckSameBank;
	        fraudPRPercentEmployeesPaidSameBank = fraudSettings.fraudPRPercentEmployeesPaidSameBank;
            fraudBPPercentPayeesPaidSameBank = fraudSettings.fraudBPPercentPayeesPaidSameBank;
            fraudPRTotalEmployeeToCheckSameBank = fraudSettings.fraudPRTotalEmployeesToCheckSameBank;
            fraudBPTotalPayeesToCheckSameBank = fraudSettings.fraudBPTotalPayeesToCheckSameBank;
            fraudPREmployeesSameBankAccountMax = fraudSettings.fraudPREmployeesSameBankAccountMax;
            fraudDDInactivityDays = fraudSettings.fraudDDInactivityDays;
            fraudDDInactivityPayrollAmount = fraudSettings.fraudDDInactivityPayrollAmount;

            fraudBPAcctUpdateMax = fraudSettings.fraudBPAcctUpdateMax;
            fraudBPAcctUpdateXDays = fraudSettings.fraudBPAcctUpdateXDays;
            fraudPayeePaidXAmtWithinYAcctUpdateDays=fraudSettings.fraudPayeePaidXAmtWithinYAcctUpdateDays;
            fraudPayeePaidMax = fraudSettings.fraudPayeePaidMax;
            fraudPayeePaidMaxXPayrolls = fraudSettings.fraudPayeePaidMaxXPayrolls;
            fraudBPMax = fraudSettings.fraudBPMax;
            fraudBPMaxXPayrolls = fraudSettings.fraudBPMaxXPayrolls;
            fraudPayeePaidXTimes = fraudSettings.fraudPayeePaidXTimes;
            fraudPayeeNumberOfDaysMultiplePayments = fraudSettings.fraudPayeeNumberOfDaysMultiplePayments;
            fraudBPInactivityDays = fraudSettings.fraudBPInactivityDays;
            fraudBPInactivityPayrollAmount = fraudSettings.fraudBPInactivityPayrollAmount;
            fraudBPNumberOfDaysForXPayments = fraudSettings.fraudBPNumberOfDaysForXPayments;
            fraudBPNumberOfPaymentsInXDays = fraudSettings.fraudBPNumberOfPaymentsInXDays;
            fraudBPXPayrollAmount = fraudSettings.fraudBPXPayrollAmount;
		}
		
		override protected function loadModelData():void {
            if (fraudRules == null) {
                SAP.instance.administrationService.getFraudRules(createLoadModelDataResponder(onFraudRulesLoaded))
            } else if (fraudRule != null) {
                SAP.instance.administrationService.getFraudSettings(fraudRule.id, createLoadModelDataResponder(onSettingsLoaded));
            } else {
                modelDataLoaded();
            }

		}

        protected function onFraudRulesLoaded(e:ResultEvent):void {
            fraudRules = ArrayCollection(e.result);
        }

        protected function onSettingsLoaded(e:ResultEvent):void{
            fraudSettings = FraudSettings(e.result);
        }
		
        [Bindable]
        public function get fraudRule():DirectDepositLimitRule {
            return mFraudRule;
        }

        public function set fraudRule(value:DirectDepositLimitRule):void {
            if(value != mFraudRule && value != null)
            {
                mFraudRule = value;
                this.refresh();
            }
        }

        override protected function onActivationComplete():void {
            if(fraudRule == null && fraudRules != null && fraudRules.length > 0) {
                fraudRule = (DirectDepositLimitRule)(fraudRules.getItemAt(0));
            }
        }

		public function editSettings():void {
            topic.findPage(AdministrationInspectorPageEnum.FRAUD_SETTINGS_EDIT).activatePage(FraudSettingsEditPageViewModel.createActivator(fraudRule));
		}

	}
}