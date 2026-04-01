package psp.sap.viewmodel {
import psp.sap.application.SAP;
import psp.sap.application.enums.ExplorerEnum;
import psp.sap.application.enums.OperationsEnum;

public class ReportExplorerViewModel extends AbstractExplorer {

        public function ReportExplorerViewModel() {

            super(ExplorerEnum.REPORT, ExplorerEnum.REPORT, true);

            inspectors.addItem(new ReportInspectorViewModel(this));

        }


        override public function permissionGranted():Boolean {

            return (SAP.canPerformOperation(OperationsEnum.REPORT_FILE_DOWNLOAD));

        }

    }
}

