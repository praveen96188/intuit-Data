/**
 * User: dweinberg
 * Date: 2/21/13
 * Time: 1:59 PM
 */
package psp.sap.validators {
    import mx.validators.ValidationResult;
    import mx.validators.Validator;

    public class BooleanValidator extends Validator {

        public var requiredValue:Boolean = true;

        public static function validateBoolean(validator:BooleanValidator, value:Boolean, baseField:String = null):Array {
            var results:Array = [];
            if (!value) {
                results.push(new ValidationResult(true));
                return results;
            }

            return results;
        }

        public function BooleanValidator() {
            super();

        }

        override protected function doValidation(value:Object):Array {
            var results:Array = super.doValidation(value);

            // Return if there are errors
            // or if the required property is set to false and length is 0.
            var val:String = value ? String(value) : "";
            if (results.length > 0 || ((val.length == 0) && !required)) {
                return results;
            } else {
                return BooleanValidator.validateBoolean(this, (value as Boolean) == requiredValue, null);
            }
        }
    }
}
