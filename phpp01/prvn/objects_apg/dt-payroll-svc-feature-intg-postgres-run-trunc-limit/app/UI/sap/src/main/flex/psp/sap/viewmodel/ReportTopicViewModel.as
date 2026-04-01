/**
 * Created by: smodgil
 * Description: To Create Duplication Employee View
 * Date:02/08/2019
 *
 */
package psp.sap.viewmodel {
import psp.sap.application.enums.ExplorerEnum;


public class ReportTopicViewModel extends InspectorTopicViewModel {
    public function ReportTopicViewModel(inspector:AbstractInspectorViewModel) {
        super(inspector, ExplorerEnum.REPORT);
        addSinglePart(ExplorerEnum.REPORT, RTBViewModel);

    }
}
}
