package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.rpc.events.ResultEvent;
    import mx.utils.StringUtil;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.PaginationCollection;
    import psp.sap.application.enums.CheckPrintBatchStatusEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.application.enums.OperatorPageEnum;
    import psp.sap.model.CheckPrintingBatch;
    import psp.sap.model.CompareUtils;
    import psp.sap.model.DateRangeEnum;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.model.SearchResults;

    public class CheckPrintingAgencyChecksViewModel extends AbstractPartViewModel {

        private var mSelectedPrintBatch:CheckPrintingBatch;
        private var mNewBatchStatus:CheckPrintBatchStatusEnum;

        private var mLastSelectedPaymentTemplate:PaymentTemplate;
        private var mLastInitiationStartDate:String;
        private var mLastInitiationEndDate:String;
        private var mLastPrintStartDate:String;
        private var mLastPrintEndDate:String;
        private var mLastStatus:CheckPrintBatchStatusEnum;

        [Bindable]
        [ArrayElementType ("psp.sap.model.AgencyCheckBatch")]
        public var searchResults:PaginationCollection = new PaginationCollection();
        [Bindable]
        public var canUpdateBatchStatus:Boolean = false;

        [Bindable]
        public var initiationDateSelectionViewModel:DateSelectionViewModel;

        [Bindable]
        public var printDateSelectionViewModel:DateSelectionViewModel;


        [Bindable]
        [ArrayElementType("psp.sap.model.PaymentTemplate")]
        public var paymentTemplates:ArrayCollection = new ArrayCollection();

        [Bindable]
        [BackingProperty]
        public var selectedTemplate:PaymentTemplate = new PaymentTemplate();

        [Bindable]
        [BackingProperty (hasChanged=false)]
        public var filterStatus:CheckPrintBatchStatusEnum;

        public function CheckPrintingAgencyChecksViewModel() {
            super();

            this.label = OperatorPageEnum.CHECK_PRINTING_AGENCY_CHECKS;
            this.reloadOnActivate = true;
            this.reloadOnSave = true;

            initiationDateSelectionViewModel = new DateSelectionViewModel(this);
            initiationDateSelectionViewModel.defaultDateRange = DateRangeEnum.CUSTOM;
            initiationDateSelectionViewModel.startDate = "";
            initiationDateSelectionViewModel.endDate = "";

            printDateSelectionViewModel = new DateSelectionViewModel(this);
            printDateSelectionViewModel.defaultDateRange = DateRangeEnum.TODAY;
            printDateSelectionViewModel.startDate = "";
            printDateSelectionViewModel.endDate = "";

            initValues();
        }

        private function initValues():void {
            searchResults = new PaginationCollection();
            filterStatus = CheckPrintBatchStatusEnum.All;

            mLastSelectedPaymentTemplate = new PaymentTemplate();
            mLastInitiationStartDate = "";
            mLastInitiationEndDate = "";
            mLastPrintStartDate = printDateSelectionViewModel.defaultDateRange.startDate;
            mLastPrintEndDate = printDateSelectionViewModel.defaultDateRange.endDate;
            mLastStatus = filterStatus;
        }

        public function getFilterString():String {
            var filterString:String = "";
            if (selectedTemplate != null && StringUtil.trim(selectedTemplate.paymentTemplateCd).length > 0) {
                filterString += "Template: " + selectedTemplate.paymentTemplateName + ", ";
            }
            filterString += "Status: " + filterStatus.label + ", ";
            if (StringUtil.trim(initiationDateSelectionViewModel.startDate).length > 0) {
                filterString += "Initiation Start: " + initiationDateSelectionViewModel.startDate + ", ";
            }

            if (StringUtil.trim(initiationDateSelectionViewModel.endDate).length > 0) {
                filterString += "Initiation End: " + initiationDateSelectionViewModel.endDate + ", ";
            }

            if (StringUtil.trim(printDateSelectionViewModel.startDate).length > 0) {
                filterString += "Print Start: " + printDateSelectionViewModel.startDate + ", ";
            }

            if (StringUtil.trim(printDateSelectionViewModel.endDate).length > 0) {
                filterString += "Print End: " + printDateSelectionViewModel.endDate + ", ";
            }

            filterString = filterString.substr(0, filterString.length - 2);
            return filterString;
        }

        override protected function loadModelData():void {
            initiationDateSelectionViewModel.startDate = mLastInitiationStartDate;
            initiationDateSelectionViewModel.endDate = mLastInitiationEndDate;
            printDateSelectionViewModel.startDate = mLastPrintStartDate;
            printDateSelectionViewModel.endDate = mLastPrintEndDate;
            filterStatus = mLastStatus;
            if (paymentTemplates.length == 0) {
                loadCount++;
                SAP.instance.taxService.getSupportedPaymentTemplates(createLoadModelDataResponder(onPaymentTemplateLoadComplete));
            }

            SAP.instance.companyService.findAgencyPrintingBatches(
                    (selectedTemplate == null || selectedTemplate.paymentTemplateCd == null || selectedTemplate.paymentTemplateCd == "") ? null : selectedTemplate.paymentTemplateCd,
                    (filterStatus != CheckPrintBatchStatusEnum.All) ? filterStatus.code : null,
                    initiationDateSelectionViewModel.startDateValue,
                    initiationDateSelectionViewModel.endDateValue,
                    printDateSelectionViewModel.startDateValue,
                    printDateSelectionViewModel.endDateValue,
                    searchResults.sortBy,
                    searchResults.sortDesc,
                    searchResults.startIndex,
                    searchResults.pageSize,
                    createLoadModelDataResponder(onResultsLoadCompleted));
        }

        protected function onResultsLoadCompleted(e:ResultEvent):void {
            var searchReturn:SearchResults = e.result as SearchResults;
            searchResults.totalRecords = searchReturn.totalRecords;
            searchResults.source = searchReturn.returnsList.source;
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

        override public function get hasChanged():Boolean {
            return true;
        }

        public function search():void {
            searchResults.reset();

            mLastSelectedPaymentTemplate = selectedTemplate;
            mLastInitiationStartDate = initiationDateSelectionViewModel.startDate;
            mLastInitiationEndDate = initiationDateSelectionViewModel.endDate;
            mLastPrintStartDate = printDateSelectionViewModel.startDate;
            mLastPrintEndDate = printDateSelectionViewModel.endDate;
            mLastStatus = filterStatus;

            refresh();
        }

        override protected function initializeBackingProperties():void {
            canUpdateBatchStatus = SAP.canPerformOperation(OperationsEnum.UPDATE_CHECK_PRINT_BATCH_STATUS);
        }

        public function savePrintBatchStatus(batch:CheckPrintingBatch, newBatchStatus:CheckPrintBatchStatusEnum):void {
            mSelectedPrintBatch = batch;
            mNewBatchStatus = newBatchStatus;
            save();
        }

        override protected function executeSave():void {
            SAP.instance.companyService.savePrintBatchStatus(
                    mSelectedPrintBatch.printBatchId,
                    mNewBatchStatus.code,
                    createSaveResponder(onSaveRefresh, onSaveRefresh));
        }

        private final function onSaveRefresh(e:ResultEvent):void {
            refresh(false);
        }
    }
}
