package psp.sap.viewmodel
{
	import psp.sap.application.enums.OperatorInspectorTopicEnum;
	import psp.sap.application.enums.OperatorPageEnum;
	
	public class ACHOffloadStatusTopicViewModel extends InspectorTopicViewModel
	{
		public function ACHOffloadStatusTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, OperatorInspectorTopicEnum.OFFLOAD_STATUS);

            addSinglePart(OperatorPageEnum.ACH_OFFLOAD_STATUS, ACHOffloadStatusViewModel);			
		}
	}
}