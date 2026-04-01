package psp.sap.viewmodel
{
	import flash.events.Event;
	
	import intuit.sbd.flex.framework.viewmodel.CollectionViewModel;
	
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.events.FlexEvent;
	import mx.events.PropertyChangeEvent;
	import mx.rpc.Responder;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.application.enums.FraudInspectorPageEnum;
	import psp.sap.model.Company;
	import psp.sap.model.CompanyKey;
	import psp.sap.model.FraudEvent;

	public class FraudDetailViewModel extends CompositePartViewModel
	{			
		public static const FRAUD_FLAG_REMOVED_EVENT:String = "FraudFlagRemoved";
		public static const ALL_FRAUD_FLAGS_REMOVED_EVENT:String = "AllFraudFlagsRemoved";
		
		// search results (from other tab) that can be iterated through in detail view 
		private var mFraudulentPayrolls:CollectionViewModel = new CollectionViewModel(FraudEvent);
							
		// the selected payroll fraud event for the set company 
		private var mSelectedPayrollFraudEvent:FraudEvent;
		
		// company fraudulent payrolls support
		private var mSelectedCompany:Company;
		private var mCompanyFraudulentPayrolls:ArrayCollection = new ArrayCollection();

		// fraudulent payroll transaction support 
		private var mShowPayrollTransactions:Boolean = false;
		private var mStatusDisplay:CompanyDisplaySubscriptionStatusViewModel;
		private var mStatusEdit:CompanyEditSubscriptionStatusViewModel;
		private var mPayrollListExpander:ExpanderViewModel;
		private var mPayrollListVm:PayrollTransactionsListViewModel;
		private var mSuspectPaycheckViewModel:FraudSuspectPaycheckViewModel;
        private var mEmployeeBankAccountMatchExpander:ExpanderViewModel;
		private var mEmployeeBankAccountMatchViewModel:FraudEmployeeBankAccountMatchViewModel;
		private var mContactViewModel:CompanyDisplayContactInfoViewModel;
        private var mLegalExpander:ExpanderViewModel;
        private var mSuspectPaycheckExpander:ExpanderViewModel;
        private var mContactExpander:ExpanderViewModel;

		public function FraudDetailViewModel()
		{
			super();
			
			this.reloadOnActivate = false;
			
			this.label = FraudInspectorPageEnum.FRAUD_DETAIL;

			mLegalExpander = addExpander(FraudInspectorPageEnum.COMPANY_LEGAL_INFO);
            mLegalExpander.expandOnActivate = false;
			var legalBox:CompositePartViewModel = mLegalExpander.addNewPart(CompositePartViewModel, FraudInspectorPageEnum.COMPANY_LEGAL_INFO + '_expander_box') as CompositePartViewModel;
			legalBox.addNewPart(CompanyEditLegalInfoViewModel, FraudInspectorPageEnum.COMPANY_LEGAL_INFO);
			var legalDEH:DisplayEditHistoryViewModel = legalBox.addDisplayEditHistory();
			mStatusDisplay = legalDEH.addDisplay(CompanyDisplaySubscriptionStatusViewModel) as CompanyDisplaySubscriptionStatusViewModel;
			mStatusEdit = legalDEH.addEdit(CompanyEditSubscriptionStatusViewModel) as CompanyEditSubscriptionStatusViewModel;
			
			mStatusDisplay.addEventListener(FRAUD_FLAG_REMOVED_EVENT, onFraudFlagRemoved, false, 0, true);

            mContactExpander = addExpander(CompanyInspectorPageEnum.COMPANY_CONTACT_INFO);
            mContactExpander.expandOnActivate = false;
			mContactViewModel = mContactExpander.addNewPart(CompanyDisplayContactInfoViewModel,CompanyInspectorPageEnum.COMPANY_CONTACT_INFO) as CompanyDisplayContactInfoViewModel;
			
			addExpander("companyFraudulentPayrolls");
			
			mSuspectPaycheckExpander = addExpander(FraudInspectorPageEnum.SUSPECT_PAYCHECKS);
			mSuspectPaycheckExpander.expandOnActivate = false;
            mSuspectPaycheckViewModel = mSuspectPaycheckExpander.addNewPart(FraudSuspectPaycheckViewModel, FraudInspectorPageEnum.SUSPECT_PAYCHECKS) as FraudSuspectPaycheckViewModel;
			
			mEmployeeBankAccountMatchExpander = addExpander(FraudInspectorPageEnum.MATCHING_EMPLOYEE_BANK_ACCOUNTS);
			mEmployeeBankAccountMatchExpander.expandOnActivate = false;
            mEmployeeBankAccountMatchViewModel = mEmployeeBankAccountMatchExpander.addNewPart(FraudEmployeeBankAccountMatchViewModel, FraudInspectorPageEnum.MATCHING_EMPLOYEE_BANK_ACCOUNTS) as FraudEmployeeBankAccountMatchViewModel;
			
			mPayrollListExpander = addExpander(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_LIST);
			//will defer this until visible so we can just have it loadmodeldata normally
			mPayrollListExpander.expandOnActivate = false;
			mPayrollListVm = mPayrollListExpander.addNewPart(PayrollTransactionsListViewModel, CompanyInspectorPageEnum.PAYROLL_TRANSACTION_LIST) as PayrollTransactionsListViewModel;
						
		}
		
		[Bindable("propertyChange")]
		public function get fraudulentPayrollsCollection():CollectionViewModel{
			return mFraudulentPayrolls;
		}
		
		public function set fraudSearchResults(value:ArrayCollection):void {
			if (value == null) {
				return;
            }

			fraudEvent = null;
								
			mFraudulentPayrolls = new CollectionViewModel(FraudEvent, value.source);
			mFraudulentPayrolls.filterFunction = function(item:FraudEvent):Boolean {
				return item.fraudIndicator == "Payroll";
			};
			mFraudulentPayrolls.refresh();
			
			var sort:Sort = new Sort();
		    sort.fields = [new SortField("eventTimeStamp", false, true), new SortField("fraudIndicator", false, true)];			
			mFraudulentPayrolls.sort = sort;			
			mFraudulentPayrolls.refresh();
			
			if (mFraudulentPayrolls.length > 0) {
                mLegalExpander.expandOnActivate = true;
                mContactExpander.expandOnActivate = true;
				mFraudulentPayrolls.selectedIndex = 0;
				fraudEvent = mFraudulentPayrolls.selectedItem as FraudEvent;
			}
            else{
                mLegalExpander.expandOnActivate = false;
                mContactExpander.expandOnActivate = false;
            }
			
			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "fraudulentPayrollsCollection", null, null));
		}
		
		private var mFraudEvent:FraudEvent = null;
		public function set fraudEvent(value:FraudEvent):void {
			if (value == null) {
				mFraudulentPayrolls.selectedIndex = -1;
				return;
			}

            mPayrollListVm.sourcePayrollRunId = value.sourcePayRunId;
            mPayrollListVm.payrollRun = null;

			// update position in record set
			mFraudulentPayrolls.selectedItem = value;
			mFraudEvent = value;
			
			// only reload company if this event is for a different company
			if (company != null && company.sourceSystemCd == value.sourceSystemCd && company.companyId == value.companyId) {
				return;
			}
			
			// load the company's data associated w/the event
			companyToLoad = createCompanyKey(mFraudEvent);
		}
			
		public function nextRecord():void {
			if (!mFraudulentPayrolls.canSelectNext) {
				return;
            }
				
			mFraudulentPayrolls.selectNext();
			fraudEvent = mFraudulentPayrolls.selectedItem as FraudEvent;
		}
		
		public function previousRecord():void {
			if (!mFraudulentPayrolls.canSelectPrevious) {
				return;
            }
			
			mFraudulentPayrolls.selectPrevious();	
			fraudEvent = mFraudulentPayrolls.selectedItem as FraudEvent;
		}
		
		public function moveToNextFraudEvent():Boolean {
			if (!mFraudulentPayrolls.canSelectNext) {
				return false;
            }

			mFraudulentPayrolls.selectNext();
			var nextEvent:FraudEvent = mFraudulentPayrolls.selectedItem as FraudEvent;
			if (nextEvent.fraudFlagSet) {
				fraudEvent = nextEvent;
				return true;
			}
			
			return moveToNextFraudEvent();
		}
		
		public function moveToPreviousFraudEvent():Boolean {
			if (!mFraudulentPayrolls.canSelectPrevious) {
				return false;
            }
			
			mFraudulentPayrolls.selectPrevious();
			var prevEvent:FraudEvent = mFraudulentPayrolls.selectedItem as FraudEvent;
			if (prevEvent.fraudFlagSet) {
				fraudEvent = prevEvent;
				return true;
			}

			return moveToPreviousFraudEvent();
		}
			
		private function createCompanyKey(fraudEvent:FraudEvent):Company {
			var companyKey:Company = new Company();
			companyKey.sourceSystemCd = fraudEvent.sourceSystemCd;
			companyKey.companyId = fraudEvent.companyId;
			return companyKey;			
		}
		
		public function set companyToLoad(value:Company):void {
			company = value;
			companyKey = CompanyKey.create(value);
			if (company != null) {
				showPayrollTransactions = false;
				refresh();
			}
		}
							
		[Bindable("propertyChange")]
		public function get companyLegalName():String {
			return (company != null ? company.legalName : "");		
		}
		
		[Bindable]
		public function get companyFraudulentPayrolls():ArrayCollection {
			return mCompanyFraudulentPayrolls;
		}

		public function set companyFraudulentPayrolls(value:ArrayCollection):void {
			if (value == null) {
				value = new ArrayCollection();
            }
			mCompanyFraudulentPayrolls = value;
		}
		


        [Bindable]
        public function get selectedPayrollFraudEvent():FraudEvent{
            return mSelectedPayrollFraudEvent;
        }

        public function set selectedPayrollFraudEvent(value:FraudEvent):void {
            var payrollRunChanged:Boolean = mSelectedPayrollFraudEvent == null || (value.sourcePayRunId != mSelectedPayrollFraudEvent.sourcePayRunId);
            
            mSelectedPayrollFraudEvent = value;
            mPayrollListVm.sourcePayrollRunId = value.sourcePayRunId;
            mPayrollListVm.paycheckDate = value.payrollCheckDate;
            mEmployeeBankAccountMatchViewModel.payrollRunId = value.sourcePayRunId;
            mSuspectPaycheckViewModel.payrollFraudEvent = value;
            if (showPayrollTransactions) {
            	//already showing so need to refresh
            	mPayrollListVm.refresh();
            } else {
            	showPayrollTransactions = true;
            }
        }
        
        [Bindable]
        public function get showPayrollTransactions():Boolean {
        	return mShowPayrollTransactions;
        }
        
        public function set showPayrollTransactions(value:Boolean):void {
        	mShowPayrollTransactions = value;
        	if (value) {
        		mPayrollListExpander.expand();
        	}
        }

		override protected function loadModelData():void {
			if (company != null) {	            
	            // load company	            	           
				SAP.instance.companyService.findCompany(company.sourceSystemCd, company.companyId, new Responder(onCompanyLoaded, onLoadModelDataFaulted));
            } else {
            	modelDataLoaded();
            }
		}

		private function onCompanyLoaded (e:ResultEvent):void {
			company = e.result as Company;
					
			SAP.instance.companyService.findCompanyFraudEvents(company.fein, "Payroll", -1, null, null, null, createLoadModelDataResponder(onFraudEventsLoaded));
		}
		
		private function onFraudEventsLoaded (e:ResultEvent):void {
			companyFraudulentPayrolls = e.result as ArrayCollection;			       
            
            // verify that the existing selected payroll run is still valid
            if (!companyFraudulentPayrolls.contains(selectedPayrollFraudEvent)) {
            	mSelectedPayrollFraudEvent = null;
            }            
                           			
		}

		override protected function initializeDefaults():void {
			mContactViewModel.selectedContactIndex = 0;
		}

		/**
		 * default behavior of CompositvePartViewModel causes parts to refresh simultaneously
		 */			
		override protected function onRefresh():void {
			// why was this a no-op before??
			super.onRefresh();
			if (showPayrollTransactions) {
				mPayrollListExpander.expand();
			}
		}

		public function canPerformOperation(operation:String):Boolean {
			return SAP.canPerformOperation(operation);
		}	
		
		public function goToCompanyInfoView():void {
			if (company != null) {
				company.display();
            }
		}
		
		public function onFraudFlagRemoved(e:Event):void {
			if (mFraudEvent != null) {
				mFraudEvent.fraudFlagSet = false;
				for each (var companyFraudEvent:FraudEvent in mFraudulentPayrolls) {
					if (mFraudEvent.sourceSystemCd == companyFraudEvent.sourceSystemCd && mFraudEvent.companyId == companyFraudEvent.companyId) {
						companyFraudEvent.fraudFlagSet = false;
					}
				}
			}

			dispatchEvent(new FlexEvent(FRAUD_FLAG_REMOVED_EVENT));

			if(!moveToNextFraudEvent() && !moveToPreviousFraudEvent()){
				dispatchEvent(new FlexEvent(ALL_FRAUD_FLAGS_REMOVED_EVENT));
            }
		}	
							
	}
}
