package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.model.DateRangeEnum;
	import psp.sap.model.SourceSystemEnum;
	
	public class PayrollConnectionLogViewModel
		extends AbstractPartViewModel
	{
		// defaults		
		private static const DEFAULT_SOURCE_SYSTEM:SourceSystemEnum = SourceSystemEnum.QBDT;
		
		// members
		private var mTransactions:ArrayCollection = new ArrayCollection();
		private var mIsFirstTimeToPage:Boolean = true;
		private var mSort:Sort;
		private var mDateSelectionViewModel:DateSelectionViewModel;
		private var mSourceSystems:Array;
		private var mSourceSystem:SourceSystemEnum;
				
		
		// validators
		
		public function PayrollConnectionLogViewModel()
        {
            this.label = CompanyInspectorPageEnum.PAYROLL_CONNECTION_LOG;
            this.reloadOnActivate = false;
            
            mDateSelectionViewModel = new DateSelectionViewModel(this);
            mDateSelectionViewModel.defaultDateRange = DateRangeEnum.LAST_7_DAYS;                       
			
			dateSelectionViewModel.dateRange = dateSelectionViewModel.defaultDateRange;
			
			// table sorting			
			mSort = new Sort();
		    mSort.fields = [new SortField("initializeDateTime", false, true), new SortField("requestToken", false, true)];
		    sourceSystems = SourceSystemEnum.connectionLogList;		
		    
		    sourceSystem = DEFAULT_SOURCE_SYSTEM;				            
        }
        
        // getters and setters 
        [Bindable]
		public function get sourceSystems():Array {
			return mSourceSystems;
		}

		public function set sourceSystems(value:Array):void {
			mSourceSystems = value;
		}

		[Bindable]
		public function get sourceSystem():SourceSystemEnum {
			return mSourceSystem;
		}

		public function set sourceSystem(value:SourceSystemEnum):void {
			if(value == null){
				value = DEFAULT_SOURCE_SYSTEM;
			}
			mSourceSystem = value;							
		}
		       		
		[Bindable]
		public function set transmissions(value:ArrayCollection):void {
			mTransactions = value;
		}
		
		public function get transmissions():ArrayCollection {
			return mTransactions;
		}
		
		public function get dateSelectionViewModel():DateSelectionViewModel {
			return mDateSelectionViewModel;
		}																		
		
		// override functions
		override public function get hasChanged():Boolean {
			return true;
		} 		
		
		override protected function loadModelData():void {					
			isDataLoading = true;
			saveMsg = "";
			saveFaulted = false;
			
			// todo: pagging indexes
			SAP.instance.companyService.findTransmissions(company.sourceSystemCd,
                                               company.companyId,
                                               dateSelectionViewModel.startDateValue,
                                               dateSelectionViewModel.endDateValue,
                                               sourceSystem.code,                                               
                                               createLoadModelDataResponder(onSearchCompleted));		
		}
		
		
		// search method :todo paging updates
		public function searchTransmissions():void {
			refresh();
		}

		override protected function initializeBackingProperties():void {
			mTransactions.sort = mSort;
			mTransactions.refresh();			
		}

		// todo paging indexing
		private function onSearchCompleted(e:ResultEvent):void {
			transmissions = e.result as ArrayCollection;						
		}


	}
}