package psp.sap.viewmodel {
    import psp.sap.application.enums.OperatorInspectorTopicEnum;
    import psp.sap.application.enums.OperatorPageEnum;

    public class OperatorEnrollmentsTopicViewModel extends InspectorTopicViewModel {
        public function OperatorEnrollmentsTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, OperatorInspectorTopicEnum.ENROLLMENTS);
            addSinglePart(OperatorPageEnum.ENROLLMENTS, OperatorEnrollmentsViewModel);
        }
    }
}