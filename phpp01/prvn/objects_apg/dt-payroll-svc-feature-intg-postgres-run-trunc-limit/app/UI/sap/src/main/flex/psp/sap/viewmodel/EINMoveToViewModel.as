package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.EntitlementSearchResult;
    import psp.sap.validators.SAPValidators;

    public class EINMoveToViewModel extends AbstractPartViewModel {

        [Bindable] public var isMove:Boolean = true; //otherwise add

        [Bindable] [BackingProperty(context=true)] public var licenseNumber:String;
        [Bindable] [BackingProperty(context=true)] public var eoc:String;
        [Bindable] [BackingProperty(context=true)] public var itemNumber:String;

        [Bindable] [BackingProperty] public var ein:String;
        [Bindable] [BackingProperty] public var selectedEntitlementUnit:EntitlementSearchResult;
        [ArrayElementType("psp.sap.model.EntitlementSearchResult")]
        [Bindable] public var searchResults:ArrayCollection;

        public function EINMoveToViewModel() {
            super();
        }

        public static function createActivator(licenseNumber:String, eoc:String, itemNumber:String):Object {
            return {"licenseNumber":licenseNumber, "eoc":eoc, "itemNumber":itemNumber};
        }

        override protected function onActivated():void {
            selectedEntitlementUnit = null;
            searchResults = null;
            ein = "";
            updateCanSave();
        }

        override protected function loadModelData():void {
            if (canSave) {
                if (isMove) {
                    SAP.instance.companyService.findEntitlementUnits(ein.replace(/-/,""), createLoadModelDataResponder(onEntitlementUnitsResult));
                } else {
                    SAP.instance.companyService.findCompaniesByEIN(ein.replace(/-/,""), createLoadModelDataResponder(onEntitlementUnitsResult));
                }
            } else {
                modelDataLoaded();
            }
        }

        private function onEntitlementUnitsResult(e:ResultEvent):void {
            searchResults = ArrayCollection(e.result);
        }

        override protected function initializeBackingProperties():void {
            clearValidators();
            validators.push(SAPValidators.createEinValidator(this, "ein", true));
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        public function search():void {
            refresh();
        }

        override protected function executeSave():void {
            if (isMove) {
                SAP.instance.companyService.moveEntitlementUnit(selectedEntitlementUnit.id, licenseNumber, eoc, itemNumber, createSaveResponder());
            } else {
                SAP.instance.companyService.addEntitlementUnitToCompany(
                        selectedEntitlementUnit.key.sourceSystemCd,
                        selectedEntitlementUnit.key.companyId,
                        licenseNumber,
                        eoc,
                        itemNumber,
                        createSaveResponder());
            }
        }

        override public function cancel():void {
            super.cancel();
            deactivate();
        }

        override protected function onDeactivated():void {
             onActivated();
        }
    }
}