/**
 * User: ihannur
 * Date: 6/19/13
 * Time: 2:35 PM
 */
package psp.sap.viewmodel {
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.PaginationCollection;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.SearchResults;
    import psp.sap.model.VMPEmployeeInfo;

    public class VMPEmployeeListViewModel extends AbstractPartViewModel {

        [Bindable]
        [ArrayElementType("psp.sap.model.VMPEmployeeInfo")]
        public var employeesSearchResult:PaginationCollection = new PaginationCollection(null, 15);

        public function VMPEmployeeListViewModel() {
            this.label = CompanyInspectorPageEnum.VIEW_MY_PAYCHECK_INFO;
            reloadOnActivate = false;
        }

        override protected function loadModelData():void {
            SAP.instance.viewMyPaycheckService.getEmployeesInfo(
                    company.sourceSystemCd,
                    company.companyId,
                    employeesSearchResult.startIndex, employeesSearchResult.pageSize, employeesSearchResult.sortBy, employeesSearchResult.sortDesc,
                    createLoadModelDataResponder(onEmployeeSearchResultLoaded));
        }

        private function onEmployeeSearchResultLoaded(e:ResultEvent):void{
            employeesSearchResult.searchResults = SearchResults(e.result);
        }

        public function pageDataGridLoadDataFunction():void {
            refresh();
        }

        public function viewVMPEmployeeInfo(employeeInfo:VMPEmployeeInfo):void {
            topic.findPage(CompanyInspectorPageEnum.VIEW_MY_PAYCHECK_EMPLOYEE_INFO).activatePage(VMPEmployeeViewModel.createActivator(employeeInfo.employeeSeq));
        }
    }
}
