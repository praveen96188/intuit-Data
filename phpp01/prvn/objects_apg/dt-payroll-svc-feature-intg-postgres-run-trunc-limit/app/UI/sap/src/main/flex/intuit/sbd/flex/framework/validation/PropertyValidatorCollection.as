package intuit.sbd.flex.framework.validation
{
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.events.PropertyChangeEvent;
	import mx.events.ValidationResultEvent;
	import mx.validators.StringValidator;
	import mx.validators.Validator;

	public class PropertyValidatorCollection extends ArrayCollection
	{
		private var mOwner: Object;
		private var mValidationHandler: Function;
		private var mPropertyHandlerMap: Dictionary = new Dictionary(true);
		
		public function PropertyValidatorCollection(owner: Object, validationHandler: Function, source:Array=null)
		{
			super(source);
			
			if (owner == null)
				throw new Error("owner cannot be null");
			mOwner = owner;
			
			if (validationHandler == null)
				throw new Error("validationHandler cannot be null");
			mValidationHandler = validationHandler;
		}
		
		/**
		 * Creates a mx.validators.StringValidator that watches the specified property on the source
		 * owner object.  The validationHandler function is registered as a listener of the 
		 * mx.events.ValidationResultEvent.INVALID and mx.events.ValidationResultEvent.VALID
		 * events.
		 *  
		 * @param propertyName
		 * @param maxLenth
		 * @param minLength
		 * @param required
		 * @param triggerEvent
		 * 
		 */		
		public function addStringValidator(propertyName: String, minLength:int=0, maxLength:int=2000, required:Boolean=false,triggerEvent:String=PropertyChangeEvent.PROPERTY_CHANGE): void {
			
			var validator:StringValidator = new StringValidator();
			
			validator.source = mOwner;
			validator.property = propertyName;
			validator.required = required;
			validator.triggerEvent = triggerEvent;
			validator.minLength = minLength;
			validator.maxLength = maxLength;
			
			this.addItem(validator);		
		}
		
		override public function addItemAt(item:Object, index:int): void {
			
			if ((item is Validator) == false) {
				throw new Error("item must be of type Validator");				
			}
			
			super.addItemAt(item, index);
			
			var validator: Validator = Validator(item);
			
			var propertyValidationHandler: PropertyValidationHandler = new PropertyValidationHandler(validator.property, mValidationHandler);
			mPropertyHandlerMap[validator.property] = propertyValidationHandler;			
			
			validator.addEventListener(ValidationResultEvent.VALID, propertyValidationHandler.onValidationHandler, false, 0, true);
			validator.addEventListener(ValidationResultEvent.INVALID, propertyValidationHandler.onValidationHandler, false, 0, true);
		}
		
		override public function removeItemAt(index:int): Object {
			
			var validator: Validator = super.removeItemAt(index) as Validator;
			
			var propertyValidationHandler: PropertyValidationHandler = mPropertyHandlerMap[validator.property];
			propertyValidationHandler.delegateFunction = null;
						
			validator.removeEventListener(ValidationResultEvent.INVALID, propertyValidationHandler.onValidationHandler, false);
			validator.removeEventListener(ValidationResultEvent.VALID, propertyValidationHandler.onValidationHandler, false);
			
			return validator;
		}
		
		
		
	}
}