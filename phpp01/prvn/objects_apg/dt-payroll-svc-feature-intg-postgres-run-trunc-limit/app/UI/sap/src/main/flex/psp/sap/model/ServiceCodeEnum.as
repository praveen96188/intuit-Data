package psp.sap.model
{
	public class ServiceCodeEnum
	{
        public static const DIRECT_DEPOSIT_DISPLAY:String = "Direct Deposit";
        public static const BILL_PAYMENT_DISPLAY:String = "Vendor Payments";
        public static const THIRD_PARTY_401K_DISPLAY:String = "401k";
        public static const CHECK_DISTRIBUTION_DISPLAY:String = "Check Distribution";
        public static const CLOUD_DISPLAY:String = "Intuit Services";
        public static const TAX_DISPLAY:String = "Assisted";
        public static const RISK_DISPLAY:String = "Risk Assessment";
        public static const WORKERS_COMP_DISPLAY:String = "Workers Compensation";
        public static const VIEW_MY_PAYCHECK_DISPLAY:String = "ViewMyPaycheck";
        public static const CLOUD_V2_DISPLAY:String = "Cloud V2";
        public static const GUIDELINE_DISPLAY:String = "Guideline401k";
        public static const Cloud_V3_DISPLAY:String = "Cloud V3";

		public static const TAX:ServiceCodeEnum = new ServiceCodeEnum("Tax", TAX_DISPLAY);
		public static const DIRECT_DEPOSIT:ServiceCodeEnum = new ServiceCodeEnum("DirectDeposit", DIRECT_DEPOSIT_DISPLAY);
        public static const BILL_PAYMENT:ServiceCodeEnum = new ServiceCodeEnum("BillPayment", BILL_PAYMENT_DISPLAY);
        public static const THIRD_PARTY_401K:ServiceCodeEnum = new ServiceCodeEnum("ThirdParty401k", THIRD_PARTY_401K_DISPLAY);
        public static const CHECK_DISTRIBUTION:ServiceCodeEnum = new ServiceCodeEnum("CheckDistribution", CHECK_DISTRIBUTION_DISPLAY);
        public static const CLOUD:ServiceCodeEnum = new ServiceCodeEnum("Cloud", CLOUD_DISPLAY);
        public static const RISK_ASSESSMENT:ServiceCodeEnum = new ServiceCodeEnum("RiskAssessment", RISK_DISPLAY);
        public static const WORKERS_COMP:ServiceCodeEnum = new ServiceCodeEnum("WorkersComp", WORKERS_COMP_DISPLAY);
        public static const VIEW_MY_PAYCHECK:ServiceCodeEnum = new ServiceCodeEnum("ViewMyPaycheck", VIEW_MY_PAYCHECK_DISPLAY);
        public static const CLOUD_V2:ServiceCodeEnum = new ServiceCodeEnum("CloudV2", CLOUD_V2_DISPLAY);
        public static const GUIDELINE:ServiceCodeEnum = new ServiceCodeEnum("Guideline401k", GUIDELINE_DISPLAY);
        public static const Cloud_V3:ServiceCodeEnum = new ServiceCodeEnum('CloudV3',Cloud_V3_DISPLAY);

        // event details use the service name so we'll map those also
        public static const DIRECT_DEPOSIT_ED:ServiceCodeEnum = new ServiceCodeEnum("Direct Deposit Service", DIRECT_DEPOSIT_DISPLAY);
        public static const BILL_PAYMENT_ED:ServiceCodeEnum = new ServiceCodeEnum("Bill Payment Service", BILL_PAYMENT_DISPLAY);
        public static const THIRD_PARTY_401K_ED:ServiceCodeEnum = new ServiceCodeEnum("Third Party 401k Service", THIRD_PARTY_401K_DISPLAY);
        public static const CHECK_DISTRIBUTION_ED:ServiceCodeEnum = new ServiceCodeEnum("Check Distribution Service", CHECK_DISTRIBUTION_DISPLAY);
        public static const TAX_ED:ServiceCodeEnum = new ServiceCodeEnum("Assisted Service", TAX_DISPLAY);
        public static const CLOUD_ED:ServiceCodeEnum = new ServiceCodeEnum(CLOUD_DISPLAY);
        public static const RISK_ASSESSMENT_ED:ServiceCodeEnum = new ServiceCodeEnum("Risk Assessment", RISK_DISPLAY);
        public static const WORKERS_COMP_ED:ServiceCodeEnum = new ServiceCodeEnum("Workers Compensation Service", WORKERS_COMP_DISPLAY);
        public static const VIEW_MY_PAYCHECK_ED:ServiceCodeEnum = new ServiceCodeEnum("ViewMyPaycheck", VIEW_MY_PAYCHECK_DISPLAY);
        public static const CLOUD_V2_ED:ServiceCodeEnum = new ServiceCodeEnum("CloudV2", CLOUD_V2_DISPLAY);
        public static const GUIDELINE_ED:ServiceCodeEnum = new ServiceCodeEnum("Guideline401k", GUIDELINE_DISPLAY);
        public static const Cloud_V3_ED:ServiceCodeEnum = new ServiceCodeEnum('CloudV3',Cloud_V3_DISPLAY);

        public static const values:Array = [TAX, DIRECT_DEPOSIT, BILL_PAYMENT, BILL_PAYMENT, THIRD_PARTY_401K, CHECK_DISTRIBUTION, CLOUD, RISK_ASSESSMENT,
            WORKERS_COMP, DIRECT_DEPOSIT_ED, BILL_PAYMENT_ED, THIRD_PARTY_401K_ED, CHECK_DISTRIBUTION_ED, TAX_ED, CLOUD_ED,
            RISK_ASSESSMENT_ED, WORKERS_COMP_ED, VIEW_MY_PAYCHECK, CLOUD_V2, VIEW_MY_PAYCHECK_ED, CLOUD_V2_ED,
            GUIDELINE, GUIDELINE_ED,Cloud_V3, Cloud_V3_ED
        ];

        private var mCode:String;
		[Bindable]
        public var label:String;

		public function ServiceCodeEnum(code:String, label:String = null)
		{
			mCode = code;
			this.label = (label != null ? label : code);
		}


		public function get code():String {
			return mCode;
		}

		public function toString():String {
			return label;
		}

		public static function valueOf(value:String):ServiceCodeEnum {
			for each (var enumValue:ServiceCodeEnum in values) {
				if (enumValue.code == value)
					return enumValue;
			}

			return null;
		}
	}
}