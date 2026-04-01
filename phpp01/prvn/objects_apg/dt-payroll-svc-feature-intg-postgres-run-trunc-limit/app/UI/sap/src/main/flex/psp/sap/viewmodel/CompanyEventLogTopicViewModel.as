package psp.sap.viewmodel
{
	import mx.binding.utils.BindingUtils;
	
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.application.enums.CompanyInspectorTopicEnum;
	import psp.sap.model.CompanyEventItem;

	public class CompanyEventLogTopicViewModel extends CompanyInspectorTopicViewModel
	{
		
		public function CompanyEventLogTopicViewModel(companyInspector:CompanyInspectorViewModel)
		{
			super(companyInspector, CompanyInspectorTopicEnum.EVENT_LOG);
			
			addSinglePart(CompanyInspectorPageEnum.EVENT_LOG, CompanyEventLogViewModel);

		}				
		
	}
}