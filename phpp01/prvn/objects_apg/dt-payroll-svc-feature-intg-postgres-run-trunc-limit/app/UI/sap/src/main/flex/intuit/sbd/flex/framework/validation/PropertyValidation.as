package intuit.sbd.flex.framework.validation
{
	import flash.utils.Dictionary;
	
	import mx.events.ValidationResultEvent;
	import mx.validators.Validator;
	
	public class PropertyValidation implements IPropertyValidation
	{
		private var mPropertySource:Object;
		private var mValidators: PropertyValidatorCollection;

		/**
		 * For each property the associated ValidationResultEvent instance.
		 */
		private var mPropValidationMap: Dictionary = new Dictionary(true);
		
		public function PropertyValidation(propertySource:Object):void {
			if (propertySource == null)
				throw new Error("propertySource cannot be null");
			mPropertySource = propertySource;
			
			mValidators = new PropertyValidatorCollection(mPropertySource, onPropertyValidation);
		}
		
		public function get propertyValidationMap(): Dictionary {
			return mPropValidationMap;
		}
		
		public function get validators(): PropertyValidatorCollection {
			return mValidators;
		}
		
		protected function onPropertyValidation(result: ValidationResultEvent): void {
			if (result.type == ValidationResultEvent.INVALID) {
				mPropValidationMap[result.field] = result;
			}
			else {
				mPropValidationMap[result.field] = null;
			}			
		}
		
		public function getValidationResultEvent(property: String): ValidationResultEvent {
			var result: ValidationResultEvent =
				mPropValidationMap[property] as ValidationResultEvent;
				
			if (result == null)
				result = new ValidationResultEvent(ValidationResultEvent.VALID);
			
			return result;
		}
		
		[Bindable("isValidChanged")]
		public function get isValid():Boolean {
			for each (var validator: Validator in this.validators) {
				validator.validate();
			}
			return true;			
		}		
	}
}