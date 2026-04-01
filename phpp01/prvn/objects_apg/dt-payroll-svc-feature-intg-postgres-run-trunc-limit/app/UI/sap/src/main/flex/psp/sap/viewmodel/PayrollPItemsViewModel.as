package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.PItem;
    import psp.sap.model.PItemSet;
    import psp.sap.viewmodel.SinglePartPageViewModel;

    public class PayrollPItemsViewModel extends AbstractPartViewModel {
        [Bindable]
        public var pitemSet:PItemSet;
        [Bindable]
        public var pitems:ArrayCollection;
        [Bindable]
        public var hasCompanyPayrollItems:Boolean;

        public function PayrollPItemsViewModel() {
            super();
        }

        override protected function loadModelData():void {
            SAP.instance.payrollRunService.findPItems(
                    this.company.companyId,
                    this.company.sourceSystemCd,
                    createLoadModelDataResponder(onPItemsResults));
        }

        public function onPItemsResults(e:ResultEvent):void {
            pitemSet = e.result as PItemSet;
            pitems = pitemSet.companyPayrollItems;
            hasCompanyPayrollItems = (pitems.length > 0);
        }

        public function viewTaxability(pitem:PItem):void {
            topic.findPage(CompanyInspectorPageEnum.PAYROLLS_PITEM_TAXABILITY).activatePage(PayrollPItemsTaxabilityViewModel.createActivator(pitem,pitemSet.companyLaws));
        }
    }
}