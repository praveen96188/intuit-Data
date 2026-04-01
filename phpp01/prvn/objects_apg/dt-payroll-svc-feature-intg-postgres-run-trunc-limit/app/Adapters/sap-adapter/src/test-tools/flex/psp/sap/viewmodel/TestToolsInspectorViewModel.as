package psp.sap.viewmodel
{
	import psp.sap.viewmodel.AbstractInspectorViewModel;

	public class TestToolsInspectorViewModel extends AbstractInspectorViewModel
	{
		public function TestToolsInspectorViewModel()
		{
			super();
			var offloadGroupTopic:TTOffloadGroupTopicViewModel = new TTOffloadGroupTopicViewModel(this); 
			topics.addItem(offloadGroupTopic);
			
			var bankSimulatorTopic:TTBankSimulatorTopicViewModel = new TTBankSimulatorTopicViewModel(this); 
			topics.addItem(bankSimulatorTopic);

			defaultTopic = offloadGroupTopic;
		}
	}
}
