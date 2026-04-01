package psp.sap.viewmodel
{
    import psp.sap.application.enums.CompanyInspectorPageEnum;

    public class EmployeesViewModel extends CompositePartViewModel
    {

        public function EmployeesViewModel()
        {
            super();
            this.label = CompanyInspectorPageEnum.EMPLOYEES;
            var employeeListTabViewModel:AbstractPartViewModel = this.addNewPart(EmployeeListTabViewModel, CompanyInspectorPageEnum.EMPLOYEE_LIST, PartAdditionStrategy.LAZY_COMPOSITE);
        }
    }
}