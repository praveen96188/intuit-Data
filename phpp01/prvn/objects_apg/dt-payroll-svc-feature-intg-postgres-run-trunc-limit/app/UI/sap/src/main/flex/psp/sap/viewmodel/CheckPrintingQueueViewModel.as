package psp.sap.viewmodel {
    import mx.rpc.events.ResultEvent;
    import mx.utils.StringUtil;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.PaginationCollection;
    import psp.sap.application.enums.CheckPrintBatchStatusEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.application.enums.OperatorPageEnum;
    import psp.sap.model.CheckPrintingBatch;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.DateRangeEnum;
    import psp.sap.model.SearchResults;

    public class CheckPrintingQueueViewModel extends AbstractPartViewModel {
        private const EMPTY_STRING:String = "";

        private var mCheckDateSelectionViewModel:DateSelectionViewModel;
        private var mPrintDateSelectionViewModel:DateSelectionViewModel;
        private var mSelectedPrintBatch:CheckPrintingBatch;
        private var mNewBatchStatus:CheckPrintBatchStatusEnum;

        // last search criteria
        private var mLastEinPsid:String;
        private var mLastCheckStartDate:String;
        private var mLastCheckEndDate:String;
        private var mLastPrintStartDate:String;
        private var mLastPrintEndDate:String;
        private var mLastStatus:CheckPrintBatchStatusEnum;

        [Bindable] [ArrayElementType ("psp.sap.model.CheckPrintingBatch")]
        public var searchResults:PaginationCollection = new PaginationCollection();

        [Bindable] [BackingProperty (hasChanged=false)] public var einPsid:String;
        [Bindable] [BackingProperty (hasChanged=false)] public var filterStatus:CheckPrintBatchStatusEnum;

        [Bindable] public var canUpdateBatchStatus:Boolean = false;

        public function CheckPrintingQueueViewModel() {
            this.label = OperatorPageEnum.CHECK_PRINTING_QUEUE;
            this.reloadOnActivate = false;
            this.reloadOnSave = false;

            mCheckDateSelectionViewModel = new DateSelectionViewModel(this);
            mCheckDateSelectionViewModel.defaultDateRange=DateRangeEnum.CUSTOM;

            mPrintDateSelectionViewModel = new DateSelectionViewModel(this);
            mPrintDateSelectionViewModel.defaultDateRange=DateRangeEnum.TODAY;

            initDefaults();
        }

        [Bindable ("propertyChange")]
        public function get checkDateSelectionViewModel():DateSelectionViewModel {
            return mCheckDateSelectionViewModel;
        }

        [Bindable ("propertyChange")]
        public function get printDateSelectionViewModel():DateSelectionViewModel {
            return mPrintDateSelectionViewModel;
        }        

        override public function get hasChanged():Boolean {
            return true;
        }

        public function getFilterString():String {
            var filterString:String = EMPTY_STRING;
            if(StringUtil.trim(einPsid).length > 0) {
                filterString += "FEIN/PSID: " + einPsid + ", ";
            }

            filterString += "Status: " + filterStatus.label + ", ";

            if(StringUtil.trim(checkDateSelectionViewModel.startDate).length > 0) {
                filterString += "Paycheck Start: " + checkDateSelectionViewModel.startDate + ", ";
            }

            if(StringUtil.trim(checkDateSelectionViewModel.endDate).length > 0) {
                filterString += "Paycheck End: " + checkDateSelectionViewModel.endDate + ", ";
            }

            if(StringUtil.trim(printDateSelectionViewModel.startDate).length > 0) {
                filterString += "Print Start: " + printDateSelectionViewModel.startDate + ", ";
            }

            if(StringUtil.trim(printDateSelectionViewModel.endDate).length > 0) {
                filterString += "Print End: " + printDateSelectionViewModel.endDate + ", ";
            }

            filterString = filterString.substr(0, filterString.length - 2);
            return filterString;
        }

        public function search():void {
            searchResults.reset();

            // keep last search results so that we can use them for F9
            mLastEinPsid = einPsid;
            mLastCheckStartDate = checkDateSelectionViewModel.startDate;
            mLastCheckEndDate = checkDateSelectionViewModel.endDate;
            mLastPrintStartDate = printDateSelectionViewModel.startDate;
            mLastPrintEndDate = printDateSelectionViewModel.endDate;
            mLastStatus = filterStatus;

            refresh();
        }


        override protected function initializeBackingProperties():void {
            canUpdateBatchStatus = SAP.canPerformOperation(OperationsEnum.UPDATE_CHECK_PRINT_BATCH_STATUS);
        }

        override protected function loadModelData():void {
            // change inputs back to last search inputs for F9 refresh
            einPsid = mLastEinPsid;
            checkDateSelectionViewModel.startDate = mLastCheckStartDate;
            checkDateSelectionViewModel.endDate = mLastCheckEndDate;
            printDateSelectionViewModel.startDate = mLastPrintStartDate;
            printDateSelectionViewModel.endDate = mLastPrintEndDate;
            filterStatus = mLastStatus;
            SAP.instance.companyService.findCheckPrintingBatches(
                    (einPsid != null && einPsid.length > 0) ? einPsid : null,
                    (filterStatus != CheckPrintBatchStatusEnum.All) ? filterStatus.code : null,
                    checkDateSelectionViewModel.startDateValue,
                    checkDateSelectionViewModel.endDateValue,
                    printDateSelectionViewModel.startDateValue,
                    printDateSelectionViewModel.endDateValue,
                    searchResults.sortBy,
                    searchResults.sortDesc,
                    searchResults.startIndex,
                    searchResults.pageSize,
                    createLoadModelDataResponder(onSearchCompleted));
        }

        private function initDefaults():void {
            searchResults = new PaginationCollection();
            einPsid = EMPTY_STRING;
            filterStatus = CheckPrintBatchStatusEnum.All;

            mLastEinPsid = einPsid;
            mLastCheckStartDate = "";
            mLastCheckEndDate = "";
            mLastPrintStartDate = printDateSelectionViewModel.defaultDateRange.startDate;
            mLastPrintEndDate = printDateSelectionViewModel.defaultDateRange.endDate;
            mLastStatus = filterStatus;
        }

        protected function onSearchCompleted(e:ResultEvent):void {
            var searchReturn:SearchResults = e.result as SearchResults;
            searchResults.totalRecords = searchReturn.totalRecords;
            searchResults.source = searchReturn.returnsList.source;
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
