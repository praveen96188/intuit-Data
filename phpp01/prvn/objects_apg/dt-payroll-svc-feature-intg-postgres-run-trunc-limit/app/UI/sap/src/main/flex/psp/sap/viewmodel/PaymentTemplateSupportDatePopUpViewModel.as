package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;

    public class PaymentTemplateSupportDatePopUpViewModel extends AbstractPartViewModel {
        public function PaymentTemplateSupportDatePopUpViewModel() {
            super();
            this.label = CompanyInspectorPageEnum.PSP_PAYMENT_TEMPLATE_SUPPORT_DATES;
            this.reloadOnSave = true;
        }

        private var mSupportedAgencies:ArrayCollection = new ArrayCollection();
        private var mAgencyShowSelection:Boolean = false;

        [Bindable]
        public function get supportedAgencies():ArrayCollection {
            return mSupportedAgencies;
        }

        public function set supportedAgencies(pSupportedAgencies:ArrayCollection):void {
            if (pSupportedAgencies == null) {
                pSupportedAgencies = new ArrayCollection();
            }
            mSupportedAgencies = pSupportedAgencies;
        }

        [Bindable]
        [BackingProperty]
        public function set agencyShowSelection(pAgencyShowSelection:Object):void {
            mAgencyShowSelection = pAgencyShowSelection;
            refresh();
        }

        public function get agencyShowSelection():Object {
            return mAgencyShowSelection;
        }

        override protected function loadModelData():void {
            SAP.instance.taxService.getCompanyAgencyTemplates(null, null, agencyShowSelection, createLoadModelDataResponder(onSearchCompleted));
        }

        private function onSearchCompleted(event:ResultEvent):void {
            supportedAgencies = event.result as ArrayCollection;
        }

        override public function get hasChanged():Boolean {
            return true;
        }
    }
}
