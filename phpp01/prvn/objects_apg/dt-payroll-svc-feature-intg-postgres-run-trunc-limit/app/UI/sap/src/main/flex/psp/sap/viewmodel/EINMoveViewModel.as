package psp.sap.viewmodel {
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.EntitlementInfo;
    import psp.sap.model.EntitlementSearchResult;
    import psp.sap.validators.SAPValidators;

    public class EINMoveViewModel extends AbstractPartViewModel {

        [Bindable] [BackingProperty(context=true)] public var from:EntitlementSearchResult;
        [Bindable] [BackingProperty] public var licenseNumber:String;
        [Bindable] [BackingProperty] public var eoc:String;

        [Bindable] public var entitlementInfo:EntitlementInfo=null;
        [Bindable] public var entitlementNotFound:Boolean = false;

        public static function createActivator(from:EntitlementSearchResult):Object {
            return {"from":from};
        }

        override protected function onActivated():void {
            entitlementInfo = null;
            entitlementNotFound = false;
            licenseNumber = "";
            eoc = "";
            updateCanSave();
        }

        override protected function loadModelData():void {
            if (canSave) {
                SAP.instance.companyService.getEntitlementInfo(licenseNumber, eoc, createLoadModelDataResponder(onEntitlementInfoLoaded));
            } else {
                modelDataLoaded();
            }
        }

        private function onEntitlementInfoLoaded(e:ResultEvent):void {
            entitlementInfo = EntitlementInfo(e.result);
            entitlementNotFound = entitlementInfo == null;
        }

        override protected function initializeBackingProperties():void {
            clearValidators();
            validators.push(SAPValidators.createRequiredFieldValidator(this, "licenseNumber", true));
            validators.push(SAPValidators.createRequiredFieldValidator(this, "eoc", true));
        }

        public function search():void {
            refresh();
        }

        override public function cancel():void {
            super.cancel();
            deactivate();
        }

        override protected function onDeactivated():void {
            onActivated();
        }

        override protected function executeSave():void {
            SAP.instance.companyService.moveEntitlementUnit(from.id, licenseNumber, eoc, entitlementInfo.assetItemNumber, createSaveResponder());
        }

    }
}
