package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.formatters.DateFormatter;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	
	public class CompanySubscriptionStatusHistoryViewModel
		extends CompositePartViewModel
	{
        [Bindable] [ArrayElementType ("psp.sap.model.ServicePropertyAudit")] public var companyLimitHistory:ArrayCollection = new ArrayCollection();
		[Bindable] [ArrayElementType ("psp.sap.model.PropertyAudit")] public var employeeLimitHistory:ArrayCollection = new ArrayCollection();
        [Bindable] [ArrayElementType ("psp.sap.model.PropertyAudit")] public var payeeLimitHistory:ArrayCollection = new ArrayCollection();
        [Bindable] [ArrayElementType ("psp.sap.model.PropertyAudit")] public var w2History:ArrayCollection = new ArrayCollection();
        [Bindable] [ArrayElementType ("psp.sap.model.ServiceStatusHistoryItem")] public var serviceStatusHistories:ArrayCollection = new ArrayCollection();
		
		public function CompanySubscriptionStatusHistoryViewModel()
		{
			this.label = CompanyInspectorPageEnum.SUBSCRIPTION_STATUS_HISTORY;

            addExpander("serviceStatusHistory");
            addExpander("perPayrollHistory");
			addExpander("perEmployeeHistory");
            addExpander("perPayeeHistory");
            addExpander("w2History");
		}
		
		override protected function loadModelData():void {
			serviceStatusHistories.removeAll();
            companyLimitHistory.removeAll();
			employeeLimitHistory.removeAll();
            payeeLimitHistory.removeAll();
            w2History.removeAll();

            loadCount = 4;

            SAP.instance.companyService.findCompanyServiceStatusHistory(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    null,
                    createLoadModelDataResponder(onServiceStatusHistoryItemsLoaded));
            SAP.instance.propertyAuditService.getCompanyDDLimitHistory(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    null,
                    createLoadModelDataResponder(onCompanyDataLoadCompleted));
            SAP.instance.propertyAuditService.getEmployeeDDLimitHistory(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    null,
                    createLoadModelDataResponder(onEmployeeDataLoadCompleted));
            SAP.instance.propertyAuditService.getPayeeDDLimitHistory(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    null,
                    createLoadModelDataResponder(onPayeeDataLoadCompleted));
            SAP.instance.propertyAuditService.getW2PrintingPreferenceHistory(
                    companyKey.sourceSystemCd,
                    companyKey.companyId,
                    createLoadModelDataResponder(onW2PrintingPreferenceLoaded));
        }

		protected function onCompanyDataLoadCompleted(e:ResultEvent):void {
			companyLimitHistory = e.result as ArrayCollection;
		}

		protected function onEmployeeDataLoadCompleted(e:ResultEvent):void {
			employeeLimitHistory = e.result as ArrayCollection;
		}

        protected function onPayeeDataLoadCompleted(e:ResultEvent):void {
			payeeLimitHistory = e.result as ArrayCollection;
		}

		public function onServiceStatusHistoryItemsLoaded(e:ResultEvent):void {
			serviceStatusHistories = e.result as ArrayCollection;			
		}

        public function onW2PrintingPreferenceLoaded(e:ResultEvent):void {
            w2History = ArrayCollection(e.result);
        }

	}
}