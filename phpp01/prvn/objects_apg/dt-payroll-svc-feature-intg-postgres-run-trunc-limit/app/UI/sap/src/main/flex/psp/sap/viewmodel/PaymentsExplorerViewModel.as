package psp.sap.viewmodel {
import psp.sap.application.SAP;
import psp.sap.application.enums.ExplorerEnum;
import psp.sap.application.enums.OperationsEnum;

    public class PaymentsExplorerViewModel extends AbstractExplorer {
        public function PaymentsExplorerViewModel() {
            super(ExplorerEnum.PAYMENTS, ExplorerEnum.PAYMENTS, true);

            inspectors.addItem(new PaymentsInspectorViewModel(this));
        }

        override public function permissionGranted():Boolean {
            return SAP.canPerformOperation(OperationsEnum.VIEW_GLOBAL_TAX_PAYMENTS);
        }
    }
}