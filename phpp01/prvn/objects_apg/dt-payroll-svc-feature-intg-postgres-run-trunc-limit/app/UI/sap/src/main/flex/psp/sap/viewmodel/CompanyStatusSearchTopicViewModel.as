package psp.sap.viewmodel
{
	import psp.sap.application.enums.RiskInspectorPageEnum;
	import psp.sap.application.enums.RiskInspectorTopicEnum;

	public class CompanyStatusSearchTopicViewModel extends InspectorTopicViewModel
	{
		public function CompanyStatusSearchTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, RiskInspectorTopicEnum.COMPANY_STATUS_SEARCH);
			
		    addSinglePart(RiskInspectorPageEnum.COMPANY_STATUS_SEARCH, CompanyStatusSearchViewModel);										

		}						
	}
}