package psp.sap.viewmodel
{
    import intuit.sbd.flex.framework.viewmodel.CollectionViewModel;

    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.events.PropertyChangeEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.StringUtil;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.CompareUtils;
    import psp.sap.model.DateRangeEnum;
    import psp.sap.model.PaymentForVerification;
    import psp.sap.model.PaymentTemplate;

    public class MmtViewModel extends AbstractPartViewModel
	{
        private const EMPTY_STRING:String = "";
		// members
		private var mPayments:CollectionViewModel = new CollectionViewModel(PaymentForVerification);

        [Bindable]
        [BackingProperty]
        public var selectedPayment:PaymentForVerification;

		private var mDateSelectionViewModel:DateSelectionViewModel;
        [Bindable]
        [BackingProperty]
        public var totalAmountFrom:String;
        [Bindable]
        [BackingProperty]
        public var totalAmountTo:String;
        [Bindable]
        [BackingProperty]
        public var relatedAmountFrom:String;
        [Bindable]
        [BackingProperty]
        public var relatedAmountTo:String;

        private var searchButtonClicked:Boolean = false;        

        [Bindable]
        [ArrayElementType("psp.sap.model.PaymentTemplate")]
        public var paymentTemplates:ArrayCollection = new ArrayCollection();

        [Bindable]
        [BackingProperty]
        public var selectedTemplate:PaymentTemplate = new PaymentTemplate();
        
		public function MmtViewModel()
        {
            this.label = CompanyInspectorPageEnum.MONEY_MOVEMENT;
            this.reloadOnActivate = false;

            mDateSelectionViewModel = new DateSelectionViewModel(this);
            mDateSelectionViewModel.defaultDateRange=DateRangeEnum.LAST_7_DAYS;

            dateSelectionViewModel.dateRange = dateSelectionViewModel.defaultDateRange;

        }

        [Bindable("propertyChange")]
		public function get payments():CollectionViewModel{
			return mPayments;
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
            if (paymentTemplates.length == 0) {
                SAP.instance.taxService.getSupportedPaymentTemplatesForCompany(company.sourceSystemCd, company.companyId, createLoadModelDataResponder(onPaymentTemplateLoadComplete));
            } else if (searchButtonClicked){
                SAP.instance.taxService.getMoneyMovementTransactionsForVerification(company.sourceSystemCd,
                                               company.companyId,
                                               dateSelectionViewModel.startDateValue,
                                               dateSelectionViewModel.endDateValue,
                                               totalAmountFrom, totalAmountTo, relatedAmountFrom, relatedAmountTo, selectedTemplate.paymentTemplateCd,
                                               createLoadModelDataResponder(onSearchCompleted));
            } else {
                modelDataLoaded();
            }

		}

		public function searchForPayments():void {
            payments.removeAll();
            selectedPayment = null;
            searchButtonClicked = true;
			refresh();
		}

        private function onPaymentTemplateLoadComplete(e:ResultEvent):void {
            paymentTemplates = e.result as ArrayCollection;
            
            /* Sort templates (IRS first, then alphabetical)    */
            var templateSort:Sort = new Sort();
            var templateSortField:SortField = new SortField("paymentTemplateCd");
            templateSortField.compareFunction = CompareUtils.comparePaymentTemplate;
            templateSort.fields = [templateSortField];
            paymentTemplates.sort = templateSort;
            paymentTemplates.refresh();
            var blankPaymentTemplate:PaymentTemplate = new PaymentTemplate();
            blankPaymentTemplate.paymentTemplateCd = "";
            blankPaymentTemplate.paymentTemplateName = "";
            paymentTemplates.addItemAt(blankPaymentTemplate, 0);

            selectedTemplate = paymentTemplates.getItemAt(0) as PaymentTemplate;
        }

        public function nextRecord():void {
			if (!mPayments.canSelectNext) {
				return;
            }

			mPayments.selectNext();
			selectedPayment = mPayments.selectedItem as PaymentForVerification;
		}

		public function previousRecord():void {
			if (!mPayments.canSelectPrevious) {
				return;
            }

			mPayments.selectPrevious();
			selectedPayment = mPayments.selectedItem as PaymentForVerification;
		}

		private function onSearchCompleted(e:ResultEvent):void {
			var paymentsList:ArrayCollection = e.result as ArrayCollection;

			mPayments = new CollectionViewModel(PaymentForVerification, paymentsList.toArray());

            if (mPayments.length > 0) {
				mPayments.selectedIndex = 0;
				selectedPayment = mPayments.selectedItem as PaymentForVerification;
			}
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "payments", null, null));
		}

        public function getFilterString():String {
            var filterString:String = EMPTY_STRING;
            filterString += dateSelectionViewModel.dateRange.toString() +" - Start: " + dateSelectionViewModel.startDate +" End: " + dateSelectionViewModel.endDate +", ";

            if (StringUtil.trim(totalAmountFrom).length > 0) {
                filterString += "Total Amount From: " + totalAmountFrom + ", ";
            }

            if (StringUtil.trim(totalAmountTo).length > 0) {
                filterString += "Total Amount To: " + totalAmountTo + ", ";
            }

            if (StringUtil.trim(relatedAmountFrom).length > 0) {
                filterString += "Related Amount From: " + relatedAmountFrom + ", ";
            }

            if (StringUtil.trim(relatedAmountTo).length > 0) {
                filterString += "Related Amount To: " + relatedAmountTo + ", ";
            }

            if (StringUtil.trim(selectedTemplate.paymentTemplateCd).length > 0) {
                filterString += "Payment Type: " + selectedTemplate.paymentTemplateCd + ", ";
            }

            filterString = filterString.substr(0, filterString.length - 2);
            return filterString;                        
        }
	}
}