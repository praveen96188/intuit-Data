package psp.sap.viewmodel
{
	import mx.events.PropertyChangeEvent;
	import mx.validators.NumberValidator;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.model.PayrollBillingTransactions;
	import psp.sap.validators.SAPDateValidator;
	import psp.sap.validators.SAPValidators;
	import psp.sap.viewmodel.events.ViewModelUserConfirmationEvent;

	public class PayrollAddRedebitViewModel extends PayrollMultiItemEnteringPageViewModel
	{
		public static const DAY_MILLIS:Number = 1000 * 60 * 60 * 24;		
		public static const FUTURE_DATE:String = "futureDate";
		
		public function PayrollAddRedebitViewModel()
		{
			super();
			this.label = CompanyInspectorPageEnum.PAYROLL_ADD_REDEBIT;			
			addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onPropertyChange, false, 0, true);
		}
		
		override protected function createDateValidator(billingTransactions:PayrollBillingTransactions):SAPDateValidator {
			// an agent can select any day from today forward as a valid date
			return SAPValidators.createDateValidator(billingTransactions, "initiationDate", false, 0, 365, SAP.instance.PSPDate);
		}
		
		private var lastDateValue:Date = null;
		
		override public function set date(value:String):void {
			lastDateValue = dateValue;
			super.date = value;
		}
		
		/**
		 * Generate an event if the redebit date is set to greater than 5 days in the future.
		 * Only nag if going from a 'legit' date to a 'suspect' date. Not from suspect to suspect date.
		 * (That's annoying.)
		 */
		protected function onPropertyChange(e:PropertyChangeEvent):void {
			if (e.property == "date") {
				var oldDate:Date = getDate(e.oldValue)
				var newDate:Date = getDate(e.newValue);
				var fiveDaysFromNow:Number = SAP.instance.PSPDate.getTime() + (DAY_MILLIS * 5); 
                
				if (newDate != null
					// newDate is more than 5 days out
					&& (newDate.getTime() > fiveDaysFromNow) && (newDate.getTime() != previousWarnDate.getTime())
					// newDate is greater than old date and old date was not > 5 days out (no repeat-nags)
					&& ((oldDate == null) || (newDate.getTime() > oldDate.getTime()) && (oldDate.getTime() < fiveDaysFromNow))) {
					
					previousWarnDate = newDate;
					var confirmationEvent:ViewModelUserConfirmationEvent = 
							ViewModelUserConfirmationEvent.createEvent(EVENT_DATE_WARNING, dateFormatter.format(lastDateValue));
					dispatchEvent(confirmationEvent);
				}
				else {
					if ((newDate != null) && (oldDate != null) && (newDate.getTime() > fiveDaysFromNow)) {
						previousWarnDate = newDate;
					}
				}				
			}
		}

		private function getDate(value:Object):Date {
			if (value == null) 
				return null;
			
			var dateStr:String = dateFormatter.format(value);
			if (dateFormatter.error)
				return null;
			
			var date:Date = SAP.instance.PSPDate;
			date.time = Date.parse(dateStr);
			return date;
		}
		
		override protected function loadModelData():void {			
         	loadCount = 2;
            super.loadModelData();
            SAP.instance.payrollRunService.findPayrollUncollectedBalances(
         																	company.companyId,
                                                                            company.sourceSystemCd,
         																	payrollRun.sourcePayRunId,
         																	createLoadModelDataResponder(onLoadPayrollsSucceeded));
         }
		
		override protected function createACHValidator(source:Object, property:String, maxValue:Object = null):NumberValidator {			
			return SAPValidators.createNumberValidator(source, property, false, 0.00, maxValue, false, 2);	
		}
		
		override protected function createNonACHValidator(source:Object, property:String, maxValue:Object = null):NumberValidator {			
			return SAPValidators.createNumberValidator(source, property, false, 0.00, null, false, 2);	
		}		 					
	}
}