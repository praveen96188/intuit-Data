package psp.sap.viewmodel {
    import mx.logging.ILogger;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.ClientLoggingTarget;
    import psp.sap.application.SAP;
    import psp.sap.model.UsageBillingDetail;
    import psp.sap.model.UsageBillingInvoice;

    public class BillingDetailsViewModel extends CompositePartViewModel {
        private var logger:ILogger = ClientLoggingTarget.getLogger(this);
        [Bindable]
        public var billingDetail:UsageBillingDetail = new UsageBillingDetail();
        [Bindable]
        public var hasBillingEmployeeDetails:Boolean = false;
        [Bindable]
        public var isMultiEin:Boolean = false;
        [Bindable]
        public var billingHistoryInfo:UsageBillingInvoice = null;
        [Bindable]
        public var isViewAll:Boolean = false;
        [Bindable]
        public var showViewAllLink:Boolean = true;
        [Bindable]
        public var showViewLessLink:Boolean = false;
        [Bindable]
        public var showExportLink:Boolean = true;

        public function BillingDetailsViewModel() {
        }


        override protected function loadModelData():void {
            SAP.instance.billingHistoryService.findBillingDetails(
                    this.company.companyId,
                    this.company.sourceSystemCd,
                    this.billingHistoryInfo.statementDate,
                    isViewAll,
                    createLoadModelDataResponder(onBillingDetailsResults));
        }

        override protected function initializeBackingProperties():void {
            hasBillingEmployeeDetails = billingDetail != null && billingDetail.employeeDetails != null && billingDetail.employeeDetails.length > 0;
            isMultiEin =  ( billingDetail != null && billingDetail.isMultiEin == true);
            showViewAllLink = isMultiEin && !isViewAll;
            showViewLessLink =   isMultiEin && isViewAll;
            showExportLink = hasBillingEmployeeDetails;
        }

        public function onBillingDetailsResults(e:ResultEvent):void {
            billingDetail = UsageBillingDetail(e.result) as UsageBillingDetail;
            isDataLoading = false;
        }

        public function displayViewMore():void {
            isViewAll = true;
            refresh();
        }

        public function displayViewLess():void {
            isViewAll = false;
            refresh();
        }

    }
}
