package psp.sap.viewmodel
{
    import intuit.sbd.flex.framework.application.collections.ArrayCollectionExt;

    import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.events.FlexEvent;
	import mx.formatters.NumberFormatter;
	import mx.rpc.events.ResultEvent;
	import mx.validators.NumberValidator;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.RiskInspectorPageEnum;
	import psp.sap.formatters.SAPCurrencyFormatter;
	import psp.sap.model.Company;
    import psp.sap.model.CompanyEventType;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.DateRangeEnum;
    import psp.sap.model.FraudEvent;
	import psp.sap.model.FraudIndicatorEnum;
	import psp.sap.viewmodel.events.EntityChangeEvent;	

	public class FraudSearchViewModel
	extends AbstractPartViewModel									
	{
		public static const FRAUD_FLAG_REMOVED_EVENT:String = "FraudFlagRemoved";		
		
		public var currencyFormatter:SAPCurrencyFormatter = new SAPCurrencyFormatter();
		
		// defaults
		private const DEFAULT_EIN:String = "";				
		
		// members		
		private var mPayrollAmount:String;
		private var mFraudIndicator:FraudIndicatorEnum;
        [ArrayElementType ("psp.sap.model.CompanyEventType")]
        private var mSelectedEventTypes:ArrayCollection = new ArrayCollection();
		[ArrayElementType ("psp.sap.model.FraudEvent")]								
		private var mSearchResults:ArrayCollection = new ArrayCollection();
		private var mEIN:String;
		private var mSort:Sort;
		private var mSelectedIndex:int;		

        private var mLastPayrollAmount:String;
        private var mLastEIN:String;
        private var mLastStartDate:String;
        private var mLastEndDate:String;
        private var mLastFraudIndicator:FraudIndicatorEnum;
		
		[Bindable] public var selectedFraudEvent:FraudEvent;
		[Bindable] public var selectedIndex:int;
		[Bindable] public var payrollAmountValidator:NumberValidator;
		
		// component
		private var mDateSelectionViewModel:DateSelectionViewModel;
		
		// formatter
		private var mNumberFormatter:NumberFormatter = new NumberFormatter();

		public function FraudSearchViewModel()
		{
			this.label = RiskInspectorPageEnum.FRAUD_SEARCH;
			this.reloadOnActivate = false;
			this.loadOnActivate = false;
			this.reloadOnSave = true;			
			
			// init component
			mDateSelectionViewModel = new DateSelectionViewModel(this);
			mDateSelectionViewModel.defaultDateRange = DateRangeEnum.LAST_2_DAYS;
            dateSelectionViewModel.dateRange = dateSelectionViewModel.defaultDateRange;

			// init formatter
			mNumberFormatter.useThousandsSeparator = false;
			mNumberFormatter.precision = 2;											    		

			// init validator									
			payrollAmountValidator = new NumberValidator();
			payrollAmountValidator.required = false;
			payrollAmountValidator.source = this;
			payrollAmountValidator.property = "payrollAmount"; 
			validators.push(payrollAmountValidator);
			
			// table sorting			
			mSort = new Sort();
		    mSort.fields = [new SortField("eventTimeStamp", false, true), new SortField("fraudIndicator", false, true)];

            initDefaults();
		}

		// getters and setters
		[Bindable]
		public function get ein():String {
			return mEIN;
		}

		public function set ein(value:String):void {
			// strip - and spaces
			mEIN = value.replace("-", "").replace(" ", "");
			updateCanSave();
		}						
		
		[Bindable]
		public function get payrollAmount():String {
			return mPayrollAmount;
		}

		public function set payrollAmount(value:String):void {
			mPayrollAmount = value;
			updateCanSave();
		}

        [Bindable]
        public function get selectedEventTypes():ArrayCollection {
            return mSelectedEventTypes;
        }

        public function set selectedEventTypes(value:ArrayCollection):void {
            mSelectedEventTypes = value;
            updateCanSave();
        }

        protected function get payrollAmountValue():Number {
			return mPayrollAmount != "" ? parseFloat(mNumberFormatter.format(mPayrollAmount)) : -1;
		}
		
		[Bindable]
		public function get fraudIndicator():FraudIndicatorEnum {
			return mFraudIndicator;
		}

		public function set fraudIndicator(value:FraudIndicatorEnum):void {
			if(value == null){
				value = FraudIndicatorEnum.ALL;
			}
			
			if(value != FraudIndicatorEnum.SIGN_UP){
				dateSelectionViewModel.startAndEndValidator.enabled = true;
				dateSelectionViewModel.startDateValidator.enabled = true;
				dateSelectionViewModel.endDateValidator.enabled = true;
				payrollAmountValidator.enabled = true;
			}
			else{
				dateSelectionViewModel.startAndEndValidator.enabled = false;
				dateSelectionViewModel.startDateValidator.enabled = false;
				dateSelectionViewModel.endDateValidator.enabled = false;
				payrollAmountValidator.enabled = false;
			}
			
			updateCanSave();			
			
			mFraudIndicator = value;
		}
				
		[Bindable ("propertyChange")]
		public function get dateSelectionViewModel():DateSelectionViewModel {
			return mDateSelectionViewModel;
		}
		
		[Bindable ("propertyChange")]
		public function get fraudIndicators():Array {
			return FraudIndicatorEnum.LIST;
		}		
		
		[ArrayElementType ("psp.sap.model.FraudEvent")]
		[Bindable]
		public function get searchResults():ArrayCollection {
			return mSearchResults;
		}

		public function set searchResults(value:ArrayCollection):void {
			if (value == null)
				value = new ArrayCollection();
			mSearchResults = value;
		}
						
		// overrides
		override protected function loadModelData():void {
            // change inputs back to last search inputs for F9 refresh
            payrollAmount = mLastPayrollAmount;
            ein = mLastEIN;
            dateSelectionViewModel.startDate = mLastStartDate;
            dateSelectionViewModel.endDate = mLastEndDate;
            fraudIndicator = mLastFraudIndicator;

            var eventTypeCodes:ArrayCollection=null;
            if (selectedEventTypes.length > 0) {
                eventTypeCodes = new ArrayCollection();
                for each (var selectedEvent:CompanyEventType in selectedEventTypes) {
                    eventTypeCodes.addItem(selectedEvent.eventTypeCode);
                }
            }

			SAP.instance.companyService.findCompanyFraudEvents(
                                   (ein != null && ein.length > 0) ? ein : null,
                                   (fraudIndicator != FraudIndicatorEnum.ALL) ? fraudIndicator.code : null,
                                   (fraudIndicator != FraudIndicatorEnum.SIGN_UP) ? payrollAmountValue : -1,
                                   (fraudIndicator != FraudIndicatorEnum.SIGN_UP) ? dateSelectionViewModel.startDateValue : null,
                                   (fraudIndicator != FraudIndicatorEnum.SIGN_UP) ? dateSelectionViewModel.endDateValue : null,
                                   eventTypeCodes,
                                   createLoadModelDataResponder(onSearchCompleted));                                   
		}

        private function initDefaults():void {
            ein = DEFAULT_EIN;
            mLastEIN = DEFAULT_EIN;
            mLastStartDate = dateSelectionViewModel.startDate;
            mLastEndDate = dateSelectionViewModel.endDate;                          
            payrollAmount = "";
            mLastPayrollAmount = "";
            fraudIndicator = FraudIndicatorEnum.ALL;
            mLastFraudIndicator = FraudIndicatorEnum.ALL;
        }
		
		override public function get hasChanged():Boolean {
			return true;
		}			

		public function searchFraud():void {
            // keep last search results so that we can use them for F9
            mLastPayrollAmount = payrollAmount;
            mLastEIN = ein;
            mLastStartDate = dateSelectionViewModel.startDate;
            mLastEndDate = dateSelectionViewModel.endDate;
            mLastFraudIndicator = fraudIndicator;

			refresh();						
		}

		private function onSearchCompleted(e:ResultEvent):void {
			selectedFraudEvent = null;
			searchResults = e.result as ArrayCollection;			
			mSearchResults.sort = mSort;
			mSearchResults.refresh();
		}
		
		public function goToCompanyInfo(fraudEvent:FraudEvent):void {
			mSelectedIndex = searchResults.getItemIndex(fraudEvent);
			
			var companyKey:CompanyKey = new CompanyKey(fraudEvent.sourceSystemCd, fraudEvent.companyId);
			
			companyKey.display();
		}		

		
		public function goToFraudCompany(fraudEvent:FraudEvent):void {
			selectedFraudEvent = fraudEvent;
			
			mSelectedIndex = searchResults.getItemIndex(fraudEvent);			
			
			(host.host as FraudViewModel).goToDetails();	
		}

		public function removeFraudFlag(sourceSystemCd:String, companyId:String):void {
			// show progress bar
			SAP.instance.showProgress("Removing Fraud Flag...");
			// reset error string
			saveMsg = "";
			SAP.instance.companyService.removeFraudFlag(sourceSystemCd, 
														  companyId,
														  createSaveResponder(onFraudFlagRemoved));
		}
		
		public function onFraudFlagRemoved(e:ResultEvent):void {
			dispatchEvent(new FlexEvent("FraudFlagRemoved"));

			SAP.instance.dispatchEvent(EntityChangeEvent.createEvent(	EntityChangeEvent.ENTITY_SAVED, 
																		EntityChangeEvent.COMPANY, 
																		CompanyKey.create(selectedFraudEvent).toString())); 
		}

        public function getFilterString():String {
			var newFilterString:String="";

            if (fraudIndicator != FraudIndicatorEnum.ALL) {
                newFilterString += fraudIndicator.code + "; ";
            }

            if (ein != "") {
                newFilterString += "EIN/CID: " + ein + "; ";
            }

            if (dateSelectionViewModel.startDate != "" && dateSelectionViewModel.endDate != "") {
				newFilterString += "From " + dateSelectionViewModel.startDate + " to " + dateSelectionViewModel.endDate + "; ";
			} else if (dateSelectionViewModel.startDate != "") {
				newFilterString += "After " + dateSelectionViewModel.startDate + "; ";
			} else if (dateSelectionViewModel.endDate != "") {
				newFilterString += "Before " + dateSelectionViewModel.endDate + "; ";
			} 

            if (payrollAmount != "") {
                newFilterString += "Payroll amount > " + payrollAmount + "; ";
            }

            if (selectedEventTypes.length > 0) {
                newFilterString += "With trigger ";
                for each (var eventType:CompanyEventType in selectedEventTypes) {
                    newFilterString += eventType.eventTypeName + ", ";
                }
                newFilterString = newFilterString.substr(0, newFilterString.length-2) + "; ";
            }

            if (newFilterString.length > 0) {
                return newFilterString.substr(0, newFilterString.length-2);
            } else {
                return "";
            }

		}
	}
}
