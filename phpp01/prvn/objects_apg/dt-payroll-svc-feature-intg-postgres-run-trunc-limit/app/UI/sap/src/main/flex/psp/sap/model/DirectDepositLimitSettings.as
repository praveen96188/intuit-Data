package psp.sap.model
{

    import flash.events.EventDispatcher;

    import mx.collections.ArrayCollection;
    import mx.utils.StringUtil;

    import psp.sap.formatters.SAPCurrencyFormatters;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDirectDepositLimitSettings")]
	public class DirectDepositLimitSettings extends EventDispatcher
	{
        public var defaultDDCompanyLimit:String;
        public var defaultDDEmployeeLimit:String;
        public var defaultBPCompanyLimit:String;
        public var defaultBPPayeeLimit:String;
        public var minimumNonSuspectPayrollAmount:String;
        public var maxDDCompanyLimitDefault:String;
        public var DDCompanyLimitDuration: String;
        public var DDEmployeeLimitDuration: String;
		public var consecutiveLimitViolationLimit: String;
		public var companyBankAccountVerificationAttemptLimit: String;
		public var companyBankAccountDurationLimitForVerification: String;

		[ArrayElementType("psp.sap.model.AutoLimitIncreaseTier")]
		public var autoLimitIncreaseTiers:ArrayCollection;
	}
	
}