package psp.sap.view.controls
{
	import mx.formatters.DateFormatter;
	import mx.formatters.PhoneFormatter;
	
	import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.formatters.SAPCurrencyFormatter;
    import psp.sap.model.CompanyEventItem;
	import psp.sap.model.companyevents.CompanyEventDetail;
	
	public class CustomHtmlEventRenderers
	{
					
		public static function newServiceStatus(event:CompanyEventItem, detail:String):String {
			return serviceStatus(event, detail, "NewOnHoldReason", true);				 
		}
		
		public static function oldServiceStatus(event:CompanyEventItem, detail:String):String {
			return serviceStatus(event, detail, "OldOnHoldReason", false);
		}

		private static function serviceStatus(event:CompanyEventItem, detail:String, onHoldReasonCode:String, edit:Boolean):String {
			if (! (detail in event.details) && ! (onHoldReasonCode in event.details)) {
                //this means there was no from
                return "No Status";
            } else if ((detail in event.details) && event.details[detail] != "On Hold") {
				if (edit) {
					return "<a href='event:editStatus'>"+getActualStatus(event.details[detail])+"</a>";
				} else {
					return getActualStatus(event.details[detail]);
				}
			} else {
				var onHoldReasons:String;
				if (edit) {
					onHoldReasons = "<a href='event:editStatus'>On Hold</a>";					
				} else {
					onHoldReasons = "On Hold";
				}
				onHoldReasons += " (";
				for each (var onHoldDetail:CompanyEventDetail in event.mCompanyEventDetails) {
					if (onHoldDetail.eventDetailTypeCd == onHoldReasonCode) {
						onHoldReasons += onHoldDetail.value + ", ";
					} 
				}
				return onHoldReasons.substring(0, onHoldReasons.length-2)+ ")";	
			}			
		}
		
		private static function getActualStatus(status:String):String {			
			return status;			
		}
		
		//@1 Link text
		public static function financialTransactionId(event:CompanyEventItem, detail:String, extraArgs:String):String {						
			var linkText:String = "transaction";
			if (extraArgs != null) {
				linkText = extraArgs;
			}
            if (detail in event.details) {
			    return "<a href='event:goFinTx=" + event.details[detail] + "'>" + linkText + "</a>";
            } else {
                return linkText;
            }
		}


		public static function sourcePayrollRun(event:CompanyEventItem, detail:String, extraArgs:String):String {
			//assume they don't care about the guid
            var linkText:String = "payroll";

			if (extraArgs != null) {
				linkText = extraArgs;
			}
            if (detail in event.details) {
			    return "<a href='event:goSourcePayrollRun=" + event.details[detail] + "'>" + linkText + "</a>";
            } else {
                return linkText;
            }
		}

		public static function payrollRun(event:CompanyEventItem, detail:String, extraArgs:String):String {
			//assume they don't care about the guid
            var linkText:String = "payroll";

			if (extraArgs != null) {
				linkText = extraArgs;
			}
            if (detail in event.details) {
			    return "<a href='event:goPayrollRun=" + event.details[detail] + "'>" + linkText + "</a>";
            } else {
                return linkText;
            }
		}
		
		public static function strikeReason(event:CompanyEventItem, detail:String):String {
			if (event.details["ManualStrikeReasonDescription"] != null) {
				return event.details[detail] + ": " + event.details["ManualStrikeReasonDescription"];
			} else {
				return event.details[detail] + " (" + financialTransactionId(event,"FinancialTransactionId","transaction") + ")";
			}
		}
		
		public static function phone(event:CompanyEventItem, detail:String):String {
			var phoneFormat:PhoneFormatter = new PhoneFormatter();
			return phoneFormat.format(event.details[detail]);
		}
		
		public static function date(event:CompanyEventItem, detail:String):String {
			
			var dateFormat:DateFormatter = new DateFormatter();		
			dateFormat.formatString = SAP.instance.configuration.dateFormatShort;		
			return  dateFormat.format(event.details[detail]);
		}

        public static function currency(event:CompanyEventItem, detail:String):String {
            return SAPCurrencyFormatter.currencyFormatter.format(event.details[detail]);
        }
		
		//to override for default behavior
		public static function text(event:CompanyEventItem, detail:String):String {
			return event.details[detail];
		}

	}
}