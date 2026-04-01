package psp.sap.viewmodel {
import psp.sap.application.enums.OperatorInspectorTopicEnum;
import psp.sap.application.enums.OperatorPageEnum;

public class OperatorConnectionsTopicViewModel extends InspectorTopicViewModel {
    public function OperatorConnectionsTopicViewModel(inspector:AbstractInspectorViewModel) {

        super(inspector, OperatorInspectorTopicEnum.CONNECTIONS);
        addSinglePart(OperatorPageEnum.CONNECTION_PAGE, OperatorConnectionsLogViewModel);
        addSinglePart(OperatorPageEnum.CONNECTION_DETAILS_PAGE, PayrollConnectionOFXViewModel);
     
    }
}
}