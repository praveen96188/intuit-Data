/**
 * Created by: smodgil
 * Description: To Create Duplication Employee View
 * Date:02/08/2019
 *
 */
package psp.sap.viewmodel.rtbAutomation {
import psp.sap.viewmodel.*;
import psp.sap.application.enums.RTBPageEnum;

public class DupEETopicViewModel extends InspectorTopicViewModel {
        public function DupEETopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, RTBPageEnum.DUP_EMP_VIEW);
        }
    }
}
