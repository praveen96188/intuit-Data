package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.EINManagementInspectorPageEnum;
    import psp.sap.application.enums.ExplorerEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.application.enums.AssistedSubTypeEnum;
    import psp.sap.model.AssetInfo;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.CompanyServiceState;
    import psp.sap.model.EntitlementInfo;
    import psp.sap.model.EntitlementSearchResult;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class EINSearchViewModel extends CompositePartViewModel {

        public static const ACTION_COPY:String = "Copy to new EIN";
        public static const ACTION_DEACTIVATE:String = "Deactivate";
        public static const ACTION_REACTIVATE:String = "Reactivate";
        public static const ACTION_MOVE:String = "Move";        

        [Bindable] [BackingProperty(context=true, required=false)] public var licenseNumber:String;
        [Bindable] [BackingProperty(context=true, required=false)] public var eoc:String;
        [Bindable] [BackingProperty(context=true)] public var serviceAccountId:String;
        [Bindable] [BackingProperty(context=true)] public var itemNumber:String;

        [Bindable] [BackingProperty] public var orderNumber:String;

        private var saveMode:String;
        private var selectedEntitlement:EntitlementSearchResult;

        [Bindable] public var usingOrderNumberLookup:Boolean = false;

        [Bindable] public var assetInfo:AssetInfo;

        [ArrayElementType("psp.sap.model.EntitlementSearchResult")]
        [Bindable] public var searchResults:ArrayCollection = null;

        [Bindable] public var canAddEIN:Boolean;
        [Bindable] public var canMoveTo:Boolean;
        [Bindable] public var canAddTo:Boolean;
        [Bindable] public var hasMaxActiveEINLimitExceeded:Boolean;

        [Bindable] public var mayAddEIN:Boolean;
        [Bindable] public var mayMoveTo:Boolean;
        [Bindable] public var mayAddTo:Boolean;



        private var movePup:PopUpPartViewModel;
        private var moveVM:EINMoveViewModel;

        private var moveToPup:PopUpPartViewModel;
        private var moveToVM:EINMoveToViewModel;

        private var addToPup:PopUpPartViewModel;
        private var addToVM:EINMoveToViewModel;

        public function EINSearchViewModel() {
            super();

            reloadOnSave = true;

            movePup = addPopUpPart(EINManagementInspectorPageEnum.MOVE_EIN);
			moveVM = movePup.addNewPart(EINMoveViewModel, EINManagementInspectorPageEnum.MOVE_EIN) as EINMoveViewModel;						
            movePup.closeOnSave = true;
            moveVM.addEventListener(ViewModelEvent.SAVE_SUCCEEDED, function(e:Event):void { refresh(); }, false, 0, false);

            moveToPup = addPopUpPart(EINManagementInspectorPageEnum.MOVE_TO);
			moveToVM = moveToPup.addNewPart(EINMoveToViewModel, EINManagementInspectorPageEnum.MOVE_TO) as EINMoveToViewModel;
            moveToPup.closeOnSave = true;
            moveToVM.addEventListener(ViewModelEvent.SAVE_SUCCEEDED, function(e:Event):void { refresh(); }, false, 0, false);

            addToPup = addPopUpPart(EINManagementInspectorPageEnum.ADD_TO);
			addToVM = addToPup.addNewPart(EINMoveToViewModel, EINManagementInspectorPageEnum.ADD_TO) as EINMoveToViewModel;
            addToVM.isMove = false;
            addToPup.closeOnSave = true;
            addToVM.addEventListener(ViewModelEvent.SAVE_SUCCEEDED, function(e:Event):void { refresh(); }, false, 0, false);
        }

        public static function createActivator(licenseNumber:String, eoc:String, serviceAccountId:String, itemNumber:String):Object {
            return {"licenseNumber":licenseNumber, "eoc":eoc, "serviceAccountId":serviceAccountId, "itemNumber":itemNumber};
        }

        override protected function loadModelData():void {
            if (licenseNumberEocLoaded) {
                saveFaulted = false;
                saveMsg = "";
                loadCount=3;
                SAP.instance.companyService.getAssetInfo(itemNumber, createLoadModelDataResponder(onAssetInfoLoaded));
                SAP.instance.companyService.findCurrentEINs(licenseNumber, eoc, createLoadModelDataResponder(onSearchResultsLoaded, onSearchResultsFaulted));
                SAP.instance.companyService.hasActiveEINsForLicenseExceededMaxAllowed(licenseNumber,eoc , createLoadModelDataResponder(onHasActiveEINsForLicenseExceededMaxAllowedLoaded));
            } else {
                usingOrderNumberLookup = true; //if ever come in without lic/eoc, will always show search box
                modelDataLoaded();
            }            
        }

        [Bindable(event="backingPropertyChanged")]
        public function get licenseNumberEocLoaded():Boolean {
            return licenseNumber != null && licenseNumber != "" && eoc != null && eoc != "";
        }

        private function onAssetInfoLoaded(e:ResultEvent):void {
            assetInfo = AssetInfo(e.result);
        }

        private function onSearchResultsLoaded(e:ResultEvent):void {
            this.searchResults = ArrayCollection(e.result);
        }

        private function onSearchResultsFaulted(e:FaultEvent):void {
            this.searchResults = null;
        }

        private function onHasActiveEINsForLicenseExceededMaxAllowedLoaded(e:ResultEvent):void {
            this.hasMaxActiveEINLimitExceeded = e.result;
        }

        public function findByOrderNumber():void {
            SAP.instance.companyService.getLicenseFromOrderNumber(orderNumber, new Responder(onLicenseResult, onLoadModelDataFaulted));
        }

        public function onLicenseResult(e:ResultEvent):void {
            var license:EntitlementInfo = EntitlementInfo(e.result);
            licenseNumber = license.licenseNumber;
            eoc = license.eoc;
            refresh();
        }

        override protected function initializeBackingProperties():void {
            if (assetInfo != null) {
                canAddEIN = true;
                for each (var searchResult:EntitlementSearchResult in searchResults) {
                    //can add unless active assisted present...
                    if (searchResult.assetInfo.assisted && searchResult.assetInfo.assistedSubType != AssistedSubTypeEnum.DIAMOND && searchResult.isActivated ) {
                        canAddEIN = false;
                    }
                }

                canAddTo = canAddEIN || !assetInfo.primary;
                canMoveTo = canAddTo;

                mayAddEIN = assetInfo.assisted ? SAP.canPerformOperation(OperationsEnum.ADD_ASSISTED_EIN) : SAP.canPerformOperation(OperationsEnum.ADD_DIY_EIN);
                mayAddTo = assetInfo.assisted ? SAP.canPerformOperation(OperationsEnum.ADD_TO_ASSISTED_EIN) : SAP.canPerformOperation(OperationsEnum.ADD_TO_DIY_EIN);
                mayMoveTo = assetInfo.assisted ? SAP.canPerformOperation(OperationsEnum.MOVE_EIN_DIY_ASSISTED) : SAP.canPerformOperation(OperationsEnum.MOVE_EIN_DIY_DIY);

            } else {
                canAddEIN = false;
                canAddTo = false;
                canMoveTo = false;
            }

            addActions(searchResults);

        }

        private function addActions(searchResults:ArrayCollection):void {            
            for each (var searchResult:EntitlementSearchResult in searchResults) {
                searchResult.actionCollection.removeAll();
                if (canAddEIN && mayAddEIN) {
                    searchResult.actionCollection.addItem(ACTION_COPY);
                }
                if (searchResult.isActivated && mayDeactivate(searchResult)) {
                    searchResult.actionCollection.addItem(ACTION_DEACTIVATE);
                }
                if (searchResult.isDeactivated && canAddTo && mayReactivate()) {
                    searchResult.actionCollection.addItem(ACTION_REACTIVATE);
                }
                if (mayMove() && !searchResult.isHistoric) {
                    searchResult.actionCollection.addItem(ACTION_MOVE);
                }
            }
        }

        private function mayDeactivate(searchResult:EntitlementSearchResult):Boolean {
            if (searchResult.companyServiceState == CompanyServiceState.AssistedActive) {
                return SAP.canPerformOperation(OperationsEnum.DEACTIVATE_EIN_ACTIVE);
            } else if (searchResult.companyServiceState == CompanyServiceState.AssistedPending) {
                return SAP.canPerformOperation(OperationsEnum.DEACTIVATE_EIN_PENDING);
            } else {
                return SAP.canPerformOperation(OperationsEnum.DEACTIVATE_EIN);
            }
        }

        private function mayReactivate():Boolean {
            if (assetInfo.assisted) {
                return SAP.canPerformOperation(OperationsEnum.REACTIVATE_EIN_ASSISTED);
            } else {
                return SAP.canPerformOperation(OperationsEnum.REACTIVATE_EIN_DIY);
            }
        }

        private function mayMove():Boolean {
            if (assetInfo.assisted) {
                return SAP.canPerformOperation(OperationsEnum.MOVE_EIN_DIY_ASSISTED);
            } else {
                return SAP.canPerformOperation(OperationsEnum.MOVE_EIN_DIY_DIY);
            }
        }

        override protected function onActivated():void {
            EINManagementExplorerViewModel(SAP.instance.explorers.getExplorer(ExplorerEnum.EIN_MANAGEMENT)).displayInMenu();            
        }

        public function addCompany():void {
            topic.findPage(EINManagementInspectorPageEnum.ADD_EIN).activatePage(EINAddCompanyViewModel.createActivator(licenseNumber, eoc, itemNumber, serviceAccountId, assetInfo.assisted, null));
        }

        public function moveCompanyHere():void {
            moveToVM.setActivator(EINMoveToViewModel.createActivator(licenseNumber, eoc, itemNumber));
            moveToPup.displayPopUp();            
        }

        public function addEntitlementUnit():void {
            addToVM.setActivator(EINMoveToViewModel.createActivator(licenseNumber, eoc, itemNumber));
            addToPup.displayPopUp();                        
        }

        public function addCompanyFrom(entitlement:EntitlementSearchResult):void {
            var copyFrom:CompanyKey = new CompanyKey(SourceSystemEnum.QBDT.code, entitlement.PSID);
            topic.findPage(EINManagementInspectorPageEnum.ADD_EIN).activatePage(EINAddCompanyViewModel.createActivator(licenseNumber, eoc, itemNumber, serviceAccountId, assetInfo.assisted, copyFrom));
        }

        public function deactivateEntitlementUnit(entitlement:EntitlementSearchResult):void {
            selectedEntitlement = entitlement;
            saveMode = "deactivate";            
            forceSave();
        }

        public function reactivateEntitlementUnit(entitlement:EntitlementSearchResult):void {
            selectedEntitlement = entitlement;
            saveMode = "reactivate";
            forceSave();
        }

        public function moveEntitlementUnit(entitlement:EntitlementSearchResult):void {
            moveVM.setActivator(EINMoveViewModel.createActivator(entitlement));
            movePup.displayPopUp();
        }

        override protected function executeSave():void {
            if (saveMode == "deactivate") {
                SAP.instance.companyService.deactivateEntitlementUnit(selectedEntitlement.id, createSaveResponder());
            } else {
                SAP.instance.companyService.reactivateEntitlementUnit(selectedEntitlement.id, createSaveResponder());
            }
        }


    }    
}