package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
    import mx.validators.NumberValidator;

    import psp.sap.application.SAP;
    import psp.sap.model.AutoLimitIncreaseTier;
    import psp.sap.model.DirectDepositLimitRule;
    import psp.sap.model.DirectDepositLimitSettings;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.validators.SAPValidators;
    import psp.sap.viewmodel.events.EntityChangeEvent;

    public class AdministrationSettingsEditPageViewModel extends AbstractPartViewModel
    {

        [Bindable] [BackingProperty (context=true, hasChanged=false)] public var limitRule:DirectDepositLimitRule;

        [Bindable] [BackingProperty] public var autoLimitIncreaseTiers:ArrayCollection = null;
        [Bindable] [BackingProperty] public var backingDTO:DirectDepositLimitSettings = new DirectDepositLimitSettings();

        public function AdministrationSettingsEditPageViewModel()
        {
            reloadOnSave = true;
        }

        public static function createActivator(limitRule:DirectDepositLimitRule):Object {
            return {"limitRule":limitRule};
        }
 
        override protected function loadModelData():void {
            SAP.instance.administrationService.getDirectDepositLimitSettings(limitRule.id, createLoadModelDataResponder(onLoadSucceeded,null));
        }

        protected function onLoadSucceeded(e:ResultEvent):void{
            this.clearValidators();

            backingDTO = e.result as DirectDepositLimitSettings;
            autoLimitIncreaseTiers = backingDTO.autoLimitIncreaseTiers;
        }

        override protected function executeSave():void {
            SAP.instance.administrationService.saveDirectDepositLimitSettings(
                    backingDTO,
                    limitRule.id,
                    createSaveResponder(updateLookUpService));
        }

        private function updateLookUpService(e:ResultEvent):void {
            // refresh the lookup services
            SAP.instance.dispatchEvent(
                    EntityChangeEvent.createEvent(
                            EntityChangeEvent.ENTITY_SAVED, EntityChangeEvent.SETTINGS));
        }

        override protected function initializeBackingProperties():void {            
            validators.push(SAPValidators.createNumberValidator(backingDTO, "defaultDDCompanyLimit", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "defaultDDEmployeeLimit", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "minimumNonSuspectPayrollAmount", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "maxDDCompanyLimitDefault", true, SAP.instance.configuration.minAllowedCurrencyValue, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "DDCompanyLimitDuration", true, SAP.instance.configuration.minNum, SAP.instance.configuration.maxNum, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "DDEmployeeLimitDuration", true, SAP.instance.configuration.minNum, SAP.instance.configuration.maxNum, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "companyBankAccountVerificationAttemptLimit", true, SAP.instance.configuration.minNum, SAP.instance.configuration.maxNum, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "companyBankAccountDurationLimitForVerification", true, SAP.instance.configuration.minNum, Number.MAX_VALUE, false, 0));
            validators.push(SAPValidators.createNumberValidator(backingDTO, "consecutiveLimitViolationLimit", true, SAP.instance.configuration.minNum, SAP.instance.configuration.maxNum, false, 0));

            for each (var tierCopy:AutoLimitIncreaseTier in backingDTO.autoLimitIncreaseTiers) {
                validators.push(SAPValidators.createNumberValidator(tierCopy, "payrollsRun", true, 0, 128, false, 0));
                validators.push(SAPValidators.createNumberValidator(tierCopy, "daysSinceFirstPayroll", true, 1, 1440, false, 0));
                validators.push(SAPValidators.createNumberValidator(tierCopy, "increaseMultiplier", true, 1, 4, false, 2));
                validators.push(SAPValidators.createNumberValidator(tierCopy, "companyCap", true, 1, 9000000, false, 0));
                validators.push(SAPValidators.createNumberValidator(tierCopy, "employeeCap", true, 1, 4500000, false, 0));
            }
        }
    }
}