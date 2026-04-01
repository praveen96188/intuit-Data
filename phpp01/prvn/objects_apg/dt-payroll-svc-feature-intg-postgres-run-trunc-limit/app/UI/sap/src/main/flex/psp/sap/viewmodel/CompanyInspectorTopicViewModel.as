package psp.sap.viewmodel
{
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	
	public class CompanyInspectorTopicViewModel extends InspectorTopicViewModel
	{	
		public function CompanyInspectorTopicViewModel(companyInspector:CompanyInspectorViewModel, label:String)
		{
			super(companyInspector, label);
		}
				
		public function get companyInspector():CompanyInspectorViewModel {
			return inspector as CompanyInspectorViewModel;
		}		

	}
}