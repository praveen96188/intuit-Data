package psp.sap.viewmodel {
    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;

    public class TaxCreditsInspectorViewModel extends AbstractInspectorViewModel {
        public function TaxCreditsInspectorViewModel(explorer:AbstractExplorer) {
            super(explorer);
            topics.addItem( new TaxCredits9061TopicViewModel(this));
            
        }
    }
}