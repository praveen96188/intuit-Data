package psp.sap.viewmodel {
    import psp.sap.application.enums.CompanyInspectorPageEnum;

    public class DataSyncToolsViewModel extends AbstractPartViewModel {

        public function DataSyncToolsViewModel() {
            this.label = CompanyInspectorPageEnum.DATA_SYNC_TOOLS;
            this.reloadOnActivate = false;
            bindSaveMessageWithChildren = true;
        }
    }
}
