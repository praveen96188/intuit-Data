package psp.sap.viewmodel {
    public class EINManagementInspectorViewModel extends AbstractInspectorViewModel {
        public function EINManagementInspectorViewModel(explorer:AbstractExplorer) {
            super(explorer);
            topics.addItem( new EINsTopicViewModel(this));

        }
    }
}