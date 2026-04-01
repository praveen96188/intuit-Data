package psp.sap.viewmodel {
    import psp.sap.application.enums.ExplorerEnum;

    public class SettingsExplorerViewModel extends AbstractExplorer{
        public function SettingsExplorerViewModel() {
            super(ExplorerEnum.SETTINGS, null, false);

            inspectors.addItem(new SettingsInspectorViewModel(this));
        }
    }
}