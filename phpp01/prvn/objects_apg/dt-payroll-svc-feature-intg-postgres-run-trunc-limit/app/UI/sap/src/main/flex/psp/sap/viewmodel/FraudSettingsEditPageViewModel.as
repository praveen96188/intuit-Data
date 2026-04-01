package psp.sap.viewmodel
{
    import mx.rpc.events.ResultEvent;
    import mx.validators.NumberValidator;

    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.model.DirectDepositLimitRule;
    import psp.sap.model.FraudSettings;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.validators.SAPValidators;
    import psp.sap.viewmodel.events.EntityChangeEvent;

    public class FraudSettingsEditPageViewModel extends AbstractPartViewModel
	{

        [Bindable] [BackingProperty (context=true, hasChanged=false)] public var fraudRule:DirectDepositLimitRule;

		[Bindable] [BackingProperty] public var backingDTO:FraudSettings;
		
		public function FraudSettingsEditPageViewModel()
		{
			reloadOnSave = true;
		}

        public static function createActivator(fraudRule:DirectDepositLimitRule):Object {
            return {"fraudRule":fraudRule};
        }

        override protected function loadModelData():void {
			SAP.instance.administrationService.getFraudSettings(fraudRule.id, createLoadModelDataResponder(onLoadSucceeded));
		}
				
		protected function onLoadSucceeded(e:ResultEvent):void{			
			this.clearValidators();

            backingDTO = e.result as FraudSettings;

            // the text input is going to write down empty string instead of null
            // lets make sure the snap shot will match here
            if(fraudRule.sourceSystem == SourceSystemEnum.QBOE.code) {
                backingDTO.fraudPayeePaidMax = "";
                backingDTO.fraudPayeePaidMaxXPayrolls = "";
                backingDTO.fraudPayeePaidXTimes = "";
                backingDTO.fraudPayeeNumberOfDaysMultiplePayments = "";
                backingDTO.fraudBPMax = "";
                backingDTO.fraudBPMaxXPayrolls = "";
                backingDTO.fraudBPInactivityDays = "";
                backingDTO.fraudBPInactivityPayrollAmount = "";
                backingDTO.fraudBPNumberOfDaysForXPayments = "";
                backingDTO.fraudBPNumberOfPaymentsInXDays = "";
                backingDTO.fraudBPRoundPaidXPayrolls = "";
                backingDTO.fraudBPRoundPaidXAmount = "";
                backingDTO.fraudBPAcctUpdateMax = "";
                backingDTO.fraudBPAcctUpdateXDays = "";
            }
		}

        override protected function initializeBackingProperties():void {
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEEPaidMax", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEEPaidMaxXPayrolls", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEEAcctUpdateMax", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEEAcctUpdateXDays", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEERoundPaidXPayrolls", true, SAP.instance.configuration.specialNumberForDefault, 999, true, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEERoundPaidXAmount", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEEPaidXTimes", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEENumberOfDaysMultiplePaychecks", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEENewEmployeeAddedXDays", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEEPercentGreaterThanOtherEEs", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudPRMax", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudPRMaxXPayrolls", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEEPercentIncreaseMax", true, SAP.instance.configuration.minNum, 9999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEEPercentIncreaseMaxXPayrolls", true, SAP.instance.configuration.specialNumberForDefault, 999, true, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudPRPercentIncreaseMax", true, SAP.instance.configuration.minNum, 9999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudPRPercentIncreaseMaxXPayrolls", true, SAP.instance.configuration.specialNumberForDefault, 999, true, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudPRNumberOfDaysForXPayrolls", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudPRNumberOfPayrollsInXDays", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEENumberOfPaychecksSpikeInPay", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEEPercentGreaterThanAverage", true, SAP.instance.configuration.minNum, 9999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudEENumberOfDaysBankAcctUpdated", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudPRNumberOfPayrollsToCheckSameBank", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudPRPercentEmployeesPaidSameBank", true, SAP.instance.configuration.minNum, 9999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudPRTotalEmployeesToCheckSameBank", true, SAP.instance.configuration.minNum, 1000, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudPREmployeesSameBankAccountMax", true, 2, 9999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudDDInactivityDays", true, SAP.instance.configuration.minNum, 999, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudDDInactivityPayrollAmount", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "fraudPRXPayrollAmount", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2));


            var mFraudPayeePaidMaxValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudPayeePaidMax", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2);
            mFraudPayeePaidMaxValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(mFraudPayeePaidMaxValidator);
            var mFraudPayeePaidMaxXPayrollsValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudPayeePaidMaxXPayrolls", true, SAP.instance.configuration.minNum, 999, false, 0);
            mFraudPayeePaidMaxXPayrollsValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(mFraudPayeePaidMaxXPayrollsValidator);
            var mFraudPayeePaidXTimesValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudPayeePaidXTimes", true, SAP.instance.configuration.minNum, 999, false, 0);
            mFraudPayeePaidXTimesValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(mFraudPayeePaidXTimesValidator);
            var mFraudPayeeNumberOfDaysMultiplePaymentsValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudPayeeNumberOfDaysMultiplePayments", true, SAP.instance.configuration.minNum, 999, false, 0);
            mFraudPayeeNumberOfDaysMultiplePaymentsValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(mFraudPayeeNumberOfDaysMultiplePaymentsValidator);
            var mFraudBPMaxValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudBPMax", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2);
            mFraudBPMaxValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(mFraudBPMaxValidator);
            var mFraudBPMaxXPayrollsValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudBPMaxXPayrolls", true, SAP.instance.configuration.minNum, 999, false, 0);
            mFraudBPMaxXPayrollsValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(mFraudBPMaxXPayrollsValidator);

            var mFraudBPAcctUpdateMaxValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudBPAcctUpdateMax", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2);
            mFraudBPAcctUpdateMaxValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(mFraudBPAcctUpdateMaxValidator);
            var mFraudBPAcctUpdateXDaysValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudBPAcctUpdateXDays", true, SAP.instance.configuration.minNum, 999, false, 0);
            mFraudBPAcctUpdateXDaysValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(mFraudBPAcctUpdateXDaysValidator);

            var mFraudBPInactivityDaysValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudBPInactivityDays", true, SAP.instance.configuration.minNum, 999, false, 0);
            mFraudBPInactivityDaysValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(mFraudBPInactivityDaysValidator);
            var mFraudBPInactivityPayrollAmountValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudBPInactivityPayrollAmount", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2);
            mFraudBPInactivityPayrollAmountValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(mFraudBPInactivityPayrollAmountValidator);

            var mFraudBPNumberOfDaysForXPaymentsValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudBPNumberOfDaysForXPayments", true, 1, 999, false, 2);
            mFraudBPNumberOfDaysForXPaymentsValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(mFraudBPNumberOfDaysForXPaymentsValidator);

            var mFraudBPNumberOfPaymentsInXDaysValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudBPNumberOfPaymentsInXDays", true, 1, 999, false, 2);
            mFraudBPNumberOfPaymentsInXDaysValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(mFraudBPNumberOfPaymentsInXDaysValidator);

            var fraudBPRoundPaidXPayrollsValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudBPRoundPaidXPayrolls",true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2);
            fraudBPRoundPaidXPayrollsValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(fraudBPRoundPaidXPayrollsValidator);

            var fraudBPRoundPaidXAmountValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudBPRoundPaidXAmount", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2);
            fraudBPRoundPaidXAmountValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(fraudBPRoundPaidXAmountValidator);

            var fraudBPNumberOfPaymentsToCheckSameBankValidator:NumberValidator = SAPValidators.createNumberValidator(backingDTO, "fraudBPNumberOfPaymentsToCheckSameBank", true, SAP.instance.configuration.minNum, 999, false, 0);
            fraudBPNumberOfPaymentsToCheckSameBankValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(fraudBPNumberOfPaymentsToCheckSameBankValidator);

            var fraudBPPercentPayeesPaidSameBankValidator:NumberValidator = SAPValidators.createNumberValidator(backingDTO, "fraudBPPercentPayeesPaidSameBank", true, SAP.instance.configuration.minNum, 9999, false, 0);
            fraudBPPercentPayeesPaidSameBankValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(fraudBPPercentPayeesPaidSameBankValidator);

            var fraudBPTotalPayeesToCheckSameBankValidator:NumberValidator = SAPValidators.createNumberValidator(backingDTO, "fraudBPTotalPayeesToCheckSameBank", true, SAP.instance.configuration.minNum, 1000, false, 0);
            fraudBPTotalPayeesToCheckSameBankValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(fraudBPTotalPayeesToCheckSameBankValidator);

            var fraudBPXPayrollAmountValidator:Validator = SAPValidators.createNumberValidator(backingDTO, "fraudBPXPayrollAmount", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2);
            fraudBPXPayrollAmountValidator.enabled = fraudRule.sourceSystem == SourceSystemEnum.QBDT.code || fraudRule.sourceSystem == SourceSystemEnum.IOP.code;
            validators.push(fraudBPXPayrollAmountValidator)

        }

		override protected function executeSave():void {
			SAP.instance.administrationService.saveFraudSettings(
							backingDTO,
							fraudRule.id,
							createSaveResponder(updateLookUpService));										
		}
		
		private function updateLookUpService(e:ResultEvent):void {
			// refresh the lookup services
			SAP.instance.dispatchEvent(
				EntityChangeEvent.createEvent(
					EntityChangeEvent.ENTITY_SAVED, EntityChangeEvent.SETTINGS));			
		}			
	}
}
