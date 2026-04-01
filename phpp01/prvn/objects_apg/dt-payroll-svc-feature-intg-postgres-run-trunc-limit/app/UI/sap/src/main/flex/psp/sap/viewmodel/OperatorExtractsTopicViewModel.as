package psp.sap.viewmodel {
    import psp.sap.application.enums.OperatorInspectorTopicEnum;
    import psp.sap.application.enums.OperatorPageEnum;

    public class OperatorExtractsTopicViewModel extends InspectorTopicViewModel {
        public function OperatorExtractsTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, OperatorInspectorTopicEnum.EXTRACTS);
            addSinglePart(OperatorPageEnum.EXTRACTS, OperatorExtractsViewModel);
        }
    }
}