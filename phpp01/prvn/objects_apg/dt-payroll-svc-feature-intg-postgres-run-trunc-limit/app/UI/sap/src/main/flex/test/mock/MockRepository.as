package test.mock
{
    import flash.utils.Dictionary;

    import psp.sap.model.Company;

    import test.mock.data.ACHOffloadData;
    import test.mock.data.AdministrativeData;
    import test.mock.data.CompanyData;
    import test.mock.data.EmployeeData;
    import test.mock.data.PayrollData;
    import test.mock.data.TaxData;
    import test.mock.data.UserData;

    public class MockRepository
	{
		public static const TEST_ACTIVATION_COMPANY_1:String = "activationCompany1";
		public static const TEST_ACTIVATION_COMPANY_2:String = "activationCompany2";
		public static const TEST_ACTIVATION_COMPANY_3:String = "activationCompany3";
		
		public static const TEST_COMPANY_PAYROLLS:String = "companyPayrolls";
		public static const TEST_EMPLOYEE_BANK_FRAUD:String = "employeeBankFraud";  
		public static const TEST_EMPLOYEE_PROFILE_HISTORY:String = "employeeProfileHistory";
		public static const TEST_GET_COMPANY_CONTACTS:String = "getCompanyContacts";
		public static const TEST_GET_DIRECT_DEPOSIT_LIMIT_SETTINGS:String = "getDirectDepositLimitSettings";
		public static const TEST_GET_FRAUD_SETTINGS:String = "getFraudSettings";
        public static const TEST_GET_PROPERTY_AUDIT_HISTORY:String = "getPropertyAuditHistory";
        public static const TEST_GET_NOTE_HISTORY:String = "getNoteHistory";
        public static const TEST_FIND_PAYROLL_RUN:String = "findPayrollRun";
		public static const TEST_PAYROLL_BILLING_TRANSACTIONS:String = "payrollBillingTransactions";						
		public static const TEST_PAYROLL_ACH_TRANSACTIONS:String = "payrollACHTransactions";
		public static const TEST_PAYROLL_ACH_TRANSACTION_DETAIL:String = "payrollACHTransactionDetail";		
		public static const TEST_USER_ROLES:String = "userRoles";
		public static const TEST_USER_OPERATIONS:String = "userOperations";
		public static const TEST_USERS:String = "users";
		public static const TEST_USER:String = "user";
		public static const TEST_USER_ROLE_NAMES:String = "userRoleNames";
		public static const TEST_PROPERTY_AUDIT:String = "propertyAudit";
		public static const TEST_COMPANY:String = "company";
		public static const TEST_ACH_OFFLOAD:String = "achOffload";
		public static const TEST_TAX_PAYMENT_YEAR:String = "taxPaymentYear";
		public static const TEST_TAX_TEMPLATE_QUARTERS:String = "taxTemplateQuarters";	
        public static const TEST_GLOBAL_PAYMENTS:String = "testGlobalPayments";
		public static const TEST_COMPANY_AGENCY_HISTORY:String = "testCompanyAgencyHistory"; 
		public static const TEST_PAYMENT_HISTORY:String ="testPaymentHistory";
        public static const TEST_FIND_TAX_TRANSACTIONS:String = "findTaxTransactions";
        public static const TEST_EMPLOYEE_LEDGER_ITEMS:String = "employeeLedgerItems";
		public static const TEST_TAX_HPDE_EMPLOYEES_YTD:String = "taxHPDEEmployeesYTD";
        public static const TEST_TAX_HPDE_EMPLOYEES_QUARTER:String = "taxHPDEEmployeesQuarter";
        public static const TEST_MULTIPLE_EMPLOYEES:String = "testMultipleEmployees"; 
        public static const TEST_HISTORICAL_LIABILITIES:String = "historicalLiabilities";
        public static const TEST_EMPLOYEE_PAYCHECK_DETAILS:String = "testEmployeePaycheckDetails";
        public static const TEST_DISPLAY_STATUS:String = "testDisplayStatus";
        public static const TEST_COMPANY_BANK_ACCOUNT:String = "testCompanyBankAccount";
  
        private static var mInstance:MockRepository;
                    
        private var mRepository:Dictionary;       
                    
        public function MockRepository()
        {
              super();
              mRepository = new Dictionary();
              loadRepositoryItems();
        }

        public static function get instance():MockRepository {
              if(mInstance  == null) mInstance = new MockRepository();
              return mInstance;
        }

        public function getTestObject(value:String):* {
              return repository[value]();
        }                                 
        
        protected function get repository():Dictionary {
              return mRepository;
        }
        
        protected function set repository(value:Dictionary):void {
              mRepository = value;
        }
        
        public function loadRepositoryItems():void {
        	  repository[TEST_ACTIVATION_COMPANY_1] = getActivationCompany1;
			  repository[TEST_ACTIVATION_COMPANY_2] = getActivationCompany2;
			  repository[TEST_ACTIVATION_COMPANY_3] = getActivationCompany3;
              //repository[TEST_ACTIVATION_COMPANY_1] = getActivationCompany1;
              repository[TEST_COMPANY_PAYROLLS] = PayrollData.getCompanyPayrolls;
              repository[TEST_GET_DIRECT_DEPOSIT_LIMIT_SETTINGS] = AdministrativeData.getDirectDepositLimitSettings;
              repository[TEST_GET_FRAUD_SETTINGS] = AdministrativeData.getFraudSettings;
              repository[TEST_GET_COMPANY_CONTACTS] = CompanyData.getCompanyContacts;
              repository[TEST_EMPLOYEE_BANK_FRAUD] = EmployeeData.getEmployeeFraudBankAccounts;
              repository[TEST_PAYROLL_BILLING_TRANSACTIONS] = PayrollData.getBillingTransactions;              
              repository[TEST_PAYROLL_ACH_TRANSACTIONS] = PayrollData.getMoneyMovementTransactions;
              repository[TEST_PAYROLL_ACH_TRANSACTION_DETAIL] = PayrollData.getMoneyMovementTransactionDetail;
              repository[TEST_USER_ROLES] = UserData.getRoleData;
              repository[TEST_USER_OPERATIONS] = UserData.getOperationData;
              repository[TEST_USERS] = UserData.getUsers;
              repository[TEST_USER_ROLE_NAMES] = UserData.getRoleNames;
              repository[TEST_USER] = UserData.buildUser;
              repository[TEST_PROPERTY_AUDIT] = CompanyData.getPropertyAuditCollection;
              repository[TEST_COMPANY] = CompanyData.getCompany;
			  repository[TEST_ACH_OFFLOAD] = ACHOffloadData.getLongOffloadData;
			  repository[TEST_TAX_PAYMENT_YEAR] = TaxData.getTaxPaymentYears;
			  repository[TEST_TAX_TEMPLATE_QUARTERS] = TaxData.getPaymentTemplateQuarters;
			  repository[TEST_COMPANY_AGENCY_HISTORY] = TaxData.getAgencyHistory;
			  repository[TEST_COMPANY_AGENCY_HISTORY] = TaxData.getAgencyHistory;
			  repository[TEST_PAYMENT_HISTORY] = TaxData.getTaxPaymentHistory;
              repository[TEST_EMPLOYEE_PROFILE_HISTORY] = EmployeeData.getEmployeeProfileHistory;
              repository[TEST_MULTIPLE_EMPLOYEES] = EmployeeData.getMultipleEmployees();
              repository[TEST_FIND_TAX_TRANSACTIONS] = TaxData.getTaxTransactions;
              repository[TEST_GET_PROPERTY_AUDIT_HISTORY] = TaxData.getPropertyAuditHistory;
              repository[TEST_GET_NOTE_HISTORY] = TaxData.getNoteHistory;
              repository[TEST_EMPLOYEE_LEDGER_ITEMS] = TaxData.getEmployeeLedgerItems;
       		  repository[TEST_GLOBAL_PAYMENTS] = TaxData.getGlobalTaxPayments;
       		  repository[TEST_DISPLAY_STATUS] = CompanyData.getDisplayStatus;
       		  repository[TEST_COMPANY_BANK_ACCOUNT] = CompanyData.getCompanyBankAccount;
       		  repository[TEST_FIND_PAYROLL_RUN] = PayrollData.findPayrollRun;
        }     
        

        /* Test data */	
		protected function getActivationCompany2():Company {
			var company:Company = getActivationCompany1();
			company.legalName = "Activation Company 2";
			//company.currentActivationsCheckListSummary.activationsErrorState = ActivationsErrorStateEnum.ERROR;
			//company.currentActivationsCheckListSummary.agentCorpId = "10000150865";		
			return company;
		}
		
		/* Test data */	
		protected function getActivationCompany3():Company {
			var company:Company = getActivationCompany1();
			company.legalName = "Activation Company 3";
			//company.currentActivationsCheckListSummary.agentCorpId = "10000106774";
			//company.currentActivationsCheckListSummary.activationsErrorState = ActivationsErrorStateEnum.WARNING;
			return company;
		}
		
		protected function getActivationCompany1():Company {
			var company:Company = new Company();
			company.sourceSystemCd = "QBOE";
			company.legalName = "Activation Company 1";
			company.fein = "123482718";
						
			return company;
		}
        
        
        private function getRandomEIN():String {
			var retEIN:String = "";
			for(var i:int=0; i < 9; i++)
			{
				retEIN+= Math.round(Math.random()*9).toString()
			}
			return retEIN;
		}
		
		private function getRandomName(value:String):String {
			var retName:String = value + "_";
			for(var i:int=0; i < 9; i++)
			{
				retName+= Math.round(Math.random()*9).toString()
			}
			return retName;
		}
		
		private function getRandomPSID():String {
			var retPSID:String = "RND_";
			for(var i:int=0; i < 7; i++)
			{
				retPSID+= Math.round(Math.random()*9).toString()
			}
			return retPSID;
		}
		
		public function getTestCompanyRandomized(value:String):Company {
			
			var returnCompany:Company = getTestObject(value) as Company;

			if(returnCompany != null)
			{
				returnCompany.fein = getRandomEIN();
				returnCompany.companyId = getRandomPSID();
				returnCompany.legalName = getRandomName(returnCompany.legalName);
			}
			return returnCompany;
		}
        
                                                  

	}
}
