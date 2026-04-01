package psp.sap.viewmodel {
    import psp.sap.application.enums.OperatorInspectorTopicEnum;
    import psp.sap.application.enums.OperatorPageEnum;

    public class CheckPrintingTopicViewModel extends InspectorTopicViewModel {

		public function CheckPrintingTopicViewModel(inspector:AbstractInspectorViewModel) {
			super(inspector, OperatorInspectorTopicEnum.CHECK_PRINTING);

            addSinglePart(OperatorPageEnum.CHECK_PRINTING_VIEW, CheckPrintingViewModel);
		}
	}
}
