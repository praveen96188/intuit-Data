/**
 * Created by anandp233 on 2/11/14.
 */
package psp.sap.viewmodel {
    import psp.sap.application.enums.RTBPageEnum;

    public class RTBTopicViewModel extends InspectorTopicViewModel {
        public function RTBTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, RTBPageEnum.RUN_THE_BUSINESS);
            addSinglePart(RTBPageEnum.RUN_THE_BUSINESS, RTBViewModel);
        }
    }
}
