package psp.sap.viewmodel
{
	import psp.sap.application.enums.OperatorPageEnum;
	import psp.sap.application.enums.OperatorInspectorTopicEnum;

	public class OperatorTopicViewModel extends InspectorTopicViewModel
	{	
		public function OperatorTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, OperatorInspectorTopicEnum.OPERATOR);

            addSinglePart(OperatorPageEnum.OPERATOR_VIEW, OperatorViewModel);
            addSinglePart(OperatorPageEnum.ACH_SECONDARY_OFFLOAD_CONFIRMATION, ACHSecondaryOffloadConfirmationViewModel);
		}
	}
}