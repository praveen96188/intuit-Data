package psp.sap.viewmodel {
    import intuit.sbd.flex.framework.application.collections.ArrayCollectionExt;

    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectUtil;
    import mx.validators.NumberValidator;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.PaginationCollection;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.events.SAPEvent;
    import psp.sap.model.DataSyncDetail;
    import psp.sap.model.DataSyncDetailEmployee;
    import psp.sap.model.DataSyncDetailPaycheck;
    import psp.sap.model.DataSyncDetailPayrollTransaction;
    import psp.sap.model.DataSyncItems;
    import psp.sap.model.DataSyncSelectedItem;
    import psp.sap.model.SearchResults;
    import psp.sap.validators.SAPValidators;

    public class DataSyncSearchViewModel extends CompositePartViewModel {

        public static const SEARCH_REQUESTED_EVENT:String = "filteredSearchRequested";
        private const PAGE_SIZE:int = 100;

        private var hasNonNumericSourceEmpIds:Boolean = false;

        [Bindable]
        public var filterViewModel:SAPDataSyncSearchFilterViewModel;

        [Bindable]
        public var searchButtonClicked:Boolean = false;

        [Bindable]
        public var anyResults:Boolean = false;

        [Bindable]
        [ArrayElementType("psp.sap.model.DataSyncDetailPaycheck")]
        public var paycheckSearchResults:PaginationCollection = new PaginationCollection();

        [Bindable]
        [ArrayElementType("psp.sap.model.DataSyncDetailPayrollTransaction")]
        public var payrollTransactionSearchResults:PaginationCollection = new PaginationCollection();

        [Bindable]
        [ArrayElementType("psp.sap.model.DataSyncDetailEmployee")]
        public var employeeSearchResults:PaginationCollection = new PaginationCollection();

        [Bindable]
        [ArrayElementType("psp.sap.model.DataSyncDetailPayrollItem")]
        public var payrollItemSearchResults:PaginationCollection = new PaginationCollection();


        [Bindable]
        [ArrayElementType("psp.sap.model.DataSyncSelectedItem")]
        public var selectedItems:ArrayCollectionExt = new ArrayCollectionExt(DataSyncSelectedItem, new Array(), null, "dataSyncDetail.detailId");

        [Bindable]
        [BackingProperty]
        public var comment:String;


        [Bindable]
        public var commentRequiredValidator:Validator;

        [Bindable]
        public var actionSelectionRequiredValidator:Validator;

        [Bindable]
        public var selectedItemRequiredValidator:NumberValidator;

        [Bindable]
        public var undelete:Boolean

        [Bindable]
        public var caseId:String

        public function DataSyncSearchViewModel() {
            this.label = CompanyInspectorPageEnum.DATA_SYNC_SEARCH_VIEW;
            this.reloadOnActivate = false;
            this.reloadOnSave = true;

            commentRequiredValidator = SAPValidators.createRequiredFieldValidator(this, "comment", true);
            validators.push(commentRequiredValidator);

            actionSelectionRequiredValidator = SAPValidators.createRequiredFieldValidator(this, "selectedAction", true);
            validators.push(actionSelectionRequiredValidator);

            selectedItemRequiredValidator = SAPValidators.createNumberValidator(selectedItems, "length", true, 1, null);
            selectedItemRequiredValidator.triggerEvent = "collectionChange";
            validators.push(selectedItemRequiredValidator);

            filterViewModel = SAPDataSyncSearchFilterViewModel(addNewPart(SAPDataSyncSearchFilterViewModel));
            filterViewModel.addEventListener(SEARCH_REQUESTED_EVENT, onSearchRequested, false, 0, true);
            employeeSearchResults.addEventListener(CollectionEvent.COLLECTION_CHANGE, function (e:CollectionEvent):void {
                if(e.kind != CollectionEventKind.REFRESH) {
                    updatePaginationCollection(employeeSearchResults);
                }
            });
            paycheckSearchResults.addEventListener(CollectionEvent.COLLECTION_CHANGE, function (e:CollectionEvent):void {
                if(e.kind != CollectionEventKind.REFRESH) {
                    updatePaginationCollection(paycheckSearchResults);
                }

            });
            payrollTransactionSearchResults.addEventListener(CollectionEvent.COLLECTION_CHANGE, function (e:CollectionEvent):void {
                if(e.kind != CollectionEventKind.REFRESH) {
                    updatePaginationCollection(payrollTransactionSearchResults);
                }
            });
        }

        private var mSelectedAction:String;
        [Bindable]
        public function get selectedAction():String {
            return mSelectedAction;
        }
        public function set selectedAction(val:String):void {
            //workaround for RadioButtonGroup binding "null" instead of null
            if (val != "null") {
                mSelectedAction = val;
                if(mSelectedAction != "Push") {
                    undelete = false;
                }
            }
            updateCanSave();
        }

        private function onSearchRequested(event:SAPEvent):void {
            searchButtonClicked = true;
            selectedItems.removeAll();
            refresh();
        }


        override public function get hasChanged():Boolean {
            return true;
        }

        public function onGridChange(label:String):void {
            //this is hooked into the LMD sequence, but won't actually do anything extra
            loadData(label);
        }

        override protected function loadModelData():void {
            if (searchButtonClicked) {
                paycheckSearchResults.reset();
                payrollTransactionSearchResults.reset();
                employeeSearchResults.reset();
                payrollItemSearchResults.reset();

                anyResults = false;
                loadCount=5;
                SAP.instance.taxService.hasNonNumericSourceIds(companyKey.sourceSystemCd,
                        companyKey.companyId, createLoadModelDataResponder(function (e:ResultEvent): void {
                            hasNonNumericSourceEmpIds = Boolean(e.result);
                        }));
                for each (var label:String in ["Paychecks", "Payroll Transactions", "Employees", "Payroll Items"]) {
                    loadData(label);
                }
            } else {
                modelDataLoaded();

            }
        }

        private function getPaginationCollection(label:String):PaginationCollection {
            switch (label) {
                case "Paychecks":
                    return paycheckSearchResults;
                case "Payroll Transactions":
                    return payrollTransactionSearchResults;
                case "Employees":
                    return employeeSearchResults;
                case "Payroll Items":
                    return payrollItemSearchResults;
            }
            return null;
        }

        private function loadData(itemType:String):void {
            var paginationCollection:PaginationCollection  =  getPaginationCollection(itemType);
            SAP.instance.taxService.getDataSyncDetails(
                    companyKey.sourceSystemCd,
                    companyKey.companyId,
                    itemType,
                    filterViewModel.selectedSearchType != 0 ? null : filterViewModel.selectedIdSearchString.replace(/ /g, ""),
                    parseInt(filterViewModel.fromId),
                    parseInt(filterViewModel.toId),
                    filterViewModel.selectedSearchType != 1 ? null : filterViewModel.selectedTypeSearchString.replace(/ /g, ""),
                    filterViewModel.fromDateValue,
                    filterViewModel.toDateValue,
                    filterViewModel.checkNumber,
                    filterViewModel.amount,
                    filterViewModel.pItemName,
                    PAGE_SIZE,
                    paginationCollection.sortBy,
                    paginationCollection.sortDesc,
                    paginationCollection.startIndex,
                    createLoadModelDataResponder(function(e:ResultEvent):void {
                        paginationCollection.totalRecords = SearchResults(e.result).totalRecords;
                        paginationCollection.source = SearchResults(e.result).returnsList.source;
                        paginationCollection.pageSize = PAGE_SIZE;

                        if (paginationCollection.totalRecords > 0) {
                            anyResults = true;
                        }
                        for each (var detail:DataSyncDetail in paginationCollection) {
                            var selectedItem:DataSyncSelectedItem = DataSyncSelectedItem(selectedItems.getItemById(detail.detailId));
                            if (selectedItem == null) {
                                detail.selected = false;
                            } else {
                                detail.selected = true;
                                selectedItem.dataSyncDetail = detail;
                            }
                        }
                    }));
        }

        private function updatePaginationCollection(paginationCollection:PaginationCollection):void {
            if(paginationCollection.sortBy == 'employeeId' && !hasNonNumericSourceEmpIds) {
                paginationCollection.compareFunction = function compareNumericEmployeeId(itemA:Object, itemB:Object, fields:Array = null):int {
                    var compareResult:int;
                    if(itemA == null || itemB == null) {
                        return 0;
                    }
                    var aId:int;
                    var bId:int;
                    if(DataSyncDetailEmployee(itemA) != null){
                        aId = parseInt(DataSyncDetailEmployee(itemA).employeeId);
                        bId = parseInt(DataSyncDetailEmployee(itemB).employeeId);
                    } else if(DataSyncDetailPaycheck(itemA) != null) {
                        aId = parseInt(DataSyncDetailPaycheck(itemA).employeeId);
                        bId = parseInt(DataSyncDetailPaycheck(itemB).employeeId);
                    } else if(DataSyncDetailPayrollTransaction(itemA) != null) {
                        aId = parseInt(DataSyncDetailPayrollTransaction(itemA).employeeId);
                        bId = parseInt(DataSyncDetailPayrollTransaction(itemB).employeeId);
                    }
                    compareResult = ObjectUtil.numericCompare(aId, bId);

                    if(paginationCollection.sortDesc) {
                        return compareResult * -1;
                    }
                    return compareResult;
                };
            } else {
                paginationCollection.compareFunction = null;
            }
            paginationCollection.refresh();
        }

        override protected function initializeBackingProperties():void {
            undelete = false;
            updateCanSave();
            for each (var labelString:String in ["Paychecks", "Payroll Transactions", "Employees", "Payroll Items"]) {
                var paginationCollection:PaginationCollection  =  getPaginationCollection(labelString);
                updatePaginationCollection(paginationCollection);
                paginationCollection.refresh();
            }
        }

        /**
         *
         * @param itemToAdd item to be added to the selection
         */
        public function addItemToSelection(itemToAdd:DataSyncDetail):void {
            var selectedItem:DataSyncSelectedItem = itemToAdd.createNewDataSyncSelectedItem();
            itemToAdd.selected = true;
            if (!selectedItems.contains(selectedItem)) {
                selectedItems.addItemAt(selectedItem, 0);
            }
        }

        /**
         * Removes the item from selection (if present)
         * @param itemToRemove the item to be removed from selection
         */
        public function removeItemFromSelection(itemToRemove:DataSyncDetail):void {
            var selectedItem:DataSyncSelectedItem = selectedItems.getItemById(itemToRemove.detailId) as DataSyncSelectedItem;
            if (selectedItem != null) {
                removeSelectedItemFromSelection(selectedItem);
            }
        }

        public function removeSelectedItemFromSelection(itemToRemove:DataSyncSelectedItem):void {
            if (itemToRemove != null) {
                if (selectedItems.contains(itemToRemove)) {
                    selectedItems.removeItem(itemToRemove);
                }
                itemToRemove.dataSyncDetail.selected = false;
            }
        }


        override protected function executeSave():void {
            var dataSyncItems:DataSyncItems = new DataSyncItems();
            dataSyncItems.employeeIds = new ArrayCollection();
            dataSyncItems.payrollItemIds = new ArrayCollection();
            dataSyncItems.paychecks = new ArrayCollection();
            dataSyncItems.priorPayments = new ArrayCollection();
            dataSyncItems.liabilityAdjustments = new ArrayCollection();
            dataSyncItems.liabilityChecks = new ArrayCollection();
            dataSyncItems.qbdtPayrollTransactions = new ArrayCollection();

            for each (var selectedItem:DataSyncSelectedItem in selectedItems) {
                switch (selectedItem.itemType) {
                    case "Employee":
                        dataSyncItems.employeeIds.addItem(selectedItem.dataSyncDetail.detailId);
                        break;
                    case "Payroll Item":
                        dataSyncItems.payrollItemIds.addItem(selectedItem.dataSyncDetail.detailId);
                        break;
                    case "Paycheck":
                        dataSyncItems.paychecks.addItem(selectedItem.dataSyncDetail.detailId);
                        break;
                    case "Payroll Transaction":
                        if (DataSyncDetailPayrollTransaction(selectedItem.dataSyncDetail).isQBOnly) {
                            dataSyncItems.qbdtPayrollTransactions.addItem(selectedItem.dataSyncDetail.detailId);
                        } else {
                            switch (DataSyncDetailPayrollTransaction(selectedItem.dataSyncDetail).payrollTransactionType) {
                                case "Prior Payment":
                                case "Refund":
                                    dataSyncItems.priorPayments.addItem(selectedItem.dataSyncDetail.detailId);
                                    break;
                                case "Liability Adjustment":
                                    dataSyncItems.liabilityAdjustments.addItem(selectedItem.dataSyncDetail.detailId);
                                    break;
                                case "Liability Check":
                                    dataSyncItems.liabilityChecks.addItem(selectedItem.dataSyncDetail.detailId);
                                    break;
                            }
                        }
                        break;
                }
            }

            SAP.instance.taxService.updatedDataSyncTokensOnSelectedItems(companyKey.sourceSystemCd, companyKey.companyId, dataSyncItems, selectedAction, undelete, comment, caseId, createSaveResponder(onSaveSucceeded));
        }

        private function onSaveSucceeded(e:ResultEvent):void {
            selectedItems.removeAll();
            comment = "";
        }

    }
}
