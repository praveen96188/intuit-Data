package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectUtil;
    import mx.utils.StringUtil;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.PaginationCollection;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.EmployeeInfo;
    import psp.sap.model.SearchResults;

    public class EmployeeListTabViewModel extends AbstractPartViewModel {

        private const EMPTY_STRING:String = "";

        [Bindable]
        [ArrayElementType("psp.sap.model.EmployeeInfo")]
        public var employeesSearchResult:PaginationCollection = new PaginationCollection(null, 15);

        //Search Fields
        [Bindable]
        [ArrayElementType("String")]
        public var workStateList:ArrayCollection;

        [Bindable]
        [ArrayElementType("String")]
        public var liveStateList:ArrayCollection;

        [Bindable] [BackingProperty]
        public var selectedWorkState:String;

        [Bindable] [BackingProperty]
        public var selectedLiveState:String;

        [Bindable] [BackingProperty]
        public var ssn:String;

        [Bindable] [BackingProperty]
        public var name:String;

        [Bindable] [BackingProperty]
        public var employeeId:String;

        [Bindable] [BackingProperty]
        public var searchButtonClicked:Boolean=false;

        [Bindable]
        public var searchResultFound:Boolean=true;

        [Bindable] [BackingProperty]
        public var cloudEmployeesFound:Boolean=true;

        private var hasNonNumericSourceEmpIds:Boolean = false;

        public function EmployeeListTabViewModel() {
            super();
            this.label = CompanyInspectorPageEnum.EMPLOYEE_LIST;
            this.reloadOnActivate = false;
        }

        public function pageDataGridLoadDataFunction():void {
            refresh();
        }

        override protected function loadModelData():void {
            hasNonNumericSourceEmpIds = false;
            loadCount = 2;
            SAP.instance.taxService.hasNonNumericSourceIds(companyKey.sourceSystemCd,
                    companyKey.companyId, createLoadModelDataResponder(function (e:ResultEvent): void {
                        hasNonNumericSourceEmpIds = Boolean(e.result);
                    }));

            SAP.instance.employeeService.findEmployeesByCriteria(
                    company.sourceSystemCd,
                    company.companyId,
                    employeeId,
                    ssn,
                    name,
                    selectedLiveState,
                    selectedWorkState,
                    employeesSearchResult.startIndex, employeesSearchResult.pageSize, employeesSearchResult.sortBy, employeesSearchResult.sortDesc,
                    createLoadModelDataResponder(onEmployeeSearchResultLoaded));
        }

        override protected function initializeBackingProperties():void {
            if(employeesSearchResult.sortBy == 'employeeId' && !hasNonNumericSourceEmpIds) {
                employeesSearchResult.compareFunction = function compareNumericEmployeeId(itemA:Object, itemB:Object, fields:Array = null):int {
                    var compareResult:int;
                    if(itemA == null || itemB == null) {
                        return 0;
                    }
                    var aId:int = parseInt(EmployeeInfo(itemA).employeeId);
                    var bId:int = parseInt(EmployeeInfo(itemB).employeeId);
                    compareResult = ObjectUtil.numericCompare(aId, bId);
                    if(employeesSearchResult.sortDesc) {
                        return compareResult * -1;
                    }
                    return compareResult;
                };
            } else {
                employeesSearchResult.compareFunction = null;
            }
            employeesSearchResult.refresh();

        }


        private function onEmployeeSearchResultLoaded(e:ResultEvent):void{
            employeesSearchResult.searchResults = SearchResults(e.result);
            populateStateLists();
            //to differentiate b/w default load and search.
            if(searchButtonClicked){
                searchResultFound = employeesSearchResult.totalRecords > 0;
            } else {
                cloudEmployeesFound = employeesSearchResult.totalRecords > 0;
            }
        }


        public function searchEmployees():void {
            searchButtonClicked = true;
            employeesSearchResult.reset();
            refresh();
        }

        public function populateStateLists():void{
            if((workStateList != null && workStateList.length > 0)  || (liveStateList != null && liveStateList.length > 0 )){
                return;
            }
            var t_WorkStateList:ArrayCollection = new ArrayCollection();
            var t_LiveStateList:ArrayCollection = new ArrayCollection();

            t_LiveStateList.addItem("");
            t_WorkStateList.addItem("");

            for each(var employeeInfo:EmployeeInfo in employeesSearchResult){
                if(employeeInfo.stateWork != null && !t_WorkStateList.contains(employeeInfo.stateWork)){
                    t_WorkStateList.addItem(employeeInfo.stateWork)
                }
                if(employeeInfo.stateLive != null && !t_LiveStateList.contains(employeeInfo.stateLive)){
                    t_LiveStateList.addItem(employeeInfo.stateLive)
                }
            }
            workStateList = t_WorkStateList;
            liveStateList = t_LiveStateList;
            workStateList.refresh();
            liveStateList.refresh();
        }

         public function goToEmployeeBankScreen():void {
           inspector.findPage(CompanyInspectorPageEnum.COMPANY_BANK).activatePage();
        }

        public function getFilterString():String {
            var filterString:String = EMPTY_STRING;

            if (StringUtil.trim(selectedWorkState).length > 0) {
                filterString += "Work State: " + selectedWorkState + ", ";
            }
            if (StringUtil.trim(selectedLiveState).length > 0) {
                filterString += "Live State: " + selectedLiveState + ", ";
            }
            if (StringUtil.trim(ssn).length > 0) {
                filterString += "SSN: " + ssn + ", ";
            }
            if (StringUtil.trim(name).length > 0) {
                filterString += "Name: " + name + ", ";
            }
            if (StringUtil.trim(employeeId).length > 0) {
                filterString += "Employee ID: " + employeeId + ", ";
            }
            filterString = filterString.substr(0, filterString.length - 2);
            return filterString;
        }
    }
}