package psp.sap.application.enums
{
	public class CompanySearchEnum
	{
        public static const SEARCH_SMART:String = "findSmartly";
		public static const SEARCH_BY_EIN:String = "findByFEIN";
		public static const SEARCH_BY_PSID:String = "findByPSID";
		public static const SEARCH_BY_LEGAL_NAME:String = "findByLegalNamePattern";        
        public static const SEARCH_BY_SERVICE_KEY:String = "findByServiceKey";
        public static const SEARCH_BY_LICENSE_NUMBER:String = "findByLicenseNumber";
        public static const SEARCH_BY_CAN:String = "findByCAN";
        public static const SEARCH_VMP_EMPLOYEE_BY_SSN:String = "findVmpEmployeeBySSN";
        public static const SEARCH_VMP_EMPLOYEE_BY_EMAIL:String = "findVmpEmployeeByEmail";
        public static const SEARCH_BY_REGISTRATION_NUMBER:String = "findByRegistrationNumber";
		public static const SEARCH_BY_CFR:String = "findByCFR";
		public static const SEARCH_BY_REALMID:String = "findByRealmId";
       // public static const SEARCH_VMP_EMPLOYEE_BY_NAME:String = "findVmpEmployeeByName";

		public function CompanySearchEnum()
		{
		}

	}
}