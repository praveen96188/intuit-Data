package psp.sap.viewmodel
{
	import psp.sap.application.enums.TestToolsInspectorTopicEnum;
	import psp.sap.application.enums.TestToolsPageEnum;

	public class TTOffloadGroupTopicViewModel extends InspectorTopicViewModel
	{	
		public function TTOffloadGroupTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, TestToolsInspectorTopicEnum.OFFLOAD_GROUP);

			pages.addItem(AbstractPartViewModel.createSinglePartPageViewModel(TTOffloadGroupViewModel,inspector,this, TestToolsPageEnum.OFFLOAD_GROUP));						
								
			defaultPage = pages.getPage(TestToolsPageEnum.OFFLOAD_GROUP); 
		}
	}
}