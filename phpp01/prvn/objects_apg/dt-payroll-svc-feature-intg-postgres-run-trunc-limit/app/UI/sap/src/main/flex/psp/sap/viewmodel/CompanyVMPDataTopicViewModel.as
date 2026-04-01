/**
 * Created with IntelliJ IDEA.
 * User: ihannur
 * Date: 6/19/13
 * Time: 1:47 PM
 */
package psp.sap.viewmodel {
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.CompanyInspectorTopicEnum;

    public class CompanyVMPDataTopicViewModel extends CompanyInspectorTopicViewModel {

        public function CompanyVMPDataTopicViewModel(companyInspector:CompanyInspectorViewModel) {
            super(companyInspector, CompanyInspectorTopicEnum.VMP_DATA);
            addSinglePart(CompanyInspectorPageEnum.VIEW_MY_PAYCHECK_INFO, VMPEmployeeListViewModel);
            addSinglePart(CompanyInspectorPageEnum.VIEW_MY_PAYCHECK_EMPLOYEE_INFO, VMPEmployeeViewModel);
        }
    }
}
