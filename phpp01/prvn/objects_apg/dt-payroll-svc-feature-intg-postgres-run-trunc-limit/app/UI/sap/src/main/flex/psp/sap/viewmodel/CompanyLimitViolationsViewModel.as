package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.formatters.DateFormatter;
	import mx.rpc.Responder;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.model.CompanyEventItem;
    import psp.sap.model.ServiceCodeEnum;
    import psp.sap.model.companyevents.CompanyEventDetail;
    import psp.sap.viewmodel.CompanyInspectorViewModel;
	import psp.sap.viewmodel.CompanyPropertyAuditHistory;
	
	public class CompanyLimitViolationsViewModel
		extends CompanyPropertyAuditHistory
	{		
		
		[Bindable] [ArrayElementType ("psp.sap.model.CompanyEventItem")]
		public var payrollViolations:ArrayCollection = new ArrayCollection();
		[Bindable] [ArrayElementType ("psp.sap.model.CompanyEventItem")]
		public var employeeViolations:ArrayCollection = new ArrayCollection();
        [Bindable] [ArrayElementType ("psp.sap.model.CompanyEventItem")]
        public var payeeViolations:ArrayCollection = new ArrayCollection();
		
		public function CompanyLimitViolationsViewModel()
		{
			super();
			
			this.label = CompanyInspectorPageEnum.LIMIT_VIOLATIONS_HISTORY;
			
			// this page is unique; a user viewing any company page can jump to this page
			// via the banner and the crumb page history should not be cleared			
		}

		override protected function loadModelData():void {
			SAP.instance.companyService.getLimitViolationEvents(
											company.companyId, company.sourceSystemCd,
											null,
											null,
											createLoadModelDataResponder(onDataLoadCompleted));
		}
		
		override protected function onDataLoadCompleted(e:ResultEvent):void {
			super.onDataLoadCompleted(e);
			
			payrollViolations.removeAll();
			employeeViolations.removeAll();
            payeeViolations.removeAll();
						
			for each (var event:CompanyEventItem in propertyHistory) {
                if(event.details.LimitType != null) {
                    if(event.details.LimitType == "Company") {
                        if(event.details.ServiceCode == null) {
                            event.details.ServiceCode = ServiceCodeEnum.DIRECT_DEPOSIT.code;
                        }
                        // this this is associated to the overall payroll
                        payrollViolations.addItem(event);
                    }
                    else if(event.details.LimitType == "Payee") {
                        payeeViolations.addItem(event);
                    }
                    else {
                        //Bank and Employee are counted as employee violations
                        employeeViolations.addItem(event);
                    }
                }
			}
		}
		
	}
}
