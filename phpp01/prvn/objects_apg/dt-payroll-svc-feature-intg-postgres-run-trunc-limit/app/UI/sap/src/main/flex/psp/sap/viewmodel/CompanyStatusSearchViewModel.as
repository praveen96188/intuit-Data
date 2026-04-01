package psp.sap.viewmodel
{
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	import mx.events.CollectionEvent;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.collections.PaginationCollection;
	import psp.sap.application.enums.RiskInspectorPageEnum;
	import psp.sap.application.events.SAPEvent;
	import psp.sap.model.ServiceSubStatus;
	import psp.sap.model.SearchResults;	

	public class CompanyStatusSearchViewModel extends AbstractPartViewModel									
	{ 	
		private var mOnHoldSubStatuses:ArrayCollection;
		private var mPendingActivationSubStatuses:ArrayCollection;
		private var mOnHoldSearchResults:PaginationCollection;
		private var mPendingActivationSearchResults:PaginationCollection;		
		private var mCanSearchOnHold:Boolean = false;
		private var mCanSearchPending:Boolean = false;							
		
		public function CompanyStatusSearchViewModel()
		{
			this.label = RiskInspectorPageEnum.COMPANY_STATUS_SEARCH;
			this.reloadOnActivate = false;
			this.loadOnActivate = false;
			
			onHoldSearchResults = new PaginationCollection();
			pendingActivationSearchResults = new PaginationCollection();
		}
		
		private function onSearchCriteriaSelectionsChanged(e:CollectionEvent):void {
			updateCanSearchOnHold();
			updateCanSearchPending();			
		}
		
		private function updateCanSearchOnHold():void {
			for each(var subStatus:ServiceSubStatus in mOnHoldSubStatuses) {
				if (subStatus.selected == true) {
					canSearchOnHold = true;
					return;
				}
			}

			canSearchOnHold = false;
		}
		
		private function updateCanSearchPending():void {
			for each(var pendingActivationSubStatus:ServiceSubStatus in mPendingActivationSubStatuses) {
				if (pendingActivationSubStatus.selected == true) {
					canSearchPending = true;
					return;
				}
			}
			
			canSearchPending = false;			
			
		}
		
		
		override protected function onActivated():void {
			canSearchOnHold = false;
			canSearchPending = false;
		}
		
		[Bindable]		
		public function get canSearchOnHold():Boolean {
			return  mCanSearchOnHold;
		}
		
		public function set canSearchOnHold(value:Boolean):void {
			mCanSearchOnHold = value;
		}
		
		[Bindable]		
		public function get canSearchPending():Boolean {
			return  mCanSearchPending;
		}
		
		public function set canSearchPending(value:Boolean):void {
			mCanSearchPending = value;
		}
		
		public function searchWithSameCriteria():void {
			search(true);
		}			
		
		public function search(useSameCriteria:Boolean=false):void {
            if(!useSameCriteria) {
                onHoldSearchResults = null;
                pendingActivationSearchResults = null;
            }
			refresh();
		}							


		override protected function loadModelData():void {
			
						
			var subStatusesToSearchFor:ArrayCollection = new ArrayCollection();
			
			if (currentSearchIndex == 0) {
			
				for each(var currOnHoldSubStatus:ServiceSubStatus in mOnHoldSubStatuses) {
					if (currOnHoldSubStatus.selected == true) {
						subStatusesToSearchFor.addItem(currOnHoldSubStatus);
					}
				}
			} else {					
				for each(var currPendingActivationSubStatus:ServiceSubStatus in mPendingActivationSubStatuses) {
					if (currPendingActivationSubStatus.selected == true) {
						subStatusesToSearchFor.addItem(currPendingActivationSubStatus);
					}
				}	
			}		
			
			SAP.instance.companyService.getCompaniesByServiceSubstatuses(subStatusesToSearchFor,
											   currentSearchIndex == 0,
											   currentSearchIndex == 0 ? onHoldSearchResults.sortBy : pendingActivationSearchResults.sortBy,
											   currentSearchIndex == 0 ? onHoldSearchResults.sortDesc : pendingActivationSearchResults.sortDesc,
											   currentSearchIndex == 0 ? onHoldSearchResults.startIndex : pendingActivationSearchResults.startIndex,
											   currentSearchIndex == 0 ? onHoldSearchResults.pageSize : pendingActivationSearchResults.pageSize,
                                               createLoadModelDataResponder(onSearchCompleted));
                                               
			
			lastSearchOnHold = currentSearchIndex == 0;                                               
									
		}

		private function onSearchCompleted(e:ResultEvent):void {
			var searchReturn:SearchResults = e.result as SearchResults;
			if(currentSearchIndex == 0){
				onHoldSearchResults.source = searchReturn.returnsList.source;
				onHoldSearchResults.totalRecords = searchReturn.totalRecords;
			}
			else {
				pendingActivationSearchResults.source = searchReturn.returnsList.source;
				pendingActivationSearchResults.totalRecords = searchReturn.totalRecords;
			}		
		}

		private function loadStatusLists():void {
			if (SAP.instance.lookupService.serviceSubStatuses == null){
				SAP.instance.lookupService.addEventListener(SAPEvent.DATA_LOAD_COMPLETED,loadStatusListsAsync, false, 0, true);				
				return;
			}

			if (mOnHoldSubStatuses != null) {
				mOnHoldSubStatuses.removeEventListener(CollectionEvent.COLLECTION_CHANGE, onSearchCriteriaSelectionsChanged, false);
			}
			onHoldSubStatuses = new ArrayCollection(SAP.instance.lookupService.serviceSubStatuses.source);
			onHoldSubStatuses.filterFunction = onHoldFilter;
			onHoldSubStatuses.refresh();
			onHoldSubStatuses.addEventListener(CollectionEvent.COLLECTION_CHANGE, onSearchCriteriaSelectionsChanged, false, 0, true);
			
			if (mPendingActivationSubStatuses != null) {
				mPendingActivationSubStatuses.removeEventListener(CollectionEvent.COLLECTION_CHANGE, onSearchCriteriaSelectionsChanged, false);
			}			
			pendingActivationSubStatuses = new ArrayCollection(SAP.instance.lookupService.serviceSubStatuses.source);
			pendingActivationSubStatuses.filterFunction = onPendingActivationFilter;
			pendingActivationSubStatuses.refresh();
			pendingActivationSubStatuses.addEventListener(CollectionEvent.COLLECTION_CHANGE, onSearchCriteriaSelectionsChanged, false, 0, true);
		}
		
		private function loadStatusListsAsync(event:Event):void{
			SAP.instance.lookupService.removeEventListener(SAPEvent.DATA_LOAD_COMPLETED,loadStatusListsAsync);
			loadStatusLists();
		}
		
		override protected function onActivating():void {
			loadStatusLists();			
		}
		
		public function onHoldFilter(item:ServiceSubStatus):Boolean {
			return (item.subStatusType=="On Hold");
		}
		
		public function onPendingActivationFilter(item:ServiceSubStatus):Boolean {
			return (item.subStatusType=="Pending Activation");
		}
		
		[Bindable]
		public function get onHoldSubStatuses():ArrayCollection {
			return mOnHoldSubStatuses;
		}

		public function set onHoldSubStatuses(value:ArrayCollection):void {
			mOnHoldSubStatuses=value;
		}

		[Bindable]				
		public function get pendingActivationSubStatuses():ArrayCollection {
			return mPendingActivationSubStatuses;
		}
		
		public function set pendingActivationSubStatuses(value:ArrayCollection):void {
			mPendingActivationSubStatuses = value;
		}		
		
		[Bindable]
		[ArrayElementType("psp.sap.model.CompanyStatusSearchResult")]
		public function get onHoldSearchResults():PaginationCollection {
			return mOnHoldSearchResults;
		}

		protected function set onHoldSearchResults(value:PaginationCollection):void {					
			if (value == null)
				value = new PaginationCollection();						
			mOnHoldSearchResults = value;						
		}
		
		[Bindable]
		[ArrayElementType("psp.sap.model.CompanyStatusSearchResult")]
		public function get pendingActivationSearchResults():PaginationCollection {
			return mPendingActivationSearchResults;
		}

		protected function set pendingActivationSearchResults(value:PaginationCollection):void {					
			if (value == null)
				value = new PaginationCollection();						
			mPendingActivationSearchResults = value;						
		}				
		
		public function getFilterString():String {
			
			var newFilterString:String;
			var any:Boolean = false;
			
			if (currentSearchIndex != 1) {				
				newFilterString = "On Hold: ";
				for each(var currOnHoldSubStatus:ServiceSubStatus in mOnHoldSubStatuses) {
					if (currOnHoldSubStatus.selected == true) {
						newFilterString += currOnHoldSubStatus.subStatusName + "/";
						any=true;
					}
				}			
			} else {
				if (mPendingActivationSubStatuses.length == 0) return "";
				newFilterString = "Pending Activation: ";				
				for each(var currPendingStatus:ServiceSubStatus in mPendingActivationSubStatuses) {
					if (currPendingStatus.selected == true) {
						newFilterString += currPendingStatus.subStatusName + "/";
						any=true;
					}
				}
			}
			
			if (! any) return "";
			
			newFilterString = newFilterString.substring(0,newFilterString.length-1);	
			return newFilterString;		
		}
		
		private var mCurrentSearchIndex:int=0;
		
		[Bindable]
		public function get currentSearchIndex():int {
			return mCurrentSearchIndex;
		}
		
		public function set currentSearchIndex(value:int):void {
			if(value == -1){
				value = 0;
			}
			mCurrentSearchIndex = value;
		}
		
		private var mLastSearchOnHold:Boolean;
		
		[Bindable]
		public function get lastSearchOnHold():Boolean {
			return mLastSearchOnHold;
		}
		
		public function set lastSearchOnHold(value:Boolean):void {
			mLastSearchOnHold = value;
		}
		
		
			
		
						
	}
}
