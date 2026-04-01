package psp.sap.viewmodel
{
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.application.enums.CompanyInspectorTopicEnum;
import psp.sap.model.EmployeeInfo;
    import psp.sap.model.Paycheck;

    public class CompanyEmployeesTopicViewModel extends CompanyInspectorTopicViewModel
	{

		public function CompanyEmployeesTopicViewModel(companyInspector:CompanyInspectorViewModel)
		{
			super(companyInspector, CompanyInspectorTopicEnum.EMPLOYEES);
			
            addSinglePart(CompanyInspectorPageEnum.EMPLOYEES, EmployeesViewModel, "Employees");

		}
	}
}