/**

 * Created by anandp233 on 2/11/14.

 */

package psp.sap.viewmodel {
import psp.sap.application.SAP;
import psp.sap.application.enums.OperationsEnum;


public class ReportInspectorViewModel extends AbstractInspectorViewModel {

    public function ReportInspectorViewModel(explorer:AbstractExplorer) {

        super(explorer);

            if(SAP.canPerformOperation(OperationsEnum.REPORT_FILE_DOWNLOAD)){
                topics.addItem(new ReportTopicViewModel(this));
            }
    }

    override public function permissionGranted():Boolean {
        return SAP.canPerformOperation(OperationsEnum.REPORT_FILE_DOWNLOAD);
    }

}

}

