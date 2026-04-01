/**
 * Created by: smodgil
 * Description: To Add RTBAutomation Tab
 * Date:02/08/2019
 *
 */
package psp.sap.viewmodel.rtbAutomation {
import psp.sap.viewmodel.*;
    import psp.sap.application.enums.RTBPageEnum;

    public class RTBAutomationTopicViewModel extends InspectorTopicViewModel {
        public function RTBAutomationTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, RTBPageEnum.RTB_JOB_AUTOMATION_VIEW);
            addSinglePart(RTBPageEnum.RTB_JOB_AUTOMATION_VIEW, RTBViewModel);
        }
    }
}
