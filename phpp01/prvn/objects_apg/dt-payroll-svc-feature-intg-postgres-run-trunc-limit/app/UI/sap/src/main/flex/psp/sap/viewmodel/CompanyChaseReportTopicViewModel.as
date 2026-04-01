package psp.sap.viewmodel
{
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.application.enums.CompanyInspectorTopicEnum;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	
	public class CompanyChaseReportTopicViewModel extends CompanyInspectorTopicViewModel
	{
		public function CompanyChaseReportTopicViewModel(companyInspector:CompanyInspectorViewModel) 
		{
			super(companyInspector, CompanyInspectorTopicEnum.CHASE_REPORT);

			addSinglePart(CompanyInspectorPageEnum.CHASE_REPORT, ChaseReportViewModel);

		}

	}
}