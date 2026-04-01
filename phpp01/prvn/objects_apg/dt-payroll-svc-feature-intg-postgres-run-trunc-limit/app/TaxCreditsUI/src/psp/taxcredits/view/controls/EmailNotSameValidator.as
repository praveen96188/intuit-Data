package psp.taxcredits.view.controls {
    import mx.utils.StringUtil;
    import mx.validators.EmailValidator;
    import mx.validators.ValidationResult;

    public class EmailNotSameValidator extends EmailValidator {

        [Bindable] public var otherValue:String;
        [Bindable] public var sameEmailError:String;

        public function EmailNotSameValidator() {
            super();
        }

        private function validateIsSame(value:String):Array {
            var results:Array = [];

            if (StringUtil.trim(value) == StringUtil.trim(otherValue)) {
                results.push(new ValidationResult(
                        true, null, "sameEmail",
                        sameEmailError));
            }

            return results;
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
                return validateIsSame(val);
        }
    }
}