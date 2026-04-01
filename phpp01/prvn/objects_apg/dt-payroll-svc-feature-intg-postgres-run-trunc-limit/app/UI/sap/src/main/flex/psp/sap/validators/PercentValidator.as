package psp.sap.validators
{
	import mx.validators.NumberValidator;
	import mx.validators.ValidationResult;
	import mx.validators.Validator;
	
	import psp.sap.application.SAP;
	
	//validates some text is in a percent format
	//and that the rate (i.e. percent/100.) is within
	//some bounds
	//sort of a bastard child of a text and number validator
	public class PercentValidator extends Validator
	{
		
		public var maxNumber:Number;
		public var minNumber:Number;
		
		public function PercentValidator()
		{
			super();			
		}
		
		override protected function doValidation(value:Object):Array
	    {
	        var results:Array = super.doValidation(value);
				
	        var val:String = value ? String(value) : "";
	        if (results.length > 0 || ((val.length == 0) && !required))
	            return results;
	        else
	            return isValidPercent(value as String);
	    }
	    
	    private function isValidPercent(value:String):Array {
	    	//left recursive grammar so i'll test to see if it ends with a percent
	    	//then test if it's a number
	    	//bam!
	    	var results:Array = [];
	    	
	    	if (! value.match(/^.+%$/)){
	    		results.push(new ValidationResult(
	    			true,
	    			"",
	    			"notPercent",
	    			"Percentage must end in a percent sign"	));
	    	}
	    	
	    	if (results.length > 0) {
	    		return results;
	    	}
	    	
	    	//get the rest
	    	var numberPart:String = value.substr(0,value.length-1);
	    	
	    	
	    		
	    	var numberValidator:NumberValidator = new NumberValidator();
	    	numberValidator.allowNegative = false;
	    	numberValidator.maxValue = (isNaN(maxNumber) ? Number.MAX_VALUE : maxNumber*100);
	    	numberValidator.minValue = (isNaN(minNumber) ? Number.MIN_VALUE : minNumber*100);	    	
	    	numberValidator.lowerThanMinError = "The value must be greater than " + SAP.instance.formatPercentage(minNumber);
	    	numberValidator.exceedsMaxError = "The value must be less than " + SAP.instance.formatPercentage(maxNumber);
	    	
	    	return NumberValidator.validateNumber(numberValidator,numberPart,this.property);
	    	
	    }
		

	}
}