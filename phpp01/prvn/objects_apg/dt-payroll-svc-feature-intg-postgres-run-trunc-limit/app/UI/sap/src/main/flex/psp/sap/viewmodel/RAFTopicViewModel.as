package psp.sap.viewmodel {
    import psp.sap.application.enums.EnrollmentsPageEnum;

    public class RAFTopicViewModel extends InspectorTopicViewModel {
        public function RAFTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, EnrollmentsPageEnum.RAF);

            addSinglePart(EnrollmentsPageEnum.RAF, RAFViewModel);
        }
    }
}