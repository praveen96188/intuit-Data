package psp.sap.validators
{
	import flash.events.IEventDispatcher;

    import mx.events.PropertyChangeEvent;
    import mx.events.ValidationResultEvent;
	import mx.formatters.NumberFormatter;
	import mx.utils.StringUtil;
	import mx.validators.DateValidator;
	import mx.validators.NumberValidator;
    import mx.validators.RegExpValidator;
    import mx.validators.StringValidator;
	import mx.validators.Validator;
	
	public class SAPValidators
	{
		public function SAPValidators()
		{
			throw new Error("SAPValidators is a static class.  Instances should not be created.");
		}
		
		private static var numberFormatter:NumberFormatter = new NumberFormatter();
		private static var numberRangerError:String = "\n\nThe value entered must be between {0} and {1}.";
		private static const NUMBER_RANGE_ERROR_MIN_ONLY:String = "\n\nThe value entered must be greater than {0}.";
		private static const PRECISION_ERROR:String = "This input field supports a decimal precision of up to {0} digits.";
		private static const DECIMAL_ENTRY_ERROR:String = "The value entered cannot include decimal entry.  Only integer values are permitted.";
        public static const TIME_REGEX:String = "^([01]?\\d|[2][0-3]):[0-5]\\d$";
		
		public static function createDateValidator(source:Object = null, property:String = null, required:Object = null, daysBeforeAllowed:Object = null, daysAfterAllowed:Object = null, fromValidationDate:Object = null, daysBeforeAllowedErrorMessage:Object = null, daysAfterAllowedErrorMessage:Object = null):SAPDateValidator {
			
			var dateValidator:SAPDateValidator = new SAPDateValidator();
			setProperty(dateValidator, "source", source);
			setProperty(dateValidator, "trigger", source);
			setProperty(dateValidator, "property", property);
			setProperty(dateValidator, "required", required);
			setProperty(dateValidator, "daysBeforeAllowed", daysBeforeAllowed); //Default is 45
			setProperty(dateValidator, "daysAfterAllowed", daysAfterAllowed); //Default is 0
			setProperty(dateValidator, "fromValidationDate", fromValidationDate); //Default is today
			setProperty(dateValidator, "daysBeforeAllowedErrorMessage", daysBeforeAllowedErrorMessage); //Default is dynamic
			setProperty(dateValidator, "daysAfterAllowedErrorMessage", daysAfterAllowedErrorMessage); //Default is dynamic
			
			return dateValidator;
		}

       public static function createRegExValidator(source:Object, fieldToValidate:String, isRequired:Boolean, regExpression:String, trigger:IEventDispatcher, errorString:String):RegExpValidator {
        var regExpValidator:RegExpValidator = new RegExpValidator();
        regExpValidator.source = source;
        regExpValidator.property = fieldToValidate;
        regExpValidator.required = isRequired;
        regExpValidator.expression = regExpression;
        regExpValidator.trigger = trigger;
        regExpValidator.noMatchError = errorString;
        return regExpValidator;
    }

        public static function createTimeValidator(source:Object, fieldToValidate:String, isRequired:Boolean, trigger:IEventDispatcher):RegExpValidator {
            return createRegExValidator(source,fieldToValidate,isRequired, TIME_REGEX, trigger,"Please enter time in 24 Hr (HH:mm) format.");
        }
		
        public static function createDefaultDatePropertyChangeValidator(source:Object = null, property:String = null, required:Object = null):DateValidator {
            var dateValidator:DateValidator = createDefaultDateValidator(source, property, required);
            setProperty(dateValidator,"triggerEvent",PropertyChangeEvent.PROPERTY_CHANGE);
            return dateValidator;
        }
		public static function createDefaultDateValidator(source:Object = null, property:String = null, required:Object = null):DateValidator {
			
			var dateValidator:DateValidator = new DateValidator();
			setProperty(dateValidator, "source", source);
			setProperty(dateValidator, "trigger", source);
			setProperty(dateValidator, "property", property);
			setProperty(dateValidator, "required", required);			
			
			return dateValidator;
		}

		public static function createEinValidator(source:Object = null, property:String = null, required:Object = null):SAPEinValidator {
			
			var einValidator:SAPEinValidator = new SAPEinValidator();
			setProperty(einValidator, "source", source);
			setProperty(einValidator, "trigger", source);
			setProperty(einValidator, "property", property);
			setProperty(einValidator, "required", required);															
			
			return einValidator;
		}

        public static function createSsnValidator(source:Object = null, property:String = null, required:Object = null):SAPSsnValidator {
			var ssnValidator:SAPSsnValidator = new SAPSsnValidator();
			setProperty(ssnValidator, "source", source);
			setProperty(ssnValidator, "trigger", source);
			setProperty(ssnValidator, "property", property);
			setProperty(ssnValidator, "required", required);

			return ssnValidator;
        }
		
		public static function createRtnNumberValidator(source:Object = null, property:String = null, required:Object = null):SAPRtnNumberValidator {
			
			var rtnNumberValidator:SAPRtnNumberValidator = new SAPRtnNumberValidator();
			setProperty(rtnNumberValidator, "source", source);
			setProperty(rtnNumberValidator, "trigger", source);
			setProperty(rtnNumberValidator, "property", property);
			setProperty(rtnNumberValidator, "required", required);															
			
			return rtnNumberValidator;
		}
		
		/**
		 * Creates a NumberValidator using the specificed values for the named property.  The created
		 * validator will have the SAP default error messages applied to its relevant properties.
		 * 
		 * If null is passed for an argument, that property's value is not set and the NumberValidator's 
		 * default settings are used for that property.
		 * 
		 * To intentionally null out a default value on the validator, the property must be set explicitly on
		 * the returned instance.
		 *    // admittedly non-sensical example  
		 *    i.e. SAPValidators.createNumberValidator(...).precision = null;
		 */
		public static function createNumberValidator(source:Object = null, property:String = null, required:Object = null, minValue:Object = null, maxValue:Object = null, allowNegative:Object = null, precision:Object = null):NumberValidator {
			
			var numberValidator:NumberValidator = new SAPNumberValidator();
			setProperty(numberValidator, "source", source);
			setProperty(numberValidator, "property", property);
			setProperty(numberValidator, "required", required);
			setProperty(numberValidator, "minValue", minValue);
			if(maxValue != null){
				setProperty(numberValidator, "maxValue", maxValue);
			}
			setProperty(numberValidator, "allowNegative", allowNegative);
			setProperty(numberValidator, "precision", precision);
			
			updateNumberValidatorErrorMessage(numberValidator);			
			
			var precisionError:String = null;
			if (Number(numberValidator.precision) > 0)
				precisionError = StringUtil.substitute(PRECISION_ERROR, (Number(numberValidator.precision)));
			else if (Number(numberValidator.precision) <= 0)
				precisionError = DECIMAL_ENTRY_ERROR;  
	
			numberValidator.precisionError = precisionError;
			

			return numberValidator;			
		}
		
		public static function updateNumberValidatorErrorMessage(numberValidator:NumberValidator):void {
			var errorMsg:String = "";
			if ((numberValidator.minValue != null && !isNaN(Number(numberValidator.minValue))) &&
				(numberValidator.maxValue != null && !isNaN(Number(numberValidator.maxValue)))) {
				errorMsg = StringUtil.substitute(	numberRangerError, 	
													numberFormatter.format(numberValidator.minValue), 
													numberFormatter.format(numberValidator.maxValue));
			}
			else if ((numberValidator.minValue != null && !isNaN(Number(numberValidator.minValue))) && 
					 (numberValidator.maxValue != null && isNaN(Number(numberValidator.maxValue)))) {
				errorMsg = StringUtil.substitute(	NUMBER_RANGE_ERROR_MIN_ONLY, 	
													numberFormatter.format(numberValidator.minValue));				
			}
														
			numberValidator.lowerThanMinError = "The amount entered is too small." + errorMsg;
			numberValidator.exceedsMaxError = "The number entered is too large." + errorMsg;
			numberValidator.negativeError = "The amount may not be negative." + errorMsg;
		}
		
		public static function createStringValidator(source:Object = null, property:String = null, required:Object = null, minLength:Object = null, maxLength:Object = null):StringValidator {
			var stringValidator:StringValidator = new SAPStringValidator();
			
			setProperty(stringValidator, "source", source);
			setProperty(stringValidator, "property", property);
			setProperty(stringValidator, "required", required);
			setProperty(stringValidator, "minLength", minLength);
			setProperty(stringValidator, "maxLength", maxLength);
			
			return stringValidator;
		}
		
		public static function createRequiredFieldValidator(source:Object = null, property:String = null, enabled:Object = null):Validator {
			var validator:Validator = new ValidatorEx();
			validator.required = true;
			
			setProperty(validator, "source", source);
			setProperty(validator, "property", property);
			setProperty(validator, "enabled", enabled);
			
			return validator;
		}
		
		public static function createSAPStartEndDateValidator(source:Object, trigger:IEventDispatcher, startDateProperty:String, endDateProperty:String, required:Boolean = false):SAPStartEndDateValidator { 
			var startEndDateValidator:SAPStartEndDateValidator = new SAPStartEndDateValidator();
			startEndDateValidator.source = source;
			startEndDateValidator.trigger = trigger;
			startEndDateValidator.startDateProperty = startDateProperty;
			startEndDateValidator.endDateProperty = endDateProperty;
			startEndDateValidator.required = required;
			return startEndDateValidator;
		}
		
		private static function setProperty(object:Object, property:String, value:Object):void {
			if (value != null)
				object[property] = value;
		}
		
		/**
		 * Replaces mx.validators.Validator.validateAll to allow for suppressiong of validation events.  Otherwise,
		 * functionally equivalent.
		 */
		public static function validateAll(validators:Array, suppressEvents:Boolean = true):Array {
			var failures:Array = [];
			for each (var validator:Validator in validators) {
				if (validator.enabled) {
					var resultEvent:ValidationResultEvent = validator.validate(null, suppressEvents); 
					if (resultEvent.type != ValidationResultEvent.VALID) {
						failures.push(resultEvent);
					}
				}
			}
			
			return failures;
		}

        public static function createSAPFromAndToIdValidator(source:Object, trigger:IEventDispatcher, fromIdProperty:String, toIdProperty:String, required:Boolean = false):SAPFromAndToValueValidator {
            var fromAndToValueValidator:SAPFromAndToValueValidator=new SAPFromAndToValueValidator();
            fromAndToValueValidator.source = source;
			fromAndToValueValidator.trigger = trigger;
            fromAndToValueValidator.fromValueProperty = fromIdProperty;
			fromAndToValueValidator.toValueProperty = toIdProperty;
			fromAndToValueValidator.required = required;
            return fromAndToValueValidator;
        }

        public static function createSAPNotSameValidator(source:Object, trigger:IEventDispatcher, property1:String, property2:String, required:Boolean = false):SAPNotSameValidator {
            var debitAndCreditAccountValidator:SAPNotSameValidator=new SAPNotSameValidator();
            debitAndCreditAccountValidator.source = source;
			debitAndCreditAccountValidator.trigger = trigger;
            debitAndCreditAccountValidator.property1 = property1;
			debitAndCreditAccountValidator.property2 = property2;
			debitAndCreditAccountValidator.required = required;
            return debitAndCreditAccountValidator;
        }

        public static function createAtLeastOneFieldValidator(source:Object, trigger:IEventDispatcher, property1:String,  property2:String, required:Boolean = false):AtLeastOneFieldValidator {
            var validator:AtLeastOneFieldValidator = new AtLeastOneFieldValidator();
            validator.source = source;
            validator.trigger = trigger;
            validator.field1Property = property1;
            validator.field2Property = property2;
            validator.required = required;
            return validator;
        }

        public static function createBooleanValidator(source:Object = null, property:String = null, required:Object = null, requiredValue:Boolean = true):BooleanValidator {
            var validator:BooleanValidator = new BooleanValidator();
            setProperty(validator, "source", source);
            setProperty(validator, "property", property);
            setProperty(validator, "required", required);
            setProperty(validator,  "requiredValue", requiredValue);
            return validator;
        }
	}
}
