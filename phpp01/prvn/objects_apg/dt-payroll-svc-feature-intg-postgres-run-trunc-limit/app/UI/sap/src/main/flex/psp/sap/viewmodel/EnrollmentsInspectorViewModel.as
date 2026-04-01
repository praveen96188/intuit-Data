package psp.sap.viewmodel {
    public class EnrollmentsInspectorViewModel extends AbstractInspectorViewModel {
        public function EnrollmentsInspectorViewModel(explorer:AbstractExplorer) {
            super(explorer);
            topics.addItem(new EnrollmentsTopicViewModel(this));
            topics.addItem(new RAFTopicViewModel(this));
            topics.addItem(new ACHEnrollmentTopicViewModel(this));
        }
    }
}
