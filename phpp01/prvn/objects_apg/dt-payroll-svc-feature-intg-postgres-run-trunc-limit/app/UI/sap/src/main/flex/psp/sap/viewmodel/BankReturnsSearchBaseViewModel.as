package psp.sap.viewmodel
{
    import flash.events.EventDispatcher;

    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.events.CollectionEvent;
    import mx.events.PropertyChangeEvent;
    import mx.events.ValidationResultEvent;
    import mx.formatters.DateFormatter;
    import mx.formatters.NumberFormatter;
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.validators.DateValidator;
    import mx.validators.NumberValidator;
    import mx.validators.StringValidator;
    import mx.validators.ValidationResult;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.PaginationCollection;
    import psp.sap.model.BankReturn;
    import psp.sap.model.DateRangeEnum;
    import psp.sap.model.SearchResults;
    import psp.sap.validators.SAPValidators;

    public class BankReturnsSearchBaseViewModel extends EventDispatcher

	{
		private const DEFAULT_EIN:String = "";		
		private const DEFAULT_ERROR:String = "";									
		
		private var mDateFormatter:DateFormatter = new DateFormatter();
		private var mStartDateValidator:DateValidator;
		private var mEndDateValidator:DateValidator;
		private var mEinValidator:StringValidator;
		private var mAchReturnValidator:NumberValidator;
		
		private var mValidators:Array = [];		
		private var stopExtendedInfo:Boolean = false;
		private var mSort:Sort = new Sort();
		private var mNumberFormatter:NumberFormatter = new NumberFormatter();
		
		public var firstTimeLoad:Boolean = true;
		public var searchOnLoad:Boolean = true;	
		
		[Bindable]
		public var extendedInformationLoaded:Boolean = false;
		
		public function BankReturnsSearchBaseViewModel()
		{


			// init formatter
			mDateFormatter.formatString = SAP.instance.configuration.dateFormatShort;

			// init validators
			mStartDateValidator = new DateValidator();
			mStartDateValidator.source = this;
			mStartDateValidator.trigger = this;
			mStartDateValidator.property = "startDate"
			mStartDateValidator.required = false;
			validators.push(mStartDateValidator);

			mEndDateValidator = new DateValidator();
			mEndDateValidator.source = this;
			mEndDateValidator.trigger = this;
			mEndDateValidator.property = "endDate"
			mEndDateValidator.required = false;
			validators.push(mEndDateValidator);

			mEinValidator = SAPValidators.createStringValidator(this, "ein", false, 5);
			mEinValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
			mEinValidator.trigger = this;
			validators.push(mEinValidator);
			
			mAchReturnValidator = SAPValidators.createNumberValidator(this, "achReturn", false, 0, null, null, 2);
			mAchReturnValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
			mAchReturnValidator.trigger = this;
			validators.push(mAchReturnValidator);
			
			mNumberFormatter.precision = 2;
			mNumberFormatter.useThousandsSeparator = false;
			
			this.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onDateChanged, false, int.MAX_VALUE, true);			
		}


		private var mCanSearch:Boolean = true;
		private var mIsSearching:Boolean = false;
		private var mErrorString:String;
		private var mIsError:Boolean = true;

		// backing properties
		private var mSearchResults:PaginationCollection;
		private var mEIN:String;
		private var mStartDate:String;
		private var mEndDate:String;
		private var mAchReturn:String;
		private var mLastEIN:String;
		private var mLastStartDate:String;
		private var mLastEndDate:String;
		private var mLastAchReturn:String;										

		[Bindable]
		public function get canSearch():Boolean {
			return mCanSearch;
		}

		public function set canSearch(value:Boolean):void {
			mCanSearch = value;
		}

		[Bindable]
		public function get isSearching():Boolean {
			return mIsSearching;
		}

		public function set isSearching(value:Boolean):void {
			if(value == true){
				SAP.instance.showProgress("Searching...");
			}
			else {
				SAP.instance.hideProgress();
			}
			mIsSearching = value;
		}

		[Bindable]
		public function get errorString():String {
			return mErrorString;
		}

		public function set errorString(value:String):void {
			mErrorString = value;
		}

		[Bindable]
		public function get isError():Boolean {
			return mIsError;
		}

		public function set isError(value:Boolean):void {
			mIsError = value;
		}

		[Bindable]
		public function get searchResults():PaginationCollection {
			return mSearchResults;
		}

		public function set searchResults(value:PaginationCollection):void {
			if (value == null)
				value = new PaginationCollection();
			if(mSearchResults != null){
				mSearchResults.removeEventListener(CollectionEvent.COLLECTION_CHANGE, getExtendedInfo);
			}
			
			mSearchResults = value;
			
			if(mSearchResults != null){
				mSearchResults.addEventListener(CollectionEvent.COLLECTION_CHANGE, getExtendedInfo, false, 0, true);
			}	
		}				

		[Bindable]
		public function get ein():String {
			return mEIN;
		}

		public function set ein(value:String):void {
			// strip - and spaces
			mEIN = value.replace("-", "").replace(" ", "");
			updateCanSearch();
		}
		
		public function get lastEin():String {
			return mLastEIN;
		}

		public function set lastEin(value:String):void {
			mLastEIN = value;
		}

		[Bindable]
		public function get startDate():String {
			return mStartDate;
		}

		public function set startDate(value:String):void {
			mStartDate = value;
			updateCanSearch();
		}

		protected function get startDateValue():Date {
			if(mStartDate == ""){
				return null;
			}
			var formattedDate:String = mDateFormatter.format(mStartDate);
			var txDate:Date = SAP.instance.PSPDate;
			var time:Number = Date.parse(formattedDate);
			txDate.setTime(time);
			return txDate;
		}
		
		public function get lastStartDate():String {
			return mLastStartDate;
		}

		public function set lastStartDate(value:String):void {
			mLastStartDate = value;
		}
		
		[Bindable]
		public function get achReturn():String {
			return mAchReturn;
		}
		
		public function set achReturn(value:String):void {
			mAchReturn = value;
			updateCanSearch();
		}
		
		public function get lastAchReturn():String {
			return mLastAchReturn;
		}
		
		public function set lastAchReturn(value:String):void {
			mLastAchReturn = value;
		}

		protected function get achReturnValue():Number {
			return isEmpty(achReturn) ? SAP.instance.configuration.specialNumberForDefault : parseFloat(mNumberFormatter.format(achReturn));	
		}
		
		public function isEmpty(str:String):Boolean {
			return (str == null || str.replace(" ", "").length == 0);
		}
		
		[Bindable]
		public function get endDate():String {
			return mEndDate;
		}

		public function set endDate(value:String):void {
			mEndDate = value;
			updateCanSearch();
		}

		protected function get endDateValue():Date {
			if(mEndDate == ""){
				return null;
			}
			var formattedDate:String = mDateFormatter.format(mEndDate);
			var txDate:Date = SAP.instance.PSPDate;
			var time:Number = Date.parse(formattedDate);
			txDate.setTime(time);
			return txDate;
		}
		
		public function get lastEndDate():String {
			return mLastEndDate;
		}

		public function set lastEndDate(value:String):void {
			mLastEndDate = value;
		}

		public function get einValidator():StringValidator {
			return mEinValidator;
		}

		public function get startDateValidator():DateValidator {
			return mStartDateValidator;
		}

		public function get endDateValidator():DateValidator {
			return mEndDateValidator;
		}
		
		public function get achReturnValidator():NumberValidator {
			return mAchReturnValidator;
		}					

		protected function get validators():Array {
			return mValidators;
		}				

		// this function is public for testing only
		public function resetToDefaults():void {
			ein = DEFAULT_EIN;
			startDate = DateRangeEnum.LAST_30_DAYS.startDate;
			endDate = DateRangeEnum.LAST_30_DAYS.endDate;			
			isSearching = false;
			errorString = DEFAULT_ERROR;
			searchResults = new PaginationCollection();
		}

		private function updateCanSearch():void {
			canSearch = ((startDateValue == null || endDateValue == null) || (startDateValue <= endDateValue)) && SAPValidators.validateAll(mValidators, false).length == 0;
		}

		private function onDateChanged(e:PropertyChangeEvent):void {
			if (e.property != "startDate" && e.property != "endDate")
        		return;

			if(startDateValue != null && endDateValue != null && startDateValue > endDateValue){
				var invalid:ValidationResultEvent = new ValidationResultEvent(ValidationResultEvent.INVALID);
    			var result:ValidationResult = new ValidationResult(true, "", "", "The start date cannot be later than the end date.");
    			invalid.results = [result];
        		dispatchEvent(invalid);
			}
			else{
				dispatchEvent(new ValidationResultEvent(ValidationResultEvent.VALID));
			}
		}

		public function searchBankReturnsWithSameCritera():void {
			searchBankReturns(true);
		}

		public function coreSearchBankReturns(useLastCriteria:Boolean = false):void {								
			searchBankReturns(useLastCriteria);
		}

		public function setupSearch():void {
			//Implement me!
			isSearching = true;
			errorString = DEFAULT_ERROR;			
		}
		
		virtual public function searchBankReturns(useLastCriteria:Boolean = false):void {
			//Override me with your search implementation .. if you don't, I will show a mean error
			errorString = "Search is not implemented.";
			isSearching = false;
		}

		protected function onSearchCompleted(e:ResultEvent):void {
			extendedInformationLoaded = false;
			var searchReturn:SearchResults = e.result as SearchResults;
			searchResults.totalRecords = searchReturn.totalRecords;
			if(searchReturn.returnsList.length == 0) extendedInformationLoaded = true;
			// this may need to be changed it does not dispatch a property change event
			searchResults.source = searchReturn.returnsList.source;												
			isSearching = false;						
		}

		protected function onSearchFaulted(error:FaultEvent):void {
			isSearching = false;
			errorString = error.message.toString();
		}
		
		private function getExtendedInfo(e:CollectionEvent):void {			
			var returnCollection:ArrayCollection = new ArrayCollection();
			for each(var bankReturn:BankReturn in searchResults){	
				if(bankReturn.bankReturnExtendedInfo == null){
					returnCollection.addItem(bankReturn);
				}			
			}
			// send last request
			if(returnCollection.length > 0){
				SAP.instance.bankReturnService.getBankReturnExtendedInfo(returnCollection, 
																		 new Responder(onExtendedInfoComplete, onExtendedInfoFaulted));
			}			
		}
		
		private function onExtendedInfoComplete(e:ResultEvent):void {
			var returnCollection:ArrayCollection = e.result as ArrayCollection;
					
			//Remove event listener prior to adding to it
			if(searchResults != null) 
				mSearchResults.removeEventListener(CollectionEvent.COLLECTION_CHANGE, getExtendedInfo);		
						
			for(var i:int = 0; i<searchResults.length; i++){	
				var searchResultsObject:BankReturn = searchResults.getItemAt(i) as BankReturn; 			
				if(searchResultsObject != null && searchResultsObject.bankReturnExtendedInfo == null){
					for(var j:int = 0; j<returnCollection.length; j++){
						var returnObject:BankReturn = returnCollection.getItemAt(j) as BankReturn;
						if(searchResultsObject.txnId == returnObject.txnId){							
							searchResultsObject.bankReturnExtendedInfo = returnObject.bankReturnExtendedInfo;
							returnCollection.removeItemAt(j);
							break;
						}
					}
				}
			}
		
			if(searchResults != null)
				mSearchResults.addEventListener(CollectionEvent.COLLECTION_CHANGE, getExtendedInfo, false, 0, true);

			searchResults.refresh();
			extendedInformationLoaded = true;			
		}				 
		
		protected function onExtendedInfoFaulted(error:FaultEvent):void {
			stopExtendedInfo = true;
			onSearchFaulted(error);
			extendedInformationLoaded = true;
		}				

		virtual public function getReportTypeCode():String {
			return "";
		}
	}
}
