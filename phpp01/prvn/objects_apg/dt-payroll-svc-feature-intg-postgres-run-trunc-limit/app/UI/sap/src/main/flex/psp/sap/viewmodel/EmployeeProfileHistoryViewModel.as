package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.model.EmployeeInfo;
	import psp.sap.model.PropertyAudit;
	
	public class EmployeeProfileHistoryViewModel extends AbstractPartViewModel
	{
		private static const DEFAULT_FILTER:String = "";
		private var mPropertyAuditHistory:ArrayCollection;
		private var mEmployeeInfo:EmployeeInfo;
		private var mPropertyFilters:ArrayCollection;
		private var mCurrentFilter:String;
		private var mFilteredPropertyHistory:ArrayCollection;
		private var mHasEmployeeProfileHistory:Boolean = false;
		
		public function EmployeeProfileHistoryViewModel()
		{
			label = CompanyInspectorPageEnum.EMPLOYEE_PROFILE_HISTORY;
		}
		
		[Bindable]
		public function get propertyAuditHistory():ArrayCollection{
			return mPropertyAuditHistory;
		}
		
		public function set propertyAuditHistory(value:ArrayCollection):void{
			
			mPropertyAuditHistory = value;
		}
		
		[Bindable]
		public function get filteredPropertyHistory():ArrayCollection{
			return mFilteredPropertyHistory;
		}
		
		public function set filteredPropertyHistory(value:ArrayCollection):void{
			
			mFilteredPropertyHistory = value;
		}
		
		[Bindable]
		public function get propertyFilters():ArrayCollection{
			return mPropertyFilters;
		}
		
		public function set propertyFilters(value:ArrayCollection):void{
			
			mPropertyFilters = value;
		}
		

		[Bindable]
		public function get hasEmployeeProfileHistory():Boolean {
			return mHasEmployeeProfileHistory;
		}
		public function set hasEmployeeProfileHistory(value:Boolean):void {
			mHasEmployeeProfileHistory = value;
		}
		
		[Bindable]
		public function get employeeInfo():EmployeeInfo {
			return mEmployeeInfo;
		}
		public function set employeeInfo(value:EmployeeInfo):void {
			mEmployeeInfo = value;
		}

		override protected function loadModelData():void{
			
			
			SAP.instance.employeeService.getEmployeeProfileHistory(company.sourceSystemCd, company.companyId, employeeInfo.employeeGseq,
				createLoadModelDataResponder(onEmployeeHistoryLoaded));
		}

		private function onEmployeeHistoryLoaded(e:ResultEvent):void{
			propertyAuditHistory = e.result as ArrayCollection;
			hasEmployeeProfileHistory = propertyAuditHistory.length > 0;			

			var newFilterableCategories:ArrayCollection = new ArrayCollection();
			
			for each (var propertyAudit:PropertyAudit in propertyAuditHistory) {
				if (! newFilterableCategories.contains(propertyAudit.propertyName)) {
					newFilterableCategories.addItem(propertyAudit.propertyName);
				}
			}
			
			newFilterableCategories.addItemAt("",0);
						
			newFilterableCategories.sort = new Sort();
			newFilterableCategories.refresh();
			
			propertyFilters = newFilterableCategories;
			
			updateFilteredPropertyHistory();
		}

		override protected function initializeBackingProperties():void
		{
			currentFilter = DEFAULT_FILTER;
		} 

		[Bindable]	
		public function get currentFilter():String {
			return mCurrentFilter;
		}
		
		public function set currentFilter(value:String):void {
			if(value==null)
			{
				value = DEFAULT_FILTER;
			}
			mCurrentFilter = value;
			updateFilteredPropertyHistory();
		}
		
		
		public function updateFilteredPropertyHistory():void {
			var value:String = mCurrentFilter;
			
			if(value==null)
			{
				value = DEFAULT_FILTER;
			}
			
			if (value == "") {
				filteredPropertyHistory = propertyAuditHistory;
			} else {
				var newFilteredPropertyHistory:ArrayCollection = new ArrayCollection();
				for each (var propertyAudit:PropertyAudit in propertyAuditHistory) {
					if (propertyAudit.propertyName == value) {
						newFilteredPropertyHistory.addItem(propertyAudit);
					}
				}
				filteredPropertyHistory = newFilteredPropertyHistory;
			}
			
		}
	
	}
}