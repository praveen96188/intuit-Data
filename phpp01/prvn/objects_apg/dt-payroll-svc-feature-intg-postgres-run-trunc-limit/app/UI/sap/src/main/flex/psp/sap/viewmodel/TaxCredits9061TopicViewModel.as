package psp.sap.viewmodel {
    import psp.sap.application.enums.AccountingInspectorPageEnum;
    import psp.sap.application.enums.AccountingInspectorTopicEnum;
    import psp.sap.application.enums.TaxCreditsInspectorPageEnum;
    import psp.sap.application.enums.TaxCreditsInspectorTopicEnum;

    public class TaxCredits9061TopicViewModel extends InspectorTopicViewModel {
        public function TaxCredits9061TopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, TaxCreditsInspectorTopicEnum.TAX_CREDITS_9061);

            addSinglePart(TaxCreditsInspectorPageEnum.TAX_CREDITS_9061, TaxCredits9061ViewModel);
		}
    }
}