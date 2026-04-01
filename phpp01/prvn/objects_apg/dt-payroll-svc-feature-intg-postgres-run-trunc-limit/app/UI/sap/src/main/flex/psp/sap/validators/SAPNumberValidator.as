package psp.sap.validators
{
	import mx.validators.NumberValidator;
	import mx.validators.ValidationResult;

	public class SAPNumberValidator extends NumberValidator
	{
		public function SAPNumberValidator()
		{
			super();
		}
		
	    /**
	     *  Override the base class <code>doValidation()</code> method 
	     *  to validate a number and if the precision is '0' mark the
	     * 	number as invalid.  Default Flex behavior is to allow
	     * 	integer values of the type 0.00, 1.00, etc. even when
	     *  precision is 0.
	     */
	    override protected function doValidation(value:Object):Array
	    {
	        var results:Array = super.doValidation(value);
			
			// Return if there are errors
			// or if the required property is set to <code>false</code> and length is 0.
			var val:String = value ? String(value) : "";
			if (results.length > 0 || ((val.length == 0) && !required))
				return results;
			else {
				var result:ValidationResult = validatePrecision(val);
				if (result)
					results.push(result);
			    return results;
			}
	    }
	    
	    private function validatePrecision(input:String):ValidationResult {
	   		if (this.precision != null && Number(this.precision) == 0) {
		   		if (input != null && input.length > 0 && input.indexOf(this.decimalSeparator) != -1) {
	   				return new ValidationResult(true, "", "precision", this.precisionError);
	   			}
	   		}	    	
	   		// idiomatic way to express validation passed
	   		return null;
	    }
	    
	    public static function validateNumber(validator:NumberValidator, value:Object, baseField:String):Array {
	   		var results:Array = NumberValidator.validateNumber(validator, value, baseField);
	   		if (results.length > 0)
	   			return results;
	   		

	   		
	   		return results;
	    } 
	}
	
}