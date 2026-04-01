package psp.sap.viewmodel {
    import psp.sap.application.enums.SettingsInspectorPageEnum;
    import psp.sap.application.enums.SettingsInspectorTopicEnum;

    public class UserSettingsAdvancedTopicViewModel extends InspectorTopicViewModel{
        public function UserSettingsAdvancedTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, SettingsInspectorTopicEnum.ADVANCED_SETTINGS);

            addSinglePart(SettingsInspectorPageEnum.ADVANCED_SETTINGS, UserSettingsAdvancedViewModel);
        }
    }
}