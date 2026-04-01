package psp.sap.model
{
    import intuit.sbd.flex.framework.model.EntityObject;

    import mx.logging.ILogger;

    import psp.sap.application.ClientLoggingTarget;
    import psp.sap.application.SAP;
    import psp.sap.service.AbstractCompanyService;

    [ValueEquals(exclude="quickbooksInfo")]
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompany")]
	public class Company extends EntityObject
	{
		[Transient]
		private var logger:ILogger = ClientLoggingTarget.getLogger(this);

        public var gseq:String;
		public var sourceSystemCd : String;	
		public var companyId : String;
        public var customerId : String;
		public var fein : String;	
		public var legalName : String;
		public var DBA : String;	
		public var notificationEmail : String;
        public var isEditable:Boolean;

        public var isAssisted:Boolean;
        public var isDIY:Boolean;
        public var companyServiceState:CompanyServiceState;
        public var canChangePriceType:Boolean;
        public var hasCompanyAgencies:Boolean;
        public var isAssistedServiceCancelled:Boolean;
        public var isVmp:Boolean;
        public var iamRealmId:String;
		public var isMoneyMovementOnboardingEnabled:Boolean;

        [Transient]
        [Bindable("propertyChange")]
        public function get einLabel():String {
            if(fein != null){
                return fein.substring(0,2) + '-' + fein.substring(2, fein.length);
            }
            return "";
        }
		
		public var payrollFrequencyCd : String;
		

		[Transient]
		public function isQBDTCompany():Boolean {
			return sourceSystemCd == SourceSystemEnum.QBDT.code;
		}

		override public function toString(): String {
			return legalName + "  (EIN: " + fein + ")";
		}

		override public function get key():String {
			return sourceSystemCd + ":" + companyId;
		}
		
		override public function display():void
		{
			logger.info("display() called on company sourceSystemCd = " + sourceSystemCd + " sourceCompanyId = " + companyId); 
			AbstractCompanyService(SAP.instance.companyService).display(this);
		}
		
		override public function close():void
		{
		}

		public function get companyKey():CompanyKey {
			var companyKey:CompanyKey = new CompanyKey();
			companyKey.companyId = this.companyId;
			companyKey.sourceSystemCd = this.sourceSystemCd;
			return companyKey;
		}

        //noinspection JSUnusedGlobalSymbols
        public function set companyServiceStateCd(value:String):void {
            if (value == null) {
                companyServiceState = null;
            }
            companyServiceState = CompanyServiceState.valueOf(value);
        }
    }
}
