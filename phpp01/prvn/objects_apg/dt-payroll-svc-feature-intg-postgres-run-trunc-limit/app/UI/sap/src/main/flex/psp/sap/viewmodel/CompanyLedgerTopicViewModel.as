package psp.sap.viewmodel
{
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.application.enums.CompanyInspectorTopicEnum;
	import psp.sap.model.CompanyLedgerAccount;
	import psp.sap.viewmodel.CompanyInspectorViewModel;

	/**
	 * Company Payroll Topic
	 * 
	 * Extends generic CompanyTopic by adding support for a currently selected item(s).
	 * A user can drill into a company payrolls in the following ways:
	 *  1) Ledger > Ledger Account	 	
	 */  
	public class CompanyLedgerTopicViewModel extends CompanyInspectorTopicViewModel
	{
		public function CompanyLedgerTopicViewModel(companyInspector:CompanyInspectorViewModel)
		{
			super(companyInspector, CompanyInspectorTopicEnum.LEDGER);
																		
			addSinglePart(CompanyInspectorPageEnum.COMPANY_LEDGER, LedgerViewModel);			
			addSinglePart(CompanyInspectorPageEnum.COMPANY_LEDGER_ACCOUNT_ENTRIES, LedgerEntriesViewModel);
			addSinglePart(CompanyInspectorPageEnum.FINANCIAL_LEDGER_ADJUSTMENT, FinancialLedgerAdjustmentViewModel);
			addSinglePart(CompanyInspectorPageEnum.TAX_PENALTIES_AND_INTEREST, PenaltiesAndInterestViewModel);
            addSinglePart(CompanyInspectorPageEnum.COURTESY_REFUND, CourtesyRefundViewModel);


		}				

	}
}
