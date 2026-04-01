package psp.sap.validators
{
	import mx.collections.ArrayCollection;
	import mx.validators.ValidationResult;
	import mx.validators.Validator;
	
	public class AllowedValuesValidator extends Validator	
	{
		
		public var allowedValues:ArrayCollection;
		public var invert:Boolean;
		public var errorMessage:String=null;
		
		public function AllowedValuesValidator()		
		{
			super();
		}
		
	    override protected function doValidation(value:Object):Array
	    {
	        var results:Array = super.doValidation(value);
				
			// Return if there are errors
	        // or if the required property is set to false and length is 0.
	        var val:String = value ? String(value) : "";
	        if (results.length > 0 || ((val.length == 0) && !required))
	            return results;
	        else
	            return isAllowedValue(value);
	    }		
	    
	    private function isAllowedValue(value:Object):Array {
	    	var results:Array = [];
	    	if ((! allowedValues.contains(value) && ! invert) ||
	    			(allowedValues.contains(value) && invert)) {
		    		results.push(new ValidationResult(
		    			true,
		    			"",
		    			"notAllowedValue",
		    			errorMessage != null ? errorMessage : "Value not allowed")); 	    			
	    	}
			return results;	    	
	    }

	}
}