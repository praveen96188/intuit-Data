package psp.sap.validators
{
	import mx.formatters.DateFormatter;
	import mx.validators.CreditCardValidator;
	import mx.validators.IValidatorListener;
	import mx.validators.ValidationResult;
	import mx.validators.Validator;


	/*
	SAPStartDateEndDateValidator
	
	The purpose of this validator is to validate whether a start date / end date pair is correct by determining
	whether the start date is less than the end date. There are a few minor differences in this validator versus
	others in the setup process, though.
	
	There are 2 listeners you have to set in this validator. The startDateListener and the endDateListener.
	These are the UI date fields that will turn red and display an error if the data bound to it is invalid.
	
	There are also 2 property fields that have to be set (startDateProperty and endDateProperty).
	These are the properties of the source object (typically viewModel) that are going to be validated.
	
	This validator is assuming that the properties will be off of the same object, so there is only one 
	"source" object (which should be the viewModel).
	
	*/



	public class SAPStartEndDateValidator extends Validator
	{
		
		private var mListeners:Array = [];
				
		//View model properties
		public var startDateProperty:String;
		public var endDateProperty:String;
	
		
		public function SAPStartEndDateValidator()
		{
			super();
			subFields = ["startDate", "endDate"];
		}
		
		
		/**
		 *  @private
		 *  Storage for the startDateListener property.
		 */
		private var _startDateListener:IValidatorListener;
	
		/** 
		 *  The component that listens for the validation result
		 *  for the start date subfield. 
		 *  If none is specified, use the value specified
		 *  to the <code>source</code> property.
		 */
		public function get startDateListener():IValidatorListener
		{
			return _startDateListener;
		}
		
		/**
		 *  @private
		 */
		public function set startDateListener(value:IValidatorListener):void
		{
			if (_startDateListener == value)
				return;
				
			removeListenerHandler();
			
			_startDateListener = value;
			
			addListenerHandler();
		}
		

		private var _endDateListener:IValidatorListener;
	

		public function get endDateListener():IValidatorListener
		{
			return _endDateListener;
		}
		

		public function set endDateListener(value:IValidatorListener):void
		{
			if (_endDateListener == value)
				return;
				
			removeListenerHandler();
			
			_endDateListener = value;
			
			addListenerHandler();
		}
		
		
		/*
			When the validator tries to get a list of listeners, return both.
		*/
		
		override protected function get actualListeners():Array
		{
			var results:Array = [];
			
			var startDateResult:Object = _startDateListener;		
			results.push(startDateResult);
			
			if (startDateResult is IValidatorListener)
				IValidatorListener(startDateResult).validationSubField = "startDate";
				
			var endDateResult:Object = _endDateListener;
			results.push(endDateResult);
			
			if (endDateResult is IValidatorListener)
				IValidatorListener(endDateResult).validationSubField = "endDate";
					
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
			
			if (source && startDateProperty)
			{
				value.startDate = source[startDateProperty];
				useValue = true;
			}
			
			if (source && endDateProperty)
			{
				value.endDate = source[endDateProperty];
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
			    return doStartEndValidation(value);
	    }	  
		
		
		/*
			Custom validation to make sure that endDate is not less than startDate
		*/
		protected function doStartEndValidation(value:Object):Array
	    {
	    	var results:Array = [];
	    	
	    	var startDateString:String = "";
	    	var endDateString:String = "";
	    	
	    	var mDateFormatter:DateFormatter = new DateFormatter();
	    	
	    	try 
			{
				startDateString = (value.startDate);
			}
			catch(e:Error)
			{
				// Error out cause the value object was not rendered correctly (see getValueFromSource())
				throw new Error("Missing startDate on object.");
			}
			
			try 
			{
				endDateString = (value.endDate);
			}
			catch(f:Error)
			{
				// Error out cause the value object was not rendered correctly (see getValueFromSource())
				throw new Error("Missing endDate on object.");
			}
	    	
	    	
	    	//Check format
	    	var startTime:Number = Date.parse(mDateFormatter.format(startDateString));
	    	var endTime:Number = Date.parse(mDateFormatter.format(endDateString));
	    		
	    	if(startTime > endTime)
	    	{
					results.push(new ValidationResult(
						true, 
						"startDate",
						"startDateGreaterThanEndDate",
						"The start date cannot be later than the end date."));
					results.push(new ValidationResult(
						true, 
						"endDate",
						"startDateLessThanEndDate",
						"The end date cannot be earlier than the start date."));
	    	}
	    		
	    	return results;
	    }
		
	}
}