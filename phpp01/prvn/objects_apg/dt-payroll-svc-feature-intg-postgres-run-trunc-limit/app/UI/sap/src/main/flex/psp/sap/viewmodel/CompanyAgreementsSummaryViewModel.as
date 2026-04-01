package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.EINManagementInspectorPageEnum;
    import psp.sap.application.enums.ExplorerEnum;
    import psp.sap.model.EntitlementUnit;

    public class CompanyAgreementsSummaryViewModel extends AbstractPartViewModel
	{
        private var mEntitlementUnitId:String;

        [ArrayElementType("psp.sap.model.EntitlementUnit")]
        [Bindable] public var entitlementUnits:ArrayCollection = new ArrayCollection();


        public function CompanyAgreementsSummaryViewModel() {
            reloadOnSave = true;
        }

        override protected function loadModelData():void {
            SAP.instance.companyService.getEntitlementUnits(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onEntitlementUnitsLoaded))
        }

        private function onEntitlementUnitsLoaded(e:ResultEvent):void {
            entitlementUnits = ArrayCollection(e.result);
        }

        public function viewMoreEINs(entitlementUnit:EntitlementUnit):void {
            var inspector:AbstractInspectorViewModel = SAP.instance.explorers.getExplorer(ExplorerEnum.EIN_MANAGEMENT).inspectors.getInspectorAt(0); 
            inspector.findPage(EINManagementInspectorPageEnum.EINS).
                    activatePage(EINSearchViewModel.createActivator(entitlementUnit.entitlement.licenseNumber, entitlementUnit.entitlement.eoc, entitlementUnit.entitlement.customerId, entitlementUnit.entitlement.entitlementCodeInfo.assetItemNumber));
        }

        override protected function executeSave():void {
            SAP.instance.companyService.syncEntitlementUnit(mEntitlementUnitId, createSaveResponder());
        }

        override protected function get savingMessage():String {
            return "Syncing...";
        }

        public function syncEntitlementUnit(entitlementUnit:EntitlementUnit):void {
            mEntitlementUnitId = entitlementUnit.id;
            forceSave();
        }

        public function updateSubscriptionEndDate(entitlementUnit:EntitlementUnit):void {
            mEntitlementUnitId = entitlementUnit.id;
            SAP.instance.companyService.updateSubscriptionEndDate(mEntitlementUnitId, createSaveResponder());
        }

        public function canPerformOperation(operation:String):Boolean {
            return SAP.canPerformOperation(operation);
        }

	}
}