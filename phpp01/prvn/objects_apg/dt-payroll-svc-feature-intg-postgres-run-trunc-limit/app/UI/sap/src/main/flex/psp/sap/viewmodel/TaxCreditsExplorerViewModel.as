package psp.sap.viewmodel {
    import psp.sap.application.SAP;
    import psp.sap.application.enums.ExplorerEnum;
    import psp.sap.application.enums.OperationsEnum;

    public class TaxCreditsExplorerViewModel extends AbstractExplorer {
        public function TaxCreditsExplorerViewModel() {
            super(ExplorerEnum.TAX_CREDITS);
			
			inspectors.addItem(new TaxCreditsInspectorViewModel(this));
        }

        override public function permissionGranted():Boolean {
			return SAP.canPerformOperation(OperationsEnum.TAX_CREDITS_WOTC);
		}
    }
}