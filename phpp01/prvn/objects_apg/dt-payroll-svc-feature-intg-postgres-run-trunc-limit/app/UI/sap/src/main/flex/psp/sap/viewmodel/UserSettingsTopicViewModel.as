package psp.sap.viewmodel {
    import psp.sap.application.enums.SettingsInspectorPageEnum;
    import psp.sap.application.enums.SettingsInspectorTopicEnum;

    public class UserSettingsTopicViewModel extends InspectorTopicViewModel{
        public function UserSettingsTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, SettingsInspectorTopicEnum.SETTINGS);

            addSinglePart(SettingsInspectorPageEnum.USER_SETTINGS, UserSettingsViewModel);

        }
    }
}