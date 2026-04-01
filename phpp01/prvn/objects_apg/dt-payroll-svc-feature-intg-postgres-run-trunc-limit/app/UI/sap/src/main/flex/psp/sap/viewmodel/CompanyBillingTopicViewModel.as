package psp.sap.viewmodel
{
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.CompanyInspectorTopicEnum;

    public class CompanyBillingTopicViewModel extends CompanyInspectorTopicViewModel
    {
        public function CompanyBillingTopicViewModel(companyInspector:CompanyInspectorViewModel)
        {
            super(companyInspector, CompanyInspectorTopicEnum.BILLING);
            addSinglePart(CompanyInspectorPageEnum.BILLING, BillingViewModel,"");
        }
    }
}
