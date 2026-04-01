package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.model.DateRangeEnum;
	
	public class ChaseReportViewModel extends AbstractPartViewModel
	{
		// members
		[ArrayElementType ("psp.sap.model.ChaseReport")]
		private var mReports:ArrayCollection = new ArrayCollection();		
		private var mSort:Sort;
		private var mCanPrint:Boolean = false;
		private var mDateSelectionViewModel:DateSelectionViewModel;
		
		public function ChaseReportViewModel()
        {
            this.label = CompanyInspectorPageEnum.CHASE_REPORT; 
            this.reloadOnActivate = false;
            
            mDateSelectionViewModel = new DateSelectionViewModel(this);
            mDateSelectionViewModel.defaultDateRange=DateRangeEnum.LAST_7_DAYS;
            
            dateSelectionViewModel.dateRange = dateSelectionViewModel.defaultDateRange;
            
            // table sorting			
			mSort = new Sort();
		    mSort.fields = [new SortField("connectionDate", false, true)];                      												            
        }
        
        // getters and setters        		
		[Bindable]
		public function set reports(value:ArrayCollection):void {
			mReports = value;
			canPrint = mReports.length > 0;
		}
		
		
		public function get reports():ArrayCollection {
			return mReports;
		}
		
		[Bindable]
		public function set canPrint(value:Boolean):void {
			mCanPrint = value;
		}
		
		public function get canPrint():Boolean {
			return mCanPrint;
		}
		
		[Bindable ("propertyChange")]
		public function get dateSelectionViewModel():DateSelectionViewModel {
			return mDateSelectionViewModel;
		}																		
		
		// override functions
		override public function get hasChanged():Boolean {
			return true;
		} 										
		
		override protected function loadModelData():void {
			SAP.instance.payrollRunService.findChaseReportForDateRange(company.sourceSystemCd,
                                               company.companyId,
                                               dateSelectionViewModel.startDateValue,
                                               dateSelectionViewModel.endDateValue,                                               
                                               createLoadModelDataResponder(onSearchCompleted));
		}
		
		// search method :todo paging updates
		public function searchForReports():void {
			refresh();			
		}

		// todo paging indexing
		private function onSearchCompleted(e:ResultEvent):void {
			reports = e.result as ArrayCollection;			
			mReports.sort = mSort;
			mReports.refresh();			
		}

		
		public function print():void {
			// todo
		} 

	}
}