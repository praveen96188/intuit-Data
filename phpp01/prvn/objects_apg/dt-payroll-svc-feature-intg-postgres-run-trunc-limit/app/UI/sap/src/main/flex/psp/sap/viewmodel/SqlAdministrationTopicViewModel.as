package psp.sap.viewmodel {
    import psp.sap.application.enums.AdministrationInspectorPageEnum;
    import psp.sap.application.enums.AdministrationInspectorTopicEnum;

    public class SqlAdministrationTopicViewModel extends InspectorTopicViewModel {
        public function SqlAdministrationTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, AdministrationInspectorTopicEnum.SQL_ADMINISTRATION);
            pages.addItem(addSinglePart(AdministrationInspectorPageEnum.SQL_ADMINISTRATION, SqlConsoleViewModel));
        }
    }
}