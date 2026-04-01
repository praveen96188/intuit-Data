package psp.sap.validators
{
    import mx.core.UIComponent;
    import mx.validators.DateValidator;
	import mx.validators.ValidationResult;
	
	import psp.sap.application.SAP;

	public class SAPDateValidator extends DateValidator
	{
		private static const millisecondsPerDay:int = 1000 * 60 * 60 * 24;
		
		public var daysBeforeAllowed:Number;
		public var daysAfterAllowed:Number;
		public var fromValidationDate:Date;
		
		public var daysBeforeAllowedErrorMessage:String;
		public var daysAfterAllowedErrorMessage:String;
		
		public function SAPDateValidator()
		{
			super();
			
			//Defaults
			daysBeforeAllowed = 45;
			daysAfterAllowed = 0;
			fromValidationDate = SAP.instance.PSPDate;
			daysBeforeAllowedErrorMessage = "";
			daysAfterAllowedErrorMessage = "";
			wrongLengthError = "Please enter the date in one of the following:\n" + 
							   "MMDDYY or MMDDYYYY\n" +							   
							   "MM-DD-YY or MM-DD-YYYY\n" +
							   "MM.DD.YY or MM.DD.YYYY\n" +
							   "MM/DD/YY or "; // MM/DD/YYYY is added here by the date validator
		}
		
			//The overriden validation function
		override protected function doValidation(value:Object):Array
	    {
			var results:Array = super.doValidation(value);
			
			// Return if there are errors
			// or if the required property is set to false and length is 0.
			var val:String = value ? String(value) : "";
			if (results.length > 0 || ((val.length == 0) && !required))
				return results;
			else
			    return doSpecialValidaton(value);
	    }
	    
	    //Implementation of an isValid function
	    public function isDateValid():Boolean {
            var value:Object = getValueFromSource();   
            var results:Array = doValidation(value);
            return (results.length == 0);
	    }
         
	 
	 /**
	 * Our validation routine
	 * Checks to make sure that the date is not greater than today and that the 
	 * strike date is not greater than a year in the past
	 */
	    private function doSpecialValidaton(value:Object):Array
	    {
	    	
	    	var results:Array = [];
	    	
	    	if(value is String)
	    	{
	    		value = new Date(value);
	    	}
	    	
	    	if(value is Date)
	    	{
		    	var date:Date = value as Date;
				date.setHours(0, 0, 0, 0);
							
				var endDate:Date = rangeEndDate;				
				if(daysAfterAllowed >= 0 && date.getTime() > endDate.getTime()) {
					
					if(daysAfterAllowed == 0)
					{
						results.push(new ValidationResult(
							true, 
							"",
							"greaterThanDaysAfterAllowed",
							(daysAfterAllowedErrorMessage == "") ? "Date cannot be in the future." : daysAfterAllowedErrorMessage));
					} else {
						results.push(new ValidationResult(
							true, 
							"",
							"greaterThanDaysAfterAllowed",
							(daysAfterAllowedErrorMessage == "") ? "Date cannot be greater than "
							 + daysAfterAllowed.toString() + " days in the future." : daysAfterAllowedErrorMessage));
					}
				}
				
				
				
				var startDate:Date = rangeStartDate;			
				if(daysBeforeAllowed >= 0 && date.getTime() < startDate.getTime()) {
					
					if(daysBeforeAllowed == 0)
					{
						results.push(new ValidationResult(
						true, "","lessThanDaysBeforeAllowed",
						(daysBeforeAllowedErrorMessage == "") ? "Date cannot be in the past." : daysBeforeAllowedErrorMessage));
					} else if(daysBeforeAllowed == 365) {
						results.push(new ValidationResult(
						true, "","lessThanDaysBeforeAllowed",
						(daysBeforeAllowedErrorMessage == "") ? "Date cannot be greater than 1 year in the past."
						 : daysBeforeAllowedErrorMessage));
					} else {
						results.push(new ValidationResult(
						true, "","lessThanDaysBeforeAllowed",
						(daysBeforeAllowedErrorMessage == "") ? "Date cannot be greater than "
						 + daysBeforeAllowed.toString() + " days in the past." : daysBeforeAllowedErrorMessage));
					}
				}
	    	}
	    	
			return results;
	    }
	    
	    private function get rangeEndDate():Date {
            var endDate:Date = new Date();
	    	endDate.time = fromValidationDate.time;
			endDate.time += (millisecondsPerDay * daysAfterAllowed);	    	
	    	endDate.setHours(0, 0, 0, 0);
	    	return endDate;
	    }
	    
	    private function get rangeStartDate():Date {
			var startDate:Date = new Date();
			startDate.time = fromValidationDate.time;
			startDate.setHours(0, 0, 0, 0);
			startDate.time -= (millisecondsPerDay * daysBeforeAllowed);
			return startDate;	    	
	    }
	    
	    [Bindable ("propertyChange")]
	    public function get validDateRange():Object {  		
	   		return {rangeStart: rangeStartDate, rangeEnd: rangeEndDate};	    	
	    }

        override public function set enabled(value:Boolean):void {
            super.enabled = value;
            clearListener();
        }

        public function clearListener():void {
            if (!required || !enabled) {
                if (listener != null && listener is UIComponent) {
                    UIComponent(listener).errorString = "";
                }
            } else {
                validate();
            }
        }
	}
}