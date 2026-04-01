package psp.sap.viewmodel
{
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
    import mx.utils.StringUtil;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.AdministrationInspectorPageEnum;
    import psp.sap.model.DirectDepositLimitRule;
    import psp.sap.model.DirectDepositLimitSettings;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class AdministrationSettingsPageViewModel extends AbstractPartViewModel
    {
        protected var directDepositLimitSettings:DirectDepositLimitSettings = new DirectDepositLimitSettings();

        private var mLimitRule:DirectDepositLimitRule;
        private var mAutoLimitIncreaseTiers:ArrayCollection = new ArrayCollection();

        [Bindable] public var directDepositLimitRules:ArrayCollection;
        [Bindable] public var ddLimitPerCompany:String = "";
        [Bindable] public var numDaysPerCompany:String = "";
        [Bindable] public var ddLimitPerEmployee:String = "";
        [Bindable] public var numDaysPerEmployee:String = "";
        [Bindable] public var numViolations:String = "";
        [Bindable] public var numAttemptsVerifyLimits:String = "";
        [Bindable] public var numDaysVerifyLimits:String = "";
        [Bindable] public var maxPayrollAmt:String = "";
        [Bindable] public var minPayrollAmt:String = "";
        [Bindable] public var defaultBPCompanyLimit:String = "";
        [Bindable] public var defaultBPPayeeLimit:String = "";

        public function AdministrationSettingsPageViewModel()
        {
        }

        [Bindable]
        public function get autoLimitIncreaseTiers():ArrayCollection {
            return mAutoLimitIncreaseTiers;
        }

        public function set autoLimitIncreaseTiers(value:ArrayCollection):void {
            if(value == null) {
                mAutoLimitIncreaseTiers = new ArrayCollection();
            } else {
                mAutoLimitIncreaseTiers = value;
            }
        }

        [Bindable]
        public function get limitRule():DirectDepositLimitRule {
            return mLimitRule;
        }

        public function set limitRule(value:DirectDepositLimitRule):void {
            if(value != mLimitRule && value != null)
            {
                mLimitRule = value;
                this.refresh();
            }
        }

        override protected function initializeBackingProperties():void {
            ddLimitPerCompany = StringUtil.trim(directDepositLimitSettings.defaultDDCompanyLimit);
            numDaysPerCompany = StringUtil.trim(directDepositLimitSettings.DDCompanyLimitDuration);
            ddLimitPerEmployee = StringUtil.trim(directDepositLimitSettings.defaultDDEmployeeLimit);
            numDaysPerEmployee = StringUtil.trim(directDepositLimitSettings.DDEmployeeLimitDuration);
            numViolations = StringUtil.trim(directDepositLimitSettings.consecutiveLimitViolationLimit);
            numAttemptsVerifyLimits = StringUtil.trim(directDepositLimitSettings.companyBankAccountVerificationAttemptLimit);
            numDaysVerifyLimits = StringUtil.trim(directDepositLimitSettings.companyBankAccountDurationLimitForVerification);
            minPayrollAmt = StringUtil.trim(directDepositLimitSettings.minimumNonSuspectPayrollAmount);
            maxPayrollAmt = StringUtil.trim(directDepositLimitSettings.maxDDCompanyLimitDefault);
            autoLimitIncreaseTiers = directDepositLimitSettings.autoLimitIncreaseTiers;
            defaultBPCompanyLimit = StringUtil.trim(directDepositLimitSettings.defaultBPCompanyLimit);
            defaultBPPayeeLimit = StringUtil.trim(directDepositLimitSettings.defaultBPPayeeLimit);
        }

        override protected function loadModelData():void {
            if(directDepositLimitRules == null) {
                SAP.instance.administrationService.getLimitRules(createLoadModelDataResponder(onLoadSucceeded));
            } else if(limitRule != null) {
                SAP.instance.administrationService.getDirectDepositLimitSettings(limitRule.id, createLoadModelDataResponder(onLoadSettingsSucceeded));
            } else {
                modelDataLoaded();
            }
        }

        protected function onLoadSucceeded(e:ResultEvent):void{
            directDepositLimitRules = e.result as ArrayCollection;
        }

        protected function onLoadSettingsSucceeded(e:ResultEvent):void{
            directDepositLimitSettings = e.result as DirectDepositLimitSettings;
        }

        override protected function onActivationComplete():void {
            if(limitRule == null && directDepositLimitRules != null && directDepositLimitRules.length > 0) {
                limitRule = (DirectDepositLimitRule)(directDepositLimitRules.getItemAt(0));
            }
        }

        public function editSettings():void {
            topic.findPage(AdministrationInspectorPageEnum.SETTINGS_EDIT).activatePage(AdministrationSettingsEditPageViewModel.createActivator(limitRule));
        }
    }
}