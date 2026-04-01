package psp.sap.viewmodel
{
    import psp.sap.application.enums.FraudInspectorPageEnum;
    import psp.sap.application.enums.RiskInspectorTopicEnum;

    public class FraudTopicViewModel extends InspectorTopicViewModel
	{
		public function FraudTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, RiskInspectorTopicEnum.FRAUD);

            addSinglePart(FraudInspectorPageEnum.FRAUD_VIEW, FraudViewModel);										

		}						
	}
}