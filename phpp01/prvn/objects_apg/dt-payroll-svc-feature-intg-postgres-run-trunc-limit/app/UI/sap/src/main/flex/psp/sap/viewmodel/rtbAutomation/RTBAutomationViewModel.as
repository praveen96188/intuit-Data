/**
 * Created by: smodgil
 * Description: To Add RTBAutomation Tab
 * Date:02/08/2019
 *
 */
package psp.sap.viewmodel.rtbAutomation {
import psp.sap.viewmodel.*;
import psp.sap.application.enums.RTBPageEnum;

public class RTBAutomationViewModel extends CompositePartViewModel {
        public function RTBAutomationViewModel() {
            super();
            this.label = RTBPageEnum.RTB_JOB_AUTOMATION_VIEW;
        }
    }
}
