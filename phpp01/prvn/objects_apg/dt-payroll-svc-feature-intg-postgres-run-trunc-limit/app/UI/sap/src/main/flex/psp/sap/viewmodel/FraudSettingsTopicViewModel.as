package psp.sap.viewmodel
{
    import psp.sap.application.enums.AdministrationInspectorPageEnum;
    import psp.sap.application.enums.AdministrationInspectorTopicEnum;

    public class FraudSettingsTopicViewModel extends InspectorTopicViewModel
	{

		public function FraudSettingsTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, AdministrationInspectorTopicEnum.FRAUD);

            addSinglePart(AdministrationInspectorPageEnum.FRAUD_SETTINGS, FraudSettingsPageViewModel);
            addSinglePart(AdministrationInspectorPageEnum.FRAUD_SETTINGS_EDIT,FraudSettingsEditPageViewModel, "Edit Payroll Fraud Thresholds");
		}
	}
}