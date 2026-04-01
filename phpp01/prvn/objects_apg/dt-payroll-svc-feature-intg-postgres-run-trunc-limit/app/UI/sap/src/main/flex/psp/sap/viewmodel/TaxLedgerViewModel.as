package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectUtil;
    import mx.validators.DateValidator;

    import psp.sap.application.CompanyInspectorLinkHandler;
    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.PaymentsPageEnum;
    import psp.sap.application.enums.SettingsEnum;
    import psp.sap.model.Agency;
    import psp.sap.model.CompareUtils;
    import psp.sap.model.LawItem;
    import psp.sap.model.LawTransactions;
    import psp.sap.model.LedgerItemDetailsCriterion;
    import psp.sap.model.Payment;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.model.TaxPaymentMethodEnum;
    import psp.sap.model.TaxTransaction;
    import psp.sap.validators.SAPStartEndDateValidator;
    import psp.sap.validators.SAPValidators;

    public class TaxLedgerViewModel extends CompositePartViewModel
	{
		private var mTaxTabSort:Sort;

        private var ledgerDetailPup:PopUpPartViewModel;
		private var ledgerDetailViewModel:TaxLedgerDetailViewModel;

        private var mStatusHistoryPopUp:PopUpPartViewModel;
        private var mPaymentsStatusHistoryViewModel:PaymentsStatusHistoryViewModel;
        private var mPayDateHistoryPopUp:PopUpPartViewModel;
        private var mPaymentsPayDateHistoryViewModel:PaymentsPayDateHistoryViewModel;
        private var mHoldsHistoryPopUp:PopUpPartViewModel;
        private var mPaymentsHoldsHistoryViewModel:PaymentsHoldsHistoryViewModel;

        private var hasSearched:Boolean = false;
		
		[Bindable]
		public var quarters:ArrayCollection = new ArrayCollection(["", "Q1", "Q2", "Q3", "Q4"]);
		[Bindable]
		public var transactionTypes:ArrayCollection = new ArrayCollection(["", "Payment", "Payroll", "Adjustment", "Take on Return", "Refund"]);
		[Bindable]
        public var paymentMethods:ArrayCollection = new ArrayCollection([TaxPaymentMethodEnum.BLANK, TaxPaymentMethodEnum.EFTPS, TaxPaymentMethodEnum.EFTPS_DIRECT_DEBIT, TaxPaymentMethodEnum.ACH, TaxPaymentMethodEnum.CHECK, TaxPaymentMethodEnum.EDI, TaxPaymentMethodEnum.RECORDED]);

        //noinspection JSUnusedGlobalSymbols
        [Bindable] [BackingProperty] public var includeNotPostedPayments:Boolean = true;

        [ArrayElementType("psp.sap.viewmodel.TaxLedgerTabViewModel")]
        private var mTaxLedgerTabViewModelCollection:ArrayCollection;

		public function TaxLedgerViewModel()
		{				
			super();			
			reloadOnActivate = false;
			bindSaveMessageWithChildren = true;
			reloadOnSave = false;

            ledgerDetailPup = addPopUpPart(CompanyInspectorPageEnum.TAX_LEDGER_DETAIL);
			ledgerDetailViewModel = ledgerDetailPup.addNewPart(TaxLedgerDetailViewModel, CompanyInspectorPageEnum.TAX_LEDGER_DETAIL) as TaxLedgerDetailViewModel;

            mStatusHistoryPopUp = addPopUpPart(PaymentsPageEnum.STATUS_POPUP);
            mPaymentsStatusHistoryViewModel = mStatusHistoryPopUp.addNewPart(PaymentsStatusHistoryViewModel, PaymentsPageEnum.STATUS_POPUP) as PaymentsStatusHistoryViewModel;

            mPayDateHistoryPopUp = addPopUpPart(PaymentsPageEnum.PAYDATE_POPUP);
            mPaymentsPayDateHistoryViewModel = mPayDateHistoryPopUp.addNewPart(PaymentsPayDateHistoryViewModel, PaymentsPageEnum.PAYDATE_POPUP) as PaymentsPayDateHistoryViewModel;

            mHoldsHistoryPopUp = addPopUpPart(CompanyInspectorPageEnum.TAX_LEDGER_HOLDS_POPUP);
            mPaymentsHoldsHistoryViewModel = mHoldsHistoryPopUp.addNewPart(PaymentsHoldsHistoryViewModel, CompanyInspectorPageEnum.TAX_LEDGER_HOLDS_POPUP) as PaymentsHoldsHistoryViewModel;


			mTaxTabSort = new Sort();
			var sortField:SortField = new SortField("", false, false);
			sortField.compareFunction = compareTaxTabs;
			mTaxTabSort.fields = [sortField];

            mTaxLedgerTabViewModelCollection = new ArrayCollection();
		}


        override protected function onActivating():void {
            selectedPaymentMethod = TaxPaymentMethodEnum.BLANK;
        }

        private function compareTaxTabs(a:Object, b:Object):int {
			var aLawTransactions:LawTransactions = LawTransactions(a);
            var bLawTransactions:LawTransactions = LawTransactions(b);

            if(aLawTransactions.label().indexOf("Summary") > -1){
				return 1;
			}
			if(bLawTransactions.label().indexOf("Summary") > -1){
				return -1;
			}
			return ObjectUtil.stringCompare(aLawTransactions.label(), bLawTransactions.label());
		}
		
		public function getFilterString():String {
			var filterString:String = "";
			if(selectedYear != ""){
				filterString += selectedYear + ", ";
			}
			if(selectedQuarter != null && selectedQuarter != ""){
				filterString += selectedQuarter + ", ";
			}
			if(selectedTransactionDescription != ""){
				filterString += selectedTransactionDescription + ", ";
			}
			if(selectedAgency.agencyAbbrev != ""){
				filterString += selectedAgency.agencyAbbrev + ", ";
			}
			if(selectedPaymentTemplate != null && selectedPaymentTemplate.paymentTemplateName != ""){
				filterString += selectedPaymentTemplate.paymentTemplateName + ", ";
			}
			if(selectedLawItem != null && selectedLawItem.name != ""){
				filterString += selectedLawItem.name + ", ";
			}
			if(selectedPaymentMethod.code != null){
				filterString += selectedPaymentMethod.label + ", ";
			}
			
			if(filterString == ""){
				filterString = "No Filters Selected";
			}
			else{
				filterString = filterString.slice(0, filterString.length - 2);
			}
			return filterString;
		}

        [ArrayElementType("psp.sap.model.Agency")]
		[Bindable] public var agencyList:ArrayCollection;
		

		
		private var mSelectedAgency:Agency;
		[Bindable]
        [BackingProperty]
		public function get selectedAgency():Agency {
			return mSelectedAgency;
		}
		
		public function set selectedAgency(value:Agency):void {
			mSelectedAgency = value;
			
			if(mSelectedAgency != null && mSelectedAgency.paymentTemplates != null){								
				paymentTemplates = mSelectedAgency.paymentTemplates;
			}
			else {
				paymentTemplates = new ArrayCollection();
			}			
		}
		
		private var mPaymentTemplates:ArrayCollection;
		[Bindable]
		public function get paymentTemplates():ArrayCollection {
			return mPaymentTemplates;
		}
		
		public function set paymentTemplates(value:ArrayCollection):void {
			mPaymentTemplates = value;			
			lawItems = new ArrayCollection();
			selectedLawItem = null;
		}
		
		private var mSelectedPaymentTemplate:PaymentTemplate;
		[BackingProperty]
        [Bindable]
		public function get selectedPaymentTemplate():PaymentTemplate {
			return mSelectedPaymentTemplate;
		}
		
		public function set selectedPaymentTemplate(value:PaymentTemplate):void {
			mSelectedPaymentTemplate = value;
			
			if(mSelectedPaymentTemplate != null && mSelectedPaymentTemplate.lawItems != null){
				var tempLawItems:ArrayCollection = new ArrayCollection();
				var blankLawItem:LawItem = new LawItem();
				blankLawItem.name =  "";
				tempLawItems.addItem(blankLawItem);
				for each(var lawItem:LawItem in mSelectedPaymentTemplate.lawItems){
					tempLawItems.addItem(lawItem);
				}
				lawItems = tempLawItems;
			}
			else {
				lawItems = new ArrayCollection();
			}
			selectedLawItem = null;

            if (value == null || value.supportStartDate == null) {
                taxYears.filterFunction = null;
            } else {
                taxYears.filterFunction = function(year:String):Boolean {
                    if (SAP.instance.session.user.getPreferenceBoolean(SettingsEnum.INCLUDE_POSSIBLE_BACKDATE_YEARS)) {
                        return parseInt(year) >= value.supportStartDate.fullYearUTC;
                    } else {
                        return parseInt(year) >= value.processingStartDate.fullYearUTC;
                    }
                };
            }
            var taxYearsSort:Sort = new Sort();
            taxYearsSort.fields = [new SortField(null, false, true, true)];
            taxYears.sort = taxYearsSort;

            taxYears.refresh();
            if (! taxYears.contains(selectedYear) && taxYears.length > 0) {
                selectedYear = String(taxYears.getItemAt(0));
            }
		}

        [ArrayElementType("psp.sap.model.LawItem")]
		[Bindable] public var lawItems:ArrayCollection;
		[Bindable] [BackingProperty] public var selectedLawItem:LawItem;

		[ArrayElementType("String")]
		[Bindable] public var taxYears:ArrayCollection;

		private var mSelectedYear:String;
		[Bindable]
		public function get selectedYear():String {
			return mSelectedYear;
		}
		
		public function set selectedYear(value:String):void {
			mSelectedYear = value;
			updateYearQuarterStartEndDates();
		}
		
		private var mSelectedQuarter:String;
		[Bindable]
		public function get selectedQuarter():String {
			return mSelectedQuarter;
		}
		
		public function set selectedQuarter(value:String):void {
			mSelectedQuarter = value;
			updateYearQuarterStartEndDates();
		}

        [Bindable] public var selectedTransactionDescription:String="";

		private var mSelectedPaymentMethod:TaxPaymentMethodEnum;
		[Bindable]
		public function get selectedPaymentMethod():TaxPaymentMethodEnum {
			return mSelectedPaymentMethod;
		}
		
		public function set selectedPaymentMethod(value:TaxPaymentMethodEnum):void {
			if(value == null){
				value = TaxPaymentMethodEnum.BLANK;
			}
			mSelectedPaymentMethod = value;
		}


		[Bindable] [BackingProperty] public var yearQuarterDateStart:String;
		
		private function get yearQuarterDateStartValue():Date { 
			return new Date(yearQuarterDateStart);
		}
		
		[Bindable] [BackingProperty] public var yearQuarterDateEnd:String;
		
		private function get yearQuarterDateEndValue():Date { 
			return new Date(yearQuarterDateEnd);
		}

        [ArrayElementType("psp.sap.model.LawTransactions")]
		[Bindable] public var taxTransactions:ArrayCollection;

		override public function get hasChanged():Boolean {
			return true;
		}				
		
		private function updateYearQuarterStartEndDates():void {											 
			var startDate:String = "";
			var endDate:String = "";
			
			if(selectedQuarter != null && selectedQuarter != ''){
				if(selectedQuarter == "Q1"){
					startDate += "01/01/" + selectedYear;
					endDate += "03/31/" + selectedYear;
				}
				else if(selectedQuarter == "Q2"){
					startDate += "04/01/" + selectedYear;
					endDate += "06/30/" + selectedYear;
				}
				else if(selectedQuarter == "Q3"){
					startDate += "07/01/" + selectedYear;
					endDate += "09/30/" + selectedYear;
				}
				else if(selectedQuarter == "Q4"){
					startDate += "10/01/" + selectedYear;
					endDate += "12/31/" + selectedYear;
				}
			}
			else if(selectedYear != null && selectedYear != ''){
				startDate += "01/01/" + selectedYear;
				endDate += "12/31/" + selectedYear;
			}
						
			yearQuarterDateStart = startDate;
			yearQuarterDateEnd = endDate;											
		}				
		
		override protected function loadModelData():void {			
			if (! hasSearched) {
                loadCount = 2;
                SAP.instance.taxService.findCompanyTaxYears(company.sourceSystemCd, company.companyId, createLoadModelDataResponder(onTaxYearsLoaded));
                SAP.instance.taxService.findCompanyAgencies(company.sourceSystemCd, company.companyId, createLoadModelDataResponder(onCompanyAgenciesLoaded));
            } else {
                loadCount = 1;
                taxTransactions = new ArrayCollection();
                SAP.instance.taxService.findTaxTransactions(company.sourceSystemCd,
                        company.companyId,
                        selectedTransactionDescription,
                        selectedAgency.agencyId,
                        selectedPaymentTemplate != null ? selectedPaymentTemplate.paymentTemplateCd : null,
                        selectedLawItem != null ? selectedLawItem.lawId : null,
                        selectedPaymentMethod.code,
                        yearQuarterDateStartValue,
                        yearQuarterDateEndValue,
                        includeNotPostedPayments,
                        createLoadModelDataResponder(onTaxTransactionsReturn));
            }
		}

        [Bindable ("propertyChange")]
        public function get taxLedgerTabViewModelCollection():ArrayCollection {
            return mTaxLedgerTabViewModelCollection;
        }

        private function onTaxYearsLoaded(e:ResultEvent):void {
            taxYears = ArrayCollection(e.result);
        }

        private function onCompanyAgenciesLoaded(e:ResultEvent):void {
            //add blank values to allow selecting none
            var newAgencies:ArrayCollection = ArrayCollection(e.result);
            var blankAgency:Agency = new Agency();
            blankAgency.agencyAbbrev="";
            blankAgency.agencyId="";
            blankAgency.paymentTemplates = new ArrayCollection();
            newAgencies.addItem(blankAgency);

            for each (var agency:Agency in newAgencies) {
                var blankPaymentTemplate:PaymentTemplate = new PaymentTemplate();
                blankPaymentTemplate.paymentTemplateName = "";
                blankPaymentTemplate.lawItems = new ArrayCollection();
                agency.paymentTemplates.addItem(blankPaymentTemplate);
            }

            agencyList = applyAgencySort(newAgencies);
        }
		
        public function applyAgencySort(agencyList:ArrayCollection):ArrayCollection {
			if(agencyList != null){
				// agency sort (blank first, IRS second, then alphabetical
				var agencySort:Sort = new Sort();
				var agencySortField:SortField = new SortField("agencyName");
				agencySortField.compareFunction = CompareUtils.compareAgencies;
				agencySort.fields = [agencySortField];

				// payment template sort (IRS first then alphabetical)
				var paymentTemplateSort:Sort = new Sort();
				var paymentTemplateSortField:SortField = new SortField("paymentTemplateName");
				paymentTemplateSortField.compareFunction = CompareUtils.comparePaymentTemplate;
				paymentTemplateSort.fields = [paymentTemplateSortField];

				// law item sort
				var lawItemSort:Sort = new Sort();
				lawItemSort.fields = [new SortField("name")];

				for each(var agency:Agency in agencyList){
					for each(var paymentTemplate:PaymentTemplate in agency.paymentTemplates){
						paymentTemplate.lawItems.sort = lawItemSort;
						paymentTemplate.lawItems.refresh();
					}
					agency.paymentTemplates.sort = paymentTemplateSort;
					agency.paymentTemplates.refresh();
				}
				agencyList.sort = agencySort;
				agencyList.refresh();
			}
            return agencyList;
		}

        override protected function initializeBackingProperties():void {
            if (!hasSearched) {
                if (agencyList.length > 1) {
                    selectedAgency = Agency(agencyList.getItemAt(1));
                }
                if (selectedAgency != null && selectedAgency.paymentTemplates.length > 1) {
                    selectedPaymentTemplate = PaymentTemplate(selectedAgency.paymentTemplates.getItemAt(1));
                }
            }
        }

		public function findTaxTransactions():void {			
			hasSearched = true;
            refresh();
		}		
		
		public function onTaxTransactionsReturn(e:ResultEvent):void {
			var temp:ArrayCollection = e.result as ArrayCollection; 
			temp.sort = mTaxTabSort;
			temp.refresh();
			taxTransactions = temp;
			mTaxLedgerTabViewModelCollection.removeAll();
			for each(var lawTransaction:LawTransactions in taxTransactions){
				lawTransaction.taxTransactions.sort = taxTransactionSort;
				lawTransaction.taxTransactions.refresh();
                var taxLedgerTabViewModel:TaxLedgerTabViewModel = new TaxLedgerTabViewModel();
                taxLedgerTabViewModel.lawTransactions = lawTransaction;
                taxLedgerTabViewModel.taxLedgerViewModel = this;
                taxLedgerTabViewModelCollection.addItem(taxLedgerTabViewModel);
			}
            taxLedgerTabViewModelCollection.refresh();
		}

        public function setDefaultSort(dataProvider:ArrayCollection):void {
            dataProvider.sort = taxTransactionSort;
            dataProvider.refresh();
        }

        private function get taxTransactionSort():Sort {
            var sort:Sort = new Sort();
            sort.fields = [new SortField("quarter", false, false, true), new SortField("checkPaymentDate", false, false)];
            return sort;
        }


        public function showPayrollTransactions(taxTransaction:TaxTransaction):void {
            new CompanyInspectorLinkHandler(CompanyInspectorViewModel(inspector)).goToPayrollRun(taxTransaction.payrollRunId);
        }

        public function viewStatusHistory(data:TaxTransaction):void {
            mPaymentsStatusHistoryViewModel.payment = getPaymentFromTaxTransaction(data);
            mStatusHistoryPopUp.displayPopUp();
        }

        public function viewPayDateHistory(data:TaxTransaction):void {
            mPaymentsPayDateHistoryViewModel.payment = getPaymentFromTaxTransaction(data);
            mPayDateHistoryPopUp.displayPopUp();
        }

        public function viewHoldsHistory(data:TaxTransaction):void {
            mPaymentsHoldsHistoryViewModel.payment = getPaymentFromTaxTransaction(data);
            mHoldsHistoryPopUp.displayPopUp();
        }

        private function getPaymentFromTaxTransaction(taxTransaction:TaxTransaction):Payment {
            var payment:Payment = new Payment();
            payment.paymentId = taxTransaction.moneyMovementTransactionId;
            payment.companyKey = companyKey;
            return payment;
        }
		
		public function showDetail(payment:TaxTransaction, isQTD:Boolean, isYTD:Boolean):void {

            ledgerDetailViewModel.ledgerItemDetailsCriterion = new LedgerItemDetailsCriterion();
            ledgerDetailViewModel.ledgerItemDetailsCriterion.sourceSystemCd = company.sourceSystemCd;
            ledgerDetailViewModel.ledgerItemDetailsCriterion.companyId = company.companyId;
			ledgerDetailViewModel.ledgerItemDetailsCriterion.payrollDate = payment.checkPaymentDate;
			ledgerDetailViewModel.ledgerItemDetailsCriterion.payrollRunId = payment.payrollRunId;
			ledgerDetailViewModel.ledgerItemDetailsCriterion.voidId = payment.voidId;
			ledgerDetailViewModel.ledgerItemDetailsCriterion.templateCd = payment.templateCd;
			ledgerDetailViewModel.ledgerItemDetailsCriterion.lawId = payment.lawId;
			ledgerDetailViewModel.ledgerItemDetailsCriterion.isQTD = isQTD;
			ledgerDetailViewModel.ledgerItemDetailsCriterion.isYTD = isYTD;
			ledgerDetailViewModel.ledgerItemDetailsCriterion.includeNotPostedPayments = includeNotPostedPayments;

            ledgerDetailPup.displayPopUp();
		}


    }
}
