package psp.sap.viewmodel.rtbAutomation {
import psp.sap.viewmodel.*;
import psp.sap.application.enums.RTBPageEnum;
public class VmpServiceTopicViewModel extends InspectorTopicViewModel {
    public function VmpServiceTopicViewModel(inspector:AbstractInspectorViewModel) {
        super(inspector, RTBPageEnum.REALM_ID_VIEW);
    }
}
}
