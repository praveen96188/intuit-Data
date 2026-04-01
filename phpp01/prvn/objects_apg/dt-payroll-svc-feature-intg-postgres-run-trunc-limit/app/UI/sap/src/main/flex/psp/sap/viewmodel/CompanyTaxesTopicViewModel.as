package psp.sap.viewmodel
{
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.CompanyInspectorTopicEnum;

    public class CompanyTaxesTopicViewModel extends CompanyInspectorTopicViewModel
	{			
		
		public function CompanyTaxesTopicViewModel(companyInspector:CompanyInspectorViewModel) 
		{
			super(companyInspector, CompanyInspectorTopicEnum.TAXES);	
			
			//------------------------
			// Company Taxes
			//------------------------			
			addSinglePart(CompanyInspectorPageEnum.TAXES, TaxViewModel, "");
            addSinglePart(CompanyInspectorPageEnum.TAX_OVERPAYMENT, TaxOverpaymentViewModel);
            addSinglePart(CompanyInspectorPageEnum.TAX_OVERPAYMENT_REFUND_LIST, ERPayableRefundsViewModel);


		}			

	}
}
