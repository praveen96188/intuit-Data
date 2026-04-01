package psp.sap.viewmodel
{
	import mx.rpc.Responder;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.BankReturnsSearchFilterEnum;

	public class BankReturnsSearchAllReturnsViewModel extends BankReturnsSearchBaseViewModel
	{
		public static const SEARCH_FILTER_1:Array = [BankReturnsSearchFilterEnum.ALL_RETURNS, BankReturnsSearchFilterEnum.OPEN_RETURNS, BankReturnsSearchFilterEnum.RESOLVED_RETURNS];
		public static const SEARCH_FILTER_2:Array = [BankReturnsSearchFilterEnum.ALL_RETURNS, BankReturnsSearchFilterEnum.EE_CREDIT_RETURNS, BankReturnsSearchFilterEnum.EE_DEBIT_RETURNS, BankReturnsSearchFilterEnum.ER_CREDIT_RETURNS, BankReturnsSearchFilterEnum.ER_DEBIT_RETURNS];		
		
		private const DEFAULT_excludeCReturnCodes:Boolean = false;
		private const DEFAULT_exclude5DayFunding:Boolean = false;
		private const DEFAULT_SearchFilter1:String = BankReturnsSearchFilterEnum.OPEN_RETURNS;
		private const DEFAULT_SearchFilter2:String = BankReturnsSearchFilterEnum.ALL_RETURNS;
		
		private var mExclude_CReturnCodes:Boolean;
		private var mExclude_5DayFunding:Boolean;	
		private var mLastExclude_CReturnCodes:Boolean;
		private var mLastExclude_5DayFunding:Boolean;
		private var mSearchFilter1:String;
		private var mSearchFilter2:String;
		private var mLastSearchFilter1:String;
		private var mLastSearchFilter2:String;
		private var mLastShowOpenReturns:Boolean;
		private var mLastShowResolvedReturns:Boolean;
		
		public var transactionType:String = null;
		public var transactionCategory:String = null;
				
		public function BankReturnsSearchAllReturnsViewModel()
		{
			super();
			
			//Set defaults
			excludeCReturnCodes = DEFAULT_excludeCReturnCodes;
			exclude5DayFunding = DEFAULT_exclude5DayFunding;
			searchFilter1 = DEFAULT_SearchFilter1;
			searchFilter2 = DEFAULT_SearchFilter2;
		}
		
		[Bindable]
		public function get searchFilter1():String {
			return mSearchFilter1;
		}
		
		public function set searchFilter1(value:String):void {
			if (value == null)
				value = DEFAULT_SearchFilter1;
			mSearchFilter1 = value;
		}
		
		public function get lastSearchFilter1():String {
			return mLastSearchFilter1;
		}
		
		public function set lastSearchFilter1(value:String):void {
			mLastSearchFilter1 = value;
		}
		
		[Bindable]
		public function get searchFilter2():String {
			return mSearchFilter2;
		}
		
		public function set searchFilter2(value:String):void {
			if (value == null)
				value = DEFAULT_SearchFilter2;
			mSearchFilter2 = value;
		}
		
		public function get lastSearchFilter2():String {
			return mLastSearchFilter2;
		}
		
		public function set lastSearchFilter2(value:String):void {
			mLastSearchFilter2 = value;
		}
		
		[Bindable]
		public function get excludeCReturnCodes():Boolean {
			return mExclude_CReturnCodes;
		}
		
		public function set excludeCReturnCodes(value:Boolean):void {
			mExclude_CReturnCodes = value;
		}
		
		private function get lastExcludeCReturnCodes():Boolean {
			return mLastExclude_CReturnCodes;
		}
		
		private function set lastExcludeCReturnCodes(value:Boolean):void {
			mLastExclude_CReturnCodes = value;
		}
		
		[Bindable]
		public function get exclude5DayFunding():Boolean {
			return mExclude_5DayFunding;
		}
		
		public function set exclude5DayFunding(value:Boolean):void {
			mExclude_5DayFunding = value;
		}
		
		private function get lastExclude5DayFunding():Boolean {
			return mLastExclude_5DayFunding;
		}
		
		private function set lastExclude5DayFunding(value:Boolean):void {
			mLastExclude_5DayFunding = value;
		}
		
		public function get showOpenReturns():Boolean {
			return (searchFilter1 == BankReturnsSearchFilterEnum.OPEN_RETURNS);
		}							
		
		public function get showResolvedReturns():Boolean {
			return (searchFilter1 == BankReturnsSearchFilterEnum.RESOLVED_RETURNS);
		}				
		
		public function setupAllReturnSearch():void {

			switch(searchFilter2) {
				case BankReturnsSearchFilterEnum.EE_CREDIT_RETURNS:
					transactionType = "Credit";
					transactionCategory = "Employee";
					break;
				case BankReturnsSearchFilterEnum.EE_DEBIT_RETURNS:
					transactionType = "Debit";
					transactionCategory = "Employee";
					break;
				case BankReturnsSearchFilterEnum.ER_CREDIT_RETURNS:
					transactionType = "Credit";
					transactionCategory = "Employer";
					break;
				case BankReturnsSearchFilterEnum.ER_DEBIT_RETURNS:
					transactionType = "Debit";
					transactionCategory = "Employer";
					break;
				default:
					transactionType = null;
					transactionCategory = null;
			}
		}
		
		override public function searchBankReturns(useLastCriteria:Boolean = false):void {
			setupSearch();
			setupAllReturnSearch();
			
			// save search criteria
			if(!useLastCriteria){
				lastEin = ein;
				lastStartDate = startDate;
				lastEndDate = endDate;
                lastSearchFilter1 = searchFilter1;
                lastSearchFilter2 = searchFilter2;
                lastExclude5DayFunding = exclude5DayFunding;
                lastExcludeCReturnCodes = excludeCReturnCodes;
                lastAchReturn = achReturn;               
			}
			// reload search criteria
			else {
				ein = lastEin;
				startDate = lastStartDate;
				endDate = lastEndDate;
                searchFilter1 = lastSearchFilter1;
                searchFilter2 = lastSearchFilter2;
                exclude5DayFunding = lastExclude5DayFunding;
                excludeCReturnCodes = lastExcludeCReturnCodes;
                achReturn = lastAchReturn; 
			}
			
			SAP.instance.bankReturnService.findCompanyBankReturns(
                                               (ein.length > 0) ? ein : null,
                                               startDateValue,
                                               endDateValue,
                                               showOpenReturns,
    		                                   showResolvedReturns,
                                               transactionType,
                                               transactionCategory,
                                               exclude5DayFunding,
                                               excludeCReturnCodes ? "R" : null,
                                               achReturnValue,
											   searchResults.sortBy,
											   searchResults.sortDesc,
											   searchResults.startIndex,
											   searchResults.pageSize,
                                               false,
                                               new Responder(onSearchCompleted, onSearchFaulted));
		}
		
		override public function getReportTypeCode():String {
			return "AllReturns";
		}
		
	}
}
