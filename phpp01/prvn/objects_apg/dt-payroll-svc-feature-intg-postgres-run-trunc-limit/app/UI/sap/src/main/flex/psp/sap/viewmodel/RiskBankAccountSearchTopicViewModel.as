package psp.sap.viewmodel {
    import psp.sap.application.enums.RiskInspectorPageEnum;
    import psp.sap.application.enums.RiskInspectorTopicEnum;

    public class RiskBankAccountSearchTopicViewModel extends InspectorTopicViewModel {
        public function RiskBankAccountSearchTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, RiskInspectorTopicEnum.BANK_ACCOUNT_SEARCH);

			addSinglePart(RiskInspectorPageEnum.BANK_ACCOUNT_SEARCH, RiskBankAccountSearchViewModel);
		}
    }
}