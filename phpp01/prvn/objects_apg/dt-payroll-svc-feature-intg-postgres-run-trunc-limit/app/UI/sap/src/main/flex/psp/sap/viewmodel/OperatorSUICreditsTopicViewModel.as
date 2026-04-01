package psp.sap.viewmodel {
    import psp.sap.application.enums.OperatorInspectorTopicEnum;
    import psp.sap.application.enums.OperatorPageEnum;

    public class OperatorSUICreditsTopicViewModel extends InspectorTopicViewModel {
        public function OperatorSUICreditsTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, OperatorInspectorTopicEnum.SUI_CREDITS);
            addSinglePart(OperatorPageEnum.SUI_CREDITS, OperatorSUICreditViewModel);
        }
    }
}