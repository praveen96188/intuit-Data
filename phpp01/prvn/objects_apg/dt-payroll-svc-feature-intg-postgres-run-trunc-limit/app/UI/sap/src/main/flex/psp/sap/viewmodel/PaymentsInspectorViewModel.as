package psp.sap.viewmodel {
    public class PaymentsInspectorViewModel extends AbstractInspectorViewModel {
        public function PaymentsInspectorViewModel(explorer:AbstractExplorer) {
            super(explorer);
            topics.addItem( new PaymentsTopicViewModel(this));
        }
    }
}