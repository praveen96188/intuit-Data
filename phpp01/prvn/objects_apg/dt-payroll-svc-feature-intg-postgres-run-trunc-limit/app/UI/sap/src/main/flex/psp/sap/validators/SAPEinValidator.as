package psp.sap.validators
{
	import mx.validators.StringValidator;
	import mx.validators.ValidationResult;
	
	public class SAPEinValidator extends StringValidator
	{
	
	    public function SAPEinValidator()
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
	            return isEIN(value as String);
	    }
	
	    private function isEmptyText(str:String):Boolean {
	        return str == null || str.replace(" ", "").length == 0;
	    }
	
	    private function isEIN(str:String):Array {
	        var results:Array = [];
	        if(!(isEmptyText(str))) {
	            var einReg:RegExp = /^(\d{9}|\d{2}-\d{7})$/;
	            var result:Object = einReg.exec(str);
	            if(result == null){
		            results.push(new ValidationResult(
		                    true,
		                    "",
		                    "notEIN",
		                    "EIN must be 9 digits"));
		        }
	        }
	        return results;
	    }
	
	}
}