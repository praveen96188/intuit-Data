/**
 * User: ihannur
 * Date: 6/7/13
 * Time: 4:01 PM
 */
package psp.sap.model {
    import mx.controls.Alert;
    import mx.rpc.Responder;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.ExplorerEnum;
    import psp.sap.viewmodel.AbstractExplorer;
    import psp.sap.viewmodel.CompanyInspectorViewModel;
    import psp.sap.viewmodel.VMPEmployeeViewModel;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeSearchResult")]
    public class EmployeeSearchResult {
        public var employeeName:String;
        public var employeeId:String;
        public var companyName:String;
        public var employeeSSN:String;
        public var employeeEmail:String;
        public var companyKey:CompanyKey;


        public function display():void {
            var companyInspector:CompanyInspectorViewModel = null;
            //if we already have the company, display that
            var explorer:AbstractExplorer = SAP.instance.explorers.getExplorer(ExplorerEnum.COMPANY);
            for each (var inspector:CompanyInspectorViewModel in (explorer.inspectors)) {
                if (inspector.company != null && inspector.company.companyKey.equals(companyKey)) {
                    companyInspector = inspector;
                }
            }

            if (companyInspector != null) {
                companyInspector.getPage(CompanyInspectorPageEnum.VIEW_MY_PAYCHECK_EMPLOYEE_INFO).activatePage(VMPEmployeeViewModel.createActivator(employeeId));
            } else {
                SAP.instance.companyService.findCompany(companyKey.sourceSystemCd, companyKey.companyId, new Responder(onCompanyResults, null));
            }
        }

        public function onCompanyResults(e:ResultEvent):void {
            var vmpCompany:Company = e.result as Company;
            var explorer:AbstractExplorer = SAP.instance.explorers.getExplorer(ExplorerEnum.COMPANY);
            if (explorer != null && explorer.inspectors != null
                    && (explorer.inspectors.length < (SAP.MAX_OPEN_COMPANIES + 1) || explorer.inspectors.findByApplicationItem(vmpCompany) != null)) {
                explorer.display(vmpCompany).getPage(CompanyInspectorPageEnum.VIEW_MY_PAYCHECK_EMPLOYEE_INFO).activatePage(VMPEmployeeViewModel.createActivator(employeeId));
            }
            else {
                Alert.show("Tab limit reached. Please close a tab before opening a new one.");
            }
        }

    }
}
