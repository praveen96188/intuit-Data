package psp.sap.viewmodel {
    import psp.sap.application.enums.AdministrationInspectorPageEnum;
    import psp.sap.application.enums.AdministrationInspectorTopicEnum;
    import psp.sap.view.SqlAdministrationView;

    public class SystemParametersTopicViewModel extends InspectorTopicViewModel {
        public function SystemParametersTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, AdministrationInspectorTopicEnum.SYSTEM_PARAMETERS);
            pages.addItem(addSinglePart(AdministrationInspectorPageEnum.SYSTEM_PARAMETERS, SystemParametersViewModel));
        }
    }
}