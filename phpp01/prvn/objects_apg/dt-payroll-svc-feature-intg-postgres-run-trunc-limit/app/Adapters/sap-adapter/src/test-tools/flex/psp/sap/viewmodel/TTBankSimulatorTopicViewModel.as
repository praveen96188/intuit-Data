package psp.sap.viewmodel
{
	import psp.sap.application.enums.TestToolsInspectorTopicEnum;
	import psp.sap.application.enums.TestToolsPageEnum;

	public class TTBankSimulatorTopicViewModel extends InspectorTopicViewModel
	{	
		public function TTBankSimulatorTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, TestToolsInspectorTopicEnum.BANK_SIMULATOR);

			pages.addItem(AbstractPartViewModel.createSinglePartPageViewModel(TTBankSimulatorViewModel,inspector,this, TestToolsPageEnum.BANK_SIMULATOR));						
								
			defaultPage = pages.getPage(TestToolsPageEnum.BANK_SIMULATOR); 
		}
	}
}