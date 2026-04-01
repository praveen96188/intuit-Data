package psp.sap.viewmodel {
    import psp.sap.application.enums.EnrollmentsPageEnum;

    public class EnrollmentsTopicViewModel extends InspectorTopicViewModel {
        public function EnrollmentsTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, EnrollmentsPageEnum.EFTPS);

            addSinglePart(EnrollmentsPageEnum.EFTPS, EnrollmentsViewModel);

        }
    }
}