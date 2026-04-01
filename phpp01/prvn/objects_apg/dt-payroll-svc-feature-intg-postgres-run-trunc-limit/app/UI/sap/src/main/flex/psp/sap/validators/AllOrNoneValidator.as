package psp.sap.validators
{
	import mx.validators.IValidatorListener;
	import mx.validators.ValidationResult;
	import mx.validators.Validator;


	/*
	AllOrNoneValidator
	
	Given a set of fields {F}, {F} is valid by the AllOrNoneValidator iff either 
		a) for each f:{F}, f is present
		b) for each f:{F}, f is not present
	
	@see also SAPStartEndDateValidator
	*/



	public class AllOrNoneValidator extends Validator
	{
		
		private var mListeners:Array = [];
				
				
				
		//View model properties
		private var mField1Property:String;
		private var mField2Property:String;
		
		//defaults
		public var field2RequiredIf1PresentErrorString:String = "Field 2 is required if Field 1 is present";
		public var field1RequiredIf2PresentErrorString:String = "Field 1 is required if Field 2 is present";
	
		
		public function set field1Property(value:String):void {
			mField1Property = value;
			subFields[0] = value;
		}
		
		public function set field2Property(value:String):void {
			mField2Property = value;
			subFields[1] = value;
		}
		
		public function AllOrNoneValidator()
		{
			super();
			subFields = ["field 1", "field 2"];
		}
		
		
		/**
		 *  @private
		 *  Storage for the startDateListener property.
		 */
		private var _field1Listener:IValidatorListener;
	
		/** 
		 *  The component that listens for the validation result
		 *  for the start date subfield. 
		 *  If none is specified, use the value specified
		 *  to the <code>source</code> property.
		 */
		public function get field1Listener():IValidatorListener
		{
			return _field1Listener;
		}
		
		/**
		 *  @private
		 */
		public function set field1Listener(value:IValidatorListener):void
		{
			if (_field1Listener == value)
				return;
				
			removeListenerHandler();
			
			_field1Listener = value;
			
			addListenerHandler();
		}
		

		private var _field2Listener:IValidatorListener;
	

		public function get field2Listener():IValidatorListener
		{
			return _field2Listener;
		}
		

		public function set field2Listener(value:IValidatorListener):void
		{
			if (_field2Listener == value)
				return;
				
			removeListenerHandler();
			
			_field2Listener = value;
			
			addListenerHandler();
		}
		
		
		/*
			When the validator tries to get a list of listeners, return both.
		*/
		
		override protected function get actualListeners():Array
		{
			var results:Array = [];
			
			var field1Result:Object = _field1Listener;		
			results.push(field1Result);
			
			if (field1Result is IValidatorListener)
				IValidatorListener(field1Result).validationSubField = subFields[0];
				
			var field2Result:Object = _field2Listener;
			results.push(field2Result);
			
			if (field2Result is IValidatorListener)
				IValidatorListener(field2Result).validationSubField = subFields[1];
					
			if (results.length > 0 && listener)
				results.push(listener);
	
			return results;
		}
		
		
		/*
			Create custom value object with both values on it.
		*/
		override protected function getValueFromSource():Object
		{
			var useValue:Boolean = false;
		
			var value:Object = {};
			
			if (source)
			{
				value.field1 = source[subFields[0]];
				useValue = true;
			}
			
			if (source)
			{
				value.field2 = source[subFields[1]];
				useValue = true;
			}
		
			if (useValue)
				return value;
			else
				return super.getValueFromSource();
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
			    return allOrNothingValidation(value);
	    }	  
				
		
		/*
			Custom validation to make sure the all or none invarient holds
		*/
		protected function allOrNothingValidation(value:Object):Array
	    {
	    	var results:Array = [];
	    	
	    	var field1Object:Object = null;
	    	var field2Object:Object = null;
	    	
	    	if ("field1" in value) {
	    		field1Object = value.field1;
	    	}
	    	
	    	if ("field2" in value) {
	    		field2Object = value.field2;
	    	}
	    	
	    	
	    	if (!isEmptyOrNull(field1Object) && isEmptyOrNull(field2Object)) {
	    		results.push(new ValidationResult(
	    			true,
	    			subFields[1],
	    			"field2RequiredIf1Present",
	    			field2RequiredIf1PresentErrorString));
	    			
	    	}
	    	if (isEmptyOrNull(field1Object) && !isEmptyOrNull(field2Object)) {
	    		results.push(new ValidationResult(
	    			true,
	    			subFields[0],
	    			"field1RequiredIf2Present",
	    			field1RequiredIf2PresentErrorString));
	    	}
	    		    		
	    	return results;
	    }
	    
	    private function isEmptyOrNull(val:Object):Boolean {
	    	return (
	    	 val == null ||
	    	 (val is String && ((val as String) == "")) || 
	    	 (val is Number && isNaN(val as Number)));
	    }
		
	}
}

