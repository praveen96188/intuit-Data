package psp.sap.viewmodel {
    import psp.sap.application.enums.OperatorInspectorTopicEnum;
    import psp.sap.application.enums.OperatorPageEnum;

    public class OperatorLedgerTopicViewModel extends InspectorTopicViewModel {
        public function OperatorLedgerTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, OperatorInspectorTopicEnum.LEDGER);
            addSinglePart(OperatorPageEnum.LEDGER_OPERATIONS, OperatorLedgerViewModel);
        }
    }
}