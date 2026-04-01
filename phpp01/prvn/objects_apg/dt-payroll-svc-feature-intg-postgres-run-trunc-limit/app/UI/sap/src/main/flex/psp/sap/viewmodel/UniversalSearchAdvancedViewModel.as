package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.core.Application;
    import mx.events.PropertyChangeEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
import psp.sap.application.collections.VMPEmployeeSearchPaginationCollection;
import psp.sap.application.enums.CompanySearchEnum;
    import psp.sap.application.events.UniversalSearchEvent;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.CompanySearchResult;
    import psp.sap.model.EmployeeSearchResult;
import psp.sap.model.SearchResults;
import psp.sap.model.VMPEmployeePaginationDetails;
import psp.sap.view.controls.UniversalSearchFieldViewModel;

    public class UniversalSearchAdvancedViewModel extends CompositePartViewModel
	{

		public var clearResults:Boolean=false;
		public var doSearch:Boolean=true;

        [Bindable] [BackingProperty]
        public var currentPage:Number=0;

        [Bindable]
        public var pageSize:Number=15;

        [Bindable]
        public var employeeCountResults:Number=-1;

        [Bindable]
        public var recountEmployees:Boolean=true;

        [Bindable]
        public var currentPaginationDetails:VMPEmployeePaginationDetails= new VMPEmployeePaginationDetails();

		[Bindable] public var searchFieldViewModel:UniversalSearchFieldViewModel;

		[ArrayElementType("psp.sap.model.CompanySearchResult")]		
		[Bindable]
		public var searchResults:ArrayCollection = new ArrayCollection();

        [ArrayElementType("psp.sap.model.EmployeeSearchResult")]
        [Bindable]
        public var employeeSearchResults:VMPEmployeeSearchPaginationCollection = new VMPEmployeeSearchPaginationCollection(null, pageSize);
		
		private var mSearchResultsSort:Sort = new Sort();

		public function UniversalSearchAdvancedViewModel() {
			mSearchResultsSort.fields = [new SortField("legalName", false)];

            searchFieldViewModel = new UniversalSearchFieldViewModel();
            searchFieldViewModel.addEventListener(UniversalSearchEvent.UNIVERSAL_SEARCH,onSearch, false, 0, true);

		}

		private function onSearch(event:UniversalSearchEvent):void {
            currentPage=0;
            recountEmployees=true;
			refresh();
		}
		
		override protected function onActivating():void {
            currentPage=0;
            recountEmployees=true;
			if (clearResults) {
				searchFieldViewModel.searchText = "";
				//todo this won't actually work because a two-way binding is messing up, but 
				//it maybe might possibly work with some finagling when it becomes needed
				searchFieldViewModel.searchType = CompanySearchEnum.SEARCH_SMART;
				searchResults = new CompanyCollectionViewModel();
				
				clearResults = false;
			}

			//Autosearch for easy testing
			//autosearch=* will search for all companies
			var qs:Object = Application.application.getQueryStringParameters();
			if ("autosearch" in qs) {
				searchFieldViewModel.searchText = qs["autosearch"] == "*" ? "%%%" : qs["autosearch"];
				doSearch = true;
			}

		}
		
		override protected function onActivated():void {
			//we don't want to come in and do a search always, but 
			//we do want to do a search when they hit the button
			doSearch = true;
            searchFieldViewModel.updateSearchMenuOptions();
		}

		override protected function loadModelData():void {
			if (searchFieldViewModel.canSearch && doSearch) {
                //if we have something to search, search here; otherwise have nothing to do
                //also if instructed not to search (ie just show results, don't do anything)                

                // remove the current results
                employeeSearchResults.removeAll();
                searchResults.removeAll();

                if(searchFieldViewModel.searchType == CompanySearchEnum.SEARCH_VMP_EMPLOYEE_BY_SSN
                        || searchFieldViewModel.searchType == CompanySearchEnum.SEARCH_VMP_EMPLOYEE_BY_EMAIL
                        || searchFieldViewModel.searchType == CompanySearchEnum.SEARCH_BY_CFR
                        // || searchFieldViewModel.searchType == CompanySearchEnum.SEARCH_VMP_EMPLOYEE_BY_NAME
                        ) {
                    resetPaginationDetails();
                    if(recountEmployees) {
                        employeeSearchResults.reset();
                        if(searchFieldViewModel.searchType==CompanySearchEnum.SEARCH_VMP_EMPLOYEE_BY_SSN) {
                            currentPaginationDetails.sortBy = "employeeSSN";
                            employeeSearchResults.sortBy="employeeSSN";
                        }
                        else if(searchFieldViewModel.searchType==CompanySearchEnum.SEARCH_VMP_EMPLOYEE_BY_EMAIL) {
                            currentPaginationDetails.sortBy = "employeeEmail";
                            employeeSearchResults.sortBy="employeeEmail";
                        } else if (searchFieldViewModel.searchType==CompanySearchEnum.SEARCH_BY_CFR) {
                            currentPaginationDetails.sortBy = "employeeSSN";
                            employeeSearchResults.sortBy="employeeSSN";
                        }
                        currentPaginationDetails.sortDesc=false;
                        SAP.instance.viewMyPaycheckService.findVMPEmployeeCount(searchFieldViewModel.searchType, searchFieldViewModel.searchText,
                                createLoadModelDataResponder(onEmployeeCountCompleted));
                    }
                    SAP.instance.viewMyPaycheckService.findVMPEmployee(searchFieldViewModel.searchType, searchFieldViewModel.searchText, currentPaginationDetails,
                            createLoadModelDataResponder(onEmployeeSearchCompleted));
                } else {
                    SAP.instance.companyService.search(searchFieldViewModel.searchType, searchFieldViewModel.searchText,
                            createLoadModelDataResponder(onSearchCompleted));
                }

            } else {
            	modelDataLoaded();
            }
		}
		
		override protected function initializeBackingProperties():void  {
			searchResults.sort = mSearchResultsSort;
			searchResults.refresh();

			//if only one result just go there directly and spare the user the pain of an extra click
            if (searchResults.length == 1 && doSearch) {
				doSearch = false;
				cancel();
				(searchResults.getItemAt(0) as CompanySearchResult).key.display();
			}
		}



        private function onEmployeeCountCompleted(e:ResultEvent):void {
            employeeCountResults = Number(e.result);
            //this forces the data provider on the dg to be set
            //which does their normal punting and unselects things
            //which in this case is actually needed (whodathought)
            //so it doesn't try selecting an imaginary row
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this,"employeeCountResults",null,employeeCountResults));
        }

        private function onEmployeeSearchCompleted(e:ResultEvent):void {
            employeeSearchResults.source = ArrayCollection(e.result).toArray();
            employeeSearchResults.startIndex=currentPage;
            employeeSearchResults.totalRecords=employeeCountResults;
            if (employeeCountResults==1) {
                cancel();
                (employeeSearchResults.source[0] as EmployeeSearchResult).display();
            }
            //this forces the data provider on the dg to be set
            //which does their normal punting and unselects things
            //which in this case is actually needed (whodathought)
            //so it doesn't try selecting an imaginary row
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this,"employeeSearchResults",null,employeeSearchResults));

        }
		private function onSearchCompleted(e:ResultEvent):void {
			searchResults.source = ArrayCollection(e.result).toArray();
			//this forces the data provider on the dg to be set
			//which does their normal punting and unselects things
			//which in this case is actually needed (whodathought) 
			//so it doesn't try selecting an imaginary row
			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this,"searchResults",null,searchResults));
		}
			
		public function displayCompany(key:CompanyKey):void {
			//close popup and display company
			cancel();			
			key.display();
		}

        public function displayVMPEmployeeDetails(employeeInfo:EmployeeSearchResult):void {
            //close popup and display company
            cancel();
            employeeInfo.display();
        }

        public function pageDataGridLoadDataFunction():void {
            currentPage=employeeSearchResults.startIndex;
            recountEmployees=false;
            refresh();
        }

        private function resetPaginationDetails():void {
            currentPaginationDetails.currentPage=currentPage;
            currentPaginationDetails.pageSize=pageSize;
            currentPaginationDetails.sortBy=employeeSearchResults.sortBy;
            currentPaginationDetails.sortDesc=employeeSearchResults.sortDesc;
        }

    }
}
