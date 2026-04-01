package psp.sap.viewmodel.rtbAutomation {
import psp.sap.viewmodel.*;
import psp.sap.application.SAP;
import psp.sap.application.enums.OperationsEnum;

public class VmpServiceInspectorViewModel extends AbstractInspectorViewModel {


    public function VmpServiceInspectorViewModel(explorer:AbstractExplorer) {

        super(explorer);


        topics.addItem(new VmpServiceTopicViewModel(this));

        if (SAP.canPerformOperation(OperationsEnum.DECRYPT_TEXT)) {
            topics.addItem(new DecryptTextTopicViewModel(this));

        }

    }
}
}


