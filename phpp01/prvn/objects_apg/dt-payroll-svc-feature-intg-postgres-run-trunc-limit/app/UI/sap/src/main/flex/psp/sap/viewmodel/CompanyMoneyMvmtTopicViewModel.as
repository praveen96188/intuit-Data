package psp.sap.viewmodel {
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.CompanyInspectorTopicEnum;

    public class CompanyMoneyMvmtTopicViewModel extends CompanyInspectorTopicViewModel {
        public function CompanyMoneyMvmtTopicViewModel(companyInspector:CompanyInspectorViewModel) {
            super(companyInspector, CompanyInspectorTopicEnum.MONEY_MOVEMENT);
            addSinglePart(CompanyInspectorPageEnum.MONEY_MOVEMENT, MmtViewModel);
        }
    }
}