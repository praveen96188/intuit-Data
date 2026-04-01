package psp.sap.viewmodel {
    import mx.events.PropertyChangeEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.ExplorerEnum;
    import psp.sap.application.enums.OperationsEnum;

    public class EINManagementExplorerViewModel extends AbstractExplorer {
        public function EINManagementExplorerViewModel() {
            super(ExplorerEnum.EIN_MANAGEMENT, ExplorerEnum.EIN_MANAGEMENT, false);

            inspectors.addItem(new EINManagementInspectorViewModel(this));
        }

        override public function permissionGranted():Boolean {
            //todo
            return true;
            //return SAP.canPerformOperation(OperationsEnum.TAX_CREDITS_WOTC);
        }

        //if the user comes in to this page, want to show in menu
        //otherwise not
        public function displayInMenu():void {
            mShowInMenu = true;
            SAP.instance.explorersMenu.refresh();
            SAP.instance.dispatchEvent(PropertyChangeEvent.createUpdateEvent(SAP.instance, "activeExplorerMenuIndex", -1, SAP.instance.activeExplorerMenuIndex));
        }
    }
}