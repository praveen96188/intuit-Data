package psp.sap.viewmodel {
    public class SettingsInspectorViewModel extends AbstractInspectorViewModel{
        public function SettingsInspectorViewModel(explorer:AbstractExplorer) {
            super(explorer);

            topics.addItem( new UserSettingsTopicViewModel(this));
            // advanced settings added dynamically

        }
    }
}