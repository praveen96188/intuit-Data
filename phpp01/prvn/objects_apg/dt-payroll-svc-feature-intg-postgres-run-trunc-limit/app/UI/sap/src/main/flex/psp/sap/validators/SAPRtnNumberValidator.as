package psp.sap.validators
{
	import mx.validators.StringValidator;
	import mx.validators.ValidationResult;
	
	public class SAPRtnNumberValidator extends StringValidator
	{
	
	    public function SAPRtnNumberValidator()
	    {
	        super();
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
	            return isValidRtnNumber(value as String);
	    }
	
	    private function isEmptyText(str:String):Boolean {
	        return str == null || str.replace(" ", "").length == 0;
	    }
	
	    private function isValidRtnNumber(str:String):Array {
	        var results:Array = [];
	        if(!(isEmptyText(str))) {
	        	// make sure the string is 9 digits
	            var rtnNumberReg:RegExp = /^\d{9}$/;
	            var result:Object = rtnNumberReg.exec(str);
	            if(result == null){
		            results.push(new ValidationResult(
		                    true,
		                    "",
		                    "notRtnNumber",
		                    "The routing number must be 9 digits"));
		        }
		        else {
		        	// Run through each digit and calculate the total.
					var n:int = 0;
					for (var i:int = 0; i < str.length; i += 3) {
						n += parseInt(str.charAt(i),     10) * 3
							+  parseInt(str.charAt(i + 1), 10) * 7
					    	+  parseInt(str.charAt(i + 2), 10);
					}
					
					// If the resulting sum is an even multiple of ten (but not zero),
					// the aba routing number is good.
					
					if (n == 0 || n % 10 != 0){
						results.push(new ValidationResult(
		                    true,
		                    "",
		                    "notRtnNumber",
		                    "The routing number entered is invalid"));
					}					  
		        }
	        }
	        return results;
	    }
	
	}
}