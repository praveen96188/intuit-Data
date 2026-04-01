package psp.sap.viewmodel
{
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.CompanyInspectorTopicEnum;

    /**
	 * Company Bank Accounts Topic
	 * 
	 * Extends generic CompanyTopic by adding support for a currently selected item(s).
	 * A user can drill into a company bank accounts in the following ways:
	 *  1) Company Bank Accounts > Company Bank Account History	 	
	 */  
	public class CompanyBankAccountsTopicViewModel extends CompanyInspectorTopicViewModel
	{
		
		public function CompanyBankAccountsTopicViewModel(companyInspector:CompanyInspectorViewModel)
		{
			super(companyInspector, CompanyInspectorTopicEnum.BANKS);

			addSinglePart(CompanyInspectorPageEnum.COMPANY_BANK, CompanyBankViewModel, "");

			addSinglePart(CompanyInspectorPageEnum.BANKS_COMPANY_ACCOUNT_HISTORY, BanksCompanyAccountHistoryViewModel, "Company Bank Account History");

			addSinglePart(CompanyInspectorPageEnum.BANKS_EMPLOYEE_ACCOUNT_HISTORY, BanksEmployeeAccountHistoryViewModel, "Employee Bank Account History");

            addSinglePart(CompanyInspectorPageEnum.BANKS_VENDOR_ACCOUNT_HISTORY, BanksVendorAccountHistoryViewModel);

			addSinglePart(CompanyInspectorPageEnum.BANKS_ADD_ACCOUNT, BanksAddBankAccountViewModel, "Add/Edit Bank Account");

		}				
		
	}
}
