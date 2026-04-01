package psp.sap.viewmodel
{
    import psp.sap.application.enums.AdministrationInspectorPageEnum;
    import psp.sap.application.enums.AdministrationInspectorTopicEnum;

    public class AdministrationSettingsTopicViewModel extends InspectorTopicViewModel
    {
        public function AdministrationSettingsTopicViewModel(inspector:AbstractInspectorViewModel)
        {
            super(inspector, AdministrationInspectorTopicEnum.SETTINGS);

            addSinglePart(AdministrationInspectorPageEnum.SETTINGS, AdministrationSettingsPageViewModel);

            addSinglePart(AdministrationInspectorPageEnum.SETTINGS_EDIT, AdministrationSettingsEditPageViewModel);            
        }
    }
}