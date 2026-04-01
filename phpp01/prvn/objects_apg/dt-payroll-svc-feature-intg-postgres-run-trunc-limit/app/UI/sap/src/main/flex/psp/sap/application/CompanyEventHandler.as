package psp.sap.application
{
    import flash.events.TextEvent;
    import flash.utils.Dictionary;

    import mx.utils.StringUtil;

    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.CompanyEventItem;
    import psp.sap.view.controls.CustomHtmlEventRenderers;
    import psp.sap.viewmodel.CompanyEventLogViewModel;
    import psp.sap.viewmodel.CompanyInspectorViewModel;

    public class CompanyEventHandler
	{
		
		private var inspector:CompanyInspectorViewModel;
		
		private var linkHandler:CompanyInspectorLinkHandler;
		
		private var customRenderers:Dictionary;	
		
		public function CompanyEventHandler(inspector:CompanyInspectorViewModel)
		{
			this.inspector = inspector;
			linkHandler = new  CompanyInspectorLinkHandler(inspector);
						
			customRenderers = new Dictionary(true);
			customRenderers["OldServiceStatus"] = CustomHtmlEventRenderers.oldServiceStatus;
			customRenderers["NewServiceStatus"] = CustomHtmlEventRenderers.newServiceStatus;
			customRenderers["FinancialTransactionId"] = CustomHtmlEventRenderers.financialTransactionId;			
			customRenderers["PayrollRunId"] = CustomHtmlEventRenderers.payrollRun;
			customRenderers["SourcePayrollRunId"] = CustomHtmlEventRenderers.sourcePayrollRun;
			customRenderers["StrikeReason"] = CustomHtmlEventRenderers.strikeReason;
            customRenderers["Amount"] = CustomHtmlEventRenderers.currency;
            customRenderers["text"] = CustomHtmlEventRenderers.text;
			customRenderers["date"] = CustomHtmlEventRenderers.date;
			customRenderers["phone"] = CustomHtmlEventRenderers.phone;

		}
		
		public function buildDescription(description:String, event:CompanyEventItem):String {						
			var pattern:RegExp = /{([^{]+)}/g;
			
			var indexedDescription:String = description.replace(pattern, groupToIndex);
			
			var details:Array = [];
			
			for each (var detail:String in description.match(pattern)) {
				var detailStripped:String = detail.replace(/[{}]/g,"");
				
				var extraArgs:String;
				
				var detailRendererOverride:String = null;
				if (detailStripped.indexOf(",") > 0) {
					var args:Array = detailStripped.split(",",3);
					detailStripped = StringUtil.trim(args[0]);
					detailRendererOverride = (args.length >= 1 ?  StringUtil.trim(args[1]) : null);					
					extraArgs = (args.length >= 2 ?  StringUtil.trim(args[2]) : null);
				} else {
                    args = [];
                    detailRendererOverride = null;
                    extraArgs = null;
                }
				
				var detailText:String;
				//populate if the detail is there OR a custom renderer (may be a "virtual" detail)
				if (detailStripped in event.details || detailRendererOverride != null || customRenderers[detailStripped] != null) {
					var f:Function;
					if (detailRendererOverride != null && customRenderers[detailRendererOverride] != null) {
						f = (customRenderers[detailRendererOverride] as Function);
						if (f.length == 2) {
							detailText = f.apply(this, [event, detailStripped]);
						} else {
							detailText = f.apply(this, [event, detailStripped, extraArgs]);
						}
					} else if (customRenderers[detailStripped] != null) {
						f = (customRenderers[detailStripped] as Function);
						if (f.length == 2) {
							detailText = f.apply(this, [event, detailStripped]);
						} else {
							detailText = f.apply(this, [event, detailStripped, extraArgs]);
						}	
					} else {
						detailText = event.details[detailStripped] != null ? event.details[detailStripped] : "[NULL]";
					}
				} else {
					detailText = detail;
				}
				details.push(detailText);
			}
			
			return StringUtil.substitute(indexedDescription, details);
		}
		
		private function groupToIndex(
							matchedSubstring:String,
							capturedMatch1:String,
							index:int,
							fullString:String):String {
			var i:int=0;
			var pos:int=-1;			
			while (true) {
				pos = fullString.indexOf("{", pos+1);
				if (pos == -1 || pos >= index) {
					break;
				}
				i++;				
			}					
			return "{" + i + "}";					
		}
		
		public function onLink(e:TextEvent):void {			
			var action:String = e.text.split("=",2)[0];
			var value:String = e.text.split("=",2)[1]; //reparse if you are passing more, but this should be 99% I think
			
			if (action == "editStatus") {
				inspector.getPage(CompanyInspectorPageEnum.SUBSCRIPTION_STATUS).activate();
			} else if (action == "goEvent") {
				inspector.getPage(CompanyInspectorPageEnum.EVENT_LOG).activatePage(CompanyEventLogViewModel.createActivator(value));
			} else if (action == "goFinTx") {
                linkHandler.goToPayrollTransaction(value);
			} else if (action == "goSourcePayrollRun") {
                linkHandler.goToSourcePayrollRun(value);
			} else if (action == "goPayrollRun") {
                linkHandler.goToPayrollRun(value);
			} else if (action == "goBanks") {
                linkHandler.goToBanks();
			}  else if (action == "resendEmail") {
                linkHandler.resendEmail(value);
            } else if(action == "sendEmailToMtl") {
				linkHandler.sendEmailToMtl(value);
			}

			
		}		

	}
}